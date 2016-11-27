package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static com.metsci.glimpse.gl.util.GLUtils.defaultVertexAttributeArray;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_POINTS;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLEditableBuffer;

public class ExampleProgram
{

    public static final String exampleVertShader_GLSL = requireResourceText( "asdf/example.vs" );
    public static final String exampleFragShader_GLSL = requireResourceText( "asdf/example.fs" );


    public static class ExampleProgramHandles
    {
        public final int program;

        public final int AXIS_RECT;
        public final int VIEWPORT_SIZE_PX;

        public final int POINT_SIZE_PX;
        public final int FEATHER_THICKNESS_PX;
        public final int RGBA;

        public final int inTxyz;

        public ExampleProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, exampleVertShader_GLSL, null, exampleFragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );
            this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( program, "VIEWPORT_SIZE_PX" );

            this.POINT_SIZE_PX = gl.glGetUniformLocation( program, "POINT_SIZE_PX" );
            this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( program, "FEATHER_THICKNESS_PX" );
            this.RGBA = gl.glGetUniformLocation( program, "RGBA" );

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

    public void setStyle( GL2ES2 gl, ExampleStyle style )
    {
        gl.glUniform1f( this.handles.POINT_SIZE_PX, style.pointSize_PX );
        gl.glUniform1f( this.handles.FEATHER_THICKNESS_PX, style.feather_PX );
        gl.glUniform4fv( this.handles.RGBA, 1, style.rgba, 0 );
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
