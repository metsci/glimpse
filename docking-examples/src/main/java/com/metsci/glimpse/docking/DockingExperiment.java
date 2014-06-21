package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingThemes.tinyLafDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.newButtonPopup;
import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.swingRun;
import static com.metsci.glimpse.docking.MiscUtils.minValueAndIndex;
import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.RIGHT;
import static com.metsci.glimpse.docking.Side.TOP;
import static java.lang.String.format;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import static javax.swing.SwingUtilities.convertPointFromScreen;
import static javax.swing.SwingUtilities.getWindowAncestor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

import com.metsci.glimpse.docking.MiscUtils.IntAndIndex;
import com.metsci.glimpse.docking2.DockingMouseAdapter2;
import com.metsci.glimpse.docking2.DockingPane2;
import com.metsci.glimpse.docking2.LandingRegions.BesideExistingTile;
import com.metsci.glimpse.docking2.LandingRegions.EdgeOfDockingPane;
import com.metsci.glimpse.docking2.LandingRegions.InExistingTile;
import com.metsci.glimpse.docking2.LandingRegions.LandingRegion;
import com.metsci.glimpse.docking2.LandingRegions.LastInExistingTile;
import com.metsci.glimpse.docking2.TileKeyGenerator;

public class DockingExperiment
{

    public static void main( String[] args ) throws Exception
    {
        Theme.loadTheme( DockingExperiment.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
        UIManager.setLookAndFeel( new TinyLookAndFeel( ) );
        final DockingTheme dockingTheme = tinyLafDockingTheme( );


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


        Tile aTile = new Tile( dockingTheme );
        aTile.addView( aView, 0 );
        aTile.addView( bView, 1 );

        Tile bTile = new Tile( dockingTheme );
        bTile.addView( cView, 0 );
        bTile.addView( dView, 1 );

        Tile cTile = new Tile( dockingTheme );
        cTile.addView( eView, 0 );
        cTile.addView( fView, 1 );

        Tile dTile = new Tile( dockingTheme );
        dTile.addView( gView, 0 );
        dTile.addView( hView, 1 );



        TileKeyGenerator tileKeyGen = new TileKeyGenerator( )
        {
            long nextIdNum = 0;

            public TileKey newTileKey( )
            {
                long idNum = ( nextIdNum++ );
                return new TileKey( format( "Tile_%02d", idNum ) );
            }
        };

        final DockingPane2 aDockingPane = new DockingPane2( dockingTheme, tileKeyGen );
        TileKey aTileKey = aDockingPane.addInitialTile( aTile );
        TileKey bTileKey = aDockingPane.addNeighborTile( bTile, aTileKey, RIGHT, 0.3 );

        final DockingPane2 bDockingPane = new DockingPane2( dockingTheme, tileKeyGen );
        TileKey cTileKey = bDockingPane.addInitialTile( cTile );
        TileKey dTileKey = bDockingPane.addNeighborTile( dTile, cTileKey, Side.TOP, 0.8 );





        aTile.addDockingMouseAdapter( new DockingMouseAdapter2( aTile )
        {
            // XXX
            List<DockingPane2> dockingPanes = Arrays.asList( aDockingPane, bDockingPane );

            protected LandingRegion findLandingRegion( ViewKey draggedViewKey, MouseEvent ev )
            {
                Point pOnScreen = ev.getLocationOnScreen( );

                // XXX: Ordering? This tile's dockingPane should probably get first dibs
                for ( DockingPane2 dockingPane : dockingPanes )
                {
                    Point pInDock = new Point( pOnScreen );
                    convertPointFromScreen( pInDock, dockingPane );

                    if ( dockingPane.contains( pInDock ) )
                    {
                        Window window = getWindowAncestor( dockingPane );
                        if ( window != null ) window.toFront( );


                        // XXX
                        TileKey fromTileKey = aTileKey;

                        TileKey toTileKey = dockingPane.findTileAt( pInDock.x, pInDock.y );
                        Tile toTile = asdf;


                        // On own tile, which has no other views
                        //
                        if ( toTileKey != null && toTileKey.equals( fromTileKey ) && toTile.numViews( ) == 1 )
                        {
                            return null;
                        }


                        // On an existing tab
                        //
                        if ( toTileKey != null )
                        {
                            Point pInTile = new Point( pOnScreen );
                            convertPointFromScreen( pInTile, toTile );

                            int viewNum = toTile.viewNumForTabAt( pInTile.x, pInTile.y );
                            if ( 0 <= viewNum && viewNum < toTile.numViews( ) )
                            {
                                return new InExistingTile( dockingPane, toTileKey, viewNum );
                            }
                        }


                        // Near edge of docking-pane
                        //
                        {
                            int dLeft = pInDock.x;
                            int dRight = dockingPane.getWidth( ) - 1 - pInDock.x;
                            int dTop = pInDock.y;
                            int dBottom = dockingPane.getHeight( ) - 1 - pInDock.y;

                            IntAndIndex closest = minValueAndIndex( dLeft, dRight, dTop, dBottom );
                            if ( closest.value < 16 )
                            {
                                switch ( closest.index )
                                {
                                    case 0: return new EdgeOfDockingPane( dockingPane, LEFT );
                                    case 1: return new EdgeOfDockingPane( dockingPane, RIGHT );
                                    case 2: return new EdgeOfDockingPane( dockingPane, TOP );
                                    case 3: return new EdgeOfDockingPane( dockingPane, BOTTOM );
                                }
                            }
                        }


                        // Near edge of an existing tile
                        //
                        if ( toTile != null )
                        {
                            Point pInTile = new Point( pOnScreen );
                            convertPointFromScreen( pInTile, toTile );

                            int dLeft = pInTile.x;
                            int dRight = toTile.getWidth( ) - 1 - pInTile.x;
                            int dTop = pInTile.y;
                            int dBottom = toTile.getHeight( ) - 1 - pInTile.y;

                            IntAndIndex closest = minValueAndIndex( dLeft, dRight, dTop, dBottom );
                            if ( closest.value < 64 )
                            {
                                switch ( closest.index )
                                {
                                    case 0: return new BesideExistingTile( dockingPane, toTileKey, LEFT );
                                    case 1: return new BesideExistingTile( dockingPane, toTileKey, RIGHT );
                                    case 2: return new BesideExistingTile( dockingPane, toTileKey, TOP );
                                    case 3: return new BesideExistingTile( dockingPane, toTileKey, BOTTOM );
                                }
                            }
                        }


                        // In an existing tile, but not the one we started from, and not near the edge
                        //
                        if ( toTileKey != null && !toTileKey.equals( fromTileKey ) )
                        {
                            return new LastInExistingTile( dockingPane, toTileKey );
                        }


                        // Nowhere else to land, except back where we started
                        //
                        return null;
                    }
                }

                // XXX: New window

                return null;
            }
        } );





        swingRun( new Runnable( )
        {
            public void run( )
            {
                JFrame aFrame = new JFrame( );
                aFrame.setDefaultCloseOperation( EXIT_ON_CLOSE );
                aFrame.setContentPane( aDockingPane );
                aFrame.setPreferredSize( new Dimension( 1024, 768 ) );
                aFrame.pack( );
                aFrame.setLocationByPlatform( true );
                aFrame.setVisible( true );

                JFrame bFrame = new JFrame( );
                bFrame.setDefaultCloseOperation( EXIT_ON_CLOSE );
                bFrame.setContentPane( bDockingPane );
                bFrame.setPreferredSize( new Dimension( 1024, 768 ) );
                bFrame.pack( );
                bFrame.setLocationByPlatform( true );
                bFrame.setVisible( true );
            }
        });
    }

}
