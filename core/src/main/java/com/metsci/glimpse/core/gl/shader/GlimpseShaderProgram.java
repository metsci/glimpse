/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.core.gl.shader;

import static com.metsci.glimpse.util.GeneralUtils.array;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import com.google.common.collect.Lists;
import com.jogamp.common.net.Uri;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLArrayData;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLUniformData;
import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import com.metsci.glimpse.core.gl.util.GLUtils;

/**
 * Simple Glimpse-specific convenience wrapper around {@link ShaderProgram}.
 *
 * @author ulman
 */
public class GlimpseShaderProgram
{
    protected Collection<ShaderCode> codes;
    protected Collection<GLUniformData> uniforms;
    protected Collection<GLArrayDataClient> arrays;

    protected ShaderProgram program;
    protected ShaderState state;

    protected boolean loaded;

    public GlimpseShaderProgram( )
    {
        this.codes = Lists.newArrayList( );
        this.uniforms = Lists.newArrayList( );
        this.arrays = Lists.newArrayList( );
    }

    public ShaderProgram getShaderProgram( )
    {
        return this.program;
    }

    public void loadProgram( GL gl )
    {
        this.load( gl.getGL3( ), this.codes );
    }

    public void useProgram( GL gl, boolean on )
    {
        if ( on )
        {
            gl.getGL3( ).glBindVertexArray( GLUtils.defaultVertexAttributeArray( gl ) );
        }

        if ( !this.load( gl.getGL3( ), this.codes ) ) return;

        this.state.useProgram( gl.getGL3( ), on );

        if ( on ) this.updateUniformData( gl );

        for ( GLArrayDataClient array : this.arrays )
        {
            if ( array.sealed( ) )
            {
                array.enableBuffer( gl, on );
            }
        }

        this.doUseProgram( gl, on );

        if ( !on )
        {
            gl.getGL3( ).glBindVertexArray( 0 );
        }
    }

    protected void doUseProgram( GL gl, boolean on )
    {
        // do nothing by default
    }

    public ShaderCode addFragmentShader( URL sourceUrl )
    {
        return this.addShader( GL3.GL_FRAGMENT_SHADER, sourceUrl );
    }

    public ShaderCode addVertexShader( URL sourceUrl )
    {
        return this.addShader( GL3.GL_VERTEX_SHADER, sourceUrl );
    }

    public ShaderCode addGeometryShader( URL sourceUrl )
    {
        return this.addShader( GL3.GL_GEOMETRY_SHADER, sourceUrl );
    }

    public ShaderCode addShader( int type, URL sourceUrl )
    {
        try
        {
            Uri sourceUri = Uri.valueOf( sourceUrl );
            return this.addShader( ShaderCode.create( null, type, 1, array( sourceUri ), true ) );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( e );
        }
    }

    public ShaderCode addShader( ShaderCode code )
    {
        this.codes.add( code );
        return code;
    }

    public GLArrayDataClient addArrayData( GLArrayDataClient array )
    {
        this.arrays.add( array );
        return array;
    }

    public GLUniformData addUniformData( GLUniformData uniform )
    {
        this.uniforms.add( uniform );
        return uniform;
    }

    public void dispose( GLContext context )
    {
        if ( this.loaded )
        {
            this.state.destroy( context.getGL( ).getGL3( ) );
            this.loaded = false;
        }
    }

    protected void updateUniformData( GL gl )
    {
        GL3 gl3 = gl.getGL3( );

        for ( GLUniformData uniform : this.uniforms )
        {
            this.state.uniform( gl3, uniform );
        }
    }

    protected boolean load( GL gl, Collection<ShaderCode> codes )
    {
        if ( this.loaded ) return true;

        GL3 gl3 = gl.getGL3( );

        this.state = new ShaderState( );
        this.state.setVerbose( false );
        this.program = new ShaderProgram( );

        for ( ShaderCode code : codes )
        {
            boolean success = this.program.add( gl3, code, System.err );

            if ( !success )
            {
                return false;
            }
        }

        this.state.attachShaderProgram( gl3, this.program, true );

        for ( GLArrayData array : this.arrays )
        {
            this.state.ownAttribute( array, true );
        }

        for ( GLUniformData uniform : this.uniforms )
        {
            this.state.ownUniform( uniform );
        }

        this.loaded = true;

        return true;
    }
}
