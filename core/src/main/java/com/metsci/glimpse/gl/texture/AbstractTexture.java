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

import static com.metsci.glimpse.gl.util.GLUtils.*;

import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.context.GlimpseContext;

/**
 * An abstract {@link Texture} implementation which wraps an OpenGL
 * integer texture handle but makes no assumptions about the
 * data types stored in the texture.
 *
 * @author osborn
 */
public abstract class AbstractTexture implements Texture
{
    protected ReentrantLock lock = new ReentrantLock( );

    protected boolean glAllocated;
    protected int glHandle;

    protected boolean dirty;

    protected int[] dim;

    public AbstractTexture( int n0 )
    {
        this.dim = new int[] { n0 };
        this.glAllocated = false;
        makeDirty( );
    }

    public AbstractTexture( int n0, int n1 )
    {
        this.dim = new int[] { n0, n1 };
        this.glAllocated = false;
        makeDirty( );
    }

    public AbstractTexture( int n0, int n1, int n2 )
    {
        this.dim = new int[] { n0, n1, n2 };
        this.glAllocated = false;
        makeDirty( );
    }

    @Override
    public int[] getHandles( )
    {
        return new int[] { glHandle };
    }

    @Override
    public void makeDirty( )
    {
        dirty = true;
    }

    @Override
    public boolean isDirty( )
    {
        return dirty;
    }

    @Override
    public int getNumDimension( )
    {
        return dim.length;
    }

    @Override
    public int getDimensionSize( int n )
    {
        return dim[n];
    }

    @Override
    public boolean prepare( GlimpseContext context, int texUnit )
    {
        GL gl = context.getGL( );

        // should we check for dirtiness and allocation before lock to speed up?
        lock.lock( );
        try
        {
            if ( !glAllocated )
            {
                allocate_genHandles( gl );
            }

            gl.glActiveTexture( getGLTextureUnit( texUnit ) );
            gl.glBindTexture( getGLTextureDim( dim.length ), glHandle );

            if ( glAllocated && isDirty( ) )
            {
                prepare_setPixelStore( gl );
                prepare_setTexParameters( gl );
                prepare_setData( gl );
                dirty = false;
            }

            return !isDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected void allocate_genHandles( GL gl )
    {
        int[] handle = new int[1];
        gl.glGenTextures( 1, handle, 0 );

        glHandle = handle[0];
        glAllocated = true;

        makeDirty( );
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( glAllocated )
        {
            context.getGL( ).glDeleteTextures( 1, new int[] { glHandle }, 0 );
        }
    }

    protected abstract void prepare_setTexParameters( GL gl );

    protected abstract void prepare_setPixelStore( GL gl );

    protected abstract void prepare_setData( GL gl );
}
