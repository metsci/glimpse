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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static java.awt.Color.black;
import static java.awt.Color.darkGray;
import static java.awt.Color.lightGray;
import static java.awt.Color.white;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

public class DockingThemes
{

    protected static final Logger logger = Logger.getLogger( DockingThemes.class.getName( ) );


    public static class DockingTheme
    {

        public final int dividerSize;

        public final int landingIndicatorThickness;
        public final Color landingIndicatorColor;

        /**
         * Even lineThickness values do NOT work well
         */
        public final int lineThickness;
        public final int cornerRadius;
        public final int cardPadding;
        public final int labelPadding;

        public final Color lineColor;
        public final Color highlightColor;
        public final Color selectedTextColor;
        public final Color unselectedTextColor;

        public final ImageIcon maximizeIcon;
        public final ImageIcon unmaximizeIcon;
        public final ImageIcon optionsIcon;


        public DockingTheme( int dividerSize,

                             int landingIndicatorThickness,
                             Color landingIndicatorColor,

                             int lineThickness, // Even lineThickness values do NOT work well
                             int cornerRadius,
                             int cardPadding,
                             int labelPadding,

                             Color lineColor,
                             Color highlightColor,
                             Color selectedTextColor,
                             Color unselectedTextColor,

                             ImageIcon maximizeIcon,
                             ImageIcon unmaximizeIcon,
                             ImageIcon optionsIcon )
        {
            this.dividerSize = dividerSize;

            this.landingIndicatorThickness = landingIndicatorThickness;
            this.landingIndicatorColor = landingIndicatorColor;

            this.lineThickness = lineThickness;
            this.cornerRadius = cornerRadius;
            this.cardPadding = cardPadding;
            this.labelPadding = labelPadding;

            this.lineColor = lineColor;
            this.highlightColor = highlightColor;
            this.selectedTextColor = selectedTextColor;
            this.unselectedTextColor = unselectedTextColor;

            this.maximizeIcon = maximizeIcon;
            this.unmaximizeIcon = unmaximizeIcon;
            this.optionsIcon = optionsIcon;
        }

    }


    public static final DockingTheme defaultDockingTheme = new DockingTheme( 5,

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
                                                                             requireIcon( "icons/options.gif" ) );


    public static DockingTheme newDockingTheme( Color lineColor, Color textColor )
    {
        return new DockingTheme( defaultDockingTheme.dividerSize,

                                 defaultDockingTheme.landingIndicatorThickness,
                                 defaultDockingTheme.landingIndicatorColor,

                                 defaultDockingTheme.lineThickness,
                                 defaultDockingTheme.cornerRadius,
                                 defaultDockingTheme.cardPadding,
                                 defaultDockingTheme.labelPadding,

                                 lineColor,
                                 defaultDockingTheme.highlightColor,
                                 textColor,
                                 textColor,

                                 defaultDockingTheme.maximizeIcon,
                                 defaultDockingTheme.unmaximizeIcon,
                                 defaultDockingTheme.optionsIcon );
    }


    public static DockingTheme tinyLafDockingTheme( )
    {
        try
        {
            return tinyLafDockingTheme0( );
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.err );
            logger.warning( "TinyLaF is not accessible; default docking theme will be used" );
            return defaultDockingTheme;
        }
    }

    public static DockingTheme tinyLafDockingTheme0( ) throws Exception
    {
        return newDockingTheme( tinyLafColor( "tabPaneBorderColor" ), tinyLafColor( "tabFontColor" ) );
    }

    public static Color tinyLafColor( String fieldName ) throws Exception
    {
        Object sbRef = Class.forName( "net.sf.tinylaf.Theme" ).getField( fieldName ).get( null );
        return ( Color ) Class.forName( "net.sf.tinylaf.util.SBReference" ).getMethod( "getColor" ).invoke( sbRef );
    }

}
