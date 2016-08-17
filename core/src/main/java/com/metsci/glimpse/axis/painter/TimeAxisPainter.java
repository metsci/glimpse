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
package com.metsci.glimpse.axis.painter;

import java.awt.Font;

import javax.media.opengl.GLContext;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.time.TimeAxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * A painter for displaying timeline axes. Axis values are interpreted as offsets
 * from an epoch. Both dates and times are displayed with a configurable format
 * and time zone.
 *
 * @author ulman
 */
public abstract class TimeAxisPainter extends NumericAxisPainter
{
    protected float[] tickColor;
    protected float[] textColor;

    protected TimeAxisLabelHandler handler;

    protected TextRenderer textRenderer;
    protected volatile Font newFont = null;
    protected volatile boolean antialias = false;

    protected boolean showDateLabels = true;
    protected boolean showCurrentTimeLabel = false;
    protected float[] currentTimeTextColor;
    protected float[] currentTimeTickColor;
    protected float currentTimeLineThickness;

    protected int hoverLabelOffset = 4;
    protected int tickLineLength = 4;

    protected boolean fontSet = false;
    protected boolean tickColorSet = false;
    protected boolean labelColorSet = false;

    public TimeAxisPainter( TimeAxisLabelHandler handler )
    {
        this.handler = handler;

        this.newFont = FontUtils.getBitstreamVeraSansPlain( 12.0f );

        this.tickColor = GlimpseColor.getBlack( );
        this.textColor = GlimpseColor.getBlack( );

        this.setCurrentTimeTextColor( GlimpseColor.getGreen( 0.5f ) );
        this.setCurrentTimeTickColor( GlimpseColor.getGreen( 1.0f ) );
        this.currentTimeLineThickness = 3;
    }

    public boolean isShowDateLabels( )
    {
        return this.showDateLabels;
    }

    public void setShowDateLabels( boolean show )
    {
        this.showDateLabels = show;
    }

    public TimeAxisLabelHandler getLabelHandler( )
    {
        return this.handler;
    }

    public void setLabelHandler( TimeAxisLabelHandler handler )
    {
        this.handler = handler;
    }
    
    public void setTickLineLength( int pixels )
    {
        this.tickLineLength = pixels;
    }

    public void setPixelsBetweenTicks( int pixels )
    {
        this.handler.setPixelsBetweenTicks( pixels );
    }

    public void setEpoch( Epoch epoch )
    {
        this.handler.setEpoch( epoch );
    }

    public Epoch getEpoch( )
    {
        return this.handler.getEpoch( );
    }

    public TimeStamp toTimeStamp( double time )
    {
        return this.handler.getEpoch( ).toTimeStamp( time );
    }

    public double fromTimeStamp( TimeStamp time )
    {
        return this.handler.getEpoch( ).fromTimeStamp( time );
    }

    public void setCurrentTimeTickColor( float[] color )
    {
        this.currentTimeTickColor = color;
    }

    public void setCurrentTimeTextColor( float[] color )
    {
        this.currentTimeTextColor = color;
    }

    public void showCurrentTimeLabel( boolean show )
    {
        this.showCurrentTimeLabel = show;
    }

    public void setFont( Font font )
    {
        setFont( font, true );
    }

    public void setTickColor( float[] color )
    {
        this.tickColor = color;
        this.tickColorSet = true;
    }

    public void setTextColor( float[] color )
    {
        this.textColor = color;
        this.labelColorSet = true;
    }

    public void setFont( Font font, boolean antialias )
    {
        this.newFont = font;
        this.antialias = antialias;
        this.fontSet = true;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        if ( laf == null ) return;

        // ignore the look and feel if a font has been manually set
        if ( !fontSet )
        {
            setFont( laf.getFont( AbstractLookAndFeel.AXIS_FONT ), false );
            fontSet = false;
        }

        if ( !labelColorSet )
        {
            setTextColor( laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR ) );
            labelColorSet = false;
        }

        if ( !tickColorSet )
        {
            setTickColor( laf.getColor( AbstractLookAndFeel.AXIS_TICK_COLOR ) );
            tickColorSet = false;
        }
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        if ( newFont != null )
        {
            if ( textRenderer != null ) textRenderer.dispose( );
            textRenderer = new TextRenderer( newFont, antialias, false );
            newFont = null;
        }
    }
}
