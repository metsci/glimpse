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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.MiscUtils.convertPointFromScreen;
import static com.metsci.glimpse.docking.MiscUtils.minValueAndIndex;
import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.RIGHT;
import static com.metsci.glimpse.docking.Side.TOP;
import static javax.swing.SwingUtilities.convertPointToScreen;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import com.metsci.glimpse.docking.MiscUtils.IntAndIndex;

public class LandingRegions
{

    public static LandingRegion landingInExistingDocker( MultiSplitPane docker, Tile fromTile, int fromViewNum, Point pOnScreen )
    {
        Point pInDocker = convertPointFromScreen( pOnScreen, docker );

        if ( !docker.contains( pInDocker ) )
        {
            // Not inside this docking-pane
            return null;
        }

        Component toLeaf = docker.findLeafAt( pInDocker.x, pInDocker.y );

        // On own tile, which has no other views
        if ( toLeaf == fromTile && fromTile.numViews( ) == 1 )
        {
            return null;
        }

        // On an existing tab
        if ( toLeaf instanceof Tile )
        {
            Tile toTile = ( Tile ) toLeaf;
            Point pInTile = convertPointFromScreen( pOnScreen, toTile );
            int viewNum = toTile.viewNumForTabAt( pInTile.x, pInTile.y );
            if ( 0 <= viewNum && viewNum < toTile.numViews( ) )
            {
                return new InExistingTile( toTile, viewNum );
            }
        }

        // In an empty docking-pane
        if ( docker.numLeaves( ) == 0 )
        {
            return new InEmptyDockingPane( docker );
        }

        // Near edge of docking-pane
        if ( docker.getMaximizedLeaf( ) == null )
        {
            int dLeft = pInDocker.x;
            int dRight = docker.getWidth( ) - 1 - pInDocker.x;
            int dTop = pInDocker.y;
            int dBottom = docker.getHeight( ) - 1 - pInDocker.y;

            IntAndIndex closest = minValueAndIndex( dLeft, dRight, dTop, dBottom );
            if ( closest.value < 16 )
            {
                switch ( closest.index )
                {
                    case 0: return new EdgeOfDockingPane( docker, LEFT );
                    case 1: return new EdgeOfDockingPane( docker, RIGHT );
                    case 2: return new EdgeOfDockingPane( docker, TOP );
                    case 3: return new EdgeOfDockingPane( docker, BOTTOM );
                }
            }
        }

        // Near edge of an existing leaf
        if ( docker.getMaximizedLeaf( ) == null && toLeaf != null )
        {
            Point pInLeaf = convertPointFromScreen( pOnScreen, toLeaf );
            int dLeft = pInLeaf.x;
            int dRight = toLeaf.getWidth( ) - 1 - pInLeaf.x;
            int dTop = pInLeaf.y;
            int dBottom = toLeaf.getHeight( ) - 1 - pInLeaf.y;

            IntAndIndex closest = minValueAndIndex( dLeft, dRight, dTop, dBottom );
            if ( closest.value < 64 )
            {
                switch ( closest.index )
                {
                    case 0: return new BesideExistingTile( docker, toLeaf, LEFT );
                    case 1: return new BesideExistingTile( docker, toLeaf, RIGHT );
                    case 2: return new BesideExistingTile( docker, toLeaf, TOP );
                    case 3: return new BesideExistingTile( docker, toLeaf, BOTTOM );
                }
            }
        }

        // In an existing tile, but not the one we started from, and not near the edge
        if ( toLeaf != fromTile && toLeaf instanceof Tile )
        {
            Tile toTile = ( Tile ) toLeaf;
            return new LastInExistingTile( toTile );
        }

        // Nowhere else to land, except back where we started
        return null;
    }

    public static interface LandingRegion
    {
        Rectangle getIndicator( );

        void placeView( View view, TileFactory tileFactory );
    }

    public static class InEmptyDockingPane implements LandingRegion
    {
        public final MultiSplitPane docker;

        public InEmptyDockingPane( MultiSplitPane docker )
        {
            this.docker = docker;
        }

        @Override
        public Rectangle getIndicator( )
        {
            Point pOnScreen = new Point( 0, 0 );
            convertPointToScreen( pOnScreen, docker );
            return new Rectangle( pOnScreen.x, pOnScreen.y, docker.getWidth( ), docker.getHeight( ) );
        }

        @Override
        public void placeView( View view, TileFactory tileFactory )
        {
            Tile tile = tileFactory.newTile( );
            tile.addView( view, 0 );
            docker.addInitialLeaf( tile );
        }
    }

    public static class EdgeOfDockingPane implements LandingRegion
    {
        public final MultiSplitPane docker;
        public final Side edgeOfPane;

        public EdgeOfDockingPane( MultiSplitPane docker, Side edgeOfPane )
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
                case LEFT:   return new Rectangle( x, y, 64, h );
                case RIGHT:  return new Rectangle( x + w - 1 - 64, y, 64, h );
                case TOP:    return new Rectangle( x, y, w, 64 );
                case BOTTOM: return new Rectangle( x, y + h - 1 - 64, w, 64 );
                default: return null;
            }
        }

        @Override
        public void placeView( View view, TileFactory tileFactory )
        {
            Tile tile = tileFactory.newTile( );
            tile.addView( view, 0 );
            docker.addEdgeLeaf( tile, edgeOfPane );
        }
    }

    public static class BesideExistingTile implements LandingRegion
    {
        public final MultiSplitPane docker;
        public final Component neighbor;
        public final Side sideOfNeighbor;

        public BesideExistingTile( MultiSplitPane docker, Component neighbor, Side sideOfNeighbor )
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
                case LEFT:   return new Rectangle( x, y, w / 2, h );
                case RIGHT:  return new Rectangle( x + w - w / 2, y, w / 2, h );
                case TOP:    return new Rectangle( x, y, w, h / 2 );
                case BOTTOM: return new Rectangle( x, y + h - h / 2, w, h / 2 );
                default: return null;
            }
        }

        @Override
        public void placeView( View view, TileFactory tileFactory )
        {
            Tile tile = tileFactory.newTile( );
            tile.addView( view, 0 );
            docker.addNeighborLeaf( tile, neighbor, sideOfNeighbor );
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
            Point pOnScreen = new Point( 0, 0 );
            convertPointToScreen( pOnScreen, tile );
            return new Rectangle( pOnScreen.x, pOnScreen.y, tile.getWidth( ), tile.getHeight( ) );
        }

        @Override
        public void placeView( View view, TileFactory tileFactory )
        {
            tile.addView( view, tile.numViews( ) );
            tile.selectView( view );
        }
    }

}
