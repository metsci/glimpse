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

/**
 * A one dimensional texture storing float values.
 *
 * @author osborn
 */
public class FloatTexture1D extends AbstractTexture
{
    protected FloatBuffer data;

    public FloatTexture1D( int n0 )
    {
        super( n0 );
        this.data = Buffers.newDirectFloatBuffer( n0 );
    }

    @Override
    protected void prepare_setTexParameters( GL gl )
    {
        gl.glTexParameteri( GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST );
        gl.glTexParameteri( GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST );

        gl.glTexParameteri( GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE );
    }

    @Override
    protected void prepare_setData( GL gl )
    {
        gl.getGL3( ).glTexImage1D( GL3.GL_TEXTURE_1D, 0, GL3.GL_R32F, dim[0], 0, GL3.GL_RED, GL3.GL_FLOAT, data.rewind( ) );
    }

    @Override
    protected void prepare_setPixelStore( GL gl )
    {
        gl.glPixelStorei( GL3.GL_UNPACK_ALIGNMENT, 1 );
    }

    public void mutate( MutatorFloat1D mutator )
    {
        lock.lock( );
        try
        {
            data.rewind( );
            mutator.mutate( data, dim[0] );
            makeDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public static interface MutatorFloat1D
    {
        public void mutate( FloatBuffer data, int n0 );
    }
}
