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
package com.metsci.glimpse.util.math;

import static com.metsci.glimpse.util.GeneralUtils.doublesEqual;
import static java.lang.Double.parseDouble;

import java.util.Arrays;

import com.metsci.glimpse.util.StringUtils;

/**
 * Used for piecewise linear interpolation between given 2D points.
 *
 * @author UNKNOWN
 */
public class LinearInterpolator
{
    public static class Xy
    {
        public final double x;
        public final double y;

        public Xy( double x, double y )
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode( )
        {
            final int prime = 84239;
            int result = 1;
            result = prime * result + Double.hashCode( this.x );
            result = prime * result + Double.hashCode( this.y );
            return result;
        }

        @Override
        public boolean equals( Object o )
        {
            if ( o == this ) return true;
            if ( o == null ) return false;
            if ( o.getClass( ) != this.getClass( ) ) return false;

            Xy other = ( Xy ) o;
            return ( doublesEqual( other.x, this.x )
                  && doublesEqual( other.y, this.y ) );
        }
    }

    protected final Xy[] points;

    /**
     * @param points    the points used in the interpolation. They do NOT have to
     *                  be in increasing values of x but must have distinct x
     *                  values.
     */
    public LinearInterpolator( Xy[] points )
    {
        this.points = Arrays.copyOf( points, points.length );
        Arrays.sort( this.points, ( a, b ) -> Double.compare( a.x, b.x ) );
    }

    /**
     * Returns the interpolated y value for the given x value.
     */
    public double evaluate( double x )
    {
        int numPoints = this.points.length;

        double y;
        if ( numPoints <= 1 )
        {
            y = this.points[0].y;
        }
        else if ( x <= this.points[0].x )
        {
            y = this.points[0].y;
        }
        else if ( x >= this.points[numPoints - 1].x )
        {
            y = this.points[numPoints - 1].y;
        }
        else
        {
            int i;
            for ( i = 0; i < numPoints; ++i )
            {
                if ( this.points[i].x >= x )
                {
                    break;
                }
            }
            double x1 = this.points[i - 1].x;
            double y1 = this.points[i - 1].y;
            double x2 = this.points[i].x;
            double y2 = this.points[i].y;
            y = ( y2 - y1 ) / ( x2 - x1 ) * ( x - x1 ) + y1;
        }
        return y;
    }

    /**
     * Arrays should be arranged so that x[k] corresponds to y[k] for all k.
     */
    public static LinearInterpolator createFromArrays( double[] x, double[] y ) throws IllegalArgumentException
    {
        if ( x == null || y == null ) throw new IllegalArgumentException( );
        if ( x.length != y.length || x.length < 1 || y.length < 1 ) throw new IllegalArgumentException( );

        Xy[] points = new Xy[x.length];
        for ( int k = 0; k < x.length; ++k )
        {
            points[k] = new Xy( x[k], y[k] );
        }
        return new LinearInterpolator( points );
    }

    /**
     * Creates a <code>LinearInterpolator</code> from a <code>String</code>
     * describing the points.
     *
     * @param function  a string in the format of "x1,y1;x2,y2;x3,y3;...".
     */
    public static LinearInterpolator parseLinearInterpolator( String function ) throws IllegalArgumentException
    {
        String[] pointsString = StringUtils.split( function, ';' );
        Xy[] points = new Xy[ pointsString.length ];
        for ( int i = 0, ni = points.length; i < ni; ++i )
        {
            String[] xyString = StringUtils.split( pointsString[i], ',' );
            double x = parseDouble( xyString[0] );
            double y = parseDouble( xyString[1] );
            points[i] = new Xy( x, y );
        }
        return new LinearInterpolator( points );
    }
}
