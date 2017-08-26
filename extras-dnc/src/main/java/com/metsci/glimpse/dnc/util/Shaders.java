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
package com.metsci.glimpse.dnc.util;

import static com.google.common.base.Charsets.UTF_8;
import static com.metsci.glimpse.util.GeneralUtils.array;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_INFO_LOG_LENGTH;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL3.GL_GEOMETRY_SHADER;
import static jogamp.opengl.glu.error.Error.gluErrorString;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;

import com.metsci.glimpse.util.primitives.IntsArray;

public class Shaders
{

    public static int createProgram( GL2ES2 gl, String vertSource, String geomSource, String fragSource )
    {
        IntsArray shaders = new IntsArray( );
        try
        {
            if ( vertSource != null ) shaders.append( compileShader( gl, GL_VERTEX_SHADER,   vertSource ) );
            if ( geomSource != null ) shaders.append( compileShader( gl, GL_GEOMETRY_SHADER, geomSource ) );
            if ( fragSource != null ) shaders.append( compileShader( gl, GL_FRAGMENT_SHADER, fragSource ) );
            return linkProgram( gl, shaders.a );
        }
        finally
        {
            for ( int i = 0; i < shaders.n; i++ )
            {
                gl.glDeleteShader( shaders.v( i ) );
            }
        }
    }

    public static int compileShader( GL2ES2 gl, int shaderType, String source )
    {
        return compileShader( gl, shaderType, array( source ) );
    }

    public static int compileShader( GL2ES2 gl, int shaderType, String[] sources )
    {
        int shader = gl.glCreateShader( shaderType );
        gl.glShaderSource( shader, 1, sources, null );

        gl.glCompileShader( shader );
        int[] compileStatus = new int[ 1 ];
        gl.glGetShaderiv( shader, GL_COMPILE_STATUS, compileStatus, 0 );
        if ( compileStatus[ 0 ] != GL_TRUE ) throw new RuntimeException( getShaderInfoLog( gl, shader ) );

        return shader;
    }

    public static String getShaderInfoLog( GL2ES2 gl, int shader )
    {
        int[] maxLength = new int[ 1 ];
        gl.glGetShaderiv( shader, GL_INFO_LOG_LENGTH, maxLength, 0 );
        if ( maxLength[ 0 ] == 0 ) return "";

        int[] length = new int[ 1 ];
        byte[] bytes = new byte[ maxLength[ 0 ] ];
        gl.glGetShaderInfoLog( shader, maxLength[ 0 ], length, 0, bytes, 0 );
        return new String( bytes, 0, length[ 0 ], UTF_8 );
    }

    public static void requireNoErrors( GL gl )
    {
        int error = gl.glGetError( );
        if ( error != GL_NO_ERROR )
        {
            throw new RuntimeException( gluErrorString( error ) );
        }
    }

    public static int linkProgram( GL2ES2 gl, int... shaders )
    {
        int program = gl.glCreateProgram( );
        for ( int s : shaders ) gl.glAttachShader( program, s );
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
            for ( int s : shaders ) gl.glDetachShader( program, s );
        }
    }

    public static String getProgramInfoLog( GL2ES2 gl, int program )
    {
        int[] maxLength = new int[ 1 ];
        gl.glGetProgramiv( program, GL_INFO_LOG_LENGTH, maxLength, 0 );
        if ( maxLength[ 0 ] == 0 ) return "";

        int[] length = new int[ 1 ];
        byte[] bytes = new byte[ maxLength[ 0 ] ];
        gl.glGetProgramInfoLog( program, maxLength[ 0 ], length, 0, bytes, 0 );
        return new String( bytes, 0, length[ 0 ], UTF_8 );
    }

}
