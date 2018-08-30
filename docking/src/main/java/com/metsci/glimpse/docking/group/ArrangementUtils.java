package com.metsci.glimpse.docking.group;

import static com.metsci.glimpse.docking.MiscUtils.union;
import static com.metsci.glimpse.docking.SplitPane.computeChildSizes;
import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class ArrangementUtils
{

    public static Set<String> findViewIds( GroupArrangement groupArr )
    {
        Set<String> viewIds = new LinkedHashSet<>( );
        for ( DockerArrangementTile tile : findTiles( groupArr ) )
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

    public static DockerArrangementTile findArrTileContaining( GroupArrangement groupArr, String viewId )
    {
        return findArrTileContaining( groupArr, buildSubtreeViewIdsMap( groupArr ), viewId );
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

    public static Map<DockerArrangementNode,Set<String>> buildSubtreeViewIdsMap( DockerArrangementNode arrNode )
    {
        Map<DockerArrangementNode,Set<String>> result = new LinkedHashMap<>( );
        putSubtreeViewIds( arrNode, result );
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

    public static DockerArrangementTile findArrTileContaining( GroupArrangement groupArr, Map<DockerArrangementNode,Set<String>> subtreeViewIds, String viewId )
    {
        FrameArrangement frame = findFrameArrContaining( groupArr, subtreeViewIds, viewId );
        return ( frame == null ? null : findArrTileContaining( frame.dockerArr, subtreeViewIds, viewId ) );
    }

    public static FrameArrangement findFrameArrContaining( GroupArrangement groupArr, Map<DockerArrangementNode,Set<String>> subtreeViewIds, String viewId )
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

    public static void replaceArrNode( GroupArrangement groupArr, DockerArrangementNode existingNode, DockerArrangementNode newNode )
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

    public static DockerArrangementSplit findArrNodeParent( DockerArrangementNode root, DockerArrangementNode child )
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
