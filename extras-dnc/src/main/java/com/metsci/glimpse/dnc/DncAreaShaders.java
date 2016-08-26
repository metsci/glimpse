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

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.util.primitives.IntsArray;

public class DncAreaShaders
{

    public static final String areaVertShader_GLSL = join( "\n",
        "                                                                                    ",
        "  #version 150                                                                      ",
        "                                                                                    ",
        "  vec2 axisMin( vec4 axisRect )                                                                    ",
        "  {                                                                                                ",
        "      return axisRect.xy;                                                                          ",
        "  }                                                                                                ",
        "                                                                                                   ",
        "  vec2 axisSize( vec4 axisRect )                                                                   ",
        "  {                                                                                                ",
        "      return axisRect.zw;                                                                          ",
        "  }                                                                                                ",
        "                                                                                                   ",
        "  vec2 axisXyToNdc( vec2 xy_AXIS, vec4 axisRect )                                                  ",
        "  {                                                                                                ",
        "      return ( ( xy_AXIS - axisMin( axisRect ) ) / axisSize( axisRect ) );                                                 ",
        "  }                                                                                                ",
        "                                                                                                   ",
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
        "  uniform vec4 AXIS_RECT;                                                           ",
        "  uniform isampler2D HIGHLIGHT_SET;                                                 ",
        "                                                                                    ",
        "  in vec3 inAreaVertex;                                                             ",
        "                                                                                    ",
        "  out float vHighlight;                                                             ",
        "                                                                                    ",
        "  void main( )                                                                      ",
        "  {                                                                                 ",
        "      float featureNum = inAreaVertex.z;                                            ",
        "      vHighlight = ( setContains( HIGHLIGHT_SET, featureNum ) ? 1.0 : 0.0 );        ",
        "                                                                                    ",
        "      vec2 xy_AXIS = inAreaVertex.xy;                                               ",
        "      gl_Position.xy = axisXyToNdc( xy_AXIS, AXIS_RECT );                           ",
        "      gl_Position.zw = vec2( 0.0, 1.0 );                                            ",
        "  }                                                                                 ",
        "                                                                                    " );

    public static final String areaFragShader_GLSL = join( "\n",
        "                                                               ",
        "  #version 150                                                 ",
        "                                                               ",
        "  uniform vec4 RGBA;                                           ",
        "                                                               ",
        "  out vec4 outRgba;                                            ",
        "                                                               ",
        "  void main( )                                                 ",
        "  {                                                            ",
        "      outRgba.rgb = RGBA.rgb * RGBA.a;                         ",
        "      outRgba.a = RGBA.a;                                      ",
        "  }                                                            ",
        "                                                               " );


    protected static final int inAreaVertex = 1;


    public static int buildAreaProgram( GL2ES2 gl )
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
                gl.glBindAttribLocation( program, inAreaVertex, "inAreaVertex" );

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


    public static void setUniformAxisRect( GL2ES2 gl, int location, Axis2D axis )
    {
        Axis1D xAxis = axis.getAxisX( );
        float xMin = ( float ) xAxis.getMin( );
        float xSize = ( float ) ( xAxis.getMax( ) - xAxis.getMin( ) );

        Axis1D yAxis = axis.getAxisY( );
        float yMin = ( float ) yAxis.getMin( );
        float ySize = ( float ) ( yAxis.getMax( ) - yAxis.getMin( ) );

        gl.glUniform4f( location, xMin, yMin, xSize, ySize );
    }


    public static class DncAreaProgram
    {
        public final int programHandle;

        public final int AXIS_RECT;
        public final int RGBA;
        public final int HIGHLIGHT_SET;

        public final int inAreaVertex;

        public DncAreaProgram( GL2ES2 gl )
        {
            this.programHandle = buildAreaProgram( gl );

            this.AXIS_RECT = gl.glGetUniformLocation( programHandle, "AXIS_RECT" );

            this.RGBA = gl.glGetUniformLocation( programHandle, "RGBA" );

            this.HIGHLIGHT_SET = gl.glGetUniformLocation( programHandle, "HIGHLIGHT_SET" );

            this.inAreaVertex = DncAreaShaders.inAreaVertex;
        }
    }

}
