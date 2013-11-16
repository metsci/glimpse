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
package com.metsci.glimpse.support.interval;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * <p>A data structure for storing {@link Keyed} values (events with start and end times).
 * IntervalSortedMultimap supports querying for all values which contain a particular time, all
 * values which overlap with a particular time window, or all values which
 * are completely contained within a time window.</p>
 * 
 * <p>Although this collection is sorted based on the Event start and end
 * TimeStamps, it uses {@link #equals(Object)} to satisfy the {@link Set} contract.
 * If two values are equal they must also have the same {@link Keyed#getStartTime()}
 * and {@link Keyed#getEndTime()}. However, two values which are not equal may have
 * the same start and end.</p> 
 * 
 * @author ulman
 */
public abstract class IntervalSortedMultimap<K extends Comparable<K>, V extends Keyed<K>>
{
    SetMultimap<K, V> startMultimap;
    SetMultimap<K, V> endMultimap;

    SortedMap<K, Collection<V>> startMap;
    SortedMap<K, Collection<V>> endMap;

    public IntervalSortedMultimap( )
    {
        startMultimap = buildMap( );
        endMultimap = buildMap( );

        startMap = ( SortedMap<K, Collection<V>> ) startMultimap.asMap( );
        endMap = ( SortedMap<K, Collection<V>> ) endMultimap.asMap( );
    }

    protected abstract K successor( K t );

    protected abstract K predecessor( K t );

    protected Set<V> getEvents( SortedMap<K, Collection<V>> map )
    {
        HashSet<V> allEvents = new HashSet<V>( );
        for ( Collection<V> c : map.values( ) )
        {
            allEvents.addAll( c );
        }
        return allEvents;
    }

    public void clear( )
    {
        startMultimap.clear( );
        endMultimap.clear( );
    }
    
    public void add( V event )
    {
        startMultimap.put( event.getStartTime( ), event );
        endMultimap.put( event.getEndTime( ), event );
    }

    public void remove( V event )
    {
        startMultimap.remove( event.getStartTime( ), event );
        endMultimap.remove( event.getEndTime( ), event );
    }

    public SetMultimap<K, V> getEndMultimap( )
    {
        return Multimaps.unmodifiableSetMultimap( endMultimap );
    }

    public SetMultimap<K, V> getStartMultimap( )
    {
        return Multimaps.unmodifiableSetMultimap( startMultimap );
    }

    public SortedMap<K, Collection<V>> getStartMap( )
    {
        return Collections.unmodifiableSortedMap( startMap );
    }

    public SortedMap<K, Collection<V>> getEndMap( )
    {
        return Collections.unmodifiableSortedMap( endMap );
    }

    /**
     * Returns the earliest start TimeStamp among Events in the map. There may be multiple
     * Events with this earliest start time.
     */
    public K earliestTime( )
    {
        return startMap.firstKey( );
    }

    /**
     * Returns the latest end TimeStamp among Events in the map. There may be multiple
     * Events with this latest end time.
     */
    public K latestTime( )
    {
        return endMap.lastKey( );
    }

    /**
     * Return all Events which contain the provided TimeStamp (inclusive on the
     * start time and end time).
     */
    public Set<V> get( K time )
    {
        return get( time, true, time, true );
    }

    /**
     * Return all Events whose time span overlaps with the provided bounds.
     * The start TimeStamp is inclusive and the TimeStamp is exclusive.
     */
    public Set<V> get( K start, K end )
    {
        return get( start, true, end, false );
    }

    /**
     * Return all Events whose time span overlaps with the provided bounds.
     */
    public Set<V> get( K start, boolean startInclusive, K end, boolean endInclusive )
    {
        if ( !startInclusive ) start = successor( start );
        if ( endInclusive ) end = successor( end );

        SortedMap<K, Collection<V>> tailMap = endMap.tailMap( start );
        SortedMap<K, Collection<V>> headMap = startMap.headMap( end );

        Set<V> tailSet = getEvents( tailMap );
        Set<V> headSet = getEvents( headMap );

        return Sets.intersection( headSet, tailSet );
    }

    public SetMultimap<K, V> getMap( K start, boolean startInclusive, K end, boolean endInclusive )
    {
        Set<V> set = get( start, startInclusive, end, endInclusive );
        SetMultimap<K, V> map = buildMap( );

        for ( V e : set )
            map.put( e.getStartTime( ), e );

        return map;
    }

    /**
     * Returns all the Events which overlap in time with the provided Event.
     */
    public Set<V> getOverlapping( V event )
    {
        return get( event.getStartTime( ), event.getEndTime( ) );
    }

    /**
     * Return all Events whose time span is strictly contained within the provided bounds.
     * The start TimeStamp is inclusive and the TimeStamp is exclusive.
     */
    public Set<V> getInterior( K start, K end )
    {
        return getInterior( start, true, end, false );
    }

    /**
     * Return all Events whose time span is strictly contained within the provided bounds.
     */
    public Set<V> getInterior( K start, boolean startInclusive, K end, boolean endInclusive )
    {
        if ( !startInclusive ) start = successor( start );
        if ( endInclusive ) end = successor( end );

        SortedMap<K, Collection<V>> tailMap = endMap.headMap( end );
        SortedMap<K, Collection<V>> headMap = startMap.tailMap( start );

        Set<V> tailSet = getEvents( tailMap );
        Set<V> headSet = getEvents( headMap );

        return Sets.intersection( headSet, tailSet );
    }

    public boolean isEmpty( )
    {
        return size( ) == 0;
    }

    public int size( )
    {
        // startMap and endMap should always be the same size
        return startMap.size( );
    }

    public static <K, V> SetMultimap<K, V> buildMap( )
    {
        TreeMap<K, Collection<V>> map = new TreeMap<K, Collection<V>>( );

        Supplier<LinkedHashSet<V>> supplier = new Supplier<LinkedHashSet<V>>( )
        {
            @Override
            public LinkedHashSet<V> get( )
            {
                return new LinkedHashSet<V>( );
            }
        };

        return Multimaps.newSetMultimap( map, supplier );
    }
}
