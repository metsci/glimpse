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

import java.util.Set;

import com.metsci.glimpse.support.interval.IntervalSortedMultimap;
import com.metsci.glimpse.support.interval.Keyed;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.metsci.glimpse.util.units.time.TimeStampPosixMillisInt64;

/**
 * <p>Although this collection is sorted based on the Event start and end
 * TimeStamps, it uses {@link #equals(Object)} to satisfy the {@link Set} contract.
 * If two values are equal they must also have the same {@link Keyed#getStartTime()}
 * and {@link Keyed#getEndTime()}. However, two values which are not equal may have
 * the same start and end.</p> 
*/
public class EventIntervalSortedMultimap extends IntervalSortedMultimap<TimeStamp, Event>
{

    @Override
    public TimeStamp successor( TimeStamp t )
    {
        return TimeStampPosixMillisInt64.factory.fromPosixMillis( t.toPosixMillis( ) + 1 );
    }

    @Override
    public TimeStamp predecessor( TimeStamp t )
    {
        return TimeStampPosixMillisInt64.factory.fromPosixMillis( t.toPosixMillis( ) - 1 );
    }

    public static void main( String[] args )
    {
        Event a = new Event( "a", "a", TimeStamp.fromPosixMillis( 0 ), TimeStamp.fromPosixMillis( 1 ) );
        Event b = new Event( "b", "b", TimeStamp.fromPosixMillis( 2 ), TimeStamp.fromPosixMillis( 4 ) );
        Event c = new Event( "c", "c", TimeStamp.fromPosixMillis( 0 ), TimeStamp.fromPosixMillis( 2 ) );
        Event d = new Event( "d", "d", TimeStamp.fromPosixMillis( 1 ), TimeStamp.fromPosixMillis( 1 ) );
        Event e = new Event( "e", "e", TimeStamp.fromPosixMillis( 1 ), TimeStamp.fromPosixMillis( 1 ) );

        EventIntervalSortedMultimap map = new EventIntervalSortedMultimap( );
        map.add( a );
        map.add( b );
        map.add( c );
        map.add( d );
        map.add( a );
        map.add( e );

        System.out.println( map.getInterior( TimeStamp.fromPosixMillis( 1 ), TimeStamp.fromPosixMillis( 4 ) ) );
    }
}
