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

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;

import com.jogamp.opengl.GL2ES2;

public class DncAreaProgram
{

    public static final String dncAreaVertShader_GLSL = requireResourceText( "shaders/DncPainter/area.vs" );
    public static final String dncAreaFragShader_GLSL = requireResourceText( "shaders/DncPainter/area.fs" );


    public static class DncAreaProgramHandles
    {
        public final int program;

        public final int AXIS_RECT;
        public final int RGBA;
        public final int HIGHLIGHT_SET;

        public final int inAreaVertex;

        public DncAreaProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, dncAreaVertShader_GLSL, null, dncAreaFragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );

            this.RGBA = gl.glGetUniformLocation( program, "RGBA" );

            this.HIGHLIGHT_SET = gl.glGetUniformLocation( program, "HIGHLIGHT_SET" );

            this.inAreaVertex = gl.glGetAttribLocation( program, "inAreaVertex" );
        }
    }


    protected DncAreaProgramHandles handles;


    public DncAreaProgram( )
    {
        this.handles = null;
    }

    /**
     * Returns the raw GL handles for the shader program, uniforms, and attributes. Compiles and
     * links the program, if necessary.
     */
    public DncAreaProgramHandles handles( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new DncAreaProgramHandles( gl );
        }

        return this.handles;
    }

    /**
     * Deletes the program, and resets this object to the way it was before {@link #handles(GL2ES2)}
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
