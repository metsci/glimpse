package com.metsci.glimpse.docking.group.frame;

import static com.metsci.glimpse.docking.MiscUtils.reversed;
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
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.lang.Math.round;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingGroupMultiframeUtils
{

    public static <T> T placeView( DockingGroupMultiframe group, GroupArrangement planArr, String viewId, ViewPlacerMultiframe<T> viewPlacer )
    {
        Map<DockerArrangementNode,Component> componentsMap = new LinkedHashMap<>( );
        GroupArrangement existingArr = group.existingArrangement( componentsMap );
        Map<DockerArrangementNode,Set<String>> existingSubtreeViewIds = buildSubtreeViewIdsMap( existingArr );
        Map<DockerArrangementNode,Set<String>> planSubtreeViewIds = buildSubtreeViewIdsMap( group.planArr );

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

        // Final fallback is in a new frame
        return viewPlacer.addInNewFallbackFrame( );
    }

    public static Rectangle fallbackFrameBounds( )
    {
        float fracOfScreen = 0.85f;
        Rectangle screenBounds = getLocalGraphicsEnvironment( ).getMaximumWindowBounds( );
        int width = round( fracOfScreen * screenBounds.width );
        int height = round( fracOfScreen * screenBounds.height );
        int x = screenBounds.x + ( ( screenBounds.width - width ) / 2 );
        int y = screenBounds.y + ( ( screenBounds.height - height ) / 2 );
        return new Rectangle( x, y, width, height );
    }

    /**
     * Restore maximized tiles in newly created frames.
     */
    public static void restoreMaximizedTilesInNewDockers( Collection<? extends ViewDestinationMultiframe> viewDestinations )
    {
        Map<DockingFrame,Tile> maximizedTiles = new LinkedHashMap<>( );
        for ( ViewDestinationMultiframe dest : viewDestinations )
        {
            if ( dest.createdFrame != null && dest.createdTile != null && dest.planTile != null && dest.planTile.isMaximized )
            {
                maximizedTiles.put( dest.createdFrame, dest.createdTile );
            }
        }

        for ( ViewDestinationMultiframe dest : viewDestinations )
        {
            Tile tile = maximizedTiles.get( dest.createdFrame );
            if ( tile != null )
            {
                dest.createdFrame.docker.maximizeLeaf( tile );
            }
        }
    }

    /**
     * Make newly created frames visible, honoring planned stacking order as much as possible.
     */
    public static void showNewFrames( Collection<? extends ViewDestinationMultiframe> viewDestinations, List<? extends FrameArrangement> orderedFrameArrs )
    {
        // Stack planned new frames in front of existing frames, in plan order
        Map<FrameArrangement,DockingFrame> plannedNewFrames = new LinkedHashMap<>( );
        for ( ViewDestinationMultiframe dest : viewDestinations )
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
        for ( ViewDestinationMultiframe dest : viewDestinations )
        {
            if ( dest.createdFrame != null && dest.planFrame == null )
            {
                dest.createdFrame.setVisible( true );
            }
        }
    }

}
