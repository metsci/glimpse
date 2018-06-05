package com.metsci.glimpse.support;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Provides one way of asynchronously computing a long-running task for painting.
 *
 * For example:
 * <pre>
 * <code>
 * public class MyPainter extends GlimpsePainterBase
 * {
 *     int paintFrame;
 *     PaintingCache<Integer, V> cache;
 *
 *     public MyPainter( )
 *     {
 *         paintFrame = -1;
 *         cache = new PaintingCache<>( this::computeForFrame );
 *     }
 *
 *     @Override
 *     public void doPaintTo( GlimpseContext context )
 *     {
 *         int selectedFrame = // ...
 *         if ( paintFrame != selectedFrame )
 *         {
 *             V value = cache.get( selectedFrame );
 *             if ( value == null )
 *             {
 *                 paintFrame = -1;
 *                 return;
 *             }
 *             else
 *             {
 *                 paintFrame = selectedFrame;
 *                 loadDataIntoPainter( value );
 *             }
 *         }
 *     }
 *
 *     V computeForFrame( int key )
 *     {
 *         // ...
 *     }
 * }
 * </code>
 * </pre>
 *
 * @author borkholder
 */
public class PainterCache<K, V>
{
    private static final Logger LOGGER = Logger.getLogger( PainterCache.class.getName( ) );

    protected final Function<K, V> computeF;
    protected final Cache<K, V> cache;
    protected final Map<K, Object> locks;
    protected final Executor executor;

    public PainterCache( Function<K, V> computeF )
    {
        this( computeF, ForkJoinPool.commonPool( ) );
    }

    public PainterCache( Function<K, V> computeF, Executor executor )
    {
        this.computeF = computeF;
        this.executor = executor;
        cache = CacheBuilder.newBuilder( ).softValues( ).build( );
        locks = new WeakHashMap<>( );
    }

    public V get( K key )
    {
        V value = cache.getIfPresent( key );
        if ( value != null )
        {
            return value;
        }

        Object lock = getLock( key );
        executor.execute( ( ) -> wrapForException( lock, key ) );
        return null;
    }

    /**
     * Allow any number of keys to be computed at once, but only one compute per key.
     */
    protected Object getLock( K key )
    {
        synchronized ( locks )
        {
            Object lock = locks.get( key );
            if ( lock == null )
            {
                lock = new Object( );
                locks.put( key, lock );
            }

            return lock;
        }
    }

    protected void wrapForException( Object lock, K key )
    {
        try
        {
            compute( lock, key );
        }
        catch ( Exception ex )
        {
            logWarning( LOGGER, "Error in background compute for %s", ex, key );
        }
    }

    protected void compute( Object lock, K key )
    {
        synchronized ( lock )
        {
            V value = cache.getIfPresent( key );
            if ( value != null )
            {
                return;
            }

            value = computeF.apply( key );
            cache.put( key, value );
        }
    }

}
