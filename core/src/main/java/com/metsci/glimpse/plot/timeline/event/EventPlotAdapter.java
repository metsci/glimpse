package com.metsci.glimpse.plot.timeline.event;

import java.util.Set;

import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class EventPlotAdapter implements EventPlotListener
{
    @Override
    public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
    {
    }

    @Override
    public void eventsEntered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
    {
    }

    @Override
    public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
    {
    }

    @Override
    public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
    {
    }

    @Override
    public void eventUpdated( Event event )
    {
    }
}
