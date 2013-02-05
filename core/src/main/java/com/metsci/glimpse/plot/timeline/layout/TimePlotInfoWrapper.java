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
import com.metsci.glimpse.plot.stacked.PlotInfoWrapper;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.listener.DataAxisMouseListener1D;

public class TimePlotInfoWrapper extends PlotInfoWrapper implements TimePlotInfo
{
    protected TimePlotInfo info;

    public TimePlotInfoWrapper( TimePlotInfo info )
    {
        super( info );
        this.info = info;
    }

    @Override
    public TaggedAxis1D getCommonAxis( )
    {
        return info.getCommonAxis( );
    }

    @Override
    public TaggedAxis1D getCommonAxis( GlimpseTargetStack stack )
    {
        return info.getCommonAxis( stack );
    }

    @Override
    public void setTimeToolTipHandler( TimeToolTipHandler toolTipHandler )
    {
        info.setTimeToolTipHandler( toolTipHandler );
    }

    @Override
    public DataAxisMouseListener1D getDataAxisMouseListener( )
    {
        return info.getDataAxisMouseListener( );
    }

    @Override
    public void setBorderColor( float[] rgba )
    {
        info.setBorderColor( rgba );
    }

    @Override
    public void setBorderWidth( float width )
    {
        info.setBorderWidth( width );
    }

    @Override
    public void setLabelBorderColor( float[] rgba )
    {
        info.setLabelBorderColor( rgba );
    }

    @Override
    public void setLabelBorderWidth( float width )
    {
        info.setLabelBorderWidth( width );
    }

    @Override
    public void setLabelText( String text )
    {
        info.setLabelText( text );
    }

    @Override
    public void setLabelColor( float[] rgba )
    {
        info.setLabelColor( rgba );
    }

    @Override
    public void setAxisColor( float[] rgba )
    {
        info.setAxisColor( rgba );
    }

    @Override
    public void setAxisFont( Font font )
    {
        info.setAxisFont( font );
    }

    @Override
    public void setBackgroundColor( float[] rgba )
    {
        info.setBackgroundColor( rgba );
    }

    @Override
    public GlimpseLayout getLabelLayout( )
    {
        return info.getLabelLayout( );
    }

    @Override
    public BackgroundPainter getBackgroundPainter( )
    {
        return info.getBackgroundPainter( );
    }

    @Override
    public GridPainter getGridPainter( )
    {
        return info.getGridPainter( );
    }

    @Override
    public NumericXYAxisPainter getAxisPainter( )
    {
        return info.getAxisPainter( );
    }

    @Override
    public SimpleTextPainter getLabelPainter( )
    {
        return info.getLabelPainter( );
    }

    @Override
    public BorderPainter getBorderPainter( )
    {
        return info.getBorderPainter( );
    }

    @Override
    public BorderPainter getLabelBorderPainter( )
    {
        return info.getLabelBorderPainter( );
    }

    @Override
    public StackedTimePlot2D getStackedTimePlot( )
    {
        return info.getStackedTimePlot( );
    }

    @Override
    public DelegatePainter getDataPainter( )
    {
        return info.getDataPainter( );
    }

    @Override
    public void addPainter( GlimpsePainter painter )
    {
        info.addPainter( painter );
    }

    @Override
    public void removePainter( GlimpsePainter painter )
    {
        info.removePainter( painter );
    }

}
