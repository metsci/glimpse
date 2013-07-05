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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.metsci.glimpse.docking.DockingPane.Arrangement.ArrangementNode;

import de.muntjak.tinylookandfeel.Theme;
import de.muntjak.tinylookandfeel.TinyLookAndFeel;

import static com.metsci.glimpse.docking.DockingPane.Arrangement.*;
import static com.metsci.glimpse.docking.DockingThemes.*;
import static com.metsci.glimpse.docking.DockingUtils.*;
import static java.util.logging.Level.*;
import static javax.swing.JFrame.*;

public class DockingExample
{

    protected static final Logger logger = Logger.getLogger( DockingExample.class.getName( ) );


    public static void main( String[] args ) throws Exception
    {
        Theme.loadTheme( DockingExample.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
        UIManager.setLookAndFeel( new TinyLookAndFeel( ) );
        DockingTheme dockingTheme = tinyLafDockingTheme( );


        JPanel aPanel = new JPanel( ) {{ setBackground( Color.red ); }};
        JPanel bPanel = new JPanel( ) {{ setBackground( Color.green ); }};
        JPanel cPanel = new JPanel( ) {{ setBackground( Color.blue ); }};
        JPanel dPanel = new JPanel( ) {{ setBackground( Color.cyan ); }};
        JPanel ePanel = new JPanel( ) {{ setBackground( Color.magenta ); }};
        JPanel fPanel = new JPanel( ) {{ setBackground( Color.yellow ); }};
        JPanel gPanel = new JPanel( ) {{ setBackground( Color.gray ); }};
        JPanel hPanel = new JPanel( ) {{ setBackground( Color.white ); }};


        JToolBar aToolbar = newToolbar( true );
        aToolbar.add( new JButton( "A1" ) );
        aToolbar.add( new JButton( "A2" ) );
        aToolbar.add( new JButton( "A3" ) );

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


        final DockingPane dockingPane = new DockingPane( dockingTheme );
        dockingPane.addView( new View( "aView", "View A", requireIcon( "icons/ViewA.png" ), null, aPanel, aToolbar ) );
        dockingPane.addView( new View( "bView", "View B", requireIcon( "icons/ViewB.png" ), null, bPanel, bToolbar ) );
        dockingPane.addView( new View( "cView", "View C", requireIcon( "icons/ViewC.png" ), null, cPanel, cToolbar ) );
        dockingPane.addView( new View( "dView", "View D", requireIcon( "icons/ViewD.png" ), null, dPanel, dToolbar ) );
        dockingPane.addView( new View( "eView", "View E", requireIcon( "icons/ViewE.png" ), null, ePanel, eToolbar ) );
        dockingPane.addView( new View( "fView", "View F", requireIcon( "icons/ViewF.png" ), null, fPanel, fToolbar ) );
        dockingPane.addView( new View( "gView", "View G", requireIcon( "icons/ViewG.png" ), null, gPanel, gToolbar ) );
        dockingPane.addView( new View( "hView", "View H", requireIcon( "icons/ViewH.png" ), null, hPanel, hToolbar ) );




        JFrame frame = new JFrame( "Docking Example" );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setContentPane( dockingPane );
        frame.setPreferredSize( new Dimension( 1600, 900 ) );
        frame.pack( );

        swingRun( dockingPane.restoreArrangement, loadDockingArrangement( "docking-example" ) );

        frame.addWindowListener( new WindowAdapter( )
        {
            public void windowClosing( WindowEvent ev )
            {
                saveDockingArrangement( "docking-example", dockingPane.captureArrangement( ) );
            }
        } );

        frame.setVisible( true );
    }

    public static void saveDockingArrangement( String appName, ArrangementNode arrangement )
    {
        try
        {
            File arrangementFile = new File( createAppDir( appName ), "arrangement.xml" );
            writeDockingArrangementXml( arrangement, arrangementFile );
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to write docking arrangement to file", e );
        }
    }

    public static ArrangementNode loadDockingArrangement( String appName )
    {
        try
        {
            File arrangementFile = new File( createAppDir( appName ), "arrangement.xml" );
            if ( arrangementFile.exists( ) )
            {
                return readDockingArrangementXml( arrangementFile );
            }
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to load docking arrangement from file", e );
        }

        try
        {
            return readDockingArrangementXml( DockingExample.class.getClassLoader( ).getResourceAsStream( "docking/arrangement-default.xml" ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

}
