/*
 * Copyright (c) 2019, Metron, Inc.
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

import java.util.function.DoubleSupplier;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.support.projection.Projection;
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
    protected static final int valuesTexUnit = 0;
    protected static final int colormapTexUnit = valuesTexUnit + 1;


    protected final HeatMapProgram program;

    protected FloatTextureProjected2D valuesTexture;
    protected ColorTexture1D colorTable;


    public HeatMapPainter( Axis1D colorAxis )
    {
        this( colorAxis::getMin, colorAxis::getMax );
    }

    public HeatMapPainter( Tag colormapMinTag, Tag colormapMaxTag )
    {
        this( colormapMinTag::getValue, colormapMaxTag::getValue );
    }

    public HeatMapPainter( DoubleSupplier colormapMinFn, DoubleSupplier colormapMaxFn )
    {
        this( new BasicHeatMapProgram( valuesTexUnit, colormapTexUnit, colormapMinFn, colormapMaxFn ) );
    }

    public HeatMapPainter( HeatMapProgram program )
    {
        this.program = program;
        this.setProgram( this.program );
    }

    public void setData( FloatTextureProjected2D texture )
    {
        this.painterLock.lock( );
        try
        {
            this.removeDrawableTexture( this.valuesTexture );
            this.valuesTexture = texture;
            this.addDrawableTexture( this.valuesTexture, valuesTexUnit );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setUseColormapAlpha( boolean useColormapAlpha )
    {
        this.painterLock.lock( );
        try
        {
            this.program.setUseColormapAlpha( useColormapAlpha );
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

    public void setDiscardBelow( boolean discard )
    {
        this.painterLock.lock( );
        try
        {
            this.program.setDiscardBelow( discard );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setDiscardAbove( boolean discard )
    {
        this.painterLock.lock( );
        try
        {
            this.program.setDiscardAbove( discard );
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

    public void setColorScale( ColorTexture1D texture )
    {
        this.painterLock.lock( );
        try
        {
            this.removeNonDrawableTexture( this.colorTable );
            this.colorTable = texture;
            this.addNonDrawableTexture( this.colorTable, colormapTexUnit );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public FloatTextureProjected2D getData( )
    {
        this.painterLock.lock( );
        try
        {
            return this.valuesTexture;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public Projection getProjection( )
    {
        this.painterLock.lock( );
        try
        {
            return ( this.valuesTexture == null ? null : this.valuesTexture.getProjection( ) );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public ColorTexture1D getColorScale( )
    {
        this.painterLock.lock( );
        try
        {
            return ( this.valuesTexture == null ? null : this.colorTable );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

}
