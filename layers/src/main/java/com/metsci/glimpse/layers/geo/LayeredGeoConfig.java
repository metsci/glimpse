package com.metsci.glimpse.layers.geo;

import static com.google.common.primitives.Doubles.max;
import static com.google.common.primitives.Doubles.min;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.LayeredView;
import com.metsci.glimpse.layers.LayeredViewConfig;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.units.Azimuth;
import com.metsci.glimpse.util.vector.Vector2d;

public class LayeredGeoConfig implements LayeredViewConfig
{

    public static final String geoConfigKey = LayeredGeoConfig.class.getName( );

    public static void setDefaultGeoConfigurator( LayeredGui gui, Supplier<? extends LayeredGeoConfig> geoConfigurator )
    {
        gui.setDefaultViewConfigurator( geoConfigKey, LayeredGeoConfig.class, geoConfigurator );
    }

    public static void setGeoConfig( LayeredView view, LayeredGeoConfig geoConfig )
    {
        view.setConfig( geoConfigKey, geoConfig );
    }

    public static LayeredGeoConfig requireGeoConfig( LayeredView view )
    {
        return view.requireConfig( geoConfigKey, LayeredGeoConfig.class );
    }


    public final GeoProjection proj;
    public final Axis2D axis;

    public LayeredGeoConfig( GeoProjection proj )
    {
        this.proj = proj;
        this.axis = new Axis2D( );
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
