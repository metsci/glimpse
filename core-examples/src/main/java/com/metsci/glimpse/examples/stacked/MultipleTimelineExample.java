package com.metsci.glimpse.examples.stacked;

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
