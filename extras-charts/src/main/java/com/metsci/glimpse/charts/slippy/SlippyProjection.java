/*
 * Copyright (c) 2019 Metron, Inc.
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
package com.metsci.glimpse.charts.slippy;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.KinematicVector2d;
import com.metsci.glimpse.util.vector.Vector2d;

import static java.lang.Math.PI;

/**
 * See http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
 * @author oren
 */
public class SlippyProjection implements GeoProjection
{

    @SuppressWarnings( "unused" )
    private final int zoom;
    private final double zoomFac;

    public SlippyProjection( int zoom )
    {
        this.zoom = zoom;
        this.zoomFac = 1 << zoom;
    }

    @Override
    public Vector2d project( LatLonGeo llg )
    {
        double lonDeg = llg.getLonDeg( );
        double latRad = llg.getLatRad( );
        double x = ( lonDeg + 180 ) / 360 * zoomFac;
        double y = ( 1 - Math.log( Math.tan( latRad ) + 1 / Math.cos( latRad ) ) / PI ) / 2 * zoomFac;
        return new Vector2d( x, y );
    }

    @Override
    public LatLonGeo unproject( double x, double y )
    {
        double lon = x / zoomFac * 360.0 - 180;
        double n = PI - ( 2.0 * PI * y ) / zoomFac;
        double lat = Math.toDegrees( Math.atan( Math.sinh( n ) ) );
        return LatLonGeo.fromDeg( lat, lon );
    }

    @Override
    public Vector2d reprojectFrom( double x, double y, GeoProjection fromProjection )
    {
        return project( fromProjection.unproject( x, y ) );
    }

    @Override
    public KinematicVector2d reprojectPosVelFrom( double x, double y, double vx, double vy, GeoProjection fromProjection )
    {
        throw new UnsupportedOperationException( );
    }

}