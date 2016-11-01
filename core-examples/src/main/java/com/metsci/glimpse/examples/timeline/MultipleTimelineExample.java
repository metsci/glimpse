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

import java.util.TimeZone;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.layout.TimelineInfo;

public class MultipleTimelineExample extends CollapsibleTimelinePlotExample
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new MultipleTimelineExample( ) );
    }

    @Override
    public StackedTimePlot2D getLayout( )
    {
        StackedTimePlot2D plot = super.getLayout( );

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

        return plot;
    }
}
