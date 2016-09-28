package com.metsci.glimpse.support.shader.triangle;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.*;
import static javax.media.opengl.GL.*;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.gl.texture.AbstractTexture;
import com.metsci.glimpse.gl.texture.DrawableTextureProgram;
import com.metsci.glimpse.support.shader.GLStreamingBufferBuilder;

/**
 * Applies a 1d rgba texture to triangles specified in axis coordinates.
 *
 * @see com.metsci.glimpse.axis.painter.ColorXAxisPainter
 */
public class ColorTexture1DProgram implements DrawableTextureProgram
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/triangle/colortex1d/colortex1d.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/triangle/colortex1d/colortex1d.fs" );

    public static class ProgramHandles
    {
        public final int program;

        // Uniforms

        public final int AXIS_RECT;
        public final int TEXTURE1D;

        // Vertex attributes

        public final int inXy;
        public final int inS;

        public ProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( this.program, "AXIS_RECT" );
            this.TEXTURE1D = gl.glGetUniformLocation( this.program, "TEXTURE1D" );

            this.inXy = gl.glGetAttribLocation( this.program, "inXy" );
            this.inS = gl.glGetAttribLocation( this.program, "inS" );
        }
    }

    // Local state

    protected int textureUnit;

    protected ProgramHandles handles;

    public ColorTexture1DProgram( )
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

    @Override
    public void begin( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        if ( this.handles == null )
        {
            this.handles = new ProgramHandles( gl );
        }

        gl.glUseProgram( this.handles.program );
        gl.glEnableVertexAttribArray( this.handles.inXy );
        gl.glEnableVertexAttribArray( this.handles.inS );
    }

    public void setAxisOrtho( GlimpseContext context, Axis2D axis )
    {
        setOrtho( context, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
    }

    public void setPixelOrtho( GlimpseContext context, GlimpseBounds bounds )
    {
        setOrtho( context, 0, bounds.getWidth( ), 0, bounds.getHeight( ) );
    }

    @Override
    public void setOrtho( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        GL3 gl = context.getGL( ).getGL3( );

        gl.glUniform4f( this.handles.AXIS_RECT, xMin, xMax, yMin, yMax );
    }

    public void setTexture( GL2ES2 gl, int textureUnit )
    {
        this.textureUnit = textureUnit;
        gl.glUniform1i( this.handles.TEXTURE1D, textureUnit );
    }

    public void draw( GlimpseContext context, int mode, AbstractTexture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        texture.prepare( context, this.textureUnit );

        draw( context, mode, xyVbo, sVbo, first, count );
    }

    public void draw( GlimpseContext context, AbstractTexture texture, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        draw( context, GL_TRIANGLE_STRIP, texture, xyVbo, sVbo, first, count );
    }

    @Override
    public void draw( GlimpseContext context, int mode, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        gl.glBindBuffer( xyVbo.target, xyVbo.buffer( ) );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glBindBuffer( sVbo.target, sVbo.buffer( ) );
        gl.glVertexAttribPointer( this.handles.inS, 1, GL_FLOAT, false, 0, sVbo.sealedOffset( ) );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GlimpseContext context, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        draw( context, GL_TRIANGLE_STRIP, xyVbo, sVbo, first, count );
    }

    @Override
    public void draw( GlimpseContext context, int mode, int xyVbo, int sVbo, int first, int count )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, sVbo );
        gl.glVertexAttribPointer( this.handles.inS, 1, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    public void draw( GlimpseContext context, int xyVbo, int sVbo, int first, int count )
    {
        draw( context, GL_TRIANGLE_STRIP, xyVbo, sVbo, first, count );
    }

    public void draw( GlimpseContext context, AbstractTexture texture, GLStreamingBufferBuilder xyVertices, GLStreamingBufferBuilder sVertices )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        draw( context, GL_TRIANGLES, texture, xyVertices.getBuffer( gl ), sVertices.getBuffer( gl ), 0, sVertices.numFloats( ) );
    }

    @Override
    public void end( GlimpseContext context )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glDisableVertexAttribArray( this.handles.inS );
        gl.glUseProgram( 0 );
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        if ( this.handles != null )
        {
            gl.glDeleteProgram( this.handles.program );
            this.handles = null;
        }
    }
}
