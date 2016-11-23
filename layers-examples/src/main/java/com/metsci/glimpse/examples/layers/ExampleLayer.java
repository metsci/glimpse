package com.metsci.glimpse.examples.layers;

import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.GeoLayer;
import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.layers.LayeredGeo;
import com.metsci.glimpse.layers.LayeredTimeline;
import com.metsci.glimpse.layers.TimelineLayer;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class ExampleLayer implements Layer, GeoLayer, TimelineLayer
{

    public static class ExampleLayerModel
    {
        // WIP: Implement example model
    }


    protected final ExampleLayerModel model;

    protected GlimpsePainter geoPainter;

    protected EventPlotInfo timelineRow;
    protected TaggedAxisListener1D timeAxisListener;


    public ExampleLayer( )
    {
        this.model = new ExampleLayerModel( );

        this.geoPainter = null;

        this.timelineRow = null;
        this.timeAxisListener = null;
    }

    @Override
    public void installToGeo( LayeredGeo geo )
    {
        // WIP: Implement example painter
        this.geoPainter = new GlimpsePainter( );
        geo.dataPainter.addPainter( this.geoPainter );

        // WIP: Add model listeners
    }

    @Override
    public void uninstallFromGeo( LayeredGeo geo, GlimpseContext context )
    {
        // WIP: Remove model listeners

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
                // WIP: Update model
            }
        };
        timeline.plot.getTimeAxis( ).addAxisListener( this.timeAxisListener );

        // WIP: Add model listeners
    }

    @Override
    public void uninstallFromTimeline( LayeredTimeline timeline, GlimpseContext context )
    {
        // WIP: Remove model listeners

        timeline.plot.removePlot( this.timelineRow.getId( ) );
        this.timelineRow = null;

        timeline.plot.getTimeAxis( ).removeAxisListener( this.timeAxisListener );
        this.timeAxisListener = null;
    }

}
