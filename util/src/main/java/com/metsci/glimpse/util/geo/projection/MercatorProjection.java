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
package com.metsci.glimpse.util.geo.projection;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.sin;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.units.Angle;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Mercator cylindrical map projection.
 *
 * @author ulman
 */
public class MercatorProjection implements GeoProjection
{
    protected final double originLon;

    public MercatorProjection( double originLongitudeDeg )
    {
        this.originLon = Angle.degreesToRadians( originLongitudeDeg );
    }

    public MercatorProjection( )
    {
        this( 0.0 );
    }

    @Override
    public Vector2d project( LatLonGeo latLon )
    {
        double lon = latLon.getLonRad( );
        double lat = latLon.getLatRad( );

        double x = Angle.normalizeAnglePi( lon - originLon );
        double y = log( ( sin( lat ) + 1 ) / cos( lat ) );

        return new Vector2d( x, y );
    }

    @Override
    public LatLonGeo unproject( double x, double y )
    {
        double lat = 2 * atan( exp( y ) ) - PI / 2;
        double lon = x + originLon;

        return LatLonGeo.fromRad( lat, lon );
    }

    @Override
    public Vector2d reprojectFrom( double x, double y, GeoProjection fromProjection )
    {
        LatLonGeo unproj = fromProjection.unproject( x, y );
        return project( unproj );
    }

    @Override
    public KinematicVector2d reprojectPosVelFrom( double x, double y, double vx, double vy, GeoProjection fromProjection )
    {
        throw new UnsupportedOperationException( );
    }
}