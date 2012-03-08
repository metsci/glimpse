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
package com.metsci.glimpse.charts.vector.display.examplesupport;

import java.awt.Dimension;

/**
 * Simple util to keep track of a bounding box.  
 * 
 * Probably should switch caller to com.metsci.glimpse.util.math.stat.StatCollectorNDim.
 * 
 * @author Cunningham 
 */
public class BoundingBox
{
    private double minX = Double.MAX_VALUE;
    private double maxX = Double.NEGATIVE_INFINITY;
    private double minY = Double.MAX_VALUE;
    private double maxY = Double.NEGATIVE_INFINITY;

    public BoundingBox( )
    {
    }

    public BoundingBox( Dimension d )
    {
        this( d.getWidth( ), d.getHeight( ) );
    }

    public BoundingBox( double width, double height )
    {
        minX = 0;
        minY = 0;
        maxX = width;
        maxY = height;
    }

    public BoundingBox( double minX, double maxX, double minY, double maxY )
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public double getMaxX( )
    {
        return maxX;
    }

    public double getMinX( )
    {
        return minX;
    }

    public double getMaxY( )
    {
        return maxY;
    }

    public double getMinY( )
    {
        return minY;
    }

    public boolean isEmpty( )
    {
        return minX == Double.MAX_VALUE || minY == Double.MIN_VALUE;
    }

    public void debug( )
    {
        assert ( maxX >= minX );
        System.out.println( maxX + " >= " + minX );
        assert ( maxY >= minY );
        System.out.println( maxY + " >= " + minY );
    }

    public void applyPoint( double x, double y )
    {
        if ( x < minX )
        {
            minX = x;
        }
        if ( x > maxX )
        {
            maxX = x;
        }
        if ( y < minY )
        {
            minY = y;
        }
        if ( y > maxY )
        {
            maxY = y;
        }
    }

    public boolean contains( double otherX, double otherY )
    {
        if ( otherX < minX ) return false;
        if ( otherX > maxX ) return false;
        if ( otherY < minY ) return false;
        if ( otherY > maxY ) return false;

        return true;
    }

    public boolean contains( BoundingBox otherBox )
    {
        if ( otherBox.minX < minX ) return false;

        if ( otherBox.minY < minY ) return false;

        if ( otherBox.maxX > maxX ) return false;

        if ( otherBox.maxY > maxY ) return false;

        return true;
    }

    public double transformX( BoundingBox fromSpace, double x )
    {
        double myWidth = maxX - minX;
        double fromWidth = fromSpace.maxX - fromSpace.minX;

        double rval = ( ( x - fromSpace.minX ) * myWidth / fromWidth ) + minX;
        //		System.out.println("# x = " + x + "; fromWidth: " + fromWidth + "; myWidth: " + myWidth +
        //				"; rval: " + rval + "; (x - fromSpace.minX): " + (x -fromSpace.minX)
        //				);
        return rval;
        //return ((x - fromSpace.minX) * myWidth/ fromWidth) + minX;
    }

    public double transformY( BoundingBox fromSpace, double y )
    {
        double myHeight = maxY - minY;
        double fromHeight = fromSpace.maxY - fromSpace.minY;

        //double adjY = ((y - minY) * (panelSize.getHeight()-10) / yWidth) + 5;
        return ( ( y - fromSpace.minY ) * myHeight / fromHeight ) + minY;
    }

    public String toString( )
    {
        return "[box] minX: " + minX + "; maxX: " + maxX + "; minY: " + minY + "; maxY: " + maxY;
    }

}
