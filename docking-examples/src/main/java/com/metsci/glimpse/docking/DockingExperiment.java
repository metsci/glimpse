package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingThemes.tinyLafDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.newButtonPopup;
import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.swingRun;
import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

import com.metsci.glimpse.docking2.DockingPane;
import com.metsci.glimpse.docking2.DockingPaneGroup;
import com.metsci.glimpse.docking2.TileFactories.TileFactory;
import com.metsci.glimpse.docking2.TileFactories.TileFactoryStandard;

public class DockingExperiment
{

    public static void main( String[] args ) throws Exception
    {
        Theme.loadTheme( DockingExperiment.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
        UIManager.setLookAndFeel( new TinyLookAndFeel( ) );
        DockingTheme dockingTheme = tinyLafDockingTheme( );


        DockingPaneGroup dockerGroup = new DockingPaneGroup( dockingTheme );
        TileFactory tileFactory = new TileFactoryStandard( dockerGroup );


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

        JToggleButton aOptionsButton = new JToggleButton( dockingTheme.optionsIcon );
        JPopupMenu aOptionsPopup = newButtonPopup( aOptionsButton );
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


        View aView = new View( "aView", "View A", requireIcon( "icons/ViewA.png" ), null, aPanel, aToolbar );
        View bView = new View( "bView", "View B", requireIcon( "icons/ViewB.png" ), null, bPanel, bToolbar );
        View cView = new View( "cView", "View C", requireIcon( "icons/ViewC.png" ), null, cPanel, cToolbar );
        View dView = new View( "dView", "View D", requireIcon( "icons/ViewD.png" ), null, dPanel, dToolbar );
        View eView = new View( "eView", "View E", requireIcon( "icons/ViewE.png" ), null, ePanel, eToolbar );
        View fView = new View( "fView", "View F", requireIcon( "icons/ViewF.png" ), null, fPanel, fToolbar );
        View gView = new View( "gView", "View G", requireIcon( "icons/ViewG.png" ), null, gPanel, gToolbar );
        View hView = new View( "hView", "View H", requireIcon( "icons/ViewH.png" ), null, hPanel, hToolbar );


        Tile aTile = tileFactory.newTile( );
        aTile.addView( aView, 0 );
        aTile.addView( bView, 1 );
        aTile.addView( cView, 2 );

        Tile bTile = tileFactory.newTile( );
        bTile.addView( dView, 0 );
        bTile.addView( eView, 1 );

        Tile cTile = tileFactory.newTile( );
        cTile.addView( fView, 0 );
        cTile.addView( gView, 1 );
        cTile.addView( hView, 2 );


        final DockingPane docker = dockerGroup.addNewDocker( );
        docker.addInitialTile( aTile );
        docker.addNeighborTile( bTile, aTile, LEFT, 0.3 );
        docker.addEdgeTile( cTile, BOTTOM, 0.3 );


        swingRun( new Runnable( )
        {
            public void run( )
            {
                JFrame frame = new JFrame( );
                frame.setContentPane( docker );

                // XXX
                frame.setDefaultCloseOperation( EXIT_ON_CLOSE );

                frame.setPreferredSize( new Dimension( 1024, 768 ) );
                frame.pack( );
                frame.setLocationByPlatform( true );
                frame.setVisible( true );
            }
        });
    }

}
