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
package com.metsci.glimpse.support;

import static com.metsci.glimpse.util.concurrent.ConcurrencyUtils.newDaemonThreadFactory;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
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
 *     PaintingCache&lt;Integer, V&gt; cache;
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
 *     public V computeForFrame( int key )
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

    public static Executor SHARED_EXEC;

    static
    {
        ThreadFactory threadFactory = newDaemonThreadFactory( "glimpse-painter-cache-%d" );
        SHARED_EXEC = newFixedThreadPool( max( getRuntime( ).availableProcessors( ) - 2, 1 ), threadFactory );
    }

    protected final Function<K, V> computeF;
    protected final Cache<K, V> cache;
    protected final Map<K, Object> locks;
    protected final Executor executor;

    public PainterCache( Function<K, V> computeF )
    {
        this( computeF, SHARED_EXEC );
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
        return cache.getIfPresent( key );
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

    public void clear( )
    {
        synchronized ( locks )
        {
            locks.clear( );
            cache.invalidateAll( );
        }
    }
}
