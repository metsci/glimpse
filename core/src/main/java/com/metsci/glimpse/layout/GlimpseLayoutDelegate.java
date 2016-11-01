/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.layout;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.media.opengl.GL;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainterCallback;
import com.metsci.glimpse.support.settings.LookAndFeel;

import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.ContainerWrapper;

public class GlimpseLayoutDelegate implements ComponentWrapper, ContainerWrapper
{
    public static final int DEFAULT = -1;
    public static final int DEFAULT_WIDTH = 100;
    public static final int DEFAULT_HEIGHT = 100;

    private int x, y, width, height;

    private boolean isDisposed = false;

    private boolean visualPadding = false;
    private static final boolean zeroMinSize = true;

    //TODO These default constraints make filling all available space the default behavior of a GlimpseLayout
    //     because older code sometimes used GlimpseLayouts in this way. However, with the new setup there
    //     should be no reason to have a GlimpseLayout which completely fills its parent GlimpseLayout (since
    //     it doesn't add/change anything)
    private GlimpseLayoutManager layoutManager = new GlimpseLayoutManagerMig( "bottomtotop, gapx 0, gapy 0, insets 0", null, null );
    private Object layoutData = "push, grow";

    // the GlimpseLayout associated with this GlimpseLayoutDelegate
    private GlimpseLayout layout;

    // GlimpseLayouts may be part of multiple hierarchies and thus may have
    // multiple parents. This pointer is temporarily set whenever the layout
    // algorithm is run
    private GlimpseLayoutDelegate layoutParent;

    private List<GlimpseLayoutDelegate> layoutChildren;
    private LinkedHashMap<GlimpsePainter, Member> memberMap;
    private List<Member> memberList;

    private static class Member
    {
        public GlimpsePainter painter;
        public GlimpsePainterCallback callback;
        public int zOrder = 0;

        public Member( GlimpsePainter painter, GlimpsePainterCallback callback, int zOrder )
        {
            this.painter = painter;
            this.callback = callback;
            this.zOrder = zOrder;
        }

        public void setZOrder( int zOrder )
        {
            this.zOrder = zOrder;
        }

        public int getZOrder( )
        {
            return this.zOrder;
        }

        @Override
        public int hashCode( )
        {
            return 31 + ( ( painter == null ) ? 0 : painter.hashCode( ) );
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            Member other = ( Member ) obj;
            if ( painter == null && other.painter != null )
                return false;
            else if ( !painter.equals( other.painter ) ) return false;
            return true;
        }
    }

    public GlimpseLayoutDelegate( GlimpseLayout layout )
    {
        this.layout = layout;

        this.layoutChildren = new ArrayList<GlimpseLayoutDelegate>( );
        this.memberList = new ArrayList<Member>( );
        this.memberMap = new LinkedHashMap<GlimpsePainter, Member>( );
    }

    public void paintTo( GlimpseContext context )
    {
        final int[] scale = context.getSurfaceScale( );
        final int scaleX = scale[0];
        final int scaleY = scale[1];
        GL gl = context.getGL( );

        GlimpseBounds bounds = context.getTargetStack( ).getBounds( );
        GlimpseBounds clippedBounds = GLUtils.getClippedBounds( context );

        if ( !clippedBounds.isValid( ) ) return;

        for ( Member m : memberList )
        {
            try
            {
                // if a GlimpsePainter is visible, paint it
                // if it is not visible, but is a GlimpseLayout (GlimpseLayout implements GlimpsePainter, which it
                // arguably should not) then GlimpseLayout still needs to layout its children or odd behavior may
                // result if the GlimpseLayout is resized while not visible (but nothing further should be painted)

                boolean isLayout = m.painter instanceof GlimpseLayout;
                boolean isVisible = layout.isVisible;

                if ( isVisible )
                {
                    gl.glEnable( GL.GL_SCISSOR_TEST );

                    gl.glViewport( bounds.getX( ) * scaleX, bounds.getY( ) * scaleY, bounds.getWidth( ) * scaleX, bounds.getHeight( ) * scaleY );
                    gl.glScissor( clippedBounds.getX( ) * scaleX, clippedBounds.getY( ) * scaleY, clippedBounds.getWidth( ) * scaleX, clippedBounds.getHeight( ) * scaleY );

                    if ( m.callback != null ) m.callback.prePaint( m.painter, context );
                    m.painter.paintTo( context );
                    if ( m.callback != null ) m.callback.postPaint( m.painter, context );
                }
                else if ( isLayout )
                {
                    ( ( GlimpseLayout ) m.painter ).layoutTo( context );
                }
            }
            finally
            {
                gl.glDisable( GL.GL_SCISSOR_TEST );
            }
        }
    }

    public void layoutTo( GlimpseContext context, GlimpseBounds bounds )
    {
        layoutTo( context.getTargetStack( ), bounds );
    }

    // lay out the children of this GlimpseLayout
    // therefore GlimpseLayout should be at the top of the
    // GlimpseContext stack with the proper GlimpseBounds
    public void layoutTo( GlimpseTargetStack stack, GlimpseBounds bounds )
    {
        // push ourself onto the stack in preparation for laying out our children
        stack.push( this.layout, bounds );

        // update the size of our axes
        this.layout.preLayout( stack, bounds );

        // fields in GlimpseLayoutDelegate are temporary and are reset for each new Context
        // which the GlimpseLayout is laid out to (the fields are used by the GlimpseLayoutManager)

        // set temporary bound field
        setBounds( bounds );

        // set temporary parent fields
        for ( GlimpseLayoutDelegate child : this.layoutChildren )
        {
            child.setParent( this );
        }

        // run the GlimpseLayoutManager to set the bounds of our children
        this.layoutManager.layout( this );

        // retrieve the bounds set by the previous call and store them in the
        // GlimpseLayout cache for the current context
        for ( GlimpseLayoutDelegate child : this.layoutChildren )
        {
            GlimpseBounds childBounds = child.getBounds( );

            child.cacheBounds( stack, childBounds );

            child.layoutTo( stack, childBounds );
        }

        // pop ourself off the stack
        stack.pop( );
    }

    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( Member m : memberList )
        {
            m.painter.setLookAndFeel( laf );
        }
    }

    public void invalidateLayout( )
    {
        for ( GlimpseLayoutDelegate child : layoutChildren )
        {
            child.layout.invalidateLayout( );
        }
    }

    public void removeLayout( GlimpseLayout layout )
    {
        Member member = memberMap.remove( layout );
        memberList.remove( member );

        GlimpseLayoutDelegate delegate = layout.getDelegate( );
        layoutChildren.remove( delegate );
    }

    public void removeAll( )
    {
        layoutChildren.clear( );
        memberList.clear( );
        memberMap.clear( );
    }

    public void addLayout( GlimpseLayout layout )
    {
        addLayout( layout, null, 0 );
    }

    public void addLayout( GlimpseLayout layout, GlimpsePainterCallback callback, int zOrder )
    {
        Member member = new Member( layout, callback, zOrder );
        memberMap.put( layout, member );
        memberList.add( member );
        updateMemeberList( );

        GlimpseLayoutDelegate delegate = layout.getDelegate( );
        layoutChildren.add( delegate );
    }

    public void addPainter( GlimpsePainter painter )
    {
        addPainter( painter, null, 0 );
    }

    public void addPainter( GlimpsePainter painter, GlimpsePainterCallback callback, int zOrder )
    {
        Member member = new Member( painter, callback, zOrder );
        memberMap.put( painter, member );
        memberList.add( member );
        updateMemeberList( );
    }

    public void removePainter( GlimpsePainter painter )
    {
        Member member = memberMap.remove( painter );
        memberList.remove( member );
    }

    public void setZOrder( GlimpsePainter painter, int zOrder )
    {
        Member member = memberMap.get( painter );

        if ( member != null )
        {
            member.setZOrder( zOrder );
            updateMemeberList( );
        }
    }

    public void updateMemeberList( )
    {
        Collections.sort( memberList, new Comparator<Member>( )
        {
            @Override
            public int compare( Member arg0, Member arg1 )
            {
                if ( arg0.getZOrder( ) < arg1.getZOrder( ) )
                {
                    return -1;
                }
                else if ( arg0.getZOrder( ) > arg1.getZOrder( ) )
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        } );
    }

    public GlimpseBounds getCachedBounds( GlimpseContext context )
    {
        return this.layout.getBounds( context );
    }

    public void cacheBounds( GlimpseContext context, GlimpseBounds bounds )
    {
        this.layout.cacheBounds( context, bounds );
    }

    public void cacheBounds( GlimpseTargetStack stack, GlimpseBounds bounds )
    {
        this.layout.cacheBounds( stack, bounds );
    }

    public GlimpseBounds getBounds( )
    {
        return new GlimpseBounds( x, y, width, height );
    }

    public Object getLayoutData( )
    {
        return layoutData;
    }

    public void setLayoutManager( GlimpseLayoutManager manager )
    {
        this.layoutManager = manager;
    }

    public GlimpseLayoutManager getLayoutManager( )
    {
        return layoutManager;
    }

    public void setLayoutData( Object layoutData )
    {
        this.layoutData = layoutData;
    }

    public void setParent( GlimpseLayoutDelegate parent )
    {
        this.layoutParent = parent;
    }

    public void dispose( GlimpseContext context )
    {
        if ( !isDisposed )
        {
            for ( Member member : memberList )
            {
                member.painter.dispose( context );
            }
        }

        isDisposed = true;
    }

    public boolean isDisposed( )
    {
        return isDisposed;
    }

    // ///////////////////////////////////////////////////////////
    // MIG Layout Stuff
    // ///////////////////////////////////////////////////////////

    public void setBounds( GlimpseBounds bounds )
    {
        this.x = bounds.getX( );
        this.y = bounds.getY( );
        this.width = bounds.getWidth( );
        this.height = bounds.getHeight( );
    }

    @Override
    public void setBounds( int x, int y, int width, int height )
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public GlimpseLayoutDelegate getComponent( )
    {
        return this;
    }

    @Override
    public int getX( )
    {
        return x;
    }

    @Override
    public int getY( )
    {
        return y;
    }

    @Override
    public int getWidth( )
    {
        return width;
    }

    @Override
    public int getHeight( )
    {
        return height;
    }

    private final Dimension computeSize( int wHint, int hHint )
    {
        int width = wHint == DEFAULT ? DEFAULT_WIDTH : wHint;
        int height = hHint == DEFAULT ? DEFAULT_HEIGHT : hHint;
        int border = 0;
        width += border * 2;
        height += border * 2;
        return new Dimension( width, height );
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
        return layout.isVisible( );
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
    public GlimpseLayoutDelegate getParent( )
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

        h = x + ( y << 12 ) + ( width << 22 ) + ( height << 16 );
        if ( isVisible( ) ) h |= ( 1 << 25 );
        String id = getLinkId( );
        if ( id != null ) h += id.hashCode( );
        if ( isLeftToRight( ) ) h |= ( 1 << 26 );

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
    public GlimpseLayoutManager getLayout( )
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
}
