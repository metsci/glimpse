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
package com.metsci.glimpse.painter.decoration;

import java.awt.Font;

import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.font.FontUtils;

/**
 * Displays a discrete copyright notice at the bottom of right corner of the screen.
 *
 * @author osborn
 */
public class CopyrightPainter extends GlimpsePainterBase
{
    private static final String copyrightSymbol = "\u00A9";

    private static final Font textFont = FontUtils.getDefaultPlain( 15.0f );
    private static float[] textColor = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };

    private static final int padding = 5;

    private TextRenderer textRenderer;
    private String text;

    public CopyrightPainter( String company, int year )
    {
        text = String.format( "%s %s %d", company, copyrightSymbol, year );
        textRenderer = new TextRenderer( textFont, true, false );
    }

    public CopyrightPainter( )
    {
        this( "Metron", 2012 );
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        if ( textRenderer == null ) return;

        GlimpseBounds bounds = getBounds( context );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        double wText = textRenderer.getBounds( text ).getWidth( );
        int xText = ( int ) Math.round( width - wText - padding );
        int yText = padding;

        textRenderer.beginRendering( width, height );
        textRenderer.setColor( textColor[0], textColor[1], textColor[2], textColor[3] );
        textRenderer.draw( text, xText, yText );
        textRenderer.endRendering( );
    }
}
