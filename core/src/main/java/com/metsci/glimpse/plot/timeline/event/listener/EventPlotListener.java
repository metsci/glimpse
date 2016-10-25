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
package com.metsci.glimpse.plot.timeline.event.listener;

import java.util.Set;

import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.event.Event;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Listener interface for receiving notifications when the mouse interacts with Events or when
 * Event start and end times are updated.
 *
 * @author ulman
 */
public interface EventPlotListener
{
    /**
     * Indicates the mouse moved outside of the events contained in the EventSelection Set.
     *
     * @param e the original MouseEvent which caused this eventsExited call
     * @param events the events which the mouse moved outside of
     * @param time the time corresponding to the mouse's current position
     */
    public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    /**
     * Indicates the mouse moved into of the Events contained in the EventSelection Set.
     *
     * @see EventPlotListener#eventsExited(GlimpseMouseEvent, Set, TimeStamp)
     */
    public void eventsEntered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    /**
     * Indicates that the mouse moved while inside the Events contained in the EventSelection Set.
     * EventSelection also provides information about what part of each Event the mouse is near
     * (the start or end edge, the icon, the text label, etc...).
     *
     * @see EventPlotListener#eventsExited(GlimpseMouseEvent, Set, TimeStamp)
     */
    public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    /**
     * Indicates the mouse clicked on the Events contained in the EventSelection Set.
     *
     * @see EventPlotListener#eventsExited(GlimpseMouseEvent, Set, TimeStamp)
     */
    public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    /**
     * <p>Indicates that the provided Event was updated by the user. If enabled, the user can click
     * and drag to change the start and/or end time of Events. When the user does this, eventUpdated
     * is called.</p>
     *
     * <p>If the update was caused by user mouse actions, the GlimpseMouseEvent argument will contain
     * the mouse event which moved the event. If the event was modified programatically, then the
     * GlimpseMouseEvent will be null.</p>
     *
     * @param event The updated Event
     * @param e The GlimpseMouseEvent which caused the Event update, or null if the Event was updated programatically
     */
    public void eventUpdated( GlimpseMouseEvent e, Event event );
}
