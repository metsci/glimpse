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

import com.metsci.glimpse.support.texture.FloatTextureProjected2D;

/**
 * A {@link ShadedTexturePainter} that uses a 2D texture for hue, another 2D
 * texture for value and a fixed value for saturation and mixes to RGBA on the
 * GPU.
 *
 * @author borkholder
 */
public class HsvTexturePainter extends ShadedTexturePainter
{
    protected static final int hueTexUnit = 0;
    protected static final int valueTexUnit = hueTexUnit + 1;

    protected final HsvTextureProgram program;

    protected FloatTextureProjected2D hueTexture;
    protected FloatTextureProjected2D valueTexture;

    public HsvTexturePainter( )
    {
        this( new HsvTextureProgram( hueTexUnit, valueTexUnit ) );
    }

    public HsvTexturePainter( HsvTextureProgram program )
    {
        this.program = program;
        this.setProgram( this.program );
    }

    public void setHue( FloatTextureProjected2D texture )
    {
        this.painterLock.lock( );
        try
        {
            this.removeDrawableTexture( this.hueTexture );
            this.hueTexture = texture;
            this.addDrawableTexture( this.hueTexture, hueTexUnit );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setValue( FloatTextureProjected2D texture )
    {
        this.painterLock.lock( );
        try
        {
            this.removeDrawableTexture( this.valueTexture );
            this.valueTexture = texture;
            this.addDrawableTexture( this.valueTexture, valueTexUnit );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setDiscardNaN( boolean discard )
    {
        this.painterLock.lock( );
        try
        {
            this.program.setDiscardNan( discard );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setSaturation( float saturation )
    {
        this.painterLock.lock( );
        try
        {
            this.program.setSaturation( saturation );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setAlpha( float alpha )
    {
        this.painterLock.lock( );
        try
        {
            this.program.setAlpha( alpha );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public FloatTextureProjected2D getHueTexture( )
    {
        return this.hueTexture;
    }

    public FloatTextureProjected2D getValueTexture( )
    {
        return valueTexture;
    }
}
