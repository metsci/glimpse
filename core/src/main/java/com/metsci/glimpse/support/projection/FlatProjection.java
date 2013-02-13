/*
 * Copyright (c) 2012, Metron, Inc.
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

import com.metsci.glimpse.axis.Axis2D;

/**
 * A simple projection which maps a texture onto a rectangle in physical
 * coordinates with bottom-left corner at (minX, minY) and upper-right
 * corner at (maxX, maxY).
 *
 * @author ulman
 *
 */
public class FlatProjection implements Projection, InvertibleProjection
{
    protected double minX, maxX, minY, maxY;

    protected double diffX, diffY;

    public FlatProjection( Axis2D axes )
    {
        this(  axes.getMinX( ), axes.getMaxX( ), axes.getMinY( ), axes.getMaxY( ) );
    }
    
    public FlatProjection( double minX, double maxX, double minY, double maxY )
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.diffX = ( maxX - minX );
        this.diffY = ( maxY - minY );
    }

    @Override
    public void getVertexXY( double textureFractionX, double textureFractionY, float[] resultXY )
    {
        resultXY[0] = ( float ) ( minX + diffX * textureFractionX );
        resultXY[1] = ( float ) ( minY + diffY * textureFractionY );
    }

    @Override
    public void getVertexXYZ( double textureFractionX, double textureFractionY, float[] resultXYZ )
    {
        getVertexXY( textureFractionX, textureFractionY, resultXYZ );
        resultXYZ[2] = 0;
    }

    // the projection is linear, so only one quad is necessary for the entire texture
    @Override
    public int getSizeX( int textureSizeX )
    {
        return 1;
    }

    @Override
    public int getSizeY( int textureSizeY )
    {
        return 1;
    }

    @Override
    public double getTextureFractionX( double vertexX, double vertexY )
    {
        return ( vertexX - minX ) / diffX;
    }

    @Override
    public double getTextureFractionY( double vertexX, double vertexY )
    {
        return ( vertexY - minY ) / diffY;
    }

    public double getMinX( )
    {
        return minX;
    }

    public double getMaxX( )
    {
        return maxX;
    }

    public double getMinY( )
    {
        return minY;
    }

    public double getMaxY( )
    {
        return maxY;
    }
}
