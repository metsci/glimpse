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
package com.metsci.glimpse.support.swing;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLRunnable;
import javax.swing.SwingUtilities;

import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.opengl.GLWindow;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.event.mouse.newt.MouseWrapperNewt;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;

/**
 * <p>A subclass of NewtSwingGlimpseCanvas which performs rendering on the Swing EDT,
 * enabling writing a Glimpse/JOGL/NEWT application using the Swing Single Threaded
 * Event Dispatch model.</p>
 *
 * <p>All Glimpse/JOGL drawing is guaranteed to be performed on the Swing EDT, provided
 * {@link SwingEDTAnimator} is attached to the canvas' {@link GLAutoDrawable}.</p>
 *
 * <p>All Glimpse mouse listeners are notified on the Swing EDT.</p>
 *
 * <p>{@link #disposeAttached()} and {@link #disposePainter(GlimpsePainter)}
 * happen on the Swing EDT.</p>
 *
 * <p>{@link GLEventListener#reshape(GLAutoDrawable, int, int, int, int)} happens on the
 * Swing EDT, but not with a current GL Context (which is fine because GlimpseCanvas does
 * not need an active context to perform its reshape operation.</p>
 *
 * <p>{@link GLEventListener#init(GLAutoDrawable)} and {@link GLEventListener#dispose(GLAutoDrawable)}
 * do NOT happen on the Swing EDT.</p>
 *
 * @author ulman
 * @see SwingEDTAnimator
 */
public class NewtSwingEDTGlimpseCanvas extends NewtSwingGlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( NewtSwingEDTGlimpseCanvas.class.getName( ) );

    private static final long serialVersionUID = 1L;

    public NewtSwingEDTGlimpseCanvas( String profile )
    {
        super( profile );
    }

    public NewtSwingEDTGlimpseCanvas( GLContext context )
    {
        super( context );
    }

    public NewtSwingEDTGlimpseCanvas( )
    {
        super( );
    }

    @Override
    protected GLWindow createGLWindow( GLCapabilities glCapabilities )
    {
        return new GLWindow( NewtFactory.createWindow( glCapabilities ) )
        {
            public void display( )
            {
                if ( SwingUtilities.isEventDispatchThread( ) )
                {
                    super.display( );
                }
            }
        };
    }

    @Override
    protected MouseWrapperNewt createMouseWrapper( )
    {
        return new MouseWrapperNewtSwingEDT( this );
    }

    @Override
    protected GLEventListener createGLEventListener( )
    {
        return new GLEventListener( )
        {
            // NOT run on Swing EDT
            // (does not need to be as it only sets up OpenGL state, doesn't touch Glimpse classes)
            @Override
            public void init( final GLAutoDrawable drawable )
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

            // run on Swing EDT automatically when using SwingAnimator
            @Override
            public void display( final GLAutoDrawable drawable )
            {
                // Before animator is attached, display() may get called from off the EDT.
                // Simply ignore these calls.
                if ( !SwingUtilities.isEventDispatchThread( ) ) return;

                // Ignore initial reshapes while canvas is not showing/
                // The canvas can report incorrect/transient sizes during this time.
                if ( !glCanvas.isShowing( ) ) return;

                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            // Called by NEWT off the Swing EDT, is run on the EDT with SwingUtilities.invokeLater().
            // This strategy would fail if reshape required using the GLAutoDrawable, since the context
            // is no longer active when SwingUtilities.invokeLater() is run.
            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
            {
                SwingUtilities.invokeLater( new Runnable( )
                {
                    @Override
                    public void run( )
                    {
                        // ignore initial reshapes while canvas is not showing
                        // (the canvas can report incorrect/transient sizes during this time)
                        if ( !glCanvas.isShowing( ) ) return;

                        layoutTo( );
                    }
                } );
            }

            // NOT run on Swing EDT
            // However, GlimsepCanvas.disposeAttached( ) and GlimsepCanvas.disposePainter( ) are called
            // from the Swing EDT when using SwingAnimator and those methods are where Glimpse resources
            // are disposed of.
            @Override
            public void dispose( final GLAutoDrawable drawable )
            {
                for ( GLRunnable runnable : disposeListeners )
                {
                    runnable.run( drawable );
                }
            }
        };
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

    public void layoutTo( )
    {
        assert ( SwingUtilities.isEventDispatchThread( ) );

        for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
        {
            layout.layoutTo( getGlimpseContext( ) );
        }
    }
}