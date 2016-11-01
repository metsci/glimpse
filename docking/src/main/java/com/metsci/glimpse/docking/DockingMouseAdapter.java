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

import static com.metsci.glimpse.docking.DockingGroup.pruneEmptyTileAndFrame;
import static com.metsci.glimpse.docking.LandingRegions.findLandingRegion;
import static com.metsci.glimpse.docking.MiscUtils.convertPointToScreen;
import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.MiscUtils.pointRelativeToAncestor;
import static com.metsci.glimpse.docking.MiscUtils.reversed;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
import static java.awt.event.MouseEvent.BUTTON1;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.metsci.glimpse.docking.LandingRegions.LandingRegion;
import com.metsci.glimpse.docking.TileFactories.TileFactory;

public class DockingMouseAdapter extends MouseAdapter
{

    protected final Tile tile;
    protected final DockingGroup dockingGroup;
    protected final TileFactory tileFactory;

    protected boolean dragging = false;
    protected View draggedView = null;
    protected int draggedViewNum = -1;

    public DockingMouseAdapter( Tile tile, DockingGroup dockingGroup, TileFactory tileFactory )
    {
        this.tile = tile;
        this.dockingGroup = dockingGroup;
        this.tileFactory = tileFactory;

        this.dragging = false;
        this.draggedView = null;
        this.draggedViewNum = -1;
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
                this.draggedViewNum = viewNum;
                this.dragging = false;

                // double-click to toggle maximize view
                if ( ev.getClickCount( ) == 2 )
                {
                    MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, tile );
                    if ( docker.getMaximizedLeaf( ) == tile )
                    {
                        docker.unmaximizeLeaf( );
                    }
                    else
                    {
                        docker.maximizeLeaf( tile );
                    }
                }
            }
        }
    }

    @Override
    public void mouseDragged( MouseEvent ev )
    {
        if ( draggedView != null )
        {
            if ( !dragging )
            {
                DockingFrame frame = getAncestorOfClass( DockingFrame.class, tile );
                dockingGroup.bringFrameToFront( frame );

                // Cause the system's window-manager to show our frames in an order
                // consistent with the way we're going to iterate through them to
                // choose the landing-region.
                //
                // This is necessary because Java does not provide a way to ask the
                // window-manager which window is on top at a given spot. It might be
                // possible to kludge a solution together using Robot.getPixelColor
                // ... but it would be ugly at best.
                //
                for ( DockingFrame f : reversed( dockingGroup.frames ) )
                {
                    f.toFront( );
                }
            }

            this.dragging = true;

            LandingRegion region = findLandingRegion( dockingGroup, tile, draggedViewNum, ev.getLocationOnScreen( ) );
            if ( region != null )
            {
                dockingGroup.setLandingIndicator( region.getIndicator( ) );
            }
            else
            {
                Point pOnScreen = convertPointToScreen( tile, new Point( 0, 0 ) );
                dockingGroup.setLandingIndicator( new Rectangle( pOnScreen.x, pOnScreen.y, tile.getWidth( ), tile.getHeight( ) ) );
            }
        }
    }

    @Override
    public void mouseReleased( MouseEvent ev )
    {
        mouseReleased( ev.getButton( ), ev.getLocationOnScreen( ) );
    }

    public void mouseReleased( int button, Point locationOnScreen )
    {
        if ( button == BUTTON1 && dragging )
        {
            LandingRegion landingRegion = findLandingRegion( dockingGroup, tile, draggedViewNum, locationOnScreen );
            if ( landingRegion != null )
            {
                tile.removeView( draggedView );
                landingRegion.placeView( draggedView, tileFactory );
                pruneEmptyTileAndFrame( dockingGroup, tile );
            }

            this.dragging = false;
            this.draggedView = null;
            this.draggedViewNum = -1;
            dockingGroup.setLandingIndicator( null );
        }
    }

}
