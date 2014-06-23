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

import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.MiscUtils.minValueAndIndex;
import static com.metsci.glimpse.docking.MiscUtils.pointRelativeToAncestor;
import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.RIGHT;
import static com.metsci.glimpse.docking.Side.TOP;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
import static java.awt.event.MouseEvent.BUTTON1;
import static javax.swing.SwingUtilities.convertPointFromScreen;
import static javax.swing.SwingUtilities.getWindowAncestor;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.docking.LandingRegions.BesideExistingTile;
import com.metsci.glimpse.docking.LandingRegions.EdgeOfDockingPane;
import com.metsci.glimpse.docking.LandingRegions.InExistingTile;
import com.metsci.glimpse.docking.LandingRegions.InNewWindow;
import com.metsci.glimpse.docking.LandingRegions.LandingRegion;
import com.metsci.glimpse.docking.LandingRegions.LastInExistingTile;
import com.metsci.glimpse.docking.LandingRegions.StayInExistingTile;
import com.metsci.glimpse.docking.MiscUtils.IntAndIndex;
import com.metsci.glimpse.docking.TileFactories.TileFactory;

public class DockingMouseAdapter extends MouseAdapter
{

    protected final Tile tile;
    protected final DockingPaneGroup dockerGroup;
    protected final TileFactory tileFactory;

    protected boolean dragging = false;
    protected View draggedView = null;


    public DockingMouseAdapter( Tile tile, DockingPaneGroup dockerGroup, TileFactory tileFactory )
    {
        this.tile = tile;
        this.dockerGroup = dockerGroup;
        this.tileFactory = tileFactory;

        this.dragging = false;
        this.draggedView = null;
    }

    @Override
    public void mousePressed( MouseEvent ev )
    {
        int buttonsDown = ( ev.getModifiersEx( ) & ( BUTTON1_DOWN_MASK | BUTTON2_DOWN_MASK | BUTTON3_DOWN_MASK ) );
        if ( buttonsDown == BUTTON1_DOWN_MASK )
        {
            Point p = pointRelativeToAncestor( ev, tile );
            int viewNum = tile.viewNumForTabAt( p.x, p.y );
            if ( 0 <= viewNum && viewNum < tile.numViews( ) )
            {
                this.draggedView = tile.view( viewNum );
                this.dragging = false;
            }
        }
    }

    @Override
    public void mouseDragged( MouseEvent ev )
    {
        if ( draggedView != null )
        {
            this.dragging = true;
            LandingRegion region = findLandingRegion( dockerGroup, tile, ev );
            dockerGroup.setLandingIndicator( region.getIndicator( ) );
        }
    }

    @Override
    public void mouseReleased( MouseEvent ev )
    {
        if ( ev.getButton( ) == BUTTON1 && dragging )
        {
            LandingRegion landingRegion = findLandingRegion( dockerGroup, tile, ev );
            if ( landingRegion != null )
            {
                // This will remove empty tiles before placing the view.
                //
                // This would cause problems if a view were placed relative to its
                // own tile, and there were no other views in the tile. We get away
                // with it, though, because we don't allow the dragged view to land
                // beside its own tile, unless the tile contains other views.
                //
                tile.removeView( draggedView );
                landingRegion.placeView( draggedView, dockerGroup, tileFactory );

                if ( tile.numViews( ) == 0 )
                {
                    DockingPane docker = getAncestorOfClass( DockingPane.class, tile );
                    docker.removeTile( tile );
                }
            }

            this.dragging = false;
            this.draggedView = null;
            dockerGroup.setLandingIndicator( null );
        }
    }

    protected static LandingRegion findLandingRegion( DockingPaneGroup dockerGroup, Tile fromTile, MouseEvent ev )
    {
        List<DockingPane> dockersInOrder = new ArrayList<>( );
        DockingPane fromDocker = getAncestorOfClass( DockingPane.class, fromTile );
        dockersInOrder.add( fromDocker );
        for ( DockingPane docker : dockerGroup.dockers )
        {
            if ( docker != fromDocker )
            {
                dockersInOrder.add( docker );
            }
        }

        Point pOnScreen = ev.getLocationOnScreen( );

        for ( DockingPane docker : dockersInOrder )
        {
            Point pInDocker = new Point( pOnScreen );
            convertPointFromScreen( pInDocker, docker );

            if ( docker.contains( pInDocker ) )
            {
                Window window = getWindowAncestor( docker );
                if ( window != null ) window.toFront( );


                Component toComp = docker.findTileAt( pInDocker.x, pInDocker.y );


                // On own tile, which has no other views
                //
                if ( toComp == fromTile && fromTile.numViews( ) == 1 )
                {
                    return new StayInExistingTile( fromTile );
                }


                // On an existing tab
                //
                if ( toComp instanceof Tile )
                {
                    Tile toTile = ( Tile ) toComp;

                    Point pInTile = new Point( pOnScreen );
                    convertPointFromScreen( pInTile, toTile );

                    int viewNum = toTile.viewNumForTabAt( pInTile.x, pInTile.y );
                    if ( 0 <= viewNum && viewNum < toTile.numViews( ) )
                    {
                        return new InExistingTile( toTile, viewNum );
                    }
                }


                // Near edge of docking-pane
                //
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


                // Near edge of an existing tile
                //
                if ( toComp != null )
                {
                    Point pInComp = new Point( pOnScreen );
                    convertPointFromScreen( pInComp, toComp );

                    int dLeft = pInComp.x;
                    int dRight = toComp.getWidth( ) - 1 - pInComp.x;
                    int dTop = pInComp.y;
                    int dBottom = toComp.getHeight( ) - 1 - pInComp.y;

                    IntAndIndex closest = minValueAndIndex( dLeft, dRight, dTop, dBottom );
                    if ( closest.value < 64 )
                    {
                        switch ( closest.index )
                        {
                            case 0: return new BesideExistingTile( docker, toComp, LEFT );
                            case 1: return new BesideExistingTile( docker, toComp, RIGHT );
                            case 2: return new BesideExistingTile( docker, toComp, TOP );
                            case 3: return new BesideExistingTile( docker, toComp, BOTTOM );
                        }
                    }
                }


                // In an existing tile, but not the one we started from, and not near the edge
                //
                if ( toComp != fromTile && toComp instanceof Tile )
                {
                    Tile toTile = ( Tile ) toComp;
                    return new LastInExistingTile( toTile );
                }


                // Nowhere else to land, except back where we started
                //
                return new StayInExistingTile( fromTile );
            }
        }


        // Outside all dockers in dockerGroup
        //
        return new InNewWindow( pOnScreen.x, pOnScreen.y, fromTile.getWidth( ), fromTile.getHeight( ) );
    }

}
