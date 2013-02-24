package com.metsci.glimpse.plot.timeline.event;

import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Center;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.End;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Start;

import java.util.Set;

import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.data.EventSelection.Location;
import com.metsci.glimpse.util.units.time.TimeStamp;

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

        reset( );
    }

    public void reset( )
    {
        dragType = null;
        anchorTime = null;
        eventStart = null;
        eventEnd = null;
        dragEvent = null;
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent e )
    {
        if ( !enabled ) return;

        if ( e.isButtonDown( MouseButton.Button1 ) && dragEvent != null )
        {
            TimeStamp time = info.getTime( e );

            if ( dragType == Location.Center )
            {
                double diff = time.durationAfter( anchorTime );
                dragEvent.setTimes( eventStart.add( diff ), eventEnd.add( diff ) );
            }
            else if ( dragType == Location.End && eventStart.isBefore( time ) )
            {
                dragEvent.setTimes( eventStart, time );
            }
            else if ( dragType == Location.Start && eventEnd.isAfter( time ) )
            {
                dragEvent.setTimes( time, eventEnd );
            }

            e.setHandled( true );
        }
    }

    @Override
    public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
    {
        if ( !enabled ) return;

        if ( e.isButtonDown( MouseButton.Button1 ) )
        {
            for ( EventSelection selection : events )
            {
                if ( selection.isLocation( Center, Start, End ) && selection.getEvent( ).isEditable( ) )
                {
                    dragEvent = selection.getEvent( );
                    eventStart = dragEvent.getStartTime( );
                    eventEnd = dragEvent.getEndTime( );
                    anchorTime = time;
                    e.setHandled( true );

                    if ( selection.isCenterSelection( ) )
                    {
                        dragType = Center;
                    }
                    else if ( selection.isStartTimeSelection( ) )
                    {
                        dragType = Start;
                    }
                    else if ( selection.isEndTimeSelection( ) )
                    {
                        dragType = End;
                    }

                    return;
                }
            }
        }
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent event )
    {
        if ( !enabled ) return;

        reset( );
    }

    //@formatter:off
    @Override public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time ) { }
    @Override public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time ) { }
    @Override public void eventsEntered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time ) { }
    @Override public void eventUpdated( Event event ) { }
    @Override public void mouseEntered( GlimpseMouseEvent event ) { }
    @Override public void mouseExited( GlimpseMouseEvent event ) { }
    @Override public void mousePressed( GlimpseMouseEvent event ) { }
    @Override public void mouseWheelMoved( GlimpseMouseEvent e ) { }
    //@formatter:on
}
