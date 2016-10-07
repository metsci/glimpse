package com.metsci.glimpse.support.shader.triangle;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static com.metsci.glimpse.gl.util.GLUtils.getGLTextureUnit;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_TRIANGLES;
import static javax.media.opengl.GL.GL_TRIANGLE_STRIP;

import java.util.logging.Logger;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.gl.GLStreamingBufferBuilder;
import com.metsci.glimpse.gl.texture.DrawableTextureProgram;
import com.metsci.glimpse.gl.util.GLErrorUtils;

/**
 * Applies a 2d rgba texture to triangles specified in axis coordinates.
 *
 * @see com.metsci.glimpse.painter.decoration.WatermarkPainter
 */
public class ColorTexture2DProgram implements DrawableTextureProgram
{
    private static final Logger logger = Logger.getLogger( ColorTexture2DProgram.class.getName( ) );

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

    protected boolean colorSet = false;

    public ColorTexture2DProgram( )
    {
        this.handles = null;
    }

    public ProgramHandles handles( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        if ( this.handles == null )
        {
            this.handles = new ProgramHandles( gl );
        }

        return this.handles;
    }

    public void begin( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        this.handles( context );

        gl.glUseProgram( this.handles.program );
        gl.glEnableVertexAttribArray( this.handles.inXy );
        gl.glEnableVertexAttribArray( this.handles.inS );

        if ( !this.colorSet )
        {
            this.setColor( context, new float[] { 1.0f, 1.0f, 1.0f, 1.0f } );
            this.colorSet = true;
        }
    }

    @Override
    public void begin( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        this.begin( context );

        this.setOrtho( context, xMin, xMax, yMin, yMax );
    }

    public void setAxisOrtho( GlimpseContext context, Axis2D axis )
    {
        setOrtho( context, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
    }

    public void setPixelOrtho( GlimpseContext context, GlimpseBounds bounds )
    {
        setOrtho( context, 0, bounds.getWidth( ), 0, bounds.getHeight( ) );
    }

    public void setOrtho( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        GL3 gl = context.getGL( ).getGL3( );

        gl.glUniform4f( this.handles.AXIS_RECT, xMin, xMax, yMin, yMax );
    }

    public void setColor( GlimpseContext context, float[] rgba )
    {
        GL3 gl = context.getGL( ).getGL3( );

        gl.glUniform4fv( this.handles.RGBA, 1, rgba, 0 );

        this.colorSet = true;
    }

    public void setTexture( GlimpseContext context, int textureUnit )
    {
        GL3 gl = context.getGL( ).getGL3( );

        this.textureUnit = textureUnit;
        gl.glUniform1i( this.handles.TEXTURE2D, textureUnit );
    }

    public void draw( GlimpseContext context, int mode, com.jogamp.opengl.util.texture.Texture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        GL3 gl = context.getGL( ).getGL3( );

        gl.glActiveTexture( getGLTextureUnit( this.textureUnit ) );
        texture.bind( gl );

        draw( context, mode, xyVbo, sVbo, first, count );
    }

    public void draw( GlimpseContext context, int mode, com.metsci.glimpse.gl.texture.Texture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        texture.prepare( context, this.textureUnit );

        draw( context, mode, xyVbo, sVbo, first, count );
    }

    public void draw( GlimpseContext context, com.jogamp.opengl.util.texture.Texture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        draw( context, GL_TRIANGLE_STRIP, texture, xyVbo, sVbo, first, count );
    }

    public void draw( GlimpseContext context, com.metsci.glimpse.gl.texture.Texture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        draw( context, GL_TRIANGLE_STRIP, texture, xyVbo, sVbo, first, count );
    }

    @Override
    public void draw( GlimpseContext context, int mode, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        GL3 gl = context.getGL( ).getGL3( );

        gl.glBindBuffer( xyVbo.target, xyVbo.buffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glBindBuffer( sVbo.target, sVbo.buffer( gl ) );
        gl.glVertexAttribPointer( this.handles.inS, 2, GL_FLOAT, false, 0, sVbo.sealedOffset( ) );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GlimpseContext context, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        draw( context, GL_TRIANGLE_STRIP, xyVbo, sVbo, first, count );
    }

    @Override
    public void draw( GlimpseContext context, int mode, int xyVbo, int sVbo, int first, int count )
    {
        GL3 gl = context.getGL( ).getGL3( );

        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, sVbo );
        gl.glVertexAttribPointer( this.handles.inS, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GlimpseContext context, int xyVbo, int sVbo, int first, int count )
    {
        draw( context, GL_TRIANGLE_STRIP, xyVbo, sVbo, first, count );
    }

    public void draw( GlimpseContext context, com.metsci.glimpse.gl.texture.Texture texture, GLStreamingBufferBuilder xyVertices, GLStreamingBufferBuilder sVertices )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        draw( context, GL_TRIANGLES, texture, xyVertices.getBuffer( gl ), sVertices.getBuffer( gl ), 0, sVertices.numFloats( ) / 2 );
    }

    public void draw( GlimpseContext context, com.jogamp.opengl.util.texture.Texture texture, GLStreamingBufferBuilder xyVertices, GLStreamingBufferBuilder sVertices )
    {
        GL3 gl = context.getGL( ).getGL3( );

        draw( context, GL_TRIANGLES, texture, xyVertices.getBuffer( gl ), sVertices.getBuffer( gl ), 0, sVertices.numFloats( ) / 2 );
    }

    @Override
    public void end( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        if ( this.handles != null )
        {
            gl.glDisableVertexAttribArray( this.handles.inXy );
            gl.glDisableVertexAttribArray( this.handles.inS );
        }

        gl.glUseProgram( 0 );

        GLErrorUtils.logGLError( logger, gl, "Error in ColorTexture2DProgram.end" );

    }

    /**
     * Deletes the program, and resets this object to the way it was before {@link #begin(GL2ES2)}
     * was first called.
     * <p>
     * This object can be safely reused after being disposed, but in most cases there is no
     * significant advantage to doing so.
     */
    @Override
    public void dispose( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        if ( this.handles != null )
        {
            gl.glDeleteProgram( this.handles.program );
            this.handles = null;
        }
    }
}
