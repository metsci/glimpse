package com.metsci.glimpse.axis.painter;

import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;

public abstract class NumericLabelHandlerAxisPainter extends NumericAxisPainter
{
    protected AxisLabelHandler ticks;

    public NumericLabelHandlerAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );

        this.ticks = ticks;
    }

    @Override
    public void setAxisLabel( String label )
    {
        this.ticks.setAxisLabel( label );
    }

    @Override
    public AxisLabelHandler getTickCalculator( )
    {
        return ticks;
    }

    @Override
    public void setTickCalculator( AxisLabelHandler ticks )
    {
        this.ticks = ticks;
    }
}
