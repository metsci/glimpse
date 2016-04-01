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