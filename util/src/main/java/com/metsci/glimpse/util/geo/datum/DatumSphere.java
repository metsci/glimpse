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
import com.metsci.glimpse.util.geo.util.SphereUtil;

/**
 * @author osborn
 */
public class DatumSphere extends Datum
{
    public DatumSphere( double radius )
    {
        super( radius, 0d );
    }

    @Override
    public LatLonGeo toLatLonGeo( LatLonRect from )
    {
        return toLatLonGeo( from.getX( ), from.getY( ), from.getZ( ) );
    }

    @Override
    public LatLonGeo toLatLonGeo( double x, double y, double z )
    {
        return SphereUtil.toLatLonGeo( x, y, z, getRadius( ) );
    }

    @Override
    public LatLonRect toLatLonRect( LatLonGeo from )
    {
        return toLatLonRect( from.getLatRad( ), from.getLonRad( ), from.getAltitude( ) );
    }

    @Override
    public LatLonRect toLatLonRect( double northLatRad, double eastLonRad, double altitude )
    {
        return SphereUtil.toLatLonRect( northLatRad, eastLonRad, altitude, getRadius( ) );
    }

    @Override
    public LatLonGeo displace( LatLonGeo from, double dist, double azimuth )
    {
        return SphereUtil.greatCircleShift( from, getRadius( ), dist, azimuth );
    }

    @Override
    public double getDistance( LatLonGeo from, LatLonGeo to )
    {
        return SphereUtil.greatCircleDistance( from, to, getRadius( ) );
    }

    @Override
    public double getAzimuth( LatLonGeo from, LatLonGeo to )
    {
        return SphereUtil.greatCircleAzimuth( from, to );
    }

    @Override
    public DistanceAzimuth getDistanceAzimuth( LatLonGeo from, LatLonGeo to )
    {
        return new DistanceAzimuth( getDistance( from, to ), getAzimuth( from, to ) );
    }

    @Override
    public boolean isSpherical( )
    {
        return true;
    }

    @Override
    public DatumSphere getSphereApproximation( )
    {
        return this;
    }

    /**
     * Converts geocentric latitude to a geodetic latitude.  See
     * {@link Datum#toGeodeticLatitude(LatLonGeo)}.
     *
     * <p>
     * <b>NOTE:</b> Altitude is passed through unchanged.
     * </p>
     */
    @Override
    public LatLonGeo toWgs84( LatLonGeo llg )
    {
        return Datum.wgs84.toGeodeticLatitude( llg );
    }

    public double getRadius( )
    {
        return getEquatorialRadius( );
    }
}
