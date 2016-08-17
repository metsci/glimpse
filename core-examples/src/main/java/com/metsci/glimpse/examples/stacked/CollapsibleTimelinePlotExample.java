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

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.animate.DragManager;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.event.Event;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.event.listener.EventPlotListener;
import com.metsci.glimpse.plot.timeline.event.listener.EventSelectionListener;
import com.metsci.glimpse.plot.timeline.group.GroupInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.OceanLookAndFeel;
import com.metsci.glimpse.util.io.StreamOpener;
import com.metsci.glimpse.util.units.time.Time;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class CollapsibleTimelinePlotExample extends HorizontalTimelinePlotExample
{
    private static final Logger logger = Logger.getLogger( CollapsibleTimelinePlotExample.class.getName( ) );

    public static void main( String[] args ) throws Exception
    {
        Example example = Example.showWithSwing( new CollapsibleTimelinePlotExample( ) );

        // use the ocean look and feel
        example.getCanvas( ).setLookAndFeel( new OceanLookAndFeel( ) );

        new DragManager( ( CollapsibleTimePlot2D ) example.getLayout( ) );
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

        // provide extra space for left hand side row labels
        plot.setLabelSize( 120 );

        plot.setIndentSize( 140 );
        plot.setIndentSubplots( true );

        plot.setShowLabels( true );

        for ( TimePlotInfo row : plot.getAllTimePlots( ) )
        {
            // create a collapsible/expandable group for each row
            GroupInfo group = plot.createGroup( String.format( "%s-group", row.getId( ) ), row );

            // set labels
            row.getLabelPainter( ).setText( "Label Here" );
            group.setLabelText( "Group Name" );

            setPlotLookAndFeel( row );
        }

        // create a 1D timeline to display event durations
        final EventPlotInfo events1 = plot.createEventPlot( "event-1" );
        EventPlotInfo events2 = plot.createEventPlot( "event-2" );
        EventPlotInfo events3 = plot.createEventPlot( "event-3" );

        // set the text label displayed to the left of each plot
        events1.setLabelText( "Snail Schedule" );
        events2.setLabelText( "Holidays" );
        events3.setLabelText( "Weather" );

        // set the event1 row to contain fixed height events (15 pixels tall)
        events1.setGrow( false );
        events1.setRowSize( 35 );

        // set the event2 row to contain variable size events which grow with available space
        events2.setGrow( true );

        events3.setGrow( false );

        // set additional display options on each plot
        setPlotLookAndFeel( events1 );
        setPlotLookAndFeel( events2 );
        setPlotLookAndFeel( events3 );

        // center the label for the event plots
        events1.getLabelPainter( ).setVerticalPosition( VerticalPosition.Center );
        events2.getLabelPainter( ).setVerticalPosition( VerticalPosition.Center );
        events3.getLabelPainter( ).setVerticalPosition( VerticalPosition.Center );

        // create a collapsible/expandable group for all the event plots
        GroupInfo group = plot.createGroup( "events-group", events1, events2, events3 );
        group.setLabelText( "Event Group" );

        // put the event group directly below the timeline
        group.setOrder( 100 );

        // order the event plots within the group
        events1.setOrder( 2 );
        events2.setOrder( 3 );
        events3.setOrder( 4 );

        // set default colors for the event plots
        events1.setDefaultEventBackgroundColor( GlimpseColor.getGreen( 0.6f ) );
        events1.setDefaultEventBorderColor( GlimpseColor.getGreen( ) );
        events3.setDefaultEventBackgroundColor( GlimpseColor.getCyan( 0.6f ) );
        events3.setDefaultEventBorderColor( GlimpseColor.getCyan( ) );

        Epoch e = plot.getEpoch( );
        TimeStamp t0 = e.toTimeStamp( 0 );

        // add some events and set their display characteristics
        Event e0 = events1.addEvent( "Wax Shell", t0, t0.add( Time.fromMinutes( 20 ) ) );
        e0.setShowBackground( false );
        events1.addEvent( "Spread Slime On Stuff", t0.add( Time.fromMinutes( 30 ) ), t0.add( Time.fromMinutes( 200 ) ) );
        Event e1 = events1.addEvent( "Chill", t0.add( Time.fromMinutes( 290 ) ), t0.add( Time.fromMinutes( 320 ) ) );
        e1.setBackgroundColor( GlimpseColor.getRed( 0.6f ) );
        e1.setBorderColor( GlimpseColor.getRed( ) );
        Event e2 = events1.addEvent( "Cloudy", t0.add( Time.fromMinutes( -200 ) ), t0.add( Time.fromMinutes( 100 ) ) );
        Event e3 = events1.addEvent( "Sunny", t0.add( Time.fromMinutes( 100 ) ), t0.add( Time.fromMinutes( 300 ) ) );
        Event e4 = events1.addEvent( "Wake Up", t0.subtract( Time.fromMinutes( 40 ) ) );

        // add some events to the other event timeline
        events2.addEvent( "Event 1", t0.add( Time.fromMinutes( -250 ) ), t0.add( Time.fromMinutes( -240 ) ) );
        events2.addEvent( "Event 2", t0.add( Time.fromMinutes( -220 ) ), t0.add( Time.fromMinutes( -200 ) ) );
        events2.addEvent( "Event 3", t0.add( Time.fromMinutes( -170 ) ), t0.add( Time.fromMinutes( -100 ) ) );

        events1.setAggregateNearbyEvents( true );
        events2.setAggregateNearbyEvents( true );

        // add constraints on how the user can adjust the various events
        e0.setEndTimeMoveable( false );
        e1.setResizeable( false );
        e2.setMinTimeSpan( Time.fromMinutes( 100 ) );
        e3.setMaxTimeSpan( Time.fromMinutes( 500 ) );

        // cause clicks which hit no events to deselect all selected events
        events1.getEventSelectionHandler( ).setClearSelectionOnClick( true );

        // make the "Cloudy" even unselectable
        e2.setSelectable( false );

        // fix the "Cloudy" event on row 2
        e2.setFixedRow( 2 );

        // recalculate the positions of events
        events1.validate( );

        // load icons into the texture atlas for the plot
        TextureAtlas atlas = plot.getTextureAtlas( );
        try
        {
            atlas.loadImage( "cloud", ImageIO.read( StreamOpener.fileThenResource.openForRead( "icons/fugue/weather-clouds.png" ) ) );
            atlas.loadImage( "sun", ImageIO.read( StreamOpener.fileThenResource.openForRead( "icons/fugue/weather.png" ) ) );
            atlas.loadImage( "glass", ImageIO.read( StreamOpener.fileThenResource.openForRead( "icons/fugue/glass.png" ) ) );
            atlas.loadImage( "alarm-clock", ImageIO.read( StreamOpener.fileThenResource.openForRead( "icons/fugue/alarm-clock-blue.png" ) ) );
        }
        catch ( IOException ex )
        {
            logWarning( logger, "Trouble loading icon.", ex );
        }

        // add icons to the events
        e1.setIconId( "glass" );
        e2.setIconId( "cloud" );
        e3.setIconId( "sun" );
        e4.setIconId( "alarm-clock" );

        // wrap the tool tips after 150 pixels
        plot.getTooltipPainter( ).setFixedWidth( 300 );

        // set tool tips on the events
        e0.setToolTipText( "Wax on. Wax off. Wax on. Wax off. Wax on. Wax off. Wax on. Wax off. Wax on. Wax off." );
        e1.setToolTipText( "The Riesling had an elegant, almost austere grip, redolent of creosote and with a corpulent, full-bodied bouquet." );
        e2.setToolTipText( "I wandered lonely as a cloud\nThat floats on high o'er vales and hills,\nWhen all at once I saw a crowd,\nA host, of golden daffodils;\nBeside the lake, beneath the trees,\nFluttering and dancing in the breeze.\n-William Wordsworth" );
        e3.setToolTipText( "NML Cygni = 1,650 Solar Radii" );
        e4.setToolTipText( "Suns up! Morning's Here!\nUp And At 'Em Engineer!\nHurry! Hurry! Load The Freight!\nTo The City! Can't Be Late!" );

        // add listeners to EventPlots
        events1.addEventPlotListener( new EventPlotListener( )
        {
            @Override
            public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
            {
                if ( !events.isEmpty( ) ) logInfo( logger, "Events Hovered: %s Time: %s", events, time );
            }

            @Override
            public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
            {
                if ( !events.isEmpty( ) ) logInfo( logger, "Events Clicked: %s Time: %s", events, time );
            }

            @Override
            public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
            {
                if ( !events.isEmpty( ) ) logInfo( logger, "Events Exited: %s Time: %s", events, time );
            }

            @Override
            public void eventsEntered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
            {
                if ( !events.isEmpty( ) ) logInfo( logger, "Events Entered: %s Time: %s", events, time );
            }

            @Override
            public void eventUpdated( GlimpseMouseEvent e, Event event )
            {
                logInfo( logger, "Event Updated: %s", event );
            }
        } );

        // add a listener for notifications of event selections and deselections
        plot.getEventSelectionHander( ).addEventSelectionListener( new EventSelectionListener( )
        {
            @Override
            public void eventsSelected( Set<Event> selectedEvents, Set<Event> deselectedEvents )
            {
                logInfo( logger, "Selected: %s%nDeselected: %s", selectedEvents, deselectedEvents );
            }
        } );

        // events may be selected by clicking on them (normally this has no visible effect)
        // here we cause the border of selected events to become thicker
        // the border or background color may also be set to change for selected events
        // in addition, client code may use the eventsSelected() callback of EventPlotListener
        // to make arbitrary display changes to selected events (in this case the client code
        // is also responsible for resetting the event display characteristics when it is deselected
        events1.getEventSelectionHandler( ).setSelectedEventBorderThickness( 3.0f );

        // replace the default StackedTimePlot2D mouse listener behavior with
        // the default tagged axis behavior (the selected area will not follow
        // the mouse, but the user can move it by clicking and dragging inside
        // the selected area)
        plot.setTimeAxisMouseListener( new TaggedAxisMouseListener1D( ) );

        // don't draw text indicating whether the selection is locked
        plot.getSelectedTimePainter( ).setShowLockedStatus( false );

        // add an axis listener which keeps the current time selection
        // equal to the max time selection
        plot.getTimeAxis( ).addAxisListener( new TaggedAxisListener1D( )
        {
            @Override
            public void tagsUpdated( TaggedAxis1D axis )
            {
                Tag s = plot.getTimeSelectionTag( );
                Tag m = plot.getTimeSelectionMaxTag( );

                if ( s != null && m != null && s.getValue( ) != m.getValue( ) )
                {
                    s.setValue( m.getValue( ) );
                    plot.getTimeAxis( ).validateTags( );
                }
            }
        } );

        return plot;
    }

    protected void setPlotLookAndFeel( TimePlotInfo row )
    {
        // draw horizontal labels in the upper left corner of the label area
        row.getLabelPainter( ).setHorizontalPosition( HorizontalPosition.Left );
        row.getLabelPainter( ).setVerticalPosition( VerticalPosition.Top );
        row.getLabelPainter( ).setHorizontalLabels( true );

        // use larger label font
        row.getLabelPainter( ).setFont( FontUtils.getDefaultPlain( 12 ), true );

        // show vertical lines instead of horizontal lines on all plots
        row.getGridPainter( ).setShowHorizontalLines( false );
        row.getGridPainter( ).setShowVerticalLines( true );
        row.getGridPainter( ).setVisible( true );

        // make grid lines solid instead of dotted
        row.getGridPainter( ).setDotted( false );

        // don't draw top or bottom border lines
        row.getBorderPainter( ).setDrawBottom( false );
        row.getBorderPainter( ).setDrawTop( false );
    }
}
