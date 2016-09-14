package com.metsci.glimpse.support.shader;

import static com.metsci.glimpse.support.line.util.ShaderUtils.*;
import static javax.media.opengl.GL.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.support.line.util.MappableBuffer;

public class ArrayColorProgram
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/array_color/array_color.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/array_color/array_color.fs" );

    public final int programHandle;

    // Uniforms

    public final int AXIS_RECT;

    // Vertex attributes

    public final int inXy;
    public final int inRgba;

    public ArrayColorProgram( GL2ES2 gl )
    {
        this.programHandle = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

        this.AXIS_RECT = gl.glGetUniformLocation( programHandle, "AXIS_RECT" );

        this.inXy = gl.glGetAttribLocation( programHandle, "inXy" );
        this.inRgba = gl.glGetAttribLocation( programHandle, "inRgba" );
    }

    public void begin( GL2ES2 gl )
    {
        gl.glUseProgram( programHandle );
        gl.glEnableVertexAttribArray( inXy );
        gl.glEnableVertexAttribArray( inRgba );
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
        gl.glUniform4f( AXIS_RECT, xMin, xMax, yMin, yMax );
    }

    public void draw( GL2ES2 gl, MappableBuffer xyVbo, MappableBuffer rgbaVbo, int first, int count )
    {
        draw( gl, GL.GL_TRIANGLES, xyVbo, rgbaVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, MappableBuffer xyVbo, MappableBuffer rgbaVbo, int first, int count )
    {
        gl.glBindBuffer( xyVbo.target, xyVbo.buffer( ) );
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );
        
        gl.glBindBuffer( rgbaVbo.target, rgbaVbo.buffer( ) );
        gl.glVertexAttribPointer( inRgba, 2, GL_FLOAT, false, 0, rgbaVbo.sealedOffset( ) );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, int mode, int xyVbo, int rgbaVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, rgbaVbo );
        gl.glVertexAttribPointer( inRgba, 2, GL_FLOAT, false, 0, 0 );
        
        gl.glDrawArrays( mode, first, count );
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( inXy );
        gl.glUseProgram( 0 );
    }
}
