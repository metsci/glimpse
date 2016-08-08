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
import static com.metsci.glimpse.dnc.util.Shaders.requireNoErrors;
import static com.metsci.glimpse.util.StringUtils.join;
import static javax.media.opengl.GL.GL_LINES;
import static javax.media.opengl.GL.GL_TRIANGLE_STRIP;
import static javax.media.opengl.GL.GL_TRUE;
import static javax.media.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static javax.media.opengl.GL2ES2.GL_LINK_STATUS;
import static javax.media.opengl.GL2ES2.GL_VERTEX_SHADER;
import static javax.media.opengl.GL2GL3.GL_GEOMETRY_INPUT_TYPE_ARB;
import static javax.media.opengl.GL2GL3.GL_GEOMETRY_OUTPUT_TYPE_ARB;
import static javax.media.opengl.GL2GL3.GL_GEOMETRY_VERTICES_OUT_ARB;
import static javax.media.opengl.GL3.GL_GEOMETRY_SHADER;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import com.metsci.glimpse.util.primitives.IntsArray;

public class DncLineShaders
{

    // XXX: Handling the ( ppvX != ppvY ) case will require separate values for cumulativeDistanceX and cumulativeDistanceY
    public static final String lineVertShader_GLSL = join( "\n",
        "                                                                                                   ",
        "  #version 120                                                                                     ",
        "  #extension GL_EXT_gpu_shader4 : enable                                                           ",
        "                                                                                                   ",
        "  bool setContains( isampler2D setTexture, float index )                                           ",
        "  {                                                                                                ",
        "      ivec2 textureSize = textureSize2D( setTexture, 0 );                                          ",
        "      if ( textureSize.x == 0 || textureSize.y == 0 )                                              ",
        "      {                                                                                            ",
        "          return false;                                                                            ",
        "      }                                                                                            ",
        "      else                                                                                         ",
        "      {                                                                                            ",
        "          float j = floor( index / textureSize.x );                                                ",
        "          float i = index - ( j * textureSize.x );                                                 ",
        "          vec2 st = vec2( i, j ) / textureSize;                                                    ",
        "          int value = texture2D( setTexture, st ).r;                                               ",
        "          return ( value != 0 );                                                                   ",
        "      }                                                                                            ",
        "  }                                                                                                ",
        "                                                                                                   ",
        "  uniform isampler2D HIGHLIGHT_SET;                                                                ",
        "  uniform float LINE_THICKNESS_PX;                                                                 ",
        "  uniform float HIGHLIGHT_EXTRA_THICKNESS_PX;                                                      ",
        "  uniform vec2 PPV;                                                                                ",
        "                                                                                                   ",
        "  varying float vLineThickness_PX;                                                                 ",
        "  varying float vCumulativeDistance_PX;                                                            ",
        "                                                                                                   ",
        "  void main( )                                                                                     ",
        "  {                                                                                                ",
        "      float featureNum = gl_Vertex.z;                                                              ",
        "      bool highlight = setContains( HIGHLIGHT_SET, featureNum );                                   ",
        "      vLineThickness_PX = LINE_THICKNESS_PX + ( highlight ? HIGHLIGHT_EXTRA_THICKNESS_PX : 0.0 );  ",
        "                                                                                                   ",
        "      float cumulativeDistance = gl_Vertex.w;                                                      ",
        "      vCumulativeDistance_PX = cumulativeDistance * PPV.x;                                         ",
        "                                                                                                   ",
        "      gl_Position = gl_ModelViewProjectionMatrix * vec4( gl_Vertex.xy, 0.0, 1.0 );                 ",
        "  }                                                                                                ",
        "                                                                                                   " );

    public static final String lineGeomShader_GLSL = join( "\n",
        "                                                                                    ",
        "  #version 120                                                                      ",
        "  #extension GL_EXT_geometry_shader4 : enable                                       ",
        "                                                                                    ",
        "  vec2 rotate( float x, float y, float cosR, float sinR )                           ",
        "  {                                                                                 ",
        "      return vec2( x*cosR - y*sinR, x*sinR + y*cosR );                              ",
        "  }                                                                                 ",
        "                                                                                    ",
        "  uniform vec2 VIEWPORT_SIZE_PX;                                                    ",
        "  uniform float FEATHER_THICKNESS_PX;                                               ",
        "                                                                                    ",
        "  varying in float vLineThickness_PX[];                                             ",
        "  varying in float vCumulativeDistance_PX[];                                        ",
        "                                                                                    ",
        "  varying out float gLineThickness_PX;                                              ",
        "  varying out vec2 gPosInQuad_PX;                                                   ",
        "                                                                                    ",
        "  void main( )                                                                      ",
        "  {                                                                                 ",
        "      vec2 posA = gl_PositionIn[ 0 ].xy;                                            ",
        "      vec2 posB = gl_PositionIn[ 1 ].xy;                                            ",
        "      vec2 lineDelta = posB - posA;                                                 ",
        "      float lineLength = length( lineDelta );                                       ",
        "                                                                                    ",
        "      float cumulativeDistanceA_PX = vCumulativeDistance_PX[ 0 ];                   ",
        "      float cumulativeDistanceB_PX = vCumulativeDistance_PX[ 1 ];                   ",
        "                                                                                    ",
        "      if ( lineLength > 0.0 && cumulativeDistanceB_PX >= cumulativeDistanceA_PX )   ",
        "      {                                                                             ",
        "          vec2 lineDir = lineDelta / lineLength;                                    ",
        "          vec2 lineNormal = vec2( -lineDir.y, lineDir.x );                          ",
        "                                                                                    ",
        "          vec2 pxToNdc = 2.0 / VIEWPORT_SIZE_PX;                                    ",
        "          float lineThickness_PX = vLineThickness_PX[ 0 ];                          ",
        "          float quadHalfHeight_PX = lineThickness_PX + FEATHER_THICKNESS_PX;        ",
        "          vec2 edgeDelta = quadHalfHeight_PX * pxToNdc * lineNormal;                ",
        "                                                                                    ",
        "                                                                                    ",
        "          gl_Position.xy = posA + edgeDelta;                                        ",
        "          gl_Position.zw = vec2( 0.0, 1.0 );                                        ",
        "          gLineThickness_PX = lineThickness_PX;                                     ",
        "          gPosInQuad_PX = vec2( cumulativeDistanceA_PX, quadHalfHeight_PX );        ",
        "          EmitVertex( );                                                            ",
        "                                                                                    ",
        "          gl_Position.xy = posA - edgeDelta;                                        ",
        "          gl_Position.zw = vec2( 0.0, 1.0 );                                        ",
        "          gLineThickness_PX = lineThickness_PX;                                     ",
        "          gPosInQuad_PX = vec2( cumulativeDistanceA_PX, -quadHalfHeight_PX );       ",
        "          EmitVertex( );                                                            ",
        "                                                                                    ",
        "          gl_Position.xy = posB + edgeDelta;                                        ",
        "          gl_Position.zw = vec2( 0.0, 1.0 );                                        ",
        "          gLineThickness_PX = lineThickness_PX;                                     ",
        "          gPosInQuad_PX = vec2( cumulativeDistanceB_PX, quadHalfHeight_PX );        ",
        "          EmitVertex( );                                                            ",
        "                                                                                    ",
        "          gl_Position.xy = posB - edgeDelta;                                        ",
        "          gl_Position.zw = vec2( 0.0, 1.0 );                                        ",
        "          gLineThickness_PX = lineThickness_PX;                                     ",
        "          gPosInQuad_PX = vec2( cumulativeDistanceB_PX, -quadHalfHeight_PX );       ",
        "          EmitVertex( );                                                            ",
        "                                                                                    ",
        "                                                                                    ",
        "          EndPrimitive( );                                                          ",
        "      }                                                                             ",
        "  }                                                                                 ",
        "                                                                                    " );

    public static final String lineFragShader_GLSL = join( "\n",
        "                                                                                                             ",
        "  #version 120                                                                                               ",
        "  #extension GL_EXT_gpu_shader4 : enable                                                                     ",
        "                                                                                                             ",
        "  uniform float FEATHER_THICKNESS_PX;                                                                        ",
        "  uniform vec4 RGBA;                                                                                         ",
        "  uniform int STIPPLE_ENABLE;                                                                                ",
        "  uniform float STIPPLE_FACTOR;                                                                              ",
        "  uniform int STIPPLE_PATTERN;                                                                               ",
        "                                                                                                             ",
        "  varying float gLineThickness_PX;                                                                           ",
        "  varying vec2 gPosInQuad_PX;                                                                                ",
        "                                                                                                             ",
        "  void main( )                                                                                               ",
        "  {                                                                                                          ",
        "      float tFeatherStart_PX = 0.5*( gLineThickness_PX - FEATHER_THICKNESS_PX );                             ",
        "      float fade = clamp( ( abs( gPosInQuad_PX.t ) - tFeatherStart_PX ) / FEATHER_THICKNESS_PX, 0.0, 1.0 );  ",
        "                                                                                                             ",
        "      if ( STIPPLE_ENABLE != 0 )                                                                             ",
        "      {                                                                                                      ",
        "          float bitNum = mod( gPosInQuad_PX.s / STIPPLE_FACTOR, 16 );                                        ",
        "          int bitMask = ( 0x1 << int( bitNum ) );                                                            ",
        "          if ( ( STIPPLE_PATTERN & bitMask ) == 0 )                                                          ",
        "          {                                                                                                  ",
        "              discard;                                                                                       ",
        "              return;                                                                                        ",
        "          }                                                                                                  ",
        "      }                                                                                                      ",
        "                                                                                                             ",
        "      float alpha = ( 1.0 - fade ) * RGBA.a;                                                                 ",
        "      gl_FragColor.rgb = RGBA.rgb * alpha;                                                                   ",
        "      gl_FragColor.a = alpha;                                                                                ",
        "  }                                                                                                          ",
        "                                                                                                             " );


    public static int buildLineProgram( GL2 gl )
    {
        IntsArray shaders = new IntsArray( );
        try
        {
            shaders.append( compileShader( gl, GL_VERTEX_SHADER,   lineVertShader_GLSL ) );
            shaders.append( compileShader( gl, GL_GEOMETRY_SHADER, lineGeomShader_GLSL ) );
            shaders.append( compileShader( gl, GL_FRAGMENT_SHADER, lineFragShader_GLSL ) );

            int program = gl.glCreateProgram( );
            for ( int s : shaders.a ) gl.glAttachShader( program, s );
            try
            {
                GL3 gl3 = gl.getGL3( );

                gl3.glProgramParameteriARB( program, GL_GEOMETRY_INPUT_TYPE_ARB, GL_LINES );
                requireNoErrors( gl3 );

                gl3.glProgramParameteriARB( program, GL_GEOMETRY_OUTPUT_TYPE_ARB, GL_TRIANGLE_STRIP );
                requireNoErrors( gl3 );

                gl3.glProgramParameteriARB( program, GL_GEOMETRY_VERTICES_OUT_ARB, 4 );
                requireNoErrors( gl3 );

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


    public static class DncLineProgram
    {
        public final int programHandle;

        public final int HIGHLIGHT_SET;

        public final int VIEWPORT_SIZE_PX;
        public final int PPV;

        public final int LINE_THICKNESS_PX;
        public final int FEATHER_THICKNESS_PX;
        public final int HIGHLIGHT_EXTRA_THICKNESS_PX;

        public final int RGBA;
        public final int STIPPLE_ENABLE;
        public final int STIPPLE_FACTOR;
        public final int STIPPLE_PATTERN;

        public DncLineProgram( GL2 gl )
        {
            this.programHandle = buildLineProgram( gl );

            this.HIGHLIGHT_SET = gl.glGetUniformLocation( programHandle, "HIGHLIGHT_SET" );

            this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( programHandle, "VIEWPORT_SIZE_PX" );
            this.PPV = gl.glGetUniformLocation( programHandle, "PPV" );

            this.LINE_THICKNESS_PX = gl.glGetUniformLocation( programHandle, "LINE_THICKNESS_PX" );
            this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( programHandle, "FEATHER_THICKNESS_PX" );
            this.HIGHLIGHT_EXTRA_THICKNESS_PX = gl.glGetUniformLocation( programHandle, "HIGHLIGHT_EXTRA_THICKNESS_PX" );

            this.RGBA = gl.glGetUniformLocation( programHandle, "RGBA" );
            this.STIPPLE_ENABLE = gl.glGetUniformLocation( programHandle, "STIPPLE_ENABLE" );
            this.STIPPLE_FACTOR = gl.glGetUniformLocation( programHandle, "STIPPLE_FACTOR" );
            this.STIPPLE_PATTERN = gl.glGetUniformLocation( programHandle, "STIPPLE_PATTERN" );
        }
    }

}
