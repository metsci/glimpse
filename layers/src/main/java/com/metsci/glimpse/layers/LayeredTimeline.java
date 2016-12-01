package com.metsci.glimpse.layers;

import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition.Right;

import javax.swing.JToolBar;

import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.util.var.Var;

public class LayeredTimeline
{

    public final NewtSwingEDTGlimpseCanvas canvas;
    public final JToolBar toolbar;
    public final View view;

    /**
     * NOTE: Do not call {@code LayeredTimeline.plot.setEpoch()} directly -- it
     * will not notify listeners. Use {@code LayeredTimeline.epoch.set()} instead.
     */
    public final CollapsibleTimePlot2D plot;

    public final Var<LayeredScenario> scenario;


    public LayeredTimeline( Var<LayeredScenario> scenario )
    {
        this.plot = new CollapsibleTimePlot2D( );
        this.plot.setShowLabels( true );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );

        this.toolbar = newToolbar( true );

        this.view = new View( "timelineView", this.canvas, "Timeline", false, null, requireIcon( "LayeredTimeline/open-icons/time.png" ), this.toolbar );

        this.scenario = scenario;
        this.scenario.addListener( true, ( ) ->
        {
            plot.setEpoch( this.scenario.v( ).timelineEpoch );
        } );
    }

    public EventPlotInfo addEventRow( String labelText )
    {
        EventPlotInfo row = this.plot.createEventPlot( );

        row.setGrow( false );

        GridPainter gridPainter = row.getGridPainter( );
        gridPainter.setShowVerticalLines( true );
        gridPainter.setShowHorizontalLines( false );
        gridPainter.setVisible( true );
        row.getLayout( ).setZOrder( gridPainter, -1 );

        SimpleTextPainter labelPainter = row.getLabelPainter( );
        labelPainter.setHorizontalLabels( true );
        labelPainter.setFont( FontUtils.getDefaultBold( 12 ), true );

        row.setFont( FontUtils.getDefaultPlain( 12 ), true );
        row.setLabelText( labelText );

        return row;
    }

    public TimePlotInfo addPlotRow( String dataAxisText )
    {
        return this.addPlotRow( dataAxisText, AxisUnitConverters.identity );
    }

    public TimePlotInfo addPlotRow( String dataAxisText, AxisUnitConverter dataAxisUnits )
    {
        TimePlotInfo row = this.plot.createTimePlot( );

        GridPainter gridPainter = row.getGridPainter( );
        gridPainter.setShowVerticalLines( true );
        gridPainter.setShowHorizontalLines( false );
        gridPainter.setVisible( true );
        row.getLayout( ).setZOrder( gridPainter, -1 );

        SimpleTextPainter labelPainter = row.getLabelPainter( );
        labelPainter.setText( dataAxisText );
        labelPainter.setHorizontalLabels( false );
        labelPainter.setHorizontalPadding( 9 );
        labelPainter.setHorizontalPosition( Right );
        labelPainter.setFont( FontUtils.getDefaultPlain( 12 ), true );

        row.getAxisPainter( ).getLabelHandlerY( ).setAxisUnitConverter( dataAxisUnits );

        return row;
    }

}
