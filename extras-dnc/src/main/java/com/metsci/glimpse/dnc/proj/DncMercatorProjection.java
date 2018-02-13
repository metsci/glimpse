package com.metsci.glimpse.dnc.proj;

import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static java.lang.Math.cos;
import static java.lang.Math.log;
import static java.lang.Math.sin;

public class DncMercatorProjection implements DncProjection
{

    public final double originLon_DEG;
    public final double originLon_RAD;


    public DncMercatorProjection( double originLon_DEG )
    {
        this.originLon_DEG = originLon_DEG;
        this.originLon_RAD = degreesToRadians( this.originLon_DEG );
    }

    @Override
    public String configString( )
    {
        return "Mercator[ " + this.originLon_DEG + " ]";
    }

    @Override
    public double suggestedPpvMultiplier( )
    {
        return degreesToRadians;
    }

    @Override
    public boolean canProjectLibrary( int databaseNum, String libraryName, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG )
    {
        return true;
    }

    @Override
    public void projectPos( double lat_DEG, double lon_DEG, float[] result, int resultOffset )
    {
        double lat_RAD = degreesToRadians( lat_DEG );
        double lon_RAD = degreesToRadians( lon_DEG );
        result[ resultOffset + 0 ] = ( float ) ( lon_RAD - this.originLon_RAD );
        result[ resultOffset + 1 ] = ( float ) log( ( sin( lat_RAD ) + 1.0 ) / cos( lat_RAD ) );
    }

    @Override
    public double projectAzimuth_MATHRAD( double x, double y, double azimuth_MATHRAD )
    {
        return azimuth_MATHRAD;
    }

}
