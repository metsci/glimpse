/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.support.swing;

import static com.metsci.glimpse.util.concurrent.ConcurrencyUtils.newDaemonThreadFactory;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.FPSAnimator;

import jogamp.opengl.FPSCounterImpl;

/**
 * An FPSAnimator-like class which performs rendering on the Swing EDT.
 *
 * @see FPSAnimator
 * @author ulman
 */
public class SwingEDTAnimator implements GLAnimatorControl
{
    private static final Logger logger = Logger.getLogger( SwingEDTAnimator.class.getName( ) );

    protected final ScheduledExecutorService executor;
    protected final CopyOnWriteArrayList<GLAutoDrawable> targets;
    protected volatile UncaughtExceptionHandler handler;
    protected volatile ScheduledFuture<?> future;
    protected final double fps;
    protected final FPSCounterImpl fpsCounter;


    public SwingEDTAnimator( double fps )
    {
        // TODO: Might be cleaner to use a javax.swing.Timer
        // TODO: Call executor.shutdown() somewhere
        this.executor = newSingleThreadScheduledExecutor( newDaemonThreadFactory( new ThreadFactory( )
        {
            @Override
            public Thread newThread( Runnable r )
            {
                Thread thread = new Thread( r );
                thread.setName( SwingEDTAnimator.class.getSimpleName( ) );
                return thread;
            }
        } ) );

        this.fps = fps;

        this.targets = new CopyOnWriteArrayList<>( );

        // create a default exception handler which simply logs exceptions
        this.handler = new UncaughtExceptionHandler( )
        {
            @Override
            public void uncaughtException( GLAnimatorControl animator, GLAutoDrawable drawable, Throwable cause )
            {
                logWarning( logger, "Exception in: %s. Drawable: %s", cause, animator, drawable );
            }
        };

        this.fpsCounter = new FPSCounterImpl( );
    }

    protected synchronized void start0( )
    {
        // Volatile read (note that method is synchronized)
        if ( this.future != null )
        {
            return;
        }

        // Volatile write (note that method is synchronized)
        this.future = this.executor.scheduleAtFixedRate( ( ) ->
        {
            try
            {
                SwingUtilities.invokeAndWait( ( ) ->
                {
                    for ( GLAutoDrawable target : this.targets )
                    {
                        try
                        {
                            target.display( );
                        }
                        catch ( Exception e )
                        {
                            // Volatile read
                            UncaughtExceptionHandler handler = this.handler;

                            if ( handler != null )
                            {
                                handler.uncaughtException( this, target, e );
                            }
                        }
                    }
                    this.fpsCounter.tickFPS( );
                } );
            }
            catch ( InvocationTargetException | InterruptedException e )
            {
                logWarning( logger, "SwingEDTAnimator Error", e );
            }

        }, 0, ( int ) ( 1000.0 / fps ), TimeUnit.MILLISECONDS );
    }

    @Override
    public synchronized Thread getThread( )
    {
        // Swing doesn't provide a public way to get a reference to the EDT.
        //
        // JOGL usually just compares the thread returned by this method against
        // Thread.currentThread(), to check whether the current thread is the
        // animator thread. We handle that case by returning the current thread
        // directly, iff we are the Swing EDT.
        //
        // JOGL also has a shutdown hook that calls Thread.stop() on the animator
        // thread, if the animator thread is non-null. Calling stop() on the Swing
        // EDT is probably a bad idea anyway, so just return null if the current
        // thread is not the Swing EDT.
        //
        if ( SwingUtilities.isEventDispatchThread( ) )
        {
            return Thread.currentThread( );
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean isStarted( )
    {
        // Volatile read
        return ( this.future != null );
    }

    @Override
    public boolean isAnimating( )
    {
        // Volatile read
        return ( this.future != null );
    }

    @Override
    public boolean isPaused( )
    {
        // Volatile read
        return ( this.future == null );
    }

    @Override
    public synchronized boolean start( )
    {
        if ( this.isStarted( ) )
        {
            return false;
        }
        else
        {
            this.start0( );
            return true;
        }
    }

    @Override
    public synchronized boolean stop( )
    {
        if ( !this.isStarted( ) )
        {
            return false;
        }
        else
        {
            // Volatile read (note that method is synchronized)
            boolean success = this.future.cancel( false );

            if ( success )
            {
                // Volatile write (note that method is synchronized)
                this.future = null;
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    @Override
    public boolean pause( )
    {
        return this.stop( );
    }

    @Override
    public boolean resume( )
    {
        return this.start( );
    }

    @Override
    public synchronized void add( GLAutoDrawable drawable )
    {
        if ( !this.targets.contains( drawable ) )
        {
            this.targets.add( drawable );
            drawable.setAnimator( this );
        }
    }

    @Override
    public synchronized void remove( GLAutoDrawable drawable )
    {
        if ( this.targets.contains( drawable ) )
        {
            this.targets.remove( drawable );
            if ( this.targets.isEmpty( ) )
            {
                this.pause( );
            }
        }
    }

    @Override
    public UncaughtExceptionHandler getUncaughtExceptionHandler( )
    {
        // Volatile read
        return this.handler;
    }

    @Override
    public void setUncaughtExceptionHandler( UncaughtExceptionHandler handler )
    {
        // Volatile write
        this.handler = handler;
    }

    @Override
    public void setUpdateFPSFrames( int frames, PrintStream out )
    {
        this.fpsCounter.setUpdateFPSFrames( frames, out );
    }

    @Override
    public void resetFPSCounter( )
    {
        this.fpsCounter.resetFPSCounter( );
    }

    @Override
    public int getUpdateFPSFrames( )
    {
        return this.fpsCounter.getUpdateFPSFrames( );
    }

    @Override
    public long getFPSStartTime( )
    {
        return this.fpsCounter.getFPSStartTime( );
    }

    @Override
    public long getLastFPSUpdateTime( )
    {
        return this.fpsCounter.getLastFPSUpdateTime( );
    }

    @Override
    public long getLastFPSPeriod( )
    {
        return this.fpsCounter.getLastFPSPeriod( );
    }

    @Override
    public float getLastFPS( )
    {
        return this.fpsCounter.getLastFPS( );
    }

    @Override
    public int getTotalFPSFrames( )
    {
        return this.fpsCounter.getTotalFPSFrames( );
    }

    @Override
    public long getTotalFPSDuration( )
    {
        return this.fpsCounter.getTotalFPSDuration( );
    }

    @Override
    public float getTotalFPS( )
    {
        return this.fpsCounter.getTotalFPS( );
    }
}
