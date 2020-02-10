/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.core.examples.timeline;

import static com.jogamp.opengl.GLProfile.GL3bc;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.core.support.QuickUtils.swingInvokeLater;

import java.util.TimeZone;

import com.metsci.glimpse.core.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.core.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.core.plot.timeline.layout.TimelineInfo;
import com.metsci.glimpse.core.support.settings.OceanLookAndFeel;

public class MultipleTimelineExample
{
    public static void main( String[] args )
    {
        swingInvokeLater( ( ) ->
        {
            StackedTimePlot2D plot = new CollapsibleTimelinePlotExample( ).getPlot( );

            // FIXME: Broken by commit 5aeb6a3 ... both timelines end up with the same labels

            // set up two timelines, one showing EST and one showing GMT time
            TimelineInfo gmtTimeline = plot.getDefaultTimeline( );
            gmtTimeline.getTimeZonePainter( ).setVerticalPosition( VerticalPosition.Top );
            gmtTimeline.getTimeZonePainter( ).setSizeText( "EST" );
            // don't show the date labels for the GMT timeline
            gmtTimeline.getAxisPainter( ).setShowDateLabels( false );
            gmtTimeline.setSize( 25 );

            // set up the additional timeline showing EST
            TimelineInfo estTimeline = plot.createTimeline( );
            estTimeline.setTimeZone( TimeZone.getTimeZone( "GMT-4:00" ) );
            estTimeline.getTimeZonePainter( ).setText( "EST" );
            estTimeline.getTimeZonePainter( ).setVerticalPosition( VerticalPosition.Top );
            estTimeline.getTimeZonePainter( ).setSizeText( "EST" );
            estTimeline.setSize( 35 );

            // create a window and show the plot
            quickGlimpseApp( "Multiple Timeline Plot Example", GL3bc, plot, new OceanLookAndFeel( ) );
        } );
    }
}
