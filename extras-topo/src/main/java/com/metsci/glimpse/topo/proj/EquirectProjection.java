package com.metsci.glimpse.topo.proj;

public class EquirectProjection implements NormalCylindricalProjection
{

    public static final EquirectProjection plateCarreeProj = new EquirectProjection( 0.0 );


    public final double originLon_DEG;


    public EquirectProjection( double originLon_DEG )
    {
        this.originLon_DEG = originLon_DEG;
    }

    @Override
    public double xToLon_DEG( double x )
    {
        return ( originLon_DEG + x );
    }

    @Override
    public double lonToX( double lon_DEG )
    {
        return ( lon_DEG - originLon_DEG );
    }

    @Override
    public double yToLat_DEG( double y )
    {
        return y;
    }

    @Override
    public double latToY( double lat_DEG )
    {
        return lat_DEG;
    }

}
