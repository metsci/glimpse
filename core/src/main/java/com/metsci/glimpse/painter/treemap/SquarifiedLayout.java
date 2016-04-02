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
package com.metsci.glimpse.painter.treemap;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;

/**
 * A TreeMap layout which better preserves aspect ratio and provides easier
 * understanding of spatial relationships between nodes.
 *
 * This layout is detailed in Squarified Treemaps, by Bruls, Huizing and van
 * Wijk. Additional information was found on <a
 * href="http://jectbd.com/?p=271">http://jectbd.com/?p=271</a>. The following
 * sources provide pseudo-code only. This custom Java implementation carries the
 * same Metron copyright and license as the rest of the Glimpse source code.
 *
 * @author borkholder
 */
public class SquarifiedLayout implements TreeMapLayout
{
    @Override
    public Rectangle2D[] layout( Rectangle2D boundary, double[] areas, int level )
    {
        // sort by descending size
        int[] order = sortDescending( areas );

        double sumOfAreas = 0;
        for ( int i = 0; i < areas.length; i++ )
        {
            sumOfAreas += areas[i];
        }

        // order and normalize areas
        double[] sorted = new double[areas.length];
        double normalizer = boundary.getWidth( ) * boundary.getHeight( ) / sumOfAreas;
        for ( int i = 0; i < order.length; i++ )
        {
            sorted[i] = areas[order[i]] * normalizer;
        }

        // squarify, or use entire area if just one area
        List<Rectangle2D> rectList = new ArrayList<Rectangle2D>( );
        if ( sorted.length > 1 )
        {
            double shortestSide = min( boundary.getWidth( ), boundary.getHeight( ) );
            squarify( sorted, boundary, shortestSide, rectList );
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

    protected void squarify( double[] sortedSizes, Rectangle2D initialBoundary, double shortestSide, List<Rectangle2D> rects )
    {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double totalArea = 0;
        DoubleList row = new DoubleArrayList( );
        Rectangle2D boundary = initialBoundary;

        int head = 0;
        while ( head < sortedSizes.length )
        {
            double newArea = sortedSizes[head];

            /*
             * If the worst aspect ratio with the new area is better than without, add
             * it to the row and continue. If not, lay out the current row and start a
             * new row.
             */
            boolean isBetter = maxAspectRatio( min, max, totalArea, shortestSide ) >= maxAspectRatio( min, max, totalArea + newArea, shortestSide );
            if ( head == sortedSizes.length - 1 || row.isEmpty( ) || isBetter )
            {
                row.add( newArea );
                min = min( min, newArea );
                max = max( max, newArea );
                totalArea += newArea;

                head++;

                if ( head == sortedSizes.length )
                {
                    layoutRow( boundary, rects, row );
                    // end loop
                }
            }
            else
            {
                boundary = layoutRow( boundary, rects, row );
                shortestSide = min( boundary.getWidth( ), boundary.getHeight( ) );

                row = new DoubleArrayList( );
                min = Double.POSITIVE_INFINITY;
                max = Double.NEGATIVE_INFINITY;
                totalArea = 0;
            }
        }
    }

    protected Rectangle2D layoutRow( Rectangle2D boundary, List<Rectangle2D> rects, DoubleList row )
    {
        double sumOfAreas = 0;
        for ( int i = 0; i < row.size( ); i++ )
        {
            sumOfAreas += row.getDouble( i );
        }

        boolean vertical = boundary.getWidth( ) >= boundary.getHeight( );

        // largest areas are to the top and left
        double x = boundary.getMinX( );
        double y = boundary.getMaxY( );

        double width = sumOfAreas / boundary.getHeight( );
        double height = sumOfAreas / boundary.getWidth( );

        for ( int i = 0; i < row.size( ); i++ )
        {
            double area = row.getDouble( i );
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

        // return remaining unused area
        if ( vertical )
        {
            return new Rectangle2D.Double( boundary.getMinX( ) + width, boundary.getMinY( ), boundary.getWidth( ) - width, boundary.getHeight( ) );
        }
        else
        {
            return new Rectangle2D.Double( boundary.getMinX( ), boundary.getMinY( ), boundary.getWidth( ), boundary.getHeight( ) - height );
        }
    }

    protected double maxAspectRatio( double minArea, double maxArea, double totalArea, double fixedSide )
    {
        double ratio = ( totalArea * totalArea ) / ( fixedSide * fixedSide );

        double worst = max( maxArea / ratio, ratio / minArea );
        return worst;
    }

    /**
     * Returns a map (index to value) from new order to original index. i.e. the
     * returned array contains the old indexes, in the new order. The order is
     * descending by size of the area.
     */
    protected int[] sortDescending( final double[] areas )
    {
        int[] indexes = new int[areas.length];
        for ( int i = 0; i < areas.length; i++ )
        {
            indexes[i] = i;
        }

        IntArrays.quickSort( indexes, new IntComparator( )
        {
            @Override
            public int compare( Integer o1, Integer o2 )
            {
                return compare( o1.intValue( ), o2.intValue( ) );
            }

            @Override
            public int compare( int k1, int k2 )
            {
                return Double.compare( areas[k2], areas[k1] );
            }
        } );

        return indexes;
    }
}
