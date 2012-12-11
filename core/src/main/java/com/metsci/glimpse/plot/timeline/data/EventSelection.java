package com.metsci.glimpse.plot.timeline.data;

import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Center;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.End;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Icon;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Start;

import java.util.EnumSet;

import com.metsci.glimpse.plot.timeline.event.Event;

public class EventSelection
{
    /**
     * The location of the mouse inside the event box when the event occurred.
     * @author ulman
     */
    public enum Location
    {
        /**
         * The click occurred near the start time of the event.
         */
        Start,
        /**
         * The click occurred near the end time of the event.
         */
        End,
        /**
         * Either the click occurred in-between the start and end time (but near neither),
         * or the click occurred very near to both the start and end times (either because
         * the timeline was zoomed in very far, or the event had the same start and end time).
         */
        Center,
        /**
         * The click occurred near the event icon.
         */
        Icon,
        /**
         * The click occurred near the event label.
         */
        Label;
    }
    
    protected Event event;
    protected EnumSet<Location> locations;
    
    public EventSelection( Event event, EnumSet<Location> locations )
    {
        this.event = event;
        this.locations = locations;
    }
    
    public Event getEvent( )
    {
        return event;
    }
    
    public EnumSet<Location> getLocations( )
    {
        return locations;
    }
    
    public boolean isIconSelection( )
    {
        return locations.contains( Icon );
    }
    
    public boolean isTextSelection( )
    {
        return locations.contains( Icon );
    }
    
    public boolean isStartTimeSelection( )
    {
        return locations.contains( Start );
    }
    
    public boolean isEndTimeSelection( )
    {
        return locations.contains( End );
    }
    
    public boolean isCenterSelection( )
    {
        return locations.contains( Center );
    }
    
    public boolean isLocation( Location... locationList )
    {
        for ( Location location : locationList )
        {
            if ( locations.contains( location ) ) return true;
        }
        
        return false;
    }
    
    @Override
    public String toString( )
    {
        return String.format( "%s %s", event, locations );
    }
}
