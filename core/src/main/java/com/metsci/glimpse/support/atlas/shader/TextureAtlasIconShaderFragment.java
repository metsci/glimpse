/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
