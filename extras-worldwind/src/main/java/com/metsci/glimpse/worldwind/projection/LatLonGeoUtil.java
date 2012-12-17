package com.metsci.glimpse.worldwind.projection;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import com.metsci.glimpse.util.geo.LatLonGeo;

public class LatLonGeoUtil
{
    public static LatLonGeo fromPosition( Position position )
    {
        return LatLonGeo.fromDeg( position.getLatitude( ).getDegrees( ), position.getLongitude( ).getDegrees( ) );
    }
    
    public static Position toPosition( LatLonGeo latlongeo, double elevation )
    {
        return Position.fromDegrees( latlongeo.getLatDeg( ), latlongeo.getLonDeg( ), elevation );
    }
    
    public static Position toPosition( LatLonGeo latlongeo )
    {
        return Position.fromDegrees( latlongeo.getLatDeg( ), latlongeo.getLonDeg( ) );
    }
    
    public static LatLonGeo fromLatLon( LatLon latlon )
    {
        return LatLonGeo.fromDeg( latlon.latitude.getDegrees( ), latlon.longitude.getDegrees( ) );
    }
    
    public static LatLon toLatLon( LatLonGeo latlongeo )
    {
        return LatLon.fromDegrees( latlongeo.getLatDeg( ), latlongeo.getLonDeg( ) );
    }
}
