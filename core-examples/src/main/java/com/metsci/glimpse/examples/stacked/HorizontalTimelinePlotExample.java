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

import java.util.TimeZone;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.track.TrackPainter;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.animate.DragManager;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.OceanLookAndFeel;
import com.metsci.glimpse.util.units.time.Time;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Demonstrates use of StackedTimePlot2D to create a horizontal timeline axis
 * with lineplots stacked vertically on top, each with an independent y axis.<p>
 *
 * Lineplots can be added and removed with
 * {@link com.metsci.glimpse.plot.timeline.StackedTimePlot2D#createChartPlot( String )}
 * and {@link com.metsci.glimpse.plot.timeline.StackedTimePlot2D#deletePlot( String )}.
 * When a new chart is created, a {@link com.metsci.glimpse.plot.timeline.layout.TimePlotInfo}
 * reference is provided, allowing addition of {@code GlimpsePainters} and modification of
 * chart size, ordering, and coloring.
 *
 * @author ulman
 */
public class HorizontalTimelinePlotExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example example = Example.showWithSwing( new HorizontalTimelinePlotExample( ) );

        // set a blue color scheme look and feel for the plot
        example.getCanvas( ).setLookAndFeel( new OceanLookAndFeel( ) );

        // allow the user to rearrange plots by dragging on their labels
        DragManager.attach( ( StackedTimePlot2D ) example.getLayout( ) );
    }

    @Override
    public StackedTimePlot2D getLayout( )
    {
        // create a timeline with plot areas arranged in a vertical line
        StackedTimePlot2D plot = createPlot( );

        // set the time zone for the timeline to local time
        plot.getDefaultTimeline( ).setTimeZone( TimeZone.getDefault( ) );

        // calculate some TimeStamps representing the selected time range and initial extents of the timeline
        Epoch epoch = plot.getEpoch( );
        TimeStamp selectionMinTime = epoch.getTimeStamp( );
        TimeStamp selectionMaxTime = selectionMinTime.add( Time.fromHours( 3 ) );
        TimeStamp axisMinTime = selectionMinTime;
        TimeStamp axisMaxTime = selectionMaxTime.add( Time.fromHours( 20 ) );

        // set the selected time range
        plot.setTimeSelection( selectionMinTime, selectionMaxTime );

        // set the overall bounds of the timeline
        plot.setTimeAxisBounds( axisMinTime, axisMaxTime );

        // add spacing between stacked plots
        plot.setPlotSpacing( 2 );
        plot.setBorderSize( 4 );

        // create two plots (which by default will appear to the right of the timeline)
        // the returned ChartLayoutInfo reference can be used to add GlimpsePainters to
        // the plot area or customize its coloring and appearance
        TimePlotInfo plot1 = plot.createTimePlot( "speed-plot-1-id" );
        TimePlotInfo plot2 = plot.createTimePlot( "viscosity-plot-2-id" );

        // give the plots custom text labels indicating value being plotted and units
        plot1.setLabelText( "Snail Speed (furlongs per fornight)" );
        plot2.setLabelText( "Snail Slime Viscosity (pascal-seconds)" );

        // turn on timeline labels
        plot.setLabelSize( 30 );
        plot.setShowLabels( true );

        // display vertical labels
        plot1.getLabelPainter( ).setHorizontalLabels( false );
        plot2.getLabelPainter( ).setHorizontalLabels( false );

        setChartData( plot1, epoch, axisMinTime, axisMaxTime );
        setChartData( plot2, epoch, axisMinTime, axisMaxTime );

        // add mouse listeners to the GlimpseLayouts of the plots
        addMouseListener( epoch, plot1 );
        addMouseListener( epoch, plot2 );

        return plot;
    }

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

                // get the x axes of the GlimpseLayout in which the mouse event occurred
                Axis1D axisTime = event.getAxis2D( ).getAxisX( );

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

                // get the y axes of the GlimpseLayout in which the mouse event occurred
                Axis1D axisY = event.getAxis2D( ).getAxisY( );

                // convert from pixel space to axis value space using the axis
                double axisValueX = axisTime.screenPixelToValue( pixelX );

                // use the StackedTimePlot2D Epoch to further convert the axis
                // value into an absolute TimeStamp
                TimeStamp axisValueTime = epoch.toTimeStamp( axisValueX );

                // convert from pixel space to axis value space using the axis
                double axisValueY = axisY.screenPixelToValue( axisY.getSizePixels( ) - pixelY );

                // print the values calculated above
                System.out.printf( "PixelX: %d PixelY: %d ValueX: %f ValueY: %f Time: %s Selection Min: %s Selection Max: %s %n", pixelX, pixelY, axisValueX, axisValueY, axisValueTime, timeSelectionMinTime, timeSelectionMaxTime );
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
            }
        } );
    }

    protected void setChartData( TimePlotInfo chart, Epoch epoch, TimeStamp startTime, TimeStamp endTime )
    {
        // create a painter to display data on the plot
        TrackPainter painter = new TrackPainter( );

        // set colors and sizes for the painter
        painter.setPointColor( 1, GlimpseColor.getGreen( ) );
        painter.setPointSize( 1, 5.0f );
        painter.setShowLines( 1, false );

        // generate some random data
        double end = endTime.toPosixSeconds( );
        double time = startTime.toPosixSeconds( );
        double step = ( end - time ) / 200;
        float valueX = 0.0f;

        while ( time < end )
        {
            time += step;
            valueX += ( float ) ( Math.random( ) * 2.0 - 1.0 );

            addData( painter, epoch, valueX, TimeStamp.fromPosixSeconds( time ) );
        }

        // add the painter to the layout
        chart.addPainter( painter );

        // adjust the axis bounds to fit the data
        setBounds( chart );
    }

    protected StackedTimePlot2D createPlot( )
    {
        // set the epoch and orientation for the timeline
        // time values will be stored relative to the epoch
        return new StackedTimePlot2D( Orientation.VERTICAL, new Epoch( TimeStamp.currentTime( ) ) );
    }

    protected void addData( TrackPainter painter, Epoch epoch, double data, TimeStamp time )
    {
        painter.addPoint( 1, 0, epoch.fromTimeStamp( time ), data, time.toPosixMillis( ) );
    }

    protected void setBounds( TimePlotInfo chart )
    {
        Axis1D axis = chart.getBaseLayout( ).getAxis( ).getAxisY( );
        axis.setMin( -20.0 );
        axis.setMax( 20.0 );
    }
}
