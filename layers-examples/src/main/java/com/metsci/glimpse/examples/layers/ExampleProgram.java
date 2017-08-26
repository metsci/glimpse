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
package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static com.metsci.glimpse.gl.util.GLUtils.defaultVertexAttributeArray;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_POINTS;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLEditableBuffer;

public class ExampleProgram
{

    public static final String exampleVertShader_GLSL = requireResourceText( "ExampleProgram/example.vs" );
    public static final String exampleGeomShader_GLSL = requireResourceText( "ExampleProgram/example.gs" );
    public static final String exampleFragShader_GLSL = requireResourceText( "ExampleProgram/example.fs" );


    public static class ExampleProgramHandles
    {
        public final int program;

        public final int TIMELINE_MODE;

        public final int AXIS_RECT;
        public final int VIEWPORT_SIZE_PX;
        public final int FEATHER_THICKNESS_PX;

        public final int RGBA_INSIDE_T_WINDOW;
        public final int RGBA_OUTSIDE_T_WINDOW;
        public final int T_WINDOW_MIN;
        public final int T_WINDOW_MAX;

        public final int POINT_SIZE_INSIDE_XY_WINDOW_PX;
        public final int POINT_SIZE_OUTSIDE_XY_WINDOW_PX;
        public final int XY_WINDOW_MIN;
        public final int XY_WINDOW_MAX;

        public final int inTxyz;

        public ExampleProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, exampleVertShader_GLSL, exampleGeomShader_GLSL, exampleFragShader_GLSL );

            this.TIMELINE_MODE = gl.glGetUniformLocation( program, "TIMELINE_MODE" );

            this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );
            this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( program, "VIEWPORT_SIZE_PX" );
            this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( program, "FEATHER_THICKNESS_PX" );

            this.RGBA_INSIDE_T_WINDOW = gl.glGetUniformLocation( program, "RGBA_INSIDE_T_WINDOW" );
            this.RGBA_OUTSIDE_T_WINDOW = gl.glGetUniformLocation( program, "RGBA_OUTSIDE_T_WINDOW" );
            this.T_WINDOW_MIN = gl.glGetUniformLocation( program, "T_WINDOW_MIN" );
            this.T_WINDOW_MAX = gl.glGetUniformLocation( program, "T_WINDOW_MAX" );

            this.POINT_SIZE_INSIDE_XY_WINDOW_PX = gl.glGetUniformLocation( program, "POINT_SIZE_INSIDE_XY_WINDOW_PX" );
            this.POINT_SIZE_OUTSIDE_XY_WINDOW_PX = gl.glGetUniformLocation( program, "POINT_SIZE_OUTSIDE_XY_WINDOW_PX" );
            this.XY_WINDOW_MIN = gl.glGetUniformLocation( program, "XY_WINDOW_MIN" );
            this.XY_WINDOW_MAX = gl.glGetUniformLocation( program, "XY_WINDOW_MAX" );

            this.inTxyz = gl.glGetAttribLocation( program, "inTxyz" );
        }
    }


    protected ExampleProgramHandles handles;

    public ExampleProgram( )
    {
        this.handles = null;
    }

    /**
     * Returns the raw GL handles for the shader program, uniforms, and attributes. Compiles and
     * links the program, if necessary.
     */
    public ExampleProgramHandles handles( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new ExampleProgramHandles( gl );
        }
        return this.handles;
    }

    public void begin( GL2ES3 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new ExampleProgramHandles( gl );
        }

        gl.glBindVertexArray( defaultVertexAttributeArray( gl ) );
        gl.glUseProgram( this.handles.program );
        gl.glEnableVertexAttribArray( this.handles.inTxyz );
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
        this.setOrtho( gl, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
    }

    public void setPixelOrtho( GL2ES2 gl, GlimpseBounds bounds )
    {
        this.setOrtho( gl, 0, bounds.getWidth( ), 0, bounds.getHeight( ) );
    }

    public void setOrtho( GL2ES2 gl, float xMin, float xMax, float yMin, float yMax )
    {
        gl.glUniform4f( this.handles.AXIS_RECT, xMin, xMax, yMin, yMax );
    }

    public void setGeoMode( GL2ES2 gl )
    {
        gl.glUniform1i( this.handles.TIMELINE_MODE, 0 );
    }

    public void setTimelineMode( GL2ES2 gl )
    {
        gl.glUniform1i( this.handles.TIMELINE_MODE, 1 );
    }

    public void setStyle( GL2ES2 gl, ExampleStyle style, long renderTime_PMILLIS )
    {
        gl.glUniform1f( this.handles.FEATHER_THICKNESS_PX, style.feather_PX );

        gl.glUniform4fv( this.handles.RGBA_INSIDE_T_WINDOW, 1, style.rgbaInsideTWindow, 0 );
        gl.glUniform4fv( this.handles.RGBA_OUTSIDE_T_WINDOW, 1, style.rgbaOutsideTWindow, 0 );

        gl.glUniform1f( this.handles.POINT_SIZE_INSIDE_XY_WINDOW_PX, style.pointSizeInsideXyWindow_PX( renderTime_PMILLIS ) );
        gl.glUniform1f( this.handles.POINT_SIZE_OUTSIDE_XY_WINDOW_PX, style.pointSizeOutsideXyWindow_PX );
    }

    public void setWindow( GL2ES2 gl, float tMin, float tMax, float xMin, float xMax, float yMin, float yMax )
    {
        gl.glUniform1f( this.handles.T_WINDOW_MIN, tMin );
        gl.glUniform1f( this.handles.T_WINDOW_MAX, tMax );

        gl.glUniform2f( this.handles.XY_WINDOW_MIN, xMin, yMin );
        gl.glUniform2f( this.handles.XY_WINDOW_MAX, xMax, yMax );
    }

    public void draw( GL2ES3 gl, GLEditableBuffer txyzBuffer )
    {
        draw( gl, txyzBuffer, 0, txyzBuffer.sizeFloats( ) / 4 );
    }

    public void draw( GL2ES3 gl, GLEditableBuffer txyzBuffer, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, txyzBuffer.deviceBuffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inTxyz, 4, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( GL_POINTS, first, count );
    }

    public void end( GL2ES3 gl )
    {
        gl.glDisableVertexAttribArray( this.handles.inTxyz );
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
