package com.metsci.glimpse.layers.time;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.layers.time.TimeTrait.requireTimeTrait;
import static com.metsci.glimpse.layers.time.TimeZoneTrait.requireTimeZoneTrait;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition.Right;
import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static java.util.Arrays.asList;
import static javax.media.opengl.GLProfile.GL3;

import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

import javax.media.opengl.GLProfile;
import javax.swing.Icon;

import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.GlimpseCanvasView;
import com.metsci.glimpse.layers.ViewOption;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.util.var.Var;

public class TimelineView extends GlimpseCanvasView
{

    protected CollapsibleTimePlot2D plot;
    protected Var<ZoneId> timeZone;

    protected Map<Object,Integer> rowRefCounts;


    public TimelineView( ViewOption... viewOptions )
    {
        this( asList( viewOptions ) );
    }

    public TimelineView( Collection<? extends ViewOption> viewOptions )
    {
        super( GLProfile.get( GL3 ), viewOptions );

        this.title.set( "Timeline" );

        this.plot = null;
        this.timeZone = null;
        this.rowRefCounts = null;
    }

    @Override
    public Icon getIcon( )
    {
        return requireIcon( "open-icons/time.png" );
    }

    @Override
    protected void doContextReady( GlimpseContext context )
    {
        this.plot = new CollapsibleTimePlot2D( );
        this.plot.setTimeAxisMouseListener( new TaggedAxisMouseListener1D( ) );
        this.plot.setShowLabels( true );

        this.timeZone = new Var<>( ZoneId.of( "UTC" ), notNull );
        this.timeZone.addListener( true, ( ) ->
        {
            TimeZone tz = TimeZone.getTimeZone( this.timeZone.v( ) );
            this.plot.getTimeAxisLabelHandler( ).setTimeZone( tz );
            this.plot.getDefaultTimeline( ).setTimeZone( tz );
        } );

        this.rowRefCounts = new HashMap<>( );

        this.canvas.addLayout( this.plot );
    }

    @Override
    public void doInit( )
    {
        TimeTrait timeTrait = requireTimeTrait( this );
        this.plot.setEpoch( timeTrait.epoch );
        this.plot.getTimeAxis( ).setParent( timeTrait.axis );

        TimeZoneTrait timeZoneTrait = requireTimeZoneTrait( this );
        this.timeZone.setParent( timeZoneTrait.timeZone );
    }

    @Override
    protected void doContextDying( GlimpseContext context )
    {
        this.canvas.removeLayout( this.plot );

        this.timeZone.setParent( null );
        this.plot.dispose( context );

        this.plot = null;
        this.timeZone = null;
        this.rowRefCounts = null;
    }

    @Override
    public TimelineView copy( )
    {
        return new TimelineView( this.viewOptions );
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
        return this.acquirePlotRow( rowId, dataAxisText, AxisUnitConverters.identity, null );
    }

    public TimePlotInfo acquirePlotRow( Object rowId, String dataAxisText, AxisUnitConverter dataAxisUnits, Consumer<TimePlotInfo> initFn )
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

            if ( initFn != null )
            {
                initFn.accept( row );
            }
        }

        this.rowRefCounts.compute( rowId, ( k, v ) -> incRefCount( v ) );

        return row;
    }

    public void releaseRow( Object rowId )
    {
        this.releaseRow( rowId, false );
    }

    public void releaseRow( Object rowId, boolean keepRow )
    {
        Integer refCount = this.rowRefCounts.compute( rowId, ( k, v ) -> decRefCount( v ) );

        if ( refCount == null && !keepRow )
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
