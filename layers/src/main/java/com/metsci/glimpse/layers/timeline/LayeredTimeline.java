package com.metsci.glimpse.layers.timeline;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.layers.timeline.LayeredTimelineConfig.requireTimelineConfig;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition.Right;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.Icon;

import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.layers.LayeredView;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public class LayeredTimeline extends LayeredView
{

    public final NewtSwingEDTGlimpseCanvas canvas;
    public final Collection<Component> toolbarComponents;

    protected final CollapsibleTimePlot2D plot;

    protected final Map<Object,Integer> rowRefCounts;


    public LayeredTimeline( )
    {
        this.title.set( "Timeline" );

        this.plot = new CollapsibleTimePlot2D( );
        this.plot.setShowLabels( true );

        this.rowRefCounts = new HashMap<>( );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );

        this.toolbarComponents = unmodifiableCollection( asList( ) );
    }

    @Override
    public Icon getIcon( )
    {
        return requireIcon( "LayeredTimeline/open-icons/time.png" );
    }

    @Override
    public Component getComponent( )
    {
        return this.canvas;
    }

    @Override
    public GLAutoDrawable getGLDrawable( )
    {
        return this.canvas.getGLDrawable( );
    }

    @Override
    public Collection<Component> getToolbarComponents( )
    {
        return this.toolbarComponents;
    }

    @Override
    public void init( )
    {
        LayeredTimelineConfig timelineConfig = requireTimelineConfig( this );
        this.plot.setEpoch( timelineConfig.epoch );
        this.plot.getTimeAxis( ).setParent( timelineConfig.axis );
    }

    @Override
    public LayeredTimeline createClone( )
    {
        return new LayeredTimeline( );
    }

    @Override
    protected void dispose( )
    {
        super.dispose( );
        this.canvas.dispose( );
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
