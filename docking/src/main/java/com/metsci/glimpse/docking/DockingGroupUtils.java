package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.MiscUtils.intersection;
import static com.metsci.glimpse.docking.MiscUtils.union;
import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.RIGHT;
import static com.metsci.glimpse.docking.Side.TOP;
import static java.awt.Frame.MAXIMIZED_HORIZ;
import static java.awt.Frame.MAXIMIZED_VERT;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

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

import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingGroupUtils
{

    public static interface ViewPlacement
    {
    }

    public static class BesideExistingNeighbor implements ViewPlacement
    {
        public final DockerArrangementNode neighbor;
        public final Side sideOfNeighbor;
        public final double extentFrac;

        public BesideExistingNeighbor( DockerArrangementNode neighbor, Side sideOfNeighbor, double extentFrac )
        {
            this.neighbor = neighbor;
            this.sideOfNeighbor = sideOfNeighbor;
            this.extentFrac = extentFrac;
        }
    }

    public static class InExistingTile implements ViewPlacement
    {
        public final DockerArrangementTile existingTile;

        public InExistingTile( DockerArrangementTile existingTile )
        {
            this.existingTile = existingTile;
        }
    }

    public static class InNewFrame implements ViewPlacement
    {
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final boolean isMaximizedHoriz;
        public final boolean isMaximizedVert;

        public InNewFrame( int x,
                           int y,
                           int width,
                           int height,
                           boolean isMaximizedHoriz,
                           boolean isMaximizedVert )
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isMaximizedHoriz = isMaximizedHoriz;
            this.isMaximizedVert = isMaximizedVert;
        }
    }

    public static class InFallbackLocation implements ViewPlacement
    {
    }




    public static ViewPlacement chooseViewPlacement( GroupArrangement existingArr, GroupArrangement planArr, String viewId )
    {
        Map<DockerArrangementNode,Set<String>> planSubtreeViewIds = buildPlanSubtreeViewIdsMap( planArr );
        Map<DockerArrangementNode,Set<String>> existingSubtreeViewIds = buildPlanSubtreeViewIdsMap( existingArr );

        FrameArrangement planFrame = findFrameArrContaining( planArr, viewId );
        if ( planFrame != null )
        {
            // Add to an existing tile
            DockerArrangementTile planTile = findArrTileContaining( planFrame.dockerArr, planSubtreeViewIds, viewId );
            Set<String> planTileViewIds = planSubtreeViewIds.get( planTile );
            DockerArrangementTile existingTile = findSimilarArrTile( existingSubtreeViewIds, planTileViewIds );
            if ( existingTile != null )
            {
                return new InExistingTile( existingTile );
            }

            // Create a new tile that neighbors an existing tile or split
            DockerArrangementSplit planParent = findArrNodeParent( planFrame.dockerArr, planTile );
            if ( planParent != null )
            {
                boolean newIsChildA = ( planTile == planParent.childA );
                DockerArrangementNode planNeighbor = ( newIsChildA ? planParent.childB : planParent.childA );
                Set<String> planNeighborViewIds = planSubtreeViewIds.get( planNeighbor );
                DockerArrangementNode existingNeighbor = findSimilarArrNode( existingSubtreeViewIds, planNeighborViewIds );
                if ( existingNeighbor != null )
                {
                    Side sideOfNeighbor = ( planParent.arrangeVertically ? ( newIsChildA ? TOP : BOTTOM ) : ( newIsChildA ? LEFT : RIGHT ) );
                    double extentFrac = ( newIsChildA ? planParent.splitFrac : 1.0 - planParent.splitFrac );
                    return new BesideExistingNeighbor( existingNeighbor, sideOfNeighbor, extentFrac );
                }
            }

            // Create a new frame, with size and position from the planned arrangement
            return new InNewFrame( planFrame.x, planFrame.y, planFrame.width, planFrame.height, planFrame.isMaximizedHoriz, planFrame.isMaximizedVert );
        }

        // Use fallback location
        return new InFallbackLocation( );
    }

    protected static Map<DockerArrangementNode,Set<String>> buildPlanSubtreeViewIdsMap( GroupArrangement groupArr )
    {
        Map<DockerArrangementNode,Set<String>> result = new LinkedHashMap<>( );
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            putPlanSubtreeViewIds( frameArr.dockerArr, result );
        }
        return result;
    }

    protected static Set<String> putPlanSubtreeViewIds( DockerArrangementNode arrNode, Map<DockerArrangementNode,Set<String>> viewIds_INOUT )
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
            Set<String> resultA = putPlanSubtreeViewIds( arrSplit.childA, viewIds_INOUT );
            Set<String> resultB = putPlanSubtreeViewIds( arrSplit.childB, viewIds_INOUT );
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

    protected static FrameArrangement findFrameArrContaining( GroupArrangement groupArr, String viewId )
    {
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            if ( containsView( frameArr.dockerArr, viewId ) )
            {
                return frameArr;
            }
        }

        return null;
    }

    protected static boolean containsView( DockerArrangementNode node, String viewId )
    {
        if ( node instanceof DockerArrangementTile )
        {
            DockerArrangementTile tile = ( DockerArrangementTile ) node;
            return tile.viewIds.contains( viewId );
        }
        else if ( node instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit split = ( DockerArrangementSplit ) node;
            return ( containsView( split.childA, viewId ) || containsView( split.childB, viewId ) );
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


    // WIP: Class name
    public static class Asdf
    {
        public final GroupArrangement groupArr;
        public final Map<FrameArrangement,DockingFrame> frames;
        public final Map<DockerArrangementNode,Component> components;

        public Asdf( GroupArrangement groupArr,
                     Map<FrameArrangement,DockingFrame> frames,
                     Map<DockerArrangementNode,Component> components )
        {
            this.groupArr = groupArr;
            this.frames = unmodifiableMap( frames );
            this.components = unmodifiableMap( components );
        }
    }

    // WIP: Function name
    public static Asdf asdf( Collection<DockingFrame> frames )
    {
        Map<FrameArrangement,DockingFrame> framesMap = new LinkedHashMap<>( );
        Map<DockerArrangementNode,Component> componentsMap = new LinkedHashMap<>( );

        GroupArrangement groupArr = new GroupArrangement( );
        for ( DockingFrame frame : frames )
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

        return new Asdf( groupArr, framesMap, componentsMap );
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
                // XXX: Handle arbitrary components
            }

            DockerArrangementTile arrTile = new DockerArrangementTile( );
            arrTile.viewIds = viewIds;
            arrTile.selectedViewId = selectedViewId;
            arrTile.isMaximized = leaf.isMaximized;

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

}
