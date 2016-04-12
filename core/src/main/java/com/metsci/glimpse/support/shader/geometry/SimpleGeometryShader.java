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
package com.metsci.glimpse.support.shader.geometry;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.gl.shader.ShaderType;

public class SimpleGeometryShader extends SimpleShader
{

    public final int inType, outType;
    private final int maxVertices;

    public SimpleGeometryShader( String name, String shaderFile, int inType, int outType, int maxVertices )
    {
        super( name, ShaderType.geometry, shaderFile );
        this.inType = inType;
        this.outType = outType;
        this.maxVertices = maxVertices;
    }

    public static SimpleGeometryShader passGeometry( int inType, int outType, int maxVertices )
    {
        return new SimpleGeometryShader( "passthrough", "shaders/geometry/passthrough.gs", inType, outType, maxVertices );
    }

    public static SimpleGeometryShader pointsToFixedSizeNGons( final boolean solid, final int N, final float radius )
    {
        return new SimpleGeometryShader( "ngons" + N, "shaders/geometry/n_gon.gs", GL3.GL_POINTS, solid ? GL3.GL_TRIANGLE_STRIP : GL3.GL_LINE_STRIP, solid ? 3 * ( N - 1 ) : N + 1 )
        {
            public void preDisplay( GL gl )
            {
                this.getArg( "N" ).setValue( N );
                this.getArg( "radius" ).setValue( radius );
                this.getArg( "solid" ).setValue( solid );
            }
        };
    }

    public static SimpleGeometryShader pointsToPixelSizedNGons( final boolean solid, final int N, final int radiusPixels )
    {
        return new SimpleGeometryShader( "ngons" + N, "shaders/geometry/pixel_n_gon.gs", GL3.GL_POINTS, solid ? GL3.GL_TRIANGLE_STRIP : GL3.GL_LINE_STRIP, solid ? 3 * ( N - 1 ) : N + 1 )
        {
            public void preDisplay( GL gl )
            {
                this.getArg( "N" ).setValue( N );
                {
                    int[] viewport = new int[4];
                    gl.glGetIntegerv( GL.GL_VIEWPORT, viewport, 0 );
                    int widthPixels = viewport[2];
                    int heightPixels = viewport[3];
                    float widthClip = radiusPixels * 2.0f / widthPixels;
                    float heightClip = radiusPixels * 2.0f / heightPixels;
                    this.getArg( "radiusX" ).setValue( widthClip );
                    this.getArg( "radiusY" ).setValue( heightClip );
                }

                this.getArg( "solid" ).setValue( solid );
            }
        };
    }

    public static SimpleGeometryShader linesToVariableSizeNGons( final boolean solid, final int N )
    {
        return new SimpleGeometryShader( "variable_ngons" + N, "shaders/geometry/variable_n_gon.gs", GL3.GL_LINES, solid ? GL3.GL_TRIANGLE_STRIP : GL3.GL_LINE_STRIP, solid ? 3 * ( N - 1 ) : N + 1 )
        {
            public void preDisplay( GL gl )
            {
                this.getArg( "N" ).setValue( N );
                this.getArg( "solid" ).setValue( solid );
            }
        };
    }

    public static SimpleGeometryShader linesToXs( )
    {
        return new SimpleGeometryShader( "xs", "shaders/geometry/x.gs", GL3.GL_LINES, GL3.GL_LINE_STRIP, 5 );
    }

    @Override
    public boolean preLink( GL gl, int glProgramHandle )
    {
        GL3 gl3 = gl.getGL3( );

        gl3.glProgramParameteriARB( glProgramHandle, GL3.GL_GEOMETRY_INPUT_TYPE_ARB, inType );
        logGlError( gl3 );
        gl3.glProgramParameteriARB( glProgramHandle, GL3.GL_GEOMETRY_OUTPUT_TYPE_ARB, outType );
        logGlError( gl3 );
        gl3.glProgramParameteriARB( glProgramHandle, GL3.GL_GEOMETRY_VERTICES_OUT_ARB, maxVertices );
        logGlError( gl3 );

        return true;
    }

}
