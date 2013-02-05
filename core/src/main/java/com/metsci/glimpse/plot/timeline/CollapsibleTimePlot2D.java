/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.plot.timeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.group.GroupInfo;
import com.metsci.glimpse.plot.timeline.group.GroupInfoImpl;
import com.metsci.glimpse.plot.timeline.group.GroupLayoutDataUpdater;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.atlas.TextureAtlas;

public class CollapsibleTimePlot2D extends StackedTimePlot2D
{
    protected Map<PlotInfo, GroupInfo> childParentMap;

    public CollapsibleTimePlot2D( )
    {
        super( Orientation.VERTICAL );

        this.childParentMap = new HashMap<PlotInfo, GroupInfo>( );
    }

    public CollapsibleTimePlot2D( Epoch epoch )
    {
        super( Orientation.VERTICAL, epoch );

        this.childParentMap = new HashMap<PlotInfo, GroupInfo>( );
    }

    @Override
    public PlotInfo createPlot( Object id, Axis1D axis )
    {
        PlotInfo info = super.createPlot( id, axis );
        wrapLayoutDataUpdater( info );
        return info;
    }

    @Override
    protected TimePlotInfo createTimePlot0( PlotInfo plotInfo )
    {
        TimePlotInfo info = super.createTimePlot0( plotInfo );
        wrapLayoutDataUpdater( info );
        return info;
    }

    @Override
    protected EventPlotInfo createEventPlot0( PlotInfo plotInfo, TextureAtlas atlas )
    {
        EventPlotInfo info = super.createEventPlot0( plotInfo, atlas );
        wrapLayoutDataUpdater( info );
        return info;
    }

    // wrap the layout updater with a group-aware updater which will
    // set the size of the plot to 0 if its parent plot is collapsed
    protected void wrapLayoutDataUpdater( PlotInfo info )
    {
        // wrap the layout updater with a group-aware updater which will
        // set the size of the plot to 0 if its parent plot is collapsed
        LayoutDataUpdater delegate = info.getLayoutDataUpdater( );
        info.setLayoutDataUpdater( new GroupLayoutDataUpdater( this, info, delegate ) );
    }

    /**
     * Create a collapsible/expandable group of plots.
     */
    public GroupInfo createGroup( PlotInfo... subplots )
    {
        return createGroup( UUID.randomUUID( ), subplots );
    }

    public GroupInfo createGroup( Object id, PlotInfo... subplots )
    {
        LinkedList<PlotInfo> list = new LinkedList<PlotInfo>( );
        for ( int i = 0; i < subplots.length; i++ )
            list.add( subplots[i] );

        return createGroup( id, list );
    }

    public GroupInfo getGroupById( Object groupId )
    {
        this.lock.lock( );
        try
        {
            return (GroupInfo) getPlot( groupId );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public GroupInfo getGroupForChild( PlotInfo childPlot )
    {
        this.lock.lock( );
        try
        {
            return childParentMap.get( childPlot );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public GroupInfo createGroup( Object id, Collection<? extends PlotInfo> subplots )
    {
        this.lock.lock( );
        try
        {
            PlotInfo plotInfo = createPlot0( id, new Axis1D( ) );
            GroupInfo group = new GroupInfoInner( this, plotInfo, subplots );
            stackedPlots.put( id, group );
            for ( PlotInfo sub : subplots )
            {
                childParentMap.put( sub, group );
            }
            validate( );
            return group;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    protected void addChildPlot0( GroupInfo group, PlotInfo child )
    {
        this.lock.lock( );
        try
        {
            childParentMap.put( child, group );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    protected void removeChildPlot0( GroupInfo group, PlotInfo child )
    {
        this.lock.lock( );
        try
        {
            childParentMap.remove( child );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public Collection<GroupInfo> getAllGroups( )
    {
        this.lock.lock( );
        try
        {
            List<GroupInfo> list = new LinkedList<GroupInfo>( );

            for ( PlotInfo plot : getAllPlots( ) )
            {
                if ( plot instanceof GroupInfo ) list.add( ( GroupInfo ) plot );
            }

            return list;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    @Override
    protected LayoutDataUpdater createTimelineLayoutDataUpdater( PlotInfo info )
    {
        return new LayoutDataUpdaterImpl( info, 1 )
        {
            protected int growingPlotCount( List<PlotInfo> list )
            {
                int count = 0;
                for ( PlotInfo info : list )
                {
                    if ( info.getSize( ) < 0 )
                    {
                        count++;
                    }

                    // the children of non-expanded groups don't count
                    // they will be counted above, so remove them from the count here
                    if ( info instanceof GroupInfo )
                    {
                        GroupInfo groupInfo = ( GroupInfo ) info;

                        if ( !groupInfo.isExpanded( ) )
                        {
                            for ( PlotInfo childInfo : groupInfo.getChildPlots( ) )
                            {
                                if ( childInfo.getSize( ) < 0 )
                                {
                                    count--;
                                }
                            }
                        }
                    }
                }

                return count;
            }

            @Override
            public int getSizePixels( List<PlotInfo> list, int index )
            {
                return growingPlotCount( list ) == 0 ? -1 : this.info.getSize( );
            }
        };
    }

    @Override
    protected List<PlotInfo> getSortedPlots( Collection<PlotInfo> unsorted )
    {
        // remove children of groups from list of all plots
        List<PlotInfo> ungroupedPlots = new ArrayList<PlotInfo>( );
        ungroupedPlots.addAll( unsorted );

        for ( GroupInfo group : getAllGroups( ) )
        {
            ungroupedPlots.removeAll( group.getChildPlots( ) );
        }

        List<PlotInfo> sortedPlots = new ArrayList<PlotInfo>( );
        sortedPlots.addAll( ungroupedPlots );
        Collections.sort( sortedPlots, PlotInfoImpl.getComparator( ) );

        List<PlotInfo> sortedPlotsCopy = new ArrayList<PlotInfo>( );
        sortedPlotsCopy.addAll( sortedPlots );

        int totalChildren = 0;
        for ( int i = 0; i < sortedPlotsCopy.size( ); i++ )
        {
            PlotInfo plot = sortedPlotsCopy.get( i );

            if ( plot instanceof GroupInfo )
            {
                GroupInfo group = ( GroupInfo ) plot;
                List<PlotInfo> childPlots = new ArrayList<PlotInfo>( );
                childPlots.addAll( group.getChildPlots( ) );
                Collections.sort( childPlots, PlotInfoImpl.getComparator( ) );
                sortedPlots.addAll( i + totalChildren + 1, childPlots );
                totalChildren += childPlots.size( );
            }
        }

        return sortedPlots;
    }

    public class GroupInfoInner extends GroupInfoImpl
    {
        public GroupInfoInner( CollapsibleTimePlot2D plot, final PlotInfo group, Collection<? extends PlotInfo> subplots )
        {
            super( plot, group, subplots );
        }

        @Override
        public void addChildPlot( PlotInfo childPlot )
        {
            addChildPlot0( this, childPlot );
            super.addChildPlot( childPlot );
        }

        @Override
        public void removeChildPlot( PlotInfo childPlot )
        {
            removeChildPlot0( this, childPlot );
            super.removeChildPlot( childPlot );
        }
    }
}