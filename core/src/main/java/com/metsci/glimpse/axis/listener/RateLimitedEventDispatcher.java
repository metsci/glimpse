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
 * A helper method used by RateLimitedAxisListener1D and RateLimitedAxisListener2D
 * for receiving notifications that the min/max bounds of an Axis1D or Axis2D have changed
 * while guaranteeing that notifications arrive no faster than a specified rate.
 *
 * This class is threaded in order to provide an additional guarantee that no axis
 * update will be missed. That is, if an axisUpdate( ) call is suppressed, but no
 * subsequent axis updates occur, axisUpdate( ) will be called one last time when the
 * rate allows.
 *
 * @author ulman
 * @see com.metsci.glimpse.axis.Axis1D
 */
public abstract class RateLimitedEventDispatcher<D>
{
    public static Logger logger = Logger.getLogger( RateLimitedEventDispatcher.class.getName( ) );

    private long idleTimeMillis;
    private long lastRunTimeMillis;

    private Thread thread;
    private ReentrantLock lock;
    private Condition cond;
    private volatile boolean updated;
    private volatile boolean shutdown;

    private D data;

    public RateLimitedEventDispatcher( )
    {
        this( 1000l / 60l );
    }

    public RateLimitedEventDispatcher( double maxFreqHz )
    {
        this( ( long ) ( 1000 / maxFreqHz ) );
    }

    public RateLimitedEventDispatcher( long _idleTimeMillis )
    {
        this.idleTimeMillis = _idleTimeMillis;
        this.lastRunTimeMillis = System.currentTimeMillis( ) - idleTimeMillis;

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
        this.thread.setName( "rate-limited-axis-listener" );

        // XXX: FIX, Don't start a thread in the constructor b/c subclasses possibly won't function properly
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
        long time = System.currentTimeMillis( );
        long timeSinceLast = time - lastRunTimeMillis;
        long timeToNext = idleTimeMillis - timeSinceLast;

        return timeToNext;
    }

    protected void eventDispatch0( )
    {
        D data_temp = null;

        lock.lock( );
        try
        {
            this.lastRunTimeMillis = System.currentTimeMillis( );
            this.updated = false;
            data_temp = this.data;
        }
        finally
        {
            lock.unlock( );
        }

        this.eventDispatch( data_temp );
    }

    public void eventOccurred( D _data )
    {
        // in the common case (we've already gotten lots up axisUpdated calls)
        // and updated is already true, avoid the overhead of calling lock.lock( )
        // this works because updated is declared as volatile, and is thus guaranteed
        // to see updates to its value from other threads
        if ( !updated )
        {
            lock.lock( );
            try
            {
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
    }

    public abstract void eventDispatch( D data );
}
