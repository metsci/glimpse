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
package com.metsci.glimpse.examples.stacked;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.animate.DragManager;
import com.metsci.glimpse.plot.timeline.group.GroupInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;

public class NestedTimelinePlotExample extends HorizontalTimelinePlotExample
{
    public static void main( String[] args ) throws Exception
    {
        Example example = Example.showWithSwing( new NestedTimelinePlotExample( ) );
        
        new DragManager( ( CollapsibleTimePlot2D ) example.getLayout( ), example.getManager( ) );
    }

    @Override
    protected StackedTimePlot2D createPlot( )
    {
        return new CollapsibleTimePlot2D( );
    }

    @Override
    public StackedTimePlot2D getLayout( )
    {
        final CollapsibleTimePlot2D plot = ( CollapsibleTimePlot2D ) super.getLayout( );

        TimePlotInfo speedPlot = plot.getTimePlot( "speed-plot-1-id" );
        speedPlot.setLabelText( "Speed" );

        TimePlotInfo viscPlot = plot.getTimePlot( "viscosity-plot-2-id" );
        viscPlot.setLabelText( "Visc" );

        TimePlotInfo plot3 = plot.createTimePlot( );
        plot3.setLabelText( "Plot 3" );

        TimePlotInfo plot4 = plot.createTimePlot( );
        plot4.setLabelText( "Plot 4" );

        TimePlotInfo plot5 = plot.createTimePlot( );
        plot5.setLabelText( "Plot 5" );

        GroupInfo group5 = plot.createGroup( plot4 );
        group5.setLabelText( "Group 5" );

        GroupInfo group1 = plot.createGroup( );
        group1.setLabelText( "Group 1" );
        group1.addChildPlot( speedPlot );
        group1.addChildPlot( group5 );

        GroupInfo group4 = plot.createGroup( plot5 );
        group4.setLabelText( "Group 4" );

        GroupInfo group2 = plot.createGroup( );
        group2.setLabelText( "Group 2" );
        group2.addChildPlot( group1 );
        group2.addChildPlot( viscPlot );
        group2.addChildPlot( group4 );

        GroupInfo group3 = plot.createGroup( plot3 );
        group3.setLabelText( "Group 3" );

        plot.setIndentSubplots( true );

        return plot;
    }
}
