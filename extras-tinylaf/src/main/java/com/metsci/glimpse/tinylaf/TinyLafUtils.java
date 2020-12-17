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
package com.metsci.glimpse.tinylaf;

import static java.lang.Boolean.FALSE;
import static java.util.logging.Level.WARNING;
import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.AWTError;
import java.awt.Color;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

public class TinyLafUtils
{
    private static final Logger logger = Logger.getLogger( TinyLafUtils.class.getName( ) );

    public static void initTinyLaf( )
    {
        initTinyLaf( TinyLafUtils.class.getResource( "radiance.theme" ) );
    }

    public static void initTinyLaf( URL themeUrl )
    {
        try
        {
            Theme.loadTheme( themeUrl );

            TinyLookAndFeel laf = new TinyLookAndFeel( );
            UIManager.setLookAndFeel( laf );

            scaleFonts( laf );

            // TinyLaf uses text-area foreground color for option-pane foreground, which doesn't look right
            Color fgColor = UIManager.getColor( "Label.foreground" );
            if ( fgColor != null )
            {
                UIManager.put( "OptionPane.messageForeground", fgColor );
            }

            // TinyLaf disables the "new folder" button in some cases ... not sure why
            UIManager.put( "FileChooser.readOnly", FALSE );

            // TinyLaf progress bars look dated
            UIManager.put( "ProgressBarUI", TinyProgressBarUI2.class.getName( ) );

            // TinyLaf table rows don't adjust
            UIManager.put( "TableUI", TinyTableUI2.class.getName( ) );

            // TinyLaf top-level menus need tweaking
            UIManager.put( "MenuUI", TinyMenuUI2.class.getName( ) );
            UIManager.put( "Menu.border", createEmptyBorder( 6, 6, 4, 3 ) );
            UIManager.put( "MenuBar.border", createEmptyBorder( ) );

            // TinyLaf menu item spacing needs tweaking
            UIManager.put( "MenuItem.border", createEmptyBorder( 5, 8, 4, 8 ) );
            UIManager.put( "CheckBoxMenuItem.border", createEmptyBorder( 5, 8, 4, 8 ) );
            UIManager.put( "RadioButtonMenuItem.border", createEmptyBorder( 5, 8, 4, 8 ) );
        }
        catch ( UnsupportedLookAndFeelException e )
        {
            logger.log( WARNING, "Failed to init Tiny L&F", e );
        }
    }

    private static void scaleFonts( LookAndFeel laf )
    {
        float scale = getDesktopTextScaling( );
        if ( scale == 1 )
        {
            return;
        }

        Enumeration<Object> keysItr = laf.getDefaults( ).keys( );
        Map<Object, Object> toChange = new HashMap<>( );
        while ( keysItr.hasMoreElements( ) )
        {
            Object key = keysItr.nextElement( );
            Object value = laf.getDefaults( ).getFont( key );

            // only scale fonts
            if ( value instanceof FontUIResource && !toChange.containsKey( key ) )
            {
                FontUIResource r = ( FontUIResource ) value;
                toChange.put( key, new FontUIResource( r.deriveFont( r.getSize( ) * scale ) ) );
            }
        }

        toChange.forEach( UIManager::put );
    }

    /**
     * Gets the text-scaling parameter which is often used to adjust for HiDPI displays on Windows and Linux.
     */
    public static float getDesktopTextScaling( )
    {
        try
        {
            // Works for GTK font-scaling
            Object dpiProp = Toolkit.getDefaultToolkit( ).getDesktopProperty( "gnome.Xft/DPI" );
            if ( dpiProp instanceof Number )
            {
                // Don't know why it's multiplied by 1024
                int dpi = ( ( Number ) dpiProp ).intValue( ) / 1024;
                if ( dpi != 96 )
                {
                    return dpi / 96.0f;
                }
            }
        }
        catch ( AWTError ex )
        {
            // ignore
        }

        return 1;
    }
}
