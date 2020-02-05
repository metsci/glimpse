/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.docking.examples;

import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.attachPopupMenu;
import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.setArrangementAndSaveOnDispose;
import static com.metsci.glimpse.docking.DockingWindowTitlers.createDefaultWindowTitler;
import static com.metsci.glimpse.docking.ViewCloseOption.VIEW_NOT_CLOSEABLE;
import static com.metsci.glimpse.support.QuickUtils.initStandardGlimpseApp;
import static com.metsci.glimpse.tinylaf.TinyLafUtils.initTinyLaf;
import static java.awt.Color.blue;
import static java.awt.Color.cyan;
import static java.awt.Color.gray;
import static java.awt.Color.green;
import static java.awt.Color.magenta;
import static java.awt.Color.red;
import static java.awt.Color.white;
import static java.awt.Color.yellow;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.group.frame.DockingGroupMultiframe;

public class SimpleDockingExample
{

    public static void main( String[] args ) throws Exception
    {
        // Initialize the GUI on the Swing thread, to avoid graphics-driver coredumps on shutdown
        SwingUtilities.invokeLater( ( ) ->
        {
            initTinyLaf( );
            initStandardGlimpseApp( );
            DockingTheme dockingTheme = defaultDockingTheme( );


            // Create view components
            //

            JPanel aPanel = newSolidPanel( red );
            JPanel bPanel = newSolidPanel( green );
            JPanel cPanel = newSolidPanel( blue );
            JPanel dPanel = newSolidPanel( cyan );
            JPanel ePanel = newSolidPanel( magenta );
            JPanel fPanel = newSolidPanel( yellow );
            JPanel gPanel = newSolidPanel( gray );
            JPanel hPanel = newSolidPanel( white );


            // Create view toolbars
            //

            JToolBar aToolbar = newToolbar( true );
            aToolbar.add( new JButton( "A1" ) );
            aToolbar.add( new JButton( "A2" ) );
            aToolbar.add( new JButton( "A3" ) );

            JToggleButton aOptionsButton = new JToggleButton( dockingTheme.optionsIcon );
            JPopupMenu aOptionsPopup = new JPopupMenu( );
            attachPopupMenu( aOptionsButton, aOptionsPopup );
            aOptionsPopup.add( new JMenuItem( "Option 1" ) );
            aToolbar.add( aOptionsButton );

            JToolBar bToolbar = newToolbar( true );
            bToolbar.add( new JButton( "B1" ) );

            JToolBar cToolbar = null;

            JToolBar dToolbar = newToolbar( true );
            dToolbar.add( new JButton( "D1" ) );
            dToolbar.add( new JButton( "D2" ) );
            dToolbar.add( new JButton( "D3" ) );
            dToolbar.add( new JButton( "D4" ) );
            dToolbar.add( new JButton( "D5" ) );

            JToolBar eToolbar = newToolbar( true );
            eToolbar.add( new JButton( "E1" ) );
            eToolbar.add( new JButton( "E2" ) );

            JToolBar fToolbar = newToolbar( true );
            fToolbar.add( new JButton( "F1" ) );
            fToolbar.add( new JButton( "F2" ) );
            fToolbar.add( new JButton( "F3" ) );

            JToolBar gToolbar = newToolbar( true );

            JToolBar hToolbar = newToolbar( true );
            hToolbar.add( new JButton( "H1" ) );


            // Create views
            //

            View[] views =
            {
                new View( "aView", aPanel, "View A", VIEW_NOT_CLOSEABLE, null, requireIcon( SimpleDockingExample.class.getResource( "icons/ViewA.png" ) ), aToolbar ),
                new View( "bView", bPanel, "View B", VIEW_NOT_CLOSEABLE, null, requireIcon( SimpleDockingExample.class.getResource( "icons/ViewB.png" ) ), bToolbar ),
                new View( "cView", cPanel, "View C", VIEW_NOT_CLOSEABLE, null, requireIcon( SimpleDockingExample.class.getResource( "icons/ViewC.png" ) ), cToolbar ),
                new View( "dView", dPanel, "View D", VIEW_NOT_CLOSEABLE, null, requireIcon( SimpleDockingExample.class.getResource( "icons/ViewD.png" ) ), dToolbar ),
                new View( "eView", ePanel, "View E", VIEW_NOT_CLOSEABLE, null, requireIcon( SimpleDockingExample.class.getResource( "icons/ViewE.png" ) ), eToolbar ),
                new View( "fView", fPanel, "View F", VIEW_NOT_CLOSEABLE, null, requireIcon( SimpleDockingExample.class.getResource( "icons/ViewF.png" ) ), fToolbar ),
                new View( "gView", gPanel, "View G", VIEW_NOT_CLOSEABLE, null, requireIcon( SimpleDockingExample.class.getResource( "icons/ViewG.png" ) ), gToolbar ),
                new View( "hView", hPanel, "View H", VIEW_NOT_CLOSEABLE, null, requireIcon( SimpleDockingExample.class.getResource( "icons/ViewH.png" ) ), hToolbar ),
            };


            // Create and show the docking group
            //

            String appName = "simple-docking-example";
            DockingGroup dockingGroup = new DockingGroupMultiframe( DISPOSE_ALL_FRAMES, dockingTheme );
            dockingGroup.addListener( createDefaultWindowTitler( "Docking Example" ) );
            setArrangementAndSaveOnDispose( dockingGroup, appName, SimpleDockingExample.class.getResource( "docking/simple-arrangement-default.xml" ) );

            dockingGroup.addViews( views );
            dockingGroup.setVisible( true );

        } );
    }

    public static JPanel newSolidPanel( Color color )
    {
        JPanel panel = new JPanel( );
        panel.setBackground( color );
        return panel;
    }

}
