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
package com.metsci.glimpse.gl.shader;

import static com.metsci.glimpse.util.logging.LoggerUtils.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL2;

public class GLShaderUtils
{
    public static void logShaderArgs( Logger logger, Level level, ShaderArg[] args, String prefix )
    {
        if ( args == null || args.length == 0 )
        {
            logger.log( level, "%sNo shader args.", prefix );
            return;
        }

        log( logger, level, "%s%d shader args found.", prefix, args.length );
        for ( ShaderArg arg : args )
        {
            log( logger, level, "%sShader args found: %s", prefix, arg.toString( ) );
        }
    }

    public static boolean logGLProgramInfoLog( Logger logger, GL2 gl, int glProgramHandle, String name )
    {
        int[] status = new int[1];
        gl.glGetProgramiv( glProgramHandle, GL2.GL_LINK_STATUS, status, 0 );

        if ( status[0] != GL2.GL_TRUE )
        {
            logger.warning( "LINKING FAILED: " + name + " did not link properly." );
            logger.warning( getGLProgramInfoLog( gl, glProgramHandle ) );
            return false;
        }

        logger.fine( name + " linked." );
        return true;
    }

    public static boolean logGLShaderInfoLog( Logger logger, GL2 gl, int glShaderHandle, String name )
    {
        int[] status = new int[1];
        gl.glGetShaderiv( glShaderHandle, GL2.GL_COMPILE_STATUS, status, 0 );

        if ( status[0] != GL2.GL_TRUE )
        {
            logger.warning( "COMPILATION FAILED: " + name + " did not compile." );
            logger.warning( logGLShaderInfoLog( gl, glShaderHandle ) );
            return false;
        }

        logger.fine( "Shader " + name + " compiled." );
        return true;
    }

    public static String logGLShaderInfoLog( GL2 gl, int glShaderHandle )
    {
        int[] logLength = new int[1];
        gl.glGetShaderiv( glShaderHandle, GL2.GL_INFO_LOG_LENGTH, logLength, 0 );

        if ( logLength[0] == 0 )
        {
            return "No message found!";
        }

        int[] log1 = new int[logLength[0]];
        byte[] log2 = new byte[logLength[0]];

        gl.glGetShaderInfoLog( glShaderHandle, logLength[0], log1, 0, log2, 0 );

        char[] msg = new char[log2.length];
        for ( int i = 0; i < log2.length; i++ )
            msg[i] = ( char ) log2[i];

        return String.valueOf( msg );
    }

    public static String getGLProgramInfoLog( GL2 gl, int glProgramHandle )
    {
        int[] logLength = new int[1];
        gl.glGetProgramiv( glProgramHandle, GL2.GL_INFO_LOG_LENGTH, logLength, 0 );

        if ( logLength[0] == 0 )
        {
            return "No message found!";
        }

        int[] log1 = new int[logLength[0]];
        byte[] log2 = new byte[logLength[0]];

        gl.glGetProgramInfoLog( glProgramHandle, logLength[0], log1, 0, log2, 0 );

        char[] msg = new char[log2.length];
        for ( int i = 0; i < log2.length; i++ )
            msg[i] = ( char ) log2[i];

        return String.valueOf( msg );
    }
}
