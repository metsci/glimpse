/*
 * Copyright (c) 2012, Metron, Inc.
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

import java.awt.Font;
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

import javax.media.opengl.GL;

import com.google.common.collect.SetMultimap;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.painter.base.GlimpseDataPainter1D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.data.EventSelection.Location;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.sun.opengl.util.j2d.TextRenderer;

/**
 * Paints 1D events with associated color, time span, icon, and label information.
 * 
 * @author ulman
 */
public class EventPainter extends GlimpseDataPainter1D
{
    protected static final int PICK_BUFFER_PIXELS = 10;

    protected Map<Object, Event> eventMap;
    protected Map<Object, Row> rowMap;
    protected List<Row> rows;

    protected EventPlotInfo plot;
    protected TextureAtlas atlas;

    protected boolean shouldStack = true;
    protected boolean isHorizontal = true;

    protected boolean visibleEventsDirty = true;
    protected double prevMin;
    protected double prevMax;

    protected TextRenderer textRenderer;
    protected boolean fontSet = false;

    protected volatile Font newFont = null;
    protected volatile boolean antialias = false;

    protected float[] backgroundColor = GlimpseColor.getGray( 0.2f );
    protected float[] borderColor = GlimpseColor.getWhite( 1f );
    protected float[] textColor = GlimpseColor.getBlack( );
    protected float[] textColorNoBackground = GlimpseColor.getBlack( );

    protected boolean textColorSet = false;
    protected boolean backgroundColorSet = false;
    protected boolean borderColorSet = false;

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

    public EventPainter( EventPlotInfo plot, Epoch epoch, TextureAtlas atlas, boolean isHorizontal )
    {
        this.plot = plot;
        this.atlas = atlas;

        this.rows = new ArrayList<Row>( );
        this.eventMap = new HashMap<Object, Event>( );
        this.rowMap = new HashMap<Object, Row>( );

        this.isHorizontal = isHorizontal;

        this.newFont = FontUtils.getDefaultPlain( 12 );
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

        this.rebuildRows0( );

        this.visibleEventsDirty = true;
        this.plot.updateSize( );
    }

    public int getRowSize( )
    {
        return this.plot.getRowSize( );
    }

    public int getRowBufferSize( )
    {
        return this.plot.getRowBufferSize( );
    }

    public int getRowCount( )
    {
        return Math.max( 1, this.rows.size( ) );
    }

    public float[] getBackgroundColor( )
    {
        return backgroundColor;
    }

    public void setBackgroundColor( float[] backgroundColor )
    {
        this.backgroundColor = backgroundColor;
        this.backgroundColorSet = true;
    }

    public float[] getBorderColor( )
    {
        return borderColor;
    }

    public void setBorderColor( float[] borderColor )
    {
        this.borderColor = borderColor;
        this.borderColorSet = true;
    }

    public float[] getTextColor( )
    {
        return textColor;
    }

    public void setTextColor( float[] textColor )
    {
        this.textColor = textColor;
        this.textColorSet = true;
    }

    public void addEvent( Event event )
    {
        if ( event == null ) return;

        // remove the event if it already exists
        this.removeEvent( event.getId( ) );

        this.eventMap.put( event.getId( ), event );
        this.addEvent0( event );
        this.visibleEventsDirty = true;
        this.plot.updateSize( );
    }

    public Event removeEvent( Object id )
    {
        Event event = this.eventMap.remove( id );

        if ( event != null )
        {
            this.removeEvent0( event );
            this.visibleEventsDirty = true;
            this.plot.updateSize( );
        }

        return event;
    }

    public Set<Event> getEvents( )
    {
        return Collections.unmodifiableSet( new HashSet<Event>( this.eventMap.values( ) ) );
    }

    public Event getEvent( Object id )
    {
        return this.eventMap.get( id );
    }

    public TextRenderer getTextRenderer( )
    {
        return this.textRenderer;
    }

    public TextureAtlas getTextureAtlas( )
    {
        return this.atlas;
    }

    public EventPainter setFont( Font font, boolean antialias )
    {
        this.newFont = font;
        this.antialias = antialias;
        this.fontSet = true;
        return this;
    }

    public boolean isHorizontal( )
    {
        return this.isHorizontal;
    }

    public Epoch getEpoch( )
    {
        return this.plot.getStackedTimePlot( ).getEpoch( );
    }

    public Set<EventSelection> getNearestEvents( GlimpseMouseEvent e )
    {
        if ( isHorizontal )
        {
            Axis1D axis = e.getAxis1D( );
            int valueY = e.getY( );
            double valueX = axis.screenPixelToValue( e.getX( ) );
            double bufferX = PICK_BUFFER_PIXELS / axis.getPixelsPerValue( );

            Epoch epoch = getEpoch( );

            TimeStamp time = epoch.toTimeStamp( valueX );
            TimeStamp timeStart = epoch.toTimeStamp( valueX - bufferX );
            TimeStamp timeEnd = epoch.toTimeStamp( valueX + bufferX );

            int rowIndex = ( int ) Math.floor( valueY / ( double ) ( getRowSize( ) + getRowBufferSize( ) ) );
            rowIndex = plot.getRowCount( ) - 1 - rowIndex;

            if ( rowIndex >= 0 && rowIndex < rows.size( ) )
            {
                Row row = rows.get( rowIndex );
                Set<Event> events = row.getMap( ).get( timeStart, timeEnd );
                Set<EventSelection> eventSelections = createEventSelection( axis, events, time );
                return eventSelections;
            }
        }

        return Collections.emptySet( );
    }

    protected Set<EventSelection> createEventSelection( Axis1D axis, Set<Event> events, TimeStamp clickTime )
    {
        Set<EventSelection> set = new HashSet<EventSelection>( );

        for ( Event event : events )
        {
            set.add( createEventSelection( axis, event, clickTime ) );
        }

        return set;
    }

    protected EventSelection createEventSelection( Axis1D axis, Event event, TimeStamp t )
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

    protected void rebuildRows0( )
    {
        rows.clear( );
        rowMap.clear( );

        for ( Event event : eventMap.values( ) )
        {
            addEvent0( event );
        }
    }

    public void setRow( Object eventId, int rowIndex )
    {
        Event event = getEvent( eventId );
        if ( event == null ) return;

        ensureRows0( rowIndex );
        Row newRow = rows.get( rowIndex );

        rowMap.remove( eventId );
        rowMap.put( eventId, newRow );

        //XXX if row is set manually, don't automatically
        //    adjust the other rows to avoid overlap

        this.visibleEventsDirty = true;
        this.plot.updateSize( );
    }

    public int getRow( Object eventId )
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

    protected void ensureRows0( int requestedIndex )
    {
        int currentRowCount = rows.size( );
        while ( requestedIndex >= currentRowCount )
        {
            rows.add( new Row( currentRowCount++ ) );
        }
    }

    protected void moveEvent0( Event event, TimeStamp newStartTime, TimeStamp newEndTime )
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
        this.plot.updateSize( );
    }

    protected void displaceEvents0( Row row, Event event )
    {
        Set<Event> overlapEvents = new HashSet<Event>( row.getOverlappingEvents( event ) );
        for ( Event overlapEvent : overlapEvents )
        {
            displaceEvent0( overlapEvent );
        }
    }

    // move an event which has been overlapped by another event
    protected void displaceEvent0( Event oldEvent )
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

    protected void clearEmptyRows0( )
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

    protected void removeEvent0( Event event )
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

    protected void shiftEvents0( Event event, Row toRow )
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

    protected void moveEventIfRoom0( Event event, Row fromRow, Row toRow )
    {
        // move the event if there is room for it and it hasn't explicitly requested its current row
        if ( !event.isFixedRow( ) && toRow.getOverlappingEvents( event ).isEmpty( ) )
        {
            fromRow.removeEvent( event );
            toRow.addEvent( event );
            shiftEvents0( event, fromRow );
        }
    }

    protected Row addEvent0( Event event )
    {
        int size = rows.size( );

        Row row = null;
        if ( shouldStack && !event.isFixedRow( ) )
        {
            for ( int i = 0; i < size; i++ )
            {
                Row candidate = rows.get( i );

                if ( candidate.getOverlappingEvents( event ).isEmpty( ) )
                {
                    row = candidate;
                    break;
                }
            }

            // add a row on top if we didn't find a non-overlapping
            // place to put this event
            if ( row == null )
            {
                row = new Row( size );
                rows.add( row );
            }

            // put the event into the non-overlapping spot we've found for it
            row.addEvent( event );
        }
        else
        {
            int requestedRow = Math.max( 0, event.getFixedRow( ) );
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

    protected void calculateVisibleEvents( double min, double max )
    {
        Epoch epoch = getEpoch( );

        for ( Row row : rows )
        {
            row.calculateVisibleEvents( epoch.toTimeStamp( min ), epoch.toTimeStamp( max ) );
        }

        this.visibleEventsDirty = false;
    }

    @Override
    public void paintTo( GL gl, GlimpseBounds bounds, Axis1D axis )
    {
        int height = bounds.getHeight( );
        int width = bounds.getWidth( );

        if ( newFont != null )
        {
            if ( textRenderer != null ) textRenderer.dispose( );
            textRenderer = new TextRenderer( newFont, antialias, false );
            newFont = null;
        }

        if ( textRenderer == null ) return;

        if ( visibleEventsDirty || axis.getMin( ) != prevMin || axis.getMax( ) != prevMax )
        {
            calculateVisibleEvents( axis.getMin( ), axis.getMax( ) );
        }

        int buffer = plot.getRowBufferSize( );
        int rowSize = plot.getRowSize( );

        int sizeMin = buffer;
        int sizeMax = buffer + rowSize;

        int size = rows.size( );
        for ( int i = 0; i < size; i++ )
        {
            Row row = rows.get( i );

            Event prev = null;
            for ( Collection<Event> eventsAtTime : row.visibleEvents.values( ) )
            {
                for ( Event event : eventsAtTime )
                {
                    if ( prev != null )
                    {
                        prev.paint( gl, axis, this, event, width, height, sizeMin, sizeMax );
                    }

                    prev = event;
                }
            }

            // paint last event
            if ( prev != null )
            {
                prev.paint( gl, axis, this, null, width, height, sizeMin, sizeMax );
            }

            sizeMin = sizeMax + buffer;
            sizeMax = sizeMax + buffer + rowSize;
        }
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a font has been manually set
        if ( !fontSet )
        {
            setFont( laf.getFont( AbstractLookAndFeel.TITLE_FONT ), false );
            fontSet = false;
        }

        if ( !textColorSet )
        {
            textColor = laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR );
            textColorNoBackground = laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR );
            textColorSet = false;
        }

        if ( !borderColorSet )
        {
            setBorderColor( laf.getColor( AbstractLookAndFeel.BORDER_COLOR ) );
            borderColorSet = false;
        }
    }
}
