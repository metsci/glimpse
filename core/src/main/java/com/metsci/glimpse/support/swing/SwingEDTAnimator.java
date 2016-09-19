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

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.SwingUtilities;

import com.google.common.collect.Lists;
import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.util.concurrent.ConcurrencyUtils;

/**
 * An FPSAnimator-like class which performs rendering on the Swing EDT.
 *
 * @see FPSAnimator
 * @author ulman
 */
public class SwingEDTAnimator implements GLAnimatorControl
{
    private static final Logger logger = Logger.getLogger( SwingEDTAnimator.class.getName( ) );

    protected List<GLAutoDrawable> targets;
    protected UncaughtExceptionHandler handler;
    protected ScheduledExecutorService executor;
    protected volatile ScheduledFuture<?> future;
    protected double fps;

    public SwingEDTAnimator( double fps )
    {
        this.fps = fps;

        this.targets = Lists.newCopyOnWriteArrayList( );

        // create a default exception handler which simply logs exceptions
        this.handler = new UncaughtExceptionHandler( )
        {

            @Override
            public void uncaughtException( GLAnimatorControl animator, GLAutoDrawable drawable, Throwable cause )
            {
                logWarning( logger, "Exception in: %s. Drawable: %s", cause, animator, drawable );
            }
        };
    }

    protected synchronized void start0( )
    {
        // do nothing if the animator is already running
        if ( this.future != null ) return;

        ThreadFactory threadFactory = ConcurrencyUtils.newDaemonThreadFactory( new ThreadFactory( )
        {
            @Override
            public Thread newThread( Runnable r )
            {
                Thread thread = new Thread( r );
                thread.setName( SwingEDTAnimator.class.getSimpleName( ) );
                return thread;
            }
        } );

        ScheduledExecutorService exectuor = Executors.newSingleThreadScheduledExecutor( threadFactory );

        this.future = exectuor.scheduleAtFixedRate( new Runnable( )
        {
            @Override
            public void run( )
            {
                try
                {
                    SwingUtilities.invokeAndWait( new Runnable( )
                    {
                        @Override
                        public void run( )
                        {
                            for ( GLAutoDrawable target : targets )
                            {
                                try
                                {
                                    target.display( );
                                }
                                catch ( Throwable t )
                                {
                                    if ( handler != null ) handler.uncaughtException( SwingEDTAnimator.this, target, t );
                                }
                            }
                        }
                    } );
                }
                catch ( InvocationTargetException | InterruptedException e )
                {
                    logWarning( logger, "SwingAnimator Error.", e );
                }
            }

        }, 0, ( int ) ( 1000.0 / fps ), TimeUnit.MILLISECONDS );
    }

    @Override
    public synchronized Thread getThread( )
    {
        //TODO Swing doesn't appear to provide a public way to get a reference to the EDT
        //Toolkit.getDefaultToolkit( ).getSystemEventQueue( ).getDispatchThread( );

        return null;
    }

    @Override
    public boolean isStarted( )
    {
        return this.future != null;
    }

    @Override
    public boolean isAnimating( )
    {
        return this.future != null;
    }

    @Override
    public boolean isPaused( )
    {
        return this.future != null;
    }

    @Override
    public synchronized boolean start( )
    {
        if ( isStarted( ) ) return false;

        start0( );

        return true;
    }

    @Override
    public synchronized boolean stop( )
    {
        if ( !isStarted( ) ) return false;

        boolean success = this.future.cancel( false );

        if ( success )
        {
            this.future = null;
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean pause( )
    {
        return stop( );
    }

    @Override
    public boolean resume( )
    {
        return start( );
    }

    @Override
    public void add( GLAutoDrawable drawable )
    {
        this.targets.add( drawable );
    }

    @Override
    public void remove( GLAutoDrawable drawable )
    {
        this.targets.remove( drawable );
    }

    @Override
    public UncaughtExceptionHandler getUncaughtExceptionHandler( )
    {
        return this.handler;
    }

    @Override
    public void setUncaughtExceptionHandler( UncaughtExceptionHandler handler )
    {
        this.handler = handler;
    }

    @Override
    public void setUpdateFPSFrames( int frames, PrintStream out )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public void resetFPSCounter( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public int getUpdateFPSFrames( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public long getFPSStartTime( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public long getLastFPSUpdateTime( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public long getLastFPSPeriod( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public float getLastFPS( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public int getTotalFPSFrames( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public long getTotalFPSDuration( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public float getTotalFPS( )
    {
        throw new UnsupportedOperationException( );
    }
}
