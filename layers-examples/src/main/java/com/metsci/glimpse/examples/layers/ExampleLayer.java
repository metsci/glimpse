package com.metsci.glimpse.examples.layers;

import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.GeoLayer;
import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.layers.LayeredGeo;
import com.metsci.glimpse.layers.LayeredTimeline;
import com.metsci.glimpse.layers.TimelineLayer;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;

public class ExampleLayer implements Layer, GeoLayer, TimelineLayer
{

    protected ExampleGeoPainter geoPainter;

    protected EventPlotInfo timelineRow;
    protected TaggedAxisListener1D timeAxisListener;


    public ExampleLayer( )
    {
        this.geoPainter = null;

        this.timelineRow = null;
        this.timeAxisListener = null;
    }

    public void addPoint( long time_PMILLIS, float x_SU, float y_SU, float z_SU )
    {
        this.geoPainter.addPoint( time_PMILLIS, x_SU, y_SU, z_SU );
    }

    @Override
    public void installToGeo( LayeredGeo geo )
    {
        this.geoPainter = new ExampleGeoPainter( );
        geo.dataPainter.addPainter( this.geoPainter );
    }

    @Override
    public void uninstallFromGeo( LayeredGeo geo, GlimpseContext context )
    {
        geo.dataPainter.removePainter( this.geoPainter );
        this.geoPainter.dispose( context );
        this.geoPainter = null;
    }

    @Override
    public void installToTimeline( LayeredTimeline timeline )
    {
        this.timelineRow = timeline.plot.createEventPlot( );
        this.timelineRow.setGrow( false );
        this.timelineRow.setLabelText( "Example" );

        this.timeAxisListener = new TaggedAxisListener1D( )
        {
            @Override
            public void tagsUpdated( TaggedAxis1D axis )
            {
                long tMin_PMILLIS = timeline.plot.getTimeSelectionMin( ).toPosixMillis( );
                long tMax_PMILLIS = timeline.plot.getTimeSelectionMax( ).toPosixMillis( );
                long tCursor_PMILLIS = timeline.plot.getTimeSelection( ).toPosixMillis( );
                geoPainter.setTimeSelection( tMin_PMILLIS, tMax_PMILLIS, tCursor_PMILLIS );
            }
        };
        timeline.plot.getTimeAxis( ).addAxisListener( this.timeAxisListener );
    }

    @Override
    public void uninstallFromTimeline( LayeredTimeline timeline, GlimpseContext context )
    {
        timeline.plot.getTimeAxis( ).removeAxisListener( this.timeAxisListener );
        this.timeAxisListener = null;

        timeline.plot.removePlot( this.timelineRow.getId( ) );
        this.timelineRow = null;
    }

}
