/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.dnc.geosym;

import static com.metsci.glimpse.util.GeneralUtils.newHashMap;
import static com.metsci.glimpse.util.units.Length.millimetersToInches;
import static java.awt.RenderingHints.KEY_FRACTIONALMETRICS;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
import static java.awt.font.TextAttribute.FONT;
import static java.awt.font.TextAttribute.FOREGROUND;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.ceil;
import static java.lang.Math.round;
import static javax.swing.SwingConstants.BOTTOM;
import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.RIGHT;
import static javax.swing.SwingConstants.TOP;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.Map;

import com.metsci.glimpse.dnc.util.AnchoredImage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class DncGeosymLabelUtils
{

    public static AnchoredImage newLabelImage( AttributedString text, DncGeosymLabelLocation labelLocation, double dpi )
    {
        BufferedImage image0 = new BufferedImage( 1, 1, TYPE_INT_ARGB );
        Graphics2D g0 = (Graphics2D) image0.getGraphics( );
        TextLayout layout0 = new TextLayout( text.getIterator( ), g0.getFontRenderContext( ) );

        int ascent = (int) ceil( layout0.getAscent( ) );
        int descent = (int) ceil( layout0.getDescent( ) );
        int advance = (int) ceil( layout0.getVisibleAdvance( ) );

        int border = 1;
        int height = ascent + descent + 2*border;
        int width = advance + 2*border;


        BufferedImage image = new BufferedImage( width, height, TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D) image.getGraphics( );

        g.setBackground( new Color( 1, 1, 1, 0 ) );
        g.clearRect( 0, 0, width, height );

        g.setRenderingHint( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF );
        g.setRenderingHint( KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_OFF );
        TextLayout layout = new TextLayout( text.getIterator( ), g.getFontRenderContext( ) );
        layout.draw( g, border, ascent+border );


        // This will give the officially correct millimeter offset when drawn with scale 1; otherwise,
        // the offset will shrink/grow with the scale, which is probably what we want anyway
        double mmToTexels = millimetersToInches * dpi;

        int iAnchor = (int) round( -labelLocation.xOffset_MM * mmToTexels );
        switch ( labelLocation.hAlign )
        {
            case LEFT:  iAnchor += border; break;
            case RIGHT: iAnchor += image.getWidth( ) - 1 - border; break;
            default:    iAnchor += image.getWidth( ) / 2; break;
        }

        int jAnchor = (int) round( -labelLocation.yOffset_MM * mmToTexels );
        switch ( labelLocation.vAlign )
        {
            case TOP:    jAnchor += image.getHeight( ) - border; break;
            case BOTTOM: jAnchor += descent + border; break;
            default:     jAnchor += image.getHeight( ) / 2; break;
        }


        return new AnchoredImage( image, iAnchor, jAnchor );
    }

    public static Map<Attribute,Object> toTextAttributes( DncGeosymTextStyle textStyle, Int2ObjectMap<Color> colors )
    {
        Color color = colors.get( textStyle.colorCode );

        Map<Attribute,Object> attributes = newHashMap( );
        attributes.put( FONT, textStyle.font );
        attributes.put( FOREGROUND, color );
        return attributes;
    }

}
