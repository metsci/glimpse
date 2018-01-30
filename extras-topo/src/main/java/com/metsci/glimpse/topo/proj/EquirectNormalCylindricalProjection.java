package com.metsci.glimpse.topo.proj;

import static com.metsci.glimpse.util.math.MathConstants.HALF_PI;
import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static com.metsci.glimpse.util.units.Angle.radiansToDegrees;

public class EquirectNormalCylindricalProjection implements NormalCylindricalProjection
{

    public static final EquirectNormalCylindricalProjection plateCarreeProj_DEG = new EquirectNormalCylindricalProjection( 0.0, true );
    public static final EquirectNormalCylindricalProjection plateCarreeProj_RAD = new EquirectNormalCylindricalProjection( 0.0, false );


    public final double originLon_RAD;
    public final boolean xyInDegrees;


    public EquirectNormalCylindricalProjection( double originLon_RAD, boolean xyInDegrees )
    {
        this.originLon_RAD = originLon_RAD;
        this.xyInDegrees = xyInDegrees;
    }

    @Override
    public double xToLon_RAD( double x )
    {
        double x_RAD = ( this.xyInDegrees ? degreesToRadians( x ) : x );
        return ( originLon_RAD + x_RAD );
    }

    @Override
    public double lonToX( double lon_RAD )
    {
        double x_RAD = lon_RAD - originLon_RAD;
        return ( this.xyInDegrees ? radiansToDegrees( x_RAD ) : x_RAD );
    }

    @Override
    public double yToLat_RAD( double y )
    {
        double y_RAD = ( this.xyInDegrees ? degreesToRadians( y ) : y );
        return y_RAD;
    }

    @Override
    public double latToY( double lat_RAD )
    {
        double y_RAD = lat_RAD;
        return ( this.xyInDegrees ? radiansToDegrees( y_RAD ) : y_RAD );
    }

    @Override
    public double dyToDlat_RAD( double y )
    {
        return ( this.xyInDegrees ? degreesToRadians( 1.0 ) : 1.0 );
    }

    @Override
    public double maxDyToDlat_RAD( double yMin, double yMax )
    {
        return ( this.xyInDegrees ? degreesToRadians( 1.0 ) : 1.0 );
    }

    @Override
    public double minUsableY( )
    {
        return ( this.xyInDegrees ? -90.0 : -HALF_PI );
    }

    @Override
    public double maxUsableY( )
    {
        return ( this.xyInDegrees ? +90.0 : +HALF_PI );
    }

}
