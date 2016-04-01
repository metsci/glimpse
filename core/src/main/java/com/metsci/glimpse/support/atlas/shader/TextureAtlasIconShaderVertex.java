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

import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

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
    protected static final Logger logger = Logger.getLogger( TextureAtlasIconShaderVertex.class.getName( ) );

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
        GL2 gl2 = gl.getGL2( );

        gl2.glBindAttribLocation( glProgramHandle, pixelCoordsAttributeIndex, "pixelCoords" );
        gl2.glBindAttribLocation( glProgramHandle, texCoordsAttributeIndex, "texCoords" );
        gl2.glBindAttribLocation( glProgramHandle, colorCoordsAttributeIndex, "pickColor" );

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
