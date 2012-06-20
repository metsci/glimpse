package com.metsci.glimpse.plot;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorYAxisPainter;

public class TaggedColorAxisPlot2D extends ColorAxisPlot2D
{
    @Override
    protected Axis1D createAxisZ( )
    {
        return new TaggedAxis1D( );
    }

    @Override
    protected AxisMouseListener createAxisMouseListenerZ( )
    {
        return new TaggedAxisMouseListener1D( );
    }

    @Override
    protected NumericAxisPainter createAxisPainterZ( AxisLabelHandler tickHandler )
    {
        return new TaggedPartialColorYAxisPainter( tickHandler );
    }
    
    @Override
    public TaggedAxis1D getAxisZ( )
    {
        return (TaggedAxis1D) axisZ;
    }
}
