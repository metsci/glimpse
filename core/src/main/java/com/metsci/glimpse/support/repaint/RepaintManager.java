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
package com.metsci.glimpse.support.repaint;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.metsci.glimpse.canvas.GlimpseCanvas;

/**
 * Manages repainting of an arbitrary number of
 * {@link com.metsci.glimpse.canvas.GlimpseCanvas} on a single thread.
 * A Glimpse application should construct one {@code RepaintManager}
 * and attach all {@link com.metsci.glimpse.canvas.GlimpseCanvas}
 * which it creates to that single {@code RepaintManager}.
 *
 *
 * @author ulman
 */
public class RepaintManager
{
    public static final Logger logger = Logger.getLogger( RepaintManager.class.getName( ) );

    public static final int DELAY = 10;

    public static RepaintManager newRepaintManager( GlimpseCanvas canvas )
    {
        RepaintManager manager = new RepaintManager( canvas );
        manager.start( );
        return manager;
    }

    protected ScheduledExecutorService executor;
    protected Thread thread;

    protected boolean started;
    protected boolean shutdown;
    protected boolean paused;
    protected ReentrantLock lock;
    protected Condition pause;

    protected Set<GlimpseCanvas> canvasList;

    public RepaintManager( GlimpseCanvas canvas )
    {
        this( );

        addGlimpseCanvas( canvas );
    }

    public RepaintManager( )
    {
        this.canvasList = new CopyOnWriteArraySet<GlimpseCanvas>( );

        this.started = false;
        this.shutdown = false;
        this.paused = false;

        this.lock = new ReentrantLock( );
        this.pause = this.lock.newCondition( );

        this.executor = Executors.newScheduledThreadPool( 1, new ThreadFactory( )
        {
            @Override
            public Thread newThread( Runnable runnable )
            {
                thread = new Thread( runnable );
                thread.setName( "repaint-manager" );
                thread.setDaemon( true );
                return thread;
            }
        } );
    }

    public void addGlimpseCanvas( GlimpseCanvas canvas )
    {
        lock.lock( );
        try
        {
            this.canvasList.add( canvas );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeGlimpseCanvas( GlimpseCanvas canvas )
    {
        lock.lock( );
        try
        {
            this.canvasList.remove( canvas );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void shutdown( )
    {
        lock.lock( );
        try
        {
            this.executor.shutdown( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void start( )
    {
        lock.lock( );
        try
        {
            if ( !started )
            {
                executor.scheduleWithFixedDelay( newRepaintRunnable( ), 0, DELAY, TimeUnit.MILLISECONDS );
                started = true;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void play( )
    {
        lock.lock( );
        try
        {
            paused = false;
            pause.signalAll( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void pause( )
    {
        lock.lock( );
        try
        {
            paused = true;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void asyncExec( Runnable runnable )
    {
        executor.execute( runnable );
    }

    public void syncExec( Runnable runnable )
    {
        try
        {
            executor.submit( runnable ).get( );
        }
        catch ( InterruptedException e )
        {
            logWarning( logger, "Trouble in RepaintManager", e );
        }
        catch ( ExecutionException e )
        {
            logWarning( logger, "Trouble in RepaintManager", e );
        }
    }

    public boolean checkThread( )
    {
        return Thread.currentThread( ).equals( thread );
    }

    public Runnable newRepaintRunnable( )
    {
        return new RepaintRunnable( );
    }

    public class RepaintRunnable implements Runnable
    {
        @Override
        public void run( )
        {
            try
            {
                lock.lock( );
                try
                {
                    while ( paused )
                    {
                        pause.await( );
                    }
                }
                finally
                {
                    lock.unlock( );
                }

                for ( GlimpseCanvas canvas : canvasList )
                {
                    canvas.paint( );
                }
            }
            catch ( Exception e )
            {
                logWarning( logger, "Problem Repainting...", e );
            }
        }
    }
}
