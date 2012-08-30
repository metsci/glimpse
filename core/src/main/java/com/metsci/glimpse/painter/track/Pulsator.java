package com.metsci.glimpse.painter.track;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.metsci.glimpse.util.primitives.IntsArray;

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
    
    protected IntsArray ids;

    protected ReentrantLock lock;
    protected Condition pauseCondition;

    public Pulsator( TrackPainter painter, long delayMillis, float minSize, float stepSize, float maxSize )
    {
        this.painter = painter;

        this.delayMillis = delayMillis;

        this.min = minSize;
        this.max = maxSize;
        this.step = stepSize;

        this.start = maxSize;
        this.direction = -1.0f;
        this.paused = false;

        this.ids = new IntsArray( );
        
        this.lock = new ReentrantLock( );
        this.pauseCondition = this.lock.newCondition( );
    }

    public Pulsator( TrackPainter painter )
    {
        this( painter, 10, 10f, 0.1f, 15f );
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

    public void addId( int id )
    {
        this.lock.lock( );
        try
        {
            this.ids.append( id );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void removeId( int id )
    {
        this.lock.lock( );
        try
        {
            this.ids.remove( id );
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
    
                        for ( int i = 0; i < ids.n; i++ )
                        {
                            painter.setPointSize( ids.a[i], size );
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

        t.start( );
    }
}
