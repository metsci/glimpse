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
package com.metsci.glimpse.gl.texture;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;
import com.metsci.glimpse.support.colormap.ColorGradient;

public class ColorTexture1D extends AbstractTexture
{
    protected FloatBuffer rgba;

    public ColorTexture1D( int n )
    {
        super( n );
        this.rgba = Buffers.newDirectFloatBuffer( 4 * dim[0] );
    }

    @Override
    protected void prepare_setTexParameters( GL gl )
    {
        gl.glTexParameteri( GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST );
        gl.glTexParameteri( GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST );

        gl.glTexParameteri( GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE );
    }

    @Override
    protected void prepare_setPixelStore( GL gl )
    {
        gl.glPixelStorei( GL3.GL_UNPACK_ALIGNMENT, 1 );
    }

    @Override
    protected void prepare_setData( GL gl )
    {
        gl.getGL3( ).glTexImage1D( GL3.GL_TEXTURE_1D, 0, GL3.GL_RGBA, dim[0], 0, GL3.GL_RGBA, GL3.GL_FLOAT, rgba.rewind( ) );
    }

    public void setColorGradient( ColorGradient gradient )
    {
        mutate( new ColorGradientBuilder( gradient ) );
    }

    public void mutate( MutatorColor1D mutator )
    {
        lock.lock( );
        try
        {
            rgba.rewind( );
            mutator.mutate( rgba, dim[0] );
            makeDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public static interface MutatorColor1D
    {
        public void mutate( FloatBuffer floatBuffer, int dim );
    }

    public static class ColorGradientBuilder extends Builder
    {
        ColorGradient gradient;

        public ColorGradientBuilder( ColorGradient gradient )
        {
            this.gradient = gradient;
        }

        @Override
        public void getColor( int index, int size, float[] rgba )
        {
            gradient.toColor( ( ( float ) index ) / ( size - 1 ), rgba );
        }
    }

    public static abstract class Builder implements MutatorColor1D
    {
        public abstract void getColor( int index, int size, float[] rgba );

        public void mutate( FloatBuffer floatBuffer, int dim )
        {
            float[] rgbaBytes = new float[4];

            for ( int i = 0; i < dim; i++ )
            {
                getColor( i, dim, rgbaBytes );

                floatBuffer.put( rgbaBytes[0] );
                floatBuffer.put( rgbaBytes[1] );
                floatBuffer.put( rgbaBytes[2] );
                floatBuffer.put( rgbaBytes[3] );
            }
        }
    }
}
