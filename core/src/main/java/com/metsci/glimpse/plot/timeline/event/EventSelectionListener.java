package com.metsci.glimpse.plot.timeline.event;

import java.util.Set;

public interface EventSelectionListener
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
    
}
