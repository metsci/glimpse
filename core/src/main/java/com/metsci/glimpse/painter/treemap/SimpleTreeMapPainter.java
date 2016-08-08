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
package com.metsci.glimpse.painter.treemap;

import static java.lang.Math.max;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.support.font.FontUtils;

/**
 * A simple implementation of {@code AbstractTreeMapPainter} that has default
 * colors for everything.
 *
 * @author borkholder
 */
public class SimpleTreeMapPainter extends AbstractTreeMapPainter
{
    protected float[] borderColor = new float[] { 0.4f, 0.4f, 0.4f, 1f };
    protected float[] selectedTitleBackgroundColor = new float[] { 1f, 0.2f, 0.2f, 1f };
    protected float[] leafColor = new float[] { 0.7f, 0.7f, 1.0f, 1f };
    protected float[] selectedLeafColor = new float[] { 0.2f, 0.2f, 0.2f, 0.3f };
    protected float[] titleBorderColor = new float[] { 1, 1, 1, 1 };

    protected TextRenderer titleRenderer;
    protected TextRenderer textRenderer;

    protected Color titleColor = Color.white;
    protected Color textColor = Color.darkGray;

    protected Font titleFont = FontUtils.getDefaultBold( 14.0f );
    protected Font textFont = FontUtils.getDefaultItalic( 12.0f );

    public float[] getBorderColor( )
    {
        return borderColor;
    }

    public void setBorderColor( float[] borderColor )
    {
        this.borderColor = borderColor;
    }

    public Color getTitleColor( )
    {
        return titleColor;
    }

    public void setTitleColor( Color titleColor )
    {
        this.titleColor = titleColor;
    }

    public float[] getTitleBorderColor( )
    {
        return titleBorderColor;
    }

    public void setTitleBorderColor( float[] titleBorderColor )
    {
        this.titleBorderColor = titleBorderColor;
    }

    public float[] getSelectedTitleBackgroundColor( )
    {
        return selectedTitleBackgroundColor;
    }

    public void setSelectedTitleBackgroundColor( float[] selectedTitleBackgroundColor )
    {
        this.selectedTitleBackgroundColor = selectedTitleBackgroundColor;
    }

    public float[] getLeafColor( )
    {
        return leafColor;
    }

    public void setLeafColor( float[] leafColor )
    {
        this.leafColor = leafColor;
    }

    public float[] getSelectedLeafColor( )
    {
        return selectedLeafColor;
    }

    public void setSelectedLeafColor( float[] selectedLeafColor )
    {
        this.selectedLeafColor = selectedLeafColor;
    }

    public Color getTextColor( )
    {
        return textColor;
    }

    public void setTextColor( Color textColor )
    {
        this.textColor = textColor;
    }

    public Font getTitleFont( )
    {
        return titleFont;
    }

    public void setTitleFont( Font titleFont )
    {
        this.titleFont = titleFont;
    }

    public Font getTextFont( )
    {
        return textFont;
    }

    public void setTextFont( Font textFont )
    {
        this.textFont = textFont;
    }

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );

        if ( titleRenderer != null )
        {
            titleRenderer.dispose( );
            titleRenderer = null;
        }
        if ( textRenderer != null )
        {
            textRenderer.dispose( );
            textRenderer = null;
        }
    }

    @Override
    protected void drawBorder( GL2 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int nodeId )
    {
        float[] color = getBorderColor( nodeId, isSelected( axis, nodeBounds ) );
        gl.glColor4f( color[0], color[1], color[2], color[3] );

        gl.glLineWidth( 1 );

        gl.glBegin( GL2.GL_LINE_LOOP );
        gl.glVertex2d( nodeBounds.getMinX( ), nodeBounds.getMinY( ) );
        gl.glVertex2d( nodeBounds.getMinX( ), nodeBounds.getMaxY( ) );
        gl.glVertex2d( nodeBounds.getMaxX( ), nodeBounds.getMaxY( ) );
        gl.glVertex2d( nodeBounds.getMaxX( ), nodeBounds.getMinY( ) );
        gl.glEnd( );
    }

    @Override
    protected void drawLeafBackground( GL2 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int leafId )
    {
        float[] color = getLeafColor( leafId, false );
        gl.glColor4f( color[0], color[1], color[2], color[3] );
        gl.glRectd( nodeBounds.getMinX( ), nodeBounds.getMinY( ), nodeBounds.getMaxX( ), nodeBounds.getMaxY( ) );

        if ( isSelected( axis, nodeBounds ) )
        {
            color = getLeafColor( leafId, true );
            gl.glColor4f( color[0], color[1], color[2], color[3] );
            gl.glRectd( nodeBounds.getMinX( ), nodeBounds.getMinY( ), nodeBounds.getMaxX( ), nodeBounds.getMaxY( ) );
        }
    }

    @Override
    protected Rectangle2D drawTitle( GL2 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D boundary, int nodeId )
    {
        String title = tree.getTitle( nodeId );
        if ( title == null )
        {
            return boundary;
        }

        if ( titleRenderer == null )
        {
            titleRenderer = new TextRenderer( titleFont );
        }

        boolean selected = isSelected( axis, boundary );

        Rectangle2D rect = titleRenderer.getBounds( title );

        // draw title background
        int textBorderPx = 3;
        double borderHeightPx = rect.getHeight( ) + textBorderPx * 2;
        double borderHeight = borderHeightPx / axis.getAxisY( ).getPixelsPerValue( );

        // don't draw if the area is too small to draw the title
        if ( boundary.getHeight( ) < borderHeight || boundary.getWidth( ) < rect.getWidth( ) / axis.getAxisX( ).getPixelsPerValue( ) )
        {
            return boundary;
        }

        float[] color = getTitleBackgroundColor( nodeId, selected );
        gl.glColor4f( color[0], color[1], color[2], color[3] );
        gl.glRectd( boundary.getMinX( ), boundary.getMaxY( ) - borderHeight, boundary.getMaxX( ), boundary.getMaxY( ) );

        // draw title border
        color = getTitleBorderColor( nodeId, selected );
        gl.glColor4f( color[0], color[1], color[2], color[3] );
        gl.glLineWidth( 0.5f );
        gl.glBegin( GL2.GL_LINE_LOOP );
        gl.glVertex2d( boundary.getMinX( ), boundary.getMaxY( ) - borderHeight );
        gl.glVertex2d( boundary.getMinX( ), boundary.getMaxY( ) );
        gl.glVertex2d( boundary.getMaxX( ), boundary.getMaxY( ) );
        gl.glVertex2d( boundary.getMaxX( ), boundary.getMaxY( ) - borderHeight );
        gl.glEnd( );

        int textPosX = axis.getAxisX( ).valueToScreenPixel( boundary.getMinX( ) );
        int textPosY = axis.getAxisY( ).valueToScreenPixel( boundary.getMaxY( ) ) - ( int ) borderHeightPx;

        // draw title text
        titleRenderer.setColor( titleColor );
        titleRenderer.beginRendering( layoutBounds.getWidth( ), layoutBounds.getHeight( ) );
        titleRenderer.draw( title, textPosX + textBorderPx, textPosY + textBorderPx );
        titleRenderer.endRendering( );

        Rectangle2D newBoundary = new Rectangle2D.Double( boundary.getMinX( ), boundary.getMinY( ), boundary.getWidth( ), boundary.getHeight( ) - borderHeight );
        return newBoundary;
    }

    @Override
    protected void drawLeafInterior( GL gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int leafId )
    {
        String text = tree.getText( leafId );
        if ( text == null )
        {
            return;
        }

        if ( textRenderer == null )
        {
            textRenderer = new TextRenderer( textFont );
        }

        Rectangle2D rect = textRenderer.getBounds( text );

        int paddingTopPx = 10;
        int textPosX = axis.getAxisX( ).valueToScreenPixel( nodeBounds.getCenterX( ) ) - ( int ) ( rect.getWidth( ) / 2 );
        textPosX = max( textPosX, axis.getAxisX( ).valueToScreenPixel( nodeBounds.getMinX( ) ) );
        int textPosY = axis.getAxisY( ).valueToScreenPixel( nodeBounds.getMaxY( ) ) - ( int ) rect.getHeight( ) - paddingTopPx;
        textPosY = max( textPosY, axis.getAxisY( ).valueToScreenPixel( nodeBounds.getMinY( ) ) );

        // don't draw if the area is too small
        if ( nodeBounds.getWidth( ) < rect.getWidth( ) / axis.getAxisX( ).getPixelsPerValue( ) || nodeBounds.getHeight( ) - paddingTopPx < rect.getHeight( ) / axis.getAxisY( ).getPixelsPerValue( ) )
        {
            return;
        }

        textRenderer.setColor( textColor );
        textRenderer.beginRendering( layoutBounds.getWidth( ), layoutBounds.getHeight( ) );
        textRenderer.draw( text, textPosX, textPosY );
        textRenderer.endRendering( );
    }

    /**
     * Gets the color for the leaf background. This is a little different from
     * other selections. First the normal background color is drawn, and then if
     * the leaf is selected, the selected color is drawn on top of it. This allows
     * a translucent color to cover the original color.
     */
    protected float[] getLeafColor( int nodeId, boolean selected )
    {
        if ( selected )
        {
            return selectedLeafColor;
        }
        else
        {
            return leafColor;
        }
    }

    /**
     * Gets the color of the border, with a flag for whether or not the node
     * contains the selection center.
     */
    protected float[] getBorderColor( int nodeId, boolean selected )
    {
        return borderColor;
    }

    /**
     * Gets the color of the title background, with a flag for whether or not the
     * node contains the selection center.
     */
    protected float[] getTitleBackgroundColor( int nodeId, boolean selected )
    {
        if ( selected )
        {
            return selectedTitleBackgroundColor;
        }
        else
        {
            return getBorderColor( nodeId, false );
        }
    }

    /**
     * Gets the color of the title border, with a flag for whether or not the node
     * contains the selection center.
     */
    protected float[] getTitleBorderColor( int nodeId, boolean selected )
    {
        return titleBorderColor;
    }
}
