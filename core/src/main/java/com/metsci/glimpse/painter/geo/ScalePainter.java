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

import static com.metsci.glimpse.support.font.FontUtils.getDefaultBold;

import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisNotSetException;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.units.Length;

/**
 * Displays a simple distance scale in the lower right corner of the plot.
 *
 * @author ulman
 */
public class ScalePainter extends GlimpsePainterImpl
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
    public void dispose( GLContext context )
    {
        if ( tickTextRenderer != null ) tickTextRenderer.dispose( );
        if ( overallTextRenderer != null ) overallTextRenderer.dispose( );

        overallTextRenderer = null;
        tickTextRenderer = null;
    }

    @Override
    protected void paintTo( GlimpseContext context, GlimpseBounds bounds )
    {
        if ( tickTextRenderer == null ) return;
        if ( overallTextRenderer == null ) return;

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

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        context.getTargetStack( );

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

        GL2 gl = context.getGL( ).getGL2( );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( 0, width, 0, height, -1, 1 );
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity( );

        gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL2.GL_BLEND );

        gl.glBegin( GL2.GL_QUADS );
        try
        {
            for ( int i = 0; i < tickCount; i++ )
            {
                if ( i % 2 == 0 )
                    gl.glColor4fv( secondaryColor, 0 );
                else
                    gl.glColor4fv( primaryColor, 0 );

                double offset1 = totalSize * ( i / ( double ) tickCount );
                double offset2 = totalSize * ( ( i + 1 ) / ( double ) tickCount );

                gl.glVertex2d( width - bufferX - offset1, bufferY );
                gl.glVertex2d( width - bufferX - offset2, bufferY );
                gl.glVertex2d( width - bufferX - offset2, bufferY + pixelHeight );
                gl.glVertex2d( width - bufferX - offset1, bufferY + pixelHeight );
            }
        }
        finally
        {
            gl.glEnd( );
        }

        gl.glLineWidth( 1f );
        gl.glColor4fv( borderColor, 0 );

        gl.glBegin( GL2.GL_LINE_LOOP );
        try
        {
            gl.glVertex2d( width - bufferX, bufferY );
            gl.glVertex2d( width - bufferX - totalSize, bufferY );
            gl.glVertex2d( width - bufferX - totalSize, bufferY + pixelHeight );
            gl.glVertex2d( width - bufferX, bufferY + pixelHeight );
        }
        finally
        {
            gl.glEnd( );
        }

        gl.glDisable( GL2.GL_BLEND );
        gl.glTranslatef( 0.375f, 0.375f, 0 );

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
