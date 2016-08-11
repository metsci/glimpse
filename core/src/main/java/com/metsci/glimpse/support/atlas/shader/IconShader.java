package com.metsci.glimpse.support.atlas.shader;

import java.nio.Buffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLUniformData;

import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.joglshader.GlimpseShaderProgram;

public class IconShader extends GlimpseShaderProgram
{
    // Fragment Shader

    protected int textureUnit;
    protected GLUniformData textureUnitArg;
    protected GLUniformData isPickModeArg;
    protected boolean enablePicking;
    
    // Vertex Shader

    protected GLArrayDataClient pixelCoordAttribute;
    protected GLArrayDataClient texCoordAttribute;
    protected GLArrayDataClient colorCoordAttribute;

    // Geometry Shader

    protected GLUniformData viewportWidth;
    protected GLUniformData viewportHeight;
    protected GLUniformData globalScale;
    
    public IconShader( int textureUnit, boolean enablePicking )
    {
        this.addFragmentShader( "shaders/atlas/texture_atlas_icon_shader.fs" );
        this.addVertexShader( "shaders/atlas/texture_atlas_icon_shader.vs" );
        this.addGeometryShader( "shaders/atlas/texture_atlas_icon_shader.gs" );
        
        // Fragment Shader
        
        this.textureUnit = textureUnit;
        this.enablePicking = enablePicking;
        
        this.textureUnitArg = this.addUniformData( new GLUniformData( "tex", textureUnit ) );
        this.isPickModeArg = this.addUniformData( new GLUniformData( "isPickMode", enablePicking ? 1 : 0 ) );
    
        // Vertex Shader
        
        this.pixelCoordAttribute = this.addArrayData( GLArrayDataServer.createGLSL( "pixelCoords", 1, GL.GL_FLOAT, false, 0, GL.GL_STATIC_DRAW ) );
        this.texCoordAttribute = this.addArrayData( GLArrayDataServer.createGLSL( "texCoords", 1, GL.GL_FLOAT, false, 0, GL.GL_STATIC_DRAW ) );
        this.colorCoordAttribute = this.addArrayData( GLArrayDataServer.createGLSL( "pickColor", 1, GL.GL_FLOAT, false, 0, GL.GL_STATIC_DRAW ) );

        // Geometry Shader
        
        this.viewportWidth = this.addUniformData( new GLUniformData( "viewportWidth", 1 ) );
        this.viewportHeight = this.addUniformData( new GLUniformData( "viewportHeight", 1 ) );
        this.globalScale = this.addUniformData( new GLUniformData( "globalScale", 1 ) );
    }
    
    @Override
    public void useProgram( GL gl, boolean on )
    {
        super.useProgram( gl, on );
        
        if ( on )
        {
            gl.getGL3( ).glProgramParameteriARB( this.program.id( ), GL3.GL_GEOMETRY_INPUT_TYPE_ARB, GL3.GL_POINTS );
            gl.getGL3( ).glProgramParameteriARB( this.program.id( ), GL3.GL_GEOMETRY_OUTPUT_TYPE_ARB, GL3.GL_TRIANGLE_STRIP );
            gl.getGL3( ).glProgramParameteriARB( this.program.id( ), GL3.GL_GEOMETRY_INPUT_TYPE_ARB, 4 );
        }
    }
    
    public void setPickMode( boolean pickMode )
    {
        this.enablePicking = pickMode;
        this.isPickModeArg.setData( pickMode ? 1 : 0 );
    }
    
    public void setPixelCoordData( Buffer b )
    {
        this.pixelCoordAttribute.reset( );
        this.pixelCoordAttribute.put( b.rewind( ) );
    }
    
    public void setTexCoordData( Buffer b )
    {
        this.texCoordAttribute.reset( );
        this.texCoordAttribute.put( b.rewind( ) );
    }
    
    public void setColorCoordData( Buffer b )
    {
        this.colorCoordAttribute.reset( );
        this.colorCoordAttribute.put( b.rewind( ) );
    }
    
    public void updateViewport( GlimpseBounds bounds )
    {
        this.updateViewport( bounds.getWidth( ), bounds.getHeight( ) );
    }

    public void updateViewport( int width, int height )
    {
        this.viewportWidth.setData( width );
        this.viewportHeight.setData( height );
    }

    public void setGlobalScale( float scale )
    {
        this.globalScale.setData( scale );
    }
    
}
