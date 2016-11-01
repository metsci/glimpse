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
package com.metsci.glimpse.support.shader.line;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_BYTE;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL3.GL_LINE_STRIP_ADJACENCY;

import java.util.Collection;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.gl.util.GLUtils;

/**
 * Represents the shader program for drawing lines. The program gets compiled and
 * linked on the first call to either {@link #begin(GL2ES2)} or {@link #handles(GL2ES2)}.
 * <p>
 * This class could be extended to support multiple GL instances. Currently, however,
 * it assumes that each instance will only ever be used with a single GL instance.
 */
public class LineProgram
{
    public static final String lineVertShader_GLSL = requireResourceText( "shaders/line/line_flat_color/line.vs" );
    public static final String lineGeomShader_GLSL = requireResourceText( "shaders/line/line_flat_color/line.gs" );
    public static final String lineFragShader_GLSL = requireResourceText( "shaders/line/line_flat_color/line.fs" );

    public static class LineProgramHandles
    {
        public final int program;

        public final int AXIS_RECT;
        public final int VIEWPORT_SIZE_PX;

        public final int LINE_THICKNESS_PX;
        public final int FEATHER_THICKNESS_PX;
        public final int JOIN_TYPE;
        public final int MITER_LIMIT;

        public final int RGBA;
        public final int STIPPLE_ENABLE;
        public final int STIPPLE_SCALE;
        public final int STIPPLE_PATTERN;

        public final int inXy;
        public final int inFlags;
        public final int inMileage;

        public LineProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, lineVertShader_GLSL, lineGeomShader_GLSL, lineFragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );
            this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( program, "VIEWPORT_SIZE_PX" );

            this.LINE_THICKNESS_PX = gl.glGetUniformLocation( program, "LINE_THICKNESS_PX" );
            this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( program, "FEATHER_THICKNESS_PX" );
            this.JOIN_TYPE = gl.glGetUniformLocation( program, "JOIN_TYPE" );
            this.MITER_LIMIT = gl.glGetUniformLocation( program, "MITER_LIMIT" );

            this.RGBA = gl.glGetUniformLocation( program, "RGBA" );
            this.STIPPLE_ENABLE = gl.glGetUniformLocation( program, "STIPPLE_ENABLE" );
            this.STIPPLE_SCALE = gl.glGetUniformLocation( program, "STIPPLE_SCALE" );
            this.STIPPLE_PATTERN = gl.glGetUniformLocation( program, "STIPPLE_PATTERN" );

            this.inXy = gl.glGetAttribLocation( program, "inXy" );
            this.inFlags = gl.glGetAttribLocation( program, "inFlags" );
            this.inMileage = gl.glGetAttribLocation( program, "inMileage" );
        }
    }

    public static class LineBufferHandles
    {
        public final int inXy;
        public final int inFlags;
        public final int inMileage;

        public LineBufferHandles( int inXy, int inFlags, int inMileage )
        {
            this.inXy = inXy;
            this.inFlags = inFlags;
            this.inMileage = inMileage;
        }
    }

    protected LineProgramHandles handles;

    public LineProgram( )
    {
        this.handles = null;
    }

    /**
     * Returns the raw GL handles for the shader program, uniforms, and attributes. Compiles and
     * links the program, if necessary.
     * <p>
     * It is perfectly acceptable to use these handles directly, rather than calling the convenience
     * methods in this class. However, the convenience methods are intended to be a fairly stable API,
     * whereas the handles may change frequently.
     */
    public LineProgramHandles handles( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new LineProgramHandles( gl );
        }

        return this.handles;
    }

    public void begin( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new LineProgramHandles( gl );
        }

        gl.getGL3( ).glBindVertexArray( GLUtils.defaultVertexAttributeArray( gl ) );
        gl.glUseProgram( this.handles.program );
        gl.glEnableVertexAttribArray( this.handles.inXy );
        gl.glEnableVertexAttribArray( this.handles.inFlags );
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

    public void setStyle( GL2ES2 gl, LineStyle style )
    {
        gl.glUniform1f( this.handles.LINE_THICKNESS_PX, style.thickness_PX );
        gl.glUniform1f( this.handles.FEATHER_THICKNESS_PX, style.feather_PX );
        gl.glUniform1i( this.handles.JOIN_TYPE, style.joinType.value );
        gl.glUniform1f( this.handles.MITER_LIMIT, style.miterLimit );

        gl.glUniform4fv( this.handles.RGBA, 1, style.rgba, 0 );

        if ( style.stippleEnable )
        {
            gl.glUniform1i( this.handles.STIPPLE_ENABLE, 1 );
            gl.glUniform1f( this.handles.STIPPLE_SCALE, style.stippleScale );
            gl.glUniform1i( this.handles.STIPPLE_PATTERN, style.stipplePattern );
        }
        else
        {
            gl.glUniform1i( this.handles.STIPPLE_ENABLE, 0 );
        }
    }

    public void draw( GL2ES3 gl, LineStyle style, StreamingLinePath path )
    {
        this.setStyle( gl, style );
        this.draw( gl, path );
    }

    public void draw( GL2ES3 gl, StreamingLinePath path )
    {
        this.draw( gl, path.xyVbo, path.flagsVbo, path.mileageVbo, 0, path.numVertices( ) );
    }

    public void draw( GL2ES3 gl, LineStyle style, LinePath path )
    {
        this.draw( gl, style, path, 1.0 );
    }

    public void draw( GL2ES3 gl, LineStyle style, LinePath path, double ppvAspectRatio )
    {
        this.setStyle( gl, style );

        GLStreamingBuffer xyVbo = path.xyVbo( gl );
        GLStreamingBuffer flagsVbo = path.flagsVbo( gl );
        GLStreamingBuffer mileageVbo = ( style.stippleEnable ? path.mileageVbo( gl, ppvAspectRatio ) : null );

        this.draw( gl, xyVbo, flagsVbo, mileageVbo, 0, path.numVertices( ) );
    }

    public void draw( GL2ES3 gl, GLStreamingBuffer xyVbo, GLStreamingBuffer flagsVbo, GLStreamingBuffer mileageVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo.buffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glBindBuffer( GL_ARRAY_BUFFER, flagsVbo.buffer( gl ) );
        gl.glVertexAttribIPointer( this.handles.inFlags, 1, GL_BYTE, 0, flagsVbo.sealedOffset( ) );

        if ( mileageVbo != null )
        {
            gl.glEnableVertexAttribArray( this.handles.inMileage );
            gl.glBindBuffer( GL_ARRAY_BUFFER, mileageVbo.buffer( gl ) );
            gl.glVertexAttribPointer( this.handles.inMileage, 1, GL_FLOAT, false, 0, mileageVbo.sealedOffset( ) );
        }

        gl.glDrawArrays( GL_LINE_STRIP_ADJACENCY, first, count );

        if ( mileageVbo != null )
        {
            gl.glDisableVertexAttribArray( this.handles.inMileage );
        }
    }

    public void draw( GL2ES3 gl, int xyVbo, int flagsVbo, int mileageVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, flagsVbo );
        gl.glVertexAttribIPointer( this.handles.inFlags, 1, GL_BYTE, 0, 0 );

        gl.glEnableVertexAttribArray( this.handles.inMileage );
        gl.glBindBuffer( GL_ARRAY_BUFFER, mileageVbo );
        gl.glVertexAttribPointer( this.handles.inMileage, 1, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( GL_LINE_STRIP_ADJACENCY, first, count );

        gl.glDisableVertexAttribArray( this.handles.inMileage );
    }

    public void draw( GL2ES3 gl, LineStyle style, LineStrip strip, double ppvAspectRatio )
    {
        this.setStyle( gl, style );
        LineBufferHandles buffers = strip.deviceBuffers( gl, style.stippleEnable, ppvAspectRatio );
        this.draw( gl, buffers, 0, strip.actualSize( ) );
    }

    public void draw( GL2ES3 gl, LineStyle style, Collection<LineStrip> strips, double ppvAspectRatio )
    {
        this.setStyle( gl, style );
        for ( LineStrip strip : strips )
        {
            LineBufferHandles buffers = strip.deviceBuffers( gl, style.stippleEnable, ppvAspectRatio );
            this.draw( gl, buffers, 0, strip.actualSize( ) );
        }
    }

    public void draw( GL2ES3 gl, LineBufferHandles buffers, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, buffers.inXy );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, buffers.inFlags );
        gl.glVertexAttribIPointer( this.handles.inFlags, 1, GL_BYTE, 0, 0 );

        if ( buffers.inMileage != 0 )
        {
            gl.glEnableVertexAttribArray( this.handles.inMileage );
            gl.glBindBuffer( GL_ARRAY_BUFFER, buffers.inMileage );
            gl.glVertexAttribPointer( this.handles.inMileage, 1, GL_FLOAT, false, 0, 0 );
        }

        gl.glDrawArrays( GL_LINE_STRIP_ADJACENCY, first, count );

        if ( buffers.inMileage != 0 )
        {
            gl.glDisableVertexAttribArray( this.handles.inMileage );
        }
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glDisableVertexAttribArray( this.handles.inFlags );
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
