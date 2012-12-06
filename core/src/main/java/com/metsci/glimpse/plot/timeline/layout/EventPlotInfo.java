package com.metsci.glimpse.plot.timeline.layout;

import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.Event;
import com.metsci.glimpse.plot.timeline.painter.EventPainter;
import com.metsci.glimpse.support.atlas.TextureAtlas;

public class EventPlotInfo extends TimePlotInfo
{
    protected EventPainter eventPainter;
    
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
        
        this.eventPainter = new EventPainter( epoch, atlas, isHorizontal );
        
        this.dataPainter.addPainter( this.eventPainter );
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
