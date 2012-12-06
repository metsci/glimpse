package com.metsci.glimpse.examples.stacked;

import java.util.Collection;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D.GroupInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.Event;
import com.metsci.glimpse.plot.timeline.layout.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.OceanLookAndFeel;
import com.metsci.glimpse.util.units.time.Time;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class CollapsibleTimelinePlotExample extends HorizontalTimelinePlotExample
{
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
        EventPlotInfo events1 = plot.createEventPlot( "event-1" );
        EventPlotInfo events2 = plot.createEventPlot( "event-2" );
        EventPlotInfo events3 = plot.createEventPlot( "event-3" );

        events1.setLabelText( "Snail Schedule" );
        events2.setLabelText( "Weather" );
        events3.setLabelText( "Nap Time" );

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
        
        // add some events
        Epoch e = plot.getEpoch( );
        TimeStamp t0 = e.toTimeStamp( 0 );
        Event e1 = new Event( "e1", "Wax Shell", t0, t0.add( Time.fromMinutes( 20 ) ) );
        e1.setBackgroundColor( GlimpseColor.getGreen( 0.6f ) );
        e1.setBorderColor( GlimpseColor.getGreen( ) );

        events1.addEvent( e1 );
        
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
