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
package com.metsci.glimpse.util.geo.util;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.LatLonRect;
import com.metsci.glimpse.util.units.Azimuth;

/**
 * @author osborn
 */
public class SphereUtil
{
    /**
     * Computes the great circle distance between the specified points using the
     * Haversine formula.
     */
    public static double greatCircleDistance( LatLonGeo from, LatLonGeo to, double radius )
    {
        return greatCircleDistance( from.getLatRad( ), from.getLonRad( ), to.getLatRad( ), to.getLonRad( ), radius );
    }

    /**
     * Computes the great circle distance between the specified points using the
     * Haversine formula.
     */
    public static double greatCircleDistance( double fromLat, double fromLon, double toLat, double toLon, double radius )
    {
        double dLat = fromLat - toLat;
        double dLon = LatLonGeo.normalizeAnglePi( fromLon - toLon );

        double c1 = Math.cos( fromLat );
        double c2 = Math.cos( toLat );
        double s1 = Math.sin( 0.5d * dLat );
        double s2 = Math.sin( 0.5d * dLon );

        // guard against roundoff-related problems
        double sinA = Math.sqrt( s1 * s1 + c1 * c2 * s2 * s2 );
        if ( sinA > 1d )
        {
            sinA = 1d;
        }

        return 2d * radius * Math.asin( sinA );
    }

    /**
     * Computes the initial azimuth along the shortest great circle path
     * connecting the two specified points.
     *
     * @param from origin
     * @param to destination
     */
    public static double greatCircleAzimuth( LatLonGeo from, LatLonGeo to )
    {
        final double lat1 = from.getLatRad( );
        final double s1 = Math.sin( lat1 );
        final double c1 = Math.cos( lat1 );

        final double lat2 = to.getLatRad( );
        final double s2 = Math.sin( lat2 );
        final double c2 = Math.cos( lat2 );

        final double lon1 = from.getLonRad( );
        final double lon2 = to.getLonRad( );
        final double dLon = lon2 - lon1;
        final double sdLon = Math.sin( dLon );
        final double cdLon = Math.cos( dLon );

        final double theta = Math.atan2( sdLon * c2, c1 * s2 - s1 * c2 * cdLon );

        return Azimuth.fromNavRad( theta );
    }

    /**
     * Transformation from ECEF-r to ECEF-g coordinates.
     */
    public static LatLonGeo toLatLonGeo( double x, double y, double z, double radius )
    {
        final double lat = Math.atan2( z, Math.sqrt( x * x + y * y ) );
        final double lon = Math.atan2( y, x );
        final double h = Math.sqrt( x * x + y * y + z * z ) - radius;

        return LatLonGeo.fromRad( lat, lon, h );
    }

    /**
     * Transformation from ECEF-g to ECEF-r coordinates.
     */
    public static LatLonRect toLatLonRect( double northLatRad, double eastLonRad, double altitude, double radius )
    {
        final double sinLat = Math.sin( northLatRad );
        final double cosLat = Math.cos( northLatRad );
        final double sinLon = Math.sin( eastLonRad );
        final double cosLon = Math.cos( eastLonRad );

        final double x = ( radius + altitude ) * cosLat * cosLon;
        final double y = ( radius + altitude ) * cosLat * sinLon;
        final double z = ( radius + altitude ) * sinLat;

        return LatLonRect.fromXyz( x, y, z );
    }

    /**
     * Shifts a point along a great circle path.
     *
     * @param from starting point
     * @param radius radius of Earth
     * @param dist distance to shift point
     * @param azimuth initial azimuth of great circle
     */
    public static LatLonGeo greatCircleShift( LatLonGeo from, double radius, double dist, double azimuth )
    {
        final double lat = from.getLatRad( );
        final double cosLat1 = Math.cos( lat );
        final double sinLat1 = Math.sin( lat );

        final double r = dist / radius;
        final double cosR = Math.cos( r );
        final double sinR = Math.sin( r );

        final double b = Azimuth.toNavRad( azimuth );
        final double cosB = Math.cos( b );
        final double sinB = Math.sin( b );

        // guard against roundoff
        double sinLat = sinLat1 * cosR + cosLat1 * sinR * cosB;
        if ( sinLat > 1d )
        {
            sinLat = 1d;
        }
        else if ( sinLat < -1d )
        {
            sinLat = -1d;
        }

        final double newLat = Math.asin( sinLat );

        final double dLon = Math.atan2( sinB * sinR * cosLat1, cosR - sinLat1 * Math.sin( newLat ) );
        final double newLon = from.getLonRad( ) + dLon;

        return LatLonGeo.fromRad( newLat, newLon, from.getAltitude( ) );
    }
}
