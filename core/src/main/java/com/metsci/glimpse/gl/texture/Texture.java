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

import javax.media.opengl.GLContext;

import com.metsci.glimpse.context.GlimpseContext;

/**
 * Wrapper interface for an OpenGL texture. Provides methods
 * for getting its size, marking it as modified, binding it to
 * a texture unit, and disposing of it properly.
 *
 * @author osborn
 */
public interface Texture
{
    /**
     * Returns the OpenGL handle to the texture data. Most implementations will
     * return an array with a single value. However, some implementations may split
     * up very large data into multiple OpenGL textures. This method is necessary
     * to support proper handling of multitexturing shaders.
     */
    public int[] getHandles( );

    /**
     * Most Texture implementations are backed by a buffer on the
     * heap which acts as a staging area for data changes to the
     * texture. Calling {@code makeDirty()} indicates that the
     * contents of this buffer has changed and the new data should
     * be copied to the GPU.
     */
    public void makeDirty( );

    /**
     * Indicates whether {@code makeDirty()} has been called. Once
     * {@code makeDirty()} is called, {@code isDirty()} will return
     * true until {@code prepare( GL, int )} is called and the tecture
     * data is copied to the GPU.
     *
     * @return whether the cached texture data has changed
     */
    public boolean isDirty( );

    /**
     * Returns whether the texture is a 1-dimensional, 2-dimensional,
     * or 3-dimensional texture.
     *
     * @return 1, 2, or 3 depending on the dimension of the texture
     */
    public int getNumDimension( );

    /**
     * Given a dimension index (0, 1, or 2) returns the number of data
     * elements in the texture along that dimension.
     *
     * @param n the texture dimension to query
     * @return the size of the queried dimension
     */
    public int getDimensionSize( int n );

    /**
     * Allocates space for the texture in GPU texture memory (once, if this is the
     * first time that {@code prepare( GL, int)} has been called), copies texture
     * data from the heap into GPU memory (if {@code isDirty()} is true). This texture
     * is then made active on the provided texture unit (using {@code glActiveTexture()})
     * and bound as the current {@code GL_TEXTURE_1D}, {@code GL_TEXTURE_2D}, or
     * {@code GL_TEXTURE_3D} as appropriate based on {@code getNumDimension()}. Once this
     * is complete, the texture is ready to be used by a
     * {@link com.metsci.glimpse.painter.base.GlimpsePainter}.
     *
     * @param gl a GL handle for the active GLContext
     * @param texUnit the texture unit to bind to this texture
     * @return whether the preparation succeeded
     */
    public boolean prepare( GlimpseContext context, int texUnit );

    /**
     * Disposes all Java heap and GPU resources associated with this texture.
     *
     * @param context the active GLContext
     */
    public void dispose( GLContext context );
}
