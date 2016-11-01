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
package com.metsci.glimpse.plot.timeline.animate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.context.TargetStackUtil;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.stacked.StackedPlot2D;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.group.GroupInfo;
import com.metsci.glimpse.plot.timeline.group.GroupUtilities;

/**
 * Helper methods for {@link DragManager}. Most methods which perform simple operations and
 * do not mutate DragManager's state should be placed here. This simplifies the DragManager
 * code so that it only contains high level drag control methods (what to do when the drag
 * starts, ends, the user moves the mouse, etc...).
 *
 * @author ulman
 */
public class DragUtils
{
    static PlotInfo getTopPlot( Orientation orientation, List<PlotInfo> list )
    {
        if ( orientation == Orientation.VERTICAL )
        {
            return list.get( 0 );
        }
        else
        {
            return list.get( list.size( ) - 1 );
        }
    }

    static int getSpacerSize( Orientation orientation, List<PlotInfo> sortedPlots, List<DragInfo> list )
    {
        PlotInfo topPlot = getTopPlot( orientation, sortedPlots );
        DragInfo topDrag = list.get( 0 ); //XXX does this also need to use index size-1 for HORIZONTAL plots?

        int size = getTotalSize( list );

        // the first PlotInfo in the StackedPlot2D does not use
        // its plot spacing (because there are no plots above it)
        if ( topPlot.equals( topDrag.info ) )
        {
            size -= topPlot.getPlotSpacing( );
        }

        return size;
    }

    static int getIndex( List<PlotInfo> list, PlotInfo search )
    {
        for ( int i = 0; i < list.size( ); i++ )
        {
            if ( list.get( i ).equals( search ) ) return i;
        }

        return 0;
    }

    static GroupInfo findParent( Collection<PlotInfo> list, PlotInfo child )
    {
        for ( PlotInfo info : list )
        {
            if ( info instanceof GroupInfo )
            {
                GroupInfo group = ( GroupInfo ) info;
                if ( group.getChildPlots( ).contains( child ) )
                {
                    return group;
                }
            }
        }

        return null;
    }

    static List<DragInfo> getDragInfoList( Orientation orientation, GlimpseMouseEvent event, List<PlotInfo> list )
    {
        List<DragInfo> dragList = new ArrayList<DragInfo>( list.size( ) );
        GlimpseTargetStack stack = popToStackedPlot( event.getTargetStack( ) );

        for ( PlotInfo info : list )
        {
            GlimpseBounds bound = getBounds( stack, info );
            int size = getSize( orientation, bound );
            dragList.add( new DragInfo( info, bound, size ) );
        }

        return dragList;
    }

    // returns a list of all descendants (children, their children, etc...)
    // of the provided group. The list will be sorted by order number.
    static List<PlotInfo> getSortedDescendants( GroupInfo group )
    {
        List<PlotInfo> list = new ArrayList<PlotInfo>( );
        GroupUtilities.getSortedPlots( Collections.<PlotInfo> singleton( group ), list );
        return list;
    }

    // calculates the current (on screen) size of the given PlotInfo
    static GlimpseBounds getBounds( GlimpseMouseEvent event, PlotInfo info )
    {
        return getBounds( popToStackedPlot( event.getTargetStack( ) ), info );
    }

    static GlimpseBounds getBounds( GlimpseTargetStack stack, PlotInfo info )
    {
        return info.getBaseLayout( ).getTargetBounds( stack );
    }

    static int getTotalSize( List<DragInfo> list )
    {
        int total = 0;

        for ( int i = 0; i < list.size( ); i++ )
        {
            DragInfo drag = list.get( i );
            total += drag.size + drag.info.getPlotSpacing( );
        }

        return total;
    }

    static int getSize( Orientation orientation, GlimpseBounds bounds )
    {
        if ( bounds == null ) return 0;

        if ( orientation == Orientation.VERTICAL )
        {
            return bounds.getHeight( );
        }
        else
        {
            return bounds.getWidth( );
        }
    }

    static int getCoordinate( Orientation orientation, GlimpseMouseEvent e )
    {
        if ( orientation == Orientation.VERTICAL )
        {
            return e.getY( );
        }
        else
        {
            return e.getX( );
        }
    }

    static int getTop( Orientation orientation, GlimpseBounds bounds )
    {
        if ( orientation == Orientation.VERTICAL )
        {
            return bounds.getY( ) + bounds.getHeight( );
        }
        else
        {
            return bounds.getX( ) + bounds.getWidth( );
        }
    }

    static int getBottom( Orientation orientation, GlimpseBounds bounds )
    {
        if ( orientation == Orientation.VERTICAL )
        {
            return bounds.getY( );
        }
        else
        {
            return bounds.getX( );
        }
    }

    // pop off all GlimpseTargets until a StackedPlot2D instance is reached
    static GlimpseTargetStack popToStackedPlot( GlimpseTargetStack stack )
    {
        GlimpseTargetStack copy = TargetStackUtil.newTargetStack( stack );

        while ( copy.getSize( ) > 1 )
        {
            copy.pop( );
            GlimpseTarget target = copy.getTarget( );
            if ( target instanceof StackedPlot2D ) break;
        }

        return copy;
    }
}
