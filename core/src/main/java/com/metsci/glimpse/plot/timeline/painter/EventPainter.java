package com.metsci.glimpse.plot.timeline.painter;

import java.awt.Font;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter1D;
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
public class EventPainter extends GlimpsePainter1D
{
    protected NavigableSet<Event> startTimes;
    protected NavigableSet<Event> endTimes;

    private TextRenderer textRenderer;
    private boolean fontSet = false;

    private volatile Font newFont = null;
    private volatile boolean antialias = false;

    protected Epoch epoch;
    protected TextureAtlas atlas;

    protected boolean isHorizontal;

    protected Set<Event> visibleEvents;
    protected double prevMin;
    protected double prevMax;

    public EventPainter( Epoch epoch, TextureAtlas atlas, boolean isHorizontal )
    {
        this.epoch = epoch;
        this.atlas = atlas;

        this.startTimes = new TreeSet<Event>( Event.getStartTimeComparator( ) );
        this.endTimes = new TreeSet<Event>( Event.getEndTimeComparator( ) );

        this.isHorizontal = isHorizontal;
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
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        int height = bounds.getHeight( );
        int width = bounds.getWidth( );

        GL gl = context.getGL( );

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
            event.paint( gl, axis, this, 0, isHorizontal ? height : width );
        }
    }
}
