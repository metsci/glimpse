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

import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.DockingUtils.getFrameExtendedState;
import static com.metsci.glimpse.docking.MiscUtils.intersection;
import static com.metsci.glimpse.docking.MiscUtils.reversed;
import static com.metsci.glimpse.docking.MiscUtils.union;
import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.RIGHT;
import static com.metsci.glimpse.docking.Side.TOP;
import static com.metsci.glimpse.docking.SplitPane.computeChildSizes;
import static java.awt.Frame.MAXIMIZED_HORIZ;
import static java.awt.Frame.MAXIMIZED_VERT;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.metsci.glimpse.docking.frame.DockingFrame;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingGroupUtils
{

    /**
     * Restore selected views in newly created tiles.
     */
    public static void restoreSelectedViewsInNewTiles( Collection<? extends ViewDestination> viewDestinations )
    {
        for ( ViewDestination dest : viewDestinations )
        {
            if ( dest.createdTile != null && dest.planTile != null )
            {
                View view = dest.createdTile.view( dest.planTile.selectedViewId );
                dest.createdTile.selectView( view );
            }
        }
    }

    /**
     * Restore maximized tiles in newly created dockers.
     */
    public static void restoreMaximizedTilesInNewDockers( Collection<? extends ViewDestination> viewDestinations )
    {
        Map<MultiSplitPane,Tile> maximizedTiles = new LinkedHashMap<>( );
        for ( ViewDestination dest : viewDestinations )
        {
            if ( dest.createdDocker != null && dest.createdTile != null && dest.planTile != null && dest.planTile.isMaximized )
            {
                maximizedTiles.put( dest.createdDocker, dest.createdTile );
            }
        }

        for ( ViewDestination dest : viewDestinations )
        {
            Tile tile = maximizedTiles.get( dest.createdDocker );
            if ( tile != null )
            {
                dest.createdDocker.maximizeLeaf( tile );
            }
        }
    }

    /**
     * Make newly created frames visible, honoring planned stacking order as much as possible.
     */
    public static void showNewFrames( Collection<? extends ViewDestination> viewDestinations, List<? extends FrameArrangement> orderedFrameArrs )
    {
        // Stack planned new frames in front of existing frames, in plan order
        Map<FrameArrangement,DockingFrame> plannedNewFrames = new LinkedHashMap<>( );
        for ( ViewDestination dest : viewDestinations )
        {
            if ( dest.createdFrame != null && dest.planFrame != null )
            {
                plannedNewFrames.put( dest.planFrame, dest.createdFrame );
            }
        }
        for ( FrameArrangement frameArr : reversed( orderedFrameArrs ) )
        {
            DockingFrame frame = plannedNewFrames.get( frameArr );
            if ( frame != null )
            {
                frame.setVisible( true );
            }
        }

        // Stack unplanned new frames in front of existing frames
        for ( ViewDestination dest : viewDestinations )
        {
            if ( dest.createdFrame != null && dest.planFrame == null )
            {
                dest.createdFrame.setVisible( true );
            }
        }
    }

    public static GroupArrangement withPlannedPlacements( GroupArrangement existingArr, GroupArrangement planArr )
    {
        // Remember which viewIds currently exist
        Set<String> existingViewIds = findViewIds( existingArr );

        // Create a new arrangement, starting with existing views
        GroupArrangement groupArr = copyGroupArr( existingArr );

        // Add viewIds that don't exist, but do have planned placements
        for ( String planViewId : findViewIds( planArr ) )
        {
            if ( !existingViewIds.contains( planViewId ) )
            {
                ViewPlacement viewPlacement = chooseViewPlacement( groupArr, planArr, planViewId );
                viewPlacement.placeView( groupArr, planViewId );
            }
        }

        // Return complete arrangement
        return groupArr;
    }

    /**
     * This method does not currently support changing the placement of existing views. If there
     * is an existing view for the specified {@code viewId}, an exception will be thrown.
     * <p>
     * The {@link ViewPlacement} returned by {@code placementRule} will be used for its
     * {@link ViewPlacement#placeView(GroupArrangement, String)} method only. (In most cases --
     * but NOT in all cases -- this means that {@code placementRule} doesn't need to worry about
     * arguments called {@code planFrame} or {@code planTile}, and can simply use {@code null} for
     * those args. But it depends on the particular implementation of {@link ViewPlacement}.)
     */
    public static GroupArrangement withPlacement( GroupArrangement existingArr, GroupArrangement planArr, String viewId, ViewPlacementRule placementRule )
    {
        // Remember which viewIds currently exist
        Set<String> existingViewIds = findViewIds( existingArr );
        if ( existingViewIds.contains( viewId ) )
        {
            // TODO: Maybe remove the existing view, insert placement, and re-add
            throw new UnsupportedOperationException( "This method does not currently support changing the placement of an existing view" );
        }

        // Create a new arrangement, starting with existing views
        GroupArrangement newPlanArr = copyGroupArr( existingArr );

        // Add viewIds that don't exist, but do have planned placements
        for ( String planViewId : findViewIds( planArr ) )
        {
            // Skip the new viewId here, because it will be placed below
            if ( !existingViewIds.contains( planViewId ) && !equal( planViewId, viewId ) )
            {
                ViewPlacement viewPlacement = chooseViewPlacement( newPlanArr, planArr, planViewId );
                viewPlacement.placeView( newPlanArr, planViewId );
            }
        }

        // Add the new view
        ViewPlacement placement = placementRule.getPlacement( newPlanArr, existingViewIds );
        if ( placement != null )
        {
            placement.placeView( newPlanArr, viewId );
        }

        // Return complete arrangement
        return newPlanArr;
    }

    public static GroupArrangement copyGroupArr( GroupArrangement groupArr )
    {
        GroupArrangement copy = new GroupArrangement( );
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            copy.frameArrs.add( copyFrameArr( frameArr ) );
        }
        return copy;
    }

    public static FrameArrangement copyFrameArr( FrameArrangement frameArr )
    {
        FrameArrangement copy = new FrameArrangement( );

        copy.x = frameArr.x;
        copy.y = frameArr.y;
        copy.width = frameArr.width;
        copy.height = frameArr.height;
        copy.isMaximizedHoriz = frameArr.isMaximizedHoriz;
        copy.isMaximizedVert = frameArr.isMaximizedVert;

        copy.dockerArr = copyArrNode( frameArr.dockerArr );

        return copy;
    }

    public static DockerArrangementNode copyArrNode( DockerArrangementNode arrNode )
    {
        if ( arrNode instanceof DockerArrangementTile )
        {
            return copyArrTile( ( DockerArrangementTile ) arrNode );
        }
        else if ( arrNode instanceof DockerArrangementSplit )
        {
            return copyArrSplit( ( DockerArrangementSplit ) arrNode );
        }
        else if ( arrNode == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + arrNode.getClass( ).getName( ) );
        }
    }

    public static DockerArrangementTile copyArrTile( DockerArrangementTile arrTile )
    {
        DockerArrangementTile copy = new DockerArrangementTile( );

        copy.viewIds.addAll( arrTile.viewIds );
        copy.selectedViewId = arrTile.selectedViewId;
        copy.isMaximized = arrTile.isMaximized;

        return copy;
    }

    public static DockerArrangementSplit copyArrSplit( DockerArrangementSplit arrSplit )
    {
        DockerArrangementSplit copy = new DockerArrangementSplit( );

        copy.arrangeVertically = arrSplit.arrangeVertically;
        copy.splitFrac = arrSplit.splitFrac;
        copy.childA = copyArrNode( arrSplit.childA );
        copy.childB = copyArrNode( arrSplit.childB );

        return copy;
    }

    public static void pruneEmpty( Tile tile, boolean disposeEmptyFrame )
    {
        if ( tile.numViews( ) == 0 )
        {
            MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, tile );
            docker.removeLeaf( tile );

            if ( disposeEmptyFrame && docker.numLeaves( ) == 0 )
            {
                DockingFrame frame = getAncestorOfClass( DockingFrame.class, docker );
                if ( frame != null && frame.getContentPane( ) == docker )
                {
                    frame.dispose( );
                }
            }
        }
    }

    public static class GroupRealization
    {
        public final DockingGroupBase group;
        public final GroupArrangement groupArr;
        public final Map<FrameArrangement,DockingFrame> frames;
        public final Map<DockerArrangementNode,Component> components;

        public GroupRealization( DockingGroupBase group,
                                 GroupArrangement groupArr,
                                 Map<FrameArrangement,DockingFrame> frames,
                                 Map<DockerArrangementNode,Component> components )
        {
            this.group = group;
            this.groupArr = groupArr;
            this.frames = unmodifiableMap( frames );
            this.components = unmodifiableMap( components );
        }
    }

    public static GroupRealization toGroupRealization( DockingGroupBase group )
    {
        Map<FrameArrangement,DockingFrame> framesMap = new LinkedHashMap<>( );
        Map<DockerArrangementNode,Component> componentsMap = new LinkedHashMap<>( );

        GroupArrangement groupArr = new GroupArrangement( );
        for ( DockingFrame frame : group.windows( ) )
        {
            FrameArrangement frameArr = new FrameArrangement( );

            Rectangle bounds = frame.getNormalBounds( );
            frameArr.x = bounds.x;
            frameArr.y = bounds.y;
            frameArr.width = bounds.width;
            frameArr.height = bounds.height;

            int state = frame.getExtendedState( );
            frameArr.isMaximizedHoriz = ( ( state & MAXIMIZED_HORIZ ) != 0 );
            frameArr.isMaximizedVert = ( ( state & MAXIMIZED_VERT ) != 0 );

            frameArr.dockerArr = toArrNode( frame.docker.snapshot( ), componentsMap );

            groupArr.frameArrs.add( frameArr );
            framesMap.put( frameArr, frame );
        }

        return new GroupRealization( group, groupArr, framesMap, componentsMap );
    }

    protected static DockerArrangementNode toArrNode( MultiSplitPane.Node node, Map<DockerArrangementNode,Component> components_INOUT )
    {
        if ( node instanceof MultiSplitPane.Leaf )
        {
            MultiSplitPane.Leaf leaf = ( MultiSplitPane.Leaf ) node;

            List<String> viewIds = new ArrayList<>( );
            String selectedViewId = null;
            Component c = leaf.component;
            if ( c instanceof Tile )
            {
                Tile tile = ( Tile ) c;
                for ( int i = 0; i < tile.numViews( ); i++ )
                {
                    String viewId = tile.view( i ).viewId;
                    viewIds.add( viewId );
                }
                View selectedView = tile.selectedView( );
                selectedViewId = ( selectedView == null ? null : selectedView.viewId );
            }
            else
            {
                // TODO: Handle non-Tile components
            }

            DockerArrangementTile arrTile = new DockerArrangementTile( );
            arrTile.viewIds = viewIds;
            arrTile.selectedViewId = selectedViewId;
            arrTile.isMaximized = leaf.isMaximized;

            // TODO: Handle non-Tile components
            components_INOUT.put( arrTile, c );

            return arrTile;
        }
        else if ( node instanceof MultiSplitPane.Split )
        {
            MultiSplitPane.Split split = ( MultiSplitPane.Split ) node;
            DockerArrangementSplit arrSplit = new DockerArrangementSplit( );
            arrSplit.arrangeVertically = split.arrangeVertically;
            arrSplit.splitFrac = split.splitFrac;
            arrSplit.childA = toArrNode( split.childA, components_INOUT );
            arrSplit.childB = toArrNode( split.childB, components_INOUT );

            components_INOUT.put( arrSplit, split.component );

            return arrSplit;
        }
        else if ( node == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + MultiSplitPane.Node.class.getName( ) + ": " + node.getClass( ).getName( ) );
        }
    }

    public static interface ViewPlacementRule
    {
        ViewPlacement getPlacement( GroupArrangement planArr, Set<String> existingViewIds );
    }

    public static interface ViewPlacement
    {
        void placeView( GroupArrangement groupArr, String viewId );
        ViewDestination placeView( GroupRealization existing, View newView );
    }

    public static class ViewDestination
    {
        public final DockingFrame createdFrame;
        public final FrameArrangement planFrame;

        public final MultiSplitPane createdDocker;

        public final Tile createdTile;
        public final DockerArrangementTile planTile;

        public ViewDestination( DockingFrame createdFrame,
                                FrameArrangement planFrame,

                                MultiSplitPane createdDocker,

                                Tile createdTile,
                                DockerArrangementTile planTile )
        {
            this.createdFrame = createdFrame;
            this.planFrame = planFrame;

            this.createdDocker = createdDocker;

            this.createdTile = createdTile;
            this.planTile = planTile;
        }
    }

    public static class InExistingTile implements ViewPlacement
    {
        public final FrameArrangement planFrame;
        public final DockerArrangementTile planTile;
        public final DockerArrangementTile existingTile;
        public final int viewNum;

        public InExistingTile( FrameArrangement planFrame, DockerArrangementTile planTile, DockerArrangementTile existingTile, int viewNum )
        {
            this.planFrame = planFrame;
            this.planTile = planTile;
            this.existingTile = existingTile;
            this.viewNum = viewNum;
        }

        @Override
        public void placeView( GroupArrangement groupArr, String viewId )
        {
            this.existingTile.viewIds.add( this.viewNum, viewId );
        }

        @Override
        public ViewDestination placeView( GroupRealization existing, View newView )
        {
            // TODO: Handle non-Tile components
            Tile tile = ( Tile ) existing.components.get( this.existingTile );
            tile.addView( newView, viewNum );

            return new ViewDestination( null, null, null, null, null );
        }
    }

    public static class BesideExistingNeighbor implements ViewPlacement
    {
        public final FrameArrangement planFrame;
        public final DockerArrangementTile planTile;
        public final DockerArrangementNode neighborNode;
        public final Side sideOfNeighbor;
        public final double extentFrac;

        public BesideExistingNeighbor( FrameArrangement planFrame, DockerArrangementTile planTile, DockerArrangementNode neighborNode, Side sideOfNeighbor, double extentFrac )
        {
            this.planFrame = planFrame;
            this.planTile = planTile;
            this.neighborNode = neighborNode;
            this.sideOfNeighbor = sideOfNeighbor;
            this.extentFrac = extentFrac;
        }

        @Override
        public void placeView( GroupArrangement groupArr, String viewId )
        {
            DockerArrangementTile newTile = new DockerArrangementTile( );
            newTile.viewIds.add( viewId );
            newTile.selectedViewId = viewId;
            newTile.isMaximized = false;

            DockerArrangementSplit newSplit = new DockerArrangementSplit( );
            newSplit.arrangeVertically = ( this.sideOfNeighbor == TOP || this.sideOfNeighbor == BOTTOM );
            boolean newIsChildA = ( this.sideOfNeighbor == LEFT || this.sideOfNeighbor == TOP );
            newSplit.childA = ( newIsChildA ? newTile : this.neighborNode );
            newSplit.childB = ( newIsChildA ? this.neighborNode : newTile );
            newSplit.splitFrac = ( newIsChildA ? this.extentFrac : 1.0 - this.extentFrac );

            replaceArrNode( groupArr, this.neighborNode, newSplit );
        }

        @Override
        public ViewDestination placeView( GroupRealization existing, View newView )
        {
            Tile newTile = existing.group.createNewTile( );
            newTile.addView( newView, 0 );

            Component neighbor = existing.components.get( this.neighborNode );

            MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, neighbor );
            docker.addNeighborLeaf( newTile, neighbor, sideOfNeighbor, extentFrac );

            return new ViewDestination( null, null, null, newTile, this.planTile );
        }
    }

    public static class InNewFrame implements ViewPlacement
    {
        public final FrameArrangement planFrame;
        public final DockerArrangementTile planTile;

        /**
         * {@code planFrame} is used by both {@link #placeView(GroupRealization, View)} and
         * {@link #placeView(GroupArrangement, String)} methods, so it must be non-null.
         */
        public InNewFrame( FrameArrangement planFrame, DockerArrangementTile planTile )
        {
            this.planFrame = requireNonNull( planFrame );
            this.planTile = planTile;
        }

        @Override
        public void placeView( GroupArrangement groupArr, String viewId )
        {
            DockerArrangementTile newTile = new DockerArrangementTile( );
            newTile.viewIds.add( viewId );
            newTile.selectedViewId = viewId;
            newTile.isMaximized = false;

            FrameArrangement newFrame = new FrameArrangement( );
            newFrame.dockerArr = newTile;

            newFrame.x = this.planFrame.x;
            newFrame.y = this.planFrame.y;
            newFrame.width = this.planFrame.width;
            newFrame.height = this.planFrame.height;
            newFrame.isMaximizedHoriz = this.planFrame.isMaximizedHoriz;
            newFrame.isMaximizedVert = this.planFrame.isMaximizedVert;

            groupArr.frameArrs.add( newFrame );
        }

        @Override
        public ViewDestination placeView( GroupRealization existing, View newView )
        {
            Tile newTile = existing.group.createNewTile( );
            newTile.addView( newView, 0 );

            DockingFrame newFrame = existing.group.addNewWindow( );
            newFrame.docker.addInitialLeaf( newTile );

            newFrame.setBounds( this.planFrame.x, this.planFrame.y, this.planFrame.width, this.planFrame.height );
            newFrame.setNormalBounds( this.planFrame.x, this.planFrame.y, this.planFrame.width, this.planFrame.height );
            newFrame.setExtendedState( getFrameExtendedState( this.planFrame.isMaximizedHoriz, this.planFrame.isMaximizedVert ) );

            return new ViewDestination( newFrame, this.planFrame, newFrame.docker, newTile, this.planTile );
        }
    }

    public static class InNewFallbackFrame implements ViewPlacement
    {
        @Override
        public void placeView( GroupArrangement groupArr, String viewId )
        {
            DockerArrangementTile newTile = new DockerArrangementTile( );
            newTile.viewIds.add( viewId );
            newTile.selectedViewId = viewId;
            newTile.isMaximized = false;

            FrameArrangement newFrame = new FrameArrangement( );
            newFrame.dockerArr = newTile;

            Rectangle newFrameBounds = getNewFallbackFrameBounds( );
            newFrame.x = newFrameBounds.x;
            newFrame.y = newFrameBounds.y;
            newFrame.width = newFrameBounds.width;
            newFrame.height = newFrameBounds.height;
            newFrame.isMaximizedHoriz = false;
            newFrame.isMaximizedVert = false;

            groupArr.frameArrs.add( newFrame );
        }

        @Override
        public ViewDestination placeView( GroupRealization existing, View newView )
        {
            Tile newTile = existing.group.createNewTile( );
            newTile.addView( newView, 0 );

            DockingFrame newFrame = existing.group.addNewWindow( );
            newFrame.docker.addInitialLeaf( newTile );

            Rectangle newFrameBounds = getNewFallbackFrameBounds( );
            newFrame.setBounds( newFrameBounds );
            newFrame.setNormalBounds( newFrameBounds );
            newFrame.setExtendedState( getFrameExtendedState( false, false ) );

            return new ViewDestination( newFrame, null, newFrame.docker, newTile, null );
        }
    }

    public static Rectangle getNewFallbackFrameBounds( )
    {
        Rectangle screenBounds = getLocalGraphicsEnvironment( ).getMaximumWindowBounds( );
        float fracOfScreen = 0.85f;
        int width = round( fracOfScreen * screenBounds.width );
        int height = round( fracOfScreen * screenBounds.height );
        int x = screenBounds.x + ( ( screenBounds.width - width ) / 2 );
        int y = screenBounds.y + ( ( screenBounds.height - height ) / 2 );

        return new Rectangle( x, y, width, height );
    }

    public static ViewPlacement chooseViewPlacement( GroupArrangement existingArr, GroupArrangement planArr, String viewId )
    {
        Map<DockerArrangementNode,Set<String>> planSubtreeViewIds = buildSubtreeViewIdsMap( planArr );
        Map<DockerArrangementNode,Set<String>> existingSubtreeViewIds = buildSubtreeViewIdsMap( existingArr );

        FrameArrangement planFrame = findFrameArrContaining( planArr, planSubtreeViewIds, viewId );
        if ( planFrame != null )
        {
            // Add to an existing tile that is similar to the planned tile
            DockerArrangementTile planTile = findArrTileContaining( planFrame.dockerArr, planSubtreeViewIds, viewId );
            Set<String> planTileViewIds = planSubtreeViewIds.get( planTile );
            DockerArrangementTile existingTile = findSimilarArrTile( existingSubtreeViewIds, planTileViewIds );
            if ( existingTile != null )
            {
                int viewNum = chooseViewNum( planTile.viewIds, existingTile.viewIds, viewId );
                return new InExistingTile( planFrame, planTile, existingTile, viewNum );
            }

            // Create a new tile, beside an existing neighbor that is similar to the planned neighbor
            //
            // We look first for a good "sibling," then for a good "uncle" ... and so on up the tree
            //
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
                    return new BesideExistingNeighbor( planFrame, planTile, existingNeighbor, sideOfNeighbor, extentFrac );
                }

                // Go one level up the tree and try again
                planNode = planParent;
            }

            // Create a new frame, with size and position from the planned arrangement
            return new InNewFrame( planFrame, planTile );
        }

        // First fallback is in the largest tile
        DockerArrangementTile existingLargest = findLargestArrTile( existingArr );
        if ( existingLargest != null )
        {
            int viewNum = existingLargest.viewIds.size( );
            return new InExistingTile( null, null, existingLargest, viewNum );
        }

        // Final fallback is in a new frame
        return new InNewFallbackFrame( );
    }

    public static int chooseViewNum( List<String> planViewIds, List<String> existingViewIds, String newViewId )
    {
        Set<String> viewIdsBefore = new LinkedHashSet<>( );
        Set<String> viewIdsAfter = new LinkedHashSet<>( );

        boolean beforeNewViewId = true;
        for ( String viewId : planViewIds )
        {
            if ( viewId.equals( newViewId ) )
            {
                beforeNewViewId = false;
            }
            else if ( beforeNewViewId )
            {
                viewIdsBefore.add( viewId );
            }
            else
            {
                viewIdsAfter.add( viewId );
            }
        }

        int viewNumBefore = -1;
        int viewNumAfter = existingViewIds.size( );

        for ( int i = 0; i < existingViewIds.size( ); i++ )
        {
            String viewId = existingViewIds.get( i );

            if ( viewIdsBefore.contains( viewId ) )
            {
                viewNumBefore = max( viewNumBefore, i );
            }

            if ( viewIdsAfter.contains( viewId ) )
            {
                viewNumAfter = min( viewNumAfter, i );
            }
        }

        if ( viewNumBefore < viewNumAfter )
        {
            return ( viewNumBefore + 1 );
        }
        else
        {
            return existingViewIds.size( );
        }
    }

    public static Set<String> findViewIds( GroupArrangement groupArr )
    {
        Set<String> viewIds = new LinkedHashSet<>( );
        for ( DockerArrangementTile tile : findTiles( groupArr ) )
        {
            viewIds.addAll( tile.viewIds );
        }
        return viewIds;
    }

    public static Set<String> findViewIds( DockerArrangementNode arrNode )
    {
        Set<String> viewIds = new LinkedHashSet<>( );
        for ( DockerArrangementTile tile : findTiles( arrNode ) )
        {
            viewIds.addAll( tile.viewIds );
        }
        return viewIds;
    }

    public static Set<DockerArrangementTile> findTiles( GroupArrangement groupArr )
    {
        Set<DockerArrangementTile> tiles = new LinkedHashSet<>( );
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            putSubtreeTiles( frameArr.dockerArr, tiles );
        }
        return tiles;
    }

    public static Set<DockerArrangementTile> findTiles( DockerArrangementNode arrNode )
    {
        Set<DockerArrangementTile> tiles = new LinkedHashSet<>( );
        putSubtreeTiles( arrNode, tiles );
        return tiles;
    }

    protected static void putSubtreeTiles( DockerArrangementNode arrNode, Set<DockerArrangementTile> result_OUT )
    {
        if ( arrNode instanceof DockerArrangementTile )
        {
            DockerArrangementTile arrTile = ( DockerArrangementTile ) arrNode;
            result_OUT.add( arrTile );
        }
        else if ( arrNode instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit arrSplit = ( DockerArrangementSplit ) arrNode;
            putSubtreeTiles( arrSplit.childA, result_OUT );
            putSubtreeTiles( arrSplit.childB, result_OUT );
        }
        else if ( arrNode != null )
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + arrNode.getClass( ).getName( ) );
        }
    }

    public static Map<DockerArrangementNode,Set<String>> buildSubtreeViewIdsMap( GroupArrangement groupArr )
    {
        Map<DockerArrangementNode,Set<String>> result = new LinkedHashMap<>( );
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            putSubtreeViewIds( frameArr.dockerArr, result );
        }
        return result;
    }

    protected static Set<String> putSubtreeViewIds( DockerArrangementNode arrNode, Map<DockerArrangementNode,Set<String>> viewIds_INOUT )
    {
        if ( arrNode instanceof DockerArrangementTile )
        {
            DockerArrangementTile arrTile = ( DockerArrangementTile ) arrNode;
            Set<String> result = unmodifiableSet( new LinkedHashSet<>( arrTile.viewIds ) );
            viewIds_INOUT.put( arrTile, result );
            return result;
        }
        else if ( arrNode instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit arrSplit = ( DockerArrangementSplit ) arrNode;
            Set<String> resultA = putSubtreeViewIds( arrSplit.childA, viewIds_INOUT );
            Set<String> resultB = putSubtreeViewIds( arrSplit.childB, viewIds_INOUT );
            Set<String> result = unmodifiableSet( union( resultA, resultB ) );
            viewIds_INOUT.put( arrSplit, result );
            return result;
        }
        else if ( arrNode == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + arrNode.getClass( ).getName( ) );
        }
    }

    protected static FrameArrangement findFrameArrContaining( GroupArrangement groupArr, Map<DockerArrangementNode,Set<String>> subtreeViewIds, String viewId )
    {
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            if ( containsView( frameArr.dockerArr, subtreeViewIds, viewId ) )
            {
                return frameArr;
            }
        }

        return null;
    }

    protected static boolean containsView( DockerArrangementNode node, Map<DockerArrangementNode,Set<String>> subtreeViewIds, String viewId )
    {
        Set<String> viewIds = subtreeViewIds.get( node );
        return ( viewIds != null && viewIds.contains( viewId ) );
    }

    public static DockerArrangementTile findArrTileContaining( GroupArrangement groupArr, String viewId )
    {
        return findArrTileContaining( groupArr, buildSubtreeViewIdsMap( groupArr ), viewId );
    }

    protected static DockerArrangementTile findArrTileContaining( GroupArrangement groupArr, Map<DockerArrangementNode,Set<String>> subtreeViewIds, String viewId )
    {
        FrameArrangement frame = findFrameArrContaining( groupArr, subtreeViewIds, viewId );
        return ( frame == null ? null : findArrTileContaining( frame.dockerArr, subtreeViewIds, viewId ) );
    }

    protected static DockerArrangementTile findArrTileContaining( DockerArrangementNode node, Map<DockerArrangementNode,Set<String>> subtreeViewIds, String viewId )
    {
        if ( node instanceof DockerArrangementTile )
        {
            DockerArrangementTile tile = ( DockerArrangementTile ) node;
            if ( subtreeViewIds.get( tile ).contains( viewId ) )
            {
                return tile;
            }

            return null;
        }
        else if ( node instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit split = ( DockerArrangementSplit ) node;
            if ( subtreeViewIds.get( split ).contains( viewId ) )
            {
                DockerArrangementTile resultA = findArrTileContaining( split.childA, subtreeViewIds, viewId );
                if ( resultA != null )
                {
                    return resultA;
                }

                DockerArrangementTile resultB = findArrTileContaining( split.childB, subtreeViewIds, viewId );
                if ( resultB != null )
                {
                    return resultB;
                }
            }

            return null;
        }
        else if ( node == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + node.getClass( ).getName( ) );
        }
    }

    protected static DockerArrangementTile findSimilarArrTile( Map<DockerArrangementNode,Set<String>> subtreeViewIds, Set<String> viewIds )
    {
        return ( DockerArrangementTile ) findSimilarArrNode( subtreeViewIds, viewIds, true );
    }

    protected static DockerArrangementNode findSimilarArrNode( Map<DockerArrangementNode,Set<String>> subtreeViewIds, Set<String> viewIds )
    {
        return findSimilarArrNode( subtreeViewIds, viewIds, false );
    }

    protected static DockerArrangementNode findSimilarArrNode( Map<DockerArrangementNode,Set<String>> subtreeViewIds, Set<String> viewIds, boolean requireTile )
    {
        DockerArrangementNode bestNode = null;
        int bestCommonCount = 0;
        int bestExtraneousCount = 0;

        for ( Entry<DockerArrangementNode,Set<String>> en : subtreeViewIds.entrySet( ) )
        {
            DockerArrangementNode node = en.getKey( );
            Set<String> nodeViewIds = en.getValue( );

            if ( !requireTile || node instanceof DockerArrangementTile )
            {
                int commonCount = intersection( nodeViewIds, viewIds ).size( );
                int extraneousCount = nodeViewIds.size( ) - commonCount;
                if ( commonCount > bestCommonCount || ( commonCount == bestCommonCount && extraneousCount < bestExtraneousCount ) )
                {
                    bestNode = node;
                    bestCommonCount = commonCount;
                    bestExtraneousCount = extraneousCount;
                }
            }
        }

        return bestNode;
    }

    protected static DockerArrangementSplit findArrNodeParent( DockerArrangementNode root, DockerArrangementNode child )
    {
        if ( root instanceof DockerArrangementTile )
        {
            return null;
        }
        else if ( root instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit split = ( DockerArrangementSplit ) root;

            if ( child == split.childA || child == split.childB )
            {
                return split;
            }

            DockerArrangementSplit resultA = findArrNodeParent( split.childA, child );
            if ( resultA != null )
            {
                return resultA;
            }

            DockerArrangementSplit resultB = findArrNodeParent( split.childB, child );
            if ( resultB != null )
            {
                return resultB;
            }

            return null;
        }
        else if ( root == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + root.getClass( ).getName( ) );
        }
    }

    protected static void replaceArrNode( GroupArrangement groupArr, DockerArrangementNode existingNode, DockerArrangementNode newNode )
    {
        FrameArrangement frameArr = findFrameArrContaining( groupArr, existingNode );
        if ( frameArr == null )
        {
            throw new RuntimeException( "No such node in this docking arrangement" );
        }

        if ( existingNode == frameArr.dockerArr )
        {
            frameArr.dockerArr = newNode;
        }
        else
        {
            DockerArrangementSplit parentSplit = findArrNodeParent( frameArr.dockerArr, existingNode );
            if ( parentSplit == null )
            {
                throw new RuntimeException( "Failed to find parent node" );
            }

            if ( existingNode == parentSplit.childA )
            {
                parentSplit.childA = newNode;
            }
            else
            {
                parentSplit.childB = newNode;
            }
        }
    }

    protected static FrameArrangement findFrameArrContaining( GroupArrangement groupArr, DockerArrangementNode node )
    {
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            if ( isDescendant( frameArr.dockerArr, node ) )
            {
                return frameArr;
            }
        }

        return null;
    }

    protected static boolean isDescendant( DockerArrangementNode node, DockerArrangementNode descendant )
    {
        if ( node instanceof DockerArrangementTile )
        {
            return ( node == descendant );
        }
        else if ( node instanceof DockerArrangementSplit )
        {
            if ( node == descendant )
            {
                return true;
            }
            else
            {
                DockerArrangementSplit split = ( DockerArrangementSplit ) node;
                return ( isDescendant( split.childA, descendant ) || isDescendant( split.childB, descendant ) );
            }
        }
        else if ( node == null )
        {
            return false;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + node.getClass( ).getName( ) );
        }
    }

    public static DockerArrangementTile findLargestArrTile( GroupArrangement groupArr )
    {
        ArrTileWithSize best = null;

        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            ArrTileWithSize frameBest = findMaximizedArrTile( frameArr );

            if ( frameBest == null )
            {
                frameBest = findLargestArrTile( frameArr );
            }

            if ( best == null || ( frameBest != null && frameBest.width*frameBest.height > best.width*best.height ) )
            {
                best = frameBest;
            }
        }

        return ( best == null ? null : best.tile );
    }

    protected static class ArrTileWithSize
    {
        public final DockerArrangementTile tile;
        public final int width;
        public final int height;

        public ArrTileWithSize( DockerArrangementTile tile, int width, int height )
        {
            this.tile = tile;
            this.width = width;
            this.height = height;
        }
    }

    protected static ArrTileWithSize findMaximizedArrTile( FrameArrangement frameArr )
    {
        DockerArrangementTile maximizedTile = findMaximizedArrTile( frameArr.dockerArr );
        return ( maximizedTile == null ? null : new ArrTileWithSize( maximizedTile, frameArr.width, frameArr.height ) );
    }

    protected static DockerArrangementTile findMaximizedArrTile( DockerArrangementNode root )
    {
        if ( root instanceof DockerArrangementTile )
        {
            DockerArrangementTile tile = ( DockerArrangementTile ) root;
            return ( tile.isMaximized ? tile : null );
        }
        else if ( root instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit split = ( DockerArrangementSplit ) root;

            DockerArrangementTile resultA = findMaximizedArrTile( split.childA );
            if ( resultA != null )
            {
                return resultA;
            }

            DockerArrangementTile resultB = findMaximizedArrTile( split.childB );
            if ( resultB != null )
            {
                return resultB;
            }

            return null;
        }
        else if ( root == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + root.getClass( ).getName( ) );
        }
    }

    protected static ArrTileWithSize findLargestArrTile( FrameArrangement frameArr )
    {
        return findLargestArrTile( frameArr.dockerArr, frameArr.width, frameArr.height );
    }

    protected static ArrTileWithSize findLargestArrTile( DockerArrangementNode root, int rootWidth, int rootHeight )
    {
        if ( root instanceof DockerArrangementTile )
        {
            DockerArrangementTile tile = ( DockerArrangementTile ) root;
            return new ArrTileWithSize( tile, rootWidth, rootHeight );
        }
        else if ( root instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit split = ( DockerArrangementSplit ) root;

            int widthA;
            int widthB;
            int heightA;
            int heightB;
            if ( split.arrangeVertically )
            {
                int[] heightsAB = computeChildSizes( rootHeight, 0, 0, split.splitFrac );
                widthA = rootWidth;
                widthB = rootWidth;
                heightA = heightsAB[ 0 ];
                heightB = heightsAB[ 1 ];
            }
            else
            {
                int[] widthsAB = computeChildSizes( rootWidth, 0, 0, split.splitFrac );
                widthA = widthsAB[ 0 ];
                widthB = widthsAB[ 1 ];
                heightA = rootHeight;
                heightB = rootHeight;
            }

            ArrTileWithSize resultA = findLargestArrTile( split.childA, widthA, heightA );
            ArrTileWithSize resultB = findLargestArrTile( split.childB, widthB, heightB );

            if ( resultA == null && resultB == null )
            {
                return null;
            }
            else if ( resultA == null )
            {
                return resultB;
            }
            else if ( resultB == null )
            {
                return resultA;
            }
            else
            {
                return ( resultA.width*resultA.height >= resultB.width*resultB.height ? resultA : resultB );
            }
        }
        else if ( root == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + root.getClass( ).getName( ) );
        }
    }

}
