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
package com.metsci.glimpse.plot.timeline.event;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.metsci.glimpse.util.units.time.TimeStampPosixMillisInt64;

public class IntervalSortedMultimap
{
    SetMultimap<TimeStamp, Event> startMultimap;
    SetMultimap<TimeStamp, Event> endMultimap;

    SortedMap<TimeStamp, Collection<Event>> startMap;
    SortedMap<TimeStamp, Collection<Event>> endMap;

    public IntervalSortedMultimap( )
    {
        this.startMultimap = buildMap( );
        this.endMultimap = buildMap( );
        
        this.startMap = ( SortedMap<TimeStamp, Collection<Event>> ) startMultimap.asMap( );
        this.endMap = ( SortedMap<TimeStamp, Collection<Event>> ) endMultimap.asMap( );
    }

    protected TimeStamp successor( TimeStamp t )
    {
        return TimeStampPosixMillisInt64.factory.fromPosixMillis( t.toPosixMillis( ) + 1 );
    }

    protected TimeStamp predecessor( TimeStamp t )
    {
        return TimeStampPosixMillisInt64.factory.fromPosixMillis( t.toPosixMillis( ) - 1 );
    }

    protected Set<Event> getEvents( SortedMap<TimeStamp, Collection<Event>> map )
    {
        HashSet<Event> allEvents = new HashSet<Event>( );
        for ( Collection<Event> c : map.values( ) )
        {
            allEvents.addAll( c );
        }
        return allEvents;
    }

    public void addEvent( Event event )
    {
        startMultimap.put( event.getStartTime( ), event );
        endMultimap.put( event.getEndTime( ), event );
    }

    public void removeEvent( Event event )
    {
        startMultimap.remove( event.getStartTime( ), event );
        endMultimap.remove( event.getEndTime( ), event );
    }

    /**
     * Return all Events which contain the provided TimeStamp (inclusive on the
     * start time and end time).
     */
    public Set<Event> get( TimeStamp time )
    {
        return get( time, true, time, true );
    }

    /**
     * Return all Events whose time span overlaps with the provided bounds.
     * The start TimeStamp is inclusive and the TimeStamp is exclusive.
     */
    public Set<Event> get( TimeStamp start, TimeStamp end )
    {
        return get( start, true, end, false );
    }

    /**
     * Return all Events whose time span overlaps with the provided bounds.
     */
    public Set<Event> get( TimeStamp start, boolean startInclusive, TimeStamp end, boolean endInclusive )
    {
        if ( !startInclusive ) start = successor( start );
        if ( endInclusive ) end = successor( end );

        SortedMap<TimeStamp, Collection<Event>> tailMap = endMap.tailMap( start );
        SortedMap<TimeStamp, Collection<Event>> headMap = startMap.headMap( end );

        Set<Event> tailSet = getEvents( tailMap );
        Set<Event> headSet = getEvents( headMap );

        return Sets.intersection( headSet, tailSet );
    }
    
    public SetMultimap<TimeStamp, Event> getMap( TimeStamp start, boolean startInclusive, TimeStamp end, boolean endInclusive )
    {
        Set<Event> set = get( start, startInclusive, end, endInclusive );
        SetMultimap<TimeStamp, Event> map = buildMap( );
        
        for ( Event e : set ) map.put( e.getStartTime( ), e );
        
        return map;
    }
    
    /**
     * Returns all the Events which overlap in time with the provided Event.
     */
    public Set<Event> getOverlapping( Event event )
    {
        return get( event.getStartTime( ), event.getEndTime( ) );
    }
    
    /**
     * Return all Events whose time span is strictly contained within the provided bounds.
     * The start TimeStamp is inclusive and the TimeStamp is exclusive.
     */
    public Set<Event> getInterior( TimeStamp start, TimeStamp end )
    {
        return getInterior( start, true, end, false );
    }

    /**
     * Return all Events whose time span is strictly contained within the provided bounds.
     */
    public Set<Event> getInterior( TimeStamp start, boolean startInclusive, TimeStamp end, boolean endInclusive )
    {
        if ( !startInclusive ) start = successor( start );
        if ( endInclusive ) end = successor( end );

        SortedMap<TimeStamp, Collection<Event>> tailMap = endMap.headMap( end );
        SortedMap<TimeStamp, Collection<Event>> headMap = startMap.tailMap( start );

        Set<Event> tailSet = getEvents( tailMap );
        Set<Event> headSet = getEvents( headMap );

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
    
    public static void main( String[] args )
    {
        Event a = new Event( "a", "a",  TimeStamp.fromPosixMillis( 0 ) , TimeStamp.fromPosixMillis( 1 ) );
        Event b = new Event( "b", "b",  TimeStamp.fromPosixMillis( 2 ) , TimeStamp.fromPosixMillis( 4 ) );
        Event c = new Event( "c", "c",  TimeStamp.fromPosixMillis( 0 ) , TimeStamp.fromPosixMillis( 2 ) );
        Event d = new Event( "d", "d",  TimeStamp.fromPosixMillis( 1 ) , TimeStamp.fromPosixMillis( 1 ) );
        
        IntervalSortedMultimap map = new IntervalSortedMultimap( );
        map.addEvent( a );
        map.addEvent( b );
        map.addEvent( c );
        map.addEvent( d );
        map.addEvent( a );
        
        System.out.println( map.getInterior( TimeStamp.fromPosixMillis( 1 ), TimeStamp.fromPosixMillis( 4 ) ) );
    }
    
    public static SetMultimap<TimeStamp, Event> buildMap( )
    {
        TreeMap<TimeStamp, Collection<Event>> map = new TreeMap<TimeStamp, Collection<Event>>( );

        Supplier<LinkedHashSet<Event>> supplier = new Supplier<LinkedHashSet<Event>>( )
        {
            @Override
            public LinkedHashSet<Event> get( )
            {
                return new LinkedHashSet<Event>( );
            }
        };

        return Multimaps.newSetMultimap( map, supplier );
    }
}
