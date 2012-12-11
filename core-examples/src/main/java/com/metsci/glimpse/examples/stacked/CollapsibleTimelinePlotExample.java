package com.metsci.glimpse.examples.stacked;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.metsci.glimpse.event.mouse.GlimpseMouseAdapter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D.GroupInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.event.Event;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo.EventPlotListener;
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
    }

    @Override
    public StackedTimePlot2D getLayout( )
    {
        CollapsibleTimePlot2D plot = ( CollapsibleTimePlot2D ) super.getLayout( );

        // provide extra space for left hand side row labels
        plot.setLabelSize( 120 );

        Collection<TimePlotInfo> rows = plot.getAllTimePlots( );

        for ( TimePlotInfo row : rows )
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

        events1.setLabelText( "Snail Schedule" );
        events2.setLabelText( "Holidays" );
        events3.setLabelText( "Weather" );

        setPlotLookAndFeel( events1 );
        setPlotLookAndFeel( events2 );
        setPlotLookAndFeel( events3 );

        events1.getLabelPainter( ).setVerticalPosition( VerticalPosition.Center );
        events2.getLabelPainter( ).setVerticalPosition( VerticalPosition.Center );
        events3.getLabelPainter( ).setVerticalPosition( VerticalPosition.Center );

        // create a collapsible/expandable group for all the event plots
        GroupInfo group = plot.createGroup( "events-group", events1, events2, events3 );
        group.setLabelText( "Event Group" );

        // put the event group directly below the timeline
        group.setOrder( 100 );
        events1.setOrder( 2 );
        events2.setOrder( 3 );
        events3.setOrder( 1 );

        // set default colors for the event plots
        events1.setBackgroundColor( GlimpseColor.getGreen( 0.6f ) );
        events1.setBorderColor( GlimpseColor.getGreen( ) );
        events3.setBackgroundColor( GlimpseColor.getCyan( 0.6f ) );
        events3.setBorderColor( GlimpseColor.getCyan( ) );

        Epoch e = plot.getEpoch( );
        TimeStamp t0 = e.toTimeStamp( 0 );

        // add some events
        Event e0 = events1.addEvent( "Wax Shell", t0, t0.add( Time.fromMinutes( 20 ) ) );
        events1.addEvent( "Spread Slime On Stuff", t0.add( Time.fromMinutes( 30 ) ), t0.add( Time.fromMinutes( 200 ) ) );

        Event e1 = events1.addEvent( "Chill", t0.add( Time.fromMinutes( 290 ) ), t0.add( Time.fromMinutes( 320 ) ) );
        e1.setBackgroundColor( GlimpseColor.getRed( 0.6f ) );
        e1.setBorderColor( GlimpseColor.getRed( ) );

        Event e2 = events1.addEvent( "Cloudy", t0.add( Time.fromMinutes( -200 ) ), t0.add( Time.fromMinutes( 100 ) ) );
        Event e3 = events1.addEvent( "Sunny", t0.add( Time.fromMinutes( 100 ) ), t0.add( Time.fromMinutes( 300 ) ) );

        Event e4 = events1.addEvent( "Wake Up", t0.subtract( Time.fromMinutes( 40 ) ) );

        // add icons to the events
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

        e4.setIconId( "alarm-clock" );
        e0.setIconId( "glass" );
        e2.setIconId( "cloud" );
        e3.setIconId( "sun" );

        // add listeners to EventPlots
        events1.addEventPlotListener( new EventPlotListener( )
        {
            @Override
            public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
            {
                System.out.println( "eventsHovered: " + events );
            }

            @Override
            public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
            {
                System.out.println( "eventClicked: " + events );
            }

            @Override
            public void eventsUpdated( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
            {
                System.out.println( "eventUpdated: " + events );
            }
        } );

        // use middle click to switch between stacking and not stacking events (just for demonstration purposes)
        plot.getOverlayLayout( ).addGlimpseMouseListener( new GlimpseMouseAdapter( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                if ( event.isButtonDown( MouseButton.Button2 ) )
                {
                    events1.setStackOverlappingEvents( !events1.isStackOverlappingEvents( ) );
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

    protected StackedTimePlot2D createPlot( )
    {
        return new CollapsibleTimePlot2D( );
    }
}
