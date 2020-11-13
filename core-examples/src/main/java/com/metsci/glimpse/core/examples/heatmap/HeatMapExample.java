/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.examples.heatmap;

import static com.jogamp.opengl.GLProfile.GL3;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.core.support.QuickUtils.swingInvokeLater;

import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.gl.texture.ColorTexture1D;
import com.metsci.glimpse.core.painter.info.CursorTextZPainter;
import com.metsci.glimpse.core.painter.texture.HeatMapPainter;
import com.metsci.glimpse.core.plot.ColorAxisPlot2D;
import com.metsci.glimpse.core.support.colormap.ColorGradients;
import com.metsci.glimpse.core.support.projection.FlatProjection;
import com.metsci.glimpse.core.support.projection.Projection;
import com.metsci.glimpse.core.support.texture.FloatTextureProjected2D;

/**
 * A square heat map plot with adjustable color scale. Uses GLSL shaders
 * and textures to provide dynamic adjustment of mapping from data value
 * to color.
 *
 * @author ulman
 */
public class HeatMapExample
{
    public static void main( String[] args )
    {
        swingInvokeLater( ( ) ->
        {
            // create a window and show the plot
            quickGlimpseApp( "Heat Map Example", GL3, newHeatMapPlot( ) );
        } );
    }

    public static ColorAxisPlot2D newHeatMapPlot( )
    {
        // create a plot to display the heat map
        ColorAxisPlot2D plot = newEmptyPlot( );

        // create a heat map painter
        HeatMapPainter heatmapPainter = newPainter( plot.getAxisZ( ) );

        // add the painter to the plot
        plot.addPainter( heatmapPainter );

        // load the color map into the plot (so the color scale is displayed on the z axis)
        plot.setColorScale( heatmapPainter.getColorScale( ) );

        // create a painter which displays the cursor position and data value under the cursor
        // add it to the foreground layer so that it draws on top of the plot data
        // this is equivalent to: plot.addPainter( cursorPainter, Plot2D.FOREGROUND_LAYER )
        CursorTextZPainter cursorPainter = new CursorTextZPainter( );
        plot.addPainterForeground( cursorPainter );

        // tell the cursor painter what texture to report data values from
        cursorPainter.setTexture( heatmapPainter.getData( ) );

        return plot;
    }

    public static ColorAxisPlot2D newEmptyPlot( )
    {
        return customizePlot( new ColorAxisPlot2D( ) );
    }

    public static ColorAxisPlot2D customizePlot( ColorAxisPlot2D plot )
    {
        // set axis labels and chart title
        plot.setTitle( "Heat Map Example" );
        plot.setAxisLabelX( "x axis" );
        plot.setAxisLabelY( "y axis" );

        // set border and offset sizes in pixels
        plot.setBorderSize( 30 );
        plot.setAxisSizeX( 40 );
        plot.setAxisSizeY( 60 );

        // set the x, y, and z initial axis bounds
        plot.setMinX( 0.0f );
        plot.setMaxX( 1000.0f );

        plot.setMinY( 0.0f );
        plot.setMaxY( 1000.0f );

        plot.setMinZ( 0.0f );
        plot.setMaxZ( 1000.0f );

        // set the size of the selection box to 100.0 units
        plot.setSelectionSize( 100.0f );

        // show minor tick marks on all the plot axes
        plot.setShowMinorTicks( true );
        plot.setMinorTickCount( 9 );

        return plot;
    }

    public static double[][] generateData( int sizeX, int sizeY )
    {
        double[][] data = new double[sizeX][sizeY];

        for ( int x = 0; x < sizeX; x++ )
        {
            for ( int y = 0; y < sizeY; y++ )
            {
                data[x][y] = Math.random( ) * 100 + ( x * y ) / 1000d;
            }
        }

        return data;
    }

    public static ColorTexture1D newColorTexture( )
    {
        // setup the color map for the painter
        ColorTexture1D colors = new ColorTexture1D( 1024 );
        colors.setColorGradient( ColorGradients.accent );

        return colors;
    }

    public static HeatMapPainter newPainter( Axis1D axis )
    {
        return newPainter( newColorTexture( ), axis );
    }

    public static HeatMapPainter newPainter( ColorTexture1D colorScale, Axis1D axis )
    {
        // generate some data to display
        double[][] data = generateData( 1000, 1000 );

        // generate a projection indicating how the data should be mapped to plot coordinates
        Projection projection = new FlatProjection( 0, 1000, 0, 1000 );

        // create an OpenGL texture wrapper object
        FloatTextureProjected2D texture = new FloatTextureProjected2D( 1000, 1000 );

        // load the data and projection into the texture
        texture.setProjection( projection );
        texture.setData( data );

        // create a painter to display the heatmap data
        HeatMapPainter heatmapPainter = new HeatMapPainter( axis );

        // add the heatmap data and color scale to the painter
        heatmapPainter.setData( texture );
        heatmapPainter.setColorScale( colorScale );

        return heatmapPainter;
    }
}
