package com.metsci.glimpse.layers.geo;

import static com.google.common.primitives.Doubles.max;
import static com.google.common.primitives.Doubles.min;

import java.util.function.DoubleUnaryOperator;

import com.google.common.base.Objects;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.Trait;
import com.metsci.glimpse.layers.View;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.units.Azimuth;
import com.metsci.glimpse.util.vector.Vector2d;

public class GeoTrait extends Trait
{

    public static final String geoTraitKey = GeoTrait.class.getName( );

    public static void addGeoLinkage( LayeredGui gui, String name, GeoTrait master )
    {
        gui.addLinkage( geoTraitKey, name, master );
    }

    public static void setGeoTrait( View view, GeoTrait geoTrait )
    {
        view.setTrait( geoTraitKey, geoTrait );
    }

    public static GeoTrait requireGeoTrait( View view )
    {
        return view.requireTrait( geoTraitKey, GeoTrait.class );
    }


    public final GeoProjection proj;
    public final Axis2D axis;


    public GeoTrait( boolean isLinkage, GeoProjection proj )
    {
        super( isLinkage );

        this.proj = proj;
        this.axis = new Axis2D( );

        this.parent.addListener( true, ( ) ->
        {
            GeoTrait newParent = ( GeoTrait ) this.parent.v( );
            this.axis.setParent( newParent == null ? null : newParent.axis );
        } );
    }

    @Override
    protected boolean isValidParent( Trait linkage )
    {
        if ( linkage instanceof GeoTrait )
        {
            GeoTrait geoLinkage = ( GeoTrait ) linkage;
            return Objects.equal( geoLinkage.proj, this.proj );
        }
        else
        {
            return false;
        }
    }

    @Override
    public GeoTrait copy( boolean isLinkage )
    {
        GeoTrait copy = new GeoTrait( isLinkage, this.proj );

        // Copy axis settings
        copy.axis.setParent( this.axis );
        copy.axis.setParent( null );

        return copy;
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

    public void setSelectionSize( DoubleUnaryOperator unitsToSu, double size_UNITS )
    {
        this.setSelectionSize( unitsToSu, size_UNITS, size_UNITS );
    }

    public void setSelectionSize( DoubleUnaryOperator unitsToSu, double xSize_UNITS, double ySize_UNITS )
    {
        this.axis.getAxisX( ).setSelectionSize( unitsToSu.applyAsDouble( xSize_UNITS ) );
        this.axis.getAxisY( ).setSelectionSize( unitsToSu.applyAsDouble( ySize_UNITS ) );
    }

}
