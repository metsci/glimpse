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
package com.metsci.glimpse.util.geo.datum;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.LatLonRect;
import com.metsci.glimpse.util.geo.util.DistanceAzimuth;
import com.metsci.glimpse.util.geo.util.SpheroidUtil;
import com.metsci.glimpse.util.vector.Vector3d;

/**
 * @author osborn
 */
public abstract class Datum
{
    public final static Datum wgs84 = new DatumWgs84( );
    public final static DatumSphere wgs84sphere = new DatumSphereWgs84( );
    public final static DatumSphere unitSphere = new DatumSphereUnit( );

    private final double equatorialRadius;
    private final double flattening;

    private final double polarRadius;
    private final double eccentricity;
    private final double eccentricitySquared;

    public abstract LatLonGeo toWgs84( LatLonGeo llg );

    public abstract DatumSphere getSphereApproximation( );

    /**
     * Projects a geodetic latitude onto a geocentric latitude
     * using a ray through the center of the spheroid. See
     * {@link SpheroidUtil#geodeticToGeocentric(double, double)}.
     */
    public LatLonGeo toGeocentricLatitude( LatLonGeo llg )
    {
        final double latRad = llg.getLatRad( );
        final double lonRad = llg.getLonRad( );
        final double newLatRad = SpheroidUtil.geodeticToGeocentric( latRad, eccentricitySquared );
        return LatLonGeo.fromRad( newLatRad, lonRad, llg.getAltitude( ) );
    }

    /**
     * Projects a geocentric latitude onto a geodetic latitude
     * using a ray through the center of the sphere. See
     * {@link SpheroidUtil#geocentricToGeodetic(double, double)}.
     */
    public LatLonGeo toGeodeticLatitude( LatLonGeo llg )
    {
        final double latRad = llg.getLatRad( );
        final double lonRad = llg.getLonRad( );
        final double newLatRad = SpheroidUtil.geocentricToGeodetic( latRad, eccentricitySquared );
        return LatLonGeo.fromRad( newLatRad, lonRad, llg.getAltitude( ) );
    }

    /**
     * Computes the ECEF-r representation of the given (east,north,up)
     * vector given the underlying projection plane's point of tangency.
     *
     * @param enuPoint (east,north,up) coordinates of point to be converted, in system units
     * @param refPoint local tangent plane point of tangency
     * @return ECEF-r representation of the input (east,north,up) point
     */
    public LatLonRect fromEnu( Vector3d enuPoint, LatLonGeo refPoint )
    {
        final double e = enuPoint.getX( );
        final double n = enuPoint.getY( );
        final double u = enuPoint.getZ( );

        final double sinLat = Math.sin( refPoint.getLatRad( ) );
        final double cosLat = Math.cos( refPoint.getLatRad( ) );
        final double sinLon = Math.sin( refPoint.getLonRad( ) );
        final double cosLon = Math.cos( refPoint.getLonRad( ) );

        final double dx = -sinLon * e - cosLon * sinLat * n + cosLon * cosLat * u;
        final double dy = cosLon * e - sinLon * sinLat * n + sinLon * cosLat * u;
        final double dz = 0 * e + cosLat * n + sinLat * u;

        LatLonRect refPointRect = refPoint.toLatLonRect( this );
        return LatLonRect.fromXyz( refPointRect.getX( ) + dx, refPointRect.getY( ) + dy, refPointRect.getZ( ) + dz );
    }

    /**
     * Computes the (east,north,up) representation of an ECEF-r point
     * given the underlying projection plane's point of tangency.
     *
     * @param point ECEF-r point to be transformed to (east,north,up) coordinates
     * @param refPoint local tangent plane point of tangency
     * @return (x,y,z) which correspond to (east, north, up) in system units
     */
    public Vector3d toEnu( LatLonRect point, LatLonGeo refPoint )
    {
        LatLonRect refPointRect = refPoint.toLatLonRect( this );

        // catastrophic cancellation: cleverness needed to maintain precision
        final double dx = point.getX( ) - refPointRect.getX( );
        final double dy = point.getY( ) - refPointRect.getY( );
        final double dz = point.getZ( ) - refPointRect.getZ( );

        final double sinLat = Math.sin( refPoint.getLatRad( ) );
        final double cosLat = Math.cos( refPoint.getLatRad( ) );
        final double sinLon = Math.sin( refPoint.getLonRad( ) );
        final double cosLon = Math.cos( refPoint.getLonRad( ) );

        final double e = -sinLon * dx + cosLon * dy + 0 * dz;
        final double n = -sinLat * cosLon * dx - sinLat * sinLon * dy + cosLat * dz;
        final double u = cosLat * cosLon * dx + cosLat * sinLon * dy + sinLat * dz;

        return new Vector3d( e, n, u );
    }

    public LatLonGeo toLatLonGeo( LatLonRect from )
    {
        return SpheroidUtil.toLatLonGeo( from.getX( ), from.getY( ), from.getZ( ), this );
    }

    public LatLonGeo toLatLonGeo( double x, double y, double z )
    {
        return SpheroidUtil.toLatLonGeo( x, y, z, this );
    }

    public LatLonRect toLatLonRect( LatLonGeo from )
    {
        return SpheroidUtil.toLatLonRect( from.getLatRad( ), from.getLonRad( ), from.getAltitude( ), this );
    }

    public LatLonRect toLatLonRect( double northLatRad, double eastLonRad, double altitude )
    {
        return SpheroidUtil.toLatLonRect( northLatRad, eastLonRad, altitude, this );
    }

    public LatLonGeo displace( LatLonGeo from, double dist, double azimuth )
    {
        return SpheroidUtil.forward( this, from, dist, azimuth ).getPosition( );
    }

    public double getDistance( LatLonGeo from, LatLonGeo to )
    {
        return SpheroidUtil.inverse( this, from, to ).getDistance( );
    }

    public double getAzimuth( LatLonGeo from, LatLonGeo to )
    {
        return SpheroidUtil.inverse( this, from, to ).getAzimuth( );
    }

    public DistanceAzimuth getDistanceAzimuth( LatLonGeo from, LatLonGeo to )
    {
        return SpheroidUtil.inverse( this, from, to );
    }

    public Datum( double equatorialRadius, double flattening )
    {
        assert ( flattening >= 0d );
        assert ( flattening < 1d );

        this.flattening = flattening;
        this.equatorialRadius = equatorialRadius;

        this.eccentricitySquared = flattening * ( 2d - flattening );
        this.eccentricity = Math.sqrt( eccentricitySquared );
        this.polarRadius = ( 1d - flattening ) * equatorialRadius;
    }

    public double getPolarRadius( )
    {
        return polarRadius;
    }

    public double getEquatorialRadius( )
    {
        return equatorialRadius;
    }

    public double getEccentricity( )
    {
        return eccentricity;
    }

    public double getEccentricitySquared( )
    {
        return eccentricitySquared;
    }

    public double getFlattening( )
    {
        return flattening;
    }

    public boolean isSpherical( )
    {
        return flattening == 0;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits( equatorialRadius );
        result = prime * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        temp = Double.doubleToLongBits( flattening );
        result = prime * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        Datum other = ( Datum ) obj;
        if ( Double.doubleToLongBits( equatorialRadius ) != Double.doubleToLongBits( other.equatorialRadius ) ) return false;
        if ( Double.doubleToLongBits( flattening ) != Double.doubleToLongBits( other.flattening ) ) return false;
        return true;
    }
}
