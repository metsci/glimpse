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
package com.metsci.glimpse.examples.plot;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutManagerMig;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.plot.HistogramPainter;
import com.metsci.glimpse.painter.plot.StackedHistogramPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

import it.unimi.dsi.fastutil.floats.Float2IntMap;
import it.unimi.dsi.fastutil.floats.Float2IntOpenHashMap;

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
    public GlimpseLayout getLayout( )
    {
        float[] data1 = generateData1( new float[NUM_POINTS], NUM_POINTS );
        float[] data2 = generateData2( new float[NUM_POINTS], NUM_POINTS );
        float[] data3 = generateData3( new float[NUM_POINTS], NUM_POINTS );

        Axis1D xAxis = new Axis1D( );
        xAxis.setMin( -10 );
        xAxis.setMax( 15 );
        xAxis.setAbsoluteMin( -10 );
        xAxis.setAbsoluteMax( 15 );

        GlimpseLayout histogramPlot1 = createDualHistograms( xAxis, data1, data2, data3 );
        GlimpseLayout histogramPlot2 = createStackedHistograms( xAxis, data1, data2, data3 );
        GlimpseLayout histogramPlot3 = createStackedScaledHistograms( xAxis, data1, data2, data3 );

        GlimpseLayout layout = new GlimpseLayout( );
        layout.addPainter( new BackgroundPainter( true ) );
        ( ( GlimpseLayoutManagerMig ) layout.getLayoutManager( ) ).setLayoutConstraints( "bottomtotop, gapx 0, gapy 0, insets 5 5 5 5" );
        ( ( GlimpseLayoutManagerMig ) layout.getLayoutManager( ) ).setRowConstraints( "[grow|grow|grow]" );
        ( ( GlimpseLayoutManagerMig ) layout.getLayoutManager( ) ).setColumnConstraints( "[grow]" );

        layout.addLayout( histogramPlot1 );
        histogramPlot1.setLayoutData( "grow, wrap" );
        layout.addLayout( histogramPlot2 );
        histogramPlot2.setLayoutData( "grow, wrap" );
        layout.addLayout( histogramPlot3 );
        histogramPlot3.setLayoutData( "grow" );
        layout.invalidateLayout( );

        return layout;
    }

    GlimpseLayout createDualHistograms( Axis1D xAxis, float[] data1, float[] data2, float[] data3 )
    {
        // create a premade histogram plot
        SimplePlot2D histogramplot = new SimplePlot2D( );

        // set axis labels and chart title
        histogramplot.setTitle( "Histogram Plot" );
        histogramplot.setAxisLabelX( "x axis" );
        histogramplot.setAxisLabelY( "frequency" );

        // link the parent x axis
        histogramplot.getAxis( ).getAxisX( ).setParent( xAxis );

        histogramplot.setMinY( 0.0 );
        histogramplot.setMaxY( 0.02 );

        // set the y absolute axis bounds
        // the axis will never be allowed to exceed these values
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

        // create a painter/layer to display the histogram data
        HistogramPainter series1 = new HistogramPainter( );
        HistogramPainter series2 = new HistogramPainter( );
        HistogramPainter series3 = new HistogramPainter( );

        // add data and color information to the first data series painter/layer
        series1.setData( data1 );
        series1.setColor( GlimpseColor.fromColorRgba( 1, 0, 0, 0.6f ) );

        // add data and color information to the second data series painter/layer
        series2.setData( data2 );
        series2.setColor( GlimpseColor.fromColorRgba( 0, 1, 0, 0.6f ) );

        series3.setData( data3 );
        series3.setColor( GlimpseColor.fromColorRgba( 0, 0, 1, 0.6f ) );

        histogramplot.addPainter( series1 );
        histogramplot.addPainter( series2 );
        histogramplot.addPainter( series3 );

        return histogramplot;
    }

    GlimpseLayout createStackedHistograms( Axis1D xAxis, float[] data1, float[] data2, float[] data3 )
    {
        SimplePlot2D histogramplot = new SimplePlot2D( );

        histogramplot.setTitle( "Stacked Histograms" );
        histogramplot.setAxisLabelX( "x axis" );
        histogramplot.setAxisLabelY( "frequency" );

        histogramplot.getAxis( ).getAxisX( ).setParent( xAxis );

        histogramplot.lockMinY( 0 );

        histogramplot.setShowMinorTicksX( true );
        histogramplot.setShowMinorTicksY( true );

        histogramplot.getCrosshairPainter( ).showSelectionBox( false );

        StackedHistogramPainter stacked = new StackedHistogramPainter( );

        stacked.setData( data1, data2, data3 );
        stacked.setSeriesColor( 0, GlimpseColor.fromColorRgba( 1, 0, 0, 0.6f ) );
        stacked.setSeriesColor( 1, GlimpseColor.fromColorRgba( 0, 1, 0, 0.6f ) );
        stacked.setSeriesColor( 2, GlimpseColor.fromColorRgba( 0, 0, 1, 0.6f ) );

        // add the two painters to the plot
        histogramplot.addPainter( stacked );

        stacked.autoAdjustAxisBounds( histogramplot.getAxis( ) );

        return histogramplot;
    }

    GlimpseLayout createStackedScaledHistograms( Axis1D xAxis, float[] data1, float[] data2, float[] data3 )
    {
        SimplePlot2D histogramplot = new SimplePlot2D( );

        histogramplot.setTitle( "Stacked/Scaled Histograms" );
        histogramplot.setAxisLabelX( "x axis" );
        histogramplot.setAxisLabelY( "frequency" );

        histogramplot.getAxis( ).getAxisX( ).setParent( xAxis );

        histogramplot.lockMinY( 0 );

        histogramplot.setShowMinorTicksX( true );
        histogramplot.setShowMinorTicksY( true );

        histogramplot.getCrosshairPainter( ).showSelectionBox( false );

        StackedHistogramPainter stacked = new StackedHistogramPainter( )
        {
            Float2IntMap totalHeights;

            @Override
            public void setData( int totalNumValues, float binSize, Float2IntMap... counts )
            {
                totalHeights = new Float2IntOpenHashMap( );
                totalHeights.defaultReturnValue( 0 );
                for ( Float2IntMap countMap : counts )
                {
                    for ( Float2IntMap.Entry entry : countMap.float2IntEntrySet( ) )
                    {
                        int sum = totalHeights.get( entry.getFloatKey( ) );
                        sum += entry.getIntValue( );
                        totalHeights.put( entry.getFloatKey( ), sum );
                    }
                }

                super.setData( totalNumValues, binSize, counts );
            }

            @Override
            protected float getBarHeight( float bin, int count, int totalValues )
            {
                return count / ( float ) totalHeights.get( bin );
            }
        };

        stacked.setData( data1, data2, data3 );
        stacked.setSeriesColor( 0, GlimpseColor.fromColorRgba( 1, 0, 0, 0.6f ) );
        stacked.setSeriesColor( 1, GlimpseColor.fromColorRgba( 0, 1, 0, 0.6f ) );
        stacked.setSeriesColor( 2, GlimpseColor.fromColorRgba( 0, 0, 1, 0.6f ) );

        // add the two painters to the plot
        histogramplot.addPainter( stacked );

        stacked.autoAdjustAxisBounds( histogramplot.getAxis( ) );

        return histogramplot;
    }

    public static float[] generateData1( float[] data, int size )
    {
        for ( int i = 0; i < size; i++ )
        {
            data[i] = ( float ) ( 5 * Math.random( ) + 10.0f * Math.sin( i * ( 20.0f / Math.PI ) ) );
        }

        return data;
    }

    public static float[] generateData2( float[] data, int size )
    {
        for ( int i = 0; i < size; i++ )
        {
            data[i] = ( float ) ( Math.pow( Math.random( ), 3.0 ) * -20.0 + 10.0 );
        }

        return data;
    }

    public static float[] generateData3( float[] data, int size )
    {
        for ( int i = 0; i < size; i++ )
        {
            data[i] = ( float ) ( Math.pow( 35, Math.random( ) ) - 10 );
        }

        return data;
    }
}
