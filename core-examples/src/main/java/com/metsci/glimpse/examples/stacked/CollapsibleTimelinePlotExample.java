package com.metsci.glimpse.examples.stacked;

import java.util.Collection;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D.GroupInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.BlackAndWhiteLookAndFeel;

public class CollapsibleTimelinePlotExample extends HorizontalTimelinePlotExample
{
    public static void main( String[] args ) throws Exception
    {
        Example example = Example.showWithSwing( new CollapsibleTimelinePlotExample( ) );

        // set a black and white look and feel
        example.getCanvas( ).setLookAndFeel( new BlackAndWhiteLookAndFeel( ) );
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

            // add a label to the group
            group.setLabelText( "Group Name" );

            // draw horizontal labels in the upper left corner of the label area
            row.getLabelPainter( ).setHorizontalPosition( HorizontalPosition.Left );
            row.getLabelPainter( ).setVerticalPosition( VerticalPosition.Top );
            row.getLabelPainter( ).setHorizontalLabels( true );

            // use larger label font
            row.getLabelPainter( ).setText( "Label Here" );
            row.getLabelPainter( ).setFont( FontUtils.getDefaultPlain( 12 ), true );

            // show vertical lines instead of horizontal lines on all plots
            row.getGridPainter( ).setShowHorizontalLines( false );
            row.getGridPainter( ).setShowVerticalLines( true );

            // make grid lines solid instead of dotted
            row.getGridPainter( ).setDotted( false );

            // don't draw top or bottom border lines
            row.getBorderPainter( ).setDrawBottom( false );
            row.getBorderPainter( ).setDrawTop( false );
        }

        return plot;
    }

    protected StackedTimePlot2D createPlot( )
    {
        return new CollapsibleTimePlot2D( );
    }
}
