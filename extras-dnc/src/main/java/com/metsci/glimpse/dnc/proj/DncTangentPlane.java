package com.metsci.glimpse.dnc.proj;

import static com.metsci.glimpse.util.GeneralUtils.doubles;
import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class DncTangentPlane implements DncProjection
{

    public final double sphereRadius;
    public final double tangentLat_DEG;
    public final double tangentLon_DEG;

    // Treat these as immutable
    protected final double[] xyzTangent_SPHERE;
    protected final double[] xyzTangentEast_SPHERE;
    protected final double[] xyzTangentNorth_SPHERE;


    public DncTangentPlane( double tangentLat_DEG, double tangentLon_DEG, double sphereRadius )
    {
        double lat_RAD = degreesToRadians( tangentLat_DEG );
        double lon_RAD = degreesToRadians( tangentLon_DEG );

        // Compute ECEF x,y,z
        double cosLat = cos( lat_RAD );
        double cosLon = cos( lon_RAD );
        double sinLat = sin( lat_RAD );
        double sinLon = sin( lon_RAD );
        double x_SPHERE = cosLat * cosLon;
        double y_SPHERE = cosLat * sinLon;
        double z_SPHERE = sinLat;

        // Compute local east vector
        double xLocalEast_SPHERE = -y_SPHERE;
        double yLocalEast_SPHERE = x_SPHERE;
        double zLocalEast_SPHERE = 0.0;
        double normLocalEast = norm3( xLocalEast_SPHERE, yLocalEast_SPHERE, zLocalEast_SPHERE );
        if ( normLocalEast == 0.0 )
        {
            xLocalEast_SPHERE = 0.0;
            yLocalEast_SPHERE = 1.0;
            zLocalEast_SPHERE = 0.0;
        }
        else
        {
            double normFactor = 1.0 / normLocalEast;
            xLocalEast_SPHERE *= normFactor;
            yLocalEast_SPHERE *= normFactor;
            zLocalEast_SPHERE *= normFactor;
        }

        // Compute local north vector
        double xLocalNorth_SPHERE = -x_SPHERE * z_SPHERE;
        double yLocalNorth_SPHERE = -y_SPHERE * z_SPHERE;
        double zLocalNorth_SPHERE = ( x_SPHERE * x_SPHERE ) + ( y_SPHERE * y_SPHERE );
        double normLocalNorth = norm3( xLocalNorth_SPHERE, yLocalNorth_SPHERE, zLocalNorth_SPHERE );
        if ( normLocalNorth == 0.0 )
        {
            xLocalNorth_SPHERE = -1.0;
            yLocalNorth_SPHERE = 0.0;
            zLocalNorth_SPHERE = 0.0;
        }
        else
        {
            double normFactor = 1.0 / normLocalNorth;
            xLocalNorth_SPHERE *= normFactor;
            yLocalNorth_SPHERE *= normFactor;
            zLocalNorth_SPHERE *= normFactor;
        }

        // Store results
        this.sphereRadius = sphereRadius;
        this.tangentLat_DEG = tangentLat_DEG;
        this.tangentLon_DEG = tangentLon_DEG;
        this.xyzTangent_SPHERE = doubles( x_SPHERE, y_SPHERE, z_SPHERE );
        this.xyzTangentEast_SPHERE = doubles( xLocalEast_SPHERE, yLocalEast_SPHERE, zLocalEast_SPHERE );
        this.xyzTangentNorth_SPHERE = doubles( xLocalNorth_SPHERE, yLocalNorth_SPHERE , zLocalNorth_SPHERE );
    }

    @Override
    public String configString( )
    {
        return "TangentPlane[ " + this.tangentLat_DEG + "," + this.tangentLon_DEG + " ]";
    }

    @Override
    public double suggestedPpvMultiplier( )
    {
        // Larger -> more crowded display
        // Smaller -> emptier display
        double empiricalTweakFactor = 2.0;

        float[] p0 = new float[ 2 ];
        this.projectPos( this.tangentLat_DEG, this.tangentLon_DEG - 0.5, p0, 0 );

        float[] p1 = new float[ 2 ];
        this.projectPos( this.tangentLat_DEG, this.tangentLon_DEG + 0.5, p1, 0 );

        float dx = p0[ 0 ] - p1[ 0 ];
        float dy = p0[ 1 ] - p1[ 1 ];
        return sqrt( empiricalTweakFactor * ( dx*dx + dy*dy ) );
    }

    @Override
    public boolean canProjectLibrary( int databaseNum, String libraryName, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG )
    {
        double antipodeLat_DEG = -this.tangentLat_DEG;
        double antipodeLon_DEG = normalizeLon( minLon_DEG, this.tangentLon_DEG + 180.0 );
        boolean libraryContainsAntipode = ( minLat_DEG <= antipodeLat_DEG && antipodeLat_DEG <= maxLat_DEG && minLon_DEG <= antipodeLon_DEG && antipodeLon_DEG <= maxLon_DEG );
        return ( !libraryContainsAntipode );
    }

    @Override
    public void projectPos( double lat_DEG, double lon_DEG, float[] result, int resultOffset )
    {
        double lat_RAD = degreesToRadians( lat_DEG );
        double lon_RAD = degreesToRadians( lon_DEG );

        // Compute unit-sphere x,y,z
        double cosLat = cos( lat_RAD );
        double cosLon = cos( lon_RAD );
        double sinLat = sin( lat_RAD );
        double sinLon = sin( lon_RAD );
        double x_SPHERE = cosLat * cosLon;
        double y_SPHERE = cosLat * sinLon;
        double z_SPHERE = sinLat;

        // Convert unit-sphere x,y,z to tangent-plane x,y
        double scale = ( 2.0 * this.sphereRadius ) / ( 1.0 + dot3( this.xyzTangent_SPHERE, x_SPHERE, y_SPHERE, z_SPHERE ) );
        double x_PLANE = scale * dot3( this.xyzTangentEast_SPHERE, x_SPHERE, y_SPHERE, z_SPHERE );
        double y_PLANE = scale * dot3( this.xyzTangentNorth_SPHERE, x_SPHERE, y_SPHERE, z_SPHERE );

        // Store results
        result[ resultOffset + 0 ] = ( float ) x_PLANE;
        result[ resultOffset + 1 ] = ( float ) y_PLANE;
    }

    @Override
    public double projectAzimuth_MATHRAD( double x_PLANE, double y_PLANE, double azimuth_MATHRAD )
    {
        double xTangent_SPHERE = this.xyzTangent_SPHERE[ 0 ];
        double yTangent_SPHERE = this.xyzTangent_SPHERE[ 1 ];
        double zTangent_SPHERE = this.xyzTangent_SPHERE[ 2 ];

        double xTangentEast_SPHERE = this.xyzTangentEast_SPHERE[ 0 ];
        double yTangentEast_SPHERE = this.xyzTangentEast_SPHERE[ 1 ];
        double zTangentEast_SPHERE = this.xyzTangentEast_SPHERE[ 2 ];

        double xTangentNorth_SPHERE = this.xyzTangentNorth_SPHERE[ 0 ];
        double yTangentNorth_SPHERE = this.xyzTangentNorth_SPHERE[ 1 ];
        double zTangentNorth_SPHERE = this.xyzTangentNorth_SPHERE[ 2 ];

        double x = x_PLANE / this.sphereRadius;
        double y = y_PLANE / this.sphereRadius;
        double x_SPHERE = xTangent_SPHERE + x*xTangentEast_SPHERE + y*xTangentNorth_SPHERE;
        double y_SPHERE = yTangent_SPHERE + x*yTangentEast_SPHERE + y*yTangentNorth_SPHERE;
        double z_SPHERE = zTangent_SPHERE + x*zTangentEast_SPHERE + y*zTangentNorth_SPHERE;

        // Compute local east vector
        double xLocalEast_SPHERE = -y_SPHERE;
        double yLocalEast_SPHERE = x_SPHERE;
        double zLocalEast_SPHERE = 0.0;
        double normLocalEast = norm3( xLocalEast_SPHERE, yLocalEast_SPHERE, zLocalEast_SPHERE );
        if ( normLocalEast == 0.0 )
        {
            xLocalEast_SPHERE = 0.0;
            yLocalEast_SPHERE = 1.0;
            zLocalEast_SPHERE = 0.0;
        }
        else
        {
            double normFactor = 1.0 / normLocalEast;
            xLocalEast_SPHERE *= normFactor;
            yLocalEast_SPHERE *= normFactor;
            zLocalEast_SPHERE *= normFactor;
        }

        // Compute local north vector
        double xLocalNorth_SPHERE = -x_SPHERE * z_SPHERE;
        double yLocalNorth_SPHERE = -y_SPHERE * z_SPHERE;
        double zLocalNorth_SPHERE = ( x_SPHERE * x_SPHERE ) + ( y_SPHERE * y_SPHERE );
        double normLocalNorth = norm3( xLocalNorth_SPHERE, yLocalNorth_SPHERE, zLocalNorth_SPHERE );
        if ( normLocalNorth == 0.0 )
        {
            xLocalNorth_SPHERE = -1.0;
            yLocalNorth_SPHERE = 0.0;
            zLocalNorth_SPHERE = 0.0;
        }
        else
        {
            double normFactor = 1.0 / normLocalNorth;
            xLocalNorth_SPHERE *= normFactor;
            yLocalNorth_SPHERE *= normFactor;
            zLocalNorth_SPHERE *= normFactor;
        }

        // Convert azimuth to unit-sphere ẋ,ẏ,ż
        double cosAzimuth = cos( azimuth_MATHRAD );
        double sinAzimuth = sin( azimuth_MATHRAD );
        double xAzimuth_SPHERE = ( cosAzimuth * xLocalEast_SPHERE ) + ( sinAzimuth * xLocalNorth_SPHERE );
        double yAzimuth_SPHERE = ( cosAzimuth * yLocalEast_SPHERE ) + ( sinAzimuth * yLocalNorth_SPHERE );
        double zAzimuth_SPHERE = ( cosAzimuth * zLocalEast_SPHERE ) + ( sinAzimuth * zLocalNorth_SPHERE );

        // Convert unit-sphere ẋ,ẏ,ż to tangent-plane ẋ,ẏ
        double scale = dot3( this.xyzTangent_SPHERE, xAzimuth_SPHERE, yAzimuth_SPHERE, zAzimuth_SPHERE ) / ( 1.0 + dot3( this.xyzTangent_SPHERE, x_SPHERE, y_SPHERE, z_SPHERE ) );
        double xAzimuth_PLANE = dot3( this.xyzTangentEast_SPHERE, xAzimuth_SPHERE, yAzimuth_SPHERE, zAzimuth_SPHERE ) - ( scale * x );
        double yAzimuth_PLANE = dot3( this.xyzTangentNorth_SPHERE, xAzimuth_SPHERE, yAzimuth_SPHERE, zAzimuth_SPHERE ) - ( scale * y );

        // Convert tangent-plane ẋ,ẏ to azimuth
        return atan2( yAzimuth_PLANE, xAzimuth_PLANE );
    }

    protected static double normalizeLon( double minLon_DEG, double lon_DEG )
    {
        double offset_DEG = ( lon_DEG - minLon_DEG ) % 360;
        if ( offset_DEG < 0 ) offset_DEG += 360;
        return ( minLon_DEG + offset_DEG );
    }

    protected static double dot3( double[] a, double b0, double b1, double b2 )
    {
        return ( a[0]*b0 + a[1]*b1 + a[2]*b2 );
    }

    protected static double norm3( double a0, double a1, double a2 )
    {
        return sqrt( a0*a0 + a1*a1 + a2*a2 );
    }

}
