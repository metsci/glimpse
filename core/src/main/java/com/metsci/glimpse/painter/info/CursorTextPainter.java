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

import static java.lang.Math.*;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

/**
 * Displays a text box which follows the cursor and displays the
 * position of the cursor in data coordinates.
 *
 * @author ulman
 */
public class CursorTextPainter extends GlimpsePainterBase
{
    protected TextRenderer textRenderer;
    protected int textSpacerX = 1;
    protected int textSpacerY = 2;
    protected int verticalBarSpacer = 6;
    protected int horizontalBarSpacer = 6;

    protected boolean zTextGap = true;
    protected boolean offsetBySelectionSize = true;
    protected boolean clampToScreenEdges = true;

    protected float[] fontColor = new float[] { 0.85f, 0.85f, 0.85f, 1.0f };
    protected float[] textBackgroundColor = new float[] { 0.2f, 0.2f, 0.2f, 0.7f };

    protected FlatColorProgram prog;
    protected GLEditableBuffer buffer;

    public CursorTextPainter( Font font )
    {
        this.prog = new FlatColorProgram( );
        this.textRenderer = new TextRenderer( font );
        this.buffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
    }

    public CursorTextPainter( )
    {
        this( FontUtils.getDefaultPlain( 12.0f ) );
    }

    public void setTextColor( float[] color )
    {
        this.fontColor = color;
    }

    public void setTextBackgroundColor( float[] color )
    {
        this.textBackgroundColor = color;
    }

    public void setOffsetBySelectionSize( boolean offset )
    {
        this.offsetBySelectionSize = offset;
    }

    public void setClampToScreenEdges( boolean clamp )
    {
        this.clampToScreenEdges = clamp;
    }

    public void setTextGapZ( boolean gap )
    {
        this.zTextGap = gap;
    }

    /**
     * Gets the x position that will be annotated. Can be either the selection
     * center, the mouse or something else entirely.
     */
    protected float getPositionX( Axis2D axis )
    {
        return ( float ) axis.getAxisX( ).getSelectionCenter( );
    }

    /**
     * Gets the y position that will be annotated. Can be either the selection
     * center, the mouse or something else entirely.
     */
    protected float getPositionY( Axis2D axis )
    {
        return ( float ) axis.getAxisY( ).getSelectionCenter( );
    }

    /**
     * Gets the bounds for the text background.  Just adds a little bit of
     * padding and aligns the rectangles.
     */
    protected Rectangle2D[] getBackgroundBounds( Rectangle2D xTextBounds, Rectangle2D yTextBounds, Rectangle2D zTextBounds )
    {
        float maxWidth = ( float ) max( max( xTextBounds.getWidth( ), yTextBounds.getWidth( ) ), zTextBounds == null ? 0 : zTextBounds.getWidth( ) );
        return new Rectangle2D[] { new Rectangle2D.Float( 0, 0, maxWidth + textSpacerX * 2, ( float ) xTextBounds.getHeight( ) + textSpacerY * 2 ), new Rectangle2D.Float( 0, 0, maxWidth + textSpacerX * 2, ( float ) yTextBounds.getHeight( ) + textSpacerY * 2 ), zTextBounds == null ? null : new Rectangle2D.Float( 0, 0, maxWidth + textSpacerX * 2, ( float ) zTextBounds.getHeight( ) + textSpacerY * 2 ) };
    }

    /**
     * Gets the lower-left corner of the text.  This helps to clamp the
     * annotations to the edge of the canvas.
     */
    protected float[] getCorners( Axis2D axis, Rectangle2D boundsX, Rectangle2D boundsY, Rectangle2D boundsZ )
    {
        float[] corners = new float[6];

        int widthPixels = axis.getAxisX( ).valueToScreenPixel( axis.getMaxX( ) );
        int heightPixels = axis.getAxisY( ).valueToScreenPixel( axis.getMaxY( ) );

        float centerX = getPositionX( axis );
        float centerY = getPositionY( axis );

        int centerPixelsX = axis.getAxisX( ).valueToScreenPixel( centerX );
        int centerPixelsY = axis.getAxisY( ).valueToScreenPixel( centerY );

        int selectionSizePixelsX = ( int ) ( axis.getAxisX( ).getSelectionSize( ) / 2.0f * Math.abs( axis.getAxisX( ).getPixelsPerValue( ) ) );

        double x = ( centerPixelsX + horizontalBarSpacer + ( offsetBySelectionSize ? selectionSizePixelsX : 0 ) );
        double y = centerPixelsY + verticalBarSpacer + boundsY.getHeight( ) + ( boundsZ == null || zTextGap ? 0 : boundsZ.getHeight( ) );

        if ( clampToScreenEdges )
        {
            x = min( x, widthPixels - boundsX.getWidth( ) );
            x = max( 0, x );
            y = min( y, heightPixels - boundsX.getHeight( ) );
            if ( boundsZ != null )
            {
                y = max( y, boundsZ.getHeight( ) + boundsY.getHeight( ) );
            }
        }

        // set the corners for X text
        corners[0] = ( float ) x;
        corners[1] = ( float ) y;

        y -= boundsY.getHeight( );
        // set the corners for Y text
        corners[2] = ( float ) x;
        corners[3] = ( float ) y;

        if ( boundsZ != null )
        {
            if ( zTextGap )
            {
                y = centerPixelsY - verticalBarSpacer - boundsZ.getHeight( );
                if ( clampToScreenEdges )
                {
                    y = min( y, heightPixels - boundsX.getHeight( ) - boundsY.getHeight( ) - boundsZ.getHeight( ) );
                    y = max( y, 0 );
                }

                // set the corners for Z text
                corners[4] = ( float ) x;
                corners[5] = ( float ) y;
            }
            else
            {
                y -= boundsZ.getHeight( );
                // set the corners for Z text
                corners[4] = ( float ) x;
                corners[5] = ( float ) y;
            }
        }

        return corners;
    }

    protected String getTextX( Axis2D axis )
    {
        return String.format( "x: %.2f", getPositionX( axis ) );
    }

    protected String getTextY( Axis2D axis )
    {
        return String.format( "y: %.2f", getPositionY( axis ) );
    }

    protected String getTextZ( Axis2D axis )
    {
        return null;
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        this.textRenderer.dispose( );

        this.prog.dispose( context.getGL( ).getGL3( ) );
        this.buffer.dispose( context.getGL( ) );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        GL3 gl = context.getGL( ).getGL3( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        String xText = getTextX( axis );
        String yText = getTextY( axis );
        String zText = getTextZ( axis );

        Rectangle2D xTextBounds = textRenderer.getBounds( xText );
        Rectangle2D yTextBounds = textRenderer.getBounds( yText );
        Rectangle2D zTextBounds = zText != null ? textRenderer.getBounds( zText ) : null;

        Rectangle2D[] backBounds = getBackgroundBounds( xTextBounds, yTextBounds, zTextBounds );
        Rectangle2D xBackBounds = backBounds[0];
        Rectangle2D yBackBounds = backBounds[1];
        Rectangle2D zBackBounds = backBounds[2];

        float[] corners = getCorners( axis, xBackBounds, yBackBounds, zBackBounds );

        GLUtils.enableStandardBlending( gl );
        try
        {
            this.buffer.clear( );
            this.buffer.growQuad2f( corners[0], corners[1], corners[0] + ( float ) xBackBounds.getWidth( ), corners[1] + ( float ) xBackBounds.getHeight( ) );
            this.buffer.growQuad2f( corners[2], corners[3], corners[2] + ( float ) yBackBounds.getWidth( ), corners[3] + ( float ) yBackBounds.getHeight( ) );

            if ( zText != null )
            {
                this.buffer.growQuad2f( corners[4], corners[5], corners[4] + ( float ) zBackBounds.getWidth( ), corners[5] + ( float ) zBackBounds.getHeight( ) );
            }

            this.prog.begin( gl );
            try
            {
                this.prog.setPixelOrtho( gl, bounds );

                this.prog.draw( gl, this.buffer, this.textBackgroundColor );
            }
            finally
            {
                this.prog.end( gl );
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }

        textRenderer.beginRendering( width, height );
        try
        {
            GlimpseColor.setColor( textRenderer, fontColor );
            textRenderer.draw( xText, ( int ) corners[0] + textSpacerX, ( int ) ( corners[1] + textSpacerY ) );
            textRenderer.draw( yText, ( int ) corners[2] + textSpacerX, ( int ) ( corners[3] + textSpacerY * 2 ) );
            if ( zText != null )
            {
                textRenderer.draw( zText, ( int ) corners[4] + textSpacerX, ( int ) ( corners[5] + textSpacerY ) );
            }
        }
        finally
        {
            textRenderer.endRendering( );
        }
    }
}
