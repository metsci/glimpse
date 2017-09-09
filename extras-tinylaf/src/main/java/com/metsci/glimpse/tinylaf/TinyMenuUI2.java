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
package com.metsci.glimpse.tinylaf;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.plaf.ComponentUI;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyMenuUI;

public class TinyMenuUI2 extends TinyMenuUI
{

    /**
     * @see ComponentUI#createUI(JComponent)
     */
    public static ComponentUI createUI( JComponent c )
    {
        return new TinyMenuUI2( );
    }

    @Override
    public void paint( Graphics g, JComponent c )
    {
        JMenuItem menuItem = ( JMenuItem ) c;
        if ( isTopLevelMenu( menuItem ) && menuItem.isSelected( ) )
        {
            // Use rollover colors for the selected top-level menu item
            Color menuFontColor = Theme.menuFontColor.getColor( );
            Color menuItemFontColor = Theme.menuItemFontColor.getColor( );
            Theme.menuFontColor.setColor( Theme.menuRolloverFgColor.getColor( ) );
            Theme.menuItemFontColor.setColor( Theme.menuRolloverFgColor.getColor( ) );
            try
            {
                super.paint( g, c );
            }
            finally
            {
                Theme.menuFontColor.setColor( menuFontColor );
                Theme.menuItemFontColor.setColor( menuItemFontColor );
            }
        }
        else
        {
            // Otherwise the superclass implementation looks fine
            super.paint( g, c );
        }
    }

    @Override
    protected void paintBackground( Graphics g, JMenuItem menuItem, Color bgColor, boolean isLeftToRight )
    {
        if ( isTopLevelMenu( menuItem ) && menuItem.isSelected( ) )
        {
            // Use rollover colors for the selected top-level menu item
            if ( menuItem.isOpaque( ) )
            {
                Color oldColor = g.getColor( );
                int w = menuItem.getWidth( );
                int h = menuItem.getHeight( );

                g.setColor( Theme.menuRolloverBgColor.getColor( ) );
                g.fillRect( 0, 0, w, h );

                g.setColor( Theme.menuBorderColor.getColor( ) );
                g.drawRect( 0, 0, w - 1, h - 1 );

                g.setColor( oldColor );
            }
        }
        else
        {
            // Otherwise the superclass implementation looks fine
            super.paintBackground( g, menuItem, bgColor, isLeftToRight );
        }
    }

    public static boolean isTopLevelMenu( JMenuItem menuItem )
    {
        return ( menuItem instanceof JMenu && ( ( JMenu ) menuItem ).isTopLevelMenu( ) );
    }

}
