package com.metsci.glimpse.support.shader.line;

import static java.lang.Math.*;

import com.metsci.glimpse.axis.Axis2D;

public class LineUtils
{

    /**
     * Computes XY distance in X units, allowing Y units to differ from X units by a linear factor.
     * The ratio of X units to Y units is specified with {@code ppvAspectRatio}. This is useful when
     * computing cumulative distance along a line-strip, for stippling.
     * <p>
     * @see #ppvAspectRatio(Axis2D)
     */
    public static double distance( double x0, double y0, double x1, double y1, double ppvAspectRatio )
    {
        double dx = x1 - x0;
        double dy = ( y1 - y0 ) / ppvAspectRatio;
        return sqrt( dx*dx + dy*dy );
    }

    public static double ppvAspectRatio( Axis2D axis )
    {
        return ( axis.getAxisX( ).getPixelsPerValue( ) / axis.getAxisY( ).getPixelsPerValue( ) );
    }

}
