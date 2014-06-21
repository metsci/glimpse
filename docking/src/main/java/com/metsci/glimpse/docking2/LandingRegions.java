package com.metsci.glimpse.docking2;

import java.awt.Component;
import java.awt.Rectangle;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.View;

public class LandingRegions
{


    public static interface LandingRegion
    {
        Rectangle getIndicator( );
        void placeView( View view, TileFactory tileFactory );
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
            int w = docker.getWidth( );
            int h = docker.getHeight( );

            // XXX: To screen coords
            switch ( edgeOfPane )
            {
                case LEFT:   return new Rectangle( 0,       0,       64,  h );
                case RIGHT:  return new Rectangle( w-1-64,  0,       64,  h );
                case TOP:    return new Rectangle( 0,       0,       w,  64 );
                case BOTTOM: return new Rectangle( 0,       h-1-64,  w,  64 );

                default: return null;
            }
        }

        @Override
        public void placeView( View view, TileFactory tileFactory )
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
            int x = neighbor.getX( );
            int y = neighbor.getY( );
            int w = neighbor.getWidth( );
            int h = neighbor.getHeight( );

            // XXX: To screen coords
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
        public void placeView( View view, TileFactory tileFactory )
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
            // XXX: To screen coords

            Rectangle tabBounds = tile.viewTabBounds( viewNum );
            if ( tabBounds == null )
            {
                return tile.getBounds( );
            }
            else
            {
                return new Rectangle( tile.getX( )+tabBounds.x, tile.getY( )+tabBounds.y, tabBounds.width, tabBounds.height );
            }
        }

        @Override
        public void placeView( View view, TileFactory tileFactory )
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
            // XXX: To screen coords

            return tile.getBounds( );
        }

        @Override
        public void placeView( View view, TileFactory tileFactory )
        {
            tile.addView( view, tile.numViews( ) );
            tile.selectView( view );
        }
    }

}
