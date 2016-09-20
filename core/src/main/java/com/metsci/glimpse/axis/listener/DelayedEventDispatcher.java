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
package com.metsci.glimpse.axis.listener;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * A helper class used to rate-limit events. The DelayedEventDispatcher should
 * be notified whenever events of interest occur via {@link #eventOccurred(Object)}.
 * Then, when no events have occurred for the duration of time specified in the
 * constructor, {@link #eventDispatch(Object)} is called.
 *
 * @author ulman
 */
public abstract class DelayedEventDispatcher<D>
{
    public static Logger logger = Logger.getLogger( DelayedEventDispatcher.class.getName( ) );

    private long delayMillis;
    private long lastRunTimeMillis;

    private Thread thread;
    private ReentrantLock lock;
    private Condition cond;
    private volatile boolean updated;
    private volatile boolean shutdown;

    private D data;

    public DelayedEventDispatcher( )
    {
        this( 1000l / 60l );
    }

    public DelayedEventDispatcher( double delayHz )
    {
        this( ( long ) ( 1000 / delayHz ) );
    }

    public DelayedEventDispatcher( long delayMillis )
    {
        this.delayMillis = delayMillis;
        this.lastRunTimeMillis = System.currentTimeMillis( ) - delayMillis;

        this.lock = new ReentrantLock( );
        this.cond = this.lock.newCondition( );

        this.thread = new Thread( )
        {
            @Override
            public void run( )
            {
                while ( true )
                {
                    lock.lock( );
                    try
                    {
                        // wait until eventOccurred is called
                        while ( !updated )
                        {
                            try
                            {
                                cond.await( );
                            }
                            catch ( InterruptedException e )
                            {
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                        logWarning( logger, "Exception in RateLimitedAxisListener", e );
                    }
                    finally
                    {
                        lock.unlock( );
                    }

                    long time;

                    if ( shutdown ) return;

                    // wait until enough time has passed between eventOccurred
                    while ( ( time = millisToNextUpdate( ) ) > 0 )
                    {
                        try
                        {
                            Thread.sleep( time );
                        }
                        catch ( InterruptedException e )
                        {
                        }
                    }

                    if ( shutdown ) return;

                    eventDispatch0( );
                }
            }
        };

        this.thread.setDaemon( true );
        this.thread.setName( "delayed-event-dispatcher" );
    }

    public void start( )
    {
        this.thread.start( );
    }

    public void dispose( )
    {
        lock.lock( );
        try
        {
            shutdown = true;
            updated = true;
            cond.signalAll( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected long millisToNextUpdate( )
    {
        lock.lock( );
        try
        {
            long time = System.currentTimeMillis( );
            long timeSinceLast = time - lastRunTimeMillis;
            long timeToNext = delayMillis - timeSinceLast;

            return timeToNext;
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected void eventDispatch0( )
    {
        D data_temp = null;

        lock.lock( );
        try
        {
            updated = false;
            data_temp = data;
        }
        finally
        {
            lock.unlock( );
        }

        this.eventDispatch( data_temp );
    }

    public void eventOccurred( D _data )
    {
        lock.lock( );
        try
        {
            lastRunTimeMillis = System.currentTimeMillis( );
            data = _data;
            updated = true;
            cond.signalAll( );
        }
        catch ( Exception e )
        {
            logWarning( logger, "Exception in RateLimitedAxisListener", e );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public abstract void eventDispatch( D data );
}
