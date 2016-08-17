package com.metsci.glimpse.axis.painter;

import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;

public abstract class NumericLabelHandlerAxisPainter extends NumericAxisPainter
{
    protected AxisLabelHandler ticks;

    public NumericLabelHandlerAxisPainter( AxisLabelHandler ticks )
    {
        this.ticks = ticks;
    }

    public void setAxisLabel( String label )
    {
        this.ticks.setAxisLabel( label );
    }

    public AxisLabelHandler getTickCalculator( )
    {
        return ticks;
    }

    public void setTickCalculator( AxisLabelHandler ticks )
    {
        this.ticks = ticks;
    }
}
