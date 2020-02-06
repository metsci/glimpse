/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.core.plot.timeline.event.listener;

import static com.metsci.glimpse.core.plot.timeline.data.EventSelection.Location.Center;
import static com.metsci.glimpse.core.plot.timeline.data.EventSelection.Location.End;
import static com.metsci.glimpse.core.plot.timeline.data.EventSelection.Location.Start;

import java.util.Set;

import com.metsci.glimpse.core.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.core.event.mouse.MouseButton;
import com.metsci.glimpse.core.plot.timeline.data.EventSelection;
import com.metsci.glimpse.core.plot.timeline.data.EventSelection.Location;
import com.metsci.glimpse.core.plot.timeline.event.Event;
import com.metsci.glimpse.core.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Helper class which supports user dragging of Events to adjust their start/end times.
 * @author ulman
 */
public class DragListener implements EventPlotListener, GlimpseMouseAllListener
{
    protected Location dragType = null;
    protected TimeStamp anchorTime = null;
    protected TimeStamp eventStart = null;
    protected TimeStamp eventEnd = null;
    protected Event dragEvent = null;

    protected boolean enabled = true;

    protected EventPlotInfo info;

    public DragListener( EventPlotInfo info )
    {
        this.info = info;
    }

    public boolean isEnabled( )
    {
        return this.enabled;
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
        this.reset( );
    }

    public void reset( )
    {
        this.dragType = null;
        this.anchorTime = null;
        this.eventStart = null;
        this.eventEnd = null;
        this.dragEvent = null;
    }

    @Override
    public void eventsClicked( GlimpseMouseEvent ev, Set<EventSelection> events, TimeStamp time )
    {
        if ( !this.enabled ) return;

        if ( this.dragEvent == null && ev.isButtonDown( MouseButton.Button1 ) )
        {
            for ( EventSelection selection : events )
            {
                if ( selection.isLocation( Center, Start, End ) && selection.getEvent( ).isEditable( ) )
                {
                    this.dragEvent = selection.getEvent( );
                    this.eventStart = dragEvent.getStartTime( );
                    this.eventEnd = dragEvent.getEndTime( );
                    this.anchorTime = time;
                    ev.setHandled( true );

                    if ( selection.isCenterSelection( ) )
                    {
                        this.dragType = Center;
                    }
                    else if ( selection.isStartTimeSelection( ) )
                    {
                        this.dragType = Start;
                    }
                    else if ( selection.isEndTimeSelection( ) )
                    {
                        this.dragType = End;
                    }

                    return;
                }
            }
        }
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent ev )
    {
        if ( !this.enabled ) return;

        if ( this.dragEvent != null )
        {
            this.doDrag( ev );
            ev.setHandled( true );
        }
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent ev )
    {
        if ( !enabled ) return;

        if ( this.dragEvent != null && ev.isButtonDown( MouseButton.Button1 ) )
        {
            // TODO: Find a less kludgy way to distinguish mouse-release from mouse-drag
            // If the mouse is outside the canvas when released, the incoming clickCount is zero; subtract 1 so that releaseClickCount is always negative
            int releaseClickCount = ( -1 * ev.getClickCount( ) ) - 1;
            GlimpseMouseEvent releaseEv = new GlimpseMouseEvent( ev.getTargetStack( ), ev.getModifiers( ), ev.getButtons( ), ev.getAllX( ), ev.getAllY( ), ev.getWheelIncrement( ), releaseClickCount, false );

            this.doDrag( releaseEv );
            this.reset( );
            ev.setHandled( true );
        }
    }

    protected void doDrag( GlimpseMouseEvent ev )
    {
        TimeStamp time = this.info.getTime( ev );

        if ( this.dragType == Center )
        {
            double diff = time.durationAfter( this.anchorTime );
            this.dragEvent.setTimes( ev, this.eventStart.add( diff ), this.eventEnd.add( diff ), false );
        }
        else if ( this.dragType == End && this.eventStart.isBefore( time ) )
        {
            this.dragEvent.setTimes( ev, this.eventStart, time, false );
        }
        else if ( this.dragType == Start && this.eventEnd.isAfter( time ) )
        {
            this.dragEvent.setTimes( ev, time, this.eventEnd, false );
        }
    }

    //@formatter:off
    @Override public void eventsHovered( GlimpseMouseEvent ev, Set<EventSelection> events, TimeStamp time ) { }
    @Override public void eventsExited( GlimpseMouseEvent ev, Set<EventSelection> events, TimeStamp time ) { }
    @Override public void eventsEntered( GlimpseMouseEvent ev, Set<EventSelection> events, TimeStamp time ) { }
    @Override public void eventUpdated( GlimpseMouseEvent ev, Event event ) { }
    @Override public void mouseEntered( GlimpseMouseEvent ev ) { }
    @Override public void mouseExited( GlimpseMouseEvent ev ) { }
    @Override public void mousePressed( GlimpseMouseEvent ev ) { }
    @Override public void mouseWheelMoved( GlimpseMouseEvent ev ) { }
    //@formatter:on
}
