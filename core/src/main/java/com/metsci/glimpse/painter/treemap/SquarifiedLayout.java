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
package com.metsci.glimpse.painter.treemap;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A TreeMap layout which better preserves aspect ratio and provides easier
 * understanding of spatial relationships between nodes.
 * 
 * This layout is detailed in Squarified Treemaps, by Bruls, Huizing and van
 * Wijk. Additional information was found on <a
 * href="http://jectbd.com/?p=271">http://jectbd.com/?p=271</a>. The following
 * sources provide pseudo-code only. This custom Java implementation carries
 * the same Metron copyright and license as the rest of the Glimpse source code. 
 * 
 * @author borkholder
 */
public class SquarifiedLayout implements TreeMapLayout
{
    @Override
    public Rectangle2D[] layout( Rectangle2D boundary, double[] areas, int level )
    {
        // sort by descending size
        int[] order = sort( areas );

        double sumOfAreas = 0;
        for ( double a : areas )
        {
            sumOfAreas += a;
        }

        // order and normalize areas
        double[] sorted = new double[areas.length];
        for ( int i = 0; i < order.length; i++ )
        {
            sorted[i] = areas[order[i]] / sumOfAreas * boundary.getWidth( ) * boundary.getHeight( );
        }

        // squarify, or use entire area if just one area
        List<Rectangle2D> rectList = new ArrayList<Rectangle2D>( );
        if ( sorted.length > 1 )
        {
            double shortestSide = Math.min( boundary.getWidth( ), boundary.getHeight( ) );
            squarify( 0, sorted, boundary, new ArrayList<Double>( ), shortestSide, rectList );
        }
        else
        {
            rectList.add( boundary );
        }

        // put them back in the original order
        Rectangle2D[] rects = new Rectangle2D[areas.length];
        for ( int i = 0; i < rectList.size( ); i++ )
        {
            rects[order[i]] = rectList.get( i );
        }

        return rects;
    }

    protected void squarify( int head, double[] sortedSizes, Rectangle2D boundary, List<Double> row, double shortestSide, List<Rectangle2D> rects )
    {

        /*
         * If the worst aspect ratio with the new area is better than without, add
         * it to the row and continue. If not, lay out the current row and start a
         * new row.
         */
        if ( row.isEmpty( ) || worst( row, null, shortestSide ) >= worst( row, sortedSizes[head], shortestSide ) )
        {
            if ( head == sortedSizes.length - 1 )
            {
                row.add( sortedSizes[head] );
                layoutRow( boundary, rects, row );
            }
            else
            {
                row.add( sortedSizes[head] );
                squarify( head + 1, sortedSizes, boundary, row, shortestSide, rects );
            }
        }
        else
        {
            Rectangle2D newBoundary = layoutRow( boundary, rects, row );
            shortestSide = Math.min( newBoundary.getWidth( ), newBoundary.getHeight( ) );
            squarify( head, sortedSizes, newBoundary, new ArrayList<Double>( ), shortestSide, rects );
        }
    }

    protected Rectangle2D layoutRow( Rectangle2D boundary, List<Rectangle2D> rects, List<Double> row )
    {
        double sumOfAreas = 0;
        for ( double a : row )
        {
            sumOfAreas += a;
        }

        // do layout vertically
        boolean vertical = boundary.getWidth( ) >= boundary.getHeight( );

        double x = boundary.getMinX( );
        double y = boundary.getMaxY( );
        double width = sumOfAreas / boundary.getHeight( );
        double height = sumOfAreas / boundary.getWidth( );

        for ( double area : row )
        {
            if ( vertical )
            {
                height = area / width;
            }
            else
            {
                width = area / height;
            }

            Rectangle2D rect = new Rectangle2D.Double( x, y - height, width, height );
            rects.add( rect );

            if ( vertical )
            {
                y -= height;
            }
            else
            {
                x += width;
            }
        }

        if ( vertical )
        {
            return new Rectangle2D.Double( boundary.getMinX( ) + width, boundary.getMinY( ), boundary.getWidth( ) - width, boundary.getHeight( ) );
        }
        else
        {
            return new Rectangle2D.Double( boundary.getMinX( ), boundary.getMinY( ), boundary.getWidth( ), boundary.getHeight( ) - height );
        }
    }

    protected double worst( List<Double> areas, Double newArea, double shortestSide )
    {
        double minArea = Double.POSITIVE_INFINITY;
        double maxArea = Double.NEGATIVE_INFINITY;
        double totalArea = 0;

        for ( double a : areas )
        {
            minArea = min( a, minArea );
            maxArea = max( a, maxArea );
            totalArea += a;
        }

        if ( newArea != null )
        {
            minArea = min( newArea, minArea );
            maxArea = max( newArea, maxArea );
            totalArea += newArea;
        }

        double worst = max( ( maxArea * shortestSide * shortestSide ) / ( totalArea * totalArea ), ( totalArea * totalArea ) / ( minArea * shortestSide * shortestSide ) );

        return worst;
    }

    /**
     * Returns a map (index to value) from new order to original index. i.e. the
     * returned array contains the old indexes, in the new order. The order is
     * descending by size of the area.
     */
    protected int[] sort( final double[] areas )
    {
        List<Integer> list = new ArrayList<Integer>( areas.length );
        for ( int i = 0; i < areas.length; i++ )
        {
            list.add( i );
        }

        Collections.sort( list, new Comparator<Integer>( )
        {
            @Override
            public int compare( Integer o1, Integer o2 )
            {
                return Double.compare( areas[o2], areas[o1] );
            }
        } );

        int[] indexes = new int[list.size( )];
        for ( int i = 0; i < list.size( ); i++ )
        {
            indexes[i] = list.get( i );
        }

        return indexes;
    }
}
