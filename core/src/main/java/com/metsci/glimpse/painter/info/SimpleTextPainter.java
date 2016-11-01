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
package com.metsci.glimpse.painter.info;

import static com.metsci.glimpse.support.font.FontUtils.*;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.opengl.math.Matrix4;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

/**
 * A painter which displays arbitrary text at a fixed pixel
 * location on the screen.
 *
 * @author ulman
 */
public class SimpleTextPainter extends GlimpsePainterBase
{
    private static final float PI_2 = ( float ) ( Math.PI / 2.0f );

    public static enum HorizontalPosition
    {
        Left, Center, Right;
    }

    public static enum VerticalPosition
    {
        Bottom, Center, Top;
    }

    protected float[] textColor = GlimpseColor.getWhite( );
    protected float[] textColorNoBackground = GlimpseColor.getBlack( );
    protected boolean textColorSet = false;

    protected boolean paintBackground = false;
    protected float[] backgroundColor = GlimpseColor.getBlack( 0.7f );
    protected boolean backgroundColorSet = false;

    protected boolean paintBorder = false;
    protected float[] borderColor = GlimpseColor.getWhite( 1f );
    protected boolean borderColorSet = false;

    protected int horizontalPadding = 5;
    protected int verticalPadding = 5;

    protected HorizontalPosition hPos;
    protected VerticalPosition vPos;

    protected TextRenderer textRenderer;
    protected boolean fontSet = false;

    protected String sizeText;
    protected String text;

    protected boolean horizontal = true;

    protected volatile Font newFont = null;
    protected volatile boolean antialias = false;

    protected FlatColorProgram fillProg;
    protected GLEditableBuffer fillBuffer;

    protected LineProgram lineProg;
    protected LinePath linePath;
    protected LineStyle lineStyle;

    protected Matrix4 transformMatrix;

    public SimpleTextPainter( )
    {
        this.newFont = getDefaultBold( 12 );
        this.hPos = HorizontalPosition.Left;
        this.vPos = VerticalPosition.Bottom;

        this.lineProg = new LineProgram( );
        this.fillProg = new FlatColorProgram( );

        this.lineStyle = new LineStyle( );
        this.lineStyle.stippleEnable = false;
        this.lineStyle.joinType = LineJoinType.JOIN_NONE;
        this.lineStyle.feather_PX = 0f;

        this.linePath = new LinePath( );
        this.fillBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );

        this.transformMatrix = new Matrix4( );
    }

    public SimpleTextPainter setHorizontalLabels( boolean horizontal )
    {
        this.horizontal = horizontal;
        return this;
    }

    public SimpleTextPainter setPaintBackground( boolean paintBackground )
    {
        this.paintBackground = paintBackground;
        return this;
    }

    public SimpleTextPainter setBackgroundColor( float[] backgroundColor )
    {
        this.backgroundColor = backgroundColor;
        this.backgroundColorSet = true;
        return this;
    }

    public SimpleTextPainter setPaintBorder( boolean paintBorder )
    {
        this.paintBorder = paintBorder;
        return this;
    }

    public SimpleTextPainter setBorderColor( float[] borderColor )
    {
        this.borderColor = borderColor;
        this.borderColorSet = true;
        return this;
    }

    public SimpleTextPainter setHorizontalPosition( HorizontalPosition hPos )
    {
        this.hPos = hPos;
        return this;
    }

    public SimpleTextPainter setVerticalPosition( VerticalPosition vPos )
    {
        this.vPos = vPos;
        return this;
    }

    public SimpleTextPainter setFont( Font font )
    {
        setFont( font, false );
        return this;
    }

    public SimpleTextPainter setFont( Font font, boolean antialias )
    {
        this.newFont = font;
        this.antialias = antialias;
        this.fontSet = true;
        return this;
    }

    public SimpleTextPainter setFont( int size, boolean bold )
    {
        setFont( size, bold, false );
        return this;
    }

    public SimpleTextPainter setFont( int size, boolean bold, boolean antialias )
    {
        if ( bold )
        {
            setFont( getDefaultBold( size ), antialias );
        }
        else
        {
            setFont( getDefaultPlain( size ), antialias );
        }

        return this;
    }

    public SimpleTextPainter setSizeText( String sizeText )
    {
        this.sizeText = sizeText;
        return this;
    }

    public SimpleTextPainter setText( String text )
    {
        this.text = text;
        return this;
    }

    public String getText( )
    {
        return this.text;
    }

    public SimpleTextPainter setPadding( int padding )
    {
        this.verticalPadding = padding;
        this.horizontalPadding = padding;
        return this;
    }

    public SimpleTextPainter setVerticalPadding( int padding )
    {
        this.verticalPadding = padding;
        return this;
    }

    public SimpleTextPainter setHorizontalPadding( int padding )
    {
        this.horizontalPadding = padding;
        return this;
    }

    public SimpleTextPainter setColor( float[] rgba )
    {
        textColor = rgba;
        textColorSet = true;
        return this;
    }

    public SimpleTextPainter setColor( float r, float g, float b, float a )
    {
        textColor[0] = r;
        textColor[1] = g;
        textColor[2] = b;
        textColor[3] = a;
        return this;
    }

    public int getVerticalPadding( )
    {
        return verticalPadding;
    }

    public int getHorizontalPadding( )
    {
        return horizontalPadding;
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
            textColor = laf.getColor( AbstractLookAndFeel.TOOLTIP_TEXT_COLOR );
            textColorNoBackground = laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR );
            textColorSet = false;
        }

        if ( !backgroundColorSet )
        {
            setBackgroundColor( laf.getColor( AbstractLookAndFeel.TOOLTIP_BACKGROUND_COLOR ) );
            backgroundColorSet = false;
        }

        if ( !borderColorSet )
        {
            setBorderColor( laf.getColor( AbstractLookAndFeel.BORDER_COLOR ) );
            borderColorSet = false;
        }
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }

    public Rectangle2D getTextBounds( )
    {
        return sizeText == null ? textRenderer.getBounds( text ) : textRenderer.getBounds( sizeText );
    }

    protected void paintToHorizontal( GL3 gl, GlimpseBounds bounds, Rectangle2D textBounds )
    {
        int xText = horizontalPadding;
        int yText = verticalPadding;

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        switch ( hPos )
        {
            case Left:
                xText = horizontalPadding;
                break;
            case Center:
                xText = ( int ) ( width / 2d - textBounds.getWidth( ) * 0.5 );
                break;
            case Right:
                xText = ( int ) ( width - textBounds.getWidth( ) - horizontalPadding );
                break;
        }

        switch ( vPos )
        {
            case Bottom:
                yText = verticalPadding;
                break;
            case Center:
                yText = ( int ) ( height / 2d - textBounds.getHeight( ) * 0.5 );
                break;
            case Top:
                yText = ( int ) ( height - textBounds.getHeight( ) - verticalPadding );
                break;
        }

        if ( paintBackground || paintBorder )
        {
            Rectangle2D bound = sizeText == null ? textRenderer.getBounds( text ) : textRenderer.getBounds( sizeText );

            int xTextMax = ( int ) ( xText + bound.getWidth( ) + ( bound.getMinX( ) ) - 1 );
            int yTextMax = ( int ) ( yText + bound.getHeight( ) - 3 );

            if ( paintBackground )
            {
                this.fillBuffer.clear( );
                this.fillBuffer.growQuad2f( xText - 0.5f - 2, yText - 0.5f - 2, xTextMax + 0.5f + 2, yTextMax + 0.5f + 2 );

                this.fillProg.begin( gl );
                try
                {
                    this.fillProg.setPixelOrtho( gl, bounds );

                    this.fillProg.draw( gl, this.fillBuffer, this.backgroundColor );
                }
                finally
                {
                    this.fillProg.end( gl );
                }
            }

            if ( paintBorder )
            {
                this.linePath.clear( );
                this.linePath.addRectangle( xText - 0.5f - 2, yText - 0.5f - 2, xTextMax + 0.5f + 2, yTextMax + 0.5f + 2 );

                this.lineProg.begin( gl );
                try
                {
                    this.lineProg.setPixelOrtho( gl, bounds );
                    this.lineProg.setViewport( gl, bounds );

                    this.lineStyle.rgba = borderColor;

                    this.lineProg.draw( gl, this.lineStyle, this.linePath );
                }
                finally
                {
                    this.lineProg.end( gl );
                }
            }
        }

        textRenderer.beginRendering( width, height );
        try
        {
            if ( !textColorSet && !paintBackground )
            {
                GlimpseColor.setColor( textRenderer, textColorNoBackground );
            }
            else
            {
                GlimpseColor.setColor( textRenderer, textColor );
            }

            textRenderer.draw( text, xText, yText );
        }
        finally
        {
            textRenderer.endRendering( );
        }
    }

    protected void paintToVertical( GL3 gl, GlimpseBounds bounds, Rectangle2D textBounds )
    {
        int xText = horizontalPadding;
        int yText = verticalPadding;

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        double textWidth = textBounds.getWidth( );
        double textHeight = textBounds.getHeight( );

        int halfTextWidth = ( int ) ( textWidth / 2d );
        int halfTextHeight = ( int ) ( textHeight / 2d );

        switch ( hPos )
        {
            case Left:
                xText = horizontalPadding - halfTextWidth + halfTextHeight;
                break;
            case Center:
                xText = ( int ) ( width / 2d - halfTextWidth );
                break;
            case Right:
                xText = width - halfTextWidth - halfTextHeight - horizontalPadding;
                break;
        }

        switch ( vPos )
        {
            case Bottom:
                yText = verticalPadding - halfTextHeight + halfTextWidth;
                break;
            case Center:
                yText = ( int ) ( height / 2d - halfTextHeight );
                break;
            case Top:
                yText = height - halfTextHeight - halfTextWidth - verticalPadding;
                break;
        }

        if ( paintBackground || paintBorder )
        {
            int buffer = 2;

            int xTextMin = xText + halfTextWidth - halfTextHeight - buffer;
            int yTextMin = yText + halfTextWidth + halfTextHeight + buffer;

            int xTextMax = xText + halfTextWidth + halfTextHeight + buffer + 3;
            int yTextMax = yText - halfTextWidth + halfTextHeight - buffer;

            if ( paintBackground )
            {
                this.fillBuffer.clear( );
                this.fillBuffer.growQuad2f( xTextMin, yTextMin, xTextMax, yTextMax );

                this.fillProg.begin( gl );
                try
                {
                    this.fillProg.setPixelOrtho( gl, bounds );

                    this.fillProg.draw( gl, this.fillBuffer, this.backgroundColor );
                }
                finally
                {
                    this.fillProg.end( gl );
                }
            }

            if ( paintBorder )
            {
                this.linePath.clear( );
                this.linePath.addRectangle( xTextMin, yTextMin, xTextMax, yTextMax );

                this.lineProg.begin( gl );
                try
                {
                    this.lineProg.setPixelOrtho( gl, bounds );
                    this.lineProg.setViewport( gl, bounds );

                    this.lineStyle.rgba = borderColor;

                    this.lineProg.draw( gl, this.lineStyle, this.linePath );
                }
                finally
                {
                    this.lineProg.end( gl );
                }
            }
        }

        textRenderer.begin3DRendering( );
        try
        {
            float xShift = xText + halfTextWidth;
            float yShift = yText + halfTextHeight;

            transformMatrix.loadIdentity( );
            transformMatrix.makeOrtho( 0, width, 0, height, -1, 1 );
            transformMatrix.translate( xShift, yShift, 0 );
            transformMatrix.rotate( PI_2, 0, 0, 1.0f );
            transformMatrix.translate( -xShift, -yShift, 0 );

            textRenderer.setTransform( transformMatrix.getMatrix( ) );

            if ( !textColorSet && !paintBackground )
            {
                GlimpseColor.setColor( textRenderer, textColorNoBackground );
            }
            else
            {
                GlimpseColor.setColor( textRenderer, textColor );
            }

            textRenderer.draw3D( text, xText, yText, 0, 1 );
        }
        finally
        {
            textRenderer.end3DRendering( );
        }
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        if ( newFont != null )
        {
            updateTextRenderer( );
        }

        if ( text == null || textRenderer == null ) return;

        GlimpseBounds bounds = getBounds( context );
        GL3 gl = context.getGL( ).getGL3( );

        Rectangle2D textBounds = sizeText == null ? textRenderer.getBounds( text ) : textRenderer.getBounds( sizeText );

        GLUtils.enableStandardBlending( gl );
        try
        {
            if ( horizontal )
            {
                paintToHorizontal( gl, bounds, textBounds );
            }
            else
            {
                paintToVertical( gl, bounds, textBounds );
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }
    }

    protected void updateTextRenderer( )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = new TextRenderer( newFont, antialias, false );
        newFont = null;
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        this.textRenderer.dispose( );

        this.lineProg.dispose( context.getGL( ).getGL3( ) );
        this.fillProg.dispose( context.getGL( ).getGL3( ) );

        this.linePath.dispose( context.getGL( ) );
        this.fillBuffer.dispose( context.getGL( ) );
    }
}
