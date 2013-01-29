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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.media.opengl.GL;

import com.google.common.collect.Sets;
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
    // map from row id to navigable sets of events within that row
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
        NavigableSet<Event> startTimes;
        NavigableSet<Event> endTimes;
        NavigableSet<Event> visibleEvents;

        public Row( int index )
        {
            this.index = index;
            this.visibleEvents = new TreeSet<Event>( Event.getStartTimeComparator( ) );
            this.startTimes = new TreeSet<Event>( Event.getStartTimeComparator( ) );
            this.endTimes = new TreeSet<Event>( Event.getEndTimeComparator( ) );
        }

        public void addEvent( Event event )
        {
            this.startTimes.add( event );
            this.endTimes.add( event );
            rowMap.put( event.getId( ), this );
        }

        public void removeEvent( Event event )
        {
            this.startTimes.remove( event );
            this.endTimes.remove( event );
            rowMap.remove( event.getId( ) );
        }

        public void calculateVisibleEvents( Event minTimestamp, Event maxTimestamp )
        {
            // all Events whose end time is greater than the axis min
            NavigableSet<Event> endAfterMinSet = endTimes.tailSet( minTimestamp, true );

            // all Events whose start time is less than the axis max
            NavigableSet<Event> startBeforeMaxSet = startTimes.headSet( maxTimestamp, true );

            // the visible events are the intersection of the above two sets
            visibleEvents.clear( );
            visibleEvents.addAll( endAfterMinSet );
            visibleEvents.retainAll( startBeforeMaxSet );
        }

        public Set<Event> getOverlappingEvents( Event event )
        {
            SortedSet<Event> s1 = startTimes.headSet( Event.createDummyEvent( event.getEndTime( ) ), false );
            SortedSet<Event> s2 = endTimes.tailSet( Event.createDummyEvent( event.getStartTime( ) ), false );
            return Sets.intersection( s1, s2 );
        }

        public boolean isEmpty( )
        {
            return startTimes.isEmpty( );
        }

        public int size( )
        {
            return startTimes.size( );
        }

        public int getIndex( )
        {
            return index;
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

    public void setStackOverlappingEvents( boolean stack )
    {
        this.shouldStack = stack;

        if ( !stack )
        {
            unstackRows0( );
        }
        else
        {
            stackRows0( );
        }

        this.visibleEventsDirty = true;
        this.plot.updateSize( );
    }

    public int getRowSize( )
    {
        return this.plot.getRowSize( );
    }

    public int getBufferSize( )
    {
        return this.plot.getBufferSize( );
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
        if ( event != null )
        {
            this.eventMap.put( event.getId( ), event );
            this.addEvent0( event );
            this.visibleEventsDirty = true;
            this.plot.updateSize( );
        }
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
            Event eventStart = Event.createDummyEvent( timeStart );
            Event eventEnd = Event.createDummyEvent( timeEnd );

            int rowIndex = ( int ) Math.floor( valueY / ( double ) ( getRowSize( ) + getBufferSize( ) ) );
            rowIndex = rows.size( ) - 1 - rowIndex;

            if ( rowIndex >= 0 && rowIndex < rows.size( ) )
            {
                Row row = rows.get( rowIndex );
                SortedSet<Event> s1 = row.startTimes.headSet( eventEnd );
                SortedSet<Event> s2 = row.endTimes.tailSet( eventStart );

                Set<Event> events = Sets.intersection( s1, s2 );
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

    protected void stackRows0( )
    {
        Map<Object, Row> newMap = new HashMap<Object, Row>( );
        newMap.putAll( rowMap );

        rows.clear( );
        rowMap.clear( );

        for ( Event event : eventMap.values( ) )
        {
            addEvent0( event );
        }
    }

    protected void unstackRows0( )
    {
        Row newRow = new Row( 0 );

        for ( Event event : eventMap.values( ) )
        {
            newRow.addEvent( event );
        }

        rows.clear( );
        rows.add( newRow );
    }

    void moveEvent0( Event event, TimeStamp newStartTime, TimeStamp newEndTime )
    {
        Event eventOld = Event.createDummyEvent( event );

        // remove the event from its current row
        // *but don't shift events yet*
        Row oldRow = rowMap.remove( event.getId( ) );
        if ( oldRow == null ) return;
        oldRow.removeEvent( event );
        event.setTimes0( newStartTime, newEndTime );

        // add the moved version of the event back in
        // (which might land it on a different row if it
        //  has been moved over top of another event)
        addEvent0( event );

        // now shift events to fill the space left
        // by moving the event
        shiftEvents0( eventOld, oldRow );
        clearEmptyRows0( );

        this.visibleEventsDirty = true;
        this.plot.updateSize( );
    }

    protected void clearEmptyRows0( )
    {
        Iterator<Row> iter = rows.iterator( );
        while ( iter.hasNext( ) )
        {
            Row row = iter.next( );
            if ( row.isEmpty( ) ) iter.remove( );
        }

        for ( int i = 0; i < rows.size( ); i++ )
        {
            Row row = rows.get( i );
            row.setIndex( i );
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
            for ( Event e : fromRow.getOverlappingEvents( event ) )
                moveEventIfRoom0( e, fromRow, toRow );
        }
    }

    protected void moveEventIfRoom0( Event event, Row fromRow, Row toRow )
    {
        if ( toRow.getOverlappingEvents( event ).isEmpty( ) )
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
        if ( shouldStack )
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
        }
        else if ( size != 0 )
        {
            row = rows.get( 0 );
        }

        // we haven't found a suitable row, so create one
        if ( row == null )
        {
            row = new Row( size );
            rows.add( row );
        }

        row.addEvent( event );

        return row;
    }

    protected void calculateVisibleEvents( double min, double max )
    {
        Epoch epoch = getEpoch( );
        
        Event minTimestamp = Event.createDummyEvent( epoch.toTimeStamp( min ) );
        Event maxTimestamp = Event.createDummyEvent( epoch.toTimeStamp( max ) );

        for ( Row row : rows )
        {
            row.calculateVisibleEvents( minTimestamp, maxTimestamp );
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

        int buffer = plot.getBufferSize( );
        int rowSize = plot.getRowSize( );

        int sizeMin = buffer;
        int sizeMax = buffer + rowSize;

        int size = rows.size( );
        for ( int i = 0; i < size; i++ )
        {
            Row row = rows.get( i );

            Event prev = null;
            NavigableSet<Event> set = row.visibleEvents;
            for ( Event event : set )
            {
                if ( prev != null )
                {
                    prev.paint( gl, axis, this, event, width, height, sizeMin, sizeMax );
                }

                prev = event;
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
