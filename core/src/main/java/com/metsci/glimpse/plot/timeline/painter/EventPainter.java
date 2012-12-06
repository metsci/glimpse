package com.metsci.glimpse.plot.timeline.painter;

import java.awt.Font;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter1D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.Event;
import com.metsci.glimpse.support.atlas.TextureAtlas;
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
    protected NavigableSet<Event> startTimes;
    protected NavigableSet<Event> endTimes;

    protected Epoch epoch;
    protected TextureAtlas atlas;

    protected boolean isHorizontal;

    protected Set<Event> visibleEvents;
    protected double prevMin;
    protected double prevMax;

    
    protected TextRenderer textRenderer;
    protected boolean fontSet = false;

    protected volatile Font newFont = null;
    protected volatile boolean antialias = false;
    
    protected int bufferPixels = 2;
    
    public EventPainter( Epoch epoch, TextureAtlas atlas, boolean isHorizontal )
    {
        this.epoch = epoch;
        this.atlas = atlas;

        this.startTimes = new TreeSet<Event>( Event.getStartTimeComparator( ) );
        this.endTimes = new TreeSet<Event>( Event.getEndTimeComparator( ) );
        this.eventMap = new HashMap<Object, Event>( );

        this.isHorizontal = isHorizontal;
    }

    public void addEvent( Event event )
    {
        if ( event != null )
        {
            this.eventMap.put( event.getId( ), event );
            this.startTimes.add( event );
            this.endTimes.add( event );
            this.visibleEvents = null;
        }
    }

    public void removeEvent( Object id )
    {
        Event event = this.eventMap.remove( id );

        if ( event != null )
        {
            this.startTimes.remove( event );
            this.endTimes.remove( event );
            this.visibleEvents = null;
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

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a font has been manually set
        if ( !fontSet )
        {
            setFont( laf.getFont( AbstractLookAndFeel.AXIS_FONT ), false );
            fontSet = false;
        }
    }

    protected void calculateVisibleEvents( double min, double max )
    {
        Event minTimestamp = Event.createDummyEvent( epoch.toTimeStamp( min ) );
        Event maxTimestamp = Event.createDummyEvent( epoch.toTimeStamp( max ) );

        // all Events whose end time is greater than the axis min
        NavigableSet<Event> endAfterMinSet = endTimes.tailSet( minTimestamp, true );

        // all Events whose start time is less than the axis max
        NavigableSet<Event> startBeforeMaxSet = startTimes.headSet( maxTimestamp, true );

        // the visible events are the intersection of the above two sets
        visibleEvents = new HashSet<Event>( );
        visibleEvents.addAll( endAfterMinSet );
        visibleEvents.retainAll( startBeforeMaxSet );
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

        if ( visibleEvents == null || axis.getMin( ) != prevMin || axis.getMax( ) != prevMax )
        {
            calculateVisibleEvents( axis.getMin( ), axis.getMax( ) );
        }

        for ( Event event : visibleEvents )
        {
            System.out.println( "painting " + event.getId( ) );
            
            event.paint( gl, axis, this, bufferPixels, (isHorizontal ? height : width)-bufferPixels );
        }
    }
}
