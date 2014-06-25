package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingThemes.*;
import static com.metsci.glimpse.docking.DockingUtils.*;
import static javax.swing.JFrame.*;

import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

/**
 * In addition to saving and restoring the view layout via XML, the view layout can be specified
 * programmatically.
 * 
 * @author ulman
 */
public class ProgrammaticLayoutExample
{
    protected static final Logger logger = Logger.getLogger( SimpleDockingExample.class.getName( ) );

    @SuppressWarnings( "serial" )
    public static void main( String[] args ) throws Exception
    {
        Theme.loadTheme( SimpleDockingExample.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
        UIManager.setLookAndFeel( new TinyLookAndFeel( ) );
        DockingTheme dockingTheme = tinyLafDockingTheme( );
        
        // create the docking pane
        final MultiSplitPane dockingPane = new MultiSplitPane( dockingTheme );
        
        // create a frame to contain the docking pane
        final JFrame frame = new JFrame( "Docking Example" );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        
        SwingUtilities.invokeAndWait( new Runnable( )
        {
            @Override
            public void run( )
            {
                frame.setContentPane( dockingPane );
            }
        });
        
        frame.setPreferredSize( new Dimension( 1600, 900 ) );
        frame.pack( );
        frame.setVisible( true );
    
        
        // create some view content panels
        final JPanel aPanel = new JPanel( ) {{ setBackground( Color.red ); }};
        final JPanel bPanel = new JPanel( ) {{ setBackground( Color.green ); }};
        final JPanel cPanel = new JPanel( ) {{ setBackground( Color.blue ); }};
        
        
        // add tiles (containers for tabbed views) and arrange them then add views to the tiles
        SwingUtilities.invokeAndWait( new Runnable( )
        {
            @Override
            public void run( )
            {
                // create the first tile with no arguments (it will fill the screen by default)
                TileKey aTile = dockingPane.addNewTile( );
                // create a tile below the first (taking up 20% of the screen space)
                TileKey bTile = dockingPane.addNewTile( aTile, Side.BOTTOM, 0.2 );
                // create a tile to the left of the first (by default, takes up half the space)
                TileKey cTile = dockingPane.addNewTile( aTile, Side.LEFT );

                dockingPane.addView( new View( "aView", "View A", requireIcon( "icons/ViewA.png" ), null, aPanel, null ), aTile );
                dockingPane.addView( new View( "bView", "View B", requireIcon( "icons/ViewB.png" ), null, bPanel, null ), bTile );
                dockingPane.addView( new View( "cView", "View C", requireIcon( "icons/ViewC.png" ), null, cPanel, null ), cTile );
            }
        } );
    }
}