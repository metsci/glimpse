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
package com.metsci.glimpse.painter.track;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Pulsator
{
    protected TrackPainter painter;
    protected long delayMillis;
    protected float min;
    protected float max;
    protected float start;
    protected float step;

    protected float size;
    protected float direction;
    protected boolean paused;
    protected boolean stopped;

    protected float defaultSize;

    protected Collection<Object> ids;

    protected ReentrantLock lock;
    protected Condition pauseCondition;

    public Pulsator( TrackPainter painter, long delayMillis, float minSize, float stepSize, float maxSize )
    {
        this.painter = painter;

        this.delayMillis = delayMillis;

        this.defaultSize = minSize;
        this.min = minSize;
        this.max = maxSize;
        this.step = stepSize;

        this.start = maxSize;
        this.direction = -1.0f;
        this.paused = false;
        this.stopped = false;

        this.ids = new LinkedList<Object>( );

        this.lock = new ReentrantLock( );
        this.pauseCondition = this.lock.newCondition( );
    }

    public Pulsator( TrackPainter painter )
    {
        this( painter, 10, 10f, 0.1f, 15f );
    }

    /**
     * When an id is removed from the Pulsator, its point size is reset to the default size.
     * @param size
     */
    public void setDefaultSize( float size )
    {
        this.defaultSize = size;
    }

    public float getDefaultSize( )
    {
        return this.defaultSize;
    }

    public void resetSize( )
    {
        this.lock.lock( );
        try
        {
            this.size = start;
            this.direction = -1.0f;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void setPaused( boolean paused )
    {
        this.lock.lock( );
        try
        {
            this.paused = paused;

            if ( !this.paused )
            {
                this.pauseCondition.signalAll( );
            }
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public long getDelayMillis( )
    {
        this.lock.lock( );
        try
        {
            return delayMillis;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void setDelayMillis( long delayMillis )
    {
        this.lock.lock( );
        try
        {
            this.delayMillis = delayMillis;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public float getMin( )
    {
        this.lock.lock( );
        try
        {
            return min;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void setMin( float min )
    {
        this.lock.lock( );
        try
        {
            this.min = min;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public float getMax( )
    {
        this.lock.lock( );
        try
        {
            return max;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void setMax( float max )
    {
        this.lock.lock( );
        try
        {
            this.max = max;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public float getStep( )
    {
        this.lock.lock( );
        try
        {
            return step;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void setStep( float step )
    {
        this.lock.lock( );
        try
        {
            this.step = step;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public float getSize( )
    {
        this.lock.lock( );
        try
        {
            return size;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void setSize( float size )
    {
        this.lock.lock( );
        try
        {
            this.size = size;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void removeAllIds( )
    {
        this.lock.lock( );
        try
        {
            for ( Object id : ids )
            {
                this.painter.setPointSize( id, defaultSize );
            }

            this.ids.clear( );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void addId( Object id )
    {
        this.lock.lock( );
        try
        {
            this.ids.add( id );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void removeId( Object id )
    {
        this.lock.lock( );
        try
        {
            this.ids.remove( id );
            this.painter.setPointSize( id, defaultSize );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void stop( )
    {
        this.lock.lock( );
        try
        {
            setPaused( false );
            this.stopped = true;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void start( )
    {
        Thread t = new Thread( "Pulsator" )
        {
            @Override
            public void run( )
            {
                while ( true )
                {
                    lock.lock( );
                    try
                    {
                        while ( paused )
                        {
                            try
                            {
                                pauseCondition.await( );
                            }
                            catch ( InterruptedException e )
                            {
                            }
                        }

                        if ( stopped ) return;

                        if ( size > max )
                        {
                            direction = -1.0f;
                            size = max;
                        }
                        else if ( size < min )
                        {
                            direction = 1.0f;
                            size = min;
                        }

                        size += step * direction;

                        for ( Object id : ids )
                        {
                            painter.setPointSize( id, size );
                        }
                    }
                    finally
                    {
                        lock.unlock( );
                    }

                    try
                    {
                        Thread.sleep( delayMillis );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
            }
        };

        t.setDaemon( true );
        t.start( );
    }
}
