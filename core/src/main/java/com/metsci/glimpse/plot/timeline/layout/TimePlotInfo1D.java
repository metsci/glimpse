package com.metsci.glimpse.plot.timeline.layout;

public class TimePlotInfo1D extends TimePlotInfo
{ 
    //@formatter:off
    public TimePlotInfo1D( TimePlotInfo delegate )
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
    
    
    }
    //@formatter:on

}
