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

import com.metsci.glimpse.util.units.Angle;
import com.metsci.glimpse.util.units.Azimuth;

/**
 * A projection which maps a texture into a circle or annulus (or an angular wedge of the above).
 *
 * @author ulman
 */
public class PolarProjection implements InvertibleProjection
{
    protected double minRange, maxRange, startTheta, endTheta;

    protected double centerX, centerY;

    protected double diffRange, diffTheta;

    protected boolean rangeIsX;

    public PolarProjection( double centerX, double centerY, double minRange, double maxRange, double startTheta, double endTheta, boolean rangeIsX )
    {
        this.rangeIsX = rangeIsX;

        this.centerX = centerX;
        this.centerY = centerY;

        this.minRange = minRange;
        this.maxRange = maxRange;

        this.diffRange = ( this.maxRange - this.minRange );

        if ( startTheta < endTheta )
        {
            this.startTheta = startTheta;
            this.endTheta = endTheta;
            this.diffTheta = endTheta - startTheta;
        }
        else
        {
            this.startTheta = Angle.normalizeAngle360( startTheta );
            this.endTheta = Angle.normalizeAngle360( endTheta );
            this.diffTheta = diffTheta( this.startTheta, this.endTheta );
        }
    }

    public PolarProjection( double centerX, double centerY, double minRange, double maxRange, double startTheta, double endTheta )
    {
        this( centerX, centerY, minRange, maxRange, startTheta, endTheta, true );
    }

    public PolarProjection( double minRange, double maxRange, double startTheta, double endTheta, boolean rangeIsX )
    {
        this( 0.0, 0.0, minRange, maxRange, startTheta, endTheta, true );
    }

    public PolarProjection( double minRange, double maxRange, double startTheta, double endTheta )
    {
        this( minRange, maxRange, startTheta, endTheta, true );
    }

    public double diffTheta( double startTheta, double endTheta )
    {
        if ( startTheta < endTheta )
        {
            return endTheta - startTheta;
        }
        else
        {
            return 360 - startTheta + endTheta;
        }
    }

    public double getMinRange( )
    {
        return minRange;
    }

    public double getMaxRange( )
    {
        return maxRange;
    }

    public double getStartTheta( )
    {
        return startTheta;
    }

    public double getEndTheta( )
    {
        return endTheta;
    }

    public double getCenterX( )
    {
        return centerX;
    }

    public double getCenterY( )
    {
        return centerY;
    }

    @Override
    public void getVertexXY( double textureFractionX, double textureFractionY, float[] resultXY )
    {
        double textureIndexRange = rangeIsX ? textureFractionX : textureFractionY;
        double textureIndexTheta = !rangeIsX ? textureFractionX : textureFractionY;

        double theta = Azimuth.fromNavDeg( startTheta + diffTheta * textureIndexTheta );
        double range = minRange + diffRange * textureIndexRange;

        resultXY[0] = ( float ) ( range * Math.cos( theta ) + centerX );
        resultXY[1] = ( float ) ( range * Math.sin( theta ) + centerY );
    }

    @Override
    public void getVertexXYZ( double textureFractionX, double textureFractionY, float[] resultXYZ )
    {
        getVertexXY( textureFractionX, textureFractionY, resultXYZ );
        resultXYZ[2] = 0;
    }

    // if the x coordinate is range, the projection is linear in range, so only one
    // quad is needed, otherwise use one quad for each texture point
    @Override
    public int getSizeX( int textureSizeX )
    {
        return rangeIsX ? 1 : textureSizeX;
    }

    @Override
    public int getSizeY( int textureSizeY )
    {
        return !rangeIsX ? 1 : textureSizeY;
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
        double x = vertexX - centerX;
        double y = vertexY - centerY;

        double frac;

        if ( rangeIsX ^ getX )
        {
            double theta = Angle.normalizeAngle360( Azimuth.toNavDeg( Math.atan2( y, x ) ) );
            double thetaMinusStart;

            if ( startTheta > theta )
            {
                thetaMinusStart = 360.0 - startTheta + theta;
            }
            else
            {
                thetaMinusStart = theta - startTheta;
            }

            frac = thetaMinusStart / diffTheta;
            if ( frac < 0 ) frac = 1 + frac;
        }
        else
        {
            double range = Math.sqrt( x * x + y * y );

            frac = ( range - minRange ) / diffRange;
        }

        return frac;
    }
}
