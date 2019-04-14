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
package com.metsci.glimpse.dnc.util;

import static com.google.common.base.Objects.equal;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SingletonEvictingBlockingQueue<V> implements BlockingQueue<V>
{

    protected final Lock lock;
    protected final Condition hasValue;
    protected V value;



    public SingletonEvictingBlockingQueue( )
    {
        this.lock = new ReentrantLock( );
        this.hasValue = lock.newCondition( );
        this.value = null;
    }



    @Override
    public boolean add( V v )
    {
        return setValueUninterruptibly( v );
    }

    @Override
    public boolean addAll( Collection<? extends V> c )
    {
        if ( c.isEmpty( ) )
        {
            return false;
        }
        else
        {
            V vLast = null;
            for ( V v : c )
            {
                requireNonNull( v );
                vLast = v;
            }
            return add( vLast );
        }
    }

    @Override
    public boolean offer( V v )
    {
        setValueUninterruptibly( v );

        // Return value is different than add -- indicates whether
        // the item was successfully added to the queue
        return true;
    }

    @Override
    public boolean offer( V v, long timeout, TimeUnit unit ) throws InterruptedException
    {
        setValueInterruptibly( v );

        // Return value is different than add -- indicates whether
        // the item was successfully added to the queue
        return true;
    }

    @Override
    public void put( V v ) throws InterruptedException
    {
        setValueInterruptibly( v );
    }

    protected boolean setValueUninterruptibly( V v )
    {
        requireNonNull( v );

        lock.lock( );
        try
        {
            if ( equal( v, value ) )
            {
                return false;
            }
            else
            {
                value = v;
                hasValue.signalAll( );
                return true;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected boolean setValueInterruptibly( V v ) throws InterruptedException
    {
        requireNonNull( v );

        lock.lockInterruptibly( );
        try
        {
            if ( equal( v, value ) )
            {
                return false;
            }
            else
            {
                value = v;
                hasValue.signalAll( );
                return true;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }



    @Override
    public V remove( )
    {
        lock.lock( );
        try
        {
            if ( value == null )
            {
                throw new NoSuchElementException( );
            }
            else
            {
                V v = value;
                value = null;
                return v;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public V poll( )
    {
        lock.lock( );
        try
        {
            V v = value;
            value = null;
            return v;
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public V poll( long timeout, TimeUnit unit ) throws InterruptedException
    {
        long nanosToWait = unit.toNanos( timeout );

        lock.lockInterruptibly( );
        try
        {
            while ( value == null )
            {
                if ( nanosToWait <= 0 )
                {
                    return null;
                }

                nanosToWait = hasValue.awaitNanos( nanosToWait );
            }

            V v = value;
            value = null;
            return v;
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public V take( ) throws InterruptedException
    {
        lock.lockInterruptibly( );
        try
        {
            while ( value == null )
            {
                hasValue.await( );
            }

            V v = value;
            value = null;
            return v;
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public V element( )
    {
        lock.lock( );
        try
        {
            if ( value == null )
            {
                throw new NoSuchElementException( );
            }
            else
            {
                return value;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public V peek( )
    {
        lock.lock( );
        try
        {
            return value;
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void clear( )
    {
        lock.lock( );
        try
        {
            value = null;
        }
        finally
        {
            lock.unlock( );
        }
    }



    @Override
    public int size( )
    {
        lock.lock( );
        try
        {
            return ( value == null ? 0 : 1 );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public boolean isEmpty( )
    {
        lock.lock( );
        try
        {
            return ( value == null );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public Iterator<V> iterator( )
    {
        lock.lock( );
        try
        {
            return new Iterator<V>( )
            {
                V vNext = value;

                public boolean hasNext( )
                {
                    return ( vNext != null );
                }

                public V next( )
                {
                    V vCopy = requireNonNull( vNext );
                    vNext = null;
                    return vCopy;
                }
            };
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public Object[] toArray( )
    {
        lock.lock( );
        try
        {
            if ( value == null )
            {
                return new Object[] { };
            }
            else
            {
                return new Object[] { value };
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T[] toArray( T[] a )
    {
        lock.lock( );
        try
        {
            if ( value == null )
            {
                return ( T[] ) Array.newInstance( a.getClass( ).getComponentType( ), 0 );
            }
            else
            {
                T[] array = ( T[] ) Array.newInstance( a.getClass( ).getComponentType( ), 1 );
                array[ 0 ] = ( T ) value;
                return array;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }



    @Override
    public boolean contains( Object o )
    {
        lock.lock( );
        try
        {
            return ( value != null && value.equals( o ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public boolean containsAll( Collection<?> c )
    {
        lock.lock( );
        try
        {
            for ( Object o : c )
            {
                boolean contains = ( value != null && value.equals( o ) );
                if ( !contains )
                {
                    return false;
                }
            }
            return true;
        }
        finally
        {
            lock.unlock( );
        }
    }



    @Override
    public boolean remove( Object o )
    {
        requireNonNull( o );

        lock.lock( );
        try
        {
            if ( value != null && value.equals( o ) )
            {
                value = null;
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public boolean removeAll( Collection<?> c )
    {
        lock.lock( );
        try
        {
            if ( value != null && c.contains( value ) )
            {
                value = null;
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public boolean retainAll( Collection<?> c )
    {
        lock.lock( );
        try
        {
            if ( value != null && !c.contains( value ) )
            {
                value = null;
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }



    @Override
    public int remainingCapacity( )
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public int drainTo( Collection<? super V> c )
    {
        return drainTo( c, 1 );
    }

    @Override
    public int drainTo( Collection<? super V> c, int maxElements )
    {
        requireNonNull( c );

        if ( c == this )
        {
            throw new IllegalArgumentException( );
        }

        if ( maxElements <= 0 )
        {
            return 0;
        }

        lock.lock( );
        try
        {
            if ( value == null )
            {
                return 0;
            }
            else
            {
                c.add( value );
                value = null;
                return 1;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

}
