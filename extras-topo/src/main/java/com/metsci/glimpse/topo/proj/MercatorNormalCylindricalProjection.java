package com.metsci.glimpse.topo.proj;

import static com.metsci.glimpse.util.math.MathConstants.HALF_PI;
import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static com.metsci.glimpse.util.units.Angle.radiansToDegrees;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.sin;

public class MercatorNormalCylindricalProjection implements NormalCylindricalProjection
{

    public final double originLon_DEG;


    public MercatorNormalCylindricalProjection( double originLon_DEG )
    {
        this.originLon_DEG = originLon_DEG;
    }

    @Override
    public double xToLon_DEG( double x )
    {
        return ( originLon_DEG + radiansToDegrees( x ) );
    }

    @Override
    public double lonToX( double lon_DEG )
    {
        return degreesToRadians( lon_DEG - originLon_DEG );
    }

    @Override
    public double yToLat_DEG( double y )
    {
        return radiansToDegrees( ( 2.0 * atan( exp( y ) ) ) - HALF_PI );
    }

    @Override
    public double latToY( double lat_DEG )
    {
        double lat_RAD = degreesToRadians( lat_DEG );
        return log( ( sin( lat_RAD ) + 1.0 ) / cos( lat_RAD ) );
    }

}
