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
import java.awt.font.FontRenderContext;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Lays out the text, centering along the horizontal and vertical axis. The
 * center of each line of the text is at the center of the bounding box provided
 * in {@link #doLayout(String, float, float, float, float)}. And the entire
 * block of text is vertically centered in the same bounding box.
 *
 * @author borkholder
 */
public class SimpleTextLayoutCenter extends SimpleTextLayout
{
    public SimpleTextLayoutCenter( Font font, FontRenderContext frc )
    {
        super( font, frc );
    }

    public SimpleTextLayoutCenter( Font font, FontRenderContext frc, BreakIterator breaker )
    {
        super( font, frc, breaker );
    }

    public void doLayout( String text, float leftX, float topY, float maxWidth, float maxHeight )
    {
        super.doLayout( text, leftX, topY, maxWidth );
        centerRectangles( leftX, topY, maxWidth, maxHeight );
    }

    protected void centerRectangles( float leftX, float topY, float maxWidth, float maxHeight )
    {
        float centerLineX = leftX + maxWidth / 2;

        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for ( TextBoundingBox box : lines )
        {
            minY = min( minY, box.getMinY( ) );
            maxY = max( maxY, box.getMaxY( ) );
        }

        float subtractY = ( maxHeight - ( maxY - minY ) ) / 2;
        subtractY = max( 0, subtractY );

        List<TextBoundingBox> newLines = new ArrayList<TextBoundingBox>( lines.size( ) );

        for ( TextBoundingBox box : lines )
        {
            float newX = centerLineX - box.width / 2;
            newX = max( newX, leftX );
            newLines.add( new TextBoundingBox( box.text, box.baselineY - subtractY, newX, box.maxDescent, box.width, box.maxHeight ) );
        }

        lines.clear( );
        lines.addAll( newLines );
    }
}