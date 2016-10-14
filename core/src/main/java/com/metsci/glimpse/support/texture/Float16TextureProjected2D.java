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

import java.nio.Buffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

/**
 * A texture class which stores 16 bit fixed point greyscale values (capped from 0 to 1).
 *
 * @author ulman
 *
 */
public class Float16TextureProjected2D extends FloatTextureProjected2D
{
    public Float16TextureProjected2D( int dataSizeX, int dataSizeY )
    {
        this( dataSizeX, dataSizeY, false );
    }

    public Float16TextureProjected2D( int dataSizeX, int dataSizeY, boolean useVertexZCoord )
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

            gl.glTexImage2D( GL3.GL_TEXTURE_2D, 0, GL3.GL_R16F, texSizesX[i], texSizesY[i], 0, GL3.GL_RED, GL3.GL_FLOAT, positionedBuffer );

        }

        gl.glPixelStorei( GL2.GL_UNPACK_SKIP_PIXELS, 0 );
        gl.glPixelStorei( GL2.GL_UNPACK_SKIP_ROWS, 0 );
        gl.glPixelStorei( GL2.GL_UNPACK_ROW_LENGTH, 0 );
    }

}
