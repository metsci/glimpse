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
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.google.common.collect.Sets;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.shader.Pipeline;
import com.metsci.glimpse.gl.texture.DrawableTexture;
import com.metsci.glimpse.gl.texture.Texture;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;

/**
 * A painter which applies shaders to textures in order to display
 * dynamically adjustable representations of 2D gridded data.
 *
 * @author ulman
 *
 */
public class ShadedTexturePainter extends GlimpsePainter2D
{
    protected static final int DEFAULT_DRAWABLE_TEXTURE_UNIT = 0;
    protected static final int DEFAULT_NONDRAWABLE_TEXTURE_UNIT = 1;

    protected final ReentrantLock lock = new ReentrantLock( );

    protected Set<TextureUnit<Texture>> nonDrawableTextures;
    protected Map<TextureUnit<DrawableTexture>, Set<TextureUnit<Texture>>> drawableTextures;

    // the shader pipeline
    protected Pipeline pipeline;

    public ShadedTexturePainter( )
    {
        this.nonDrawableTextures = new HashSet<>( );
        this.drawableTextures = new HashMap<>( );
    }

    public void setPipeline( Pipeline pipeline )
    {
        lock.lock( );
        try
        {
            this.pipeline = pipeline;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addDrawableTexture( DrawableTexture texture )
    {
        addDrawableTexture( texture, DEFAULT_DRAWABLE_TEXTURE_UNIT );
    }

    public void addDrawableTexture( DrawableTexture texture, int textureUnit )
    {
        lock.lock( );
        try
        {
            this.drawableTextures.put( new TextureUnit<>( textureUnit, texture ), Sets.<TextureUnit<Texture>> newHashSet( ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeDrawableTexture( DrawableTexture texture )
    {
        lock.lock( );
        try
        {
            this.drawableTextures.remove( new TextureUnit<DrawableTexture>( texture ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeAllDrawableTextures( )
    {
        lock.lock( );
        try
        {
            this.drawableTextures.clear( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addNonDrawableTexture( Texture drawableTexture, Texture nonDrawableTexture, int textureUnit )
    {
        lock.lock( );
        try
        {
            Set<TextureUnit<Texture>> nonDrawableTextures = this.drawableTextures.get( new TextureUnit<>( drawableTexture ) );
            nonDrawableTextures.add( new TextureUnit<>( textureUnit, nonDrawableTexture ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeNonDrawableTexture( Texture drawableTexture, Texture nonDrawableTexture )
    {
        lock.lock( );
        try
        {
            Set<TextureUnit<Texture>> nonDrawableTextures = this.drawableTextures.get( new TextureUnit<>( drawableTexture ) );
            nonDrawableTextures.remove( new TextureUnit<>( nonDrawableTexture ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addNonDrawableTexture( Texture texture )
    {
        addNonDrawableTexture( texture, DEFAULT_NONDRAWABLE_TEXTURE_UNIT );
    }

    public void addNonDrawableTexture( Texture texture, int textureUnit )
    {
        lock.lock( );
        try
        {
            this.nonDrawableTextures.add( new TextureUnit<Texture>( textureUnit, texture ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeNonDrawableTexture( Texture texture )
    {
        lock.lock( );
        try
        {
            this.nonDrawableTextures.remove( new TextureUnit<Texture>( texture ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeAllNonDrawableTextures( )
    {
        lock.lock( );
        try
        {
            this.nonDrawableTextures.clear( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        GL2 gl = context.getGL( ).getGL2( );

        lock.lock( );
        try
        {
            gl.glMatrixMode( GL2.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( axis.getMinX( ), axis.getMaxX( ), axis.getMinY( ), axis.getMaxY( ), -1, 1 );

            if ( pipeline != null ) pipeline.beginUse( gl );
            try
            {
                for ( TextureUnit<DrawableTexture> textureUnit : drawableTextures.keySet( ) )
                {
                    draw( textureUnit, gl );
                }
            }
            finally
            {
                if ( pipeline != null ) pipeline.endUse( gl );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected void draw( TextureUnit<DrawableTexture> textureUnit, GL2 gl )
    {
        Set<TextureUnit<Texture>> nonDrawableTextures = Sets.union( this.nonDrawableTextures, this.drawableTextures.get( textureUnit ) );

        textureUnit.texture.draw( gl, textureUnit.textureUnit, nonDrawableTextures );
    }

    protected void prepare( TextureUnit<Texture> textureUnit, GL2 gl )
    {
        textureUnit.texture.prepare( gl, textureUnit.textureUnit );
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( pipeline != null ) pipeline.dispose( context );
    }
}
