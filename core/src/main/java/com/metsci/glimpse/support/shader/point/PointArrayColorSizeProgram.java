package com.metsci.glimpse.support.shader.point;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.*;
import static javax.media.opengl.GL.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLES1;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;

/**
 * Draws 2D point with feathered edges, and a color and size specified per point.
 */
public class PointArrayColorSizeProgram
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/point/point_array_color_size/point.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/point/point_array_color_size/point.fs" );

    public static class ProgramHandles
    {
        public final int program;

        // Uniforms

        public final int AXIS_RECT;
        public final int FEATHER_THICKNESS_PX;

        // Vertex attributes

        public final int inXy;
        public final int inRgba;
        public final int inSize;

        public ProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( this.program, "AXIS_RECT" );
            this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( this.program, "FEATHER_THICKNESS_PX" );

            this.inXy = gl.glGetAttribLocation( this.program, "inXy" );
            this.inRgba = gl.glGetAttribLocation( this.program, "inRgba" );
            this.inSize = gl.glGetAttribLocation( this.program, "inSize" );

            // set defaults
            gl.glUseProgram( this.program );
            gl.glUniform1f( this.FEATHER_THICKNESS_PX, 0.8f );
        }
    }

    protected ProgramHandles handles;

    public PointArrayColorSizeProgram( )
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
        gl.glEnableVertexAttribArray( this.handles.inRgba );
        gl.glEnableVertexAttribArray( this.handles.inSize );

        gl.glEnable( GL3.GL_PROGRAM_POINT_SIZE );
        if ( gl.isGL2( ) ) gl.glEnable( GLES1.GL_POINT_SPRITE );
    }

    public void setFeatherThickness( GL2ES2 gl, float value )
    {
        gl.glUniform1f( this.handles.FEATHER_THICKNESS_PX, value );
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

    public void draw( GL2ES2 gl, GLEditableBuffer xyVbo, GLEditableBuffer rgbaVbo, GLEditableBuffer sizeVbo, int first, int count )
    {
        draw( gl, GL.GL_POINTS, xyVbo, rgbaVbo, sizeVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, GLEditableBuffer xyVbo, GLEditableBuffer rgbaVbo, GLEditableBuffer sizeVbo, int first, int count )
    {
        draw( gl, mode, xyVbo.deviceBuffer( gl ), rgbaVbo.deviceBuffer( gl ), sizeVbo.deviceBuffer( gl ), first, count );
    }

    public void draw( GL2ES2 gl, int mode, int xyVbo, int rgbaVbo, int sizeVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, rgbaVbo );
        gl.glVertexAttribPointer( this.handles.inRgba, 4, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, sizeVbo );
        gl.glVertexAttribPointer( this.handles.inSize, 1, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, GLEditableBuffer xy, GLEditableBuffer rgba, GLEditableBuffer size )
    {
        draw( gl, GL_POINTS, xy, rgba, size, 0, xy.sizeFloats( ) / 2 );
    }

    public void end( GL2ES2 gl )
    {
        gl.getGL3( ).glBindVertexArray( 0 );
        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glDisableVertexAttribArray( this.handles.inRgba );
        gl.glDisableVertexAttribArray( this.handles.inSize );
        gl.glUseProgram( 0 );

        gl.glDisable( GL3.GL_PROGRAM_POINT_SIZE );
        if ( gl.isGL2( ) ) gl.glDisable( GLES1.GL_POINT_SPRITE );
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
