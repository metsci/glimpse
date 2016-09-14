package com.metsci.glimpse.support.shader;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.*;
import static javax.media.opengl.GL.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLStreamingBuffer;

public class FlatColorProgram
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/flat_color/flat_color.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/flat_color/flat_color.fs" );

    public final int programHandle;

    // Uniforms

    public final int AXIS_RECT;
    public final int RGBA;

    // Vertex attributes

    public final int inXy;

    public FlatColorProgram( GL2ES2 gl )
    {
        this.programHandle = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

        this.AXIS_RECT = gl.glGetUniformLocation( programHandle, "AXIS_RECT" );
        this.RGBA = gl.glGetUniformLocation( programHandle, "RGBA" );

        this.inXy = gl.glGetAttribLocation( programHandle, "inXy" );
    }

    public void begin( GL2ES2 gl )
    {
        gl.glUseProgram( programHandle );
        gl.glEnableVertexAttribArray( inXy );
    }
    
    public void setColor( GL2ES2 gl, float r, float g, float b, float a )
    {
        gl.glUniform4f( RGBA, r, g, b, a );
    }
    
    public void setColor( GL2ES2 gl, float[] vRGBA )
    {
        gl.glUniform4fv( RGBA, 1, vRGBA, 0 );
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

    public void draw( GL2ES2 gl, GLStreamingBuffer xyVbo, int first, int count )
    {
        draw( gl, GL.GL_TRIANGLES, xyVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, GLStreamingBuffer xyVbo, int first, int count )
    {
        gl.glBindBuffer( xyVbo.target, xyVbo.buffer( ) );
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, int mode, int xyVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( inXy );
        gl.glUseProgram( 0 );
    }
}
