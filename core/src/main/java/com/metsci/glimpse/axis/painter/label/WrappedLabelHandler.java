package com.metsci.glimpse.axis.painter.label;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.WrappedAxis1D;

public class WrappedLabelHandler extends GridAxisLabelHandler
{
    @Override
    protected String tickString( Axis1D axis, double number, int orderAxis )
    {
        if ( axis instanceof WrappedAxis1D )
        {
            WrappedAxis1D wrappedAxis = ( WrappedAxis1D ) axis;
            number = wrappedAxis.getWrappedValue( number );
        }
        
        return super.tickString( axis, number, orderAxis );
    }
}
