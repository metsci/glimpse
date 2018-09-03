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
package com.metsci.glimpse.docking.group.dialog;

import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.LandingRegions.landingInExistingDocker;
import static com.metsci.glimpse.docking.MiscUtils.containsScreenPoint;
import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.RIGHT;
import static com.metsci.glimpse.docking.Side.TOP;
import static com.metsci.glimpse.docking.group.ArrangementUtils.buildSubtreeViewIdsMap;
import static com.metsci.glimpse.docking.group.ArrangementUtils.findArrNodeParent;
import static com.metsci.glimpse.docking.group.ArrangementUtils.findArrTileContaining;
import static com.metsci.glimpse.docking.group.ArrangementUtils.findFrameArrContaining;
import static com.metsci.glimpse.docking.group.ArrangementUtils.findLargestArrTile;
import static com.metsci.glimpse.docking.group.ViewPlacementUtils.chooseViewNum;
import static com.metsci.glimpse.docking.group.ViewPlacementUtils.findSimilarArrNode;
import static com.metsci.glimpse.docking.group.ViewPlacementUtils.findSimilarArrTile;

import java.awt.Component;
import java.awt.Point;
import java.util.Map;
import java.util.Set;

import com.metsci.glimpse.docking.DockingFrameCloseOperation;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.DockingWindow;
import com.metsci.glimpse.docking.LandingRegions.LandingRegion;
import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.group.DockingGroupBase;
import com.metsci.glimpse.docking.group.ViewDestination;
import com.metsci.glimpse.docking.group.ViewPlacer;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingGroupDialog extends DockingGroupBase
{

    public DockingGroupDialog( DockingFrameCloseOperation frameCloseOperation )
    {
        this( frameCloseOperation, defaultDockingTheme( ) );
    }

    public DockingGroupDialog( DockingFrameCloseOperation frameCloseOperation, DockingTheme theme )
    {
        super( frameCloseOperation, theme );
    }

    public DockingDialog ensureDialog( )
    {
        if ( !this.windows.isEmpty( ) )
        {
            return ( ( DockingDialog ) this.windows.get( 0 ) );
        }
        else
        {
            return this.addWindow( new DockingDialog( this.createDocker( ) ) );
        }
    }

    @Override
    protected <W extends DockingWindow> W addWindow( W window )
    {
        if ( !( window instanceof DockingDialog ) )
        {
            throw new RuntimeException( "Unexpected window type: " + window.getClass( ).getName( ) );
        }
        else if ( !this.windows.isEmpty( ) )
        {
            throw new RuntimeException( "Singleton dialog already exists" );
        }
        else
        {
            return super.addWindow( window );
        }
    }

    @Override
    protected ViewPlacerDialog<ViewDestination> createViewPlacer( Map<DockerArrangementNode,Component> existingComponents, View newView )
    {
        return new ViewPlacerDialogGroup( this, existingComponents, newView );
    }

    @Override
    protected ViewPlacerDialog<Void> createViewPlacer( GroupArrangement groupArr, String newViewId )
    {
        return new ViewPlacerDialogArr( groupArr, newViewId );
    }

    @Override
    protected <T> T placeView( GroupArrangement existingArr, GroupArrangement planArr, String viewId, ViewPlacer<T> viewPlacer0 )
    {
        // Cast is compatible with the return types of both createViewPlacer() methods
        ViewPlacerDialog<T> viewPlacer = ( ViewPlacerDialog<T> ) viewPlacer0;

        Map<DockerArrangementNode,Set<String>> existingSubtreeViewIds = buildSubtreeViewIdsMap( existingArr );
        Map<DockerArrangementNode,Set<String>> planSubtreeViewIds = buildSubtreeViewIdsMap( planArr );

        FrameArrangement planFrame = findFrameArrContaining( planArr, viewId );
        if ( planFrame != null )
        {
            // Add to an existing tile that is similar to the planned tile
            DockerArrangementTile planTile = findArrTileContaining( planFrame.dockerArr, viewId );
            Set<String> planTileViewIds = planSubtreeViewIds.get( planTile );
            DockerArrangementTile existingTile = findSimilarArrTile( existingSubtreeViewIds, planTileViewIds );
            if ( existingTile != null )
            {
                int viewNum = chooseViewNum( planTile.viewIds, existingTile.viewIds, viewId );
                return viewPlacer.addToTile( existingTile, viewNum );
            }

            // Create a new tile, beside an existing neighbor that is similar to the planned neighbor
            // First look for a good "sibling," then for a good "uncle," and so on up the tree
            DockerArrangementNode planNode = planTile;
            while ( true )
            {
                DockerArrangementSplit planParent = findArrNodeParent( planFrame.dockerArr, planNode );
                if ( planParent == null )
                {
                    break;
                }

                boolean newIsChildA = ( planNode == planParent.childA );
                DockerArrangementNode planNeighbor = ( newIsChildA ? planParent.childB : planParent.childA );
                Set<String> planNeighborViewIds = planSubtreeViewIds.get( planNeighbor );
                DockerArrangementNode existingNeighbor = findSimilarArrNode( existingSubtreeViewIds, planNeighborViewIds );
                if ( existingNeighbor != null )
                {
                    Side sideOfNeighbor = ( planParent.arrangeVertically ? ( newIsChildA ? TOP : BOTTOM ) : ( newIsChildA ? LEFT : RIGHT ) );
                    double extentFrac = ( newIsChildA ? planParent.splitFrac : 1.0 - planParent.splitFrac );
                    return viewPlacer.addBesideNeighbor( planTile, existingNeighbor, sideOfNeighbor, extentFrac );
                }

                // Go one level up the tree and try again
                planNode = planParent;
            }

            // Create a new frame, with size and position from the planned arrangement
            return viewPlacer.addInNewFrame( planFrame, planTile );
        }

        // First fallback is in the largest tile
        DockerArrangementTile existingLargest = findLargestArrTile( existingArr );
        if ( existingLargest != null )
        {
            int viewNum = existingLargest.viewIds.size( );
            return viewPlacer.addToTile( existingLargest, viewNum );
        }

        // Final fallback is in a new window
        return viewPlacer.addInNewFallbackFrame( );
    }

    @Override
    public LandingRegion findLandingRegion( Tile fromTile, int fromViewNum, Point pOnScreen )
    {
        for ( DockingWindow window : this.windows )
        {
            MultiSplitPane docker = window.docker( );
            if ( containsScreenPoint( docker, pOnScreen ) )
            {
                return landingInExistingDocker( docker, fromTile, fromViewNum, pOnScreen );
            }
        }
        return null;
    }

}
