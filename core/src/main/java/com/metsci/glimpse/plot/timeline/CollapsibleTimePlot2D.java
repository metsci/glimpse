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
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.stacked.PlotInfoImpl;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.group.GroupInfo;
import com.metsci.glimpse.plot.timeline.group.GroupInfoImpl;

public class CollapsibleTimePlot2D extends StackedTimePlot2D
{
    public CollapsibleTimePlot2D( )
    {
        super( Orientation.VERTICAL );
    }

    public CollapsibleTimePlot2D( Epoch epoch )
    {
        super( Orientation.VERTICAL, epoch );
    }

    public CollapsibleTimePlot2D( Epoch epoch, TaggedAxis1D commonAxis )
    {
        super( Orientation.VERTICAL, epoch, commonAxis );
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
            return ( GroupInfo ) getPlot( groupId );
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
            GroupInfo group = new GroupInfoImpl( this, plotInfo, subplots );
            stackedPlots.put( id, group );

            if ( isAutoValidate( ) ) validate( );

            return group;
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
    protected List<PlotInfo> getSortedPlots( Collection<PlotInfo> unsorted )
    {

        // remove children of groups from list of all plots
        List<PlotInfo> ungroupedPlots = new ArrayList<PlotInfo>( unsorted.size( ) );
        ungroupedPlots.addAll( unsorted );

        for ( GroupInfo group : getAllGroups( ) )
        {
            ungroupedPlots.removeAll( group.getChildPlots( ) );
        }

        ArrayList<PlotInfo> accumulator = new ArrayList<PlotInfo>( unsorted.size( ) );
        getSortedPlots0( ungroupedPlots, accumulator );

        return accumulator;

    }

    protected void getSortedPlots0( Collection<PlotInfo> toVisitUnsorted, List<PlotInfo> accumulator )
    {
        if ( toVisitUnsorted == null || toVisitUnsorted.isEmpty( ) ) return;

        List<PlotInfo> toVisitSorted = new ArrayList<PlotInfo>( );
        toVisitSorted.addAll( toVisitUnsorted );
        Collections.sort( toVisitSorted, PlotInfoImpl.getComparator( ) );

        for ( PlotInfo info : toVisitSorted )
        {
            accumulator.add( info );

            if ( info instanceof GroupInfo )
            {
                GroupInfo group = ( GroupInfo ) info;
                getSortedPlots0( group.getChildPlots( ), accumulator );
            }
        }
    }
}