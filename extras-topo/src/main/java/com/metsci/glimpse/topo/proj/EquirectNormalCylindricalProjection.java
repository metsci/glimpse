package com.metsci.glimpse.topo.proj;

import static com.metsci.glimpse.util.math.MathConstants.HALF_PI;

public class EquirectNormalCylindricalProjection implements NormalCylindricalProjection
{

    public static final EquirectNormalCylindricalProjection plateCarreeProj = new EquirectNormalCylindricalProjection( 0.0 );


    public final double originLon_RAD;


    public EquirectNormalCylindricalProjection( double originLon_RAD )
    {
        this.originLon_RAD = originLon_RAD;
    }

    @Override
    public double xToLon_RAD( double x )
    {
        return ( originLon_RAD + x );
    }

    @Override
    public double lonToX( double lon_RAD )
    {
        return ( lon_RAD - originLon_RAD );
    }

    @Override
    public double yToLat_RAD( double y )
    {
        return y;
    }

    @Override
    public double latToY( double lat_RAD )
    {
        return lat_RAD;
    }

    @Override
    public double dyToDlat_RAD( double y )
    {
        return 1.0;
    }

    @Override
    public double maxDyToDlat_RAD( double yMin, double yMax )
    {
        return 1.0;
    }

    @Override
    public double minUsableY( )
    {
        return -HALF_PI;
    }

    @Override
    public double maxUsableY( )
    {
        return +HALF_PI;
    }

}
