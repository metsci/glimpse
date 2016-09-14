package com.metsci.glimpse.support.line.util;

import static java.lang.Math.*;

import java.nio.FloatBuffer;

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

    public static FloatBuffer flipped( FloatBuffer buffer )
    {
        FloatBuffer flipped = buffer.duplicate( );
        flipped.flip( );
        return flipped;
    }

    /**
     * Like {@link FloatBuffer#put(FloatBuffer)}, except prepends a duplicate copy of the first
     * {@code floatsToDuplicate} values, and appends a duplicate copy of the last {@code floatsToDuplicate}
     * values. This is useful when using {@link javax.media.opengl.GL3#GL_LINE_STRIP_ADJACENCY},
     * for example.
     * <p>
     * If the {@code from} buffer does not have at least {@code floatsToDuplicate} values remaining,
     * this function does not modify either buffer.
     */
    public static void putWithFirstAndLastDuplicated( FloatBuffer from, FloatBuffer to, int floatsToDuplicate )
    {
        if ( from.remaining( ) >= floatsToDuplicate )
        {
            // Double-put first vertex, to work with GL_LINE_STRIP_ADJACENCY
            for ( int i = 0; i < floatsToDuplicate; i++ )
            {
                // Use get-at-index to avoid modifying from's position
                to.put( from.get( i ) );
            }

            to.put( from );

            // Double-put last vertex, to work with GL_LINE_STRIP_ADJACENCY
            for ( int i = from.position( ) - floatsToDuplicate; i < from.position( ); i++ )
            {
                // Use get-at-index to avoid modifying from's position
                to.put( from.get( i ) );
            }
        }
    }

}
