package com.metsci.glimpse.docking2;

import static javax.swing.JFrame.EXIT_ON_CLOSE;
import static javax.swing.SwingUtilities.convertPointToScreen;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JFrame;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.View;

public class LandingRegions
{


    public static interface LandingRegion
    {
        Rectangle getIndicator( );
        void placeView( View view, DockingPaneGroup dockerGroup, TileFactory tileFactory );
    }


    public static class StayInExistingTile implements LandingRegion
    {
        public final Tile tile;

        public StayInExistingTile( Tile tile )
        {
            this.tile = tile;
        }

        @Override
        public Rectangle getIndicator( )
        {
            Point pOnScreen = new Point( 0, 0 );
            convertPointToScreen( pOnScreen, tile );
            return new Rectangle( pOnScreen.x, pOnScreen.y, tile.getWidth( ), tile.getHeight( ) );
        }

        @Override
        public void placeView( View view, DockingPaneGroup dockerGroup, TileFactory tileFactory )
        {
            // Do nothing
        }
    }


    public static class EdgeOfDockingPane implements LandingRegion
    {
        public final DockingPane docker;
        public final Side edgeOfPane;

        public EdgeOfDockingPane( DockingPane docker, Side edgeOfPane )
        {
            this.docker = docker;
            this.edgeOfPane = edgeOfPane;
        }

        @Override
        public Rectangle getIndicator( )
        {
            Point pOnScreen = new Point( 0, 0 );
            convertPointToScreen( pOnScreen, docker );
            int x = pOnScreen.x;
            int y = pOnScreen.y;

            int w = docker.getWidth( );
            int h = docker.getHeight( );

            switch ( edgeOfPane )
            {
                case LEFT:   return new Rectangle( x,        y,         64,  h );
                case RIGHT:  return new Rectangle( x+w-1-64, y,         64,  h );
                case TOP:    return new Rectangle( x,        y,         w,  64 );
                case BOTTOM: return new Rectangle( x,        y+h-1-64,  w,  64 );

                default: return null;
            }
        }

        @Override
        public void placeView( View view, DockingPaneGroup dockerGroup, TileFactory tileFactory )
        {
            Tile tile = tileFactory.newTile( );
            tile.addView( view, 0 );
            docker.addEdgeTile( tile, edgeOfPane );
        }
    }


    public static class BesideExistingTile implements LandingRegion
    {
        public final DockingPane docker;
        public final Component neighbor;
        public final Side sideOfNeighbor;

        public BesideExistingTile( DockingPane docker, Component neighbor, Side sideOfNeighbor )
        {
            this.docker = docker;
            this.neighbor = neighbor;
            this.sideOfNeighbor = sideOfNeighbor;
        }

        @Override
        public Rectangle getIndicator( )
        {
            Point pOnScreen = new Point( 0, 0 );
            convertPointToScreen( pOnScreen, neighbor );
            int x = pOnScreen.x;
            int y = pOnScreen.y;

            int w = neighbor.getWidth( );
            int h = neighbor.getHeight( );

            switch ( sideOfNeighbor )
            {
                case LEFT:   return new Rectangle( x,       y,       w/2, h );
                case RIGHT:  return new Rectangle( x+w-w/2, y,       w/2, h );
                case TOP:    return new Rectangle( x,       y,       w,   h/2 );
                case BOTTOM: return new Rectangle( x,       y+h-h/2, w,   h/2 );

                default: return null;
            }
        }

        @Override
        public void placeView( View view, DockingPaneGroup dockerGroup, TileFactory tileFactory )
        {
            Tile tile = tileFactory.newTile( );
            tile.addView( view, 0 );
            docker.addNeighborTile( tile, neighbor, sideOfNeighbor );
        }
    }


    public static class InExistingTile implements LandingRegion
    {
        public final Tile tile;
        public final int viewNum;

        public InExistingTile( Tile tile, int viewNum )
        {
            this.tile = tile;
            this.viewNum = viewNum;
        }

        @Override
        public Rectangle getIndicator( )
        {
            Rectangle tabBounds = tile.viewTabBounds( viewNum );
            if ( tabBounds == null )
            {
                Point pOnScreen = new Point( 0, 0 );
                convertPointToScreen( pOnScreen, tile );
                return new Rectangle( pOnScreen.x, pOnScreen.y, tile.getWidth( ), tile.getHeight( ) );
            }
            else
            {
                Point pOnScreen = new Point( tabBounds.x, tabBounds.y );
                convertPointToScreen( pOnScreen, tile );
                return new Rectangle( pOnScreen.x, pOnScreen.y, tabBounds.width, tabBounds.height );
            }
        }

        @Override
        public void placeView( View view, DockingPaneGroup dockerGroup, TileFactory tileFactory )
        {
            // If you think about it, you'll wonder why we always insert at viewNum --
            // if we're moving a view a few tabs to the right, then the view's original
            // tab will get removed first, which will change the tab numbers!
            //
            // This approach places the tab to the right of the indicated tab instead of
            // its left, which is a little weird. However, the indicated tab has shifted
            // to the left -- so if we placed the tab to the left of the indicated tab,
            // it would not end up under the mouse, which feels pretty unsettling.
            //
            tile.addView( view, viewNum );
            tile.selectView( view );
        }
    }


    public static class LastInExistingTile implements LandingRegion
    {
        public final Tile tile;

        public LastInExistingTile( Tile tile )
        {
            this.tile = tile;
        }

        @Override
        public Rectangle getIndicator( )
        {
            Point pOnScreen = new Point( 0, 0 );
            convertPointToScreen( pOnScreen, tile );
            return new Rectangle( pOnScreen.x, pOnScreen.y, tile.getWidth( ), tile.getHeight( ) );
        }

        @Override
        public void placeView( View view, DockingPaneGroup dockerGroup, TileFactory tileFactory )
        {
            tile.addView( view, tile.numViews( ) );
            tile.selectView( view );
        }
    }


    public static class InNewWindow implements LandingRegion
    {
        public final int xOnScreen;
        public final int yOnScreen;
        public final int width;
        public final int height;

        public InNewWindow( int xOnScreen, int yOnScreen, int width, int height )
        {
            this.xOnScreen = xOnScreen;
            this.yOnScreen = yOnScreen;
            this.width = width;
            this.height = height;
        }

        @Override
        public Rectangle getIndicator( )
        {
            return new Rectangle( xOnScreen, yOnScreen, width, height );
        }

        @Override
        public void placeView( View view, DockingPaneGroup dockerGroup, TileFactory tileFactory )
        {
            Tile tile = tileFactory.newTile( );
            tile.addView( view, 0 );

            DockingPane docker = dockerGroup.addNewDocker( );
            docker.addInitialTile( tile );

            JFrame frame = new JFrame( );

            // XXX
            frame.setDefaultCloseOperation( EXIT_ON_CLOSE );

            frame.setContentPane( docker );
            frame.setBounds( xOnScreen, yOnScreen, width, height );
            frame.setVisible( true );
        }
    }

}
