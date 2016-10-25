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
package com.metsci.glimpse.examples.projection;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.info.CursorTextZPainter;
import com.metsci.glimpse.painter.texture.HeatMapPainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.projection.GenericProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;

/**
 * Demonstrates the ability to provide an array of arbitrary data
 * coordinates to use to position a Glimpse texture. Glimpse also
 * provides a number of specialized texture transformations (such
 * as polar and flat tangent plane).
 *
 * @author ulman
 */
public class GenericProjectionExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new GenericProjectionExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        // create a premade heat map window
        ColorAxisPlot2D plot = new ColorAxisPlot2D( );

        // set axis labels and chart title
        plot.setTitle( "Generic Projection Example" );
        plot.setAxisLabelX( "x axis" );
        plot.setAxisLabelY( "y axis" );

        // set the x, y, and z initial axis bounds
        plot.setMinX( 0.0f );
        plot.setMaxX( 1000.0f );

        plot.setMinY( 0.0f );
        plot.setMaxY( 1000.0f );

        plot.setMinZ( 0.0f );
        plot.setMaxZ( 1000.0f );

        // lock the aspect ratio of the x and y axis to 1 to 1
        plot.lockAspectRatioXY( 1.0f );

        // set the size of the selection box to 100.0 units
        plot.setSelectionSize( 100.0f );

        // generate some data to display
        double[][] data = HeatMapExample.generateData( 1000, 1000 );

        // generate a projection indicating how the data should be mapped to plot coordinates
        // here we use a GenericProjection which simply specifies a grid of vertex coordinates
        // between which the texture data should be linearly interpolated
        // the grid below consists of six points: (-10, 30), (1200, 100), (0, 1000),
        // (1000, 1000), (-100, 1500), and (500, 2500).

        double[][] vertexX = new double[2][3];
        vertexX[0][0] = -10;
        vertexX[1][0] = 1200;
        vertexX[0][1] = 0;
        vertexX[1][1] = 1000;
        vertexX[0][2] = -100;
        vertexX[1][2] = 500;

        double[][] vertexY = new double[2][3];
        vertexY[0][0] = 30;
        vertexY[1][0] = 100;
        vertexY[0][1] = 1000;
        vertexY[1][1] = 2000;
        vertexY[0][2] = 1500;
        vertexY[1][2] = 2500;

        Projection projection = new GenericProjection( vertexX, vertexY );

        // create an OpenGL texture wrapper object
        FloatTextureProjected2D texture = new FloatTextureProjected2D( 1000, 1000 );

        // load the data and projection into the texture
        texture.setProjection( projection );
        texture.setData( data );

        // setup the color map for the painter
        ColorTexture1D colors = new ColorTexture1D( 1024 );
        colors.setColorGradient( ColorGradients.jet );

        // create a painter to display the heatmap data
        HeatMapPainter heatmap = new HeatMapPainter( plot.getAxisZ( ) );

        // add the heatmap data and color scale to the painter
        heatmap.setData( texture );
        heatmap.setColorScale( colors );

        // add the painter to the plot
        plot.addPainter( heatmap );

        // load the color map into the plot (so the color scale is displayed on the z axis)
        plot.setColorScale( colors );

        // add the painter to the plot
        plot.addPainter( heatmap );

        // create a painter which displays the cursor position and data value under the cursor
        CursorTextZPainter cursorPainter = new CursorTextZPainter( );
        plot.addPainter( cursorPainter );

        // tell the cursor painter what texture to report data values from
        cursorPainter.setTexture( texture );

        return plot;
    }
}
