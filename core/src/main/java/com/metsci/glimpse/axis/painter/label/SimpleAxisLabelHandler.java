package com.metsci.glimpse.axis.painter.label;

import com.metsci.glimpse.axis.Axis1D;

/**
 * An AxisLabelHandler which displays axis value "as-is" without
 * attempting to truncate very large values by displaying as powers of 1000.
 * 
 * @author ulman
 */
public class SimpleAxisLabelHandler extends GridAxisLabelHandler
{
    @Override
    protected String tickString( Axis1D axis, double number, int orderAxis )
    {
        if ( orderAxis < 0 )
        {
            tickNumberFormatter.setMaximumFractionDigits( -orderAxis );
        }
        else
        {
            tickNumberFormatter.setMaximumFractionDigits( 1 );
        }
        return tickNumberFormatter.format( number );
    }
}
