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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.Sets;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.plot.timeline.event.Event;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;

/**
 * Helper class which manages keeping track of selected Events for {@code EventPlotInfo}.
 *
 * @author ulman
 */
public class EventSelectionHandler
{
    protected Collection<EventSelectionListener> eventListeners;
    protected Set<Event> selectedEvents;

    protected volatile float[] selectedBorderColor;
    protected volatile float[] selectedBackgroundColor;
    protected volatile float selectedBorderThickness = 1.8f;
    protected volatile boolean highlightSelectedEvents = false;
    protected volatile boolean clearSelectionOnClick = false;
    protected volatile boolean allowMouseEventSelection = true;
    protected volatile boolean allowMultipleEventSelection = true;

    public EventSelectionHandler( )
    {
        this.eventListeners = new CopyOnWriteArrayList<EventSelectionListener>( );
        this.selectedEvents = new CopyOnWriteArraySet<Event>( );
    }

    public void addEventSelectionListener( EventSelectionListener listener )
    {
        this.eventListeners.add( listener );
    }

    public void removeEventSelectionListener( EventSelectionListener listener )
    {
        this.eventListeners.remove( listener );
    }

    /**
     * If true, multiple events may be selected by holding down the ctrl key while
     * clicking them. Does not restrict programmatic selection in any way.
     */
    public void setAllowMultipleEventSelection( boolean allowMultiple )
    {
        this.allowMultipleEventSelection = allowMultiple;
    }

    public boolean isAllowMultipleEventSelection( )
    {
        return this.allowMultipleEventSelection;
    }

    /**
     * If true, the set of selected events is cleared when the user clicks
     * on an area of the timeline which contains no events. Otherwise, such
     * a click has no effect on the selected events. In this case, the only
     * way to deselect all events is to ctrl-click on all the selected events.
     *
     * @param clear
     */
    public void setClearSelectionOnClick( boolean clear )
    {
        this.clearSelectionOnClick = clear;
    }

    /**
     * @see EventPlotInfo#setClearSelectionOnClick(boolean)
     */
    public boolean isClearSelectionOnClick( )
    {
        return this.clearSelectionOnClick;
    }

    /**
     * <p>If true, Events are automatically selected when users click inside or near
     * them with the mouse (unless they are set as unselectable via
     * {@link Event#setSelectable(boolean)}. Clicking one event removes all other
     * selected events unless the ctrl key is held down, in which case the clicked
     * event is added or removed from the set of selected Events.</p>
     *
     * <p>If custom selection semmantics are required (only selecting events
     * when their icon is clicked, for example), then
     * {@link #setAllowMouseEventSelection(boolean)} can be set to false and client
     * code can attach a custom {@link EventPlotListener} or {@link GlimpseMouseListener}
     * which calls {@link #setSelectedEvents(Set)} as desired based on user clicks.</p>
     *
     * @param allowSelection
     */
    public void setAllowMouseEventSelection( boolean allowSelection )
    {
        this.allowMouseEventSelection = allowSelection;
    }

    public boolean isAllowMouseEventSelection( )
    {
        return this.allowMouseEventSelection;
    }

    public Set<Event> getSelectedEvents( )
    {
        return Collections.unmodifiableSet( Sets.newHashSet( selectedEvents ) );
    }

    public void setSelectedEvents( Set<Event> newEvents )
    {
        Set<Event> oldEvents = Sets.newHashSet( selectedEvents );

        if ( newEvents.equals( oldEvents ) ) return;

        // set of deselected events
        Set<Event> deselectedEvents = Sets.difference( oldEvents, newEvents ).immutableCopy( );
        // set of newly selected events
        Set<Event> newlySelectedEvents = Sets.difference( newEvents, oldEvents ).immutableCopy( );

        //XXX these two operations should really happen atomically
        // (we want the end result to be that selectedEvents contains everything in newEvents)
        selectedEvents.retainAll( newEvents );
        selectedEvents.addAll( newEvents );

        notifyEventsSelected( newlySelectedEvents, deselectedEvents );
    }

    public void clearSelectedEvents( )
    {
        Set<Event> tempDeselectedEvents = Collections.unmodifiableSet( Sets.newHashSet( selectedEvents ) );

        selectedEvents.clear( );

        notifyEventsSelected( Collections.<Event> emptySet( ), tempDeselectedEvents );
    }

    public void addSelectedEvent( Event event )
    {
        selectedEvents.add( event );

        notifyEventsSelected( Collections.singleton( event ), Collections.<Event> emptySet( ) );
    }

    public void removeSelectedEvent( Event event )
    {
        selectedEvents.remove( event );

        notifyEventsSelected( Collections.<Event> emptySet( ), Collections.singleton( event ) );
    }

    public boolean isEventSelected( Event event )
    {
        return selectedEvents.contains( event );
    }

    public float[] getSelectedEventBorderColor( )
    {
        return selectedBorderColor;
    }

    public float[] getSelectedEventBackgroundColor( )
    {
        return selectedBackgroundColor;
    }

    public float getSelectedEventBorderThickness( )
    {
        return selectedBorderThickness;
    }

    public boolean isHighlightSelectedEvents( )
    {
        return highlightSelectedEvents;
    }

    public void setSelectedEventBorderColor( float[] color )
    {
        selectedBorderColor = color;
        highlightSelectedEvents = true;
    }

    public void setSelectedEventBackgroundColor( float[] color )
    {
        selectedBackgroundColor = color;
        highlightSelectedEvents = true;
    }

    public void setSelectedEventBorderThickness( float thickness )
    {
        selectedBorderThickness = thickness;
        highlightSelectedEvents = true;
    }

    public void setHighlightSelectedEvents( boolean highlight )
    {
        highlightSelectedEvents = highlight;
    }

    protected void notifyEventsSelected( Set<Event> selectedEvents, Set<Event> deselectedEvents )
    {
        for ( EventSelectionListener listener : eventListeners )
        {
            listener.eventsSelected( selectedEvents, deselectedEvents );
        }
    }
}
