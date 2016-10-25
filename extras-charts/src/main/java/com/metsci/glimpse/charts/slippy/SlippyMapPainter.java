/*
 * Copyright (c) 2016 Metron, Inc.
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
package com.metsci.glimpse.charts.slippy;

import java.awt.Font;

import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.support.font.FontUtils;

/**
 * This just wraps a tile painter and an attribution text painter (if one is specified).
 *
 * @author oren
 *
 */
public class SlippyMapPainter extends DelegatePainter
{

    protected final SlippyMapTilePainter tilePainter;
    protected final String attribution;
    protected final SimpleTextPainter attributionPainter;

    private static final Font textFont = FontUtils.getDefaultPlain( 15.0f );
    private static final float[] textColor = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
    private static final float[] bgColor = new float[] { 0.95f, 0.95f, 0.95f, 1f };

    public SlippyMapPainter( SlippyMapTilePainter tilePainter )
    {
        this( tilePainter, null );
    }

    public SlippyMapPainter( SlippyMapTilePainter tilePainter, String attribution )
    {
        super( );
        this.tilePainter = tilePainter;
        this.attribution = attribution;
        addPainter( tilePainter );
        if ( attribution != null )
        {
            attributionPainter = createAttributionPainter( attribution );
            addPainter( attributionPainter );
        }
        else
        {
            attributionPainter = null;
        }
    }

    public static SimpleTextPainter createAttributionPainter( String text )
    {
        SimpleTextPainter painter = new SimpleTextPainter( );
        painter.setText( text );
        painter.setFont( textFont );
        painter.setColor( textColor );
        painter.setBackgroundColor( bgColor );
        painter.setPaintBackground( true );
        return painter;
    }

    public SlippyMapTilePainter getTilePainter( )
    {
        return tilePainter;
    }

    public SimpleTextPainter getAttributionPainter( )
    {
        return attributionPainter;
    }

}
