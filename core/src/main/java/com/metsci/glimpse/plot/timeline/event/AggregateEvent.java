package com.metsci.glimpse.plot.timeline.event;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class AggregateEvent extends Event
{
    // events can be nested (when many events are very close together
    // on the screen they can be combined into a single event for readability)
    protected Set<Event> children;

    // constructor used to create aggregate events
    protected AggregateEvent( Set<Event> children, TimeStamp startTime, TimeStamp endTime )
    {
        super( UUID.randomUUID( ), "", startTime, endTime );

        this.children = ImmutableSet.copyOf( children );

        this.isEditable = false;
    }

    /**
     * EventPlotInfo can automatically create synthetic groups of Events when the timeline
     * is zoomed out far enough that a bunch of Events are crowded into the same space.
     * The individual constituent Events can be accessed via this method.
     * User created Events never have children.
     */
    @Override
    public Set<Event> getChildren( )
    {
        return this.children;
    }

    /**
     * @see #getChildren()
     */
    @Override
    public boolean hasChildren( )
    {
        return this.children != null && !this.children.isEmpty( );
    }

    @Override
    public void setEditable( boolean isEditable )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setEndTimeMoveable( boolean isEndTimeMoveable )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setStartTimeMoveable( boolean isStartTimeMoveable )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setResizeable( boolean isResizeable )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setMaxTimeSpan( double maxTimeSpan )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setMinTimeSpan( double minTimeSpan )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setTimes( TimeStamp startTime, TimeStamp endTime, boolean force )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setFloatingRow( )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setFixedRow( int rowIndex )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setTimes( TimeStamp startTime, TimeStamp endTime )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }
    
    @Override
    public void setStartTime( TimeStamp startTime )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }

    @Override
    public void setEndTime( TimeStamp endTime )
    {
        throw new UnsupportedOperationException( "Aggregate events cannot be edited." );
    }
}