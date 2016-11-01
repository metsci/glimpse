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
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.metsci.glimpse.support.texture.ByteTextureProjected2D.MutatorByte2D;

/**
 *
 * A texture class which stores 3 channel RGB colors. Each color channel
 * contains 8 bit values (capped from 0 to 255).
 *
 * @author oren
 */
public class RGBTextureProjected2D extends TextureProjected2D
{

    public static final int BYTES_PER_PIXEL = 3;

    public RGBTextureProjected2D( BufferedImage img )
    {
        this( img.getWidth( ), img.getHeight( ), false );
        setData( img );
    }

    public RGBTextureProjected2D( int dataSizeX, int dataSizeY )
    {
        this( dataSizeX, dataSizeY, false );
    }

    public RGBTextureProjected2D( int dataSizeX, int dataSizeY, boolean useVertexZCoord )
    {
        super( dataSizeX, dataSizeY, useVertexZCoord );
    }

    @Override
    protected void prepare_setData( GL gl )
    {

        for ( int i = 0; i < numTextures; i++ )
        {
            gl.glBindTexture( getTextureType( ), textureHandles[i] );

            prepare_setTexParameters( gl );
            Buffer positionedBuffer = prepare_setPixelStore( gl, i );
            gl.glTexImage2D( GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, texSizesX[i], texSizesY[i], 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, positionedBuffer );
        }

        gl.glPixelStorei( GL2.GL_UNPACK_SKIP_PIXELS, 0 );
        gl.glPixelStorei( GL2.GL_UNPACK_SKIP_ROWS, 0 );
        gl.glPixelStorei( GL2.GL_UNPACK_ROW_LENGTH, 0 );
    }

    protected Buffer prepare_setPixelStore( GL gl, int i )
    {
        gl.glPixelStorei( GL2.GL_UNPACK_ALIGNMENT, 1 );
        gl.glPixelStorei( GL2.GL_UNPACK_SKIP_PIXELS, texStartsX[i] );
        gl.glPixelStorei( GL2.GL_UNPACK_ROW_LENGTH, dataSizeX );

        // for some reason, the following does not work:
        //gl.glPixelStorei( GL2.GL_UNPACK_SKIP_ROWS, texStartY[i] );
        // however, skipping rows manually using data.position works
        return data.position( texStartsY[i] * dataSizeX * BYTES_PER_PIXEL );
    }

    @Override
    protected int getRequiredCapacityBytes( )
    {
        return BYTES_PER_PIXEL * dataSizeX * dataSizeY;
    }

    @Override
    protected float getData( int index )
    {
        int offset = index * BYTES_PER_PIXEL;
        byte r = data.get( offset );
        byte g = data.get( offset + 1 );
        byte b = data.get( offset + 2 );
        return ( r << 16 ) | ( g << 8 ) | b;
    }

    public void setData( InputStream in ) throws IOException
    {
        setData( ImageIO.read( in ) );
    }

    public void setData( final BufferedImage image )
    {
        setData0( image );
    }

    protected void setData0( final BufferedImage image )
    {
        resize( image.getWidth( ), image.getHeight( ) );

        lock.lock( );
        try
        {
            data.rewind( );
            final byte[] rgb = new byte[3];
            //Note: x and y here are in java image space, not texture space.
            for ( int y = dataSizeY - 1; y >= 0; y-- )
            {
                for ( int x = 0; x < dataSizeX; x++ )
                {
                    int argb = image.getRGB( x, y );
                    rgb[0] = ( byte ) ( ( 0x00ff0000 & argb ) >> 16 );
                    rgb[1] = ( byte ) ( ( 0x0000ff00 & argb ) >> 8 );
                    rgb[2] = ( byte ) ( ( 0x000000ff & argb ) );
                    data.put( rgb );
                }
            }
            makeDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    /**
     * For modifying the byte buffer directly, pixels should be packed as RGB.
     * @param mutator
     */
    public void mutate( MutatorByte2D mutator )
    {
        lock.lock( );
        try
        {
            data.rewind( );
            mutator.mutate( data, dataSizeX, dataSizeY );
            makeDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

}
