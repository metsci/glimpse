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
package com.metsci.glimpse.examples.shape;

import java.util.Random;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.RateLimitedAxisListener1D;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.CopyrightPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.painter.shape.PolygonPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.colormap.ColorGradient;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LineStyle;

/**
 * Demonstrates usage of Polygon painter to display arbitrary (convex and concave) polygons.
 *
 * @author ulman
 */
public class PolygonPainterExample implements GlimpseLayoutProvider
{
    public static void main( String args[] ) throws Exception
    {
        Example.showWithSwing( new PolygonPainterExample( ) );
    }

    @Override
    public SimplePlot2D getLayout( )
    {
        // create a premade geoplot
        final SimplePlot2D polyplot = new SimplePlot2D( );

        polyplot.setTitle( "Polygon Example" );
        polyplot.setAxisLabelX( "easting", "meters", false );
        polyplot.setAxisLabelY( "northing", "meters", false );
        polyplot.setAxisLabelZ( "time", "hours" );

        polyplot.setAxisSizeZ( 65 );

        polyplot.setMinX( -20.0f );
        polyplot.setMaxX( 20.0f );

        polyplot.setMinY( -20.0f );
        polyplot.setMaxY( 20.0f );

        polyplot.setMinZ( 0.0f );
        polyplot.setMaxZ( 1000.0f );

        //polyplot.lockAspectRatioXY( 1.0f );

        polyplot.setSelectionSize( 50000.0f );

        polyplot.addPainter( new CopyrightPainter( ) );
        polyplot.addPainter( new FpsPainter( ) );
        final PolygonPainter polygonPainter = new PolygonPainter( );
        polyplot.addPainter( polygonPainter );

        for ( int i = 0; i < 100; i++ )
        {
            polygonPainter.addPolygon( 1, i, i, i, new float[] { 0f, i, i, 0f }, new float[] { 0f, 0f, i, i }, i );
        }

        for ( int i = 0; i < 100; i++ )
        {
            polygonPainter.addPolygon( 2, i, i, i, new float[] { 0f, -i, -i, 0f }, new float[] { 0f, 0f, -i, -i }, i + 100 );
        }

        polygonPainter.addPolygon( 2, 0, new float[] { 1000, 2000, 2000, 1000 }, new float[] { 1000, 1000, 2000, 2000 }, 0 );

        LineStyle style = new LineStyle( );
        style.joinType = LineJoinType.JOIN_MITER;

        polygonPainter.setLineStyle( 1, style );
        polygonPainter.setLineColor( 1, GlimpseColor.getYellow( ) );

        polygonPainter.addPolygon( 4, 1, new float[] { -10f, 0, 10, 10, 0f }, new float[] { -10f, 8, 0f, 10, 10 }, 200 );

        polygonPainter.setLineWidth( 1, 3 );

        polygonPainter.setLineColor( 2, 0.0f, 0.0f, 1.0f, 1.0f );
        polygonPainter.setLineDotted( 2, true );

        polygonPainter.setFill( 3, true );
        polygonPainter.setLineWidth( 3, 3 );
        polygonPainter.setFillColor( 3, 0.0f, 1.0f, 0.0f, 1.0f );

        polygonPainter.setFill( 4, true );

        polygonPainter.setLineColor( 6, 1.0f, 0.0f, 0.0f, 1.0f );
        polygonPainter.setFill( 6, false );

        polygonPainter.setPolyDotted( 4, true );
        polygonPainter.setPolyDotted( 3, true );

        polygonPainter.setShowLines( 3, false );
        polygonPainter.setShowLines( 4, false );
        polygonPainter.setShowLines( 5, false );

        ( new Thread( )
        {
            @Override
            public void run( )
            {
                int i = 0;

                while ( true )
                {
                    polygonPainter.addPolygon( 6, i, i, i + 20, new float[] { 0f, -i, -i, 0f }, new float[] { 0f, 0f, i, i }, 100 );

                    int deleteId = ( int ) ( Math.random( ) * i );
                    polygonPainter.deletePolygon( 6, deleteId );

                    i++;

                    try
                    {
                        Thread.sleep( 100 );
                    }
                    catch ( InterruptedException e )
                    {
                        e.printStackTrace( );
                    }
                }
            }
        } ).start( );

        ( new Thread( )
        {
            int i = 5;
            ColorGradient c = ColorGradients.jet;
            float[] rgba = new float[4];
            Random r = new Random( );

            @Override
            public void run( )
            {
                while ( true )
                {
                    float x0 = 200 * r.nextFloat( );
                    float y0 = 200 * r.nextFloat( );
                    float t = ( float ) ( r.nextFloat( ) * Math.PI * 2 );
                    float d = 15;
                    float dt = ( float ) ( Math.PI / 36 );

                    float x1 = ( float ) ( x0 + d * Math.sin( t - dt ) );
                    float y1 = ( float ) ( y0 + d * Math.cos( t - dt ) );
                    float x2 = ( float ) ( x0 + d * Math.sin( t + dt ) );
                    float y2 = ( float ) ( y0 + d * Math.cos( t + dt ) );

                    if ( i % 2 == 0 )
                    {
                        polygonPainter.addPolygon( 5, i, i, i + 1000, new float[] { x0, x1, x2 }, new float[] { y0, y1, y2 }, 200 + i );
                    }
                    else
                    {
                        polygonPainter.addPolygon( 5 + i, 0, i, i + 1000, new float[] { x0, x1, x2 }, new float[] { y0, y1, y2 }, 200 + i );
                    }

                    polygonPainter.setFill( 5 + i, true );
                    polygonPainter.setShowLines( 5 + i, false );
                    c.toColor( ( i % 50 ) / 50f, rgba );

                    polygonPainter.setFillColor( 5 + i, rgba[0], rgba[1], rgba[2], r.nextFloat( ) );

                    polygonPainter.setFillColor( 5, rgba[0], rgba[1], rgba[2], 0.2f );
                    polygonPainter.setLineColor( 5, rgba[0], rgba[1], rgba[2], 1.0f );

                    i = i + 1;

                    try
                    {
                        if ( i < 2000 )
                        {
                            Thread.sleep( 1 );
                        }
                        else
                        {
                            Thread.sleep( 100 );
                        }
                    }
                    catch ( InterruptedException e )
                    {
                        e.printStackTrace( );
                    }
                }
            }
        } ).start( );

        polyplot.getAxisZ( ).addAxisListener( new RateLimitedAxisListener1D( )
        {
            double prevMinTime = -1;
            double prevMaxTime = -1;

            @Override
            public void axisUpdatedRateLimited( Axis1D handler )
            {
                double minTime = handler.getMin( );
                double maxTime = handler.getMax( );

                if ( prevMinTime != minTime || prevMaxTime != maxTime )
                {
                    polygonPainter.displayTimeRange( minTime, maxTime );

                    prevMinTime = minTime;
                    prevMaxTime = maxTime;
                }
            }
        } );

        return polyplot;
    }
}
