package com.metsci.glimpse.worldwind.projection;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.KinematicVector2d;
import com.metsci.glimpse.util.vector.Vector2d;

public class PlateCarreeProjection implements GeoProjection
{
    @Override
    public Vector2d project( LatLonGeo latLon )
    {
        return new Vector2d( latLon.getLonDeg( ), latLon.getLatDeg( ) );
    }

    @Override
    public LatLonGeo unproject( double x, double y )
    {
        return LatLonGeo.fromDeg( y, x );
    }

    @Override
    public Vector2d reprojectFrom( double x, double y, GeoProjection fromProjection )
    {
        LatLonGeo unproj = fromProjection.unproject( x, y );
        return project( unproj );
    }

    @Override
    public KinematicVector2d reprojectPosVelFrom( double x, double y, double vx, double vy, GeoProjection fromProjection )
    {
        throw new UnsupportedOperationException( );
    }
}