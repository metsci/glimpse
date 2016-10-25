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
package com.metsci.glimpse.plot.timeline.event.paint;

import com.metsci.glimpse.plot.timeline.event.Event;

/**
 * A helper data structure used by {@code DefaultGroupedEventPainter} to manage
 * bounds of timeline events being rendered.
 *
 * @author ulman
 */
public class EventDrawInfo
{
    // the event to draw
    protected Event event;

    // the next event in the same row, as ordered by start time
    // (or null if this is the last event in the row
    protected Event nextEvent;

    // the top and bottom bounds of the event (in pixels) orthogonal to the time axis
    protected int posMin;
    protected int posMax;

    public EventDrawInfo( Event event, Event nextEvent, int posMin, int posMax )
    {
        this.event = event;
        this.nextEvent = nextEvent;
        this.posMin = posMin;
        this.posMax = posMax;
    }

    public Event getEvent( )
    {
        return event;
    }

    public Event getNextEvent( )
    {
        return nextEvent;
    }

    public int getPosMin( )
    {
        return posMin;
    }

    public int getPosMax( )
    {
        return posMax;
    }
}