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

import static com.metsci.glimpse.gl.shader.GLShaderUtils.logGLShaderInfoLog;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.logShaderArgs;
import static com.metsci.glimpse.gl.shader.ShaderArgInOut.IN;
import static com.metsci.glimpse.gl.shader.ShaderArgQualifier.UNIFORM;
import static java.util.logging.Level.FINE;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import com.metsci.glimpse.util.io.StreamOpener;

/**
 * A wrapper for an OpenGL geometry, vertex, or fragment shader. Handles
 * compiling the shader inside the OpenGL context, automatically
 * recognizing uniform variables and providing handles via
 * {@link ShaderArg} objects. {@code Shaders} may be combined via
 * a {@link Pipeline} into a rendering pipeline which may be used
 * by a {@link com.metsci.glimpse.painter.base.GlimpsePainter} which
 * desires shaders to be active during its rendering.
 *
 * @author osborn
 */
public abstract class Shader
{
    private static final Logger logger = Logger.getLogger( Shader.class.getName( ) );

    private final String name;
    private final ShaderType type;

    private final ShaderSource[] sources;
    private final ShaderArg[] args;

    private int[] glShaderHandles;
    private int[] glArgHandles;

    public static ShaderSource[] getSource( String... shaderFile )
    {
        ShaderSource[] list = new ShaderSource[shaderFile.length];

        try
        {
            for ( int i = 0; i < shaderFile.length; i++ )
            {
                list[i] = new ShaderSource( shaderFile[i], StreamOpener.fileThenResource );
            }

            return list;
        }
        catch ( IOException ioe )
        {
            throw new RuntimeException( ioe );
        }
    }

    public Shader( String name, ShaderType type, String... source )
    {
        this( name, type, false, getSource( source ) );
    }

    public Shader( String name, ShaderType type, ShaderSource... source )
    {
        this( name, type, false, source );
    }

    public Shader( String name, ShaderType type, boolean noParse, String... source )
    {
        this( name, type, noParse, getSource( source ) );
    }

    // our parser is intended for OpenGL ES, so it doesn't parse full GLSL
    // the noParse argument is provided as a temporary solution for situations
    // where the parser does not yet understand the shader
    public Shader( String name, ShaderType type, boolean noParse, ShaderSource... source )
    {
        if ( noParse )
        {
            this.args = new ShaderArg[0];
        }
        else
        {
            this.args = verify( type, source );
        }

        this.name = name;
        this.type = type;
        this.sources = source;

        logShaderArgs( logger, FINE, args, toString( ) + ": " );
    }

    private static ShaderArg[] verify( ShaderType type, ShaderSource... source )
    {
        if ( type == null ) throw new GLException( "Shader type not specified." );

        if ( source == null || source.length == 0 ) throw new GLException( "Shader source code not present." );

        // TODO: Make this work with multiple source files
        List<ShaderArg> args = source[0].extractArgs( );
        return args.toArray( new ShaderArg[0] );
    }

    protected ShaderArg getArg( String name )
    {
        // TODO: Hello, Map!
        for ( int i = 0; i < args.length; i++ )
            if ( args[i].getName( ).contentEquals( name ) ) return args[i];

        return null;
    }

    public String getName( )
    {
        return name;
    }

    public ShaderType getType( )
    {
        return type;
    }

    /**
     * Called right after the program has been compiled, but before it has been
     * linked.
     *
     * @return false if any problems specific to this shader are found, true
     *         otherwise
     */
    public abstract boolean preLink( GL gl, int glProgramHandle );

    /**
     * Called right after this shader is made current as part of the pipeline.
     * Use this method to update uniform variables for this shader.
     */
    public abstract void preDisplay( GL gl );

    /**
     * Called right after rendering is complete, just after reverting to the
     * fixed pipeline functionality. Use this method for any necessary cleanup
     * specific to this shader.
     */
    public abstract void postDisplay( GL gl );

    @Override
    public String toString( )
    {
        return "'" + getType( ).toString( ).toUpperCase( ) + " SHADER " + getName( ) + "'";
    }

    /**
     * Called by the pipeline.
     *
     * @return true if the compilation completes without error, false otherwise
     */
    protected boolean compileAndAttach( GL gl, int glProgramHandle )
    {
        GL2 gl2 = gl.getGL2( );

        int segmentIndex = 0;
        glShaderHandles = new int[sources.length];
        for ( ShaderSource segment : sources )
        {
            int handle = gl2.glCreateShader( type.glTypeCode( ) );
            glShaderHandles[segmentIndex++] = handle;

            gl2.glShaderSource( handle, 1, segment.getSourceLines( ), null );
            gl2.glCompileShader( handle );

            boolean success = logGLShaderInfoLog( logger, gl2, handle, toString( ) );

            // TODO: Clean up if compilation fails.
            if ( !success ) return false;
        }

        for ( int i = 0; i < glShaderHandles.length; i++ )
        {
            logger.fine( "Attached " + toString( ) + " to GL program handle " + glProgramHandle + "." );
            gl2.glAttachShader( glProgramHandle, glShaderHandles[i] );
        }

        return preLink( gl, glProgramHandle );
    }

    // TODO: Handle errors in this method.
    /**
     * Called by the pipeline.
     *
     * @return true if all goes well
     */
    protected boolean getShaderArgHandles( GL gl, int glProgramHandle )
    {
        GL2 gl2 = gl.getGL2( );

        glArgHandles = new int[args.length];

        for ( int i = 0; i < args.length; i++ )
        {
            ShaderArg arg = args[i];
            if ( arg.getQual( ) == UNIFORM )
            {
                glArgHandles[i] = gl2.glGetUniformLocation( glProgramHandle, arg.getName( ) );
            }
            else if ( arg.getInOut( ) == IN )
            {
                glArgHandles[i] = gl2.glGetAttribLocation( glProgramHandle, arg.getName( ) );
            }
        }

        return true;
    }

    /**
     * Called by the pipeline.
     */
    protected void updateArgValues( GL gl )
    {
        GL2 gl2 = gl.getGL2( );

        for ( int i = 0; i < args.length; i++ )
        {
            ShaderArg arg = args[i];
            if ( arg.getQual( ) == UNIFORM )
            {
                arg.update( gl2, glArgHandles[i] );
            }
        }
    }

    public void dispose( GLContext context )
    {
        GL2 gl = context.getGL( ).getGL2( );

        if ( glShaderHandles != null )
        {
            for ( int handle : glShaderHandles )
                gl.glDeleteShader( handle );
        }
    }
}
