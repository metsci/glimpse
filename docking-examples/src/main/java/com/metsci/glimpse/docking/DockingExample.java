package com.metsci.glimpse.docking;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.metsci.glimpse.docking.DockingPane.Config.ConfigNode;

import de.muntjak.tinylookandfeel.Theme;
import de.muntjak.tinylookandfeel.TinyLookAndFeel;

import static com.metsci.glimpse.docking.DockingPane.Config.*;
import static com.metsci.glimpse.docking.DockingUtils.*;
import static java.util.logging.Level.*;
import static javax.swing.BorderFactory.*;
import static javax.swing.JFrame.*;

public class DockingExample
{

    protected static final Logger logger = Logger.getLogger( DockingExample.class.getName( ) );


    public static void main( String[] args ) throws Exception
    {
        Theme.loadTheme( DockingExample.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
        UIManager.setLookAndFeel( new TinyLookAndFeel( ) );


        JPanel aPanel = new JPanel( ) {{ setBorder( createLineBorder( getBackground( ), 25 ) ); setBackground( Color.red ); }};
        JPanel bPanel = new JPanel( ) {{ setBorder( createLineBorder( getBackground( ), 25 ) ); setBackground( Color.green ); }};
        JPanel cPanel = new JPanel( ) {{ setBorder( createLineBorder( getBackground( ), 25 ) ); setBackground( Color.blue ); }};
        JPanel dPanel = new JPanel( ) {{ setBorder( createLineBorder( getBackground( ), 25 ) ); setBackground( Color.cyan ); }};
        JPanel ePanel = new JPanel( ) {{ setBorder( createLineBorder( getBackground( ), 25 ) ); setBackground( Color.magenta ); }};
        JPanel fPanel = new JPanel( ) {{ setBorder( createLineBorder( getBackground( ), 25 ) ); setBackground( Color.yellow ); }};
        JPanel gPanel = new JPanel( ) {{ setBorder( createLineBorder( getBackground( ), 25 ) ); setBackground( Color.gray ); }};
        JPanel hPanel = new JPanel( ) {{ setBorder( createLineBorder( getBackground( ), 25 ) ); setBackground( Color.white ); }};


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


        final DockingPane<?> dockingPane = new ExperimentalDockingPane( );
        dockingPane.addView( new View( "aView", "View A", null, null, aPanel, aToolbar ) );
        dockingPane.addView( new View( "bView", "View B", null, null, bPanel, bToolbar ) );
        dockingPane.addView( new View( "cView", "View C", null, null, cPanel, cToolbar ) );
        dockingPane.addView( new View( "dView", "View D", null, null, dPanel, dToolbar ) );
        dockingPane.addView( new View( "eView", "View E", null, null, ePanel, eToolbar ) );
        dockingPane.addView( new View( "fView", "View F", null, null, fPanel, fToolbar ) );
        dockingPane.addView( new View( "gView", "View G", null, null, gPanel, gToolbar ) );
        dockingPane.addView( new View( "hView", "View H", null, null, hPanel, hToolbar ) );




        JFrame frame = new JFrame( "Docking Example" );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setContentPane( dockingPane );
        frame.setPreferredSize( new Dimension( 1600, 900 ) );
        frame.pack( );

        swingRun( dockingPane.restoreConfig, loadDockingArrangement( "docking-example" ) );

//        frame.addWindowListener( new WindowAdapter( )
//        {
//            public void windowClosing( WindowEvent ev )
//            {
//                saveDockingArrangement( "docking-example", dockingPane.captureConfig( ) );
//            }
//        } );

        frame.setVisible( true );
    }

    public static void saveDockingArrangement( String appName, ConfigNode arrangement )
    {
        try
        {
            File arrangementFile = new File( createAppDir( appName ), "arrangement.xml" );
            writeDockingConfigXml( arrangement, arrangementFile );
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to write docking arrangement to file", e );
        }
    }

    public static ConfigNode loadDockingArrangement( String appName )
    {
        try
        {
            File arrangementFile = new File( createAppDir( appName ), "arrangement.xml" );
            if ( arrangementFile.exists( ) )
            {
                return readDockingConfigXml( arrangementFile );
            }
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to load docking arrangement from file", e );
        }

        try
        {
            return readDockingConfigXml( DockingExample.class.getClassLoader( ).getResourceAsStream( "docking/arrangement-default.xml" ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

}
