package com.metsci.glimpse.topo.proj;

public interface NormalCylindricalProjection
{

    double xToLon_DEG( double x );
    double yToLat_DEG( double y );

    double lonToX( double lon_DEG );
    double latToY( double lat_DEG );

}
