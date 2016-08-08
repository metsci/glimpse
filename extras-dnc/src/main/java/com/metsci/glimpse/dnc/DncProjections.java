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

import static com.metsci.glimpse.dnc.DncTangentPlanes.azimuthToTangentPlane_MATHRAD;
import static com.metsci.glimpse.dnc.DncTangentPlanes.posToTangentPlane;
import static java.lang.Math.sqrt;

import com.metsci.glimpse.dnc.DncTangentPlanes.DncTangentPlane;
import com.metsci.glimpse.util.vector.Vector2d;

public class DncProjections
{


    public interface DncProjection
    {
        String configString( );

        double suggestedPpvMultiplier( );

        boolean canProjectLibrary( int databaseNum, String libraryName, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG );

        void projectPos( double lat_DEG, double lon_DEG, float[] result, int resultOffset );

        double projectAzimuth_MATHRAD( double x, double y, double azimuth_MATHRAD );
    }



    public static boolean canProjectBrowse( DncProjection proj )
    {
        return proj.canProjectLibrary( 1, "BROWSE", -90, 90, -180, 180 );
    }



    public static final DncProjection dncPlateCarree = new DncProjection( )
    {
        public String configString( )
        {
            return "PlateCarree";
        }

        public double suggestedPpvMultiplier( )
        {
            return 1;
        }

        public boolean canProjectLibrary( int databaseNum, String libraryName, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG )
        {
            return true;
        }

        public void projectPos( double lat_DEG, double lon_DEG, float[] result, int resultOffset )
        {
            result[ resultOffset + 0 ] = ( float ) lon_DEG;
            result[ resultOffset + 1 ] = ( float ) lat_DEG;
        }

        public double projectAzimuth_MATHRAD( double x, double y, double azimuth_MATHRAD )
        {
            // XXX
            return azimuth_MATHRAD;
        }
    };



    public static final DncProjection dncTangentPlane( double tangentLat_DEG, double tangentLon_DEG )
    {
        return dncTangentPlane( tangentLat_DEG, tangentLon_DEG, 0.0, 0.0 );
    }

    public static final DncProjection dncTangentPlane( double tangentLat_DEG, double tangentLon_DEG, Vector2d tangentPointOnPlane )
    {
        return dncTangentPlane( tangentLat_DEG, tangentLon_DEG, tangentPointOnPlane.getX( ), tangentPointOnPlane.getY( ) );
    }

    public static final DncProjection dncTangentPlane( final double tangentLat_DEG, final double tangentLon_DEG, final double tangentPointOnPlaneX, final double tangentPointOnPlaneY )
    {
        return new DncProjection( )
        {
            DncTangentPlane plane = new DncTangentPlane( tangentLat_DEG, tangentLon_DEG );

            public String configString( )
            {
                return "TangentPlane[ " + plane.tangentLat_DEG + "," + plane.tangentLon_DEG + " ]";
            }

            public double suggestedPpvMultiplier( )
            {
                // Larger -> more crowded display
                // Smaller -> emptier display
                double empiricalTweakFactor = 2;

                float[] p0 = new float[ 2 ];
                projectPos( plane.tangentLat_DEG, plane.tangentLon_DEG - 0.5, p0, 0 );

                float[] p1 = new float[ 2 ];
                projectPos( plane.tangentLat_DEG, plane.tangentLon_DEG + 0.5, p1, 0 );

                float dx = p0[ 0 ] - p1[ 0 ];
                float dy = p0[ 1 ] - p1[ 1 ];
                return sqrt( empiricalTweakFactor * ( dx*dx + dy*dy ) );
            }

            public boolean canProjectLibrary( int databaseNum, String libraryName, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG )
            {
                double antipodeLat_DEG = -tangentLat_DEG;
                double antipodeLon_DEG = normalizeLon( minLon_DEG, tangentLon_DEG + 180 );
                boolean libraryContainsAntipode = ( minLat_DEG <= antipodeLat_DEG && antipodeLat_DEG <= maxLat_DEG && minLon_DEG <= antipodeLon_DEG && antipodeLon_DEG <= maxLon_DEG );
                return ( !libraryContainsAntipode );
            }

            protected double normalizeLon( double minLon_DEG, double lon_DEG )
            {
                double offset_DEG = ( lon_DEG - minLon_DEG ) % 360;
                if ( offset_DEG < 0 ) offset_DEG += 360;
                return ( minLon_DEG + offset_DEG );
            }

            public void projectPos( double lat_DEG, double lon_DEG, float[] result, int resultOffset )
            {
                posToTangentPlane( plane, lat_DEG, lon_DEG, result, resultOffset );
            }

            public double projectAzimuth_MATHRAD( double x, double y, double azimuth_MATHRAD )
            {
                return azimuthToTangentPlane_MATHRAD( plane, x, y, azimuth_MATHRAD );
            }
        };
    }


}
