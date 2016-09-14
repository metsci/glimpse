package com.metsci.glimpse.support.shader;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.*;
import static com.metsci.glimpse.gl.util.GLUtils.*;
import static javax.media.opengl.GL.*;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLStreamingBuffer;

public class ColorTexture2DProgram
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/colortex2d/colortex2d.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/colortex2d/colortex2d.fs" );

    public final int programHandle;

    // Uniforms

    public final int AXIS_RECT;
    public final int TEXTURE2D;

    // Vertex attributes

    public final int inXy;
    public final int inS;

    // Local state

    protected int textureUnit;

    public ColorTexture2DProgram( GL2ES2 gl )
    {
        this.programHandle = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

        this.AXIS_RECT = gl.glGetUniformLocation( programHandle, "AXIS_RECT" );
        this.TEXTURE2D = gl.glGetUniformLocation( programHandle, "TEXTURE2D" );

        this.inXy = gl.glGetAttribLocation( programHandle, "inXy" );
        this.inS = gl.glGetAttribLocation( programHandle, "inS" );
    }

    public void begin( GL2ES2 gl )
    {
        gl.glUseProgram( programHandle );
        gl.glEnableVertexAttribArray( inXy );
        gl.glEnableVertexAttribArray( inS );
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

    public void setTexture( GL2ES2 gl, int textureUnit )
    {
        this.textureUnit = textureUnit;
        gl.glUniform1i( TEXTURE2D, textureUnit );
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
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glBindBuffer( sVbo.target, sVbo.buffer( ) );
        gl.glVertexAttribPointer( inS, 2, GL_FLOAT, false, 0, sVbo.sealedOffset( ) );

        gl.glDrawArrays( mode, first, count );
    }
    
    public void draw( GL2ES2 gl, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count )
    {
        draw( gl, GL_TRIANGLE_STRIP, xyVbo, sVbo, first, count );
    }

    public void draw( GL2ES2 gl, int mode, int xyVbo, int sVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, sVbo );
        gl.glVertexAttribPointer( inS, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }
    
    public void draw( GL2ES2 gl, int xyVbo, int sVbo, int first, int count )
    {
        draw( gl, GL_TRIANGLE_STRIP, xyVbo, sVbo, first, count);
    }
    
    public void draw( GL2ES2 gl, com.metsci.glimpse.gl.texture.Texture texture, MappableBufferBuilder xyVertices, MappableBufferBuilder sVertices )
    {
        draw( gl, GL_TRIANGLES, texture, xyVertices.getBuffer( gl ), sVertices.getBuffer( gl ), 0, sVertices.numFloats( ) / 2 );
    }

    public void draw( GL2ES2 gl, com.jogamp.opengl.util.texture.Texture texture, MappableBufferBuilder xyVertices, MappableBufferBuilder sVertices )
    {
        draw( gl, GL_TRIANGLES, texture, xyVertices.getBuffer( gl ), sVertices.getBuffer( gl ), 0, sVertices.numFloats( ) / 2 );
    }
    
    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( inXy );
        gl.glDisableVertexAttribArray( inS );
        gl.glUseProgram( 0 );
    }
}
