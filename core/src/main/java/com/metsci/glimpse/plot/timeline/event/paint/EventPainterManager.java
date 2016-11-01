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
package com.metsci.glimpse.plot.timeline.event.paint;

import java.awt.Font;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL;

import com.google.common.collect.Lists;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.event.Event;
import com.metsci.glimpse.plot.timeline.event.EventManager;
import com.metsci.glimpse.plot.timeline.event.EventManager.Row;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * Paints 1D events with associated color, time span, icon, and label information.
 *
 * @author ulman
 */
public class EventPainterManager extends GlimpsePainterBase
{
    protected EventPlotInfo plot;
    protected EventManager manager;

    protected TextureAtlas atlas;
    protected TextRenderer textRenderer;
    protected boolean fontSet = false;

    protected volatile Font newFont = null;
    protected volatile boolean antialias = false;

    protected float borderThickness = 1.0f;

    protected float[] backgroundColor = GlimpseColor.getGray( 0.2f );
    protected float[] borderColor = GlimpseColor.getWhite( 1f );
    protected float[] textColor = GlimpseColor.getBlack( );
    protected float[] textColorNoBackground = GlimpseColor.getBlack( );

    protected boolean borderThicknessSet = false;
    protected boolean textColorSet = false;
    protected boolean backgroundColorSet = false;
    protected boolean borderColorSet = false;

    protected boolean isHorizontal = true;

    protected GroupedEventPainter defaultPainter;

    public EventPainterManager( EventPlotInfo plot, EventManager manager, Epoch epoch, TextureAtlas atlas )
    {
        this.plot = plot;
        this.manager = manager;

        this.atlas = atlas;
        this.newFont = FontUtils.getDefaultPlain( 12 );
        this.isHorizontal = plot.getStackedTimePlot( ).isTimeAxisHorizontal( );

        this.defaultPainter = new DefaultGroupedEventPainter( );
    }

    public void setEventPainter( GroupedEventPainter painter )
    {
        this.defaultPainter = painter;
    }

    public GroupedEventPainter getEventPainter( )
    {
        return this.defaultPainter;
    }

    public boolean isBorderThicknessSet( )
    {
        return borderThicknessSet;
    }

    public boolean isTextColorSet( )
    {
        return textColorSet;
    }

    public boolean isBackgroundColorSet( )
    {
        return backgroundColorSet;
    }

    public boolean isBorderColorSet( )
    {
        return borderColorSet;
    }

    public float getBorderThickness( )
    {
        return borderThickness;
    }

    public void setBorderThickness( float thickness )
    {
        this.borderThickness = thickness;
        this.borderThicknessSet = true;
    }

    public float[] getBackgroundColor( )
    {
        return backgroundColor;
    }

    public void setBackgroundColor( float[] backgroundColor )
    {
        this.backgroundColor = backgroundColor;
        this.backgroundColorSet = true;
    }

    public float[] getBorderColor( )
    {
        return borderColor;
    }

    public void setBorderColor( float[] borderColor )
    {
        this.borderColor = borderColor;
        this.borderColorSet = true;
    }

    public float[] getTextColorNoBackground( )
    {
        return this.textColorNoBackground;
    }

    public float[] getTextColor( )
    {
        return textColor;
    }

    public void setTextColor( float[] textColor )
    {
        this.textColor = textColor;
        this.textColorSet = true;
    }

    public TextRenderer getTextRenderer( )
    {
        return this.textRenderer;
    }

    public TextureAtlas getTextureAtlas( )
    {
        return this.atlas;
    }

    public EventPainterManager setFont( Font font, boolean antialias )
    {
        this.newFont = font;
        this.antialias = antialias;
        this.fontSet = true;
        return this;
    }

    public boolean isHorizontal( )
    {
        return this.isHorizontal;
    }

    public Epoch getEpoch( )
    {
        return this.plot.getStackedTimePlot( ).getEpoch( );
    }

    public EventPlotInfo getEventPlotInfo( )
    {
        return this.plot;
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis1D axis = requireAxis1D( context );
        GL gl = context.getGL( );

        if ( newFont != null )
        {
            if ( textRenderer != null ) textRenderer.dispose( );
            textRenderer = new TextRenderer( newFont, antialias, false );
            newFont = null;
        }

        if ( textRenderer == null ) return;

        GLUtils.enableStandardBlending( gl );
        manager.lock( );
        try
        {
            manager.calculateVisibleEvents( axis );

            int buffer = plot.getEventPadding( );
            double rowSize = plot.getRowSize( bounds );

            double posMin = buffer;
            double posMax = buffer + rowSize;

            List<Row> rows = manager.getRows( );

            Collection<EventDrawInfo> events = Lists.newLinkedList( );

            int count = rows.size( );
            for ( int i = 0; i < count; i++ )
            {
                Row row = rows.get( i );

                Event prev = null;
                for ( Event next : row.visibleEvents )
                {
                    if ( prev != null )
                    {
                        if ( prev.getEventPainter( ) == null )
                        {
                            events.add( new EventDrawInfo( prev, next, ( int ) posMin, ( int ) posMax ) );
                        }
                        else
                        {
                            prev.getEventPainter( ).paint( context, prev, next, plot, ( int ) posMin, ( int ) posMax );
                        }
                    }

                    prev = next;
                }

                // paint last event
                if ( prev != null )
                {
                    if ( prev.getEventPainter( ) == null )
                    {
                        events.add( new EventDrawInfo( prev, null, ( int ) posMin, ( int ) posMax ) );
                    }
                    else
                    {
                        prev.getEventPainter( ).paint( context, prev, null, plot, ( int ) posMin, ( int ) posMax );
                    }
                }

                posMin = posMax + buffer;
                posMax = posMax + buffer + rowSize;
            }

            // paint all the events which did not have a custom painter
            defaultPainter.paint( context, plot, events );
        }
        finally
        {
            manager.unlock( );
            GLUtils.disableBlending( gl );
        }
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a font has been manually set
        if ( !fontSet )
        {
            setFont( laf.getFont( AbstractLookAndFeel.TITLE_FONT ), false );
            fontSet = false;
        }

        if ( !textColorSet )
        {
            textColor = laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR );
            textColorNoBackground = laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR );
            textColorSet = false;
        }

        if ( !borderColorSet )
        {
            setBorderColor( laf.getColor( AbstractLookAndFeel.BORDER_COLOR ) );
            borderColorSet = false;
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        this.atlas.dispose( );
        this.textRenderer.dispose( );
    }
}
