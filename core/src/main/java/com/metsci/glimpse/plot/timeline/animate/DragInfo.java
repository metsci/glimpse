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

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.stacked.StackedPlot2D;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.group.GroupInfo;

public class DragInfo
{
    public PlotInfo info;
    public GlimpseBounds bounds;
    public double size;
    public boolean top;
    public int indent;

    public DragInfo( PlotInfo info, GlimpseBounds bounds, double size )
    {
        this.info = info;
        this.bounds = bounds;
        this.size = size;
        this.setIndent( info );
    }

    public DragInfo( DragInfo drag, double size )
    {
        this.info = drag.info;
        this.bounds = drag.bounds;
        this.top = drag.top;
        this.indent = drag.indent;
        this.size = size;
    }

    public DragInfo( PlotInfo info, double size, boolean top )
    {
        this.info = info;
        this.size = size;
        this.top = top;
        this.setIndent( info );
    }

    protected void setIndent( PlotInfo info )
    {
        this.indent = 0;

        StackedPlot2D plot = info.getStackedPlot( );

        if ( plot instanceof CollapsibleTimePlot2D )
        {
            CollapsibleTimePlot2D collapsiblePlot = ( CollapsibleTimePlot2D ) plot;

            if ( collapsiblePlot.isIndentSubplots( ) && collapsiblePlot.getIndentSize( ) > 0 )
            {
                this.indent = collapsiblePlot.getIndentSize( ) * getDepth( info );
            }
        }
    }

    protected int getDepth( PlotInfo info )
    {
        int depth = 0;
        PlotInfo parent = info.getParent( );

        while ( parent != null )
        {
            parent = parent.getParent( );
            depth++;
        }

        return info instanceof GroupInfo ? depth : Math.max( 0, depth - 1 );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( info == null ) ? 0 : info.hashCode( ) );
        result = prime * result + ( top ? 1231 : 1237 );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        DragInfo other = ( DragInfo ) obj;
        if ( info == null )
        {
            if ( other.info != null ) return false;
        }
        else if ( !info.equals( other.info ) ) return false;
        if ( top != other.top ) return false;
        return true;
    }
}
