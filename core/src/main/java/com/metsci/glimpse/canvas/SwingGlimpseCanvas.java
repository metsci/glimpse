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
package com.metsci.glimpse.canvas;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPanel;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.swing.MouseWrapperSwing;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A JPanel onto which Glimpse rendering can take place. This class represents
 * the interface between Swing and OpenGL/Glimpse.
 *
 * @author ulman
 */
public class SwingGlimpseCanvas extends JPanel implements GlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( SwingGlimpseCanvas.class.getName( ) );

    private static final long serialVersionUID = -5279064113986688397L;

    protected GLCanvas glCanvas;
    protected FrameBufferGlimpseCanvas offscreenCanvas;

    protected LayoutManager layoutManager;

    protected MouseWrapperSwing mouseHelper;
    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;
    protected boolean isDisposed = false;

    public SwingGlimpseCanvas( )
    {
        this( true );
    }

    public SwingGlimpseCanvas( GLContext _context )
    {
        this( true, _context );
    }

    public SwingGlimpseCanvas( boolean setNoEraseBackgroundProperty )
    {
        this( setNoEraseBackgroundProperty, null );
    }

    public SwingGlimpseCanvas( boolean setNoEraseBackgroundProperty, GLContext _context )
    {
        if ( setNoEraseBackgroundProperty )
        {
            System.setProperty( "sun.awt.noerasebackground", "true" );
        }

        if ( _context == null )
        {
            this.glCanvas = new GLCanvas( );
        }
        else
        {
            this.glCanvas = new GLCanvas( null, null, _context, null );
        }

        this.mouseHelper = new MouseWrapperSwing( this );
        this.addMouseListener( this.mouseHelper );
        this.addMouseMotionListener( this.mouseHelper );
        this.addMouseWheelListener( this.mouseHelper );

        this.layoutManager = new LayoutManager( );

        this.setLayout( new BorderLayout( ) );
        this.add( this.glCanvas, BorderLayout.CENTER );

        // workaround to enable the panel to shrink
        this.setMinimumSize( new Dimension( 0, 0 ) );

        this.isDisposed = false;

        this.addGLEventListener( this.glCanvas );
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
        this.glCanvas.display( );
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

    /**
     * This implementation of removeNotify is to get around an inconvenience
     * where this canvas is removed from one component hierarchy and added to
     * another.  Technically, this will destroy the context.  But since Glimpse
     * makes significant use of buffers and textures, this data needs to come
     * along.  Therefore, we force shared contexts to preserve the information
     * as we transition to another canvas.  This helps for docking frameworks
     * and moving the canvas seamlessly.
     * <p>
     * We could override the removeNotify method of the enclosed GLCanvas, but
     * then we'd have to take care of removing it from drawing into the screen.
     * We could create a new GLCanvas directly shared with the old, instead of
     * a pbuffer, but you have to draw into a new context at least once to share
     * all the information, and you can't draw into a GLCanvas until it's
     * physically displayed.
     * </p>
     */
    @Override
    public void removeNotify( )
    {
        // transfer context to a holding drawable
        if ( offscreenCanvas == null )
        {
            offscreenCanvas = new FrameBufferGlimpseCanvas( getWidth( ), getHeight( ), glCanvas.getContext( ) );
        }

        // add the layouts
        offscreenCanvas.removeAllLayouts( );
        for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
        {
            offscreenCanvas.addLayout( layout );
        }

        // paint so that all the buffers get added to the context
        offscreenCanvas.paint( );

        // remove the canvas (will destroy the context)
        boolean autoSwap = glCanvas.getAutoSwapBufferMode( );
        remove( this.glCanvas );
        super.removeNotify( );

        // initialize the new canvas, share the temp context
        this.glCanvas = new GLCanvas( null, null, offscreenCanvas.getGLContext( ), null );
        this.glCanvas.setAutoSwapBufferMode( autoSwap );
        attachAllGLListeners( glCanvas );
        add( this.glCanvas, BorderLayout.CENTER );
    }

    /**
     * Attaches all the glimpse-related listeners to the new drawable.  This
     * should also transfer over the AWT listeners, but it doesn't.
     */
    private void attachAllGLListeners( GLAutoDrawable drawable )
    {
        this.addMouseListener( this.mouseHelper );
        this.addMouseMotionListener( this.mouseHelper );
        this.addMouseWheelListener( this.mouseHelper );
        this.addGLEventListener( drawable );
    }

    private void addGLEventListener( GLAutoDrawable drawable )
    {
        drawable.addGLEventListener( new GLEventListener( )
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
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
            {
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.layoutTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void dispose( GLAutoDrawable drawable )
            {
                // TODO Auto-generated method stub -- ttran17

            }
        } );
    }

    @Override
    public boolean isDisposed( )
    {
        return this.isDisposed;
    }

    @Override
    public void dispose( RepaintManager manager )
    {
        Runnable dispose = new Runnable( )
        {
            @Override
            public void run( )
            {
                GLContext glContext = getGLContext( );
                GlimpseContext context = new GlimpseContextImpl( glContext );
                glContext.makeCurrent( );
                try
                {
                    for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                    {
                        layout.dispose( context );
                    }
                }
                finally
                {
                    glContext.release( );
                }

                isDisposed = true;
            }
        };

        if ( manager != null )
        {
            manager.asyncExec( dispose );
        }
        else
        {
            dispose.run( );
        }
    }
}
