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

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.plot.timeline.event.Event;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;

/**
 * A painter responsible for making OpenGL calls to visualize an {@code Event}.
 *
 * @author ulman
 */
public interface EventPainter
{
    /**
     * <p>Renders the provided Event (potentially displaying its icon, label, time extents, etc...).<p>
     *
     * <p>Both the Event to be painted and the next Event in the row (the event with the next largest
     * start time) are provided. Only event should be rendered by this call. The nextEvent argument
     * is provided only as context to allow the EventPainter to modify its rendering to ensure that
     * it does not overlap with nextEvent.</p>
     *
     * @param context Glimpse Context, providing access to Axis, GlimpseBounds, and OpenGL context
     * @param Event the Event to be painted
     * @param nextEvent the next Event to be painted (as ordered by start time)
     * @param info parent EventPlotInfo of Event to be painted
     * @param posMin the min y (or x, depending on orientation) in pixel coordinates of the Event
     * @param posMax the max y (or x, depending on orientation) in pixel coordinates of the Event
     */
    public void paint( GlimpseContext context, Event event, Event nextEvent, EventPlotInfo info, int posMin, int posMax );
}
