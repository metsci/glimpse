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
import static com.metsci.glimpse.docking.MiscUtils.reversed;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.metsci.glimpse.docking.DockingWindow;
import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.View;
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
     * Restore maximized tiles in newly created windows.
     */
    public static void restoreMaximizedTilesInNewWindows( Collection<? extends ViewDestination> viewDestinations )
    {
        Set<DockingWindow> newWindows = new LinkedHashSet<>( );
        for ( ViewDestination dest : viewDestinations )
        {
            if ( dest.createdWindow != null )
            {
                newWindows.add( dest.createdWindow );
            }
        }

        Set<Tile> maximizedNewTiles = new LinkedHashSet<>( );
        for ( ViewDestination dest : viewDestinations )
        {
            if ( dest.createdTile != null && dest.planTile != null && dest.planTile.isMaximized )
            {
                maximizedNewTiles.add( dest.createdTile );
            }
        }

        for ( Tile tile : maximizedNewTiles )
        {
            DockingWindow window = getAncestorOfClass( DockingWindow.class, tile );
            if ( newWindows.contains( window ) )
            {
                window.docker( ).maximizeLeaf( tile );
            }
        }
    }

    /**
     * Order newly created windows according to planned stacking order.
     */
    public static List<DockingWindow> newWindowsBackToFront( Collection<? extends ViewDestination> viewDestinations, GroupArrangement planArr )
    {
        List<DockingWindow> result = new ArrayList<>( );

        // Stack planned new windows in front of existing windows, in plan order
        Map<FrameArrangement,DockingWindow> plannedNewWindows = new LinkedHashMap<>( );
        for ( ViewDestination dest : viewDestinations )
        {
            if ( dest.createdWindow != null && dest.planWindow != null )
            {
                plannedNewWindows.put( dest.planWindow, dest.createdWindow );
            }
        }
        for ( FrameArrangement windowArr : reversed( planArr.frameArrs ) )
        {
            DockingWindow window = plannedNewWindows.get( windowArr );
            if ( window != null )
            {
                result.add( window );
            }
        }

        // Stack unplanned new windows in front of existing windows
        for ( ViewDestination dest : viewDestinations )
        {
            if ( dest.createdWindow != null && dest.planWindow == null )
            {
                result.add( dest.createdWindow );
            }
        }

        return result;
    }

    public static DockerArrangementNode toArrNode( MultiSplitPane.Node node, Map<DockerArrangementNode,Component> components )
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

                if ( components != null )
                {
                    components.put( arrTile, c );
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
            arrSplit.childA = toArrNode( split.childA, components );
            arrSplit.childB = toArrNode( split.childB, components );

            if ( components != null )
            {
                components.put( arrSplit, split.component );
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
