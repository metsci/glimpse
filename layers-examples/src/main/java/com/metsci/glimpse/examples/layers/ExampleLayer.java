package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.util.ImmutableCollectionUtils.mapMinus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.mapWith;
import static com.metsci.glimpse.util.PredicateUtils.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.layers.LayerRepr;
import com.metsci.glimpse.layers.LayeredView;
import com.metsci.glimpse.layers.geo.GeoView;
import com.metsci.glimpse.layers.time.TimelineView;
import com.metsci.glimpse.util.GeneralUtils;
import com.metsci.glimpse.util.var.ReadableVar;
import com.metsci.glimpse.util.var.Var;

public class ExampleLayer extends Layer
{

    protected ExampleStyle style;

    protected final List<ExamplePoint> points;

    protected final Var<ImmutableMap<LayeredView,ExampleLayerRepr>> reprs;


    public ExampleLayer( String title, float[] rgba )
    {
        this.title.set( title );

        this.style = new ExampleStyle( );
        this.style.rgbaInsideTWindow = Arrays.copyOf( rgba, 4 );
        this.style.rgbaOutsideTWindow = GeneralUtils.floats( 0.4f + 0.6f*rgba[0], 0.4f + 0.6f*rgba[1], 0.4f + 0.6f*rgba[2], 0.4f*rgba[3] );

        this.points = new ArrayList<>( );

        this.reprs = new Var<>( ImmutableMap.of( ), notNull );
    }

    @Override
    public ReadableVar<? extends Map<? extends LayeredView,? extends LayerRepr>> reprs( )
    {
        return this.reprs;
    }

    @Override
    public void installTo( LayeredView view )
    {
        if ( !this.reprs.v( ).containsKey( view ) )
        {
            if ( view instanceof GeoView )
            {
                GeoView geo = ( GeoView ) view;
                ExampleLayerRepr repr = new ExampleLayerGeoRepr( this, geo, this.style );
                this.reprs.update( ( v ) -> mapWith( v, view, repr ) );
                for ( ExamplePoint point : this.points )
                {
                    repr.addPoint( point );
                }
            }

            if ( view instanceof TimelineView )
            {
                TimelineView timeline = ( TimelineView ) view;
                ExampleLayerRepr repr = new ExampleLayerTimelineRepr( this, timeline, this.style );
                this.reprs.update( ( v ) -> mapWith( v, view, repr ) );
                for ( ExamplePoint point : this.points )
                {
                    repr.addPoint( point );
                }
            }
        }
    }

    @Override
    public void uninstallFrom( LayeredView view, boolean isReinstall )
    {
        if ( this.reprs.v( ).containsKey( view ) )
        {
            LayerRepr repr = this.reprs.v( ).get( view );
            repr.dispose( isReinstall );

            this.reprs.update( ( v ) -> mapMinus( v, view ) );
        }
    }

    public void addPoint( ExamplePoint point )
    {
        this.points.add( point );

        for ( ExampleLayerRepr repr : this.reprs.v( ).values( ) )
        {
            repr.addPoint( point );
        }
    }

}
