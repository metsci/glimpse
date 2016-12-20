package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.getFrameExtendedState;
import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.MiscUtils.intersection;
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

import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
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

    public static class GroupRealization
    {
        public final DockingGroup group;
        public final GroupArrangement groupArr;
        public final Map<FrameArrangement,DockingFrame> frames;
        public final Map<DockerArrangementNode,Component> components;

        public GroupRealization( DockingGroup group,
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

    public static GroupRealization toGroupRealization( DockingGroup group )
    {
        Map<FrameArrangement,DockingFrame> framesMap = new LinkedHashMap<>( );
        Map<DockerArrangementNode,Component> componentsMap = new LinkedHashMap<>( );

        GroupArrangement groupArr = new GroupArrangement( );
        for ( DockingFrame frame : group.frames )
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
                // XXX: Handle non-Tile components
            }

            DockerArrangementTile arrTile = new DockerArrangementTile( );
            arrTile.viewIds = viewIds;
            arrTile.selectedViewId = selectedViewId;
            arrTile.isMaximized = leaf.isMaximized;

            // XXX: Handle non-Tile components
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

    public static interface ViewPlacement
    {
        void placeView( GroupArrangement groupArr, String viewId );
        ViewDestination placeView( GroupRealization existing, View newView );
    }

    public static class ViewDestination
    {
        public final FrameArrangement planFrame;
        public final boolean isNewFrame;
        public final DockingFrame frame;

        public final DockerArrangementTile planTile;
        public final boolean isNewTile;
        public final Tile tile;

        public ViewDestination( FrameArrangement planFrame,
                                boolean isNewFrame,
                                DockingFrame frame,

                                DockerArrangementTile planTile,
                                boolean isNewTile,
                                Tile tile )
        {
            this.planFrame = planFrame;
            this.isNewFrame = isNewFrame;
            this.frame = frame;

            this.planTile = planTile;
            this.isNewTile = isNewTile;
            this.tile = tile;
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
            // XXX: Handle non-Tile components
            Tile tile = ( Tile ) existing.components.get( this.existingTile );
            tile.addView( newView, viewNum );

            DockingFrame frame = getAncestorOfClass( DockingFrame.class, tile );

            return new ViewDestination( this.planFrame, false, frame, this.planTile, false, tile );
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
            Tile newTile = existing.group.tileFactory.newTile( );
            newTile.addView( newView, 0 );

            Component neighbor = existing.components.get( this.neighborNode );

            DockingFrame frame = getAncestorOfClass( DockingFrame.class, neighbor );
            frame.docker.addNeighborLeaf( newTile, neighbor, sideOfNeighbor, extentFrac );

            return new ViewDestination( this.planFrame, false, frame, this.planTile, true, newTile );
        }
    }

    public static class InNewFrame implements ViewPlacement
    {
        public final FrameArrangement planFrame;
        public final DockerArrangementTile planTile;

        public InNewFrame( FrameArrangement planFrame, DockerArrangementTile planTile )
        {
            this.planFrame = planFrame;
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
            Tile newTile = existing.group.tileFactory.newTile( );
            newTile.addView( newView, 0 );

            DockingFrame newFrame = existing.group.addNewFrame( );
            newFrame.docker.addInitialLeaf( newTile );

            newFrame.setBounds( this.planFrame.x, this.planFrame.y, this.planFrame.width, this.planFrame.height );
            newFrame.setNormalBounds( this.planFrame.x, this.planFrame.y, this.planFrame.width, this.planFrame.height );
            newFrame.setExtendedState( getFrameExtendedState( this.planFrame.isMaximizedHoriz, this.planFrame.isMaximizedVert ) );
            newFrame.setVisible( true );

            return new ViewDestination( this.planFrame, true, newFrame, this.planTile, true, newTile );
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
            Tile newTile = existing.group.tileFactory.newTile( );
            newTile.addView( newView, 0 );

            DockingFrame newFrame = existing.group.addNewFrame( );
            newFrame.docker.addInitialLeaf( newTile );

            Rectangle newFrameBounds = getNewFallbackFrameBounds( );
            newFrame.setBounds( newFrameBounds );
            newFrame.setNormalBounds( newFrameBounds );
            newFrame.setExtendedState( getFrameExtendedState( false, false ) );
            newFrame.setVisible( true );

            return new ViewDestination( null, true, newFrame, null, true, newTile );
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
        Map<DockerArrangementNode,Set<String>> planSubtreeViewIds = buildPlanSubtreeViewIdsMap( planArr );
        Map<DockerArrangementNode,Set<String>> existingSubtreeViewIds = buildPlanSubtreeViewIdsMap( existingArr );

        FrameArrangement planFrame = findFrameArrContaining( planArr, viewId );
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

        Map<DockerArrangementNode,Set<String>> map = new LinkedHashMap<>( );
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            Set<String> frameViewIds = putPlanSubtreeViewIds( frameArr.dockerArr, map );
            viewIds.addAll( frameViewIds );
        }

        return viewIds;
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

    public static boolean containsView( GroupArrangement groupArr, String viewId )
    {
        return ( findFrameArrContaining( groupArr, viewId ) != null );
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
