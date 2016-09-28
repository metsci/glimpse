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

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.io.IOException;
import java.util.logging.Logger;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.gl.texture.DrawableTextureProgram;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.shader.colormap.ColorMapProgram;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;

/**
 * A simplified facade to {@link ShadedTexturePainter} which applies
 * a color scale (adjustable via an {@link com.metsci.glimpse.axis.Axis1D}
 * to a double[][] rectangular array of data stored as an OpenGL
 * texture in order to display a two dimensional heat map.
 *
 * @author ulman
 */
public class HeatMapPainter extends ShadedTexturePainter
{
    public static final Logger logger = Logger.getLogger( HeatMapPainter.class.getName( ) );

    protected FloatTextureProjected2D heatMap;
    protected ColorTexture1D colorScale;

    protected DrawableTextureProgram program;

    public HeatMapPainter( Axis1D axis )
    {
        try
        {
            this.loadDefaultPipeline( axis );
        }
        catch ( IOException e )
        {
            logWarning( logger, "Unable to load HeatMapPainter shader.", e );
        }
    }

    protected void loadDefaultPipeline( Axis1D axis ) throws IOException
    {
        this.program = new ColorMapProgram( axis, DEFAULT_DRAWABLE_TEXTURE_UNIT, DEFAULT_NONDRAWABLE_TEXTURE_UNIT );
        this.setProgram( this.program );
    }

    private ColorMapProgram getProgram( )
    {
        return ( ColorMapProgram ) this.program;
    }

    public void setDiscardNaN( boolean discard )
    {
        painterLock.lock( );
        try
        {
            getProgram( ).setDiscardNaN( discard );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setAlpha( float alpha )
    {
        painterLock.lock( );
        try
        {
            getProgram( ).setAlpha( alpha );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setData( FloatTextureProjected2D texture )
    {
        painterLock.lock( );
        try
        {
            this.removeDrawableTexture( heatMap );
            this.heatMap = texture;
            this.addDrawableTexture( heatMap, DEFAULT_DRAWABLE_TEXTURE_UNIT );
        }
        finally
        {
            painterLock.unlock( );
        }

    }

    public void setColorScale( ColorTexture1D texture )
    {
        painterLock.lock( );
        try
        {
            this.removeNonDrawableTexture( colorScale );
            this.colorScale = texture;
            this.addNonDrawableTexture( colorScale, DEFAULT_NONDRAWABLE_TEXTURE_UNIT );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public ColorTexture1D getColorScale( )
    {
        painterLock.lock( );
        try
        {
            if ( heatMap != null )
            {
                return colorScale;
            }
            else
            {
                return null;
            }
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public FloatTextureProjected2D getData( )
    {
        painterLock.lock( );
        try
        {
            if ( heatMap != null )
            {
                return heatMap;
            }
            else
            {
                return null;
            }
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public Projection getProjection( )
    {
        painterLock.lock( );
        try
        {
            if ( heatMap != null )
            {
                return heatMap.getProjection( );
            }
            else
            {
                return null;
            }
        }
        finally
        {
            painterLock.unlock( );
        }
    }
}
