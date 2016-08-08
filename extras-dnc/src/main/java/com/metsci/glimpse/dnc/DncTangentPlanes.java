/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.dnc;

import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import com.metsci.glimpse.util.geo.datum.DatumSphereWgs84;

public class DncTangentPlanes
{

    // SPHERE suffix indicates unit-sphere coordinates


    public static class DncTangentPlane
    {
        public final double earthRadius;
        public final double tangentLat_DEG;
        public final double tangentLon_DEG;
        public final double[] xyzTangent_SPHERE;
        public final double[] xyzTangentEast_SPHERE;
        public final double[] xyzTangentNorth_SPHERE;

        public DncTangentPlane( double tangentLat_DEG, double tangentLon_DEG )
        {
            this( tangentLat_DEG, tangentLon_DEG, DatumSphereWgs84.Constants.avgGeodesicRadius );
        }

        public DncTangentPlane( double tangentLat_DEG, double tangentLon_DEG, double earthRadius )
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
            this.earthRadius = earthRadius;
            this.tangentLat_DEG = tangentLat_DEG;
            this.tangentLon_DEG = tangentLon_DEG;
            this.xyzTangent_SPHERE = new double[] { x_SPHERE, y_SPHERE, z_SPHERE };
            this.xyzTangentEast_SPHERE = new double[] { xLocalEast_SPHERE, yLocalEast_SPHERE, zLocalEast_SPHERE };
            this.xyzTangentNorth_SPHERE = new double[] { xLocalNorth_SPHERE, yLocalNorth_SPHERE , zLocalNorth_SPHERE };
        }
    }


    public static double dot3( double[] a, double b0, double b1, double b2 )
    {
        return ( a[0]*b0 + a[1]*b1 + a[2]*b2 );
    }


    public static double norm3( double a0, double a1, double a2 )
    {
        return sqrt( a0*a0 + a1*a1 + a2*a2 );
    }


    /**
     * Converts from (lat,lon) to (x,y) on a tangent plane
     */
    public static void posToTangentPlane( DncTangentPlane plane, double lat_DEG, double lon_DEG, float[] result, int resultOffset )
    {
        double earthRadius = plane.earthRadius;
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
        double scale = ( 2.0 * earthRadius ) / ( 1.0 + dot3( plane.xyzTangent_SPHERE, x_SPHERE, y_SPHERE, z_SPHERE ) );
        double x_PLANE = scale * dot3( plane.xyzTangentEast_SPHERE, x_SPHERE, y_SPHERE, z_SPHERE );
        double y_PLANE = scale * dot3( plane.xyzTangentNorth_SPHERE, x_SPHERE, y_SPHERE, z_SPHERE );

        // Store results
        result[ resultOffset + 0 ] = ( float ) x_PLANE;
        result[ resultOffset + 1 ] = ( float ) y_PLANE;
    }


    /**
     * Converts from local azimuth at (x,y) to projected azimuth
     */
    public static double azimuthToTangentPlane_MATHRAD( DncTangentPlane plane, double x_PLANE, double y_PLANE, double azimuth_MATHRAD )
    {
        double[] xyzTangent_SPHERE = plane.xyzTangent_SPHERE;
        double xTangent_SPHERE = xyzTangent_SPHERE[ 0 ];
        double yTangent_SPHERE = xyzTangent_SPHERE[ 1 ];
        double zTangent_SPHERE = xyzTangent_SPHERE[ 2 ];

        double[] xyzTangentEast_SPHERE = plane.xyzTangentEast_SPHERE;
        double xTangentEast_SPHERE = xyzTangentEast_SPHERE[ 0 ];
        double yTangentEast_SPHERE = xyzTangentEast_SPHERE[ 1 ];
        double zTangentEast_SPHERE = xyzTangentEast_SPHERE[ 2 ];

        double[] xyzTangentNorth_SPHERE = plane.xyzTangentNorth_SPHERE;
        double xTangentNorth_SPHERE = xyzTangentNorth_SPHERE[ 0 ];
        double yTangentNorth_SPHERE = xyzTangentNorth_SPHERE[ 1 ];
        double zTangentNorth_SPHERE = xyzTangentNorth_SPHERE[ 2 ];

        double earthRadius = plane.earthRadius;
        double x = x_PLANE / earthRadius;
        double y = y_PLANE / earthRadius;
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
        double scale = dot3( xyzTangent_SPHERE, xAzimuth_SPHERE, yAzimuth_SPHERE, zAzimuth_SPHERE ) / ( 1.0 + dot3( xyzTangent_SPHERE, x_SPHERE, y_SPHERE, z_SPHERE ) );
        double xAzimuth_PLANE = dot3( xyzTangentEast_SPHERE, xAzimuth_SPHERE, yAzimuth_SPHERE, zAzimuth_SPHERE ) - ( scale * x );
        double yAzimuth_PLANE = dot3( xyzTangentNorth_SPHERE, xAzimuth_SPHERE, yAzimuth_SPHERE, zAzimuth_SPHERE ) - ( scale * y );

        // Convert tangent-plane ẋ,ẏ to azimuth
        return atan2( yAzimuth_PLANE, xAzimuth_PLANE );
    }


}
