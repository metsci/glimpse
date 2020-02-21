/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.docking.group;

import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.docking.MiscUtils.union;
import static com.metsci.glimpse.docking.SplitPane.computeChildSizes;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.util.Collections.unmodifiableSet;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

    public static void removeView( GroupArrangement groupArr, String viewId )
    {
        DockerArrangementTile tile = findArrTileContaining( groupArr, viewId );
        if ( tile != null )
        {
            tile.viewIds.remove( viewId );

            if ( tile.viewIds.isEmpty( ) )
            {
                pruneEmpty( groupArr );
            }
            else if ( equal( tile.selectedViewId, viewId ) )
            {
                tile.selectedViewId = tile.viewIds.get( 0 );
            }
        }
    }

    public static FrameArrangement findFrameArrContaining( GroupArrangement groupArr, String viewId )
    {
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            DockerArrangementTile tile = findArrTileContaining( frameArr.dockerArr, viewId );
            if ( tile != null )
            {
                return frameArr;
            }
        }
        return null;
    }

    public static DockerArrangementTile findArrTileContaining( GroupArrangement groupArr, String viewId )
    {
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            DockerArrangementTile tile = findArrTileContaining( frameArr.dockerArr, viewId );
            if ( tile != null )
            {
                return tile;
            }
        }
        return null;
    }

    public static DockerArrangementTile findArrTileContaining( DockerArrangementNode arrNode, String viewId )
    {
        for ( DockerArrangementTile tile : findTiles( arrNode ) )
        {
            if ( tile.viewIds.contains( viewId ) )
            {
                return tile;
            }
        }
        return null;
    }

    public static void pruneEmpty( GroupArrangement groupArr )
    {
        List<FrameArrangement> newFrameArrs = new ArrayList<>( );
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            frameArr.dockerArr = pruneEmpty( frameArr.dockerArr );
            if ( frameArr.dockerArr != null )
            {
                newFrameArrs.add( frameArr );
            }
        }
        groupArr.frameArrs = newFrameArrs;
    }

    public static DockerArrangementNode pruneEmpty( DockerArrangementNode arrNode )
    {
        if ( arrNode instanceof DockerArrangementTile )
        {
            DockerArrangementTile tile = ( DockerArrangementTile ) arrNode;
            return ( tile.viewIds.isEmpty( ) ? null : tile );
        }
        else if ( arrNode instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit split = ( DockerArrangementSplit ) arrNode;
            split.childA = pruneEmpty( split.childA );
            split.childB = pruneEmpty( split.childB );
            if ( split.childA != null && split.childB != null )
            {
                return split;
            }
            else if ( split.childA != null )
            {
                return split.childA;
            }
            else if ( split.childB != null )
            {
                return split.childB;
            }
            else
            {
                return null;
            }
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

    public static FrameArrangement findFrameArrContaining( GroupArrangement groupArr, DockerArrangementNode node )
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

    public static FrameArrangement findLargestFrameArr( GroupArrangement groupArr )
    {
        FrameArrangement best = null;
        long bestArea = 0;

        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            long area = frameArea( frameArr );
            if ( area > bestArea )
            {
                best = frameArr;
                bestArea = area;
            }
        }

        return best;
    }

    protected static long frameArea( FrameArrangement frameArr )
    {
        long w = frameWidth( frameArr );
        long h = frameHeight( frameArr );
        return ( w * h );
    }

    protected static int frameWidth( FrameArrangement frameArr )
    {
        // TODO: Use the bounds of the display for the frame's location -- see javadocs for getMaximumWindowBounds()
        Rectangle screenBounds = getLocalGraphicsEnvironment( ).getMaximumWindowBounds( );
        return ( frameArr.isMaximizedHoriz ? screenBounds.width : frameArr.width );
    }

    protected static int frameHeight( FrameArrangement frameArr )
    {
        // TODO: Use the bounds of the display for the frame's location -- see javadocs for getMaximumWindowBounds()
        Rectangle screenBounds = getLocalGraphicsEnvironment( ).getMaximumWindowBounds( );
        return ( frameArr.isMaximizedVert ? screenBounds.height : frameArr.height );
    }

    public static DockerArrangementTile findLargestArrTile( GroupArrangement groupArr )
    {
        TileAndArea best = null;
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            TileAndArea bestInFrame = findLargestTile( frameArr );
            if ( best == null || ( bestInFrame != null && bestInFrame.area > best.area ) )
            {
                best = bestInFrame;
            }
        }
        return ( best == null ? null : best.tile );
    }

    protected static class TileAndArea
    {
        public final DockerArrangementTile tile;
        public final long area;

        public TileAndArea( DockerArrangementTile tile, long width, long height )
        {
            this( tile, width*height );
        }

        public TileAndArea( DockerArrangementTile tile, long area )
        {
            this.tile = tile;
            this.area = area;
        }
    }

    protected static TileAndArea findLargestTile( FrameArrangement frameArr )
    {
        TileAndArea maximized = findMaximizedTile( frameArr );
        if ( maximized != null )
        {
            return maximized;
        }
        else
        {
            return findLargestUnmaximizedTile( frameArr );
        }
    }

    protected static TileAndArea findMaximizedTile( FrameArrangement frameArr )
    {
        for ( DockerArrangementTile tile : findTiles( frameArr.dockerArr ) )
        {
            if ( tile.isMaximized )
            {
                return new TileAndArea( tile, frameArea( frameArr ) );
            }
        }
        return null;
    }

    protected static TileAndArea findLargestUnmaximizedTile( FrameArrangement frameArr )
    {
        return findLargestUnmaximizedTile( frameArr.dockerArr, frameWidth( frameArr ), frameHeight( frameArr ) );
    }

    protected static TileAndArea findLargestUnmaximizedTile( DockerArrangementNode root, int rootWidth, int rootHeight )
    {
        if ( root instanceof DockerArrangementTile )
        {
            DockerArrangementTile tile = ( DockerArrangementTile ) root;
            return new TileAndArea( tile, rootWidth, rootHeight );
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

            TileAndArea resultA = findLargestUnmaximizedTile( split.childA, widthA, heightA );
            TileAndArea resultB = findLargestUnmaximizedTile( split.childB, widthB, heightB );

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
                return ( resultA.area >= resultB.area ? resultA : resultB );
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
