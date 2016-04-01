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
package com.metsci.glimpse.plot.timeline;

import static com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation.VERTICAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.group.GroupInfo;
import com.metsci.glimpse.plot.timeline.group.GroupInfoImpl;
import com.metsci.glimpse.plot.timeline.group.GroupUtilities;

public class CollapsibleTimePlot2D extends StackedTimePlot2D
{
    protected boolean indentSubplots = false;
    protected int indentSize = -1;
    protected int maxLevel = 0;

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

    public boolean isIndentSubplots( )
    {
        return indentSubplots;
    }

    public void setIndentSubplots( boolean indent )
    {
        this.indentSubplots = indent;
        this.validate( );
    }

    public void setIndentSize( int size )
    {
        this.indentSize = size;
        this.validate( );
    }

    public int getIndentSize( )
    {
        return indentSize < 0 ? getLabelSize( ) : this.indentSize;
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

            addPlotInfoListeners( group );

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

    public List<PlotInfo> getUngroupedPlots( )
    {
        this.lock.lock( );
        try
        {
            return getUngroupedPlots( getAllPlots( ) );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public int getMaxLevel( )
    {
        this.lock.lock( );
        try
        {
            return this.maxLevel;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    @Override
    public void validateLayout( )
    {
        updateRemovedChildren0( );

        super.validateLayout( );
    }

    @Override
    protected void setRowColumnConstraints( )
    {
        int tempIndentSize = getIndentSize( );

        if ( this.indentSubplots )
        {
            this.maxLevel = setIndentLevel0( ) + 1;
            setRowColumnConstraints( maxLevel, tempIndentSize );
        }
        else
        {
            this.maxLevel = 1;
            resetIndentLevel0( 0 );
            setRowColumnConstraints( 0, tempIndentSize );
        }
    }

    @Override
    protected List<PlotInfo> getSortedPlots( Collection<PlotInfo> unsorted )
    {
        List<PlotInfo> ungroupedPlots = getUngroupedPlots( unsorted );
        ArrayList<PlotInfo> accumulator = new ArrayList<PlotInfo>( unsorted.size( ) );
        GroupUtilities.getSortedPlots( ungroupedPlots, accumulator );

        return accumulator;
    }

    @Override
    public int getOverlayLayoutOffsetX( )
    {
        return orient == VERTICAL ? getMaxLevel( ) * getIndentSize( ) : 0;
    }

    @Override
    public int getOverlayLayoutOffsetY2( )
    {
        return orient == VERTICAL ? 0 : getMaxLevel( ) * getIndentSize( );
    }

    protected List<PlotInfo> getUngroupedPlots( Collection<PlotInfo> unsorted )
    {
        // remove children of groups from list of all plots
        List<PlotInfo> ungroupedPlots = new ArrayList<PlotInfo>( unsorted.size( ) );
        ungroupedPlots.addAll( unsorted );

        for ( GroupInfo group : getAllGroups( ) )
        {
            ungroupedPlots.removeAll( group.getChildPlots( ) );
        }

        return ungroupedPlots;
    }

    protected void updateRemovedChildren0( )
    {
        // GroupInfos aren't directly told about deletion of PlotInfo
        // so when validate is called, update groups by removing deleted PlotInfo
        for ( GroupInfo group : getAllGroups( ) )
        {
            for ( PlotInfo plot : Lists.newArrayList( group.getChildPlots( ) ) )
            {
                if ( getPlot( plot.getId( ) ) == null )
                {
                    group.removeChildPlot( plot );
                }
            }
        }
    }

    protected void resetIndentLevel0( int level )
    {
        for ( PlotInfo info : getAllPlots( ) )
        {
            info.setIndentLevel( 0 );
        }
    }

    protected int setIndentLevel0( )
    {
        List<PlotInfo> ungroupedPlots = getUngroupedPlots( getAllPlots( ) );
        return setIndentLevel0( ungroupedPlots, -1 );
    }

    protected int setIndentLevel0( Collection<PlotInfo> plots, int level )
    {
        int maxLevel = level;
        for ( PlotInfo info : plots )
        {
            if ( info instanceof GroupInfo )
            {
                GroupInfo group = ( GroupInfo ) info;
                info.setIndentLevel( level + 1 );
                int levelReached = setIndentLevel0( group.getChildPlots( ), level + 1 );
                if ( levelReached > maxLevel ) maxLevel = levelReached;
            }
            else
            {
                info.setIndentLevel( level );
            }
        }
        return maxLevel;
    }
}