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
package com.metsci.glimpse.plot.timeline.group;

import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

public class GroupLabelPainter extends GlimpsePainterBase
{
    protected int buttonSize = 8;
    protected int padding = 5;

    protected float[] lineColor = GlimpseColor.getBlack( );

    protected SimpleTextPainter textDelegate;

    protected boolean isExpanded = true;

    protected boolean showDivider = true;
    protected boolean showArrow = true;

    protected LineProgram lineProg;
    protected LinePath linePath;
    protected LineStyle lineStyle;

    protected FlatColorProgram fillProg;
    protected GLEditableBuffer fillPath;

    public GroupLabelPainter( String name )
    {
        this.textDelegate = new SimpleTextPainter( );
        this.textDelegate.setHorizontalPosition( HorizontalPosition.Left );
        this.textDelegate.setVerticalPosition( VerticalPosition.Top );
        this.textDelegate.setHorizontalLabels( true );
        this.textDelegate.setHorizontalPadding( buttonSize + padding * 2 );
        this.textDelegate.setVerticalPadding( 0 );
        this.textDelegate.setText( name );
        this.textDelegate.setFont( FontUtils.getDefaultPlain( 14 ), true );

        this.lineProg = new LineProgram( );
        this.linePath = new LinePath( );
        this.lineStyle = new LineStyle( );
        this.lineStyle.joinType = LineJoinType.JOIN_NONE;
        this.lineStyle.feather_PX = 0;
        this.lineStyle.thickness_PX = 1;
        this.lineStyle.stippleEnable = false;

        this.fillProg = new FlatColorProgram( );
        this.fillPath = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
    }

    public SimpleTextPainter getTextPainter( )
    {
        return this.textDelegate;
    }

    public void setExpanded( boolean isExpanded )
    {
        this.isExpanded = isExpanded;
    }

    public void setText( String text )
    {
        this.textDelegate.setText( text );
    }

    public void setShowArrow( boolean show )
    {
        this.showArrow = show;
    }

    public boolean isShowArrow( )
    {
        return this.showArrow;
    }

    public void setShowDivider( boolean show )
    {
        this.showDivider = show;
    }

    public boolean isShowDivider( )
    {
        return this.showDivider;
    }

    public void setDividerColor( float[] color )
    {
        this.lineColor = color;
    }

    public float[] getDividerColor( )
    {
        return lineColor;
    }

    public void setArrowSpacing( int size )
    {
        this.padding = size;
    }

    public int getArrowSpacing( )
    {
        return this.padding;
    }

    public void setArrowSize( int size )
    {
        this.buttonSize = size;
    }

    public int getArrowSize( )
    {
        return this.buttonSize;
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        this.textDelegate.paintTo( context );

        GlimpseBounds bounds = getBounds( context );
        GL3 gl = context.getGL( ).getGL3( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        // Paint Line
        if ( showDivider )
        {
            Rectangle2D textBounds = this.textDelegate.getTextBounds( );
            float startY = height / 2.0f;
            float startX = ( float ) ( padding + this.textDelegate.getHorizontalPadding( ) + textBounds.getWidth( ) + ( textBounds.getMinX( ) ) - 1 );

            lineProg.begin( gl );
            try
            {
                lineProg.setPixelOrtho( gl, bounds );
                lineProg.setViewport( gl, bounds );

                linePath.clear( );

                linePath.moveTo( startX, startY );
                linePath.lineTo( width, startY );

                lineStyle.rgba = lineColor;

                lineProg.draw( gl, lineStyle, linePath );

            }
            finally
            {
                lineProg.end( gl );
            }

        }

        if ( showArrow )
        {
            float halfSize = buttonSize / 2.0f;
            float centerX = halfSize + padding;
            float centerY = height / 2.0f;

            // Paint Expand/Collapse Button
            fillProg.begin( gl );
            try
            {
                fillProg.setPixelOrtho( gl, bounds );

                fillPath.clear( );

                if ( isExpanded )
                {
                    fillPath.grow2f( centerX - halfSize, centerY + halfSize );
                    fillPath.grow2f( centerX + halfSize, centerY + halfSize );
                    fillPath.grow2f( centerX, centerY - halfSize );
                }
                else
                {
                    fillPath.grow2f( centerX - halfSize, centerY - halfSize );
                    fillPath.grow2f( centerX - halfSize, centerY + halfSize );
                    fillPath.grow2f( centerX + halfSize, centerY );
                }

                fillProg.draw( gl, fillPath, lineColor );
            }
            finally
            {
                fillProg.end( gl );
            }
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        this.fillPath.dispose( gl );
        this.linePath.dispose( gl );
        this.fillProg.dispose( gl );
        this.lineProg.dispose( gl );

        this.textDelegate.dispose( context );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        super.setLookAndFeel( laf );

        if ( laf != null )
        {
            this.textDelegate.setLookAndFeel( laf );
            this.lineColor = laf.getColor( AbstractLookAndFeel.BORDER_COLOR );
        }
    }
}