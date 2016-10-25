package com.metsci.glimpse.support.shader.triangle;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.*;
import static javax.media.opengl.GL.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;

/**
 * Applies rgba colors to filled triangles.
 *
 * @see com.metsci.glimpse.painter.decoration.MapBorderPainter
 */
public class ArrayColorProgram
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/triangle/array_color/array_color.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/triangle/array_color/array_color.fs" );

    public static class ProgramHandles
    {
        public final int program;

        // Uniforms

        public final int AXIS_RECT;

        // Vertex attributes

        public final int inXy;
        public final int inRgba;

        public ProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( this.program, "AXIS_RECT" );

            this.inXy = gl.glGetAttribLocation( this.program, "inXy" );
            this.inRgba = gl.glGetAttribLocation( this.program, "inRgba" );
        }
    }

    protected ProgramHandles handles;

    public ArrayColorProgram( )
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

    public void draw( GL2ES2 gl, GLEditableBuffer xyVbo, GLEditableBuffer rgbaVbo, int first, int count )
    {
        draw( gl, GL.GL_TRIANGLES, xyVbo, rgbaVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, GLEditableBuffer xyVbo, GLEditableBuffer rgbaVbo, int first, int count )
    {
        draw( gl, mode, xyVbo.deviceBuffer( gl ), rgbaVbo.deviceBuffer( gl ), first, count );
    }

    public void draw( GL2ES2 gl, int mode, int xyVbo, int rgbaVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, rgbaVbo );
        gl.glVertexAttribPointer( this.handles.inRgba, 4, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, GLEditableBuffer xy, GLEditableBuffer rgba )
    {
        draw( gl, GL_TRIANGLES, xy, rgba, 0, xy.sizeFloats( ) / 2 );
    }

    public void end( GL2ES2 gl )
    {
        gl.getGL3( ).glBindVertexArray( 0 );
        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glUseProgram( 0 );
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
