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

import static com.metsci.glimpse.support.color.GlimpseColor.*;
import static com.metsci.glimpse.support.font.FontUtils.*;

import java.awt.Font;

import javax.media.opengl.GLContext;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.painter.base.GlimpsePainter1D;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A simple axis painter which displays labeled ticks at regular
 * intervals along the length of the axis. The ticks are spaced
 * so that they fall on round numbered values.
 *
 * @author ulman
 */
public abstract class NumericAxisPainter extends GlimpsePainter1D
{
    protected int tickBufferSize = 0;
    protected int tickSize = 8;
    protected int textBufferSize = 1;
    protected int labelBufferSize = 1;
    protected int tickLineWidth = 1;
    protected int markerWidth = 2;

    protected boolean keepLabelsForExtremaFullyVisible = true;
    protected boolean showLabelsForOffscreenTicks = false;
    protected boolean showSelectionLine = false;
    protected boolean showLabel = true;
    protected boolean showMinorTicks = false;
    protected boolean showTickLabels = true;

    protected TextRenderer textRenderer;
    protected volatile Font newFont = null;
    protected volatile boolean antialias = false;

    protected float[] tickColor;
    protected float[] tickLabelColor;
    protected float[] axisLabelColor;

    protected boolean fontSet = false;
    protected boolean tickColorSet = false;
    protected boolean labelColorSet = false;

    public NumericAxisPainter( )
    {

        resetFont( );
        resetLabelColors( );
        resetTickColor( );
    }

    public void setShowLabelsForOffscreenTicks( boolean show )
    {
        this.showLabelsForOffscreenTicks = show;
    }

    public void setKeepLabelsForExtremaFullyVisible( boolean keepFullyVisible )
    {
        this.keepLabelsForExtremaFullyVisible = keepFullyVisible;
    }

    public void setShowTickLabels( boolean show )
    {
        this.showTickLabels = show;
    }

    public void setShowLabel( boolean show )
    {
        this.showLabel = show;
    }

    public void setFont( Font font )
    {
        setFont( font, false );
    }

    public void setFont( Font font, boolean antialias )
    {
        this.newFont = font;
        this.antialias = antialias;
        this.fontSet = true;
    }

    public void resetFont( )
    {
        setFont( getDefaultPlain( 12 ), false );
        this.fontSet = false;
    }

    public void setTickSize( int size )
    {
        this.tickSize = size;
    }

    public void setTickBufferSize( int size )
    {
        this.tickBufferSize = size;
    }

    public void setTickLabelBufferSize( int size )
    {
        this.textBufferSize = size;
    }

    public void setAxisLabelBufferSize( int size )
    {
        this.labelBufferSize = size;
    }

    public void setMarkerWidth( int width )
    {
        this.markerWidth = width;
    }

    public void setShowMarker( boolean show )
    {
        this.showSelectionLine = show;
    }

    public void setTickColor( float[] color )
    {
        this.tickColor = color;
        this.tickColorSet = true;
    }

    public void resetTickColor( )
    {
        setTickColor( getBlack( ) );
        this.tickColorSet = false;
    }

    public void setTickLabelColor( float[] color )
    {
        this.tickLabelColor = color;
        this.labelColorSet = true;
    }

    public void setAxisLabelColor( float[] color )
    {
        this.axisLabelColor = color;
        this.labelColorSet = true;
    }

    public void resetLabelColors( )
    {
        setTickLabelColor( getBlack( ) );
        setAxisLabelColor( getBlack( ) );
        this.labelColorSet = false;
    }

    public void setShowMinorTicks( boolean show )
    {
        this.showMinorTicks = show;
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
            setAxisLabelColor( laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR ) );
            setTickLabelColor( laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR ) );
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

    public void updateTextRenderer( )
    {
        if ( newFont != null )
        {
            if ( textRenderer != null ) textRenderer.dispose( );
            textRenderer = new TextRenderer( newFont, antialias, false );
            newFont = null;
        }
    }
}
