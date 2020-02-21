/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.support.shader.point;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.metsci.glimpse.core.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.core.gl.util.GLUtils.disablePointSprite;
import static com.metsci.glimpse.core.gl.util.GLUtils.enablePointSprite;
import static com.metsci.glimpse.core.support.wrapped.Wrapper2D.NOOP_WRAPPER_2D;
import static com.metsci.glimpse.util.io.IoUtils.requireText;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.context.GlimpseBounds;
import com.metsci.glimpse.core.gl.GLEditableBuffer;
import com.metsci.glimpse.core.gl.GLStreamingBuffer;
import com.metsci.glimpse.core.gl.util.GLUtils;
import com.metsci.glimpse.core.support.wrapped.Wrapper2D;

/**
 * Draws 2D point with feathered edges, a constant pixel radius, and a constant color.
 */
public class PointFlatColorProgram
{
    public static final String vertShader_GLSL = requireText( PointFlatColorProgram.class.getResource( "point_flat_color/point.vs" ) );
    public static final String geomShader_GLSL = requireText( PointFlatColorProgram.class.getResource( "point_flat_color/point.gs" ) );
    public static final String fragShader_GLSL = requireText( PointFlatColorProgram.class.getResource( "point_flat_color/point.fs" ) );

    public static class ProgramHandles
    {
        public final int program;

        // Uniforms

        public final int AXIS_RECT;
        public final int WRAP_RECT;
        public final int VIEWPORT_SIZE_PX;

        public final int POINT_SIZE_PX;
        public final int FEATHER_THICKNESS_PX;
        public final int RGBA;

        // Vertex attributes

        public final int inXy;

        public ProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, vertShader_GLSL, geomShader_GLSL, fragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( this.program, "AXIS_RECT" );
            this.WRAP_RECT = gl.glGetUniformLocation( program, "WRAP_RECT" );
            this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( program, "VIEWPORT_SIZE_PX" );

            this.POINT_SIZE_PX = gl.glGetUniformLocation( this.program, "POINT_SIZE_PX" );
            this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( this.program, "FEATHER_THICKNESS_PX" );

            this.RGBA = gl.glGetUniformLocation( this.program, "RGBA" );
            this.inXy = gl.glGetAttribLocation( this.program, "inXy" );

            // set defaults
            gl.glUseProgram( this.program );
            gl.glUniform1f( this.FEATHER_THICKNESS_PX, 0.8f );
            gl.glUniform1f( this.POINT_SIZE_PX, 3.0f );
        }
    }

    protected ProgramHandles handles;

    public PointFlatColorProgram( )
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

        gl.glEnable( GL3.GL_PROGRAM_POINT_SIZE );
        enablePointSprite( gl );

        // Init uniforms to defaults -- may be overridden by later calls
        this.setWrapper( gl, NOOP_WRAPPER_2D );
    }

    public void setRgba( GL2ES2 gl, float[] rgba )
    {
        gl.glUniform4fv( this.handles.RGBA, 1, rgba, 0 );
    }

    public void setFeatherThickness( GL2ES2 gl, float value )
    {
        gl.glUniform1f( this.handles.FEATHER_THICKNESS_PX, value );
    }

    public void setPointSize( GL2ES2 gl, float value )
    {
        gl.glUniform1f( this.handles.POINT_SIZE_PX, value );
    }

    public void setViewport( GL2ES2 gl, GlimpseBounds bounds )
    {
        this.setViewport( gl, bounds.getWidth( ), bounds.getHeight( ) );
    }

    public void setViewport( GL2ES2 gl, int viewportWidth, int viewportHeight )
    {
        gl.glUniform2f( this.handles.VIEWPORT_SIZE_PX, viewportWidth, viewportHeight );
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

    public void setWrapper( GL2ES2 gl, Wrapper2D wrapper )
    {
        this.setWrapper( gl, ( float ) wrapper.x.wrapMin( ), ( float ) wrapper.x.wrapMax( ), ( float ) wrapper.y.wrapMin( ), ( float ) wrapper.y.wrapMax( ) );
    }

    public void setWrapper( GL2ES2 gl, float xMin, float xMax, float yMin, float yMax )
    {
        gl.glUniform4f( this.handles.WRAP_RECT, xMin, xMax, yMin, yMax );
    }

    public void draw( GL2ES2 gl, GLStreamingBuffer xyVbo, int first, int count )
    {
        draw( gl, GL.GL_POINTS, xyVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, GLStreamingBuffer xyVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo.buffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, int mode, int xyVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, GLEditableBuffer xy )
    {
        draw( gl, GL_POINTS, xy.deviceBuffer( gl ), 0, xy.sizeFloats( ) / 2 );
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glUseProgram( 0 );

        gl.glDisable( GL3.GL_PROGRAM_POINT_SIZE );
        disablePointSprite( gl );

        gl.getGL3( ).glBindVertexArray( 0 );
    }

    public void dispose( GL2ES2 gl )
    {
        if ( this.handles != null )
        {
            gl.glDeleteProgram( this.handles.program );
            this.handles = null;
        }
    }
}
