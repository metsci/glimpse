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
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.awt.GLCanvas;
import javax.swing.JPanel;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.swing.MouseWrapperSwing;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A Swing GlimpseCanvas implementation which draws to a {@link javax.media.opengl.awt.GLCanvas}. This
 * Canvas implementation is not as well supported as the newer JOGL {@link com.jogamp.newt.awt.NewtCanvasAWT}.
 * For this reason, {@link NewtSwingGlimpseCanvas} should generally be preferred over this class.
 *
 * @author ulman
 * @deprecated see {@link NewtSwingGlimpseCanvas}
 */
@Deprecated
public class SwingGlimpseCanvas extends JPanel implements GlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( SwingGlimpseCanvas.class.getName( ) );

    private static final long serialVersionUID = 1L;

    protected GLProfile glProfile;
    protected GLCapabilities glCapabilities;
    protected GLCanvas glCanvas;
    protected GLEventListener glListener;

    protected LayoutManager layoutManager;
    protected MouseWrapperSwing mouseHelper;

    protected List<GLRunnable> disposeListeners;

    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;
    protected boolean isDestroyed = false;

    public SwingGlimpseCanvas( )
    {
        init( true, GLUtils.getDefaultGLProfile( ), null );
    }

    public SwingGlimpseCanvas( GLContext context )
    {
        init( true, context.getGLDrawable( ).getGLProfile( ), context );
    }

    public SwingGlimpseCanvas( GLProfile glProfile )
    {
        init( true, glProfile, null );
    }

    /**
     * @deprecated Use {@link #SwingGlimpseCanvas(GLContext)} instead. The context implicitly provides a GLProfile.
     */
    public SwingGlimpseCanvas( GLProfile glProfile, GLContext context )
    {
        init( true, glProfile, context );
    }

    /**
     * @deprecated Use {@link #SwingGlimpseCanvas(GLContext)} instead. The context implicitly provides a GLProfile.
     */
    public SwingGlimpseCanvas( String profile, GLContext context )
    {
        init( true, GLProfile.get( profile ), context );
    }

    /**
     * @deprecated Use {@link #SwingGlimpseCanvas(GLContext)} instead. The context implicitly provides a GLProfile.
     */
    public SwingGlimpseCanvas( boolean setNoEraseBackgroundProperty, String profile, GLContext context )
    {
        init( setNoEraseBackgroundProperty, GLProfile.get( profile ), context );
    }

    /**
     * @deprecated Use {@link #SwingGlimpseCanvas(GLContext)} instead. The context implicitly provides a GLProfile.
     */
    public SwingGlimpseCanvas( boolean setNoEraseBackgroundProperty, GLProfile glProfile, GLContext context )
    {
        init( setNoEraseBackgroundProperty, glProfile, context );
    }

    private void init( boolean setNoEraseBackgroundProperty, GLProfile glProfile, GLContext context )
    {
        if ( setNoEraseBackgroundProperty )
        {
            System.setProperty( "sun.awt.noerasebackground", "true" );
        }

        this.glProfile = glProfile;
        this.glCapabilities = new GLCapabilities( glProfile );

        this.glCanvas = new GLCanvas( glCapabilities, null, null );

        if ( context != null )
        {
            this.glCanvas.setSharedContext( context );
        }

        this.mouseHelper = new MouseWrapperSwing( this );
        this.addMouseListener( this.mouseHelper );
        this.addMouseMotionListener( this.mouseHelper );
        this.addMouseWheelListener( this.mouseHelper );

        this.setLayout( new BorderLayout( ) );
        this.add( this.glCanvas, BorderLayout.CENTER );

        this.layoutManager = new LayoutManager( );

        // workaround to enable the panel to shrink
        this.setMinimumSize( new Dimension( 0, 0 ) );

        this.isDestroyed = false;

        this.glListener = createGLEventListener( );
        this.glCanvas.addGLEventListener( this.glListener );

        this.disposeListeners = new CopyOnWriteArrayList<GLRunnable>( );
    }

    private GLEventListener createGLEventListener( )
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
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseListener( MouseListener listener )
    {
        this.glCanvas.addMouseListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseMotionListener( MouseMotionListener listener )
    {
        this.glCanvas.addMouseMotionListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseWheelListener( MouseWheelListener listener )
    {
        this.glCanvas.addMouseWheelListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeMouseListener( MouseListener listener )
    {
        this.glCanvas.removeMouseListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeMouseMotionListener( MouseMotionListener listener )
    {
        this.glCanvas.removeMouseMotionListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeMouseWheelListener( MouseWheelListener listener )
    {
        this.glCanvas.removeMouseWheelListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addKeyListener( KeyListener listener )
    {
        this.glCanvas.addKeyListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeKeyListener( KeyListener listener )
    {
        this.glCanvas.removeKeyListener( listener );
    }

    @Override
    public GLProfile getGLProfile( )
    {
        return this.glProfile;
    }

    @Override
    public GLAutoDrawable getGLDrawable( )
    {
        return this.glCanvas;
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
        return this.glCanvas.getSize( );
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
        this.repaint( );
    }

    @Override
    public GLContext getGLContext( )
    {
        return this.glCanvas.getContext( );
    }

    @Override
    public String toString( )
    {
        return SwingGlimpseCanvas.class.getSimpleName( );
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
    public boolean isDestroyed( )
    {
        return this.isDestroyed;
    }

    @Override
    public void destroy( )
    {
        if ( !this.isDestroyed )
        {
            if ( this.glCanvas != null ) this.glCanvas.destroy( );
            this.isDestroyed = true;
        }
    }

    @Override
    public void addDisposeListener( GLRunnable runnable )
    {
        this.disposeListeners.add( runnable );
    }

    @Override
    public void dispose( )
    {
        // Stop the animator so that disposeAttached runs immediately in this thread
        // instead of on the animator thread. If this is not the case, then destroy( )
        // could run first and then the getGLDrawable( ).invoke( ) call will do nothing
        // because the window is already destroyed
        this.getGLDrawable( ).setAnimator( null );

        this.removeMouseListener( this.mouseHelper );
        this.removeMouseMotionListener( this.mouseHelper );
        this.removeMouseWheelListener( this.mouseHelper );
        this.glCanvas.removeGLEventListener( this.glListener );
        this.mouseHelper.dispose( );

        this.disposeAttached( );
        this.destroy( );
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
    public float[] getSurfaceScale( )
    {
        return this.glCanvas.getCurrentSurfaceScale( new float[2] );
    }
}