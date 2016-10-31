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

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.shader.colormap.ColorMapProgram;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D.MutatorFloat2D;

/**
 * Demonstrates dynamically updating the data stored in a texture.
 *
 * @author ulman
 */
public class AnimatedTextureExample implements GlimpseLayoutProvider
{
    public static void main( String args[] ) throws Exception
    {
        Example.showWithSwing( new AnimatedTextureExample( ) );
    }

    @Override
    public ColorAxisPlot2D getLayout( )
    {
        // create a premade heat map window
        ColorAxisPlot2D plot = new ColorAxisPlot2D( );

        plot.setPlotBackgroundColor( GlimpseColor.getBlack( ) );
        plot.setBackgroundColor( GlimpseColor.getBlack( ) );
        plot.setAxisColor( GlimpseColor.getWhite( ) );
        plot.getGridPainter( ).setLineColor( GlimpseColor.getWhite( 0.5f ) );

        plot.setAxisLabelX( "x axis" );
        plot.setAxisLabelY( "y axis" );
        plot.setAxisLabelZ( "z axis" );
        plot.setTitle( "Animated Texture" );
        plot.setTitleColor( GlimpseColor.getWhite( ) );
        plot.getBorderPainter( ).setColor( GlimpseColor.getWhite( ) );
        
        plot.getCrosshairPainter( ).setCursorColor( GlimpseColor.getGreen( 0.2f ) );
        plot.getCrosshairPainter( ).setShadeColor( GlimpseColor.getGreen( 0.05f ) );
        plot.getCrosshairPainter( ).setShadeSelectionBox( true );
        plot.getCrosshairPainter( ).setLineWidth( 1 );

        plot.setMinX( -1.2 );
        plot.setMaxX( 1.2 );
        plot.setMinY( -1.2 );
        plot.setMaxY( 1.2 );
        plot.setMinZ( 0 );
        plot.setMaxZ( 1000 );

        plot.lockAspectRatioXY( 1 );

        // add a painter that will use a shader for color-mapping
        final ShadedTexturePainter painter = new ShadedTexturePainter( );
        plot.addPainter( painter );

        // setup the color map for the painter
        ColorTexture1D colors = new ColorTexture1D( 1024 );
        colors.setColorGradient( ColorGradients.jet );

        // The data texture and color map texture need to be placed in "texture units"
        // on the graphics card where the shader can find them.
        // To facilitate this, these integer constants are passed both to the painter
        // and do the shader.
        // **Note** If you aren't writing your own shaders, HeatMapPainter takes care
        // of these details for you. See HeatMapExample for a simpler example.
        final int dataTextureUnit = 0;
        final int colorScaleTextureUnit = 1;

        // the colors texture won't be drawn (it's simply used as source data by the texture
        // to choose colors for the heat map data) so we add it as a non-drawable texture.
        // we set the same texture unit which will be supplied to the shader so that the
        // shader knows how to reference this texture.
        painter.addNonDrawableTexture( colors, colorScaleTextureUnit );

        // create a shader which samples from a 1D color texture to
        // color map a 2D data texture
        ColorMapProgram shader;
        try
        {
            shader = new ColorMapProgram( plot.getAxisZ( ), dataTextureUnit, colorScaleTextureUnit );
        }
        catch ( IOException e )
        {
            e.printStackTrace( );
            throw new RuntimeException( e );
        }
        painter.setProgram( shader );

        // setup a thread that will mutate the target texture at regular intervals
        new Thread( new Runnable( )
        {
            @Override
            public void run( )
            {
                // allocate a texture
                FloatTextureProjected2D data = setupTexture( );

                // create a mutator which will periodically modify the data stored in the texture
                MutatorFloat2D mutator = setupMutator( data );

                // register the texture with the painter so that it is drawn.
                // we set the same texture unit which will be supplied to the shader so that the
                // shader knows how to reference this texture
                painter.addDrawableTexture( data, dataTextureUnit );

                while ( true )
                {
                    data.mutate( mutator );

                    try
                    {
                        Thread.sleep( 40 );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
            }
        } ).start( );

        // load the color map into the plot (so the color scale is displayed on the z axis)
        plot.setColorScale( colors );

        // paints a border around the plot area
        plot.addPainter( new BorderPainter( ) );

        // paints an estimate of how many times per second the display is being updated
        plot.addPainter( new FpsPainter( ) );

        return plot;
    }

    static FloatTextureProjected2D setupTexture( )
    {
        int n = 200;

        FloatTextureProjected2D data = new FloatTextureProjected2D( n, n, false );
        data.setProjection( new FlatProjection( -1, 1, -1, 1 ) );

        return data;
    }

    static MutatorFloat2D setupMutator( FloatTextureProjected2D data )
    {
        MutatorFloat2D editor = new MutatorFloat2D( )
        {
            double w = 2 * PI / 60;
            int fr = 0;

            @Override
            public void mutate( FloatBuffer data, int dataSizeX, int dataSizeY )
            {
                data.clear( );
                for ( int i = 0; i < dataSizeX; i++ )
                    for ( int j = 0; j < dataSizeY; j++ )
                    {
                        double x0 = 2 * PI * i / dataSizeX;
                        double x1 = 2 * PI * j / dataSizeY;
                        data.put( ( float ) ( 1000 * ( abs( sin( x0 + w * fr ) ) * ( sin( x0 - w * fr ) * sin( 8 * x0 + w * fr ) * cos( -8 * x1 - w * fr ) + abs( sin( 24 * x1 ) ) / 3 ) ) ) );
                    }
                fr++;
            }
        };

        return editor;
    }
}
