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
package com.metsci.glimpse.support.shader.triangle;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_FLOAT;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;

/**
 * Applies a flat color to filled triangles with 1d stippling.
 *
 * @see com.metsci.glimpse.painter.decoration.BorderPainter
 */
public class FlatColorStippleProgram
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/triangle/flat_color_stipple/flat_color_stipple.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/triangle/flat_color_stipple/flat_color_stipple.fs" );

    public static class ProgramHandles
    {
        public final int program;

        // Uniforms

        public final int AXIS_RECT;
        public final int RGBA;

        public final int FEATHER_THICKNESS_PX;

        public final int STIPPLE_ENABLE;
        public final int STIPPLE_SCALE;
        public final int STIPPLE_PATTERN;

        // Vertex attributes

        public final int inXy;
        public final int inMileage;

        public ProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( this.program, "AXIS_RECT" );
            this.RGBA = gl.glGetUniformLocation( this.program, "RGBA" );

            this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( program, "FEATHER_THICKNESS_PX" );

            this.STIPPLE_ENABLE = gl.glGetUniformLocation( program, "STIPPLE_ENABLE" );
            this.STIPPLE_SCALE = gl.glGetUniformLocation( program, "STIPPLE_SCALE" );
            this.STIPPLE_PATTERN = gl.glGetUniformLocation( program, "STIPPLE_PATTERN" );

            this.inXy = gl.glGetAttribLocation( this.program, "inXy" );
            this.inMileage = gl.glGetAttribLocation( this.program, "inMileage" );
        }
    }

    protected ProgramHandles handles;

    public FlatColorStippleProgram( )
    {
        this.handles = null;
    }

    public ProgramHandles handles( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new ProgramHandles( gl );
        }

        return this.handles;
    }

    public void begin( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new ProgramHandles( gl );
        }

        gl.getGL3( ).glBindVertexArray( GLUtils.defaultVertexAttributeArray( gl ) );
        gl.glUseProgram( this.handles.program );
        gl.glEnableVertexAttribArray( this.handles.inXy );
    }

    public void setFeatherThickness( GL2ES2 gl, float thickness )
    {
        gl.glUniform1f( this.handles.FEATHER_THICKNESS_PX, thickness );
    }

    public void setStipple( GL2ES2 gl, boolean enable, float scale, short pattern )
    {
        gl.glUniform1i( this.handles.STIPPLE_ENABLE, enable ? 1 : 0 );
        gl.glUniform1f( this.handles.STIPPLE_SCALE, scale );
        gl.glUniform1i( this.handles.STIPPLE_PATTERN, pattern );
    }

    public void setColor( GL2ES2 gl, float r, float g, float b, float a )
    {
        gl.glUniform4f( this.handles.RGBA, r, g, b, a );
    }

    public void setColor( GL2ES2 gl, float[] vRGBA )
    {
        gl.glUniform4fv( this.handles.RGBA, 1, vRGBA, 0 );
    }

    public void setAxisOrtho( GL2ES2 gl, Axis2D axis )
    {
        setOrtho( gl, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
    }

    public void setPixelOrtho( GL2ES2 gl, GlimpseBounds bounds )
    {
        setOrtho( gl, 0, bounds.getWidth( ), 0, bounds.getHeight( ) );
    }

    public void setOrtho( GL2ES2 gl, float xMin, float xMax, float yMin, float yMax )
    {
        gl.glUniform4f( this.handles.AXIS_RECT, xMin, xMax, yMin, yMax );
    }

    public void draw( GL2ES2 gl, GLEditableBuffer xyVbo, GLEditableBuffer mileageVbo, int first, int count )
    {
        draw( gl, GL.GL_TRIANGLES, xyVbo, mileageVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, GLEditableBuffer xyVbo, GLEditableBuffer mileageVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo.deviceBuffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, mileageVbo.deviceBuffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inMileage, 1, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, int mode, int xyVbo, int mileageVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, mileageVbo );
        gl.glVertexAttribPointer( this.handles.inMileage, 1, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, GLEditableBuffer xyVbo, GLEditableBuffer mileageVbo, float[] color )
    {
        setColor( gl, color );

        draw( gl, GL.GL_TRIANGLES, xyVbo, mileageVbo, 0, mileageVbo.sizeFloats( ) );
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glUseProgram( 0 );
        gl.getGL3( ).glBindVertexArray( 0 );
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
