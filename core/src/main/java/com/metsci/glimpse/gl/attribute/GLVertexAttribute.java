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
package com.metsci.glimpse.gl.attribute;

import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_COLOR_ARRAY;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_NORMAL_ARRAY;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_TEXTURE_COORD_ARRAY;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import javax.media.opengl.GL2;

/**
 * According to NVIDIA:
 *
 * "GLSL attempts to eliminate aliasing of vertex attributes but this is
 * integral to NVIDIA's hardware approach and necessary for maintaining
 * compatibility with existing OpenGL applications that NVIDIA customers rely
 * on. NVIDIA's GLSL implementation therefore does not allow built-in vertex
 * attributes to collide with a generic vertex attributes that is assigned to a
 * particular vertex attribute index with glBindAttribLocation. For example, you
 * should not use gl_Normal (a built-in vertex attribute) and also use
 * glBindAttribLocation to bind a generic vertex attribute named "whatever"
 * to vertex attribute index 2 because gl_Normal aliases to index 2."
 *
 * gl_Vertex             0
 * gl_Normal             2
 * gl_Color              3
 * gl_SecondaryColor     4
 * gl_FogCoord           5
 * gl_MultiTexCoord0     8
 * gl_MultiTexCoord1     9
 * gl_MultiTexCoord2    10
 * gl_MultiTexCoord3    11
 * gl_MultiTexCoord4    12
 * gl_MultiTexCoord5    13
 * gl_MultiTexCoord6    14
 * gl_MultiTexCoord7    15
 * 
 * (source: http://developer.download.nvidia.com/opengl/glsl/glsl_release_notes.pdf)
 */
public enum GLVertexAttribute
{
    ATTRIB_POSITION_1D(1), ATTRIB_POSITION_2D(2), ATTRIB_POSITION_3D(3), ATTRIB_POSITION_4D(4),

    ATTRIB_COLOR_3D(3), ATTRIB_COLOR_4D(4),

    ATTRIB_NORMAL(4),

    ATTRIB_TEXCOORD_1D(1), ATTRIB_TEXCOORD_2D(2), ATTRIB_TEXCOORD_3D(3), ATTRIB_TEXCOORD_4D(4);

    public final int length;

    private GLVertexAttribute( int length )
    {
        this.length = length;
    }

    public void bind( GL2 gl, int stride, int offset )
    {
        toggle( gl, true, stride, offset );
    }

    public void unbind( GL2 gl )
    {
        toggle( gl, false, 0, 0 );
    }

    private final void toggle( GL2 gl, boolean bind, int stride, int offset )
    {
        switch ( this )
        {
            case ATTRIB_POSITION_1D:
            case ATTRIB_POSITION_2D:
            case ATTRIB_POSITION_3D:
            case ATTRIB_POSITION_4D:
                if ( bind )
                {
                    gl.glEnableClientState( GL_VERTEX_ARRAY );
                    gl.glVertexPointer( length, GL_FLOAT, stride, offset );
                }
                else
                {
                    gl.glDisableClientState( GL_VERTEX_ARRAY );
                }
                break;

            case ATTRIB_COLOR_3D:
            case ATTRIB_COLOR_4D:
                if ( bind )
                {
                    gl.glEnableClientState( GL_COLOR_ARRAY );
                    gl.glColorPointer( length, GL_FLOAT, stride, offset );
                }
                else
                {
                    gl.glDisableClientState( GL_COLOR_ARRAY );
                }
                break;

            case ATTRIB_NORMAL:
                if ( bind )
                {
                    gl.glEnableClientState( GL_NORMAL_ARRAY );
                    gl.glNormalPointer( GL_FLOAT, stride, offset );
                }
                else
                {
                    gl.glDisableClientState( GL_NORMAL_ARRAY );
                }
                break;

            case ATTRIB_TEXCOORD_1D:
            case ATTRIB_TEXCOORD_2D:
            case ATTRIB_TEXCOORD_3D:
            case ATTRIB_TEXCOORD_4D:
                if ( bind )
                {
                    gl.glEnableClientState( GL_TEXTURE_COORD_ARRAY );
                    gl.glTexCoordPointer( length, GL_FLOAT, 0, 0 );
                }
                else
                {
                    gl.glDisableClientState( GL_TEXTURE_COORD_ARRAY );
                }
                break;

            default:
                throw new UnsupportedOperationException( "Unsupported attribute." );
        }
    }

    public static void bind( GL2 gl, int index, int length, int stride, int offset )
    {
        gl.glEnableVertexAttribArray( index );
        gl.glVertexAttribPointer( index, length, GL_FLOAT, false, stride, offset );
    }

    public static void unbind( GL2 gl, int index )
    {
        gl.glDisableVertexAttribArray( index );
    }
}
