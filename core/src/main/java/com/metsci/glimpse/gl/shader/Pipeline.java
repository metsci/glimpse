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

import static com.metsci.glimpse.gl.shader.GLShaderUtils.logGLProgramInfoLog;

import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

/**
 * A structure wrapping a set of three OpenGL shaders (a geometry
 * shader, a vertex shader, and a fragment shader) into a complete
 * rendering pipeline. This class can be passed to painter which
 * use shaders as part of their rendering. The pipeline methods
 * {@code beginUse()} and {@code endUse()} can be used in the
 * {@link com.metsci.glimpse.painter.base.GlimpsePainter} {@code paintTo()}
 * method to turn on and off use of the {@code Pipeline}.
 *
 * @author osborn
 * @see com.metsci.glimpse.painter.texture.ShadedTexturePainter
 */
public class Pipeline
{
    private static final Logger logger = Logger.getLogger( Pipeline.class.getName( ) );

    private final String name;
    private Shader geometryShader;
    private Shader vertexShader;
    private Shader fragmentShader;

    private int glProgramHandle;
    private boolean isEmpty;
    private boolean isDirty;
    private boolean isLinked;

    public static final Pipeline empty = new Pipeline( "null", null, null, null );

    public Pipeline( String name, Shader geom, Shader vert, Shader frag )
    {
        verify( geom, vert, frag );

        this.name = name;
        geometryShader = geom;
        vertexShader = vert;
        fragmentShader = frag;

        isDirty = true;
        if ( geom == null && vert == null && frag == null )
        {
            isEmpty = true;
            isDirty = false;
            isLinked = true;
        }
    }

    private static void verify( Shader geom, Shader vert, Shader frag )
    {
        if ( geom != null && vert == null ) throw new GLException( "Geometry shader present w/o accompanying vertex shader." );

        if ( ( geom != null && geom.getType( ) != ShaderType.geometry ) || ( vert != null && vert.getType( ) != ShaderType.vertex ) || ( frag != null && frag.getType( ) != ShaderType.fragment ) ) throw new IllegalArgumentException( "Incorrect shader type supplied in pipeline construction." );
    }

    public String getName( )
    {
        return name;
    }

    @Override
    public String toString( )
    {
        return "'PIPELINE " + getName( ) + "'";
    }

    public boolean isLinked( GL gl )
    {
        return isLinked;
    }

    public void beginUse( GL2 gl )
    {
        if ( isEmpty ) return;

        if ( isDirty ) isLinked = compileAndLink( gl );

        // In the future we can support replacing pieces of the pipeline, even
        // functions inside the shaders, and relinking on the fly. For now,
        // we only init once.
        isDirty = false;

        if ( !isLinked ) return;

        gl.glUseProgram( glProgramHandle );

        if ( vertexShader != null )
        {
            vertexShader.preDisplay( gl );
            vertexShader.updateArgValues( gl );
        }

        if ( geometryShader != null )
        {
            geometryShader.preDisplay( gl );
            geometryShader.updateArgValues( gl );
        }

        if ( fragmentShader != null )
        {
            fragmentShader.preDisplay( gl );
            fragmentShader.updateArgValues( gl );
        }
    }

    public void endUse( GL2 gl )
    {
        if ( isEmpty ) return;

        if ( !isLinked ) return;

        if ( fragmentShader != null ) fragmentShader.postDisplay( gl );

        if ( geometryShader != null ) geometryShader.postDisplay( gl );

        if ( vertexShader != null ) vertexShader.postDisplay( gl );

        gl.glUseProgram( 0 );
    }

    // TODO: Clean up when compilation fails inside this method
    private boolean compileAndLink( GL2 gl )
    {
        if ( isEmpty ) return true;

        isLinked = false;
        logger.fine( "Compiling " + toString( ) + "..." );

        glProgramHandle = gl.glCreateProgram( );

        if ( geometryShader != null )
        {
            if ( !geometryShader.compileAndAttach( gl, glProgramHandle ) ) return false;
        }

        if ( vertexShader != null )
        {
            if ( !vertexShader.compileAndAttach( gl, glProgramHandle ) ) return false;
        }

        if ( fragmentShader != null )
        {
            if ( !fragmentShader.compileAndAttach( gl, glProgramHandle ) ) return false;
        }

        logger.fine( "Linking " + toString( ) + "..." );
        gl.glLinkProgram( glProgramHandle );

        boolean success = logGLProgramInfoLog( logger, gl, glProgramHandle, toString( ) );
        if ( !success )
        {
            return false;
        }
        else
        {
            isLinked = true;

            if ( geometryShader != null ) geometryShader.getShaderArgHandles( gl, glProgramHandle );

            if ( vertexShader != null ) vertexShader.getShaderArgHandles( gl, glProgramHandle );

            if ( fragmentShader != null ) fragmentShader.getShaderArgHandles( gl, glProgramHandle );

            return true;
        }
    }

    public void dispose( GLContext context )
    {
        GL2 gl = context.getGL( ).getGL2( );

        gl.glDeleteProgram( glProgramHandle );

        if ( geometryShader != null ) geometryShader.dispose( context );
        if ( vertexShader != null ) vertexShader.dispose( context );
        if ( fragmentShader != null ) fragmentShader.dispose( context );
    }
}
