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
import java.util.SortedMap;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.SetMultimap;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.data.EventSelection.Location;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class EventManager
{
    protected static final double OVERLAP_HEURISTIC = 20.0;
    protected static final int PICK_BUFFER_PIXELS = 10;

    protected ReentrantLock lock;

    protected EventPlotInfo info;

    protected Map<Object, Event> eventMap;
    protected Map<Object, Row> rowMap;
    protected List<Row> rows;

    protected boolean shouldStack = true;
    protected boolean isHorizontal = true;

    protected boolean visibleEventsDirty = true;
    protected double prevMin;
    protected double prevMax;

    protected class Row
    {
        int index;
        IntervalSortedMultimap events;
        // sorted by event start time
        SortedMap<TimeStamp, Collection<Event>> visibleEvents;

        public Row( int index )
        {
            this.index = index;
            this.events = new IntervalSortedMultimap( );
        }

        public void addEvent( Event event )
        {
            this.events.addEvent( event );
            rowMap.put( event.getId( ), this );
        }

        public void removeEvent( Event event )
        {
            this.events.removeEvent( event );
            rowMap.remove( event.getId( ) );
        }

        public void calculateVisibleEvents( TimeStamp min, TimeStamp max )
        {
            SetMultimap<TimeStamp, Event> visibleMap = this.events.getMap( min, true, max, true );
            this.visibleEvents = ( SortedMap<TimeStamp, Collection<Event>> ) visibleMap.asMap( );
        }

        public Set<Event> getOverlappingEvents( Event event )
        {
            return this.events.get( event.getStartTime( ), false, event.getEndTime( ), false );
        }

        public IntervalSortedMultimap getMap( )
        {
            return this.events;
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

        this.lock = new ReentrantLock( );

        this.rows = new ArrayList<Row>( );
        this.eventMap = new HashMap<Object, Event>( );
        this.rowMap = new HashMap<Object, Row>( );

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

    public Set<EventSelection> getNearestEvents( GlimpseMouseEvent e )
    {
        lock.lock( );
        try
        {
            if ( isHorizontal )
            {
                Row row = getNearestRow( e );

                if ( row != null )
                {
                    Axis1D axis = e.getAxis1D( );
                    double valueX = axis.screenPixelToValue( e.getX( ) );
                    double bufferX = PICK_BUFFER_PIXELS / axis.getPixelsPerValue( );

                    Epoch epoch = info.getStackedTimePlot( ).getEpoch( );

                    TimeStamp time = epoch.toTimeStamp( valueX );
                    TimeStamp timeStart = epoch.toTimeStamp( valueX - bufferX );
                    TimeStamp timeEnd = epoch.toTimeStamp( valueX + bufferX );

                    Set<Event> events = row.getMap( ).get( timeStart, timeEnd );
                    Set<EventSelection> eventSelections = createEventSelection( axis, events, time );
                    return eventSelections;
                }
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
            Axis1D axis = e.getAxis1D( );
            double valueX = axis.screenPixelToValue( e.getX( ) );
            TimeStamp time = epoch.toTimeStamp( valueX );

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
        if ( isHorizontal )
        {
            int valueY = e.getY( );

            int rowIndex = ( int ) Math.floor( valueY / ( double ) ( info.getRowSize( ) + info.getEventPadding( ) ) );
            rowIndex = info.getRowCount( ) - 1 - rowIndex;

            if ( rowIndex >= 0 && rowIndex < rows.size( ) )
            {
                return rows.get( rowIndex );
            }
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
    private Set<EventSelection> createEventSelection( Axis1D axis, Set<Event> events, TimeStamp clickTime )
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

        boolean start = t2.isAfterOrEquals( e1 ) && t1.isBeforeOrEquals( e1 );
        boolean end = t2.isAfterOrEquals( e2 ) && t1.isBeforeOrEquals( e2 );

        TimeStamp i1 = event.getIconStartTime( );
        TimeStamp i2 = event.getIconEndTime( );
        boolean icon = event.isIconVisible( ) && i1 != null && i2 != null && t.isAfterOrEquals( i1 ) && t.isBeforeOrEquals( i2 );

        TimeStamp l1 = event.getLabelStartTime( );
        TimeStamp l2 = event.getLabelEndTime( );
        boolean text = event.isLabelVisible( ) && l1 != null && l2 != null && t.isAfterOrEquals( l1 ) && t.isBeforeOrEquals( l2 );

        if ( text ) locations.add( Label );
        if ( icon ) locations.add( Icon );
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
    //
    // "least" overlap is defined by the total amount of overlap time
    // TODO: this doesn't work well with zero duration events...
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

        Set<Event> events = candidate.getOverlappingEvents( event );
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

        for ( Row row : rows )
        {
            row.calculateVisibleEvents( epoch.toTimeStamp( min ), epoch.toTimeStamp( max ) );
        }

        this.visibleEventsDirty = false;
    }
}
