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
package com.metsci.glimpse.core.examples.scatterplot;

import static com.jogamp.opengl.GLProfile.GL3bc;
import static com.metsci.glimpse.core.support.QuickUtils.initGlimpseOrExitJvm;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseCanvas;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseWindow;
import static java.lang.Math.sqrt;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.jogamp.opengl.GLProfile;
import com.metsci.glimpse.core.gl.GLCapabilityEventListener;
import com.metsci.glimpse.core.gl.texture.ColorTexture1D;
import com.metsci.glimpse.core.gl.texture.FloatTexture1D;
import com.metsci.glimpse.core.gl.texture.ColorTexture1D.ColorGradientBuilder;
import com.metsci.glimpse.core.gl.texture.FloatTexture1D.MutatorFloat1D;
import com.metsci.glimpse.core.painter.decoration.BorderPainter;
import com.metsci.glimpse.core.painter.info.FpsPainter;
import com.metsci.glimpse.core.painter.shape.ShadedPointPainter;
import com.metsci.glimpse.core.plot.ColorAxisPlot2D;
import com.metsci.glimpse.core.support.color.GlimpseColor;
import com.metsci.glimpse.core.support.colormap.ColorGradients;
import com.metsci.glimpse.core.support.swing.NewtSwingEDTGlimpseCanvas;

/**
 * @author osborn
 */
public class PointShaderExample
{
    static final int NPOINTS = 100 * 1000;

    public static void main( String[] args ) throws Exception
    {
        SwingUtilities.invokeLater( ( ) ->
        {
            // random number generator
            final Random r = new Random( );

            // create a premade heat map window
            ColorAxisPlot2D plot = new ColorAxisPlot2D( );

            plot.setPlotBackgroundColor( GlimpseColor.getBlack( ) );
            plot.setBackgroundColor( GlimpseColor.getBlack( ) );
            plot.setAxisColor( GlimpseColor.getWhite( ) );
            plot.setTitleColor( GlimpseColor.getWhite( ) );
            plot.getBorderPainter( ).setColor( GlimpseColor.getWhite( ) );

            plot.getCrosshairPainter( ).setCursorColor( GlimpseColor.getGreen( 0.2f ) );
            plot.getCrosshairPainter( ).setShadeColor( GlimpseColor.getGreen( 0.05f ) );
            plot.getCrosshairPainter( ).setShadeSelectionBox( true );
            plot.getCrosshairPainter( ).setLineWidth( 1 );

            plot.setAxisLabelX( "x axis" );
            plot.setAxisLabelY( "y axis" );
            plot.setAxisLabelZ( "z axis" );
            plot.setTitle( "An Impatient Universe" );

            plot.setMinX( -3 );
            plot.setMaxX( 3 );
            plot.setMinY( -3 );
            plot.setMaxY( 3 );
            plot.setMinZ( 0 );
            plot.setMaxZ( 10 );

            plot.lockAspectRatioXY( 1 );

            // add a painter that will use our new shader for color-mapping
            final ShadedPointPainter dp;
            try
            {
                dp = new ShadedPointPainter( plot.getAxisZ( ), plot.getAxisZ( ) );
            }
            catch ( IOException e )
            {
                e.printStackTrace( );
                throw new RuntimeException( e );
            }
            plot.addPainter( dp );

            // setup the color-map for the painter
            ColorTexture1D colors = new ColorTexture1D( 256 );

            // we want to customize how the ColorTexture1D samples from the jet ColorGradient,
            // so we create a custom ColorGradientBuilder and apply it to the ColorTexture1D
            // using ColorTexture1D.mutate( )
            ColorGradientBuilder builder = new ColorGradientBuilder( ColorGradients.jet )
            {
                @Override
                public void getColor( int index, int size, float[] rgba )
                {
                    super.getColor( index, size, rgba );
                    rgba[3] = 0.2f + 0.5f * ( ( float ) index ) / ( size - 1 );
                }
            };
            colors.mutate( builder );

            dp.useColorScale( colors );

            plot.setColorScale( colors );

            // setup the size-map for the painter
            FloatTexture1D sizes = new FloatTexture1D( 256 );
            final MutatorFloat1D sizeMutator = new MutatorFloat1D( )
            {
                @Override
                public void mutate( FloatBuffer data, int n0 )
                {
                    float minSize = 1f;
                    float maxSize = 12f;
                    float dSize = maxSize - minSize;

                    data.clear( );
                    for ( int i = 0; i < data.capacity( ); i++ )
                    {
                        if ( i == data.capacity( ) - 1 )
                        {
                            data.put( maxSize * 2 );
                        }
                        else
                        {
                            data.put( ( float ) ( minSize + dSize * sqrt( i / ( n0 - 1f ) ) ) );
                        }
                    }
                }
            };
            sizes.mutate( sizeMutator );

            dp.useSizeScale( sizes );

            // setup the position data for the points
            final FloatBuffer positions = FloatBuffer.allocate( NPOINTS * 2 );
            for ( int i = 0; i < NPOINTS; i++ )
            {
                positions.put( ( float ) r.nextGaussian( ) ); // x
                positions.put( ( float ) r.nextGaussian( ) ); // y
            }
            positions.rewind( );
            dp.useVertexPositionData( positions );

            // setup the color value data for the points
            final FloatBuffer colorValues = FloatBuffer.allocate( NPOINTS );
            updateColors( colorValues, r );
            colorValues.rewind( );
            dp.useColorAttribData( colorValues );

            // setup the size value data for the points
            final FloatBuffer sizeValues = FloatBuffer.allocate( NPOINTS );
            updateSizes( sizeValues, r );
            sizeValues.rewind( );
            dp.useSizeAttribData( sizeValues );

            // setup a thread that will mutate the points at regular intervals
            Thread t = new Thread( new Runnable( )
            {
                @Override
                public void run( )
                {
                    while ( true )
                    {
                        updatePositions( positions, r );
                        positions.rewind( );
                        dp.useVertexPositionData( positions );

                        updateColors( colorValues, r );
                        colorValues.rewind( );
                        dp.useColorAttribData( colorValues );

                        updateSizes( sizeValues, r );
                        sizeValues.rewind( );
                        dp.useSizeAttribData( sizeValues );
                    }
                }
            } );
            t.setDaemon( true );
            t.start( );

            // paints a border around the plot area
            plot.addPainter( new BorderPainter( ) );

            // paints an estimate of how many times per second the display is being updated
            plot.addPainter( new FpsPainter( ) );

            // create a window and show the plot
            String appName = "Point Shader Example";
            GLProfile glProfile = initGlimpseOrExitJvm( appName, GL3bc );
            NewtSwingEDTGlimpseCanvas canvas = quickGlimpseCanvas( glProfile, plot );
            quickGlimpseWindow( appName, canvas );

            // add a GLEventListener to the GlimpseCanvas which will print some information
            // about GLCapabilities upon initialization
            canvas.getGLDrawable( ).addGLEventListener( new GLCapabilityEventListener( ) );
        } );
    }

    protected static void updatePositions( FloatBuffer data, Random r )
    {
        data.clear( );
        for ( int i = 0; i < NPOINTS; i++ )
        {
            data.put( ( float ) ( data.get( 2 * i ) + .001 * r.nextGaussian( ) ) );
            data.put( ( float ) ( data.get( 2 * i + 1 ) + .001 * r.nextGaussian( ) ) );
        }
    }

    protected static void updateColors( FloatBuffer data, Random r )
    {
        data.clear( );
        for ( int i = 0; i < NPOINTS; i++ )
        {
            if ( r.nextDouble( ) > 0.999 )
            {
                data.put( 10 * r.nextFloat( ) );
            }
            else
            {
                data.put( ( float ) 0.99 * data.get( i ) );
            }
        }
    }

    protected static void updateSizes( FloatBuffer data, Random r )
    {
        data.clear( );
        for ( int i = 0; i < NPOINTS; i++ )
        {
            if ( r.nextDouble( ) > 0.999 )
            {
                data.put( 10 * r.nextFloat( ) );
            }
            else
            {
                data.put( ( float ) 0.99 * data.get( i ) );
            }
        }
    }
}
