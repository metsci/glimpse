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

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.text.BreakIterator;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL2;

import com.google.common.collect.Lists;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisNotSetException;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
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

    // if true, tooltip text will be wrapped if it extends past the edge of the box
    protected boolean isFixedWidth = false;
    protected int fixedWidth = 50;
    protected int borderSize = 4;
    protected float lineSpacing = 2;
    protected boolean breakOnEol = true;
    protected int offsetX = 14;
    protected int offsetY = -10;
    protected boolean clampToScreenEdges = true;

    protected SimpleTextLayout textLayout;
    protected BreakIterator breakIterator;
    protected List<TextBoundingBox> lines;
    protected Bounds linesBounds;

    protected TextureAtlas atlas;
    protected List<Object> iconIds;
    protected List<ImageData> icons;
    protected List<float[]> iconColors;
    protected boolean noIcons = false;
    protected float iconSize;

    protected boolean wrapTextAroundIcon = false;

    protected double x;
    protected double y;

    protected boolean drawInPixelCoords = true;

    public TooltipPainter( TextureAtlas atlas )
    {
        this.breakIterator = BreakIterator.getWordInstance( );
        this.paintBackground = true;
        this.paintBorder = true;
        this.atlas = atlas;
    }

    public TooltipPainter( )
    {
        this( null );
    }

    /**
     * Sets the icon to be displayed on the first line of the tool tip.
     * @param id the id of the icon in TooltipPainter's TextureAtlas
     */
    public synchronized TooltipPainter setIcon( Object iconId )
    {
        this.iconIds = Collections.singletonList( iconId );
        this.iconColors = null;
        this.icons = null; // signal that the icons should be recalculated
        this.lines = null; // signal that layout should be recalculated
        return this;
    }

    /**
     * Sets the TooltipPainter to display multiple icons, one per line,
     * down the left hand side of the tooltip window.
     */
    public synchronized TooltipPainter setIcons( List<Object> iconIds )
    {
        this.iconIds = Lists.newArrayList( iconIds );
        this.iconColors = null;
        this.icons = null; // signal that the icons should be recalculated
        this.lines = null; // signal that layout should be recalculated
        return this;
    }

    /**
     * Sets icons and associated colors.
     * 
     * @see #setIcons(List)
     */
    public synchronized TooltipPainter setIcons( List<Object> iconIds, List<float[]> colors )
    {
        setIcons( iconIds );
        this.iconColors = Lists.newArrayList( colors );
        return this;
    }

    public synchronized TooltipPainter setWrapTextAroundIcon( boolean wrap )
    {
        this.wrapTextAroundIcon = wrap;
        this.lines = null; // signal that layout should be recalculated
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
        this.lines = null; // signal that layout should be recalculated
        return this;
    }

    public synchronized TooltipPainter setFixedWidth( int fixedWidth )
    {
        this.fixedWidth = fixedWidth;
        this.isFixedWidth = true;
        this.lines = null; // signal that layout should be recalculated
        return this;
    }

    public synchronized TooltipPainter setUnlimitedWidth( )
    {
        this.isFixedWidth = false;
        this.lines = null; // signal that layout should be recalculated
        return this;
    }

    public synchronized TooltipPainter setBreakOnEol( boolean breakOnEol )
    {
        this.breakOnEol = breakOnEol;
        this.textLayout = null; // signal that textLayout should be recreated
        return this;
    }

    public synchronized TooltipPainter setLineSpacing( float lineSpacing )
    {
        this.lineSpacing = lineSpacing;
        this.textLayout = null; // signal that textLayout should be recreated
        return this;
    }

    public synchronized TooltipPainter setBreakIterator( BreakIterator breakIterator )
    {
        this.breakIterator = breakIterator;
        this.textLayout = null; // signal that textLayout should be recreated
        return this;
    }

    @Override
    public synchronized TooltipPainter setText( String text )
    {
        this.text = text;
        this.lines = null; // signal that layout should be recalculated
        return this;
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
    public synchronized float getLineSpacing( )
    {
        return lineSpacing;
    }

    protected void updateTextLayout( )
    {
        Font font = textRenderer.getFont( );
        FontRenderContext frc = textRenderer.getFontRenderContext( );
        textLayout = new SimpleTextLayoutCenter( font, frc, breakIterator );
        textLayout.setBreakOnEol( breakOnEol );
        textLayout.setLineSpacing( lineSpacing );
        if ( ( Float ) iconSize != null && iconSize != 0 && !noIcons ) textLayout.setAscent( iconSize );
        iconSize = getIconSize( );
    }

    protected float getIconSize( )
    {
        //XXX another spacing heuristic which it would be nice to eliminate
        return ( float ) textLayout.getAscent( );
    }

    public void setIconSize( float size )
    {
        iconSize = size;
    }

    protected float getIconSpacing( int i )
    {
        //XXX another spacing heuristic which it would be nice to eliminate
        Object iconId = iconIds != null && i < iconIds.size( ) ? iconIds.get( i ) : null;

        float indent = ( float ) ( textLayout.getAscent( ) + borderSize );

        if ( noIcons )
        {
            return 0;
        }
        else if ( iconId != null )
        {
            return indent;
        }
        else if ( iconId == null && !wrapTextAroundIcon )
        {
            return indent;
        }
        else
        {
            return 0;
        }
    }

    protected void updateLayout( )
    {
        textLayout.doLayout( text, 0, 0, isFixedWidth ? fixedWidth : Float.MAX_VALUE );
        lines = textLayout.getLines( );

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        if ( !lines.isEmpty( ) )
        {
            for ( int i = 0; i < lines.size( ); i++ )
            {
                TextBoundingBox line = lines.get( i );

                float iconsize = getIconSpacing( i );

                minX = Math.min( minX, line.getMinX( ) );
                minY = Math.min( minY, line.getMinY( ) );
                maxX = Math.max( maxX, line.getMaxX( ) + iconsize );
                maxY = Math.max( maxY, line.getMaxY( ) );
            }
        }

        double overallMinX = minX - borderSize;
        double overallMaxX = maxX + borderSize;
        //XXX subtracting .5 of the descent is just a heuristic to make the spacing
        //XXX at the top and bottom of the bounding box look more uniform
        double overallMinY = minY - textLayout.getDescent( ) * 0.5 - borderSize;
        double overallMaxY = maxY + borderSize;
        linesBounds = new Bounds( overallMinX, overallMaxX, overallMinY, overallMaxY );
    }

    protected void loadIcons( )
    {
        // looks strange, but causes atlas to load pending icons
        // this is necessary to do here because calls to atlas.getImageData( )
        // will fail if we do not
        atlas.beginRendering( );
        atlas.endRendering( );

        int size = iconIds == null ? 0 : iconIds.size( );

        this.icons = Lists.newArrayListWithCapacity( size );

        this.noIcons = true;
        for ( int i = 0; i < size; i++ )
        {
            Object iconId = this.iconIds.get( i );
            ImageData icon = iconId != null ? atlas.getImageData( iconId ) : null;
            if ( icon != null ) noIcons = false;
            this.icons.add( icon );
        }
    }

    @Override
    protected void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        if ( axis == null && !drawInPixelCoords )
        {
            throw new AxisNotSetException( this, context );
        }

        double x = drawInPixelCoords ? this.x : axis.getAxisX( ).valueToScreenPixelUnits( this.x );
        double y = drawInPixelCoords ? this.y : axis.getAxisY( ).getSizePixels( ) - axis.getAxisY( ).valueToScreenPixelUnits( this.y );

        if ( icons == null )
        {
            loadIcons( );
        }

        if ( newFont != null )
        {
            updateTextRenderer( );
        }

        if ( textLayout == null && textRenderer != null )
        {
            if ( text == null ) setText( " " );
            updateTextLayout( );
        }

        if ( lines == null && textLayout != null && text != null )
        {
            updateLayout( );
        }

        if ( textRenderer == null || lines == null ) return;

        GL2 gl = context.getGL( ).getGL2( );
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        double clampX = 0;
        double clampY = 0;
        if ( clampToScreenEdges )
        {
            double maxX = x + linesBounds.maxX + offsetX;
            if ( maxX > width ) clampX = width - maxX;
            double minX = x + linesBounds.minX + offsetX;
            if ( minX < 0 ) clampX = -minX;

            double maxY = height - y + linesBounds.maxY + offsetY;
            if ( maxY > height ) clampY = height - maxY;
            double minY = height - y + linesBounds.minY + offsetY;
            if ( minY < 0 ) clampY = -minY;
        }

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( -0.5, width - 1 + 0.5, -0.5, height - 1 + 0.5, -1, 1 );
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity( );

        gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL2.GL_BLEND );

        if ( this.paintBackground || this.paintBorder )
        {
            if ( this.paintBackground )
            {
                // Draw Text Background
                gl.glColor4fv( backgroundColor, 0 );

                gl.glBegin( GL2.GL_QUADS );
                try
                {
                    borderVertices( gl, height, clampX + offsetX, clampY + offsetY, x, y );
                }
                finally
                {
                    gl.glEnd( );
                }
            }

            if ( this.paintBorder )
            {
                // Draw Text Background
                gl.glColor4fv( borderColor, 0 );
                gl.glEnable( GL2.GL_LINE_SMOOTH );

                gl.glBegin( GL2.GL_LINE_LOOP );
                try
                {
                    borderVertices( gl, height, clampX + offsetX, clampY + offsetY, x, y );
                }
                finally
                {
                    gl.glEnd( );
                }
            }
        }

        gl.glDisable( GL2.GL_BLEND );

        // draw text
        GlimpseColor.setColor( textRenderer, textColor );
        textRenderer.beginRendering( width, height );
        try
        {
            for ( int i = 0; i < lines.size( ); i++ )
            {
                TextBoundingBox line = lines.get( i );

                float iconsize = getIconSpacing( i );

                int posX = ( int ) ( x + line.leftX + iconsize + clampX + offsetX );
                int posY = ( int ) ( height - y + line.getMinY( ) + clampY + offsetY );

                textRenderer.draw( line.text, posX, posY );
            }
        }
        finally
        {
            textRenderer.endRendering( );
        }

        // draw icon
        if ( !lines.isEmpty( ) && iconIds != null && !iconIds.isEmpty( ) )
        {
            atlas.beginRendering( );
            try
            {
                for ( int i = 0; i < iconIds.size( ); i++ )
                {
                    Object iconId = iconIds.get( i );
                    ImageData iconData = icons.get( i );
                    TextBoundingBox line = lines.get( i );

                    if ( iconId != null && iconData != null && line != null )
                    {
                        double iconScale = iconSize / ( double ) iconData.getWidth( );

                        int posX = ( int ) ( x + line.leftX + clampX + offsetX );
                        //XXX another spacing heuristic which it would be nice to eliminate
                        int posY = ( int ) ( height - y + line.getMinY( ) + clampY + offsetY - textLayout.getDescent( ) * 0.25 );

                        float[] color = defaultIconColor;
                        if ( iconColors != null && i < iconColors.size( ) )
                        {
                            float[] iconColor = iconColors.get( i );
                            if ( iconColor != null )
                            {
                                color = iconColor;
                            }
                        }

                        GlimpseColor.glColor( gl, color );

                        atlas.drawImage( gl, iconId, posX, posY, iconScale, iconScale, 0, iconData.getHeight( ) );
                    }
                }
            }
            finally
            {
                atlas.endRendering( );
            }
        }
    }

    protected void borderVertices( GL2 gl, int height, double offsetX, double offsetY, double x, double y )
    {
        double posX = x + linesBounds.minX + offsetX;
        double posY = height - y + linesBounds.minY + offsetY;
        gl.glVertex2d( posX, posY );

        posX = x + linesBounds.maxX + offsetX;
        posY = height - y + linesBounds.minY + offsetY;
        gl.glVertex2d( posX, posY );

        posX = x + linesBounds.maxX + offsetX;
        posY = height - y + linesBounds.maxY + offsetY;
        gl.glVertex2d( posX, posY );

        posX = x + linesBounds.minX + offsetX;
        posY = height - y + linesBounds.maxY + offsetY;
        gl.glVertex2d( posX, posY );
    }

    protected static class Bounds
    {
        public double minX, maxX, minY, maxY;

        public Bounds( double minX, double maxX, double minY, double maxY )
        {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }

        @Override
        public String toString( )
        {
            return String.format( "%f %f %f %f", minX, maxX, minY, maxY );
        }
    }
}
