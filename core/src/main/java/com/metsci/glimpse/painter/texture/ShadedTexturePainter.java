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
package com.metsci.glimpse.painter.texture;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;

import com.google.common.collect.Sets;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.DrawableTexture;
import com.metsci.glimpse.gl.texture.DrawableTextureProgram;
import com.metsci.glimpse.gl.texture.Texture;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.shader.triangle.ColorTexture2DProgram;

/**
 * A painter which applies shaders to textures in order to display
 * dynamically adjustable representations of 2D gridded data.
 *
 * @author ulman
 *
 */
public class ShadedTexturePainter extends GlimpsePainterBase
{
    protected static final int DEFAULT_DRAWABLE_TEXTURE_UNIT = 0;
    protected static final int DEFAULT_NONDRAWABLE_TEXTURE_UNIT = 1;

    protected Set<TextureUnit<Texture>> nonDrawableTextures;
    protected Map<TextureUnit<DrawableTexture>, Set<TextureUnit<Texture>>> drawableTextures;

    // the shader pipeline
    protected DrawableTextureProgram program;

    public ShadedTexturePainter( )
    {
        this.nonDrawableTextures = new HashSet<>( );
        this.drawableTextures = new HashMap<>( );
        // default behavior in Glimpse 2.0 (with no shader set)
        // was to assume texture contained RGBA color data
        // so set the default program to mimic this behavior
        this.program = new ColorTexture2DProgram( );
    }

    public void setProgram( DrawableTextureProgram program )
    {
        painterLock.lock( );
        try
        {
            this.program = program;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void addDrawableTexture( DrawableTexture texture )
    {
        addDrawableTexture( texture, DEFAULT_DRAWABLE_TEXTURE_UNIT );
    }

    public void addDrawableTexture( DrawableTexture texture, int textureUnit )
    {
        painterLock.lock( );
        try
        {
            this.drawableTextures.put( new TextureUnit<>( textureUnit, texture ), Sets.<TextureUnit<Texture>> newHashSet( ) );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void removeDrawableTexture( DrawableTexture texture )
    {
        painterLock.lock( );
        try
        {
            this.drawableTextures.remove( new TextureUnit<DrawableTexture>( texture ) );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void removeAllDrawableTextures( )
    {
        painterLock.lock( );
        try
        {
            this.drawableTextures.clear( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void addNonDrawableTexture( Texture drawableTexture, Texture nonDrawableTexture, int textureUnit )
    {
        painterLock.lock( );
        try
        {
            Set<TextureUnit<Texture>> nonDrawableTextures = this.drawableTextures.get( new TextureUnit<>( drawableTexture ) );
            nonDrawableTextures.add( new TextureUnit<>( textureUnit, nonDrawableTexture ) );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void removeNonDrawableTexture( Texture drawableTexture, Texture nonDrawableTexture )
    {
        painterLock.lock( );
        try
        {
            Set<TextureUnit<Texture>> nonDrawableTextures = this.drawableTextures.get( new TextureUnit<>( drawableTexture ) );
            nonDrawableTextures.remove( new TextureUnit<>( nonDrawableTexture ) );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void addNonDrawableTexture( Texture texture )
    {
        addNonDrawableTexture( texture, DEFAULT_NONDRAWABLE_TEXTURE_UNIT );
    }

    public void addNonDrawableTexture( Texture texture, int textureUnit )
    {
        painterLock.lock( );
        try
        {
            this.nonDrawableTextures.add( new TextureUnit<Texture>( textureUnit, texture ) );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void removeNonDrawableTexture( Texture texture )
    {
        painterLock.lock( );
        try
        {
            this.nonDrawableTextures.remove( new TextureUnit<Texture>( texture ) );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void removeAllNonDrawableTextures( )
    {
        painterLock.lock( );
        try
        {
            this.nonDrawableTextures.clear( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GL gl = context.getGL( );

        GLUtils.enableStandardBlending( gl );
        try
        {
            for ( TextureUnit<DrawableTexture> textureUnit : drawableTextures.keySet( ) )
            {
                draw( textureUnit, context );
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }
    }

    protected void draw( TextureUnit<DrawableTexture> textureUnit, GlimpseContext context )
    {
        Set<TextureUnit<Texture>> nonDrawableTextures = Sets.union( this.nonDrawableTextures, this.drawableTextures.get( textureUnit ) );
        textureUnit.texture.draw( context, program, textureUnit.textureUnit, nonDrawableTextures );

    }

    protected void prepare( TextureUnit<Texture> textureUnit, GlimpseContext context )
    {
        textureUnit.texture.prepare( context, textureUnit.textureUnit );
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        if ( program != null ) program.dispose( context );
    }
}
