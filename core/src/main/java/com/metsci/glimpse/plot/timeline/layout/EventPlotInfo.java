package com.metsci.glimpse.plot.timeline.layout;

import java.awt.Font;
import java.util.UUID;

import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.Event;
import com.metsci.glimpse.plot.timeline.painter.EventPainter;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class EventPlotInfo extends TimePlotInfo
{
    protected EventPainter eventPainter;
    protected GlimpseAxisLayout1D layout1D;

    //@formatter:off
    public EventPlotInfo( TimePlotInfo delegate )
    {
        this( delegate, new TextureAtlas( ) );
    }
    
    public EventPlotInfo( TimePlotInfo delegate, TextureAtlas atlas )
    {
        super( delegate.parent,
                delegate.child,
                delegate.labelLayout,
                delegate.listener,
                delegate.gridPainter,
                delegate.axisPainter,
                delegate.labelPainter,
                delegate.borderPainter,
                delegate.labelBorderPainter,
                delegate.dataPainter );
    
        Epoch epoch = delegate.parent.getEpoch( );
        boolean isHorizontal = delegate.parent.isTimeAxisHorizontal( );
        
        if ( isHorizontal )
        {
            this.layout1D = new GlimpseAxisLayoutX( getLayout( ), "EventLayout1D" );
        }
        else
        {
            this.layout1D = new GlimpseAxisLayoutY( getLayout( ), "EventLayout1D" );
        }
        
        this.layout1D.setEventConsumer( false );
        this.eventPainter = new EventPainter( epoch, atlas, isHorizontal );
        this.layout1D.addPainter( this.eventPainter );        
    }
    //@formatter:on

    public TextureAtlas getTextureAtlas( )
    {
        return this.eventPainter.getTextureAtlas( );
    }
    
    public void setBackgroundColor( float[] backgroundColor )
    {
        this.eventPainter.setBackgroundColor( backgroundColor );
    }

    public void setBorderColor( float[] borderColor )
    {
        this.eventPainter.setBorderColor( borderColor );
    }

    public void setTextColor( float[] textColor )
    {
        this.eventPainter.setTextColor( textColor );
    }

    public void setBuffer( int bufferPixels )
    {
        this.eventPainter.setBuffer( bufferPixels );
    }

    public void setFont( Font font, boolean antialias )
    {
        this.eventPainter.setFont( font, antialias );
    }
    
    public Event addEvent( String name, TimeStamp time )
    {
        return addEvent( UUID.randomUUID( ), name, time );
    }
    
    public Event addEvent( String name, TimeStamp startTime, TimeStamp endTime )
    {
        return addEvent( UUID.randomUUID( ), name, startTime, endTime );
    }
    
    public Event addEvent( Object id, String name, TimeStamp time )
    {
        Event event = new Event( id, name, time );
        addEvent( event );
        return event;
    }
    
    public Event addEvent( Object id, String name, TimeStamp startTime, TimeStamp endTime )
    {
        Event event = new Event( id, name, startTime, endTime );
        addEvent( event );
        return event;
    }

    public void addEvent( Event event )
    {
        this.eventPainter.addEvent( event );
    }

    public void removeEvent( Event event )
    {
        this.eventPainter.removeEvent( event.getId( ) );
    }

    public void removeEvent( Object id )
    {
        this.eventPainter.removeEvent( id );
    }
}