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
package com.metsci.glimpse.dnc;

import static com.metsci.glimpse.dnc.util.Shaders.compileShader;
import static com.metsci.glimpse.dnc.util.Shaders.getProgramInfoLog;
import static com.metsci.glimpse.util.StringUtils.join;
import static javax.media.opengl.GL.GL_TRUE;
import static javax.media.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static javax.media.opengl.GL2ES2.GL_LINK_STATUS;
import static javax.media.opengl.GL2ES2.GL_VERTEX_SHADER;

import javax.media.opengl.GL2;

import com.metsci.glimpse.util.primitives.IntsArray;

public class DncAreaShaders
{

    public static final String areaVertShader_GLSL = join( "\n",
        "                                                                                    ",
        "  #version 120                                                                      ",
        "  #extension GL_EXT_gpu_shader4 : enable                                            ",
        "                                                                                    ",
        "  bool setContains( isampler2D setTexture, float index )                            ",
        "  {                                                                                 ",
        "      ivec2 textureSize = textureSize2D( setTexture, 0 );                           ",
        "      if ( textureSize.x == 0 || textureSize.y == 0 )                               ",
        "      {                                                                             ",
        "          return false;                                                             ",
        "      }                                                                             ",
        "      else                                                                          ",
        "      {                                                                             ",
        "          float j = floor( index / textureSize.x );                                 ",
        "          float i = index - ( j * textureSize.x );                                  ",
        "          vec2 st = vec2( i, j ) / textureSize;                                     ",
        "          int value = texture2D( setTexture, st ).r;                                ",
        "          return ( value != 0 );                                                    ",
        "      }                                                                             ",
        "  }                                                                                 ",
        "                                                                                    ",
        "  uniform isampler2D HIGHLIGHT_SET;                                                 ",
        "                                                                                    ",
        "  varying float vHighlight;                                                         ",
        "                                                                                    ",
        "  void main( )                                                                      ",
        "  {                                                                                 ",
        "      float featureNum = gl_Vertex.z;                                               ",
        "      vHighlight = ( setContains( HIGHLIGHT_SET, featureNum ) ? 1.0 : 0.0 );        ",
        "                                                                                    ",
        "      gl_Position = gl_ModelViewProjectionMatrix * vec4( gl_Vertex.xy, 0.0, 1.0 );  ",
        "  }                                                                                 ",
        "                                                                                    " );

    public static final String areaFragShader_GLSL = join( "\n",
        "                                                               ",
        "  #version 120                                                 ",
        "                                                               ",
        "  uniform vec4 RGBA;                                           ",
        "                                                               ",
        "  void main( )                                                 ",
        "  {                                                            ",
        "      gl_FragColor.rgb = RGBA.rgb * RGBA.a;                    ",
        "      gl_FragColor.a = RGBA.a;                                 ",
        "  }                                                            ",
        "                                                               " );


    public static int buildAreaProgram( GL2 gl )
    {
        IntsArray shaders = new IntsArray( );
        try
        {
            shaders.append( compileShader( gl, GL_VERTEX_SHADER,   areaVertShader_GLSL ) );
            shaders.append( compileShader( gl, GL_FRAGMENT_SHADER, areaFragShader_GLSL ) );

            int program = gl.glCreateProgram( );
            for ( int s : shaders.a ) gl.glAttachShader( program, s );
            try
            {
                gl.glLinkProgram( program );
                int[] linkStatus = new int[ 1 ];
                gl.glGetProgramiv( program, GL_LINK_STATUS, linkStatus, 0 );
                if ( linkStatus[ 0 ] != GL_TRUE ) throw new RuntimeException( getProgramInfoLog( gl, program ) );

                return program;
            }
            finally
            {
                for ( int s : shaders.a ) gl.glDetachShader( program, s );
            }
        }
        finally
        {
            for ( int i = 0; i < shaders.n; i++ )
            {
                gl.glDeleteShader( shaders.v( i ) );
            }
        }
    }


    public static class DncAreaProgram
    {
        public final int programHandle;

        public final int HIGHLIGHT_SET;
        public final int RGBA;

        public DncAreaProgram( GL2 gl )
        {
            this.programHandle = buildAreaProgram( gl );

            this.HIGHLIGHT_SET = gl.glGetUniformLocation( programHandle, "HIGHLIGHT_SET" );
            this.RGBA = gl.glGetUniformLocation( programHandle, "RGBA" );
        }
    }

}
