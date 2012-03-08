/*
 * Copyright (c) 2012, Metron, Inc.
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

import static com.metsci.glimpse.support.font.FontUtils.getDefaultBold;
import static com.metsci.glimpse.support.font.FontUtils.getDefaultPlain;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.sun.opengl.util.j2d.TextRenderer;

/**
 * A painter which displays arbitrary text at a fixed pixel
 * location on the screen.
 *
 * @author ulman
 */
public class SimpleTextPainter extends GlimpsePainterImpl
{
    public enum HorizontalPosition
    {
        Left, Center, Right;
    }

    public enum VerticalPosition
    {
        Bottom, Center, Top;
    }

    private Font textFont;
    private float[] textColor = GlimpseColor.getBlack( );

    private boolean paintBackground = false;
    private float[] backgroundColor = GlimpseColor.getBlack( 0.3f );

    private int padding = 5;

    protected HorizontalPosition hPos = HorizontalPosition.Left;
    protected VerticalPosition vPos = VerticalPosition.Bottom;

    private TextRenderer textRenderer;

    private String sizeText;
    private String text;

    private boolean fontSet = false;

    public SimpleTextPainter( )
    {
        setFont( 12, true, false );
    }

    public SimpleTextPainter setPaintBackground( boolean paintBackground )
    {
        this.paintBackground = paintBackground;
        return this;
    }

    public SimpleTextPainter setBackgroundColor( float[] backgroundColor )
    {
        this.backgroundColor = backgroundColor;
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
        setFont( font, true );
        return this;
    }

    public SimpleTextPainter setFont( Font font, boolean antialias )
    {
        this.textFont = font;

        if ( this.textRenderer != null ) this.textRenderer.dispose( );

        this.textRenderer = new TextRenderer( this.textFont, antialias, false );

        this.fontSet = true;

        return this;
    }

    public SimpleTextPainter setFont( int size, boolean bold )
    {
        setFont( size, bold, true );
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

    public SimpleTextPainter setPadding( int padding )
    {
        this.padding = padding;
        return this;
    }

    public SimpleTextPainter setColor( float[] rgba )
    {
        textColor = rgba;
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

    public int getPadding( )
    {
        return padding;
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
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }

    @Override
    protected void paintTo( GlimpseContext context, GlimpseBounds bounds )
    {
        if ( text == null ) return;

        GL gl = context.getGL( );
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        Rectangle2D textBounds = sizeText == null ? textRenderer.getBounds( text ) : textRenderer.getBounds( sizeText );

        int xText = padding;
        int yText = padding;

        switch ( hPos )
        {
        case Left:
            xText = ( int ) padding;
            break;
        case Center:
            xText = ( int ) ( width / 2d - textBounds.getWidth( ) / 2d );
            break;
        case Right:
            xText = ( int ) ( width - textBounds.getWidth( ) - padding );
            break;
        }

        switch ( vPos )
        {
        case Bottom:
            yText = ( int ) padding;
            break;
        case Center:
            yText = ( int ) ( height / 2d - textBounds.getHeight( ) / 2d );
            break;
        case Top:
            yText = ( int ) ( height - textBounds.getHeight( ) - padding );
            break;
        }

        if ( this.paintBackground )
        {
            gl.glMatrixMode( GL.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( -0.5, width - 1 + 0.5, -0.5, height - 1 + 0.5, -1, 1 );
            gl.glMatrixMode( GL.GL_MODELVIEW );
            gl.glLoadIdentity( );

            gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
            gl.glEnable( GL.GL_BLEND );

            Rectangle2D bound = sizeText == null ? textRenderer.getBounds( text ) : textRenderer.getBounds( sizeText );

            int xTextMax = ( int ) ( xText + bound.getWidth( ) + ( bound.getMinX( ) ) - 1 );
            int yTextMax = ( int ) ( yText + bound.getHeight( ) - 3 );

            // Draw Text Background
            gl.glColor4fv( backgroundColor, 0 );

            gl.glBegin( GL.GL_QUADS );
            try
            {
                gl.glVertex2f( xText - 0.5f - 2, yText - 0.5f - 2 );
                gl.glVertex2f( xTextMax + 0.5f + 2, yText - 0.5f - 2 );
                gl.glVertex2f( xTextMax + 0.5f + 2, yTextMax + 0.5f + 2 );
                gl.glVertex2f( xText - 0.5f - 2, yTextMax + 0.5f + 2 );
            }
            finally
            {
                gl.glEnd( );
            }
        }

        textRenderer.beginRendering( width, height );
        try
        {
            textRenderer.setColor( textColor[0], textColor[1], textColor[2], textColor[3] );
            textRenderer.draw( text, xText, yText );
        }
        finally
        {
            textRenderer.endRendering( );
        }
    }
}
