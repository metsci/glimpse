/*
 * Copyright (c) 2019, Metron, Inc.
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

import static com.metsci.glimpse.support.QuickUtils.quickGlimpseApp;
import static javax.media.opengl.GLProfile.GL3bc;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.axis.UpdateMode;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.painter.texture.HeatMapPainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.QuickUtils.QuickGlimpseApp;
import com.metsci.glimpse.support.colormap.ColorGradient;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;

/**
 * Demonstrates Glimpse's axis linking capability by creating
 * two independent Glimpse plots and linking their axes so that they move together.
 *
 * @author ulman
 */
public class LinkedHeatMapExample
{
    public static void main( String[] args ) throws Exception
    {
        SwingUtilities.invokeLater( ( ) ->
        {
            // create two heat map plots
            ColorAxisPlot2D leftPlot = newPlot( ColorGradients.reverseBone );
            ColorAxisPlot2D rightPlot = newPlot( ColorGradients.jet );

            // link the x, y, and z axis of the two heat maps by creating a parent/child
            // relationship between them. Any changes to the parent or child will propagate
            // to all ancestors and siblings
            rightPlot.getAxis( ).setParent( leftPlot.getAxis( ) );

            // create a window and show the left plot
            QuickGlimpseApp leftApp = quickGlimpseApp( "Linked Heat Map Example", GL3bc, 800, 800, leftPlot );
            // create a window and show the right plot
            QuickGlimpseApp rightApp = quickGlimpseApp( "Linked Heat Map Example", GL3bc, 800, 800, rightPlot );

            // place the windows side by side
            rightApp.getFrame( ).setLocation( 800, 0 );
            leftApp.getFrame( ).setLocation( 0, 0 );
        } );
    }

    public static ColorAxisPlot2D newPlot( ColorGradient gradient )
    {
        // create a premade lineplot
        ColorAxisPlot2D plot = new ColorAxisPlot2D( );

        plot.setTitle( "Linked Heat Map Example" );
        plot.setAxisLabelX( "x axis" );
        plot.setAxisLabelY( "y axis" );

        plot.setUpdateModeXY( UpdateMode.CenterScale );

        plot.setMinX( 0.0f );
        plot.setMaxX( 1000.0f );

        plot.setMinY( 0.0f );
        plot.setMaxY( 1000.0f );

        plot.setMinZ( 0.0f );
        plot.setMaxZ( 1000.0f );

        plot.setAbsoluteMinZ( -1000.0f );
        plot.setAbsoluteMaxZ( 2000.0f );

        plot.setSelectionSize( 100.0f );

        // setup the color-map for the painter
        ColorTexture1D colorScale = new ColorTexture1D( 1024 );
        colorScale.setColorGradient( gradient );

        // load the color-map into the plot (so the color scale is displayed on the z axis)
        plot.setColorScale( colorScale );

        // create a painter to display the texture in the plot
        HeatMapPainter painter = new HeatMapPainter( plot.getAxisZ( ) );

        // load the color-map and texture into the painter
        painter.setColorScale( colorScale );
        FloatTextureProjected2D texture = createTextureData( );
        painter.setData( texture );

        // set the color scale on the plot z axis
        plot.setColorScale( colorScale );

        // add the painter to the frame
        plot.addPainter( painter );

        return plot;
    }

    protected static FloatTextureProjected2D createTextureData( )
    {
        // generate a projection indicating how the data should be mapped to plot coordinates
        Projection projection = new FlatProjection( 0, 1000, 0, 1000 );

        // create an OpenGL texture wrapper object
        FloatTextureProjected2D texture = new FloatTextureProjected2D( 1000, 1000 );

        // load the data and projection into the texture
        texture.setProjection( projection );
        texture.setData( HeatMapExample.generateData( 1000, 1000 ) );

        return texture;
    }
}
