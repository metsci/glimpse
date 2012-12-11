package com.metsci.glimpse.plot.timeline.data;

import com.metsci.glimpse.plot.timeline.event.Event;

public interface EventConstraint
{
    /**
     * Describes a set of constraints on the start and end times of an event.
     * 
     * @param event the event being modified
     * @param proposedTimeSpan the proposed new start and end times for the event
     * @return the constrained start and end times for the event
     */
    public TimeSpan applyConstraint( Event event, TimeSpan proposedTimeSpan );
}
