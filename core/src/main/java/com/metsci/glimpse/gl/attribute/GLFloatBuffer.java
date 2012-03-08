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
package com.metsci.glimpse.gl.attribute;

import static com.metsci.glimpse.gl.util.GLUtils.*;

import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;


public class GLFloatBuffer
{
    protected ReentrantLock lock = new ReentrantLock();

    protected int elementSize; // 1, 2, 3, or 4
    protected FloatBuffer data;

    protected int glHandle;
    protected boolean dirty;

    protected GLVertexAttribute boundType;
    protected int boundGenericIndex;

    public GLFloatBuffer( int length, int elementSize )
    {
        if( elementSize > 4 || elementSize < 1 )
            throw new IllegalArgumentException( "length must be 1, 2, 3, or 4" );

        this.glHandle = -1;
        this.dirty = true;

        this.elementSize = elementSize;
        this.data = BufferUtil.newFloatBuffer( length * elementSize );

        this.boundType = null;
        this.boundGenericIndex = -1;
    }

    public boolean isDirty( )
    {
        return dirty;
    }

    public void makeDirty( )
    {
        dirty = true;
    }

    public int getNumVertices( )
    {
        return data.limit() / elementSize;
    }

    public int getMaxVertices( )
    {
        return data.capacity() / elementSize;
    }

    public void bind( GLVertexAttribute type, GL gl )
    {
        lock.lock();
        try
        {
            prepare( gl );
            type.bind( gl, 0, 0 );
            boundType = type;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void bind( int genericIndex, GL gl )
    {
        lock.lock();
        try
        {
            prepare( gl );
            GLVertexAttribute.bind( gl, genericIndex, elementSize, 0, 0 );
            boundGenericIndex = genericIndex;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void unbind( GL gl )
    {
        lock.lock();
        try
        {
            if( boundType != null )
            {
                boundType.unbind( gl );
                boundType = null;
            }
            else if( boundGenericIndex > 0 )
            {
                GLVertexAttribute.unbind( gl, boundGenericIndex );
                boundGenericIndex = -1;
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean prepare( GL gl )
    {
        lock.lock();
        try
        {
            if( glHandle == -1 )
            {
                glHandle = genBuffer( gl );
                makeDirty();
            }

            gl.glBindBuffer( GL.GL_ARRAY_BUFFER, glHandle );

            if( isDirty() )
            {
                gl.glBufferData( GL.GL_ARRAY_BUFFER, data.limit() * BufferUtil.SIZEOF_FLOAT, data.rewind(),
                                 GL.GL_STATIC_DRAW );
                dirty = false;
            }

            return !isDirty();
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean dispose( GL gl )
    {
        lock.lock();
        try
        {
            if( glHandle != -1 )
            {
                gl.glDeleteBuffers( 1, new int[] { glHandle }, 0 );
                glHandle = -1;
            }
        }
        finally
        {
            lock.unlock();
        }

        lock = null;
        return true;
    }

    public void mutate( Mutator mutator )
    {
        lock.lock();
        try
        {
            mutator.mutate( data, elementSize );
            data.flip();
            makeDirty();
        }
        finally
        {
            lock.unlock();
        }
    }

    public static interface Mutator
    {
        public void mutate( FloatBuffer data, int length );
    }
}
