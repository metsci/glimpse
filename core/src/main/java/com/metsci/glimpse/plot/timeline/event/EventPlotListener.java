package com.metsci.glimpse.plot.timeline.event;

import java.util.Set;

import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.util.units.time.TimeStamp;

public interface EventPlotListener
{
    /**
     * <p>Indicates that the set of selected events has changed. Events are selected either programmatically
     * or by the user clicking on the event. A click with no modifier keys selects the event, a click with
     * the CTRL key down adds (or subtracts, if the event is already selected) from the current selection.</p>
     * 
     * <p>The selectedEvents Set contains only newly selected Events (for which an eventsSelected event has not
     * yet been fired). It does not necessarily contain the full set of selected events. This can be obtained via
     * {@link EventPlotInfo#getSelectedEvents()}.</p>
     *  
     * @param selectedEvents the newly selected events
     */
    public void eventsSelected( Set<Event> selectedEvents, Set<Event> deselectedEvents );
    
    public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    public void eventsEntered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    public void eventUpdated( Event event );
}
