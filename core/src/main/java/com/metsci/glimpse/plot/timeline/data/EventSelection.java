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
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( event == null ) ? 0 : event.hashCode( ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        EventSelection other = ( EventSelection ) obj;
        if ( event == null )
        {
            if ( other.event != null ) return false;
        }
        else if ( !event.equals( other.event ) ) return false;
        return true;
    }

    @Override
    public String toString( )
    {
        return String.format( "%s %s", event, locations );
    }
}
