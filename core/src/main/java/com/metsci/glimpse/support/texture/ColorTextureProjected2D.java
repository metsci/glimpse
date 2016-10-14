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
package com.metsci.glimpse.support.texture;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;

/**
 * A texture class which stores 4 channel RGBA colors. Each color channel
 * contains 8 bit fixed point values (capped from 0 to 1).
 *
 * @author ulman
 *
 */
public class ColorTextureProjected2D extends FloatTextureProjected2D
{
    private static final Logger logger = Logger.getLogger( FloatTextureProjected2D.class.getName( ) );

    public static final int FLOATS_PER_PIXEL = 4;

    public ColorTextureProjected2D( )
    {
        this( 0, 0, false );
    }

    public ColorTextureProjected2D( int dataSizeX, int dataSizeY )
    {
        this( dataSizeX, dataSizeY, false );
    }

    public ColorTextureProjected2D( int dataSizeX, int dataSizeY, boolean useVertexZCoord )
    {
        super( dataSizeX, dataSizeY, useVertexZCoord );
    }

    @Override
    protected int getRequiredCapacityBytes( )
    {
        return dataSizeX * dataSizeY * FLOATS_PER_PIXEL * BYTES_PER_FLOAT;
    }

    @Override
    protected Buffer prepare_setPixelStore( GL gl, int i )
    {
        gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
        gl.glPixelStorei( GL3.GL_UNPACK_SKIP_PIXELS, texStartsX[i] );
        gl.glPixelStorei( GL3.GL_UNPACK_ROW_LENGTH, dataSizeX );

        // for some reason, the following does not work:
        //gl.glPixelStorei( GL2.GL_UNPACK_SKIP_ROWS, texStartsY[i] );
        // however, skipping rows manually using data.position works
        return data.asFloatBuffer( ).position( texStartsY[i] * dataSizeX * FLOATS_PER_PIXEL );
    }

    @Override
    protected void prepare_setData( GL gl )
    {
        for ( int i = 0; i < numTextures; i++ )
        {
            gl.glBindTexture( getTextureType( ), textureHandles[i] );

            prepare_setTexParameters( gl );
            Buffer positionBuffer = prepare_setPixelStore( gl, i );

            gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL3.GL_RGBA, texSizesX[i], texSizesY[i], 0, GL3.GL_RGBA, GL3.GL_FLOAT, positionBuffer );
        }

        gl.glPixelStorei( GL3.GL_UNPACK_SKIP_PIXELS, 0 );
        gl.glPixelStorei( GL3.GL_UNPACK_SKIP_ROWS, 0 );
        gl.glPixelStorei( GL3.GL_UNPACK_ROW_LENGTH, 0 );
    }

    public void setData( InputStream in ) throws IOException
    {
        setData( ImageIO.read( in ) );
    }

    public void setData( final BufferedImage image )
    {
        setData0( image, false, -1 );
    }

    public void setData( final BufferedImage image, final float alpha )
    {
        setData0( image, true, alpha );
    }

    protected void setData0( final BufferedImage image, final boolean alphaOverride, final float alpha )
    {
        resize( image.getWidth( ), image.getHeight( ) );

        mutate( new MutatorFloat2D( )
        {
            @Override
            public void mutate( FloatBuffer data, int dataSizeX, int dataSizeY )
            {
                WritableRaster r = image.getAlphaRaster( );

                for ( int y = 0; y < dataSizeY; y++ )
                {
                    for ( int x = 0; x < dataSizeX; x++ )
                    {
                        int rgb = image.getRGB( x, image.getHeight( ) - y - 1 );

                        float a;

                        if ( alphaOverride )
                        {
                            a = alpha;
                        }
                        else if ( r == null )
                        {
                            a = 1.0f;
                        }
                        else
                        {
                            a = r.getSample( x, image.getHeight( ) - y - 1, 0 ) / 255f;
                        }

                        data.put( ( ( rgb >> 16 ) & 0x000000FF ) / 255f );
                        data.put( ( ( rgb >> 8 ) & 0x000000FF ) / 255f );
                        data.put( ( ( rgb ) & 0x000000FF ) / 255f );
                        data.put( a );
                    }
                }
            }
        } );
    }

    public void setData( float[][][] data )
    {
        mutate( new SetDataMutator( data ) );
    }

    /**
     * Updates this texture with new data values. The dimensions of the data
     * array should be float[n0][n1][4] where n0 and n1 are the first and second
     * arguments provided to resize( ) or to the Texture constructor. The last
     * dimension contains the r, g, b, and a channels.
     *
     * @param data the new data values to load into this texture.
     */
    public void setData( double[][][] data )
    {
        mutate( new SetDataMutator( data ) );
    }

    public static class SetDataMutator implements MutatorFloat2D
    {
        public float[][][] dataf;
        public double[][][] datad;

        public SetDataMutator( float[][][] data )
        {
            this.dataf = data;
        }

        public SetDataMutator( double[][][] data )
        {
            this.datad = data;
        }

        @Override
        public void mutate( FloatBuffer buffer, int dataSizeX, int dataSizeY )
        {
            if ( dataf != null )
                mutatef( buffer, dataSizeX, dataSizeY );
            else if ( datad != null ) mutated( buffer, dataSizeX, dataSizeY );
        }

        public void mutatef( FloatBuffer buffer, int dataSizeX, int dataSizeY )
        {
            if ( dataf == null )
            {
                logger.warning( "Null data array passed to SetDataMutator." );
                return;
            }

            if ( dataf.length != dataSizeX || dataf[0].length != dataSizeY )
            {
                logger.warning( "Incorrectly sized data array passed to SetDataMutator." );
                return;
            }

            buffer.clear( );

            for ( int y = 0; y < dataSizeY; y++ )
            {
                for ( int x = 0; x < dataSizeX; x++ )
                {
                    for ( int i = 0; i < FLOATS_PER_PIXEL; i++ )
                    {
                        buffer.put( dataf[x][y][i] );
                    }
                }
            }
        }

        public void mutated( FloatBuffer buffer, int dataSizeX, int dataSizeY )
        {
            if ( datad == null )
            {
                logger.warning( "Null data array passed to SetDataMutator." );
                return;
            }

            if ( datad.length != dataSizeX || datad[0].length != dataSizeY )
            {
                logger.warning( "Incorrectly sized data array passed to SetDataMutator." );
                return;
            }

            buffer.clear( );

            for ( int y = 0; y < dataSizeY; y++ )
            {
                for ( int x = 0; x < dataSizeX; x++ )
                {
                    for ( int i = 0; i < FLOATS_PER_PIXEL; i++ )
                    {
                        buffer.put( ( float ) datad[x][y][i] );
                    }
                }
            }
        }
    }

    @Override
    public void setData( float[][] data )
    {
        mutate( new SetDataMutatorGrayscale( data ) );
    }

    @Override
    public void setData( double[][] data )
    {
        mutate( new SetDataMutatorGrayscale( data ) );
    }

    public static class SetDataMutatorGrayscale implements MutatorFloat2D
    {
        public float[][] dataf;
        public double[][] datad;

        public SetDataMutatorGrayscale( float[][] data )
        {
            this.dataf = data;
        }

        public SetDataMutatorGrayscale( double[][] data )
        {
            this.datad = data;
        }

        @Override
        public void mutate( FloatBuffer buffer, int dataSizeX, int dataSizeY )
        {
            if ( dataf != null )
                mutatef( buffer, dataSizeX, dataSizeY );
            else if ( datad != null ) mutated( buffer, dataSizeX, dataSizeY );
        }

        public void mutatef( FloatBuffer buffer, int dataSizeX, int dataSizeY )
        {
            if ( dataf == null )
            {
                logger.warning( "Null data array passed to SetDataMutator." );
                return;
            }

            if ( dataf.length != dataSizeX || dataf[0].length != dataSizeY )
            {
                logger.warning( "Incorrectly sized data array passed to SetDataMutator." );
                return;
            }

            buffer.clear( );

            for ( int y = 0; y < dataSizeY; y++ )
            {
                for ( int x = 0; x < dataSizeX; x++ )
                {
                    for ( int i = 0; i < FLOATS_PER_PIXEL; i++ )
                    {
                        float d = dataf[x][y];
                        buffer.put( d ).put( d ).put( d ).put( d );
                    }
                }
            }
        }

        public void mutated( FloatBuffer buffer, int dataSizeX, int dataSizeY )
        {
            if ( datad == null )
            {
                logger.warning( "Null data array passed to SetDataMutator." );
                return;
            }

            if ( datad.length != dataSizeX || datad[0].length != dataSizeY )
            {
                logger.warning( "Incorrectly sized data array passed to SetDataMutator." );
                return;
            }

            buffer.clear( );

            for ( int y = 0; y < dataSizeY; y++ )
            {
                for ( int x = 0; x < dataSizeX; x++ )
                {
                    for ( int i = 0; i < FLOATS_PER_PIXEL; i++ )
                    {
                        float d = ( float ) datad[x][y];
                        buffer.put( d ).put( d ).put( d ).put( d );
                    }
                }
            }
        }
    }
}
