package com.metsci.glimpse.examples.layers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.layers.LayerRepr;
import com.metsci.glimpse.layers.LayeredView;
import com.metsci.glimpse.layers.geo.LayeredGeo;
import com.metsci.glimpse.layers.timeline.LayeredTimeline;
import com.metsci.glimpse.util.GeneralUtils;

public class ExampleLayer implements Layer
{

    protected final String title;
    protected final ExampleStyle style;

    protected final List<ExamplePoint> points;

    protected final Map<LayeredGeo,ExampleLayerGeoRepr> geoReprs;
    protected final Map<LayeredTimeline,ExampleLayerTimelineRepr> timelineReprs;


    public ExampleLayer( String title, float[] rgba )
    {
        this.title = title;

        this.style = new ExampleStyle( );
        this.style.rgbaInsideTWindow = Arrays.copyOf( rgba, 4 );
        this.style.rgbaOutsideTWindow = GeneralUtils.floats( 0.4f + 0.6f*rgba[0], 0.4f + 0.6f*rgba[1], 0.4f + 0.6f*rgba[2], 0.4f*rgba[3] );

        this.points = new ArrayList<>( );

        this.geoReprs = new LinkedHashMap<>( );
        this.timelineReprs = new LinkedHashMap<>( );
    }

    @Override
    public String title( )
    {
        return this.title;
    }

    @Override
    public LayerRepr installTo( LayeredView view )
    {
        if ( view instanceof LayeredGeo )
        {
            LayeredGeo geo = ( LayeredGeo ) view;
            return this.geoReprs.computeIfAbsent( geo, ( k ) ->
            {
                ExampleLayerGeoRepr repr = new ExampleLayerGeoRepr( geo, this.style );
                for ( ExamplePoint point : this.points )
                {
                    repr.addPoint( point );
                }
                return repr;
            } );
        }

        if ( view instanceof LayeredTimeline )
        {
            LayeredTimeline timeline = ( LayeredTimeline ) view;
            return this.timelineReprs.computeIfAbsent( timeline, ( k ) ->
            {
                ExampleLayerTimelineRepr repr = new ExampleLayerTimelineRepr( timeline, this.style );
                for ( ExamplePoint point : this.points )
                {
                    repr.addPoint( point );
                }
                return repr;
            } );
        }

        return null;
    }

    public void addPoint( ExamplePoint point )
    {
        this.points.add( point );

        for ( ExampleLayerGeoRepr repr : this.geoReprs.values( ) )
        {
            repr.addPoint( point );
        }

        for ( ExampleLayerTimelineRepr repr : this.timelineReprs.values( ) )
        {
            repr.addPoint( point );
        }
    }

}
