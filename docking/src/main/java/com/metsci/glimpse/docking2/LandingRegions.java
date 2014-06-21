package com.metsci.glimpse.docking2;

import java.awt.Rectangle;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.TileKey;
import com.metsci.glimpse.docking.View;

public class LandingRegions
{


    public static interface LandingRegion
    {
        Rectangle getIndicator( );
        void placeView( View view );
    }


    public static class EdgeOfDockingPane implements LandingRegion
    {
        public final DockingPane2 dockingPane;
        public final Side edgeOfPane;

        public EdgeOfDockingPane( DockingPane2 dockingPane, Side edgeOfPane )
        {
            this.dockingPane = dockingPane;
            this.edgeOfPane = edgeOfPane;
        }

        @Override
        public Rectangle getIndicator( )
        {
            int w = dockingPane.getWidth( );
            int h = dockingPane.getHeight( );

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
        public void placeView( View view )
        {
            TileKey newTileKey = dockingPane.addEdgeTile( c, edgeOfPane );
            addView( view, newTileKey );
        }
    }


    public static class BesideExistingTile implements LandingRegion
    {
        public final DockingPane2 dockingPane;
        public final TileKey neighborKey;
        public final Side sideOfNeighbor;

        public BesideExistingTile( DockingPane2 dockingPane, TileKey neighborKey, Side sideOfNeighbor )
        {
            this.dockingPane = dockingPane;
            this.neighborKey = neighborKey;
            this.sideOfNeighbor = sideOfNeighbor;
        }

        @Override
        public Rectangle getIndicator( )
        {
            Tile neighbor = tile( neighborKey );
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
        public void placeView( View view )
        {
            TileKey newTileKey = addNewTile( neighborKey, sideOfNeighbor );
            addView( view, newTileKey );
        }
    }


    public static class InExistingTile implements LandingRegion
    {
        public final DockingPane2 dockingPane;
        public final TileKey tileKey;
        public final int viewNum;

        public InExistingTile( DockingPane2 dockingPane, TileKey tileKey, int viewNum )
        {
            this.dockingPane = dockingPane;
            this.tileKey = tileKey;
            this.viewNum = viewNum;
        }

        @Override
        public Rectangle getIndicator( )
        {
            // XXX: To screen coords

            Tile tile = tile( tileKey );
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
        public void placeView( View view )
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
            addView( view, tileKey, viewNum );
            tile( tileKey ).selectView( view );
        }
    }


    public static class LastInExistingTile implements LandingRegion
    {
        public final DockingPane2 dockingPane;
        public final TileKey tileKey;

        public LastInExistingTile( DockingPane2 dockingPane, TileKey tileKey )
        {
            this.dockingPane = dockingPane;
            this.tileKey = tileKey;
        }

        @Override
        public Rectangle getIndicator( )
        {
            // XXX: To screen coords

            return tile( tileKey ).getBounds( );
        }

        @Override
        public void placeView( View view )
        {
            addView( view, tileKey );
            tile( tileKey ).selectView( view );
        }
    }

}
