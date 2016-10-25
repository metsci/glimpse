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
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.line.StreamingLinePath;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

/**
 * A simple implementation of {@code AbstractTreeMapPainter} that has default
 * colors for everything.
 *
 * @author borkholder
 */
public class SimpleTreeMapPainter extends AbstractTreeMapPainter
{
    protected int minPixelsBeforeHide = 3;
    protected int minPixelsBeforeHideText = 15;

    protected float[] borderColor = new float[] { 0.4f, 0.4f, 0.4f, 1f };
    protected float[] selectedTitleBackgroundColor = new float[] { 1f, 0.2f, 0.2f, 1f };
    protected float[] leafColor = new float[] { 0.7f, 0.7f, 1.0f, 1f };
    protected float[] selectedLeafColor = new float[] { 0.2f, 0.2f, 0.2f, 0.3f };
    protected float[] titleBorderColor = new float[] { 1, 1, 1, 1 };

    protected TextRenderer titleRenderer;
    protected TextRenderer textRenderer;

    protected float[] titleColor = GlimpseColor.getWhite( );
    protected float[] textColor = GlimpseColor.getBlack( );

    protected Font titleFont = FontUtils.getDefaultBold( 14.0f );
    protected Font textFont = FontUtils.getDefaultItalic( 12.0f );

    protected FlatColorProgram flatProg;
    protected GLEditableBuffer flatPath;

    protected LineStyle borderStyle;
    protected LineProgram lineProg;
    protected StreamingLinePath linePath;

    public SimpleTreeMapPainter( )
    {
        lineProg = new LineProgram( );
        borderStyle = new LineStyle( );
        borderStyle.feather_PX = 0;
        borderStyle.joinType = LineJoinType.JOIN_MITER;
        borderStyle.stippleEnable = false;
        borderStyle.thickness_PX = 1;
        linePath = new StreamingLinePath( 10_000 );

        flatPath = new GLEditableBuffer( GL.GL_STATIC_DRAW, 1024 );
        flatProg = new FlatColorProgram( );
    }

    public float[] getBorderColor( )
    {
        return borderColor;
    }

    public void setBorderColor( float[] borderColor )
    {
        this.borderColor = borderColor;
    }

    public float[] getTitleColor( )
    {
        return titleColor;
    }

    public void setTitleColor( float[] titleColor )
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

    public float[] getTextColor( )
    {
        return textColor;
    }

    public void setTextColor( float[] textColor )
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
    protected void doPaintTo( GlimpseContext context )
    {
        GL3 gl = getGL3( context );
        GlimpseBounds layoutBounds = getBounds( context );
        Axis2D axis = getAxis2D( context );

        lineProg.begin( gl );
        try
        {
            lineProg.setAxisOrtho( gl, axis );
            lineProg.setViewport( gl, layoutBounds );
        }
        finally
        {
            lineProg.end( gl );
        }

        flatProg.begin( gl );
        try
        {
            /*
             * When we don't draw really tiny boxes, this gives the illusion
             * that something is being drawn when we zoom.
             */
            flatProg.setAxisOrtho( gl, axis );
            flatProg.setColor( gl, borderColor );
            flatPath.clear( );
            flatPath.growQuad2f( ( float ) axis.getMinX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxX( ), ( float ) axis.getMaxY( ) );
            flatProg.draw( gl, GL.GL_TRIANGLE_STRIP, flatPath, 0, 6 );
        }
        finally
        {
            flatProg.end( gl );
        }

        super.doPaintTo( context );
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        lineProg.dispose( getGL3( context ) );
        flatProg.dispose( getGL3( context ) );
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
    protected void displayNode( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int nodeId )
    {
        if ( axis.getAxisX( ).getPixelsPerValue( ) * nodeBounds.getWidth( ) < minPixelsBeforeHide &&
                axis.getAxisY( ).getPixelsPerValue( ) * nodeBounds.getHeight( ) < minPixelsBeforeHide )
        {
            return;
        }
        else
        {
            super.displayNode( gl, axis, layoutBounds, nodeBounds, nodeId );
        }
    }

    @Override
    protected void drawBorder( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int nodeId )
    {
        linePath.map( gl, 7 );
        linePath.moveTo( ( float ) nodeBounds.getMinX( ), ( float ) nodeBounds.getMinY( ) );
        linePath.lineTo( ( float ) nodeBounds.getMinX( ), ( float ) nodeBounds.getMaxY( ) );
        linePath.lineTo( ( float ) nodeBounds.getMaxX( ), ( float ) nodeBounds.getMaxY( ) );
        linePath.lineTo( ( float ) nodeBounds.getMaxX( ), ( float ) nodeBounds.getMinY( ) );
        linePath.closeLoop( );
        linePath.seal( gl );

        float[] color = getBorderColor( nodeId, isSelected( axis, nodeBounds ) );
        borderStyle.rgba = color;
        borderStyle.thickness_PX = 1f;

        lineProg.begin( gl );
        try
        {
            lineProg.setStyle( gl, borderStyle );
            lineProg.draw( gl, linePath );
        }
        finally
        {
            lineProg.end( gl );
        }
    }

    @Override
    protected void drawLeafBackground( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int leafId )
    {
        flatProg.begin( gl );
        try
        {
            boolean isLeafSelected = isSelected( axis, nodeBounds );
            float[] color = getLeafColor( leafId, isLeafSelected );
            flatProg.setColor( gl, color );

            flatPath.clear( );
            flatPath.growQuad2f( ( float ) nodeBounds.getMinX( ), ( float ) nodeBounds.getMinY( ), ( float ) nodeBounds.getMaxX( ), ( float ) nodeBounds.getMaxY( ) );
            flatProg.draw( gl, GL.GL_TRIANGLE_STRIP, flatPath, 0, 6 );
        }
        finally
        {
            flatProg.end( gl );
        }
    }

    @Override
    protected Rectangle2D drawTitle( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D boundary, int nodeId )
    {
        String title = tree.getTitle( nodeId );
        if ( title == null || title.isEmpty( ) || axis.getAxisY( ).getPixelsPerValue( ) * boundary.getHeight( ) < minPixelsBeforeHideText )
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

        flatProg.begin( gl );
        try
        {
            float[] color = getTitleBackgroundColor( nodeId, selected );
            flatProg.setColor( gl, color );

            flatPath.clear( );
            flatPath.growQuad2f( ( float ) boundary.getMinX( ), ( float ) ( boundary.getMaxY( ) - borderHeight ), ( float ) boundary.getMaxX( ), ( float ) boundary.getMaxY( ) );
            flatProg.draw( gl, GL.GL_TRIANGLE_STRIP, flatPath, 0, 6 );
        }
        finally
        {
            flatProg.end( gl );
        }

        // draw title border
        lineProg.begin( gl );
        try
        {
            lineProg.setAxisOrtho( gl, axis );
            lineProg.setViewport( gl, layoutBounds );

            float[] color = getTitleBorderColor( nodeId, selected );
            borderStyle.rgba = color;
            borderStyle.thickness_PX = 0.5f;

            linePath.map( gl, 7 );
            linePath.moveTo( ( float ) boundary.getMinX( ), ( float ) boundary.getMaxY( ) );
            linePath.lineTo( ( float ) boundary.getMinX( ), ( float ) ( boundary.getMaxY( ) - borderHeight ) );
            linePath.lineTo( ( float ) boundary.getMaxX( ), ( float ) ( boundary.getMaxY( ) - borderHeight ) );
            linePath.lineTo( ( float ) boundary.getMaxX( ), ( float ) boundary.getMaxY( ) );
            linePath.closeLoop( );
            linePath.seal( gl );
            lineProg.draw( gl, borderStyle, linePath );
        }
        finally
        {
            lineProg.end( gl );
        }

        int textPosX = axis.getAxisX( ).valueToScreenPixel( boundary.getMinX( ) );
        int textPosY = axis.getAxisY( ).valueToScreenPixel( boundary.getMaxY( ) ) - ( int ) borderHeightPx;

        // draw title text
        titleRenderer.beginRendering( layoutBounds.getWidth( ), layoutBounds.getHeight( ) );
        try
        {
            GlimpseColor.setColor( titleRenderer, titleColor );
            titleRenderer.draw( title, textPosX + textBorderPx, textPosY + textBorderPx );
        }
        finally
        {
            titleRenderer.endRendering( );
        }

        Rectangle2D newBoundary = new Rectangle2D.Double( boundary.getMinX( ), boundary.getMinY( ), boundary.getWidth( ), boundary.getHeight( ) - borderHeight );
        return newBoundary;
    }

    @Override
    protected void drawLeafInterior( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int leafId )
    {
        String text = tree.getText( leafId );
        if ( text == null || text.isEmpty( ) || axis.getAxisY( ).getPixelsPerValue( ) * nodeBounds.getHeight( ) < minPixelsBeforeHideText )
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

        try
        {
            textRenderer.beginRendering( layoutBounds.getWidth( ), layoutBounds.getHeight( ) );
            GlimpseColor.setColor( textRenderer, textColor );
            textRenderer.draw( text, textPosX, textPosY );
        }
        finally
        {
            textRenderer.endRendering( );
        }
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
