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
package com.metsci.glimpse.canvas;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLRunnable;
import javax.swing.JPanel;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.newt.MouseWrapperNewt;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A GlimpseCanvas backed by the JOGL {@link NewtCanvasAWT}. This is the default GlimpseCanvas
 * which should be used for most Swing applications. The underlying {@link NewtCanvasAWT} is
 * being actively developed, whereas the underlying canvas for {@link SwingGlimpseCanvas} is
 * no longer supported and has fewer features.
 *
 * @author ulman
 */
public class NewtSwingGlimpseCanvas extends JPanel implements NewtGlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( NewtSwingGlimpseCanvas.class.getName( ) );

    private static final long serialVersionUID = -160715062527667555L;

    protected GLProfile glProfile;
    protected GLCapabilities glCapabilities;
    protected GLWindow glWindow;
    protected NewtCanvasAWT glCanvas;

    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;
    protected boolean isDestroyed = false;

    protected LayoutManager layoutManager;
    protected MouseWrapperNewt mouseHelper;

    protected List<GLRunnable> disposeListeners;

    /**
     * @deprecated Use {@link #NewtSwingGlimpseCanvas(GLContext)} instead. The context implicitly provides a GLProfile.
     */
    public NewtSwingGlimpseCanvas( String glProfile, GLContext context )
    {
        this( GLProfile.get( glProfile ), context );
    }

    /**
     * @deprecated Use {@link #NewtSwingGlimpseCanvas(GLContext)} instead. The context implicitly provides a GLProfile.
     */
    public NewtSwingGlimpseCanvas( GLProfile glProfile, GLContext context )
    {
        init( glProfile, context );
    }

    public NewtSwingGlimpseCanvas( String profile )
    {
        init( GLProfile.get( profile ), null );
    }

    public NewtSwingGlimpseCanvas( GLContext context )
    {
        init( context.getGLDrawable( ).getGLProfile( ), context );
    }

    public NewtSwingGlimpseCanvas( )
    {
        init( GLUtils.getDefaultGLProfile( ), null );
    }

    protected void init( GLProfile glProfile, GLContext context )
    {
        this.glProfile = glProfile;
        this.glCapabilities = new GLCapabilities( glProfile );

        this.glWindow = createGLWindow( glCapabilities );
        if ( context != null ) this.glWindow.setSharedContext( context );
        this.glWindow.addGLEventListener( createGLEventListener( ) );

        this.mouseHelper = createMouseWrapper( );
        this.glWindow.addMouseListener( this.mouseHelper );

        this.glCanvas = new NewtCanvasAWT( glWindow );
        this.setLayout( new BorderLayout( ) );
        this.add( this.glCanvas, BorderLayout.CENTER );

        this.layoutManager = new LayoutManager( );

        // workaround to enable the panel to shrink
        // see: http://jogamp.org/jogl/doc/userguide/#heavylightweightissues
        this.setMinimumSize( new Dimension( 0, 0 ) );

        this.isDestroyed = false;

        this.disposeListeners = new CopyOnWriteArrayList<GLRunnable>( );
    }

    protected GLWindow createGLWindow( GLCapabilities glCapabilities )
    {
        return GLWindow.create( glCapabilities );
    }

    protected MouseWrapperNewt createMouseWrapper( )
    {
        return new MouseWrapperNewt( this );
    }

    protected GLEventListener createGLEventListener( )
    {
        return new GLEventListener( )
        {
            @Override
            public void init( GLAutoDrawable drawable )
            {
                try
                {
                    GL gl = drawable.getGL( );
                    gl.setSwapInterval( 0 );
                }
                catch ( Exception e )
                {
                    // without this, repaint rate is tied to screen refresh rate on some systems
                    // this doesn't work on some machines (Mac OSX in particular)
                    // but it's not a big deal if it fails
                    logWarning( logger, "Trouble in init.", e );
                }
            }

            @Override
            public void display( GLAutoDrawable drawable )
            {
                // ignore initial reshapes while canvas is not showing
                // (the canvas can report incorrect/transient sizes during this time)
                if ( !glCanvas.isShowing( ) ) return;

                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
            {
                // ignore initial reshapes while canvas is not showing
                // (the canvas can report incorrect/transient sizes during this time)
                if ( !glCanvas.isShowing( ) ) return;

                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.layoutTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void dispose( GLAutoDrawable drawable )
            {
                for ( GLRunnable runnable : disposeListeners )
                {
                    runnable.run( drawable );
                }
            }
        };
    }

    @Override
    public GLProfile getGLProfile( )
    {
        return this.glProfile;
    }

    public NewtCanvasAWT getCanvas( )
    {
        return glCanvas;
    }

    @Override
    public GLAutoDrawable getGLDrawable( )
    {
        return glWindow;
    }

    @Override
    public GLWindow getGLWindow( )
    {
        return glWindow;
    }

    @Override
    public GlimpseContext getGlimpseContext( )
    {
        return new GlimpseContextImpl( this );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpseTarget target : this.layoutManager.getLayoutList( ) )
        {
            target.setLookAndFeel( laf );
        }
    }

    @Override
    public void addLayout( GlimpseLayout layout )
    {
        this.layoutManager.addLayout( layout );
    }

    @Override
    public void addLayout( GlimpseLayout layout, int zOrder )
    {
        this.layoutManager.addLayout( layout, zOrder );
    }

    @Override
    public void setZOrder( GlimpseLayout layout, int zOrder )
    {
        this.layoutManager.setZOrder( layout, zOrder );
    }

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        this.layoutManager.removeLayout( layout );
    }

    @Override
    public void removeAllLayouts( )
    {
        this.layoutManager.removeAllLayouts( );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public List<GlimpseTarget> getTargetChildren( )
    {
        // layoutManager returns an unmodifiable list, thus this cast is typesafe
        // (there is no way for the recipient of the List<GlimpseTarget> view to
        // add GlimpseTargets which are not GlimpseLayouts to the list)
        return ( List ) this.layoutManager.getLayoutList( );
    }

    public Dimension getDimension( )
    {
        return new Dimension( this.glWindow.getWidth( ), this.glWindow.getHeight( ) );
    }

    @Override
    public GlimpseBounds getTargetBounds( GlimpseTargetStack stack )
    {
        return new GlimpseBounds( getDimension( ) );
    }

    @Override
    public GlimpseBounds getTargetBounds( )
    {
        return getTargetBounds( null );
    }

    @Override
    public void paint( )
    {
        this.glWindow.display( );
    }

    @Override
    public GLContext getGLContext( )
    {
        return this.glWindow.getContext( );
    }

    @Override
    public String toString( )
    {
        return NewtSwingGlimpseCanvas.class.getSimpleName( );
    }

    @Override
    public boolean isEventConsumer( )
    {
        return this.isEventConsumer;
    }

    @Override
    public void setEventConsumer( boolean consume )
    {
        this.isEventConsumer = consume;
    }

    @Override
    public boolean isEventGenerator( )
    {
        return this.isEventGenerator;
    }

    @Override
    public void setEventGenerator( boolean generate )
    {
        this.isEventGenerator = generate;
    }

    @Override
    public void destroy( )
    {
        if ( !this.isDestroyed )
        {
            if ( this.glWindow != null ) this.glWindow.destroy( );
            this.isDestroyed = true;
        }
    }

    @Override
    public boolean isDestroyed( )
    {
        return this.isDestroyed;
    }

    @Override
    public void addDisposeListener( GLRunnable runnable )
    {
        this.disposeListeners.add( runnable );
    }

    @Override
    public void dispose( )
    {
        disposeAttached( );
        destroy( );
    }

    @Override
    public void disposeAttached( )
    {
        this.getGLDrawable( ).invoke( false, new GLRunnable( )
        {
            @Override
            public boolean run( GLAutoDrawable drawable )
            {
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.dispose( getGlimpseContext( ) );
                }

                // after layouts are disposed they should not be painted
                // so remove them from the canvas
                removeAllLayouts( );

                return true;
            }
        } );
    }

    @Override
    public void disposePainter( final GlimpsePainter painter )
    {
        this.getGLDrawable( ).invoke( false, new GLRunnable( )
        {
            @Override
            public boolean run( GLAutoDrawable drawable )
            {
                painter.dispose( getGlimpseContext( ) );
                return true;
            }
        } );
    }

    @Override
    public int[] getSurfaceScale( )
    {
        return this.glWindow.getNativeSurfaceScale( new int[2] );
    }
}