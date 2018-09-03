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

import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.MiscUtils.convertPointToScreen;
import static com.metsci.glimpse.docking.MiscUtils.pointRelativeToAncestor;
import static com.metsci.glimpse.docking.group.DockingGroupUtils.pruneEmptyTile;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
import static java.awt.event.MouseEvent.BUTTON1;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.metsci.glimpse.docking.LandingRegions.LandingRegion;
import com.metsci.glimpse.docking.group.DockingGroupBase;

public class DockingMouseAdapter extends MouseAdapter
{

    protected final Tile tile;
    protected final DockingGroupBase group;
    protected final TileFactory tileFactory;

    protected boolean dragging = false;
    protected View draggedView = null;
    protected int draggedViewNum = -1;


    public DockingMouseAdapter( Tile tile, DockingGroupBase group, TileFactory tileFactory )
    {
        this.tile = tile;
        this.group = group;
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
            Point p = pointRelativeToAncestor( ev, this.tile );
            int viewNum = this.tile.viewNumForTabAt( p.x, p.y );
            if ( 0 <= viewNum && viewNum < this.tile.numViews( ) )
            {
                this.draggedView = this.tile.view( viewNum );
                this.draggedViewNum = viewNum;
                this.dragging = false;

                // double-click to toggle maximize view
                if ( ev.getClickCount( ) == 2 )
                {
                    MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, this.tile );
                    if ( docker.getMaximizedLeaf( ) == this.tile )
                    {
                        docker.unmaximizeLeaf( );
                    }
                    else
                    {
                        docker.maximizeLeaf( this.tile );
                    }
                }
            }
        }
    }

    @Override
    public void mouseDragged( MouseEvent ev )
    {
        if ( this.draggedView != null )
        {
            if ( !this.dragging )
            {
                this.group.onDragStarting( this.tile );
            }

            this.dragging = true;

            LandingRegion region = this.group.findLandingRegion( this.tile, this.draggedViewNum, ev.getLocationOnScreen( ) );
            if ( region != null )
            {
                this.group.setLandingIndicator( region.getIndicator( ) );
            }
            else
            {
                Point pOnScreen = convertPointToScreen( this.tile, new Point( 0, 0 ) );
                this.group.setLandingIndicator( new Rectangle( pOnScreen.x, pOnScreen.y, this.tile.getWidth( ), this.tile.getHeight( ) ) );
            }
        }
    }

    @Override
    public void mouseReleased( MouseEvent ev )
    {
        this.mouseReleased( ev.getButton( ), ev.getLocationOnScreen( ) );
    }

    public void mouseReleased( int button, Point locationOnScreen )
    {
        if ( button == BUTTON1 && this.dragging )
        {
            LandingRegion region = this.group.findLandingRegion( this.tile, this.draggedViewNum, locationOnScreen );
            if ( region != null )
            {
                this.tile.removeView( this.draggedView );
                region.placeView( this.draggedView, this.tileFactory );
                pruneEmptyTile( this.tile );
            }

            this.dragging = false;
            this.draggedView = null;
            this.draggedViewNum = -1;
            this.group.setLandingIndicator( null );
        }
    }

}
