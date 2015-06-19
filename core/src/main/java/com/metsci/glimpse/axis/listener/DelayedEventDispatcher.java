package com.metsci.glimpse.axis.listener;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

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
