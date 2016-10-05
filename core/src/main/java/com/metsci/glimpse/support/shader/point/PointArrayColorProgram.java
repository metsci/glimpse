package com.metsci.glimpse.support.shader.point;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.*;
import static javax.media.opengl.GL.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLES1;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.gl.GLStreamingBufferBuilder;

/**
 * Draws 2D point with feathered edges, a constant pixel radius, and a color specified per point.
 */
public class PointArrayColorProgram
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/point/point_array_color/point.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/point/point_array_color/point.fs" );

    public static class ProgramHandles
    {
        public final int program;

        // Uniforms

        public final int AXIS_RECT;
        public final int POINT_SIZE_PX;
        public final int FEATHER_THICKNESS_PX;

        // Vertex attributes

        public final int inXy;
        public final int inRgba;

        public ProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( this.program, "AXIS_RECT" );
            this.POINT_SIZE_PX = gl.glGetUniformLocation( this.program, "POINT_SIZE_PX" );
            this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( this.program, "FEATHER_THICKNESS_PX" );

            gl.glUniform1f( this.FEATHER_THICKNESS_PX, 2.0f );
            gl.glUniform1f( this.POINT_SIZE_PX, 3.0f );

            this.inXy = gl.glGetAttribLocation( this.program, "inXy" );
            this.inRgba = gl.glGetAttribLocation( this.program, "inRgba" );
        }
    }

    protected ProgramHandles handles;

    public PointArrayColorProgram( )
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

        gl.glUseProgram( this.handles.program );
        gl.glEnableVertexAttribArray( this.handles.inXy );
        gl.glEnableVertexAttribArray( this.handles.inRgba );

        gl.glEnable( GL3.GL_PROGRAM_POINT_SIZE );
        //XXX this appears to be necessary for gl_PointCoord be set with proper values in the fragment shader
        //XXX however I don't believe setting it should be necessary (it's deprecated in GL3)
        gl.glEnable( GLES1.GL_POINT_SPRITE );
    }

    public void setFeatherThickness( GL2ES2 gl, float value )
    {
        gl.glUniform1f( this.handles.FEATHER_THICKNESS_PX, value );
    }

    public void setPointSize( GL2ES2 gl, float value )
    {
        gl.glUniform1f( this.handles.POINT_SIZE_PX, value );
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

    public void draw( GL2ES2 gl, GLStreamingBuffer xyVbo, GLStreamingBuffer rgbaVbo, int first, int count )
    {
        draw( gl, GL.GL_POINTS, xyVbo, rgbaVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, GLStreamingBuffer xyVbo, GLStreamingBuffer rgbaVbo, int first, int count )
    {
        gl.glBindBuffer( xyVbo.target, xyVbo.buffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glBindBuffer( rgbaVbo.target, rgbaVbo.buffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inRgba, 4, GL_FLOAT, false, 0, rgbaVbo.sealedOffset( ) );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, int mode, int xyVbo, int rgbaVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, rgbaVbo );
        gl.glVertexAttribPointer( this.handles.inRgba, 4, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, GLStreamingBufferBuilder xy, GLStreamingBufferBuilder rgba )
    {
        draw( gl, GL_POINTS, xy.getBuffer( gl ), rgba.getBuffer( gl ), 0, xy.numFloats( ) / 2 );
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glUseProgram( 0 );

        gl.glDisable( GL3.GL_PROGRAM_POINT_SIZE );
        gl.glDisable( GLES1.GL_POINT_SPRITE );
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
