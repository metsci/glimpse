package com.metsci.glimpse.support.line;

import static java.lang.Math.sqrt;

import java.nio.FloatBuffer;

public class LineUtils
{

    public static double distance( double x0, double y0, double x1, double y1 )
    {
        double dx = x1 - x0;
        double dy = y1 - y0;
        return sqrt( dx*dx + dy*dy );
    }

    public static void put1f( FloatBuffer buffer, double a )
    {
        buffer.put( ( float ) a );
    }

    public static void put2f( FloatBuffer buffer, double a, double b )
    {
        buffer.put( ( float ) a )
              .put( ( float ) b );
    }

}
