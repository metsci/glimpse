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
package com.metsci.glimpse.examples.stacked;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.time.RelativeTimeAxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.painter.track.TrackPainter;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.animate.DragManager;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.settings.OceanLookAndFeel;
import com.metsci.glimpse.util.units.time.Time;
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
        Example example = Example.showWithSwing( new VerticalTimelinePlotExample( ) );

        // set a blue color scheme look and feel for the plot
        example.getCanvas( ).setLookAndFeel( new OceanLookAndFeel( ) );

        // allow the user to rearrange plots by dragging on their labels
        DragManager.attach( ( StackedTimePlot2D ) example.getLayout( ) );
    }

    @Override
    public StackedTimePlot2D getLayout( )
    {
        StackedTimePlot2D plot = super.getLayout( );

        // Set a tick labeler which labels timeline tick marks by the hours/days elapsed since a reference date
        final RelativeTimeAxisLabelHandler handler = new RelativeTimeAxisLabelHandler( plot.getEpoch( ).getTimeStamp( ).add( -Time.fromHours( 100 ) ) );
        handler.setFuturePositive( false );
        
        plot.setTimeAxisLabelHandler( handler );
        
        // Update the reference time in a loop to animate the time labels
        new Thread( )
        {
            @Override
            public void run( )
            {
                while( true )
                {
                    handler.setReferenceTime( handler.getReferenceTime( ).add( Time.fromSeconds( 10 ) ) );
                    try { Thread.sleep( 10 ); } catch ( InterruptedException e ) { e.printStackTrace(); }
                }
            }
        }.start( );
        
        plot.setPlotSpacing( 20 );

        // display horizontal labels
        for ( TimePlotInfo info : plot.getAllTimePlots( ) )
        {
            info.getLabelPainter( ).setHorizontalLabels( true );
        }

        return plot;
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
        Axis1D axis = chart.getOrthogonalAxis( );
        axis.setMin( -20.0 );
        axis.setMax( 20.0 );
    }

    @Override
    protected void addMouseListener( final Epoch epoch, final TimePlotInfo plot1 )
    {
        plot1.getLayout( ).addGlimpseMouseListener( new GlimpseMouseListener( )
        {
            @Override
            public void mouseEntered( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseExited( GlimpseMouseEvent event )
            {
            }

            @SuppressWarnings( "unused" )
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                // get the pixel location of the click (relative to the
                // upper left corner of the GlimpseLayout in which the
                // mouse event occurred)
                int pixelX = event.getX( );
                int pixelY = event.getY( );

                // get the y axes of the GlimpseLayout in which the mouse event occurred
                Axis1D axisTime = event.getAxis2D( ).getAxisY( );

                // another way to get the same time axis, getting the time axis from the
                // TimePlotInfo has the advantage of automatically casting to a TaggedAxis1D,
                // which allows us to access the selected time range
                TaggedAxis1D axisTaggedTime = plot1.getCommonAxis( event.getTargetStack( ) );

                // the StackedTimePlot2D allows access to the time selection region
                StackedTimePlot2D parent = plot1.getStackedTimePlot( );
                Tag timeSelectionMin = parent.getTimeSelectionMinTag( );
                Tag timeSelectionMax = parent.getTimeSelectionMaxTag( );

                // alternatively, we can get the tags directly from the TaggedAxis1D if we know
                // their String identifiers, which StackedTimePlot2D provides as public fields
                Tag timeSelectionAlternateMin = axisTaggedTime.getTag( StackedTimePlot2D.MIN_TIME );
                Tag timeSelectionAlternateMax = axisTaggedTime.getTag( StackedTimePlot2D.MAX_TIME );

                // use the StackedTimePlot2D Epoch to convert the Tag values into absolute TimeStamps
                TimeStamp timeSelectionMinTime = epoch.toTimeStamp( timeSelectionMin.getValue( ) );
                TimeStamp timeSelectionMaxTime = epoch.toTimeStamp( timeSelectionMax.getValue( ) );

                // get the x axes of the GlimpseLayout in which the mouse event occurred
                Axis1D axisX = event.getAxis2D( ).getAxisX( );

                // convert from pixel space to axis value space using the axis
                double axisValueY = axisTime.screenPixelToValue( pixelY );

                // use the StackedTimePlot2D Epoch to further convert the axis
                // value into an absolute TimeStamp
                TimeStamp axisValueTime = epoch.toTimeStamp( axisValueY );

                // convert from pixel space to axis value space using the axis
                double axisValueX = axisX.screenPixelToValue( axisX.getSizePixels( ) - pixelX );

                // print the values calculated above
                System.out.printf( "PixelX: %d PixelY: %d ValueX: %f ValueY: %f Time: %s Selection Min: %s Selection Max: %s %n", pixelX, pixelY, axisValueX, axisValueY, axisValueTime, timeSelectionMinTime, timeSelectionMaxTime );
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
            }
        } );
    }
}
