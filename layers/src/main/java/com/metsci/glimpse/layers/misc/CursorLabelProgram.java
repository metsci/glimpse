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
package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static com.metsci.glimpse.gl.util.GLUtils.defaultVertexAttributeArray;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLE_STRIP;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;

import com.jogamp.opengl.util.texture.Texture;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLEditableBuffer;

public class CursorLabelProgram
{

    public static final String cursorLabelVertShader_GLSL = requireResourceText( "CursorLabelProgram/cursorLabel.vs" );
    public static final String cursorLabelFragShader_GLSL = requireResourceText( "CursorLabelProgram/cursorLabel.fs" );


    public static class CursorLabelProgramHandles
    {
        public final int program;

        public final int VIEWPORT_SIZE_PX;
        public final int IMAGE;

        public final int inSt;
        public final int inXy;

        public CursorLabelProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, cursorLabelVertShader_GLSL, null, cursorLabelFragShader_GLSL );

            this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( program, "VIEWPORT_SIZE_PX" );
            this.IMAGE = gl.glGetUniformLocation( program, "IMAGE" );

            this.inSt = gl.glGetAttribLocation( program, "inSt" );
            this.inXy = gl.glGetAttribLocation( program, "inXy" );
        }
    }


    protected CursorLabelProgramHandles handles;

    public CursorLabelProgram( )
    {
        this.handles = null;
    }

    /**
     * Returns the raw GL handles for the shader program, uniforms, and attributes. Compiles and
     * links the program, if necessary.
     */
    public CursorLabelProgramHandles handles( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new CursorLabelProgramHandles( gl );
        }
        return this.handles;
    }

    public void begin( GL2ES3 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new CursorLabelProgramHandles( gl );
        }

        gl.glBindVertexArray( defaultVertexAttributeArray( gl ) );
        gl.glUseProgram( this.handles.program );
        gl.glEnableVertexAttribArray( this.handles.inSt );
        gl.glEnableVertexAttribArray( this.handles.inXy );
    }

    public void setViewport( GL2ES2 gl, GlimpseBounds bounds )
    {
        this.setViewport( gl, bounds.getWidth( ), bounds.getHeight( ) );
    }

    public void setViewport( GL2ES2 gl, int viewportWidth, int viewportHeight )
    {
        gl.glUniform2f( this.handles.VIEWPORT_SIZE_PX, viewportWidth, viewportHeight );
    }

    public void draw( GL2ES3 gl, Texture texture, GLEditableBuffer stBuffer, GLEditableBuffer xyBuffer )
    {
        draw( gl, texture, stBuffer, xyBuffer, 0, stBuffer.sizeFloats( ) / 2 );
    }

    public void draw( GL2ES3 gl, Texture texture, GLEditableBuffer stBuffer, GLEditableBuffer xyBuffer, int first, int count )
    {
        if ( texture.getTarget( ) != GL_TEXTURE_2D )
        {
            throw new RuntimeException( "Texture has wrong target: required = GL_TEXTURE_2D, found = " + texture.getTarget( ) );
        }
        gl.glActiveTexture( GL_TEXTURE0 );
        texture.bind( gl );
        gl.glUniform1i( this.handles.IMAGE, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, stBuffer.deviceBuffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inSt, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, xyBuffer.deviceBuffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( GL_TRIANGLE_STRIP, first, count );
    }

    public void end( GL2ES3 gl )
    {
        gl.glDisableVertexAttribArray( this.handles.inSt );
        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glUseProgram( 0 );
        gl.glBindVertexArray( 0 );
    }

    /**
     * Deletes the program, and resets this object to the way it was before {@link #begin(GL2ES2)}
     * was first called.
     * <p>
     * This object can be safely reused after being disposed, but in most cases there is no
     * significant advantage to doing so.
     */
    public void dispose( GL2ES2 gl )
    {
        if ( this.handles != null )
        {
            gl.glDeleteProgram( this.handles.program );
            this.handles = null;
        }
    }

}
