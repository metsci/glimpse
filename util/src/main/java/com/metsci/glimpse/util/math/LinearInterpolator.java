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
package com.metsci.glimpse.util.math;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Comparator;

import com.metsci.glimpse.util.StringUtils;

/**
 * Used for piecewise linear interpolation between given 2D points.
 *
 * @author UNKNOWN
 */
public class LinearInterpolator
{
    private Point2D[] _points;

    /**
     * @param points    the points used in the interpolation. They do NOT have to
     *                  be in increasing values of x but must have distinct x
     *                  values.
     */
    public LinearInterpolator( Point2D[] points )
    {
        _points = points;
        // Order points based on their x value.
        Comparator<Point2D> comparator = new Comparator<Point2D>( )
        {
            public int compare( Point2D p1, Point2D p2 )
            {
                double x1 = p1.getX( );
                double x2 = p2.getX( );

                if ( x1 < x2 )
                {
                    return -1;
                }
                else if ( x1 > x2 )
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        };
        Arrays.sort( points, comparator );
    }

    /**
     * Returns the interpolated y value for the given x value.
     */
    public double evaluate( double x )
    {
        int numPoints = _points.length;
        double y;
        if ( numPoints <= 1 )
        {
            y = _points[0].getY( );
        }
        else if ( x <= _points[0].getX( ) )
        {
            y = _points[0].getY( );
        }
        else if ( x >= _points[numPoints - 1].getX( ) )
        {
            y = _points[numPoints - 1].getY( );
        }
        else
        {
            int i;
            for ( i = 0; i < numPoints; ++i )
            {
                if ( _points[i].getX( ) >= x )
                {
                    break;
                }
            }
            double x1 = _points[i - 1].getX( );
            double y1 = _points[i - 1].getY( );
            double x2 = _points[i].getX( );
            double y2 = _points[i].getY( );
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

        Point2D[] points = new Point2D[x.length];
        for ( int k = 0; k < x.length; ++k )
        {
            points[k] = new Point2D.Double( );
            points[k].setLocation( x[k], y[k] );
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
        Point2D[] points = new Point2D.Double[pointsString.length];
        for ( int i = 0, ni = points.length; i < ni; ++i )
        {
            String[] xyString = StringUtils.split( pointsString[i], ',' );
            double x = Double.parseDouble( xyString[0] );
            double y = Double.parseDouble( xyString[1] );
            points[i] = new Point2D.Double( x, y );
        }
        return new LinearInterpolator( points );
    }
}
