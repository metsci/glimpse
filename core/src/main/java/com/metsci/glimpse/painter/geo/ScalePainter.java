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
package com.metsci.glimpse.painter.geo;

import static com.metsci.glimpse.support.font.FontUtils.*;

import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisNotSetException;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.ArrayColorProgram;
import com.metsci.glimpse.util.units.Length;

/**
 * Displays a simple distance scale in the lower right corner of the plot.
 *
 * @author ulman
 */
public class ScalePainter extends GlimpsePainterBase
{
    protected float[] borderColor = GlimpseColor.fromColorRgba( 0.0f, 0.0f, 0.0f, 1.0f );
    protected float[] primaryColor = GlimpseColor.fromColorRgba( 1.0f, 1.0f, 1.0f, 0.5f );
    protected float[] secondaryColor = GlimpseColor.fromColorRgba( 0.6f, 0.6f, 0.6f, 0.5f );
    protected float[] textColor = GlimpseColor.fromColorRgba( 0.0f, 0.0f, 0.0f, 1.0f );

    protected int bufferX;
    protected int bufferY;
    protected int pixelHeight;
    protected int pixelWidth;

    protected AxisUnitConverter converter;

    protected String unitLabel;

    protected TextRenderer tickTextRenderer;
    protected TextRenderer overallTextRenderer;

    protected NumberFormat formatter;

    protected boolean showTickLength = true;
    protected boolean showOverallLength = true;

    protected ArrayColorProgram fillProg;
    protected GLEditableBuffer fillXy;
    protected GLEditableBuffer fillRgba;

    protected LineProgram lineProg;
    protected LineStyle lineStyle;
    protected LinePath linePath;

    public ScalePainter( )
    {
        this.tickTextRenderer = createTickTextRenderer( );
        this.overallTextRenderer = createOverallTextRenderer( );

        this.converter = new AxisUnitConverter( )
        {

            @Override
            public double fromAxisUnits( double value )
            {
                return Length.fromNauticalMiles( value );
            }

            @Override
            public double toAxisUnits( double value )
            {
                return Length.toNauticalMiles( value );
            }

        };

        this.formatter = NumberFormat.getNumberInstance( );
        this.formatter.setGroupingUsed( false );

        this.unitLabel = "nmi";
        this.bufferX = 0;
        this.bufferY = 0;
        this.pixelHeight = 20;
        this.pixelWidth = 300;

        this.lineProg = new LineProgram( );
        this.fillProg = new ArrayColorProgram( );

        this.lineStyle = new LineStyle( );
        this.lineStyle.feather_PX = 0;
        this.lineStyle.stippleEnable = false;
        this.lineStyle.thickness_PX = 1.0f;
        this.lineStyle.rgba = borderColor;

        this.linePath = new LinePath( );
        this.fillXy = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
        this.fillRgba = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
    }

    protected TextRenderer createTickTextRenderer( )
    {
        return new TextRenderer( getDefaultBold( 11 ), false, false );
    }

    protected TextRenderer createOverallTextRenderer( )
    {
        return new TextRenderer( getDefaultBold( 16 ), false, false );
    }

    public float[] getBorderColor( )
    {
        return borderColor;
    }

    public void setBorderColor( float[] borderColor )
    {
        this.borderColor = borderColor;
        this.lineStyle.rgba = borderColor;
    }

    public float[] getPrimaryColor( )
    {
        return primaryColor;
    }

    public void setPrimaryColor( float[] primaryColor )
    {
        this.primaryColor = primaryColor;
    }

    public float[] getSecondaryColor( )
    {
        return secondaryColor;
    }

    public void setSecondaryColor( float[] secondaryColor )
    {
        this.secondaryColor = secondaryColor;
    }

    public float[] getTextColor( )
    {
        return textColor;
    }

    public void setTextColor( float[] textColor )
    {
        this.textColor = textColor;
    }

    public int getPixelBufferX( )
    {
        return bufferX;
    }

    public void setPixelBufferX( int buffer )
    {
        this.bufferX = buffer;
    }

    public int getPixelBufferY( )
    {
        return bufferY;
    }

    public void setPixelBufferY( int buffer )
    {
        this.bufferY = buffer;
    }

    public int getScaleHeight( )
    {
        return pixelHeight;
    }

    public void setPixelHeight( int pixelHeight )
    {
        this.pixelHeight = pixelHeight;
    }

    public int getPixelHeight( )
    {
        return pixelWidth;
    }

    public void setPixelWidth( int pixelWidth )
    {
        this.pixelWidth = pixelWidth;
    }

    public AxisUnitConverter getUnitConverter( )
    {
        return converter;
    }

    public void setUnitConverter( AxisUnitConverter converter )
    {
        this.converter = converter;
    }

    public String getUnitLabel( )
    {
        return unitLabel;
    }

    public void setUnitLabel( String unitLabel )
    {
        this.unitLabel = unitLabel;
    }

    public void setShowTickLength( boolean show )
    {
        this.showTickLength = show;
    }

    public void setShowOverallLength( boolean show )
    {
        this.showOverallLength = show;
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        if ( this.tickTextRenderer != null ) this.tickTextRenderer.dispose( );
        if ( this.overallTextRenderer != null ) this.overallTextRenderer.dispose( );

        this.overallTextRenderer = null;
        this.tickTextRenderer = null;

        this.linePath.dispose( context.getGL( ) );
        this.fillXy.dispose( context.getGL( ) );
        this.fillRgba.dispose( context.getGL( ) );

        this.lineProg.dispose( context.getGL( ).getGL3( ) );
        this.fillProg.dispose( context.getGL( ).getGL3( ) );
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        if ( this.tickTextRenderer == null ) return;
        if ( this.overallTextRenderer == null ) return;

        Axis1D axis = null;

        GlimpseTarget target = context.getTargetStack( ).getTarget( );
        if ( target instanceof GlimpseAxisLayout2D )
        {
            GlimpseAxisLayout2D layout = ( GlimpseAxisLayout2D ) target;
            axis = getAxis( layout.getAxis( context ) );
        }
        else if ( target instanceof GlimpseAxisLayout1D )
        {
            GlimpseAxisLayout1D layout = ( GlimpseAxisLayout1D ) target;
            axis = layout.getAxis( context );
        }

        if ( axis == null )
        {
            // Some GlimpseAxisLayout2D in the GlimpseContext must define an Axis2D
            throw new AxisNotSetException( this, context );
        }

        GlimpseBounds bounds = getBounds( context );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        double diff = axis.getMax( ) - axis.getMin( );
        double ratio = axis.getSizePixels( ) / converter.toAxisUnits( diff );

        double hintValue = pixelWidth / ratio;
        double hintOrder = Math.log10( hintValue );

        int order = ( int ) Math.ceil( hintOrder );

        double scaleValueSize = Math.pow( 10.0, order - 1 );
        double scalePixelSize = scaleValueSize * ratio;
        int tickCount = ( int ) Math.floor( pixelWidth / scalePixelSize );

        if ( tickCount < 5 )
        {
            scaleValueSize = scaleValueSize / 2;
            scalePixelSize = scaleValueSize * ratio;
            tickCount = ( int ) Math.floor( pixelWidth / scalePixelSize );
        }

        double totalSize = scalePixelSize * tickCount;

        GL3 gl = context.getGL( ).getGL3( );

        fillXy.clear( );
        fillRgba.clear( );

        for ( int i = 0; i < tickCount; i++ )
        {
            float[] color = i % 2 == 0 ? secondaryColor : primaryColor;

            double offset1 = totalSize * ( i / ( double ) tickCount );
            double offset2 = totalSize * ( ( i + 1 ) / ( double ) tickCount );

            fillXy.growQuad2f( ( float ) ( width - bufferX - offset1 ), ( bufferY ), ( float ) ( width - bufferX - offset2 ), bufferY + pixelHeight );

            fillRgba.growQuadSolidColor( color );
        }

        linePath.clear( );

        linePath.addRectangle( width - bufferX, ( bufferY ), ( float ) ( width - bufferX - totalSize ), bufferY + pixelHeight );

        GLUtils.enableStandardBlending( gl );
        try
        {
            fillProg.begin( gl );
            try
            {
                fillProg.setPixelOrtho( gl, bounds );

                fillProg.draw( gl, fillXy, fillRgba );
            }
            finally
            {
                fillProg.end( gl );
            }

            lineProg.begin( gl );
            try
            {
                lineProg.setPixelOrtho( gl, bounds );
                lineProg.setViewport( gl, bounds );

                lineProg.draw( gl, lineStyle, linePath );
            }
            finally
            {
                lineProg.end( gl );
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }

        if ( order < 2 )
        {
            formatter.setMaximumFractionDigits( Math.abs( order - 2 ) );
        }
        else
        {
            formatter.setMaximumFractionDigits( 0 );
        }

        if ( showTickLength )
        {
            String tickText = formatter.format( scaleValueSize ) + " " + unitLabel;

            Rectangle2D textBounds = tickTextRenderer.getBounds( tickText );

            int posX = ( int ) ( width - 1 - bufferX - textBounds.getWidth( ) - 1 );
            int posY = ( int ) ( bufferY + pixelHeight / 2 - textBounds.getHeight( ) / 2 );

            tickTextRenderer.beginRendering( width, height );
            try
            {
                GlimpseColor.setColor( tickTextRenderer, textColor );
                tickTextRenderer.draw( tickText, posX, posY );
            }
            finally
            {
                tickTextRenderer.endRendering( );
            }
        }

        if ( showOverallLength )
        {
            String overallText = formatter.format( scaleValueSize * tickCount ) + " " + unitLabel;

            Rectangle2D overallTextBounds = overallTextRenderer.getBounds( overallText );

            int posX = ( int ) ( width - pixelWidth / 2 - overallTextBounds.getWidth( ) / 2 );
            int posY = ( int ) ( bufferY + pixelHeight / 2 - overallTextBounds.getHeight( ) / 2 );

            overallTextRenderer.beginRendering( width, height );
            try
            {
                GlimpseColor.setColor( overallTextRenderer, textColor );
                overallTextRenderer.draw( overallText, posX, posY );
            }
            finally
            {
                overallTextRenderer.endRendering( );
            }
        }
    }

    protected Axis1D getAxis( Axis2D axis )
    {
        if ( axis != null )
        {
            return axis.getAxisX( );
        }
        else
        {
            return null;
        }
    }
}
