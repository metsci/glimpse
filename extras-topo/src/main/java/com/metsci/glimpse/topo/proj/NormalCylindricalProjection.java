package com.metsci.glimpse.topo.proj;

public interface NormalCylindricalProjection
{

    double xToLon_RAD( double x );
    double yToLat_RAD( double y );

    double lonToX( double lon_RAD );
    double latToY( double lat_RAD );

    /**
     * Derivative of lat_RAD with respect to dy (i.e. dlat/dy), at the given y.
     */
    double dLatDy_RAD( double y );

    /**
     * Maximum value of dlat/dy over all y values in [yMin,yMax].
     */
    double maxDlatDy_RAD( double yMin, double yMax );

    double minUsableY( );
    double maxUsableY( );

}
