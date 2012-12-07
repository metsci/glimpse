package com.metsci.glimpse.plot.timeline.painter;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter1D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.Event;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.sun.opengl.util.j2d.TextRenderer;

/**
 * Paints 1D events with associated color, time span, icon, and label information.
 * 
 * @author ulman
 */
public class EventPainter extends GlimpseDataPainter1D
{
    protected Map<Object, Event> eventMap;
    protected Map<Object, Row> rowMap;
    // map from row id to navigable sets of events within that row
    protected List<Row> rows;

    protected Epoch epoch;
    protected TextureAtlas atlas;

    protected boolean isHorizontal;

    protected boolean visibleEventsDirty = true;
    protected double prevMin;
    protected double prevMax;

    protected TextRenderer textRenderer;
    protected boolean fontSet = false;

    protected volatile Font newFont = null;
    protected volatile boolean antialias = false;

    protected int bufferPixels = 2;

    protected float[] backgroundColor = GlimpseColor.getBlack( 0.3f );
    protected float[] borderColor = GlimpseColor.getWhite( 1f );
    protected float[] textColor = GlimpseColor.getBlack( );

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
    }

    public EventPainter( Epoch epoch, TextureAtlas atlas, boolean isHorizontal )
    {
        this.epoch = epoch;
        this.atlas = atlas;

        this.rows = new ArrayList<Row>( );
        this.eventMap = new HashMap<Object, Event>( );
        this.rowMap = new HashMap<Object, Row>( );

        this.isHorizontal = isHorizontal;

        this.newFont = FontUtils.getDefaultPlain( 12 );
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
        }
    }

    public void removeEvent( Object id )
    {
        Event event = this.eventMap.remove( id );

        if ( event != null )
        {
            this.removeEvent0( event );
            this.visibleEventsDirty = true;
        }
    }

    public Event getEvent( Object id )
    {
        return this.eventMap.get( id );
    }

    public void setBuffer( int bufferPixels )
    {
        this.bufferPixels = bufferPixels;
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
        return this.epoch;
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
    }

    protected void shiftEvents0( Event event, Row toRow )
    {
        // determine if the removal of this event allows others to shift down
        int size = rows.size( );
        for ( int i = size - 1; i > toRow.index; i-- )
        {
            Row fromRow = rows.get( i );

            SortedSet<Event> subEnd = getOverlappingStartTimes0( event, toRow );
            SortedSet<Event> subStart = getOverlappingEndTimes0( event, toRow );

            // check to see if any of these candidates can be moved down to
            // fill the spot in toRow left by the deleted event
            for ( Event e : subEnd )
                moveEventIfRoom0( e, fromRow, toRow );
            for ( Event e : subStart )
                moveEventIfRoom0( e, fromRow, toRow );
        }
    }

    protected void moveEventIfRoom0( Event event, Row fromRow, Row toRow )
    {
        SortedSet<Event> subEnd = getOverlappingStartTimes0( event, toRow );
        SortedSet<Event> subStart = getOverlappingEndTimes0( event, toRow );

        if ( subEnd.isEmpty( ) && subStart.isEmpty( ) )
        {
            toRow.addEvent( event );
            fromRow.removeEvent( event );
            shiftEvents0( event, fromRow );
        }
    }

    protected SortedSet<Event> getOverlappingEndTimes0( Event event, Row row )
    {
        Event startTime = Event.createDummyEvent( event.getStartTime( ) );
        Event endTime = Event.createDummyEvent( event.getEndTime( ) );

        return row.endTimes.subSet( startTime, false, endTime, false );
    }

    protected SortedSet<Event> getOverlappingEndTimes0( Event event, Event startTime, Event endTime, Row row )
    {
        return row.endTimes.subSet( startTime, false, endTime, false );
    }

    protected SortedSet<Event> getOverlappingStartTimes0( Event event, Row row )
    {
        Event startTime = Event.createDummyEvent( event.getStartTime( ) );
        Event endTime = Event.createDummyEvent( event.getEndTime( ) );

        return row.startTimes.subSet( startTime, false, endTime, false );
    }

    protected SortedSet<Event> getOverlappingStartTimes0( Event event, Event startTime, Event endTime, Row row )
    {
        return row.startTimes.subSet( startTime, false, endTime, false );
    }

    protected void addEvent0( Event event )
    {
        Event startTime = Event.createDummyEvent( event.getStartTime( ) );
        Event endTime = Event.createDummyEvent( event.getEndTime( ) );

        int size = rows.size( );
        for ( int i = 0; i < size; i++ )
        {
            Row row = rows.get( i );

            SortedSet<Event> subEnd = getOverlappingStartTimes0( event, startTime, endTime, row );
            SortedSet<Event> subStart = getOverlappingEndTimes0( event, startTime, endTime, row );

            if ( subEnd.isEmpty( ) && subStart.isEmpty( ) )
            {
                row.addEvent( event );
                return;
            }
        }

        // we haven't found a suitable row, so create one
        Row row = new Row( size );
        rows.add( row );
        row.addEvent( event );
    }

    protected void calculateVisibleEvents( double min, double max )
    {
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

        if ( visibleEventsDirty || axis.getMin( ) != prevMin || axis.getMax( ) != prevMax )
        {
            calculateVisibleEvents( axis.getMin( ), axis.getMax( ) );
        }

        int x = bufferPixels;
        int y = ( isHorizontal ? height : width ) - bufferPixels;

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
                    prev.paint( gl, axis, this, event, width, height, x, y );
                }

                prev = event;
            }

            // paint last event
            if ( prev != null )
            {
                prev.paint( gl, axis, this, null, width, height, x, y );
            }
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
            setTextColor( laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR ) );
            textColorSet = false;
        }

        if ( !backgroundColorSet )
        {
            setBackgroundColor( laf.getColor( AbstractLookAndFeel.TEXT_BACKGROUND_COLOR ) );
            backgroundColorSet = false;
        }

        if ( !borderColorSet )
        {
            setBorderColor( laf.getColor( AbstractLookAndFeel.BORDER_COLOR ) );
            borderColorSet = false;
        }
    }
}
