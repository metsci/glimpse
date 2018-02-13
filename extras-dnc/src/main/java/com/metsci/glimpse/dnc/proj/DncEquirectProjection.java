package com.metsci.glimpse.dnc.proj;

import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class DncEquirectProjection implements DncProjection
{

    public final double originLon_DEG;


    public DncEquirectProjection( double originLon_DEG )
    {
        this.originLon_DEG = originLon_DEG;
    }

    @Override
    public String configString( )
    {
        return "Equirect[ " + this.originLon_DEG + " ]";
    }

    @Override
    public double suggestedPpvMultiplier( )
    {
        return 1.0;
    }

    @Override
    public boolean canProjectLibrary( int databaseNum, String libraryName, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG )
    {
        return true;
    }

    @Override
    public void projectPos( double lat_DEG, double lon_DEG, float[] result, int resultOffset )
    {
        result[ resultOffset + 0 ] = ( float ) ( lon_DEG - this.originLon_DEG );
        result[ resultOffset + 1 ] = ( float ) lat_DEG;
    }

    @Override
    public double projectAzimuth_MATHRAD( double x, double y, double azimuth_MATHRAD )
    {
        double cos_LOCAL = cos( azimuth_MATHRAD );
        double sin_LOCAL = sin( azimuth_MATHRAD );

        double lat_DEG = this.originLon_DEG + y;
        double cosLat = cos( degreesToRadians( lat_DEG ) );
        double cos_PROJ = cos_LOCAL / cosLat;
        double sin_PROJ = sin_LOCAL;

        return atan2( sin_PROJ, cos_PROJ );
    }

}
