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
package com.metsci.glimpse.plot.timeline.event;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * When a {@code StackedTimePlot2D} is zoomed out very far, Events
 * can be squashed very close together in pixel space, making them hard
 * to see. {@code EventPlotInfo} can alleviate this problem by automatically
 * creating combined Events which represent aggregations of many user created
 * Events.
 *
 * @author ulman
 */
public class AggregateEvent extends Event
{
    // events can be nested (when many events are very close together
    // on the screen they can be combined into a single event for readability)
    protected Set<Event> children;

    // constructor used to create aggregate events
    protected AggregateEvent( Set<Event> children, TimeStamp startTime, TimeStamp endTime )
    {
        super( UUID.randomUUID( ), null, startTime, endTime );

        this.children = ImmutableSet.copyOf( children );

        this.isEditable = false;
    }

    /**
     * EventPlotInfo can automatically create synthetic groups of Events when the timeline
     * is zoomed out far enough that a bunch of Events are crowded into the same space.
     * The individual constituent Events can be accessed via this method.
     * User created Events never have children.
     */
    @Override
    public Iterator<Event> iterator( )
    {
        return this.children.iterator( );
    }

    /**
     * @see #iterator()
     */
    @Override
    public int getEventCount( )
    {
        return this.children.size( );
    }

    @Override
    public void setEditable( boolean isEditable )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setEndTimeMoveable( boolean isEndTimeMoveable )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setStartTimeMoveable( boolean isStartTimeMoveable )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setResizeable( boolean isResizeable )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setMaxTimeSpan( double maxTimeSpan )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setMinTimeSpan( double minTimeSpan )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setTimes( TimeStamp startTime, TimeStamp endTime, boolean force )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setFloatingRow( )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setFixedRow( int rowIndex )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setTimes( TimeStamp startTime, TimeStamp endTime )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setStartTime( TimeStamp startTime )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setEndTime( TimeStamp endTime )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }
}