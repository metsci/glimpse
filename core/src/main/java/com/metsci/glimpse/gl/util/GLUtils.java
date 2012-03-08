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
package com.metsci.glimpse.gl.util;

import javax.media.opengl.GL;


public class GLUtils
{
    private GLUtils( )
    {
    };

    public static int genBuffer( GL gl )
    {
        int[] handle = new int[1];
        gl.glGenBuffers( 1, handle, 0 );
        return handle[0];
    }

    public static int genTexture( GL gl )
    {
        int[] handle = new int[1];
        gl.glGenTextures( 1, handle, 0 );
        return handle[0];
    }

    public static int queryGLInteger( int param, GL gl )
    {
        int[] value = new int[1];
        gl.glGetIntegerv( param, value, 0 );

        return value[0];
    }

    public static boolean queryGLBoolean( int param, GL gl )
    {
        byte[] value = new byte[1];
        gl.glGetBooleanv( param, value, 0 );

        return value[0] != 0;
    }

    public static int getGLTextureDim( int ndim )
    {
        switch( ndim )
        {
            case 1:
                return GL.GL_TEXTURE_1D;
            case 2:
                return GL.GL_TEXTURE_2D;
            case 3:
                return GL.GL_TEXTURE_3D;
            default:
                throw new IllegalArgumentException( "Only 1D, 2D, and 3D textures allowed." );
        }
    }

    public static int getGLTextureUnit( int texUnit )
    {
        if( texUnit > 31 || texUnit < 0 )
            throw new IllegalArgumentException( "Only 31 texture units supported." );

        return GL.GL_TEXTURE0 + texUnit;
    }
}
