package com.metsci.glimpse.charts.slippy;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.KinematicVector2d;
import com.metsci.glimpse.util.vector.Vector2d;

import static java.lang.Math.PI;

/**
 * See http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
 * @author oren
 */
public class SlippyProjection implements GeoProjection
{

    @SuppressWarnings( "unused" )
    private final int zoom;
    private final double zoomFac;

    public SlippyProjection( int zoom )
    {
        this.zoom = zoom;
        this.zoomFac = 1 << zoom;
    }

    @Override
    public Vector2d project( LatLonGeo llg )
    {
        double lonDeg = llg.getLonDeg( );
        double latRad = llg.getLatRad( );
        double x = ( lonDeg + 180 ) / 360 * zoomFac;
        double y = ( 1 - Math.log( Math.tan( latRad ) + 1 / Math.cos( latRad ) ) / PI ) / 2 * zoomFac;
        return new Vector2d( x, y );
    }

    @Override
    public LatLonGeo unproject( double x, double y )
    {
        double lon = x / zoomFac * 360.0 - 180;
        double n = PI - ( 2.0 * PI * y ) / zoomFac;
        double lat = Math.toDegrees( Math.atan( Math.sinh( n ) ) );
        return LatLonGeo.fromDeg( lat, lon );
    }

    @Override
    public Vector2d reprojectFrom( double x, double y, GeoProjection fromProjection )
    {
        return project( fromProjection.unproject( x, y ) );
    }

    @Override
    public KinematicVector2d reprojectPosVelFrom( double x, double y, double vx, double vy, GeoProjection fromProjection )
    {
        throw new UnsupportedOperationException( );
    }

}