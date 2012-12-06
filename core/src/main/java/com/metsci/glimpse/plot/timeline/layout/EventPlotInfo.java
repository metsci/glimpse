package com.metsci.glimpse.plot.timeline.layout;

import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.Event;
import com.metsci.glimpse.plot.timeline.painter.EventPainter;
import com.metsci.glimpse.support.atlas.TextureAtlas;

public class EventPlotInfo extends TimePlotInfo
{
    protected EventPainter eventPainter;
    protected GlimpseAxisLayout1D layout1D;
    
    //@formatter:off
    public EventPlotInfo( TimePlotInfo delegate )
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
        TextureAtlas atlas = new TextureAtlas( );
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
