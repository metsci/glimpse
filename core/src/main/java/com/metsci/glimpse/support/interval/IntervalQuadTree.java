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
package com.metsci.glimpse.support.interval;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.metsci.glimpse.util.quadtree.longvalued.LongQuadTreeObjects;
import com.metsci.glimpse.util.units.time.TimeStamp;

public abstract class IntervalQuadTree<V>
{
    // set is necessary to enforce set semantics, which LongQuadTreeObjects does not do
    protected Map<V, V> map;
    protected LongQuadTreeObjects<V> tree;
    protected int maxBucketSize;

    public IntervalQuadTree( )
    {
        this( 100 );
    }

    public IntervalQuadTree( int maxBucketSize )
    {
        this.maxBucketSize = maxBucketSize;
        this.tree = buildTree( maxBucketSize );
        this.map = Maps.newHashMap( );
    }

    protected LongQuadTreeObjects<V> buildTree( int maxBucketSize )
    {
        // x is start time, y is end time
        return new LongQuadTreeObjects<V>( maxBucketSize )
        {
            @Override
            public long x( V v )
            {
                return getStartTimeMillis( v );
            }

            @Override
            public long y( V v )
            {
                return getEndTimeMillis( v );
            }
        };
    }

    public abstract long getStartTimeMillis( V v );

    public abstract long getEndTimeMillis( V v );

    public Set<V> getAll( )
    {
        return Collections.unmodifiableSet( this.map.keySet( ) );
    }

    public void clear( )
    {
        this.tree = buildTree( maxBucketSize );
        this.map.clear( );
    }

    public void add( V value )
    {
        V oldValue = this.map.get( value );
        if ( oldValue != null )
        {
            this.tree.remove( oldValue );
        }

        this.tree.add( value );
        this.map.put( value, value );
    }

    public void remove( V value )
    {
        V oldValue = this.map.remove( value );

        if ( oldValue != null )
        {
            this.tree.remove( oldValue );
        }
    }

    public Collection<V> get( TimeStamp time )
    {
        return get( time, true, time, true );
    }

    /**
     * @param start inclusive start TimeStamp
     * @param end exclusive end TimeStamp
     *
     * @return all Events whose time span overlaps with the provided bounds.
     */
    public Collection<V> get( TimeStamp start, TimeStamp end )
    {
        return get( start, true, end, false );
    }

    /**
     * @return all Events whose time span overlaps with the provided bounds.
     */
    public Collection<V> get( TimeStamp start, boolean startInclusive, TimeStamp end, boolean endInclusive )
    {
        return get( start.toPosixMillis( ), startInclusive, end.toPosixMillis( ), endInclusive );
    }

    /**
     * @param time time expressed as posix milliseconds
     * @see #get(TimeStamp)
     */
    public Collection<V> get( long time )
    {
        return get( time, true, time, true );
    }

    /**
     * @param start time expressed as posix milliseconds
     * @param end time expressed as posix milliseconds
     * @see #get(TimeStamp, TimeStamp)
     */
    public Collection<V> get( long start, long end )
    {
        return get( start, true, end, false );
    }

    /**
     * @param start time expressed as posix milliseconds
     * @param end time expressed as posix milliseconds
     * @see #get(TimeStamp, boolean, TimeStamp, boolean )
     */
    public Collection<V> get( long start, boolean startInclusive, long end, boolean endInclusive )
    {
        // search indices are inclusive by default -- adjust by 1 millisecond to make non-inclusive
        if ( !startInclusive && start != Long.MAX_VALUE ) start += 1;
        if ( !endInclusive && end != Long.MIN_VALUE ) end -= 1;

        return this.tree.search( Long.MIN_VALUE, end, start, Long.MAX_VALUE );
    }

    public Collection<V> getOverlapping( V value )
    {
        return get( getStartTimeMillis( value ), getEndTimeMillis( value ) );
    }

    public Collection<V> getInterior( TimeStamp start, TimeStamp end )
    {
        return getInterior( start.toPosixMillis( ), end.toPosixMillis( ) );
    }

    public Collection<V> getInterior( TimeStamp start, boolean startInclusive, TimeStamp end, boolean endInclusive )
    {
        return getInterior( start.toPosixMillis( ), startInclusive, end.toPosixMillis( ), endInclusive );
    }

    public Collection<V> getInterior( long start, long end )
    {
        return getInterior( start, true, end, false );
    }

    public Collection<V> getInterior( long start, boolean startInclusive, long end, boolean endInclusive )
    {
        // search indices are inclusive by default -- adjust by 1 millisecond to make non-inclusive
        if ( !startInclusive && start != Long.MAX_VALUE ) start += 1;
        if ( !endInclusive && end != Long.MIN_VALUE ) end -= 1;

        return this.tree.search( start, end, start, end );
    }

    public boolean isEmpty( )
    {
        return size( ) == 0;
    }

    public int size( )
    {
        return this.map.size( );
    }
}
