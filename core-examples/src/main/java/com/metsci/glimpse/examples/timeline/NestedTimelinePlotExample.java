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
package com.metsci.glimpse.examples.timeline;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.animate.DragManager;
import com.metsci.glimpse.plot.timeline.group.GroupInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.color.GlimpseColor;

public class NestedTimelinePlotExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example example = Example.showWithSwing( new NestedTimelinePlotExample( ) );

        // allow the user to rearrange plots by dragging on their labels
        DragManager.attach( ( StackedTimePlot2D ) example.getLayout( ) );
    }

    @Override
    public StackedTimePlot2D getLayout( )
    {
        final CollapsibleTimePlot2D plot = new CollapsibleTimePlot2D( );

        plot.getDefaultTimeline( ).setAxisColor( GlimpseColor.getBlack( ) );

        plot.setShowLabels( true );
        plot.setLabelSize( 30 );

        plot.setIndentSubplots( true );
        plot.setIndentSize( 30 );

        GroupInfo topGroup1 = plot.createGroup( );
        topGroup1.setLabelText( "Top Group 1" );
        topGroup1.setShowDivider( false );

        GroupInfo topGroup2 = plot.createGroup( );
        topGroup2.setLabelText( "Top Group 1" );
        topGroup2.setShowDivider( false );

        GroupInfo midGroup1 = plot.createGroup( );
        midGroup1.setLabelText( "Mid Group 1" );
        midGroup1.setShowDivider( false );
        topGroup1.addChildPlot( midGroup1 );

        TimePlotInfo plot1 = plot.createTimePlot( );
        plot1.setLabelText( "Bottom Plot 1" );
        midGroup1.addChildPlot( plot1 );

        TimePlotInfo plot2 = plot.createTimePlot( );
        plot2.setLabelText( "Bottom Plot 2" );
        midGroup1.addChildPlot( plot2 );

        GroupInfo midGroup2 = plot.createGroup( );
        midGroup2.setLabelText( "Mid Group 2" );
        midGroup2.setShowDivider( false );
        topGroup1.addChildPlot( midGroup2 );

        TimePlotInfo plot3 = plot.createTimePlot( );
        plot3.setLabelText( "Bottom Plot 3" );
        midGroup2.addChildPlot( plot3 );

        GroupInfo midGroup3 = plot.createGroup( );
        midGroup3.setLabelText( "Mid Group 3" );
        midGroup3.setShowDivider( false );
        topGroup1.addChildPlot( midGroup3 );

        TimePlotInfo plot4 = plot.createTimePlot( );
        plot4.setLabelText( "Bottom Plot 4" );
        midGroup3.addChildPlot( plot4 );

        TimePlotInfo plot5 = plot.createTimePlot( );
        plot5.setLabelText( "Bottom Plot 5" );
        midGroup3.addChildPlot( plot5 );

        GroupInfo midGroup4 = plot.createGroup( );
        midGroup4.setLabelText( "Mid Group 4" );
        midGroup4.setShowDivider( false );
        topGroup2.addChildPlot( midGroup4 );

        TimePlotInfo plot6 = plot.createTimePlot( );
        plot6.setLabelText( "Bottom Plot 6" );
        midGroup4.addChildPlot( plot6 );

        return plot;
    }
}
