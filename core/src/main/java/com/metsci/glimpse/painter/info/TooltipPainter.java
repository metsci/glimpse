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

import java.awt.Font;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.text.BreakIterator;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL3;

import com.google.common.collect.Lists;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisNotSetException;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.support.ImageData;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.SimpleTextLayout;
import com.metsci.glimpse.support.font.SimpleTextLayout.TextBoundingBox;
import com.metsci.glimpse.support.font.SimpleTextLayoutCenter;

/**
 * Displays tool tip text at a specified position.
 *
 * @author ulman
 */
public class TooltipPainter extends SimpleTextPainter
{
    protected static final float[] defaultIconColor = GlimpseColor.getWhite( );

    protected boolean isFixedWidth = false;
    protected int fixedWidth = 50;
    protected int borderSize = 4;
    protected int lineSpacing = 2;
    protected boolean breakOnEol = true;
    protected int offsetX = 14;
    protected int offsetY = -10;
    protected boolean clampToScreenEdges = true;
    protected int iconSpacing = 2;
    protected int textIconSpacing = 4;
    protected Insets insets = new Insets( 0, 2, 4, 0 );

    protected SimpleTextLayout textLayout;
    protected BreakIterator breakIterator;
    protected List<TextBoundingBox> lines;

    protected TextureAtlas atlas;
    protected List<Object> iconIds;
    protected List<ImageData> icons;
    protected List<float[]> iconColors;
    protected float iconSize;

    protected boolean wrapTextAroundIcon = false;
    protected boolean iconSizeFixedToText = true;

    protected double x;
    protected double y;

    protected boolean drawInPixelCoords = true;

    public TooltipPainter( TextureAtlas atlas )
    {
        this.breakIterator = BreakIterator.getCharacterInstance( );
        this.paintBackground = true;
        this.paintBorder = true;
        this.atlas = atlas;
    }

    public TooltipPainter( )
    {
        this( new TextureAtlas( ) );
    }

    /**
     * Sets the icon to be displayed on the first line of the tool tip.
     * @param id the id of the icon in TooltipPainter's TextureAtlas
     */
    public synchronized TooltipPainter setIcon( Object iconId )
    {
        this.iconIds = iconId != null ? Collections.singletonList( iconId ) : null;
        this.iconColors = null;
        this.resetIconLayout( );
        return this;
    }

    /**
     * Sets the TooltipPainter to display multiple icons, one per line,
     * down the left hand side of the tooltip window.
     */
    public synchronized TooltipPainter setIcons( List<Object> iconIds )
    {
        this.iconIds = iconIds != null ? Lists.newArrayList( iconIds ) : null;
        this.iconColors = null;
        this.resetIconLayout( );
        return this;
    }

    /**
     * Sets icons and associated colors.
     *
     * @see #setIcons(List)
     */
    public synchronized TooltipPainter setIcons( List<Object> iconIds, List<float[]> colors )
    {
        this.iconIds = Lists.newArrayList( iconIds );
        this.iconColors = Lists.newArrayList( colors );
        this.resetIconLayout( );
        return this;
    }

    public synchronized TooltipPainter setWrapTextAroundIcon( boolean wrap )
    {
        this.wrapTextAroundIcon = wrap;
        this.resetTextLayout( );
        return this;
    }

    public synchronized TooltipPainter setInsets( Insets insets )
    {
        this.insets = insets;
        return this;
    }

    /**
     * Sets the location of the upper left corner of the tooltip box
     * in screen/pixel coordinates.
     */
    public synchronized TooltipPainter setLocation( int x, int y )
    {
        this.x = x;
        this.y = y;
        this.drawInPixelCoords = true;
        return this;
    }

    /**
     * Sets the location of the upper left corner of the tooltip box
     * in axis coordinates.
     */
    public synchronized TooltipPainter setLocationAxisCoords( double x, double y )
    {
        this.x = x;
        this.y = y;
        this.drawInPixelCoords = false;
        return this;
    }

    public synchronized TooltipPainter setOffset( int x, int y )
    {
        this.offsetX = x;
        this.offsetY = y;
        return this;
    }

    public synchronized TooltipPainter setLocation( GlimpseMouseEvent e )
    {
        return setLocation( e.getScreenPixelsX( ), e.getScreenPixelsY( ) );
    }

    public synchronized TooltipPainter setBorderSize( int size )
    {
        this.borderSize = size;
        this.resetTextLayout( );
        return this;
    }

    public synchronized TooltipPainter setFixedWidth( int fixedWidth )
    {
        this.fixedWidth = fixedWidth;
        this.isFixedWidth = true;
        this.resetTextLayout( );
        return this;
    }

    public synchronized TooltipPainter setUnlimitedWidth( )
    {
        this.isFixedWidth = false;
        this.resetTextLayout( );
        return this;
    }

    public synchronized TooltipPainter setBreakOnEol( boolean breakOnEol )
    {
        this.breakOnEol = breakOnEol;
        this.resetTextLayout( );
        return this;
    }

    public synchronized TooltipPainter setLineSpacing( int lineSpacing )
    {
        this.lineSpacing = lineSpacing;
        this.resetTextLayout( );
        return this;
    }

    public synchronized TooltipPainter setBreakIterator( BreakIterator breakIterator )
    {
        this.breakIterator = breakIterator;
        this.resetTextLayout( );
        return this;
    }

    @Override
    public synchronized TooltipPainter setText( String text )
    {
        this.text = text;
        this.resetTextLayout( );
        return this;
    }

    public synchronized TooltipPainter setTextIconSpacing( int textIconSpacing )
    {
        this.textIconSpacing = textIconSpacing;
        return this;
    }

    public synchronized TooltipPainter setIconSize( float size )
    {
        this.iconSize = size;
        this.iconSizeFixedToText = false;
        return this;
    }

    /**
     * If true, the height of each icon will be set to the height of each line of text.
     */
    public synchronized TooltipPainter setIconSizeFixedToTextHeight( boolean iconSizeFixedToText )
    {
        this.iconSizeFixedToText = iconSizeFixedToText;
        return this;
    }

    public synchronized TooltipPainter setIconSpacing( int i )
    {
        iconSpacing = i;
        return this;
    }

    public synchronized TooltipPainter clear( )
    {
        this.setText( null );
        this.setIcon( null );
        return this;
    }

    public synchronized Insets getInsets( )
    {
        return this.insets;
    }

    public synchronized int getBorderSize( )
    {
        return this.borderSize;
    }

    public synchronized void setClampToScreenEdges( boolean clamp )
    {
        this.clampToScreenEdges = clamp;
    }

    public synchronized int getFixedWidth( )
    {
        return this.fixedWidth;
    }

    public synchronized boolean isFixedWidth( )
    {
        return this.isFixedWidth;
    }

    public synchronized boolean isIconSizeFixedToTextHeight( )
    {
        return this.iconSizeFixedToText;
    }

    public synchronized int getTextIconSpacing( )
    {
        return this.textIconSpacing;
    }

    /**
     * Whether to force a break on the end of line characters (\r \f \n).
     */
    public synchronized boolean getBreakOnEol( )
    {
        return breakOnEol;
    }

    /**
     * The spacing between the bottom (descent) of one line of text to the top
     * (ascent) of the next line.
     */
    public synchronized int getLineSpacing( )
    {
        return lineSpacing;
    }

    public synchronized float getIconSize( )
    {
        return iconSize;
    }

    public synchronized float getIconSpacing( )
    {
        return iconSpacing;
    }

    protected void resetTextLayout( )
    {
        this.textLayout = null;
        this.lines = null;
    }

    protected void resetIconLayout( )
    {
        this.icons = null;
    }

    @Override
    protected synchronized void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = getAxis2D( context );
        GL3 gl = context.getGL( ).getGL3( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( axis == null && !drawInPixelCoords )
        {
            throw new AxisNotSetException( this, context );
        }

        double x = drawInPixelCoords ? this.x : axis.getAxisX( ).valueToScreenPixelUnits( this.x );
        double y = drawInPixelCoords ? bounds.getHeight( ) - this.y : axis.getAxisY( ).valueToScreenPixelUnits( this.y );

        if ( icons == null )
        {
            loadIcons( context );
        }

        if ( newFont != null )
        {
            updateTextRenderer( );
        }

        if ( text != null && textLayout == null && textRenderer != null )
        {
            updateTextLayout( );
        }

        if ( iconIds == null && lines == null ) return;

        GLUtils.enableStandardBlending( gl );
        try
        {
            double textHeight = 0;
            if ( lines != null )
            {
                textHeight = ( textLayout.getLineHeight( ) ) * lines.size( ) + lineSpacing * ( lines.size( ) - 1 );
            }

            float iconSize;
            if ( iconIds == null || iconIds.isEmpty( ) )
            {
                iconSize = 0;
            }
            else if ( iconSizeFixedToText && textHeight != 0 )
            {
                iconSize = ( float ) textLayout.getLineHeight( );
            }
            else
            {
                iconSize = this.iconSize;
            }

            // calculate largest height of box
            double iconHeight = 0;
            if ( icons != null )
            {
                iconHeight = iconSize * icons.size( ) + iconSpacing * ( icons.size( ) - 1 );
            }

            int boundingHeight = ( int ) ( borderSize * 2 + Math.max( iconHeight, textHeight ) );

            // calculate largest width of box
            float textLength = 0;
            if ( lines != null )
            {
                for ( int k = 0; k < lines.size( ); k++ )
                {
                    textLength = Math.max( textLength, lines.get( k ).width );
                }
            }

            int boundingWidth = ( int ) ( borderSize * 2 + iconSize + textLength );
            if ( iconIds != null && !iconIds.isEmpty( ) && lines != null )
            {
                boundingWidth += textIconSpacing;
            }

            // fold the insets and the offset together
            int offsetX = this.offsetX + this.insets.left;
            int offsetY = this.offsetY - this.insets.top;

            // adjust bounds to clamp the text box to the edge of the screen
            double clampX = offsetX, clampY = offsetY;
            if ( clampToScreenEdges )
            {
                if ( x + boundingWidth + offsetX > bounds.getWidth( ) ) clampX = bounds.getWidth( ) - boundingWidth - x;
                if ( x + offsetX < 0 ) clampX = -x;
                if ( y + offsetY > bounds.getHeight( ) ) clampY = bounds.getHeight( ) - y;
                if ( boundingHeight > y + offsetY ) clampY = boundingHeight - y;
            }

            // paint background and border
            if ( this.paintBackground || this.paintBorder )
            {
                float xMin = ( float ) ( x + clampX );
                float yMin = ( float ) ( y + clampY );
                float xMax = xMin + boundingWidth;
                float yMax = yMin - boundingHeight;

                if ( paintBackground )
                {
                    this.fillBuffer.clear( );
                    this.fillBuffer.growQuad2f( xMin, yMin, xMax, yMax );

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
                    this.linePath.addRectangle( xMin, yMin, xMax, yMax );

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

            // draw text
            if ( lines != null && textRenderer != null )
            {
                GlimpseColor.setColor( textRenderer, textColor );
                textRenderer.beginRendering( width, height );
                try
                {
                    double posX = x + iconSize + borderSize;

                    if ( iconIds != null && !iconIds.isEmpty( ) )
                    {
                        posX += textIconSpacing;
                    }

                    double posY = y - borderSize - textLayout.getLineHeight( );
                    double iconPosY = Float.NEGATIVE_INFINITY;

                    if ( wrapTextAroundIcon && icons != null )
                    {
                        iconPosY = y - ( iconSize * icons.size( ) + iconSpacing * ( icons.size( ) - 1 ) + borderSize );
                    }

                    for ( int i = 0; i < lines.size( ); i++ )
                    {
                        if ( posY + textLayout.getLineHeight( ) < iconPosY ) posX = x + borderSize;
                        textRenderer.draw( lines.get( i ).text, ( int ) ( posX + clampX ), ( int ) ( posY + clampY ) );
                        posY = posY - lineSpacing - ( textLayout.getLineHeight( ) );
                    }
                }
                finally
                {
                    textRenderer.endRendering( );
                }
            }

            // draw icons
            if ( iconIds != null && !iconIds.isEmpty( ) )
            {
                GLUtils.enableStandardBlending( gl );
                atlas.beginRenderingPixelOrtho( context, bounds );
                try
                {
                    double posY = y - borderSize - iconSize;
                    for ( int i = 0; i < iconIds.size( ); i++ )
                    {
                        Object iconId = iconIds.get( i );
                        ImageData iconData = icons.get( i );

                        if ( iconId != null && iconData != null )
                        {
                            double iconScale = iconSize / ( double ) iconData.getWidth( );

                            float[] color = defaultIconColor;

                            if ( iconColors != null && i < iconColors.size( ) )
                            {
                                float[] iconColor = iconColors.get( i );
                                if ( iconColor != null ) color = iconColor;
                            }

                            int scaleX = ( int ) ( x + borderSize + clampX );
                            int scaleY = ( int ) ( posY + clampY );

                            atlas.drawImage( context, iconId, scaleX, scaleY, iconScale, iconScale, 0, iconData.getHeight( ), color );
                            posY = posY - iconSize - iconSpacing;
                        }
                    }
                }
                finally
                {
                    atlas.endRendering( context );
                    GLUtils.disableBlending( gl );
                }
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }
    }

    protected void updateTextLayout( )
    {
        Font font = textRenderer.getFont( );
        FontRenderContext frc = textRenderer.getFontRenderContext( );

        textLayout = new SimpleTextLayoutCenter( font, frc, breakIterator );
        textLayout.setBreakOnEol( breakOnEol );
        textLayout.setLineSpacing( lineSpacing );

        if ( iconSize == 0 ) iconSize = ( float ) textLayout.getLineHeight( );

        textLayout.doLayout( text, 0, 0, isFixedWidth ? fixedWidth : Float.MAX_VALUE );
        lines = textLayout.getLines( );
    }

    protected void loadIcons( GlimpseContext context )
    {
        // looks strange, but causes atlas to load pending icons
        // this is necessary to do here because calls to atlas.getImageData( )
        // will fail if we do not
        if ( iconIds != null )
        {
            atlas.beginRenderingPixelOrtho( context );
            atlas.endRendering( context );
        }

        int size = iconIds == null ? 0 : iconIds.size( );

        this.icons = Lists.newArrayListWithCapacity( size );

        for ( int i = 0; i < size; i++ )
        {
            Object iconId = this.iconIds.get( i );
            ImageData icon = iconId != null ? atlas.getImageData( iconId ) : null;
            this.icons.add( icon );
        }
    }
}
