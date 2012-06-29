/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.examples.shader;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.GLCapabilityLogger;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer.Mutator;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer2D;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.gl.texture.ColorTexture1D.ColorGradientBuilder;
import com.metsci.glimpse.gl.texture.FloatTexture1D;
import com.metsci.glimpse.gl.texture.FloatTexture1D.MutatorFloat1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.adapter.GLSimpleListenerPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.painter.shape.ShadedPointPainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.util.math.stochastic.Generator;
import com.metsci.glimpse.util.math.stochastic.StochasticEngineMersenne;
import com.metsci.glimpse.util.math.stochastic.pdfcont.PdfCont;
import com.metsci.glimpse.util.math.stochastic.pdfcont.PdfContGaussianZiggurat;
import com.metsci.glimpse.util.math.stochastic.pdfcont.PdfContUniform;

import static java.lang.Math.*;

/**
 * @author osborn
 */
public class PointShaderExample implements GlimpseLayoutProvider
{
    static final int NPOINTS = 100 * 1000;

    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new PointShaderExample( ) );
    }

    @Override
    public GlimpseAxisLayout2D getLayout( )
    {
        // generate a fast source of randomness to use later for making points
        final Generator g = StochasticEngineMersenne.createEngine( 1234567890 ).getGenerator( );

        // create a premade heat map window
        ColorAxisPlot2D plot = new ColorAxisPlot2D( );

        plot.setPlotBackgroundColor( GlimpseColor.getBlack( ) );
        plot.setBackgroundColor( GlimpseColor.getBlack( ) );
        plot.setAxisColor( GlimpseColor.getWhite( ) );
        plot.setTitleColor( GlimpseColor.getWhite( ) );

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

        // add a logger that tells us about OpenGL versions and buffer capabilities
        // because this is a GLSimpleListener, we must use the GLSimpleListenerPainter
        // adapter to satisfy the GlimpsePainter interface
        plot.addPainter( new GLSimpleListenerPainter( new GLCapabilityLogger( ) ) );

        // add a painter that will use our new shader for color-mapping
        ShadedPointPainter dp;
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
        final GLFloatBuffer2D positions = new GLFloatBuffer2D( NPOINTS );
        positions.mutate( new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                PdfCont dist = new PdfContGaussianZiggurat( 0, 1 );

                data.clear( );
                for ( int i = 0; i < NPOINTS; i++ )
                {
                    data.put( ( float ) dist.draw( g ) ); // x
                    data.put( ( float ) dist.draw( g ) ); // y
                }
            }
        } );

        // create an mutator that "wiggles" the points around
        final Mutator positionMutator = new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                PdfCont dist = new PdfContGaussianZiggurat( 0, 0.001 );

                data.clear( );
                for ( int i = 0; i < NPOINTS; i++ )
                {
                    data.put( ( float ) ( data.get( 2 * i ) + dist.draw( g ) ) );
                    data.put( ( float ) ( data.get( 2 * i + 1 ) + dist.draw( g ) ) );
                }
            }
        };
        dp.useVertexPositionData( positions );

        // setup the color value data for the points
        final GLFloatBuffer colorValues = new GLFloatBuffer( NPOINTS, 1 );
        final Mutator colorValueMutator = new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                PdfCont dist = new PdfContUniform( 0, 10 );

                data.clear( );
                for ( int i = 0; i < NPOINTS; i++ )
                {
                    if ( g.nextDouble( ) > 0.999 )
                    {
                        data.put( ( float ) dist.draw( g ) );
                    }
                    else
                    {
                        data.put( ( float ) 0.99 * data.get( i ) );
                    }
                }
            }
        };
        colorValues.mutate( colorValueMutator );
        dp.useColorAttribData( colorValues );

        // setup the size value data for the points
        final GLFloatBuffer sizeValues = new GLFloatBuffer( NPOINTS, 1 );
        final Mutator sizeValueMutator = new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                PdfCont dist = new PdfContUniform( 0, 10 );

                data.clear( );
                for ( int i = 0; i < NPOINTS; i++ )
                {
                    if ( g.nextDouble( ) > 0.999 )
                    {
                        data.put( ( float ) dist.draw( g ) );
                    }
                    else
                    {
                        data.put( ( float ) 0.99 * data.get( i ) );
                    }
                }
            }
        };
        sizeValues.mutate( sizeValueMutator );
        dp.useSizeAttribData( sizeValues );

        // setup a thread that will mutate the points at regular intervals
        new Thread( new Runnable( )
        {
            @Override
            public void run( )
            {
                while ( true )
                {
                    try
                    {
                        positions.mutate( positionMutator );
                        colorValues.mutate( colorValueMutator );
                        sizeValues.mutate( sizeValueMutator );
                        Thread.sleep( 10 );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
            }
        } ).start( );

        // paints a border around the plot area
        plot.addPainter( new BorderPainter( ) );

        // paints an estimate of how many times per second the display is being updated
        plot.addPainter( new FpsPainter( ) );

        return plot;
    }
}
