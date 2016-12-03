package com.metsci.glimpse.layers;

import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition.Right;
import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static com.metsci.glimpse.util.PredicateUtils.require;

import javax.swing.JToolBar;

import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class LayeredTimeline
{

    public final NewtSwingEDTGlimpseCanvas canvas;
    public final JToolBar toolbar;

    /**
     * NOTE: Do not call {@code LayeredTimeline.plot.setEpoch()} directly -- it
     * will not notify listeners. Use {@code LayeredTimeline.epoch.set()} instead.
     */
    public final CollapsibleTimePlot2D plot;


    public LayeredTimeline( )
    {
        this.plot = new CollapsibleTimePlot2D( );
        this.plot.setShowLabels( true );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );

        this.toolbar = newToolbar( true );
    }

    public void init( LayeredScenario scenario )
    {
        Epoch epoch = require( scenario.timelineEpoch, notNull );
        LayeredTimelineBounds bounds = require( scenario.timelineInitBounds, notNull );

        plot.setEpoch( epoch );
        plot.setTimeAxisBounds( TimeStamp.fromPosixMillis( bounds.min_PMILLIS ), TimeStamp.fromPosixMillis( bounds.max_MILLIS ) );
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

    public TimePlotInfo getPlotRow( Object rowId, String dataAxisText )
    {
        return this.getPlotRow( rowId, dataAxisText, AxisUnitConverters.identity );
    }

    public TimePlotInfo getPlotRow( Object rowId, String dataAxisText, AxisUnitConverter dataAxisUnits )
    {
        TimePlotInfo row = this.plot.getTimePlot( rowId );
        if ( row == null )
        {
            row = this.plot.createTimePlot( rowId );
        }

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
