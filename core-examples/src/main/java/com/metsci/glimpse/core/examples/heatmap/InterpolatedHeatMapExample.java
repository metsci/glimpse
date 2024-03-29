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
import static com.metsci.glimpse.core.examples.heatmap.HeatMapExample.generateData;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.core.support.QuickUtils.swingInvokeLater;

import java.io.IOException;

import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.gl.texture.ColorTexture1D;
import com.metsci.glimpse.core.painter.texture.HeatMapPainter;
import com.metsci.glimpse.core.plot.ColorAxisPlot2D;
import com.metsci.glimpse.core.support.colormap.ColorGradient;
import com.metsci.glimpse.core.support.projection.FlatProjection;
import com.metsci.glimpse.core.support.projection.Projection;
import com.metsci.glimpse.core.support.shader.colormap.InterpColorMapProgram;
import com.metsci.glimpse.core.support.texture.FloatTextureProjected2D;

/**
 * A square heat map plot with adjustable color scale. Uses GLSL shaders
 * and textures to provide dynamic adjustment of mapping from data value
 * to color.
 *
 * @author borkholder
 */
public class InterpolatedHeatMapExample
{
    public static void main( String[] args )
    {
        swingInvokeLater( ( ) ->
        {
            // create a plot to display the heat map
            ColorAxisPlot2D plot = HeatMapExample.newEmptyPlot( );

            // create a heat map painter
            HeatMapPainter heatmapPainter = newHeatMapPainter( plot.getAxisZ( ) );

            // add the painter to the plot
            plot.addPainter( heatmapPainter );

            // load the color map into the plot (so the color scale is displayed on the z axis)
            plot.setColorScale( heatmapPainter.getColorScale( ) );

            // create a window and show the plot
            quickGlimpseApp( "Interpolated Heat Map Example", GL3, plot );
        } );
    }

    public static ColorTexture1D newContainmentColorTexture( )
    {
        ColorGradient containmentMap = new ColorGradient( )
        {
            @Override
            public void toColor( float fraction, float[] rgba )
            {
                if ( fraction <= 0.66 )
                {
                    rgba[0] = 1;
                    rgba[1] = 0.7f;
                    rgba[2] = 0.7f;
                    rgba[3] = 0.5f;
                }
                else if ( fraction <= 0.85 )
                {
                    rgba[0] = 1f;
                    rgba[1] = 1f;
                    rgba[2] = .7f;
                    rgba[3] = 0.5f;
                }
                else if ( fraction <= 0.99 )
                {
                    rgba[0] = 0.3f;
                    rgba[1] = 0.3f;
                    rgba[2] = 1f;
                    rgba[3] = 0.2f;
                }
                else
                {
                    rgba[0] = 0f;
                    rgba[1] = 0f;
                    rgba[2] = 0f;
                    rgba[3] = 0f;
                }
            }
        };

        ColorTexture1D colors = new ColorTexture1D( 1_000 );
        colors.setColorGradient( containmentMap );

        return colors;
    }

    public static HeatMapPainter newHeatMapPainter( Axis1D axis ) throws IOException
    {
        return newHeatMapPainter( newContainmentColorTexture( ), axis );
    }

    public static HeatMapPainter newHeatMapPainter( ColorTexture1D colorScale, Axis1D axis ) throws IOException
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
        heatmapPainter.setProgram( new InterpColorMapProgram( axis, 0, 1 ) );

        // add the heatmap data and color scale to the painter
        heatmapPainter.setData( texture );
        heatmapPainter.setColorScale( colorScale );

        return heatmapPainter;
    }
}
