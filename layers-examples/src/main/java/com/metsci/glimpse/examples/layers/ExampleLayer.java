package com.metsci.glimpse.examples.layers;

import static java.util.Collections.unmodifiableMap;

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

    protected String title;
    protected boolean visible;
    protected ExampleStyle style;

    protected final List<ExamplePoint> points;

    protected final Map<LayeredView,LayerRepr> allReprs;
    protected final Map<LayeredView,LayerRepr> allReprsUnmod;
    protected final Map<LayeredGeo,ExampleLayerGeoRepr> geoReprs;
    protected final Map<LayeredTimeline,ExampleLayerTimelineRepr> timelineReprs;


    public ExampleLayer( String title, float[] rgba )
    {
        this.title = title;

        this.style = new ExampleStyle( );
        this.style.rgbaInsideTWindow = Arrays.copyOf( rgba, 4 );
        this.style.rgbaOutsideTWindow = GeneralUtils.floats( 0.4f + 0.6f*rgba[0], 0.4f + 0.6f*rgba[1], 0.4f + 0.6f*rgba[2], 0.4f*rgba[3] );

        this.points = new ArrayList<>( );

        this.allReprs = new LinkedHashMap<>( );
        this.allReprsUnmod = unmodifiableMap( this.allReprs );
        this.geoReprs = new LinkedHashMap<>( );
        this.timelineReprs = new LinkedHashMap<>( );
    }

    @Override
    public String getTitle( )
    {
        return this.title;
    }

    @Override
    public boolean isVisible( )
    {
        return this.visible;
    }

    @Override
    public void setVisible( boolean visible )
    {
        if ( visible != this.visible )
        {
            this.visible = visible;

            for ( ExampleLayerGeoRepr repr : this.geoReprs.values( ) )
            {
                repr.refreshVisibility( );
            }

            for ( ExampleLayerTimelineRepr repr : this.timelineReprs.values( ) )
            {
                repr.refreshVisibility( );
            }
        }
    }

    @Override
    public Map<? extends LayeredView,? extends LayerRepr> reprs( )
    {
        return this.allReprsUnmod;
    }

    @Override
    public void installTo( LayeredView view )
    {
        if ( !this.allReprs.containsKey( view ) )
        {
            if ( view instanceof LayeredGeo )
            {
                LayeredGeo geo = ( LayeredGeo ) view;

                ExampleLayerGeoRepr repr = new ExampleLayerGeoRepr( this, geo, this.style );
                for ( ExamplePoint point : this.points )
                {
                    repr.addPoint( point );
                }

                this.allReprs.put( geo, repr );
                this.geoReprs.put( geo, repr );
            }

            if ( view instanceof LayeredTimeline )
            {
                LayeredTimeline timeline = ( LayeredTimeline ) view;

                ExampleLayerTimelineRepr repr = new ExampleLayerTimelineRepr( this, timeline, this.style );
                for ( ExamplePoint point : this.points )
                {
                    repr.addPoint( point );
                }

                this.allReprs.put( timeline, repr );
                this.timelineReprs.put( timeline, repr );
            }
        }
    }

    @Override
    public void uninstallFrom( LayeredView view, boolean isReinstall )
    {
        if ( this.allReprs.containsKey( view ) )
        {
            this.geoReprs.remove( view );
            this.timelineReprs.remove( view );
            LayerRepr repr = this.allReprs.remove( view );

            repr.dispose( isReinstall );
        }
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
