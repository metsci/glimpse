package com.metsci.glimpse.support.atlas.shader;

import java.util.logging.Logger;

import javax.media.opengl.GL;

import com.metsci.glimpse.gl.shader.Shader;
import com.metsci.glimpse.gl.shader.ShaderType;

/**
 * Vertex shader wrapper for {@link com.metsci.glimpse.support.atlas.painter.IconPainter}. Simply
 * applies projection matrices and passes attribute arrays through to geometry shader.
 * 
 * @author ulman
 */
public class TextureAtlasIconShaderVertex extends Shader
{
    protected static final Logger logger = Logger.getLogger( TextureAtlasIconShaderVertex.class.getSimpleName( ) );

    protected int pixelCoordsAttributeIndex;
    protected int texCoordsAttributeIndex;
    protected int colorCoordsAttributeIndex;

    //@formatter:off
    public TextureAtlasIconShaderVertex( int pixelCoordsAttributeIndex, int texCoordsAttributeIndex, int colorCoordsAttributeIndex )
    {
        super( "Texture Atlas Icon Vertex Shader", ShaderType.vertex, "shaders/atlas/texture_atlas_icon_shader.vs" );

        this.pixelCoordsAttributeIndex = pixelCoordsAttributeIndex;
        this.texCoordsAttributeIndex = texCoordsAttributeIndex;
        this.colorCoordsAttributeIndex = colorCoordsAttributeIndex;
    }
    //@formatter:on

    @Override
    public boolean preLink( GL gl, int glProgramHandle )
    {
        gl.glBindAttribLocation( glProgramHandle, pixelCoordsAttributeIndex, "pixelCoords" );
        gl.glBindAttribLocation( glProgramHandle, texCoordsAttributeIndex, "texCoords" );
        gl.glBindAttribLocation( glProgramHandle, colorCoordsAttributeIndex, "pickColor" );

        return true;
    }

    @Override
    public void preDisplay( GL gl )
    {
    }

    @Override
    public void postDisplay( GL gl )
    {
    }
}
