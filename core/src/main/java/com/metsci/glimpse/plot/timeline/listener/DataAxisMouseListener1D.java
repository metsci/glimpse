package com.metsci.glimpse.plot.timeline.listener;

import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.plot.StackedPlot2D.PlotInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;

public class DataAxisMouseListener1D extends AxisMouseListener1D
{
    protected static final int AXIS_SIZE = 28;

    protected StackedTimePlot2D plot;
    protected PlotInfo info;

    protected boolean axisSelected;

    protected int axisSize = AXIS_SIZE;

    public DataAxisMouseListener1D( StackedTimePlot2D plot, PlotInfo info )
    {
        this.info = info;
        this.plot = plot;
    }

    public void setAxisSize( int size )
    {
        this.axisSize = size;
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent e )
    {
        GlimpseAxisLayout1D layout = getAxisLayout( e );
        if ( layout == null ) return;

        if ( e.isAnyButtonDown( ) && this.axisSelected )
        {
            super.mouseMoved( e );
            e.setHandled( true );
        }
    }

    @Override
    public void mousePressed( GlimpseMouseEvent e )
    {
        plot.setSelectedPlot( info );

        this.axisSelected = isAxisSelected( e );

        if ( this.axisSelected )
        {
            super.mousePressed( e );
            e.setHandled( true );
        }
    }

    protected boolean isAxisSelected( GlimpseMouseEvent e )
    {
        GlimpseAxisLayout1D layout = getAxisLayout( e );
        if ( layout == null ) return false;

        if ( e.isKeyDown( ModifierKey.Shift ) ) return true;
        if ( layout.isHorizontal( ) && e.getY( ) < axisSize ) return true;
        if ( !layout.isHorizontal( ) && e.getX( ) < axisSize ) return true;

        return false;
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent e )
    {
        super.mouseReleased( e );
    }

    @Override
    public void mouseWheelMoved( GlimpseMouseEvent e )
    {
        this.axisSelected = isAxisSelected( e );

        if ( this.axisSelected )
        {
            super.mouseWheelMoved( e );
            e.setHandled( true );
        }
    }
}
