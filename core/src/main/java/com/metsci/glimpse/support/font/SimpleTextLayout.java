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
package com.metsci.glimpse.support.font;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Performs simple text layout so text can be wrapped in small areas or broken
 * on words or newlines. This implementation currently makes the following
 * assumptions which are valid for latin text:
 *
 * <p>
 * <ul>
 * <li>mapping chars to glyphs is 1:1 in order, (no Unicode surrogate pairs or
 * multi-glyph characters)</li>
 * <li>left-to-right and top-to-bottom text layout</li>
 * </ul>
 * </p>
 *
 * <p>
 * All coordinates are in the same space as the text is being drawn (typically
 * pixels).
 * </p>
 *
 * @author borkholder
 */
public class SimpleTextLayout
{
    protected Font font;
    protected FontRenderContext frc;
    protected BreakIterator breaker;
    protected double lineHeight;

    /**
     * The maximum ascent of any character in the font.
     */
    protected float ascent;

    /**
     * The maximum descent of any character in the font.
     */
    protected float descent;

    protected String text;

    protected List<TextBoundingBox> lines;

    private float lineSpacing;
    private boolean breakOnEol;

    /**
     * @param font
     *            The font to use for the layout
     * @param frc
     *            The FontRenderContext used to rasterize the font. This will
     *            help define the character bounds
     */
    public SimpleTextLayout( Font font, FontRenderContext frc )
    {
        this( font, frc, BreakIterator.getWordInstance( ) );
    }

    /**
     * @param font
     *            The font to use for the layout
     * @param frc
     *            The FontRenderContext used to rasterize the font. This will
     *            help define the character bounds
     * @param breaker
     *            The BreakIterator that is used to break the text (also see
     *            {@link #forceBreakAfter(int)})
     */
    public SimpleTextLayout( Font font, FontRenderContext frc, BreakIterator breaker )
    {
        this.font = font;
        this.frc = frc;
        this.breaker = breaker;

        Rectangle2D rect = font.getMaxCharBounds( frc );
        ascent = ( float ) ( rect.getMaxY( ) - rect.getY( ) );
        descent = ( float ) -rect.getY( );
        lineHeight = font.getSize( );

        setLineSpacing( 0 );
        setBreakOnEol( true );
    }

    public double getLineHeight( )
    {
        return lineHeight;
    }

    public double getDescent( )
    {
        return descent;
    }

    public synchronized double getAscent( )
    {
        return ascent;
    }

    public synchronized void setAscent( float ascent )
    {
        this.ascent = ascent;
    }

    public void setBreakOnEol( boolean breakOnEol )
    {
        this.breakOnEol = breakOnEol;
    }

    /**
     * Whether to force a break on the end of line characters (\r \f \n).
     */
    public boolean getBreakOnEol( )
    {
        return breakOnEol;
    }

    public void setLineSpacing( float lineSpacing )
    {
        this.lineSpacing = lineSpacing;
    }

    /**
     * The spacing between the bottom (descent) of one line of text to the top
     * (ascent) of the next line.
     */
    public float getLineSpacing( )
    {
        return lineSpacing;
    }

    /**
     * Takes the text and performs the layout. The provided constraints
     * determine the top-left position of the text and the maximum width. See
     * {@link #numberOfLines()}, {@link #getLine(int)} and
     * {@link #getBounds(int)} for the results of the layout.
     *
     * @param text
     *            The text to lay out
     * @param leftX
     *            The leftmost X value of any pixel of text
     * @param topY
     *            The topmost Y value of any pixel of text
     * @param maxWidth
     *            The suggested maximum width of any line of the text
     */
    public void doLayout( String text, float leftX, float topY, float maxWidth )
    {
        this.text = text;

        GlyphVector glyphs = font.createGlyphVector( frc, text );
        IntList breaks = getBreaks( glyphs, maxWidth );

        if ( breaks.size( ) == 2 )
        {
            layoutNoBreaks( glyphs, leftX, topY );
            return;
        }

        layout( breaks, glyphs, leftX, topY, maxWidth );
    }

    /**
     * Returns a list of indexes. Each consecutive, overlapping pair of ints is
     * a line, inclusive to exclusive.
     */
    protected IntList getBreaks( GlyphVector glyphs, float maxWidth )
    {
        IntList breaks = new IntArrayList( );
        breaks.add( 0 );

        breaker.setText( text );

        int lastBreakIdx = 0;
        while ( lastBreakIdx < text.length( ) )
        {
            double currentWidth = 0;
            int breakAt = BreakIterator.DONE;

            for ( int i = lastBreakIdx; i < text.length( ) && breakAt == BreakIterator.DONE; i++ )
            {
                if ( forceBreakAfter( i ) )
                {
                    breakAt = i + 1;
                }
                else
                {
                    Shape l = glyphs.getGlyphLogicalBounds( i );
                    double width = l.getBounds2D( ).getWidth( );

                    if ( currentWidth + width > maxWidth )
                    {
                        breakAt = breaker.preceding( i );

                        if ( breakAt == BreakIterator.DONE || breakAt <= lastBreakIdx )
                        {
                            breakAt = breaker.following( i );
                        }

                        if ( breakAt == BreakIterator.DONE )
                        {
                            breakAt = text.length( );
                        }
                    }
                    else
                    {
                        currentWidth += width;
                    }
                }
            }

            if ( breakAt == BreakIterator.DONE )
            {
                breakAt = text.length( );
            }

            breaks.add( breakAt );
            lastBreakIdx = breakAt;

            assert breaks.size( ) <= text.length( );
        }

        return breaks;
    }

    protected void layoutNoBreaks( GlyphVector glyphs, float leftX, float topY )
    {
        Rectangle2D visualBounds = glyphs.getVisualBounds( );
        float baseline = ( float ) -visualBounds.getY( );
        float width = ( float ) visualBounds.getWidth( );
        float height = ( float ) visualBounds.getHeight( );
        float minX = ( float ) visualBounds.getMinX( );
        float minY = topY - ascent;
        lines = Collections.singletonList( new TextBoundingBox( text, baseline, minX, minY, width, height ) );
    }

    protected void layout( IntList breaks, GlyphVector glyphs, float leftX, float topY, float maxWidth )
    {
        lines = new ArrayList<TextBoundingBox>( breaks.size( ) - 1 );

        double prevMinX = 0;
        for ( int i = 1; i < breaks.size( ); i++ )
        {
            int firstIdx = breaks.getInt( i - 1 );
            int lastIdx = breaks.getInt( i ) - 1;

            firstIdx = trimLeft( text, firstIdx );
            lastIdx = trimRight( text, lastIdx );

            if ( lastIdx < firstIdx )
            {
                continue;
            }

            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            for ( int j = firstIdx; j <= lastIdx; j++ )
            {
                Rectangle2D b = glyphs.getGlyphLogicalBounds( j ).getBounds2D( );
                minX = min( minX, b.getMinX( ) );
                minY = min( minY, b.getMinY( ) );
                maxX = max( maxX, b.getMaxX( ) );
                maxY = max( maxY, b.getMaxY( ) );
            }

            prevMinX = minX;
            double height = maxY - minY;
            double width = maxX - minX;

            float baseline = topY - ascent;

            String line = text.substring( firstIdx, lastIdx + 1 );
            TextBoundingBox box = new TextBoundingBox( line, baseline, ( float ) ( minX - prevMinX ), ( float ) minY, ( float ) width, ( float ) height );
            lines.add( box );

            topY = baseline - getLineSpacing( );
        }
    }

    protected int trimLeft( String text, int index )
    {
        while ( index < text.length( ) && trimFromLine( text.charAt( index ) ) )
        {
            index++;
        }

        return index;
    }

    protected int trimRight( String text, int index )
    {
        while ( 0 < index && trimFromLine( text.charAt( index ) ) )
        {
            index--;
        }

        return index;
    }

    protected boolean trimFromLine( char c )
    {
        return Character.isWhitespace( c );
    }

    protected boolean forceBreakAfter( int index )
    {
        char c = text.charAt( index );
        return getBreakOnEol( ) && ( c == '\n' || c == '\r' || c == '\f' );
    }

    public String getSourceText( )
    {
        return text;
    }

    public int numberOfLines( )
    {
        return lines.size( );
    }

    public TextBoundingBox getLine( int line )
    {
        return lines.get( line );
    }

    public List<TextBoundingBox> getLines( )
    {
        return new ArrayList<TextBoundingBox>( lines );
    }

    /**
     * When drawing using the JOGL {@code TextRenderer}, the {@link #leftX} and
     * {@link #baselineY} should be used as the text origin.
     *
     * @author borkholder
     */
    public static class TextBoundingBox
    {
        /**
         * The text to display on this line.
         */
        public final String text;

        /**
         * The y coordinate for the baseline, this is in the same
         * coordinate-system as the provided parameters in
         * {@link SimpleTextLayout#doLayout(String, float, float, float)}.
         */
        public final float baselineY;

        /**
         * The left-most x coordinate for the text.
         */
        public final float leftX;

        /**
         * The maximum descent of any character in this string. If no characters
         * in the line have descenders, this will 0 or negative (indicating all
         * characters are above the baseline).
         */
        public final float maxDescent;

        /**
         * The total width of the string.
         */
        public final float width;

        /**
         * The difference between the maximum ascent of any character and the
         * lowest descent of any character.
         */
        public final float maxHeight;

        public TextBoundingBox( String text, float baselineY, float leftX, float descent, float width, float height )
        {
            this.text = text;
            this.baselineY = baselineY;
            this.leftX = leftX;
            this.maxDescent = descent;
            this.width = width;
            this.maxHeight = height;
        }

        /**
         * Gets the maximum y coordinate of the text bounding box.
         */
        public float getMaxY( )
        {
            return baselineY + maxHeight - maxDescent;
        }

        /**
         * Gets the minimum Y coordinate of the text bounding box.
         */
        public float getMinY( )
        {
            return baselineY - maxDescent;
        }

        /**
         * Gets the minimum X coordinate of the text bounding box.
         */
        public float getMinX( )
        {
            return leftX;
        }

        /**
         * Gets the maximum X coordinate of the text bounding box.
         */
        public float getMaxX( )
        {
            return leftX + width;
        }
    }
}