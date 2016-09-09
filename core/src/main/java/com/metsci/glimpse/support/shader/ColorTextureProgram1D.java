package com.metsci.glimpse.support.shader;

import static com.metsci.glimpse.support.line.util.ShaderUtils.*;
import static javax.media.opengl.GL.*;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.texture.AbstractTexture;
import com.metsci.glimpse.support.line.util.MappableBuffer;

public class ColorTextureProgram1D
{
    public static final String vertShader_GLSL = requireResourceText( "shaders/colortex1d/colortex1d.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/colortex1d/colortex1d.fs" );
    
    public final int programHandle;

    // Uniforms

    public final int AXIS_RECT;
    public final int VIEWPORT_SIZE_PX;
    public final int TEXTURE1D;

    // Vertex attributes

    public final int inXy;
    public final int inS;
    
    // Local state
    
    protected int textureUnit;
    
    public ColorTextureProgram1D( GL2ES2 gl )
    {
        this.programHandle = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

        this.AXIS_RECT = gl.glGetUniformLocation( programHandle, "AXIS_RECT" );
        this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( programHandle, "VIEWPORT_SIZE_PX" );
        this.TEXTURE1D = gl.glGetUniformLocation( programHandle, "TEXTURE1D" );

        this.inXy = gl.glGetAttribLocation( programHandle, "inXy" );
        this.inS = gl.glGetAttribLocation( programHandle, "inS" );
    }
    
    public void begin( GL2ES2 gl )
    {
        gl.glUseProgram( programHandle );
        gl.glEnableVertexAttribArray( inXy );
        gl.glEnableVertexAttribArray( inS );
    }
    
    public void setViewport( GL2ES2 gl, GlimpseBounds bounds )
    {
        setViewport( gl, bounds.getWidth( ), bounds.getHeight( ) );
    }

    public void setViewport( GL2ES2 gl, int viewportWidth, int viewportHeight )
    {
        gl.glUniform2f( VIEWPORT_SIZE_PX, viewportWidth, viewportHeight );
    }
    
    public void setOrtho( GL2ES2 gl, float xMin, float xMax, float yMin, float yMax )
    {
        gl.glUniform4f( AXIS_RECT, xMin, xMax, yMin, yMax );
    }
    
    public void setTexture( GL2ES2 gl, int textureUnit )
    {
        this.textureUnit = textureUnit;
        gl.glUniform1i( TEXTURE1D, textureUnit );
    }
    
    public void draw( GL2ES2 gl, AbstractTexture texture, MappableBuffer xyVbo, MappableBuffer sVbo, int first, int count )
    {
        texture.prepare( gl, this.textureUnit );
        
        draw( gl, xyVbo, sVbo, first, count );
    }
    
    public void draw( GL2ES2 gl, MappableBuffer xyVbo, MappableBuffer sVbo, int first, int count )
    {
        gl.glBindBuffer( xyVbo.target, xyVbo.buffer( ) );
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glBindBuffer( sVbo.target, sVbo.buffer( ) );
        gl.glVertexAttribPointer( inS, 1, GL_FLOAT, false, 0, sVbo.sealedOffset( ) );

        gl.glDrawArrays( GL_LINE_STRIP, first, count );
    }

    public void draw( GL2ES2 gl, int xyVbo, int sVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, sVbo );
        gl.glVertexAttribPointer( inS, 1, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( GL_LINE_STRIP, first, count );
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( inXy );
        gl.glDisableVertexAttribArray( inS );
        gl.glUseProgram( 0 );
    }
}
