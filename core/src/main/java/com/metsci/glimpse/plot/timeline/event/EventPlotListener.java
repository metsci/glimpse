package com.metsci.glimpse.plot.timeline.event;

import java.util.Set;

import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.util.units.time.TimeStamp;

public interface EventPlotListener
{
    public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    public void eventsEntered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

    public void eventUpdated( Event event );
}
