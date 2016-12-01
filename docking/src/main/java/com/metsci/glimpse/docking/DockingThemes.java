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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static java.awt.Color.black;
import static java.awt.Color.darkGray;
import static java.awt.Color.lightGray;
import static java.awt.Color.white;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

public class DockingThemes
{
    protected static final Logger logger = Logger.getLogger( DockingThemes.class.getName( ) );

    public static DockingTheme defaultDockingTheme( )
    {
        LookAndFeel laf = UIManager.getLookAndFeel( );
        if ( laf instanceof TinyLookAndFeel )
        {
            return tinyLafDockingTheme( );
        }
        else
        {
            return basicDockingTheme;
        }
    }

    public static final DockingTheme basicDockingTheme = new DockingTheme( 5,

                                                                           2,
                                                                           black,

                                                                           1, // Even lineThickness values do NOT work well
                                                                           5,
                                                                           2,
                                                                           4,

                                                                           lightGray,
                                                                           white,
                                                                           darkGray,
                                                                           darkGray,

                                                                           requireIcon( "icons/maximize.gif" ),
                                                                           requireIcon( "icons/unmaximize.gif" ),
                                                                           requireIcon( "icons/options.gif" ),

                                                                           requireIcon( "icons/chromium/close.png" ),
                                                                           requireIcon( "icons/chromium/close_h.png" ),
                                                                           requireIcon( "icons/chromium/close_p.png" ) );

    public static DockingTheme newDockingTheme( Color lineColor, Color textColor )
    {
        return new DockingTheme( basicDockingTheme.dividerSize,

                                 basicDockingTheme.landingIndicatorThickness,
                                 basicDockingTheme.landingIndicatorColor,

                                 basicDockingTheme.lineThickness,
                                 basicDockingTheme.cornerRadius,
                                 basicDockingTheme.cardPadding,
                                 basicDockingTheme.labelPadding,

                                 lineColor,
                                 basicDockingTheme.highlightColor,
                                 textColor,
                                 textColor,

                                 basicDockingTheme.maximizeIcon,
                                 basicDockingTheme.unmaximizeIcon,
                                 basicDockingTheme.optionsIcon,

                                 basicDockingTheme.closeViewIcon,
                                 basicDockingTheme.closeViewHoveredIcon,
                                 basicDockingTheme.closeViewPressedIcon );
    }

    public static DockingTheme tinyLafDockingTheme( )
    {
        return newDockingTheme( Theme.tabPaneBorderColor.getColor( ), Theme.tabFontColor.getColor( ) );
    }

}
