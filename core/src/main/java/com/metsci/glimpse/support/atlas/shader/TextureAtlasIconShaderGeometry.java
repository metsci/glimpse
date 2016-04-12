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

import javax.media.opengl.GL3;

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
        super( "Texture Atlas Icon Geometry Shader", "shaders/atlas/texture_atlas_icon_shader.gs", GL3.GL_POINTS, GL3.GL_TRIANGLE_STRIP, 4 );

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
