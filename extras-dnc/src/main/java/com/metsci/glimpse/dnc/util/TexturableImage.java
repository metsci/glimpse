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
package com.metsci.glimpse.dnc.util;

import static com.metsci.glimpse.util.GeneralUtils.ints;
import static java.awt.color.ColorSpace.CS_sRGB;
import static java.awt.image.DataBuffer.TYPE_BYTE;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import com.jogamp.opengl.GL;

public class TexturableImage extends BufferedImage
{
    // Textures must use premultiplied alpha, to avoid ugliness at the boundary between transparent
    // and non-transparent pixels. When interpolation (e.g. with GL_LINEAR) involves a transparent
    // pixel, the pixel's RGB affects the interpolated color, even though its alpha is zero.

    protected static final ColorModel colorModel = new ComponentColorModel( ColorSpace.getInstance( CS_sRGB ),
                                                                            ints( 8, 8, 8, 8 ),
                                                                            true,
                                                                            true,
                                                                            TRANSLUCENT,
                                                                            TYPE_BYTE );

    protected static int requirePowerOfTwo( int x )
    {
        boolean isPowerOfTwo = ( x != 0 && ( x & (x-1) ) == 0 );
        if ( !isPowerOfTwo ) throw new RuntimeException( "Not a power of two: " + x );
        return x;
    }

    public TexturableImage( int width, int height )
    {
        super( colorModel,
               colorModel.createCompatibleWritableRaster( requirePowerOfTwo( width ), requirePowerOfTwo( height ) ),
               true,
               null );
    }

    public void pushToTexture( GL gl, int target )
    {
        gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );
        ByteBuffer bytes = ByteBuffer.wrap( ( ( DataBufferByte ) getRaster( ).getDataBuffer( ) ).getData( ) );
        gl.glTexImage2D( target, 0, GL_RGBA, getWidth( ), getHeight( ), 0, GL_RGBA, GL_UNSIGNED_BYTE, bytes );
    }
}