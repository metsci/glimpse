package com.metsci.glimpse.support.line;

import static java.lang.Math.sqrt;

import com.jogamp.opengl.util.GLArrayDataEditable;

public class LineUtils
{

    public static void put1f( GLArrayDataEditable array, float a )
    {
        array.putf( a );
    }

    public static void put2f( GLArrayDataEditable array, float a, float b )
    {
        array.putf( a );
        array.putf( b );
    }

    public static void put3f( GLArrayDataEditable array, float a, float b, float c )
    {
        array.putf( a );
        array.putf( b );
        array.putf( c );
    }

    public static void put4f( GLArrayDataEditable array, float a, float b, float c, float d )
    {
        array.putf( a );
        array.putf( b );
        array.putf( c );
        array.putf( d );
    }

    public static double distance( double xA, double yA, double xB, double yB )
    {
        double dx = xB - xA;
        double dy = yB - yA;
        return sqrt( dx*dx + dy*dy );
    }

}
