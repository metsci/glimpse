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
package com.metsci.glimpse.examples.line;

import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.NumericRightYAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.LegendPainter.BlockLegendPainter;
import com.metsci.glimpse.painter.decoration.LegendPainter.LegendPlacement;
import com.metsci.glimpse.painter.info.CursorTextPainter;
import com.metsci.glimpse.painter.plot.XYLinePainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LineStyle;

/**
 * A basic x/y line plot with a simple legend.
 *
 * @author ulman
 */
public class LinePlotExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new LinePlotExample( ) );
    }

    public static int NUM_POINTS = 100;

    @Override
    public SimplePlot2D getLayout( )
    {
        // create a plot frame
        SimplePlot2D plot = new SimplePlot2D( )
        {
            // paint the z axis with a custom painter which places tick marks on the left hand side
            @Override
            protected NumericAxisPainter createAxisPainterZ( AxisLabelHandler tickHandler )
            {
                return new NumericRightYAxisPainter( tickHandler );
            }
        };

        GlimpseAxisLayout2D layout = new GlimpseAxisLayout2D( plot.getAxisX( ), plot.getAxisZ( ) );
        layout.setEventConsumer( false );
        plot.getLayoutCenter( ).addLayout( layout );

        AxisMouseListener listener = new AxisMouseListener2D( );
        layout.addGlimpseMouseAllListener( listener );

        // set the size of the custom z axis (which acts as the Y axis
        // for the second data series)
        plot.setAxisSizeZ( 40 );

        // customize the pixel sizes of the y axis and borders
        plot.setBorderSize( 15 );
        plot.setAxisSizeY( 40 );

        // set axis labels and chart title
        plot.setTitle( "Line Plot Example" );
        plot.setAxisLabelX( "x axis" );
        plot.setAxisLabelY( "data series 1" );
        plot.setAxisLabelZ( "data series 2" );

        // set the x, y initial axis bounds
        plot.setMinX( 0.0 );
        plot.setMaxX( 100.0 );

        plot.setMinY( 0.0 );
        plot.setMaxY( 10.0 );

        // don't show the square selection box, only the x and y crosshairs
        plot.getCrosshairPainter( ).showSelectionBox( false );

        // creating a data series painter, passing it the lineplot frame
        // this constructor will have the painter draw according to the lineplot x and y axis
        XYLinePainter series1 = createXYLinePainter1( );
        plot.addPainter( series1 );

        // in order for our second data series to use the right hand
        // axis as its y axis, we must manually specify the axes which it should use
        XYLinePainter series2 = createXYLinePainter2( );
        layout.addPainter( series2 );

        // add a painter to display the x and y position of the cursor
        CursorTextPainter cursorPainter = new CursorTextPainter( );
        plot.addPainter( cursorPainter );

        // don't offset the text by the size of the selection box, since we aren't showing it
        cursorPainter.setOffsetBySelectionSize( false );

        BlockLegendPainter legend = new BlockLegendPainter( LegendPlacement.SE );

        //Move the legend further away from the right side;
        legend.setOffsetY( 10 );
        legend.setOffsetX( 100 );
        legend.addItem( "Series 1", GlimpseColor.fromColorRgba( 1.0f, 0.0f, 0.0f, 0.8f ) );
        legend.addItem( "Series 2", GlimpseColor.fromColorRgba( 0.0f, 0.0f, 1.0f, 0.8f ) );
//        legend.setLineStipple( "Series 2", 1, ( short ) 0x00FF );

        //make the lines in the legend slightly longer
        legend.setLegendItemWidth( 60 );

        // add the legend painter to the top of the center GlimpseLayout
        plot.addPainter( legend );

        return plot;
    }

    public static XYLinePainter createXYLinePainter1( )
    {
        // add two data series to the plot
        float[] dataX = new float[NUM_POINTS];
        float[] dataY = new float[NUM_POINTS];

        XYLinePainter series1 = new XYLinePainter( );
        generateData1( dataX, dataY, NUM_POINTS );
        series1.setData( dataX, dataY, GlimpseColor.fromColorRgba( 1.0f, 0.0f, 0.0f, 1.0f ) );
        
        LineStyle style = new LineStyle( );
        style.thickness_PX = 3.5f;
        style.joinType = LineJoinType.JOIN_BEVEL;
        
        series1.setLineStyle( style );
        series1.showPoints( true );
        series1.setPointSize( 8f );
        series1.setPointFeather( 4.0f );

        return series1;
    }

    public static XYLinePainter createXYLinePainter2( )
    {
        // add two data series to the plot
        float[] dataX = new float[NUM_POINTS];
        float[] dataY = new float[NUM_POINTS];

        XYLinePainter series2 = new XYLinePainter( );

        // setting series2's data and display settings works the same way
        generateData2( dataX, dataY, NUM_POINTS );
        series2.setData( dataX, dataY, GlimpseColor.fromColorRgba( 0.0f, 0.0f, 1.0f, 0.8f ) );
        series2.setLineThickness( 1.5f );
        series2.showPoints( false );
        series2.setLineStipple( true );
        series2.setLineStipple( 1, (short) 0xFF00 );

        return series2;
    }

    public static void generateData1( float[] dataX, float[] dataY, int size )
    {
        for ( int i = 0; i < size; i++ )
        {
            float x = i;

            dataX[i] = x;
            dataY[i] = ( float ) ( Math.random( ) * 0.4 - 0.2 + Math.sqrt( x ) );
        }
    }

    public static void generateData2( float[] dataX, float[] dataY, int size )
    {
        for ( int i = 0; i < size; i++ )
        {
            float x = i;

            dataX[i] = x;
            dataY[i] = ( float ) Math.sqrt( x );
        }
    }
}
