/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.gl.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.ContainerWrapper;

import com.metsci.glimpse.gl.GLSimpleListener;
import com.metsci.glimpse.gl.GLSimpleListenerAbstract;

// TODO: Think through threading issues?? There are no locks currently.
/**
 * Don't forget the "bottomtotop" layout constraint for MiG, or things will be
 * upside-down from what you probably expect.
 *
 * @author osborn
 */
public class GLLayoutPainter extends GLSimpleListenerAbstract implements ComponentWrapper, ContainerWrapper
{
    public ReentrantLock lock = new ReentrantLock( );

    public static final int DEFAULT = -1;
    public static final int DEFAULT_WIDTH = 100;
    public static final int DEFAULT_HEIGHT = 100;

    private String name = "";

    private int x;
    private int y;
    private int width;
    private int height;

    private boolean hovered = false;
    private boolean visible = true;

    private static boolean visualPadding = false;
    private static boolean zeroMinSize = true;

    private GLLayoutManger layoutManager = new GLLayoutManagerMig( );
    private Object layoutData = null;
    private boolean validLayout = false;
    private boolean validListenerShapes = false;

    private GLLayoutPainter layoutParent;
    private List<GLLayoutPainter> layoutChildren;

    private List<Member> members; // includes children
    private List<Member> newMembers;
    private List<Member> deletedMembers;

    private List<GLLayoutUpdateListener> updateListeners;

    public GLLayoutPainter( GLLayoutPainter parent )
    {
        this.layoutParent = parent;
        this.layoutChildren = new CopyOnWriteArrayList<GLLayoutPainter>( );

        this.members = new CopyOnWriteArrayList<Member>( );
        this.newMembers = new CopyOnWriteArrayList<Member>( );
        this.deletedMembers = new CopyOnWriteArrayList<Member>( );

        this.updateListeners = new CopyOnWriteArrayList<GLLayoutUpdateListener>( );

        if ( parent != null ) parent.addLayoutPainter( this );
    }

    public GLLayoutPainter( )
    {
        this( null );
    }

    public void addLayoutUpdateListener( GLLayoutUpdateListener listener )
    {
        updateListeners.add( listener );
        listener.layoutEvent( x, y, width, height );
    }

    public void removeLayoutPainter( GLLayoutPainter layout )
    {
        deletedMembers.add( new Member( layout, null, true ) );
        invalidateLayout( );
    }

    public void removeSimpleListener( GLSimpleListener layout )
    {
        deletedMembers.add( new Member( layout, null, false ) );
        invalidateLayout( );
    }

    public void addLayoutPainter( GLLayoutPainter layout )
    {
        addLayoutPainter( layout, null );
    }

    public void addLayoutPainter( GLLayoutPainter layout, GLDisplayCallback c )
    {
        if ( layout == null ) throw new IllegalArgumentException( "Null value for layout object." );

        newMembers.add( new Member( layout, c, true ) );
        invalidateLayout( );
    }

    public void addSimpleListener( GLSimpleListener listener )
    {
        addSimpleListener( listener, null );
    }

    public void addSimpleListener( GLSimpleListener listener, GLDisplayCallback c )
    {
        if ( listener == null ) throw new IllegalArgumentException( "Null value for listener object." );

        newMembers.add( new Member( listener, c, false ) );
    }

    @Override
    public void dispose( GLContext context )
    {
        lock.lock( );
        try
        {
            for ( GLLayoutPainter child : layoutChildren )
            {
                child.dispose( context );
            }

            for ( Member member : members )
            {
                member.listener.dispose( context );
            }

            for ( Member member : newMembers )
            {
                member.listener.dispose( context );
            }

            for ( Member member : deletedMembers )
            {
                member.listener.dispose( context );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    private void initializeNewMembers( GLContext context, boolean reshape )
    {
        lock.lock( );
        try
        {
            for ( Member m : newMembers )
            {
                if ( m.layoutParticipant )
                {
                    GLLayoutPainter child = ( GLLayoutPainter ) m.listener;

                    child.layoutParent = this;
                    layoutChildren.add( child );
                    child.init( context );
                    invalidateLayout( );
                }
                else
                {
                    m.listener.init( context );

                    if ( reshape ) m.listener.reshape( context, x, y, width, height );
                }

                members.add( m );
            }
            newMembers.clear( );

            for ( Member m : deletedMembers )
            {
                if ( m.layoutParticipant )
                {
                    GLLayoutPainter child = ( GLLayoutPainter ) m.listener;
                    layoutChildren.remove( child );
                    child.dispose( context );
                    invalidateLayout( );
                }

                members.remove( m );
            }
            deletedMembers.clear( );

            if ( !reshape ) invalidateListenerShapes( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void init( GLContext context )
    {
        initializeNewMembers( context, false );
        invalidateListenerShapes( );
    }

    @Override
    public void display( GLContext context )
    {
        initializeNewMembers( context, true );
        validateLayout( context );
        validateListenerShapes( context );

        Rectangle b = getBounds( );
        if ( b.width == 0 || b.height == 0 ) return;

        GL gl = context.getGL( );
        try
        {
            gl.glEnable( GL.GL_SCISSOR_TEST );

            // TODO: scissor so as not to overrun the parent viewport
            // TODO: reconcile viewport with GLGlimpseListener viewport call
            gl.glViewport( b.x, b.y, b.width, b.height );
            gl.glScissor( b.x, b.y, b.width, b.height );

            for ( Member m : members )
            {
                if ( m.callback != null ) m.callback.preDisplay( m.listener, context, isHovered( ) );

                m.listener.display( context );

                if ( m.callback != null ) m.callback.postDisplay( m.listener, context, isHovered( ) );
            }
        }
        finally
        {
            gl.glDisable( GL.GL_SCISSOR_TEST );
        }
    }

    @Override
    public void reshape( GLContext context, int x, int y, int width, int height )
    {
        lock.lock( );
        try
        {
            if ( layoutParent != null ) throw new IllegalStateException( "Should never be called when a parent exists." );
            setBounds( x, y, width, height );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void setBounds( int x, int y, int width, int height )
    {
        lock.lock( );
        try
        {
            if ( this.x != x || this.y != y || this.width != width || this.height != height )
            {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;

                invalidateLayout( );
                invalidateListenerShapes( );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void displayChanged( GLContext context, boolean modeChanged, boolean deviceChanged )
    {
        lock.lock( );
        try
        {
            initializeNewMembers( context, true );
            for ( Member m : members )
                m.listener.displayChanged( context, modeChanged, deviceChanged );
        }
        finally
        {
            lock.unlock( );
        }
    }

    /**
     * Marks the layout as needing to be recalculated after user changes.
     */
    public void invalidate( )
    {
        invalidateLayout( );
        invalidateListenerShapes( );
    }

    void invalidateListenerShapes( )
    {
        validListenerShapes = false;
    }

    void validateListenerShapes( GLContext context )
    {
        lock.lock( );
        try
        {
            if ( !validListenerShapes )
            {
                for ( Member m : members )
                {
                    if ( m.layoutParticipant )
                    {
                        ( ( GLLayoutPainter ) m.listener ).validateListenerShapes( context );
                    }
                    else
                    {
                        m.listener.reshape( context, x, y, width, height );
                    }
                }

                validListenerShapes = true;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void invalidateLayout( )
    {
        lock.lock( );
        try
        {
            validLayout = false;
            if ( layoutParent != null && layoutParent.validLayout ) layoutParent.invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    void validateLayout( GLContext context )
    {
        assert ( context != null );

        lock.lock( );
        try
        {
            if ( !validLayout )
            {
                layoutManager.layout( context, this );

                for ( Member m : members )
                    if ( m.layoutParticipant ) ( ( GLLayoutPainter ) m.listener ).validateLayout( context );

                validLayout = true;
                invalidateListenerShapes( );

            }
            for ( GLLayoutUpdateListener l : updateListeners )
                l.layoutEvent( x, y, width, height );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Rectangle getBounds( )
    {
        return new Rectangle( x, y, width, height );
    }

    public String getName( )
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setHovered( boolean value )
    {
        this.hovered = value;
    }

    public boolean isHovered( )
    {
        return hovered;
    }

    // ///////////////////////////////////////////////////////////
    // MIG Layout Stuff
    // ///////////////////////////////////////////////////////////

    public Object getLayoutData( )
    {
        return layoutData;
    }

    public void setLayout( GLLayoutManger manager )
    {
        this.layoutManager = manager;
    }

    public void setLayoutData( Object layoutData )
    {
        this.layoutData = layoutData;
    }

    @Override
    public GLLayoutPainter getComponent( )
    {
        return this;
    }

    @Override
    public int getX( )
    {
        return getBounds( ).x;
    }

    @Override
    public int getY( )
    {
        return getBounds( ).y;
    }

    @Override
    public int getWidth( )
    {
        return getBounds( ).width;
    }

    @Override
    public int getHeight( )
    {
        return getBounds( ).height;
    }

    private final Dimension computeSize( int wHint, int hHint )
    {
        lock.lock( );
        try
        {
            int width = wHint == DEFAULT ? DEFAULT_WIDTH : wHint;
            int height = hHint == DEFAULT ? DEFAULT_HEIGHT : hHint;
            int border = 0;
            width += border * 2;
            height += border * 2;
            return new Dimension( width, height );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public int getMinimumWidth( int hHint )
    {
        return zeroMinSize ? 0 : getPreferredWidth( hHint );
    }

    @Override
    public int getMinimumHeight( int wHint )
    {
        return zeroMinSize ? 0 : getPreferredHeight( wHint );
    }

    @Override
    public int getPreferredWidth( int hHint )
    {
        return computeSize( DEFAULT, hHint ).width;
    }

    @Override
    public int getPreferredHeight( int wHint )
    {
        return computeSize( wHint, DEFAULT ).height;
    }

    @Override
    public int getMaximumWidth( int hHint )
    {
        return Short.MAX_VALUE;
    }

    @Override
    public int getMaximumHeight( int wHint )
    {
        return Short.MAX_VALUE;
    }

    @Override
    public boolean isVisible( )
    {
        return visible;
    }

    @Override
    public int getBaseline( int width, int height )
    {
        return -1;
    }

    @Override
    public boolean hasBaseline( )
    {
        return false;
    }

    @Override
    public GLLayoutPainter getParent( )
    {
        return layoutParent;
    }

    @Override
    public String getLinkId( )
    {
        return null;
    }

    @Override
    public int getLayoutHashCode( )
    {
        int h;

        lock.lock( );
        try
        {
            Rectangle b = getBounds( );
            h = b.x + ( b.y << 12 ) + ( b.width << 22 ) + ( b.height << 16 );
            if ( isVisible( ) ) h |= ( 1 << 25 );
            String id = getLinkId( );
            if ( id != null ) h += id.hashCode( );
            if ( isLeftToRight( ) ) h |= ( 1 << 26 );
        }
        finally
        {
            lock.unlock( );
        }
        return h;
    }

    public boolean isPadded( )
    {
        return visualPadding;
    }

    public void setPadding( boolean v )
    {
        visualPadding = v;
    }

    @Override
    public int[] getVisualPadding( )
    {
        return null;
    }

    @Override
    public int getComponetType( boolean disregardScrollPane )
    {
        return TYPE_CONTAINER;
    }

    // ////////////////////////////////////////////////////
    // // TODO: Handle screen functions when not off-screen.
    // ////////////////////////////////////////////////////

    RuntimeException screenAccessException( )
    {
        return new UnsupportedOperationException( "GL layout parameters cannot use screen parameters." );
    }

    @Override
    public void paintDebugOutline( )
    {
        throw screenAccessException( );
    }

    @Override
    public float getPixelUnitFactor( boolean isHor )
    {
        // TODO: Figure out how to return a meaningful value here.

        return 1f;
    }

    @Override
    public int getHorizontalScreenDPI( )
    {
        throw screenAccessException( );
    }

    @Override
    public int getVerticalScreenDPI( )
    {
        throw screenAccessException( );
    }

    @Override
    public int getScreenWidth( )
    {
        throw screenAccessException( );
    }

    @Override
    public int getScreenHeight( )
    {
        throw screenAccessException( );
    }

    @Override
    public int getScreenLocationX( )
    {
        throw screenAccessException( );
    }

    @Override
    public int getScreenLocationY( )
    {
        throw screenAccessException( );
    }

    // ///////////////////////////////////
    // // MIG Container Specific
    // ///////////////////////////////////

    @Override
    public ComponentWrapper[] getComponents( )
    {
        return layoutChildren.toArray( new ComponentWrapper[0] );
    }

    @Override
    public int getComponentCount( )
    {
        return layoutChildren.size( );
    }

    @Override
    public GLLayoutManger getLayout( )
    {
        return layoutManager;
    }

    @Override
    public boolean isLeftToRight( )
    {
        return true;
    }

    @Override
    public void paintDebugCell( int x, int y, int width, int height )
    {
        throw screenAccessException( );
    }

    private static class Member
    {
        public boolean layoutParticipant;

        public GLSimpleListener listener;
        public GLDisplayCallback callback;

        public Member( GLSimpleListener listener, GLDisplayCallback callback, boolean layoutParticipant )
        {
            this.listener = listener;
            this.callback = callback;
            this.layoutParticipant = layoutParticipant;
        }

        @Override
        public int hashCode( )
        {
            return 31 + ( ( listener == null ) ? 0 : listener.hashCode( ) );
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            Member other = ( Member ) obj;
            if ( listener == null && other.listener != null )
                return false;
            else if ( !listener.equals( other.listener ) ) return false;
            return true;
        }
    }
}
