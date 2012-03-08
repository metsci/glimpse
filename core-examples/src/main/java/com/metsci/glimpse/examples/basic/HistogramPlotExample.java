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
package com.metsci.glimpse.examples.basic;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.plot.HistogramPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * A basic histogram plot.
 *
 * @author ulman
 */
public class HistogramPlotExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new HistogramPlotExample( ) );
    }

    public static int NUM_POINTS = 100000;

    @Override
    public SimplePlot2D getLayout( )
    {
        // create a premade histogram plot
        SimplePlot2D histogramplot = new SimplePlot2D( );

        // set axis labels and chart title
        histogramplot.setTitle( "Histogram Plot Example" );
        histogramplot.setAxisLabelX( "x axis" );
        histogramplot.setAxisLabelY( "frequency" );

        // set the x, y initial axis bounds
        histogramplot.setMinX( -10.0 );
        histogramplot.setMaxX( 15.0 );

        histogramplot.setMinY( 0.0 );
        histogramplot.setMaxY( 0.02 );

        // set the x, y absolute axis bounds
        // the axis will never be allowed to exceed these values
        histogramplot.setAbsoluteMinX( -10.0 );
        histogramplot.setAbsoluteMaxX( 15.0 );

        histogramplot.setAbsoluteMinY( 0.0 );
        histogramplot.setAbsoluteMaxY( 1.0 );

        // lock the minimum value of the y axis
        // for a histogram we always want 0 at the
        // bottom of the screen
        histogramplot.lockMinY( 0.0 );

        // don't show the square selection box, only the x and y crosshairs
        histogramplot.getCrosshairPainter( ).showSelectionBox( false );

        // show minor tick marks on all the plot axes
        histogramplot.setShowMinorTicksX( true );
        histogramplot.setShowMinorTicksY( true );

        // add two data series to the plot
        final float[] data = new float[NUM_POINTS];

        // create a painter/layer to display the histogram data
        HistogramPainter series1 = new HistogramPainter( );
        HistogramPainter series2 = new HistogramPainter( );

        // add data and color information to the first data series painter/layer
        series1.setData( generateData1( data, NUM_POINTS ) );
        series1.setColor( GlimpseColor.fromColorRgba( 1.0f, 0.0f, 0.0f, 0.6f ) );

        // add data and color information to the second data series painter/layer
        series2.setData( generateData2( data, NUM_POINTS ) );
        series2.setColor( GlimpseColor.fromColorRgba( 0.0f, 1.0f, 0.0f, 0.6f ) );

        // add the two painters to the plot
        histogramplot.addPainter( series1 );
        histogramplot.addPainter( series2 );

        return histogramplot;
    }

    public static float[] generateData1( float[] data, int size )
    {
        for ( int i = 0; i < size; i++ )
            data[i] = ( float ) ( 5 * Math.random( ) + 10.0f * Math.sin( i * ( 20.0f / Math.PI ) ) );

        return data;
    }

    public static float[] generateData2( float[] data, int size )
    {
        for ( int i = 0; i < size; i++ )
            data[i] = ( float ) ( Math.pow( Math.random( ), 3.0 ) * -20.0 + 10.0 );

        return data;
    }
}
