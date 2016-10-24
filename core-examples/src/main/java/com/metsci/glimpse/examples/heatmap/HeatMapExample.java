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
package com.metsci.glimpse.examples.heatmap;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.info.CursorTextZPainter;
import com.metsci.glimpse.painter.texture.HeatMapPainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;

/**
 * A square heat map plot with adjustable color scale. Uses GLSL shaders
 * and textures to provide dynamic adjustment of mapping from data value
 * to color.
 *
 * @author ulman
 */
public class HeatMapExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new HeatMapExample( ) );
    }

    protected HeatMapPainter heatmapPainter;
    protected CursorTextZPainter cursorPainter;

    @Override
    public ColorAxisPlot2D getLayout( )
    {
        // create a premade heat map window
        ColorAxisPlot2D plot = newPlot( );

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

        // create a heat map painter
        heatmapPainter = newHeatMapPainter( plot.getAxisZ( ) );

        // add the painter to the plot
        plot.addPainter( heatmapPainter );

        // load the color map into the plot (so the color scale is displayed on the z axis)
        plot.setColorScale( heatmapPainter.getColorScale( ) );

        // create a painter which displays the cursor position and data value under the cursor
        // add it to the foreground layer so that it draws on top of the plot data
        // this is equivalent to: plot.addPainter( cursorPainter, Plot2D.FOREGROUND_LAYER )
        cursorPainter = new CursorTextZPainter( );
        plot.addPainterForeground( cursorPainter );

        // tell the cursor painter what texture to report data values from
        cursorPainter.setTexture( heatmapPainter.getData( ) );

        return plot;
    }

    protected ColorAxisPlot2D newPlot( )
    {
        return new ColorAxisPlot2D( );
    }

    public CursorTextZPainter getCursorPainter( )
    {
        return cursorPainter;
    }

    public HeatMapPainter getPainter( )
    {
        return heatmapPainter;
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

    public static HeatMapPainter newHeatMapPainter( Axis1D axis )
    {
        return newHeatMapPainter( newColorTexture( ), axis );
    }

    public static HeatMapPainter newHeatMapPainter( ColorTexture1D colorScale, Axis1D axis )
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
