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

/**
 * An alternative means of specifying a general projection. The cleanest method
 * is to implement Projection (and possibly InvertibleProjection) and
 * manually calculate the texture fraction and vertex coordinates for any given
 * inputs. GenericProjection instead allows the vertices of the projection to
 * be directly specified.
 *
 * @author ulman
 */
public class GenericProjection implements Projection
{
    protected double[][] coordsX;
    protected double[][] coordsY;

    protected int sizeX;
    protected int sizeY;

    public GenericProjection( double[][] coordsX, double[][] coordsY )
    {
        this.coordsX = coordsX;
        this.coordsY = coordsY;

        this.sizeX = this.coordsX.length - 1;
        this.sizeY = this.coordsX[0].length - 1;
    }

    @Override
    public void getVertexXY( double dataFractionX, double dataFractionY, float[] resultXY )
    {
        resultXY[0] = ( float ) getVertex( coordsX, dataFractionX, dataFractionY );
        resultXY[1] = ( float ) getVertex( coordsY, dataFractionX, dataFractionY );
    }

    @Override
    public void getVertexXYZ( double textureFractionX, double textureFractionY, float[] resultXYZ )
    {
        getVertexXY( textureFractionX, textureFractionY, resultXYZ );
        resultXYZ[2] = 0;
    }

    protected double getVertex( double[][] data, double dataFractionX, double dataFractionY )
    {
        double indexX = dataFractionX * sizeX;
        double indexY = dataFractionY * sizeY;

        int minIndexX = ( int ) Math.floor( indexX );
        int minIndexY = ( int ) Math.floor( indexY );

        if ( minIndexX < 0 ) minIndexX = 0;
        if ( minIndexX > sizeX ) minIndexX = sizeX;

        if ( minIndexY < 0 ) minIndexY = 0;
        if ( minIndexY > sizeY ) minIndexY = sizeY;

        int maxIndexX = minIndexX + 1;
        int maxIndexY = minIndexY + 1;

        if ( maxIndexX > sizeX ) maxIndexX = sizeX;
        if ( maxIndexY > sizeY ) maxIndexY = sizeY;

        double avgBotX, avgTopX;

        if ( maxIndexX == minIndexX )
        {
            avgBotX = data[minIndexX][minIndexY];
            avgTopX = data[minIndexX][maxIndexY];
        }
        else
        {
            double fracX = ( indexX - minIndexX ) / ( maxIndexX - minIndexX );
            avgBotX = ( 1 - fracX ) * data[minIndexX][minIndexY] + fracX * data[maxIndexX][minIndexY];
            avgTopX = ( 1 - fracX ) * data[minIndexX][maxIndexY] + fracX * data[maxIndexX][maxIndexY];
        }

        if ( maxIndexY == minIndexY )
        {
            return avgBotX;
        }
        else
        {
            double fracY = ( indexY - minIndexY ) / ( maxIndexY - minIndexY );
            return ( 1 - fracY ) * avgBotX + fracY * avgTopX;
        }
    }

    @Override
    public int getSizeX( int textureSizeX )
    {
        return sizeX;
    }

    @Override
    public int getSizeY( int textureSizeY )
    {
        return sizeY;
    }

}
