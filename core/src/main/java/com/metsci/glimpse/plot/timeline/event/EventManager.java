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
package com.metsci.glimpse.plot.timeline.event;

import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Center;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.End;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Icon;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Label;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Start;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.data.EventSelection.Location;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Helper class which maintains sorted Event data structures for {@code EventPlotInfo}.
 *
 * @author ulman
 */
public class EventManager
{
    protected static final double BUFFER_MULTIPLIER = 2;
    protected static final double OVERLAP_HEURISTIC = 20.0;
    protected static final int PICK_BUFFER_PIXELS = 10;

    protected EventPlotInfo info;
    protected ReentrantLock lock;

    protected Map<Object, EventBounds> eventBoundsMap;
    protected Map<Object, Event> eventMap;
    protected Map<Object, Row> rowMap;
    protected List<Row> rows;

    protected boolean aggregateNearbyEvents = false;
    protected int maxAggregateSize = 30;
    protected int maxAggregateGap = 5;

    protected boolean shouldStack = true;
    protected boolean isHorizontal = true;

    protected boolean visibleEventsDirty = true;
    protected double prevMin;
    protected double prevMax;

    public class Row
    {
        public int index;

        // all Events in the Row
        public EventIntervalQuadTree events;

        // all visible Events in the Row (some Events may be aggregated)
        // will not be filled in if aggregation is not turned on (in that
        // case it is unneeded because the events map can be queried instead)
        public EventIntervalQuadTree visibleAggregateEvents;

        // all visible Events (including aggregated events, if turned on)
        // sorted by starting timestamp
        public List<Event> visibleEvents;

        public Row( int index )
        {
            this.index = index;
            this.visibleAggregateEvents = new EventIntervalQuadTree( );
            this.events = new EventIntervalQuadTree( );
        }

        public void addEvent( Event event )
        {
            this.events.add( event );
            EventManager.this.rowMap.put( event.getId( ), this );
        }

        public void removeEvent( Event event )
        {
            this.events.remove( event );
            EventManager.this.rowMap.remove( event.getId( ) );
        }

        public void calculateVisibleEvents( Axis1D axis, TimeStamp min, TimeStamp max )
        {
            if ( EventManager.this.aggregateNearbyEvents )
            {
                calculateVisibleEventsAggregated( axis, min, max );
            }
            else
            {
                calculateVisibleEventsNormal( min, max );
            }
        }

        public void calculateVisibleEventsAggregated( Axis1D axis, TimeStamp min, TimeStamp max )
        {
            // calculate size of bin in system (time) units
            double ppv = axis.getPixelsPerValue( );
            double maxDuration = EventManager.this.maxAggregateSize / ppv;
            double maxGap = EventManager.this.maxAggregateGap / ppv;

            // expand the visible window slightly
            // since we only aggregate visible Events, we don't want weird
            // visual artifacts (aggregate groups appearing and disappearing)
            // as Events scroll off the screen
            TimeStamp expandedMin = min.subtract( maxDuration * BUFFER_MULTIPLIER );
            TimeStamp expandedMax = max.add( maxDuration * BUFFER_MULTIPLIER );

            List<Event> visible = calculateVisibleEventsNormal0( events, expandedMin, expandedMax );

            EventIntervalQuadTree events = new EventIntervalQuadTree( );

            Set<Event> children = new HashSet<Event>( );
            TimeStamp childrenMin = null;
            TimeStamp childrenMax = null;
            for ( Event event : visible )
            {
                // only aggregate small events
                boolean isDurationSmall = event.getDuration( ) < maxDuration;

                // only aggregate events with small gaps between them
                double gap = childrenMax == null ? 0 : childrenMax.durationBefore( event.getStartTime( ) );
                boolean isGapSmall = gap < maxGap;

                // if the gap is large, end the current aggregate group
                if ( !isGapSmall )
                {
                    addAggregateEvent( events, children, childrenMin, childrenMax, min, max );
                    children.clear( );
                    childrenMin = null;
                    childrenMax = null;
                }

                // if the event is small enough to be aggregated, add it to the child list
                if ( isDurationSmall )
                {
                    children.add( event );

                    // events are in start time order, so this will never change after being set
                    if ( childrenMin == null ) childrenMin = event.getStartTime( );

                    if ( childrenMax == null || childrenMax.isBefore( event.getEndTime( ) ) ) childrenMax = event.getEndTime( );
                }
                // otherwise just add it to the result map
                else
                {
                    if ( isVisible( event, min, max ) ) events.add( event );
                }
            }

            // add any remaining child events
            addAggregateEvent( events, children, childrenMin, childrenMax, min, max );

            this.visibleAggregateEvents = events;
            this.visibleEvents = calculateVisibleEventsNormal0( events.getAll( ) );
        }

        protected void addAggregateEvent( EventIntervalQuadTree events, Set<Event> children, TimeStamp childrenMin, TimeStamp childrenMax, TimeStamp min, TimeStamp max )
        {
            // if there is only one or zero events in the current group, just add a regular event
            if ( children.size( ) <= 1 )
            {
                for ( Event child : children )
                    if ( isVisible( child, min, max ) ) events.add( child );
            }
            // otherwise create an aggregate group and add it to the result map
            else
            {
                AggregateEvent aggregate = new AggregateEvent( children, childrenMin, childrenMax );

                if ( isVisible( aggregate, min, max ) ) events.add( aggregate );
            }
        }

        protected boolean isVisible( Event event, TimeStamp min, TimeStamp max )
        {
            return ! ( event.getEndTime( ).isBefore( min ) || event.getStartTime( ).isAfter( max ) );
        }

        protected List<Event> calculateVisibleEventsNormal0( EventIntervalQuadTree events, TimeStamp min, TimeStamp max )
        {
            return calculateVisibleEventsNormal0( events.get( min, true, max, true ) );
        }

        protected List<Event> calculateVisibleEventsNormal0( Collection<Event> visible )
        {
            ArrayList<Event> visible_start_sorted = new ArrayList<Event>( visible.size( ) );
            visible_start_sorted.addAll( visible );
            Collections.sort( visible_start_sorted, Event.getStartTimeComparator( ) );
            return visible_start_sorted;
        }

        public void calculateVisibleEventsNormal( TimeStamp min, TimeStamp max )
        {
            this.visibleEvents = calculateVisibleEventsNormal0( this.events, min, max );
        }

        public Collection<Event> getOverlappingEvents( Event event )
        {
            return this.events.get( event.getStartTime( ), false, event.getEndTime( ), false );
        }

        public Collection<Event> getNearestVisibleEvents( TimeStamp timeStart, TimeStamp timeEnd )
        {
            if ( EventManager.this.aggregateNearbyEvents )
            {
                return this.visibleAggregateEvents.get( timeStart, timeEnd );
            }
            else
            {
                return this.events.get( timeStart, timeEnd );
            }
        }

        public boolean isEmpty( )
        {
            return this.events.isEmpty( );
        }

        public int size( )
        {
            return this.events.size( );
        }

        public int getIndex( )
        {
            return this.index;
        }

        public void setIndex( int index )
        {
            this.index = index;
        }
    }

    public EventManager( EventPlotInfo info )
    {
        this.info = info;

        this.lock = info.getStackedPlot( ).getLock( );

        this.rows = new ArrayList<>( );
        this.eventMap = new HashMap<>( );
        this.eventBoundsMap = new HashMap<>( );
        this.rowMap = new HashMap<>( );

        this.isHorizontal = info.getStackedTimePlot( ).isTimeAxisHorizontal( );
    }

    public void lock( )
    {
        this.lock.lock( );
    }

    public void unlock( )
    {
        this.lock.unlock( );
    }

    public List<Row> getRows( )
    {
        return Collections.unmodifiableList( rows );
    }

    /**
     * @see #setStackOverlappingEvents(boolean)
     */
    public boolean isStackOverlappingEvents( )
    {
        return this.shouldStack;
    }

    /**
     * If true, Events will be automatically placed into rows in order to
     * avoid overlap. Any row requested by {@link Event#setFixedRow(int)} will
     * be ignored.
     */
    public void setStackOverlappingEvents( boolean stack )
    {
        this.shouldStack = stack;
        this.validate( );
    }

    /**
     * @see #setMaxAggregatedGroupSize(int)
     */
    public int getMaxAggregatedGroupSize( )
    {
        return this.maxAggregateSize;
    }

    /**
     * Sets the maximum pixel size above which an Event will not be aggregated
     * with nearby Events (in order to reduce visual clutter).
     *
     * @see #setAggregateNearbyEvents(boolean)
     */
    public void setMaxAggregatedGroupSize( int size )
    {
        this.maxAggregateSize = size;
        this.validate( );
    }

    public int getMaxAggregatedEventGapSize( )
    {
        return this.maxAggregateGap;
    }

    /**
     * Sets the maximum pixel distance between adjacent events above which
     * events will not be aggregated into a single Event (in order to reduce
     * visual clutter).
     *
     * @param size
     */
    public void setMaxAggregatedEventGapSize( int size )
    {
        this.maxAggregateGap = size;
        this.validate( );
    }

    /**
     * @see #setAggregateNearbyEvents(boolean)
     */
    public boolean isAggregateNearbyEvents( )
    {
        return this.aggregateNearbyEvents;
    }

    /**
     * If true, nearby events in the same row will be combined into one
     * event to reduce visual clutter.
     */
    public void setAggregateNearbyEvents( boolean aggregate )
    {
        this.aggregateNearbyEvents = aggregate;
        this.validate( );
    }

    public void validate( )
    {
        lock.lock( );
        try
        {
            this.rebuildRows0( );
            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public int getRowCount( )
    {
        lock.lock( );
        try
        {
            return Math.max( 1, this.rows.size( ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setRow( Object eventId, int rowIndex )
    {
        lock.lock( );
        try
        {
            Event event = getEvent( eventId );
            if ( event == null ) return;

            int oldRowIndex = getRow( eventId );
            Row oldRow = rows.get( oldRowIndex );
            if ( oldRow != null ) oldRow.removeEvent( event );

            ensureRows0( rowIndex );
            Row newRow = rows.get( rowIndex );
            newRow.addEvent( event );

            // row was set manually so don't automatically
            // adjust the other rows to avoid overlap

            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public int getRow( Object eventId )
    {
        lock.lock( );
        try
        {
            Row row = rowMap.get( eventId );
            if ( row != null )
            {
                return row.getIndex( );
            }
            else
            {
                return 0;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addEvent( Event event )
    {
        if ( event == null ) return;

        lock.lock( );
        try
        {
            // remove the event if it already exists
            this.removeEvent( event.getId( ) );

            this.eventMap.put( event.getId( ), event );
            this.addEvent0( event );
            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Event removeEvent( Object id )
    {
        lock.lock( );
        try
        {
            Event event = this.eventMap.remove( id );
            this.eventBoundsMap.remove( id );

            if ( event != null )
            {
                this.removeEvent0( event );
                this.visibleEventsDirty = true;
                this.info.updateSize( );
            }

            return event;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeAllEvents( )
    {
        lock.lock( );
        try
        {
            for ( Event event : this.eventMap.values( ) )
            {
                event.setEventPlotInfo( null );
            }

            this.eventMap.clear( );
            this.eventBoundsMap.clear( );
            this.rowMap.clear( );
            this.rows.clear( );

            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void moveEvent( Event event, TimeStamp newStartTime, TimeStamp newEndTime )
    {
        lock.lock( );
        try
        {

            Event eventOld = Event.createDummyEvent( event );

            Row oldRow = rowMap.get( event.getId( ) );
            if ( oldRow == null ) return;

            if ( event.isFixedRow( ) )
            {
                // update the event times (its row will stay the same)
                // remove and add it to update start/end time indexes
                oldRow.removeEvent( event );
                event.setTimes0( newStartTime, newEndTime );
                oldRow.addEvent( event );

                // displace the events this event has shifted on to
                displaceEvents0( oldRow, event );
            }
            else
            {
                // remove the event from its old row
                oldRow.removeEvent( event );

                // update the event times
                event.setTimes0( newStartTime, newEndTime );

                // add the moved version of the event back in
                // (which might land it on a different row if it
                //  has been moved over top of another event)
                addEvent0( event );
            }

            // now shift events to fill the space left by moving the event
            shiftEvents0( eventOld, oldRow );
            clearEmptyRows0( );

            this.visibleEventsDirty = true;
            this.info.updateSize( );

        }
        finally
        {
            lock.unlock( );
        }
    }

    public Set<Event> getEvents( )
    {
        lock.lock( );
        try
        {
            return Collections.unmodifiableSet( new HashSet<Event>( this.eventMap.values( ) ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Event getEvent( Object id )
    {
        lock.lock( );
        try
        {
            return this.eventMap.get( id );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public EventBounds getOrCreateEventBounds( Object id )
    {
        lock.lock( );
        try
        {
            EventBounds bounds = this.eventBoundsMap.get( id );

            if ( bounds == null )
            {
                bounds = new EventBounds( );
                this.eventBoundsMap.put( id, bounds );
            }

            return bounds;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public EventBounds getEventBounds( Object id )
    {
        lock.lock( );
        try
        {
            return this.eventBoundsMap.get( id );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public EventBounds setEventBounds( Object id, EventBounds bounds )
    {
        lock.lock( );
        try
        {
            return this.eventBoundsMap.put( id, bounds );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Set<EventSelection> getNearestEvents( GlimpseMouseEvent e )
    {
        lock.lock( );
        try
        {
            Row row = getNearestRow( e );

            if ( row != null )
            {
                Axis1D axis = e.getAxis1D( );
                double value = isHorizontal ? e.getAxisCoordinatesX( ) : e.getAxisCoordinatesY( );
                double buffer = PICK_BUFFER_PIXELS / axis.getPixelsPerValue( );

                Epoch epoch = info.getStackedTimePlot( ).getEpoch( );

                TimeStamp time = epoch.toTimeStamp( value );
                TimeStamp timeStart = epoch.toTimeStamp( value - buffer );
                TimeStamp timeEnd = epoch.toTimeStamp( value + buffer );

                Collection<Event> events = row.getNearestVisibleEvents( timeStart, timeEnd );
                Set<EventSelection> eventSelections = createEventSelection( axis, events, time );
                return eventSelections;
            }

            return Collections.emptySet( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    // find the event which minimizes: abs(clickPos-eventEnd)+abs(clickPos-eventStart)
    // this is a heuristic for the single event "closest" to the click position
    // we don't want to require that the click be inside the event because we want
    // to make selection of instantaneous events possible
    // (but if the click *is* inside an event, it gets priority)
    public EventSelection getNearestEvent( Set<EventSelection> events, GlimpseMouseEvent e )
    {
        lock.lock( );
        try
        {
            Epoch epoch = info.getStackedTimePlot( ).getEpoch( );
            double value = isHorizontal ? e.getAxisCoordinatesX( ) : e.getAxisCoordinatesY( );
            TimeStamp time = epoch.toTimeStamp( value );

            double bestDist = Double.MAX_VALUE;
            EventSelection bestEvent = null;

            for ( EventSelection s : events )
            {
                Event event = s.getEvent( );

                if ( event.contains( time ) )
                {
                    return s;
                }
                else
                {
                    double dist = distance0( event, time );
                    if ( bestEvent == null || dist < bestDist )
                    {
                        bestDist = dist;
                        bestEvent = s;
                    }
                }
            }

            return bestEvent;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public EventSelection getNearestEvent( GlimpseMouseEvent e )
    {
        return getNearestEvent( getNearestEvents( e ), e );
    }

    // heuristic distance measure for use in getNearestEvent( )
    protected double distance0( Event event, TimeStamp time )
    {
        double startDiff = Math.abs( time.durationAfter( event.getStartTime( ) ) );
        double endDiff = Math.abs( time.durationAfter( event.getEndTime( ) ) );
        return Math.min( startDiff, endDiff );
    }

    // must be called while holding lock
    protected Row getNearestRow( GlimpseMouseEvent e )
    {
        GlimpseBounds bounds = e.getTargetStack( ).getBounds( );

        int value = isHorizontal ? e.getY( ) : e.getTargetStack( ).getBounds( ).getWidth( ) - e.getX( );

        int rowIndex = ( int ) Math.floor( value / ( double ) ( info.getRowSize( bounds ) + info.getEventPadding( ) ) );

        // flip rowIndex (due to GlimpseMouseEvent coordinate system)
        rowIndex = info.getRowCount( ) - 1 - rowIndex;

        if ( rowIndex >= 0 && rowIndex < rows.size( ) )
        {
            return rows.get( rowIndex );
        }

        return null;
    }

    public void calculateVisibleEvents( Axis1D axis )
    {
        lock.lock( );
        try
        {
            if ( visibleEventsDirty || axis.getMin( ) != prevMin || axis.getMax( ) != prevMax )
            {
                calculateVisibleEvents( axis.getMin( ), axis.getMax( ) );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    // must be called while holding lock
    private Set<EventSelection> createEventSelection( Axis1D axis, Collection<Event> events, TimeStamp clickTime )
    {
        Set<EventSelection> set = new HashSet<EventSelection>( );

        for ( Event event : events )
        {
            set.add( createEventSelection( axis, event, clickTime ) );
        }

        return set;
    }

    // must be called while holding lock
    private EventSelection createEventSelection( Axis1D axis, Event event, TimeStamp t )
    {
        double buffer = PICK_BUFFER_PIXELS / axis.getPixelsPerValue( );

        TimeStamp t1 = t.subtract( buffer );
        TimeStamp t2 = t.add( buffer );

        TimeStamp e1 = event.getStartTime( );
        TimeStamp e2 = event.getEndTime( );

        EnumSet<Location> locations = EnumSet.noneOf( Location.class );

        EventBounds bounds = getEventBounds( event.getId( ) );
        if ( bounds != null )
        {
            if ( bounds.containsText( t ) ) locations.add( Label );
            if ( bounds.containsIcon( t ) ) locations.add( Icon );
        }

        boolean start = t2.isAfterOrEquals( e1 ) && t1.isBeforeOrEquals( e1 );
        boolean end = t2.isAfterOrEquals( e2 ) && t1.isBeforeOrEquals( e2 );

        if ( start ) locations.add( Start );
        if ( end ) locations.add( End );
        if ( ( !start && !end ) || ( start && end ) ) locations.add( Center );

        return new EventSelection( event, locations );
    }

    // must be called while holding lock
    private void rebuildRows0( )
    {
        rows.clear( );
        rowMap.clear( );

        for ( Event event : eventMap.values( ) )
        {
            addEvent0( event );
        }
    }

    // must be called while holding lock
    private void ensureRows0( int requestedIndex )
    {
        int currentRowCount = rows.size( );
        while ( requestedIndex >= currentRowCount )
        {
            rows.add( new Row( currentRowCount++ ) );
        }
    }

    // must be called while holding lock
    private void displaceEvents0( Row row, Event event )
    {
        Set<Event> overlapEvents = new HashSet<Event>( row.getOverlappingEvents( event ) );
        for ( Event overlapEvent : overlapEvents )
        {
            displaceEvent0( overlapEvent );
        }
    }

    // must be called while holding lock
    // move an event which has been overlapped by another event
    private void displaceEvent0( Event oldEvent )
    {
        if ( !oldEvent.isFixedRow( ) )
        {
            Row oldRow = rowMap.get( oldEvent.getId( ) );
            oldRow.removeEvent( oldEvent );
            addEvent0( oldEvent );
            shiftEvents0( oldEvent, oldRow );
        }
        else
        {
            // if the displaced event requested the row it is in, don't move it
        }
    }

    // must be called while holding lock
    private void clearEmptyRows0( )
    {
        // clear empty rows until we find a non-empty one
        for ( int i = rows.size( ) - 1; i >= 0; i-- )
        {
            if ( rows.get( i ).isEmpty( ) )
            {
                rows.remove( i );
            }
            else
            {
                break;
            }
        }
    }

    // must be called while holding lock
    private void removeEvent0( Event event )
    {
        // remove the event then determine if other events should be
        // shifted down to fill its place
        eventMap.remove( event.getId( ) );

        Row row = rowMap.remove( event.getId( ) );
        if ( row == null ) return;
        row.removeEvent( event );

        shiftEvents0( event, row );
        clearEmptyRows0( );
    }

    // must be called while holding lock
    private void shiftEvents0( Event event, Row toRow )
    {
        // determine if the removal of this event allows others to shift down
        int size = rows.size( );
        for ( int i = size - 1; i > toRow.index; i-- )
        {
            Row fromRow = rows.get( i );

            // check to see if any of these candidates can be moved down to
            // fill the spot in toRow left by the deleted event
            HashSet<Event> events = new HashSet<Event>( fromRow.getOverlappingEvents( event ) );
            for ( Event e : events )
                moveEventIfRoom0( e, fromRow, toRow );
        }
    }

    // must be called while holding lock
    private void moveEventIfRoom0( Event event, Row fromRow, Row toRow )
    {
        // move the event if there is room for it and it hasn't explicitly requested its current row
        if ( !event.isFixedRow( ) && toRow.getOverlappingEvents( event ).isEmpty( ) )
        {
            fromRow.removeEvent( event );
            toRow.addEvent( event );
            shiftEvents0( event, fromRow );
        }
    }

    // must be called while holding lock
    private Row addEvent0( Event event )
    {
        Row row = null;
        if ( shouldStack && !event.isFixedRow( ) )
        {
            row = getRowWithLeastOverlaps( event );

            // put the event into the non-overlapping spot we've found for it
            row.addEvent( event );
        }
        else
        {
            // the requested row index must be less than the maximum row count and greater than or equal to 0
            int requestedRow = Math.min( Math.max( 0, event.getFixedRow( ) ), info.getRowMaxCount( ) - 1 );
            ensureRows0( requestedRow );
            row = rows.get( requestedRow );

            row.addEvent( event );

            // this spot might overlap with other events, move them out of the way
            if ( shouldStack )
            {
                displaceEvents0( row, event );
            }
        }

        return row;
    }

    // must be called while holding lock
    //
    // If plot.getMaxRowCount() is large, we'll always be able to simply
    // make a new row (which will have no overlaps. If we're constrained
    // regarding the number of rows we can create, we may have to accept
    // some overlaps.
    private Row getRowWithLeastOverlaps( Event event )
    {
        int size = rows.size( );
        int max = info.getRowMaxCount( );

        double leastTime = Double.POSITIVE_INFINITY;
        Row leastRow = null;

        for ( int i = 0; i < size; i++ )
        {
            Row candidate = rows.get( i );

            double overlapTime = getTotalOverlapTime( candidate, event );

            if ( overlapTime < leastTime )
            {
                leastTime = overlapTime;
                leastRow = candidate;
            }
        }

        // if we didn't find an empty row, and there's room to make
        // a new row, then make a new row, which will have 0 overlap
        if ( leastTime != 0.0 && size < max )
        {
            leastTime = 0;
            leastRow = new Row( size );
            rows.add( leastRow );
        }

        return leastRow;
    }

    // must be called while holding lock
    private double getTotalOverlapTime( Row candidate, Event event )
    {
        double totalOverlap = 0;

        //XXX Heuristic: we want overlaps with very small events (in the
        // limit we have 0 duration events) to count for something, so
        // we make the minimum time penalty for any overlap be 1/20th
        // of the total duration of either event
        double minOverlap1 = event.getDuration( ) / OVERLAP_HEURISTIC;

        Collection<Event> events = candidate.getOverlappingEvents( event );
        for ( Event overlapEvent : events )
        {
            double minOverlap = Math.max( minOverlap1, overlapEvent.getDuration( ) / OVERLAP_HEURISTIC );
            double overlap = event.getOverlapTime( overlapEvent );

            totalOverlap += Math.max( minOverlap, overlap );
        }

        return totalOverlap;
    }

    // must be called while holding lock
    private void calculateVisibleEvents( double min, double max )
    {
        Epoch epoch = info.getStackedTimePlot( ).getEpoch( );
        TaggedAxis1D axis = info.getStackedTimePlot( ).getTimeAxis( );

        for ( Row row : rows )
        {
            row.calculateVisibleEvents( axis, epoch.toTimeStamp( min ), epoch.toTimeStamp( max ) );
        }

        this.visibleEventsDirty = false;
    }
}
