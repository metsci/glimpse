package com.metsci.glimpse.layers.geo;

import static com.google.common.primitives.Doubles.max;
import static com.google.common.primitives.Doubles.min;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import com.google.common.base.Objects;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.layers.LayeredExtension;
import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.LayeredView;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.units.Azimuth;
import com.metsci.glimpse.util.var.Var;
import com.metsci.glimpse.util.vector.Vector2d;

public class GeoExtension implements LayeredExtension
{

    public static final String geoExtensionKey = GeoExtension.class.getName( );

    public static void setDefaultGeoExtender( LayeredGui gui, Supplier<? extends GeoExtension> geoExtender )
    {
        gui.setDefaultExtender( geoExtensionKey, GeoExtension.class, geoExtender );
    }

    public static void setGeoExtension( LayeredView view, GeoExtension geoExtension )
    {
        view.setExtension( geoExtensionKey, geoExtension );
    }

    public static GeoExtension requireGeoExtension( LayeredView view )
    {
        return view.requireExtension( geoExtensionKey, GeoExtension.class );
    }


    public final GeoProjection proj;
    public final Axis2D axis;

    protected final Var<LayeredExtension> parent;


    public GeoExtension( GeoProjection proj )
    {
        this.proj = proj;
        this.axis = new Axis2D( );

        this.parent = new Var<>( null, ( candidate ) ->
        {
            if ( candidate == null )
            {
                return true;
            }
            else if ( candidate instanceof GeoExtension )
            {
                GeoExtension geoCandidate = ( GeoExtension ) candidate;
                return Objects.equal( geoCandidate.proj, this.proj );
            }
            else
            {
                return false;
            }
        } );

        this.parent.addListener( true, ( ) ->
        {
            GeoExtension newParent = ( GeoExtension ) this.parent.v( );
            this.axis.setParent( newParent == null ? null : newParent.axis );
        } );
    }

    @Override
    public Var<LayeredExtension> parent( )
    {
        return this.parent;
    }

    @Override
    public GeoExtension createClone( )
    {
        return new GeoExtension( this.proj );
    }

    public void setBounds( LatLonGeo center, DoubleUnaryOperator unitsToSu, double ewExtent_UNITS, double nsExtent_UNITS )
    {
        double ewExtent_SU = unitsToSu.applyAsDouble( ewExtent_UNITS );
        double nsExtent_SU = unitsToSu.applyAsDouble( nsExtent_UNITS );

        LatLonGeo[] latlons = { center.displacedBy( 0.5*ewExtent_SU, Azimuth.fromNavDeg( -90 ) ),
                                center.displacedBy( 0.5*ewExtent_SU, Azimuth.fromNavDeg( +90 ) ),
                                center.displacedBy( 0.5*nsExtent_SU, Azimuth.fromNavDeg( 0 ) ),
                                center.displacedBy( 0.5*nsExtent_SU, Azimuth.fromNavDeg( 180 ) ) };

        double[] xs_SU = new double[ latlons.length ];
        double[] ys_SU = new double[ latlons.length ];
        for ( int i = 0; i < latlons.length; i++ )
        {
            Vector2d xy_SU = proj.project( latlons[ i ] );
            xs_SU[ i ] = xy_SU.getX( );
            ys_SU[ i ] = xy_SU.getY( );
        }

        this.axis.set( min( xs_SU ), max( xs_SU ), min( ys_SU ), max( ys_SU ) );
    }

    public void setProjectedBounds( DoubleUnaryOperator unitsToSu, double xMin_UNITS, double xMax_UNITS, double yMin_UNITS, double yMax_UNITS )
    {
        this.axis.set( unitsToSu.applyAsDouble( xMin_UNITS ),
                       unitsToSu.applyAsDouble( xMax_UNITS ),
                       unitsToSu.applyAsDouble( yMin_UNITS ),
                       unitsToSu.applyAsDouble( yMax_UNITS ) );
    }

}
