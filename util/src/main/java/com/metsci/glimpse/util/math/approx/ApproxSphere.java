/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.util.math.approx;

import static java.lang.Math.*;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.datum.DatumSphere;
import com.metsci.glimpse.util.geo.util.SphereUtil;
import com.metsci.glimpse.util.units.Azimuth;

/**
 * Similar to {@link SphereUtil}, but uses classes from {@link com.metsci.glimpse.util.math.approx}
 * for trig calls.
 */
public class ApproxSphere
{

    public final ApproxSin approxSin;
    public final ApproxCos approxCos;
    public final ApproxAsin approxAsin;
    public final ApproxAtan approxAtan;


    public ApproxSphere( int numSamples )
    {
        this.approxSin = new ApproxSin( numSamples );
        this.approxCos = new ApproxCos( numSamples );
        this.approxAsin = new ApproxAsin( numSamples );
        this.approxAtan = new ApproxAtan( numSamples );
    }
    
    public LatLonGeo toLatLonGeo( double x, double y, double z, double radius )
    {
        final double lat = approxAtan.atan2( z, Math.sqrt( x * x + y * y ) );
        final double lon = approxAtan.atan2( y, x );
        final double h = Math.sqrt( x * x + y * y + z * z ) - radius;

        return LatLonGeo.fromRad( lat, lon, h );
    }

    public LatLonGeo greatCircleShift( DatumSphere sphere, LatLonGeo from, double distance, double azimuth_SU )
    {
        return this.greatCircleShift( sphere, from, distance, azimuth_SU, new double[ 2 ] );
    }

    public LatLonGeo greatCircleShift( DatumSphere sphere, LatLonGeo from, double distance, double azimuth_SU, double[] temp2 )
    {
        this.greatCircleShift( from.getLatRad( ), from.getLonRad( ), distance / sphere.getRadius( ), Azimuth.toNavRad( azimuth_SU ), temp2 );
        return LatLonGeo.fromRad( temp2[ 0 ], temp2[ 1 ] );
    }

    public void greatCircleShift( double fromLat_RAD, double fromLon_RAD, double distance_RAD, double azimuth_NAVRAD, double[] resultLatLon_RAD )
    {
        double lat = fromLat_RAD;
        double cosLat1 = this.approxCos.cos( lat );
        double sinLat1 = this.approxSin.sin( lat );

        double r = distance_RAD;
        double cosR = this.approxCos.cos( r );
        double sinR = this.approxSin.sin( r );

        double b = azimuth_NAVRAD;
        double cosB = this.approxCos.cos( b );
        double sinB = this.approxSin.sin( b );

        double sinLat = max( -1.0, min( +1.0, ( sinLat1 * cosR ) + ( cosLat1 * sinR * cosB ) ) );
        double newLat = this.approxAsin.asin( sinLat );

        double dLon = this.approxAtan.atan2( sinB * sinR * cosLat1, cosR - ( sinLat1 * this.approxSin.sin( newLat ) ) );
        double newLon = fromLon_RAD + dLon;

        resultLatLon_RAD[ 0 ] = newLat;
        resultLatLon_RAD[ 1 ] = newLon;
    }

    public double greatCircleDistance( DatumSphere sphere, LatLonGeo from, LatLonGeo to )
    {
        double result_RAD = this.greatCircleDistance_RAD( from.getLatRad( ), from.getLonRad( ), to.getLatRad( ), to.getLonRad( ) );
        return ( sphere.getRadius( ) * result_RAD );
    }

    public double greatCircleDistance_RAD( double fromLat_RAD, double fromLon_RAD, double toLat_RAD, double toLon_RAD )
    {
        double dLat = fromLat_RAD - toLat_RAD;
        double dLon = fromLon_RAD - toLon_RAD;

        double c1 = this.approxCos.cos( fromLat_RAD );
        double c2 = this.approxCos.cos( toLat_RAD );
        double s1 = this.approxSin.sin( 0.5 * dLat );
        double s2 = this.approxSin.sin( 0.5 * dLon );

        double sinA = sqrt( s1*s1 + c1*c2*s2*s2 );

        // Guard against roundoff-related problems
        if ( sinA > 1.0 )
        {
            sinA = 1.0;
        }

        return ( 2.0 * this.approxAsin.asin( sinA ) );
    }

    public double greatCircleAzimuth( LatLonGeo from, LatLonGeo to )
    {
        double result_NAVRAD = this.greatCircleAzimuth_NAVRAD( from.getLatRad( ), from.getLonRad( ), to.getLatRad( ), to.getLonRad( ) );
        return Azimuth.fromNavRad( result_NAVRAD );
    }

    public double greatCircleAzimuth_NAVRAD( double fromLat_RAD, double fromLon_RAD, double toLat_RAD, double toLon_RAD )
    {
        double lat1 = fromLat_RAD;
        double s1 = this.approxSin.sin( lat1 );
        double c1 = this.approxCos.cos( lat1 );

        double lat2 = toLat_RAD;
        double s2 = this.approxSin.sin( lat2 );
        double c2 = this.approxCos.cos( lat2 );

        double lon1 = fromLon_RAD;
        double lon2 = toLon_RAD;
        double dLon = lon2 - lon1;
        double sdLon = this.approxSin.sin( dLon );
        double cdLon = this.approxCos.cos( dLon );

        return this.approxAtan.atan2( sdLon*c2, c1*s2 - s1*c2*cdLon );
    }

}
