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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.shader.triangle.ColorTexture2DProgram;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D.MutatorByte2D;
import com.metsci.glimpse.support.texture.RGBATextureProjected2D;
import com.metsci.glimpse.util.io.StreamOpener;

/**
 * Demonstrates display of a png file in a Glimpse plot.
 *
 * @author ulman
 */
public class ImageExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new ImageExample( ) );
    }

    @Override
    public SimplePlot2D getLayout( )
    {
        // create a premade heat map window
        SimplePlot2D plot = new SimplePlot2D( );

        // don't show the crosshair painter
        plot.getCrosshairPainter( ).setVisible( false );

        // hide the x and y axes and title
        plot.setBorderSize( 10 );
        plot.setAxisSizeX( 0 );
        plot.setAxisSizeY( 0 );
        plot.setTitleHeight( 0 );

        // set the x, y, and z initial axis bounds
        plot.setMinX( -10.0f );
        plot.setMaxX( 300.0f );

        plot.setMinY( -50.0f );
        plot.setMaxY( 250.0f );

        // lock the aspect ratio of the x and y axis to 1 to 1
        plot.lockAspectRatioXY( 1.0f );

        // set the size of the selection box to 100.0 units
        plot.setSelectionSize( 100.0f );

        BufferedImage img = null;
        // load image data from a file
        try
        {
            img = ImageIO.read( StreamOpener.fileThenResource.openForRead( "images/GlimpseLogo.png" ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace( );
            throw new RuntimeException( e );
        }

        // create an OpenGL texture wrapper object
        final RGBATextureProjected2D texture1 = new RGBATextureProjected2D( img );

        // set a projection to display the data without distortion
        texture1.setProjection( new FlatProjection( 0, texture1.getDimensionSize( 0 ), 0, texture1.getDimensionSize( 1 ) ) );

        // create another an OpenGL texture wrapper object
        RGBATextureProjected2D texture2 = new RGBATextureProjected2D( 400, 400 );

        // add data to the texture directly
        texture2.mutate( new MutatorByte2D( )
        {

            @Override
            public void mutate( ByteBuffer data, int dataSizeX, int dataSizeY )
            {

                for ( int y = 0; y < dataSizeY; y++ )
                {
                    for ( int x = 0; x < dataSizeX; x++ )
                    {
                        data.put( ( byte ) ( 255 * ( Math.random( ) * 100 + ( x * y ) ) / ( 420 * 420 ) ) );
                        data.put( ( byte ) 50 );
                        data.put( ( byte ) ( 255 * ( 0.3 + ( y / ( float ) dataSizeY ) * 0.3f ) ) );
                        data.put( ( byte ) 127 );
                    }
                }
            }
        } );

        // set a projection to position the texture
        texture2.setProjection( new FlatProjection( -50, 350, -100, 300 ) );

        // create a painter to display the texture in the plot
        ShadedTexturePainter imagePainter = new ShadedTexturePainter( );

        imagePainter.setProgram( new ColorTexture2DProgram( ) );

        // load the textures into the painter
        imagePainter.addDrawableTexture( texture1 );
        imagePainter.addDrawableTexture( texture2 );

        // add the painter to the plot
        plot.addPainter( imagePainter );

        return plot;
    }
}
