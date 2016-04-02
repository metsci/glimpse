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
package com.metsci.glimpse.support.projection;

import static java.lang.Math.floor;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * A projection which maps a texture with data values spaced uniformly in lat/lon
 * space onto a flat plane defined by the provided GeoProjection.
 *
 * @author ulman
 */
public class LatLonProjection implements InvertibleProjection
{
    public static final double SIZE_DOWNSAMPLE = 0.1;

    protected double minLat, maxLat, minLon, maxLon;

    protected double diffLat, diffLon;

    protected GeoProjection projection;

    protected boolean latIsX;

    public LatLonProjection( GeoProjection projection, double minLat, double maxLat, double minLon, double maxLon, boolean latIsX )
    {
        this.latIsX = latIsX;

        this.projection = projection;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;

        this.diffLat = ( this.maxLat - this.minLat );
        this.diffLon = ( this.maxLon - this.minLon );
    }

    public LatLonProjection( GeoProjection projection, double minLat, double maxLat, double minLon, double maxLon )
    {
        this( projection, minLat, maxLat, minLon, maxLon, true );
    }

    @Override
    public void getVertexXY( double textureFractionX, double textureFractionY, float[] resultXY )
    {
        Vector2d xy = project( textureFractionX, textureFractionY );
        resultXY[0] = ( float ) xy.getX( );
        resultXY[1] = ( float ) xy.getY( );
    }

    @Override
    public void getVertexXYZ( double textureFractionX, double textureFractionY, float[] resultXYZ )
    {
        getVertexXY( textureFractionX, textureFractionY, resultXYZ );
        resultXYZ[2] = 0;
    }

    protected Vector2d project( double textureFractionX, double textureFractionY )
    {
        double lat0 = minLat + diffLat * ( latIsX ? textureFractionX : textureFractionY );
        double lon0 = minLon + diffLon * ( !latIsX ? textureFractionX : textureFractionY );

        int poleCrosses = ( int ) floor( ( lat0 + 90 ) / 180 );
        boolean latWrapped = ( poleCrosses % 2 != 0 );
        double lat = ( latWrapped ? -1 : 1 ) * ( lat0 - 180 * poleCrosses );
        double lon = ( latWrapped ? 180 : 0 ) + lon0;

        return projection.project( LatLonGeo.fromDeg( lat, lon ) );
    }

    @Override
    public int getSizeX( int textureSizeX )
    {
        return ( int ) Math.round( Math.max( 1, textureSizeX * SIZE_DOWNSAMPLE ) );
    }

    @Override
    public int getSizeY( int textureSizeY )
    {
        return ( int ) Math.round( Math.max( 1, textureSizeY * SIZE_DOWNSAMPLE ) );
    }

    @Override
    public double getTextureFractionX( double vertexX, double vertexY )
    {
        return getTextureFraction( vertexX, vertexY, true );
    }

    @Override
    public double getTextureFractionY( double vertexX, double vertexY )
    {
        return getTextureFraction( vertexX, vertexY, false );
    }

    protected double getTextureFraction( double vertexX, double vertexY, boolean getX )
    {
        LatLonGeo latLon = projection.unproject( vertexX, vertexY );

        if ( latIsX ^ getX )
        {
            return ( latLon.getLonDeg( ) - minLon ) / diffLon;
        }
        else
        {
            return ( latLon.getLatDeg( ) - minLat ) / diffLat;
        }
    }
}
