package com.metsci.glimpse.examples.stacked;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.animate.DragManager;
import com.metsci.glimpse.plot.timeline.group.GroupInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;

public class NestedTimelinePlotExample extends HorizontalTimelinePlotExample
{
    public static void main( String[] args ) throws Exception
    {
        Example example = Example.showWithSwing( new NestedTimelinePlotExample( ) );
        
        new DragManager( ( CollapsibleTimePlot2D ) example.getLayout( ), example.getManager( ) );
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

        TimePlotInfo speedPlot = plot.getTimePlot( "speed-plot-1-id" );
        speedPlot.setLabelText( "Speed" );

        TimePlotInfo viscPlot = plot.getTimePlot( "viscosity-plot-2-id" );
        viscPlot.setLabelText( "Visc" );

        TimePlotInfo plot3 = plot.createTimePlot( );
        plot3.setLabelText( "Plot 3" );

        TimePlotInfo plot4 = plot.createTimePlot( );
        plot4.setLabelText( "Plot 4" );

        TimePlotInfo plot5 = plot.createTimePlot( );
        plot5.setLabelText( "Plot 5" );

        GroupInfo group5 = plot.createGroup( plot4 );
        group5.setLabelText( "Group 5" );

        GroupInfo group1 = plot.createGroup( );
        group1.setLabelText( "Group 1" );
        group1.addChildPlot( speedPlot );
        group1.addChildPlot( group5 );

        GroupInfo group4 = plot.createGroup( plot5 );
        group4.setLabelText( "Group 4" );

        GroupInfo group2 = plot.createGroup( );
        group2.setLabelText( "Group 2" );
        group2.addChildPlot( group1 );
        group2.addChildPlot( viscPlot );
        group2.addChildPlot( group4 );

        GroupInfo group3 = plot.createGroup( plot3 );
        group3.setLabelText( "Group 3" );

        plot.setIndentSubplots( true );

        return plot;
    }
}
