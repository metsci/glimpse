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

import java.nio.ByteBuffer;

import javax.media.opengl.GL;

/**
 * A wrapper around an OpenGL texture handle which is being handled (allocated, freed,
 * drawn into, data loaded onto, etc...) by another class. ExternalTextureProjected2D
 * simply allows the projection capabilities of TextureProjected2D to be applied
 * to such an externally handled texture.
 *
 * @author ulman
 */
public class ExternalTextureProjected2D extends TextureProjected2D
{

    public ExternalTextureProjected2D( int texHandle, int dataSizeX, int dataSizeY, boolean useVertexZCoord )
    {
        super( dataSizeX, dataSizeY, useVertexZCoord );

        this.numTextures = 1;
        this.textureHandles = new int[] { texHandle };
    }

    @Override
    protected void allocate_genTextureHandles( GL gl )
    {
        // do nothing, the texture handle has already been created externally
    }

    @Override
    protected void prepare_setData( GL gl )
    {
        // do nothing, loading data into the texture is handled externally
    }

    @Override
    protected ByteBuffer newByteBuffer( )
    {
        // don't allocate any space, texture data is handled externally
        return null;
    }

    @Override
    protected int getRequiredCapacityBytes( )
    {
        throw new UnsupportedOperationException( "getRequiredCapacityBytes() is not supported by ExternalTextureProjected2D because its underlying OpenGL texture is handled externally." );
    }

    @Override
    protected float getData( int index )
    {
        throw new UnsupportedOperationException( "getData() is not supported by ExternalTextureProjected2D because its underlying OpenGL texture is handled externally." );
    }

    @Override
    public void resize( int dataSizeX, int dataSizeY )
    {
        throw new UnsupportedOperationException( "resize() is not supported by ExternalTextureProjected2D because its underlying OpenGL texture is handled externally." );
    }

}
