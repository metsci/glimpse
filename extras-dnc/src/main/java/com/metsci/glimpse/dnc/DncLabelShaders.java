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
import static javax.media.opengl.GL.GL_POINTS;
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

public class DncLabelShaders
{

    public static final String labelVertShader_GLSL = join( "\n",
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
        "  attribute vec2 aImageAlign;                                                       ",
        "  attribute vec4 aImageBounds;                                                      ",
        "                                                                                    ",
        "  varying float vHighlight;                                                         ",
        "  varying vec2 vImageAlign;                                                         ",
        "  varying vec4 vImageBounds;                                                        ",
        "                                                                                    ",
        "  void main( )                                                                      ",
        "  {                                                                                 ",
        "      float featureNum = gl_Vertex.z;                                               ",
        "      vHighlight = ( setContains( HIGHLIGHT_SET, featureNum ) ? 1.0 : 0.0 );        ",
        "                                                                                    ",
        "      vImageAlign = aImageAlign;                                                    ",
        "      vImageBounds = aImageBounds;                                                  ",
        "                                                                                    ",
        "      gl_Position = gl_ModelViewProjectionMatrix * vec4( gl_Vertex.xy, 0.0, 1.0 );  ",
        "  }                                                                                 ",
        "                                                                                    " );

    public static final String labelGeomShader_GLSL = join( "\n",
        "                                                                                    ",
        "  #version 120                                                                      ",
        "  #extension GL_EXT_geometry_shader4 : enable                                       ",
        "                                                                                    ",
        "  uniform vec2 VIEWPORT_SIZE_PX;                                                    ",
        "  uniform vec2 ATLAS_SIZE_PX;                                                       ",
        "  uniform float HIGHLIGHT_SCALE;                                                    ",
        "                                                                                    ",
        "  varying in float vHighlight[];                                                    ",
        "  varying in vec2 vImageAlign[];                                                    ",
        "  varying in vec4 vImageBounds[];                                                   ",
        "                                                                                    ",
        "  varying out vec2 gAtlasCoords;                                                    ",
        "  varying out float gHighlight;                                                     ",
        "                                                                                    ",
        "  void main( )                                                                      ",
        "  {                                                                                 ",
        "      vec2 p = gl_PositionIn[ 0 ].xy;                                               ",
        "                                                                                    ",
        "      vec2 imageAlign = vImageAlign[ 0 ];                                           ",
        "                                                                                    ",
        "      vec4 imageBounds = vImageBounds[ 0 ];                                         ",
        "      float sMin = imageBounds.s;                                                   ",
        "      float tMin = imageBounds.t;                                                   ",
        "      float sMax = imageBounds.p;                                                   ",
        "      float tMax = imageBounds.q;                                                   ",
        "                                                                                    ",
        "      vec2 imageSize_PX = vec2( sMax - sMin, tMax - tMin ) * ATLAS_SIZE_PX;         ",
        "      vec2 offsetA_PX = -imageAlign * imageSize_PX;                                 ",
        "      vec2 offsetB_PX = offsetA_PX + imageSize_PX;                                  ",
        "      bool highlight = ( vHighlight[ 0 ] >= 0.5 );                                  ",
        "      if ( highlight )                                                              ",
        "      {                                                                             ",
        "          offsetA_PX *= HIGHLIGHT_SCALE;                                            ",
        "          offsetB_PX *= HIGHLIGHT_SCALE;                                            ",
        "      }                                                                             ",
        "                                                                                    ",
        "      vec2 pxToNdc = 2.0 / VIEWPORT_SIZE_PX;                                        ",
        "      vec2 offsetA = offsetA_PX * pxToNdc;                                          ",
        "      vec2 offsetB = offsetB_PX * pxToNdc;                                          ",
        "                                                                                    ",
        "                                                                                    ",
        "      gl_Position.xy = p + vec2( offsetA.x, offsetB.y );                            ",
        "      gl_Position.zw = vec2( 0.0, 1.0 );                                            ",
        "      gAtlasCoords = vec2( sMin, tMin );                                            ",
        "      gHighlight = vHighlight[ 0 ];                                                 ",
        "      EmitVertex( );                                                                ",
        "                                                                                    ",
        "      gl_Position.xy = p + offsetB;                                                 ",
        "      gl_Position.zw = vec2( 0.0, 1.0 );                                            ",
        "      gAtlasCoords = vec2( sMax, tMin );                                            ",
        "      gHighlight = vHighlight[ 0 ];                                                 ",
        "      EmitVertex( );                                                                ",
        "                                                                                    ",
        "      gl_Position.xy = p + offsetA;                                                 ",
        "      gl_Position.zw = vec2( 0.0, 1.0 );                                            ",
        "      gAtlasCoords = vec2( sMin, tMax );                                            ",
        "      gHighlight = vHighlight[ 0 ];                                                 ",
        "      EmitVertex( );                                                                ",
        "                                                                                    ",
        "      gl_Position.xy = p + vec2( offsetB.x, offsetA.y );                            ",
        "      gl_Position.zw = vec2( 0.0, 1.0 );                                            ",
        "      gAtlasCoords = vec2( sMax, tMax );                                            ",
        "      gHighlight = vHighlight[ 0 ];                                                 ",
        "      EmitVertex( );                                                                ",
        "                                                                                    ",
        "                                                                                    ",
        "      EndPrimitive( );                                                              ",
        "  }                                                                                 ",
        "                                                                                    " );

    public static final String labelFragShader_GLSL = join( "\n",
        "                                                                      ",
        "  #version 120                                                        ",
        "  #extension GL_EXT_gpu_shader4 : enable                              ",
        "                                                                      ",
        "  uniform sampler2D ATLAS;                                            ",
        "                                                                      ",
        "  varying vec2 gAtlasCoords;                                          ",
        "  varying float gHighlight;                                           ",
        "                                                                      ",
        "  void main( )                                                        ",
        "  {                                                                   ",
        "      // For scaled labels we need interpolation, but for unscaled    ",
        "      // labels we want to snap to pixels to get crisp text           ",
        "                                                                      ",
        "      vec2 st;                                                        ",
        "      bool highlight = ( gHighlight >= 0.5 );                         ",
        "      if ( highlight )                                                ",
        "      {                                                               ",
        "          st = gAtlasCoords;                                          ",
        "      }                                                               ",
        "      else                                                            ",
        "      {                                                               ",
        "          vec2 atlasSize_PX = textureSize2D( ATLAS, 0 );              ",
        "          vec2 st_PX = floor( gAtlasCoords * atlasSize_PX + 0.5 );    ",
        "          st = ( st_PX + 0.5 ) / atlasSize_PX;                        ",
        "      }                                                               ",
        "                                                                      ",
        "      gl_FragColor = texture2D( ATLAS, st );                          ",
        "  }                                                                   ",
        "                                                                      " );


    protected static final int aImageAlign = 1;
    protected static final int aImageBounds = 2;


    public static int buildLabelProgram( GL2 gl )
    {
        IntsArray shaders = new IntsArray( );
        try
        {
            shaders.append( compileShader( gl, GL_VERTEX_SHADER,   labelVertShader_GLSL ) );
            shaders.append( compileShader( gl, GL_GEOMETRY_SHADER, labelGeomShader_GLSL ) );
            shaders.append( compileShader( gl, GL_FRAGMENT_SHADER, labelFragShader_GLSL ) );

            int program = gl.glCreateProgram( );
            for ( int s : shaders.a ) gl.glAttachShader( program, s );
            try
            {
                gl.glBindAttribLocation( program, aImageAlign, "aImageAlign" );
                gl.glBindAttribLocation( program, aImageBounds, "aImageBounds" );


                GL3 gl3 = gl.getGL3( );

                gl3.glProgramParameteriARB( program, GL_GEOMETRY_INPUT_TYPE_ARB, GL_POINTS );
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


    public static class DncLabelProgram
    {
        public final int programHandle;

        public final int HIGHLIGHT_SET;

        public final int VIEWPORT_SIZE_PX;

        public final int HIGHLIGHT_SCALE;

        public final int ATLAS;
        public final int ATLAS_SIZE_PX;

        public final int aImageAlign;
        public final int aImageBounds;

        public DncLabelProgram( GL2 gl )
        {
            this.programHandle = buildLabelProgram( gl );

            this.HIGHLIGHT_SET = gl.glGetUniformLocation( programHandle, "HIGHLIGHT_SET" );

            this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( programHandle, "VIEWPORT_SIZE_PX" );

            this.HIGHLIGHT_SCALE = gl.glGetUniformLocation( programHandle, "HIGHLIGHT_SCALE" );

            this.ATLAS = gl.glGetUniformLocation( programHandle, "ATLAS" );
            this.ATLAS_SIZE_PX = gl.glGetUniformLocation( programHandle, "ATLAS_SIZE_PX" );

            this.aImageAlign = DncLabelShaders.aImageAlign;
            this.aImageBounds = DncLabelShaders.aImageBounds;
        }
    }

}
