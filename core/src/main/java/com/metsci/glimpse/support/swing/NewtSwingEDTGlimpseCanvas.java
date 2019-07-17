/*
 * Copyright (c) 2019, Metron, Inc.
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

import static com.metsci.glimpse.support.swing.NewtClickTimeoutWorkaround.attachNewtClickTimeoutWorkaround;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static java.lang.Long.parseLong;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.Window;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.event.key.newt.KeyWrapperNewt;
import com.metsci.glimpse.event.mouse.newt.MouseWrapperNewt;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;

import jogamp.newt.driver.awt.AWTEDTUtil;

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

    protected static final long clickTimeout_MILLIS = findClickTimeout_MILLIS( );
    protected static long findClickTimeout_MILLIS( )
    {
        try
        {
            String sysPropValue = System.getProperty( "glimpse.clickTimeoutMillis" );
            return parseLong( sysPropValue );
        }
        catch ( Exception e )
        {
            try
            {
                Object awtPropValue = Toolkit.getDefaultToolkit( ).getDesktopProperty( "awt.multiClickInterval" );
                return ( ( Number ) awtPropValue ).longValue( );
            }
            catch ( Exception e2 )
            {
                return 500;
            }
        }
    }

    static
    {
        logger.fine( "Glimpse click timeout is " + clickTimeout_MILLIS + " ms" );
    }

    public NewtSwingEDTGlimpseCanvas( String profile )
    {
        super( profile );
    }

    public NewtSwingEDTGlimpseCanvas( GLProfile profile )
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
        Window window = NewtFactory.createWindow( glCapabilities );

        // Workaround for https://jogamp.org/bugzilla/show_bug.cgi?id=1127
        window.addWindowListener( 0, new WindowAdapter( )
        {
            @Override
            public void windowGainedFocus( WindowEvent ev )
            {
                ev.setConsumed( true );
            }
        } );

        Display display = window.getScreen( ).getDisplay( );
        if ( !( display.getEDTUtil( ) instanceof AWTEDTUtil ) )
        {
            display.setEDTUtil( new AWTEDTUtil( currentThread( ).getThreadGroup( ), "AWTDisplay-" + display.getFQName( ), display::dispatchMessages ) );
        }

        attachNewtClickTimeoutWorkaround( window, clickTimeout_MILLIS );

        return GLWindow.create( window );
    }

    @Override
    protected MouseWrapperNewt createMouseWrapper( )
    {
        return new MouseWrapperNewtSwingEDT( this );
    }

    @Override
    protected KeyWrapperNewt createKeyWrapper( )
    {
        return new KeyWrapperNewtSwingEDT( this, requireNonNull( this.mouseHelper ) );
    }

    @Override
    protected GLEventListener createGLEventListener( )
    {
        return new GLEventListener( )
        {
            // runs on the Swing EDT, as long as AWTEDTUtil is used
            @Override
            public void init( final GLAutoDrawable drawable )
            {
                requireSwingThread( );

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

            // runs on the Swing EDT, as long as SwingEDTAnimator is used
            @Override
            public void display( final GLAutoDrawable drawable )
            {
                requireSwingThread( );

                // Ignore initial reshapes while canvas is not showing/
                // The canvas can report incorrect/transient sizes during this time.
                if ( !glCanvas.isShowing( ) ) return;

                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            // runs on the Swing EDT, as long as AWTEDTUtil is used
            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
            {
                requireSwingThread( );

                // ignore initial reshapes while canvas is not showing
                // (the canvas can report incorrect/transient sizes during this time)
                if ( !glCanvas.isShowing( ) ) return;

                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.layoutTo( getGlimpseContext( ) );
                }
            }

            // runs on the Swing EDT, as long as AWTEDTUtil is used
            @Override
            public void dispose( final GLAutoDrawable drawable )
            {
                requireSwingThread( );

                for ( GLRunnable runnable : disposeListeners )
                {
                    runnable.run( drawable );
                }
            }
        };
    }

    /**
     * When used in a window-closing listener, this method <strong>MUST</strong> run
     * before NewtCanvasAWT's built-in window-closing listener. This can be accomplished
     * by adding a window-closing listener (that will call this method) BEFORE adding
     * this canvas to the frame. It also works to remove all existing WindowListeners,
     * add a window-closing listener that will call this method, then re-adding the old
     * WindowListeners.
     * <p>
     * It is safe to remove this canvas from its parent after calling this method.
     */
    @Override
    public void destroy( )
    {
        if ( !this.isDestroyed )
        {
            if ( this.getCanvas( ).getNEWTChild( ) == null )
            {
                logger.warning( "Canvas's NEWT child was already null before canvas.destroy() -- this may mean that JOGL's built-in window-closing listener is running first, which can cause problems on Windows" );
            }

            // Remove the canvas from its animator -- which will pause the animator if it
            // doesn't have any other drawables
            GLAutoDrawable drawable = this.getGLDrawable( );
            GLAnimatorControl animator = drawable.getAnimator( );
            if ( animator != null )
            {
                animator.remove( drawable );
            }

            // Hold a reference to the screen so that JOGL's auto-cleanup doesn't destroy
            // and then recreate resources (like the NEDT thread) while we're still working
            Screen screen = this.getGLWindow( ).getScreen( );
            screen.addReference( );
            try
            {
                // Canvas destruction is finicky -- the relevant JOGL code is complicated,
                // the relevant AWT code is platform-dependent native code, and the relevant
                // AWT behavior is affected by quirks and mysteries of the window manager
                // and/or OS. Debugging problems directly would take a long time (weeks or
                // months).
                //
                // The following call sequence seems to work reliably. It was arrived at by
                // trying various sequences until one worked for the platforms and situations
                // we care about.
                //
                // This sequence has only been tested with NewtSwingEDTGlimpseCanvas.
                //
                // Notes:
                //
                //  * Without setVisible(false), the screen area formerly occupied by the
                //    canvas ends up unusable -- it appears blank or continues to show the
                //    canvas's final frame, and it does not respond to resize events.
                //
                //  * On Windows 10, without the explicit getGLWindow().destroy(), the NEDT
                //    thread begins receiving WM_TIMER events, and continues to receive them
                //    indefinitely. This prevents the AWT thread from exiting, which in turn
                //    can prevent the JVM from exiting. This is particularly strange because
                //    getCanvas().destroy() calls getGLWindow().destroy() internally. The
                //    difference could be in the timing (due to a race), or simply in the
                //    ordering of the various calls.
                //
                //  * If we call setNEWTChild(null) instead of setVisible(false), we get the
                //    WM_TIMER issue.
                //
                //  * If we call parent.remove(canvas) instead of setVisible(false), we get
                //    the WM_TIMER issue.
                //
                //  * If we don't call getCanvas().destroy(), we get the WM_TIMER issue. This
                //    is a little dicey, because getCanvas().destroy() has caused segfaults
                //    in the past -- however, we haven't seen such segfaults since switching
                //    to AWTEDTUtil, so hopefully that's no longer an issue. (Of course, it's
                //    also possible that we've simply perturbed the timing of a race condition,
                //    and that the segfaults could reappear someday.)
                //
                this.setVisible( false );
                this.getGLWindow( ).destroy( );
                this.getCanvas( ).destroy( );
            }
            finally
            {
                screen.removeReference( );
            }

            this.isDestroyed = true;
        }
    }

    public BufferedImage toBufferedImage( )
    {
        requireSwingThread( );

        GLContext glContext = this.getGLDrawable( ).getContext( );
        glContext.makeCurrent( );
        try
        {
            this.paint( );
            AWTGLReadBufferUtil util = new AWTGLReadBufferUtil( this.glProfile, true );
            return util.readPixelsToBufferedImage( glContext.getGL( ), true );
        }
        finally
        {
            glContext.release( );
        }
    }

    protected static void requireSwingThread( )
    {
        if ( !SwingUtilities.isEventDispatchThread( ) )
        {
            throw new RuntimeException( "This operation is only allowed on the Swing/AWT event-dispatch thread" );
        }
    }
}