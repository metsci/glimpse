package com.metsci.glimpse.plot.timeline.layout;

import java.awt.Font;

import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.plot.StackedPlot2D.PlotInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfoImpl.TimeToolTipHandler;
import com.metsci.glimpse.plot.timeline.listener.DataAxisMouseListener1D;

public interface TimePlotInfo extends PlotInfo
{
    public void setTimeToolTipHandler( TimeToolTipHandler toolTipHandler );

    public DataAxisMouseListener1D getDataAxisMouseListener( );

    public void setBorderColor( float[] rgba );

    public void setBorderWidth( float width );

    public void setLabelBorderColor( float[] rgba );

    public void setLabelBorderWidth( float width );

    public void setLabelText( String text );

    public void setLabelColor( float[] rgba );

    public void setAxisColor( float[] rgba );

    public void setAxisFont( Font font );

    public void setBackgroundColor( float[] rgba );

    public GlimpseLayout getLabelLayout( );

    public BackgroundPainter getBackgroundPainter( );

    public GridPainter getGridPainter( );

    public NumericXYAxisPainter getAxisPainter( );

    public SimpleTextPainter getLabelPainter( );

    public BorderPainter getBorderPainter( );

    public BorderPainter getLabelBorderPainter( );

    public StackedTimePlot2D getStackedTimePlot( );

    public DelegatePainter getDataPainter( );

    public void addPainter( GlimpsePainter painter );

    public void removePainter( GlimpsePainter painter );

    @Override
    public TaggedAxis1D getCommonAxis( );

    @Override
    public TaggedAxis1D getCommonAxis( GlimpseTargetStack stack );
}
