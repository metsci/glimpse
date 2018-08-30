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
package com.metsci.glimpse.docking.group;

import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;

import java.awt.Component;
import java.util.Collection;
import java.util.Map;

import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.group.frame.DockingFrame;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;

public class DockingGroupUtils
{

    /**
     * Restore selected views in newly created tiles.
     */
    public static void restoreSelectedViewsInNewTiles( Collection<? extends ViewDestinationBase> viewDestinations )
    {
        for ( ViewDestinationBase dest : viewDestinations )
        {
            if ( dest.createdTile != null && dest.planTile != null )
            {
                View view = dest.createdTile.view( dest.planTile.selectedViewId );
                dest.createdTile.selectView( view );
            }
        }
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

    public static DockerArrangementNode toArrNode( MultiSplitPane.Node node, Map<DockerArrangementNode,Component> componentsMap )
    {
        if ( node instanceof MultiSplitPane.Leaf )
        {
            MultiSplitPane.Leaf leaf = ( MultiSplitPane.Leaf ) node;
            Component c = leaf.component;
            if ( c instanceof Tile )
            {
                Tile tile = ( Tile ) c;
                DockerArrangementTile arrTile = new DockerArrangementTile( );

                for ( int i = 0; i < tile.numViews( ); i++ )
                {
                    String viewId = tile.view( i ).viewId;
                    arrTile.viewIds.add( viewId );
                }

                View selectedView = tile.selectedView( );
                arrTile.selectedViewId = ( selectedView == null ? null : selectedView.viewId );

                arrTile.isMaximized = leaf.isMaximized;

                if ( componentsMap != null )
                {
                    componentsMap.put( arrTile, c );
                }

                return arrTile;
            }
            else
            {
                return null;
            }
        }
        else if ( node instanceof MultiSplitPane.Split )
        {
            MultiSplitPane.Split split = ( MultiSplitPane.Split ) node;
            DockerArrangementSplit arrSplit = new DockerArrangementSplit( );
            arrSplit.arrangeVertically = split.arrangeVertically;
            arrSplit.splitFrac = split.splitFrac;
            arrSplit.childA = toArrNode( split.childA, componentsMap );
            arrSplit.childB = toArrNode( split.childB, componentsMap );

            if ( componentsMap != null )
            {
                componentsMap.put( arrSplit, split.component );
            }

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
