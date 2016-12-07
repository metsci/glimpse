package com.metsci.glimpse.layers;

import static com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition.Right;
import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static com.metsci.glimpse.util.PredicateUtils.require;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
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
    public final Collection<Component> toolbarComponents;

    protected final CollapsibleTimePlot2D plot;

    protected final Map<Object,Integer> rowRefCounts;


    public LayeredTimeline( )
    {
        this.plot = new CollapsibleTimePlot2D( );
        this.plot.setShowLabels( true );

        this.rowRefCounts = new HashMap<>( );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );

        this.toolbarComponents = unmodifiableCollection( asList( ) );
    }

    public void init( LayeredScenario scenario )
    {
        Epoch epoch = require( scenario.timelineEpoch, notNull );
        LayeredTimelineBounds bounds = require( scenario.timelineInitBounds, notNull );

        plot.setEpoch( epoch );
        plot.setTimeAxisBounds( TimeStamp.fromPosixMillis( bounds.min_PMILLIS ), TimeStamp.fromPosixMillis( bounds.max_MILLIS ) );
    }

    public TaggedAxis1D timeAxis( )
    {
        return this.plot.getTimeAxis( );
    }

    public TimeAxisSelection selection( )
    {
        return new TimeAxisSelection( this.selectionMin_PMILLIS( ), this.selectionMax_PMILLIS( ), this.selectionCursor_PMILLIS( ) );
    }

    public long selectionMin_PMILLIS( )
    {
        Epoch epoch = this.plot.getEpoch( );
        return epoch.toPosixMillis( this.plot.getTimeSelectionMinTag( ).getValue( ) );
    }

    public long selectionMax_PMILLIS( )
    {
        Epoch epoch = this.plot.getEpoch( );
        return epoch.toPosixMillis( this.plot.getTimeSelectionMaxTag( ).getValue( ) );
    }

    public long selectionCursor_PMILLIS( )
    {
        Epoch epoch = this.plot.getEpoch( );
        return epoch.toPosixMillis( this.plot.getTimeSelectionTag( ).getValue( ) );
    }

    public EventPlotInfo acquireEventRow( Object rowId, String labelText )
    {
        EventPlotInfo row = this.plot.getEventPlot( rowId );
        if ( row == null )
        {
            row = this.plot.createEventPlot( rowId );

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
        }

        this.rowRefCounts.compute( rowId, ( k, v ) -> incRefCount( v ) );

        return row;
    }

    public TimePlotInfo acquirePlotRow( Object rowId, String dataAxisText )
    {
        return this.acquirePlotRow( rowId, dataAxisText, AxisUnitConverters.identity );
    }

    public TimePlotInfo acquirePlotRow( Object rowId, String dataAxisText, AxisUnitConverter dataAxisUnits )
    {
        TimePlotInfo row = this.plot.getTimePlot( rowId );
        if ( row == null )
        {
            row = this.plot.createTimePlot( rowId );

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
        }

        this.rowRefCounts.compute( rowId, ( k, v ) -> incRefCount( v ) );

        return row;
    }

    public void releaseRow( Object rowId )
    {
        Integer refCount = this.rowRefCounts.compute( rowId, ( k, v ) -> decRefCount( v ) );

        if ( refCount == null )
        {
            this.plot.removePlot( rowId );
        }
    }

    public static Integer incRefCount( Integer refCount )
    {
        return ( refCount == null ? 1 : refCount + 1 );
    }

    public static Integer decRefCount( Integer refCount )
    {
        return ( refCount >= 2 ? refCount - 1 : null );
    }

}
