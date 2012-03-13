package com.metsci.glimpse.support.atlas.shader;

import javax.media.opengl.GL;

import com.metsci.glimpse.gl.shader.Shader;
import com.metsci.glimpse.gl.shader.ShaderArg;
import com.metsci.glimpse.gl.shader.ShaderType;

/**
 * Fragment shader wrapper for {@link com.metsci.glimpse.support.atlas.painter.IconPainter}. Normally
 * simply colors the fragment based on the provided texture coordinates. However, in picking mode,
 * the shader substitutes non-transparent elements of the texture for the pick color for the given icon.
 * 
 * @author ulman
 */
public class TextureAtlasIconShaderFragment extends Shader
{
    protected int textureUnit;
    protected ShaderArg textureUnitArg;
    protected ShaderArg isPickModeArg;
    protected boolean enablePicking;
    
    public TextureAtlasIconShaderFragment( int textureUnit, boolean enablePicking )
    {
        super( "Texture Atlas Icon Fragment Shader", ShaderType.fragment, "shaders/atlas/texture_atlas_icon_shader.fs" );
        this.textureUnit = textureUnit;
        this.enablePicking = enablePicking;
    }

    @Override
    public boolean preLink( GL gl, int glProgramHandle )
    {
        this.textureUnitArg = getArg( "tex" );
        this.textureUnitArg.setValue( textureUnit );
        
        this.isPickModeArg = getArg( "isPickMode" );
        this.isPickModeArg.setValue( this.enablePicking );
        
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
    
    public void setPickMode( boolean pickMode )
    {
        this.enablePicking = pickMode;
        this.isPickModeArg.setValue( pickMode );
    }
}
