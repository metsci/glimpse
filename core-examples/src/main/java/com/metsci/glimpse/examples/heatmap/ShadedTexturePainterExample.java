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

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.shader.colormap.ColorMapProgram;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;

/**
 * Demonstrates use of the Glimpse
 * {@link com.metsci.glimpse.painter.texture.ShadedTexturePainter}.
 * This example reproduces
 * {@link com.metsci.glimpse.examples.heatmap.HeatMapExample} using the
 * more general ShadedTexturePainter.
 *
 * @author ulman
 */
public class ShadedTexturePainterExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new ShadedTexturePainterExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        // create a simple plot with x, y, and z axes
        ColorAxisPlot2D plot = new ColorAxisPlot2D( );

        // create a generic painter for displaying textures with shaders applied to them
        ShadedTexturePainter painter = new ShadedTexturePainter( );

        // create a shader which colors a 2D texture according to a 1D color map
        ColorMapProgram shader = new ColorMapProgram( plot.getAxisZ( ), 0, 1 );

        // associated the pipeline with the painter
        painter.setProgram( shader );

        // generate some data to display
        double[][] data = HeatMapExample.generateData( 1000, 1000 );

        // generate a projection indicating how the data should be mapped to plot coordinates
        Projection projection = new FlatProjection( 0, 1000, 0, 1000 );

        // create an OpenGL texture wrapper object
        FloatTextureProjected2D texture = new FloatTextureProjected2D( 1000, 1000 );

        // load the data and projection into the texture
        texture.setProjection( projection );
        texture.setData( data );

        // add the texture to the painter, the second argument (0) is arbitrary but must match
        // the second argument to the SampledColorScaleShader above. This indicates to the
        // shader that the texture object represents the 2D array of heat map data values.
        painter.addDrawableTexture( texture, 0 );

        // create a 1D texture containing a jet color scale
        ColorTexture1D colors = new ColorTexture1D( 1024 );
        colors.setColorGradient( ColorGradients.jet );
        plot.setColorScale( colors );

        // add the texture to the painter, the second argument (1) is arbitrary but must match
        // the third argument to the SampledColorScaleShader above. This indicates to the
        // shader that the colors object represents the 1D array of color scale values.
        painter.addNonDrawableTexture( colors, 1 );

        // add the painter to the plot
        plot.addPainter( painter );

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

        // lock the aspect ratio of the x and y axis to 1 to 1
        plot.lockAspectRatioXY( 1.0f );

        // set the size of the selection box to 100.0 units
        plot.setSelectionSize( 100.0f );

        return plot;
    }
}
