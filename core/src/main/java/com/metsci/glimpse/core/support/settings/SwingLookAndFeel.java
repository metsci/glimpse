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
package com.metsci.glimpse.core.support.settings;

import static com.metsci.glimpse.core.support.color.GlimpseColor.addRgb;
import static com.metsci.glimpse.core.support.color.GlimpseColor.getBlack;
import static com.metsci.glimpse.core.support.color.GlimpseColor.getWhite;
import static com.metsci.glimpse.core.support.font.FontUtils.getDefaultPlain;

import java.awt.Color;
import java.awt.SystemColor;

import javax.swing.UIManager;

import com.metsci.glimpse.core.support.color.GlimpseColor;

public class SwingLookAndFeel extends AbstractLookAndFeel
{
    public SwingLookAndFeel( )
    {
        Color swingBg = UIManager.getColor( "Panel.background" );
        float[] bg = GlimpseColor.fromColorAwt( swingBg == null ? SystemColor.window : swingBg );

        map.put( CROSSHAIR_COLOR, getBlack( ) );
        map.put( BORDER_COLOR, getBlack( ) );

        map.put( PLOT_BACKGROUND_COLOR, addRgb( bg, -0.1f ) );
        map.put( FRAME_BACKGROUND_COLOR, bg );

        map.put( AXIS_TEXT_COLOR, getBlack( ) );
        map.put( AXIS_TICK_COLOR, getBlack( ) );
        map.put( AXIS_TAG_COLOR, getBlack( 0.2f ) );

        map.put( AXIS_FONT, getDefaultPlain( 11 ) );
        map.put( TITLE_FONT, getDefaultPlain( 14 ) );

        map.put( TOOLTIP_BACKGROUND_COLOR, getBlack( 0.7f ) );
        map.put( TOOLTIP_TEXT_COLOR, getWhite( ) );
    }
}
