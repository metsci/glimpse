package com.metsci.glimpse.support.shader.triangle;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.*;
import static com.metsci.glimpse.gl.util.GLUtils.*;
import static javax.media.opengl.GL.*;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.support.shader.GLStreamingBufferBuilder;

public class ColorTexture2DProgram
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/triangle/colortex2d/colortex2d.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/triangle/colortex2d/colortex2d.fs" );

    public static class ProgramHandles
    {
        public final int program;

        // Uniforms

        public final int AXIS_RECT;
        public final int TEXTURE2D;

        public final int RGBA;

        // Vertex attributes

        public final int inXy;
        public final int inS;

        public ProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( this.program, "AXIS_RECT" );
            this.TEXTURE2D = gl.glGetUniformLocation( this.program, "TEXTURE2D" );
            this.RGBA = gl.glGetUniformLocation( this.program, "RGBA" );

            this.inXy = gl.glGetAttribLocation( this.program, "inXy" );
            this.inS = gl.glGetAttribLocation( this.program, "inS" );
        }
    }

    // Local state

    protected int textureUnit;

    protected ProgramHandles handles;

    public ColorTexture2DProgram( )
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
            // set default for RGBA color multiplier (identity)
            gl.glUniform4f( this.handles.RGBA, 1, 1, 1, 1 );
        }

        gl.glUseProgram( this.handles.program );
        gl.glEnableVertexAttribArray( this.handles.inXy );
        gl.glEnableVertexAttribArray( this.handles.inS );
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

    public void setColor( GL2ES2 gl, float[] rgba )
    {
        gl.glUniform4fv( this.handles.RGBA, 1, rgba, 0 );
    }

    public void setTexture( GL2ES2 gl, int textureUnit )
    {
        this.textureUnit = textureUnit;
        gl.glUniform1i( this.handles.TEXTURE2D, textureUnit );
    }

    public void draw( GL2ES2 gl, int mode, com.jogamp.opengl.util.texture.Texture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        gl.glActiveTexture( getGLTextureUnit( this.textureUnit ) );
        texture.bind( gl );

        draw( gl, mode, xyVbo, sVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, com.metsci.glimpse.gl.texture.Texture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        texture.prepare( gl, this.textureUnit );

        draw( gl, mode, xyVbo, sVbo, first, count );
    }

    public void draw( GL2ES2 gl, com.jogamp.opengl.util.texture.Texture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        draw( gl, GL_TRIANGLE_STRIP, texture, xyVbo, sVbo, first, count );
    }

    public void draw( GL2ES2 gl, com.metsci.glimpse.gl.texture.Texture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        draw( gl, GL_TRIANGLE_STRIP, texture, xyVbo, sVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        gl.glBindBuffer( xyVbo.target, xyVbo.buffer( ) );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glBindBuffer( sVbo.target, sVbo.buffer( ) );
        gl.glVertexAttribPointer( this.handles.inS, 2, GL_FLOAT, false, 0, sVbo.sealedOffset( ) );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        draw( gl, GL_TRIANGLE_STRIP, xyVbo, sVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, int xyVbo, int sVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, sVbo );
        gl.glVertexAttribPointer( this.handles.inS, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GL2ES2 gl, int xyVbo, int sVbo, int first, int count )
    {
        draw( gl, GL_TRIANGLE_STRIP, xyVbo, sVbo, first, count );
    }

    public void draw( GL2ES2 gl, com.metsci.glimpse.gl.texture.Texture texture, GLStreamingBufferBuilder xyVertices, GLStreamingBufferBuilder sVertices )
    {
        draw( gl, GL_TRIANGLES, texture, xyVertices.getBuffer( gl ), sVertices.getBuffer( gl ), 0, sVertices.numFloats( ) / 2 );
    }

    public void draw( GL2ES2 gl, com.jogamp.opengl.util.texture.Texture texture, GLStreamingBufferBuilder xyVertices, GLStreamingBufferBuilder sVertices )
    {
        draw( gl, GL_TRIANGLES, texture, xyVertices.getBuffer( gl ), sVertices.getBuffer( gl ), 0, sVertices.numFloats( ) / 2 );
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glDisableVertexAttribArray( this.handles.inS );
        gl.glUseProgram( 0 );
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
