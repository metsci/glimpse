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

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.painter.track.TrackPainter;
import com.metsci.glimpse.plot.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Demonstrates use of StackedTimePlot2D to create a vertical timeline axis
 * with lineplots stacked horizontally, each with an independent x axis.<p>
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.stacked.HorizontalTimelinePlotExample
 */
public class VerticalTimelinePlotExample extends HorizontalTimelinePlotExample
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new VerticalTimelinePlotExample( ) );
    }

    @Override
    protected StackedTimePlot2D createPlot( )
    {
        return new StackedTimePlot2D( Orientation.HORIZONTAL, Epoch.currentTime( ) );
    }

    @Override
    protected void addData( TrackPainter painter, Epoch epoch, double data, TimeStamp time )
    {
        painter.addPoint( 1, 0, data, epoch.fromTimeStamp( time ), time.toPosixMillis( ) );
    }

    @Override
    protected void setBounds( TimePlotInfo chart )
    {
        Axis1D axis = chart.getLayout( ).getAxis( ).getAxisX( );
        axis.setMin( -20.0 );
        axis.setMax( 20.0 );
    }
}
