package com.metsci.glimpse.support.atlas.shader;

import javax.media.opengl.GL;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.shader.ShaderArg;
import com.metsci.glimpse.support.shader.geometry.SimpleGeometryShader;

/**
 * Geometry shader wrapper for {@link com.metsci.glimpse.support.atlas.painter.IconPainter}. Takes
 * points indicating icon positions and expands them into rectangles (triangle strips) textured using
 * data from a {@link com.metsci.glimpse.support.atlas.TextureAtlas}.
 * 
 * @author ulman
 */
public class TextureAtlasIconShaderGeometry extends SimpleGeometryShader
{
    protected ShaderArg viewportWidth;
    protected ShaderArg viewportHeight;
    
    protected ShaderArg globalScale;

    public TextureAtlasIconShaderGeometry( )
    {
        super( "Texture Atlas Icon Geometry Shader", "shaders/atlas/texture_atlas_icon_shader.gs", GL.GL_POINTS, GL.GL_TRIANGLE_STRIP, 4 );

        this.viewportWidth = getArg( "viewportWidth" );
        this.viewportWidth.setValue( 1 );

        this.viewportHeight = getArg( "viewportHeight" );
        this.viewportHeight.setValue( 1 );
        
        this.globalScale = getArg( "globalScale" );
        this.globalScale.setValue( 1 );
    }

    public void updateViewport( GlimpseBounds bounds )
    {
        this.updateViewport( bounds.getWidth( ), bounds.getHeight( ) );
    }

    public void updateViewport( int width, int height )
    {
        this.viewportWidth.setValue( width );
        this.viewportHeight.setValue( height );
    }
    
    public void setGlobalScale( float scale )
    {
        this.globalScale.setValue( scale );
    }
}
