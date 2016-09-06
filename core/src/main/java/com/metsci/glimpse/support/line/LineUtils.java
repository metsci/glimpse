package com.metsci.glimpse.support.line;

import static com.jogamp.common.nio.Buffers.newDirectFloatBuffer;
import static com.metsci.glimpse.support.line.DirectBufferDealloc.deallocateDirectBuffers;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_ONE;
import static javax.media.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static javax.media.opengl.GL.GL_SRC_ALPHA;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis2D;

public class LineUtils
{

    public static void enableStandardBlending( GL gl )
    {
        gl.glBlendFuncSeparate( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL_BLEND );
    }

    public static double distance( double x0, double y0, double x1, double y1 )
    {
        double dx = x1 - x0;
        double dy = y1 - y0;
        return sqrt( dx*dx + dy*dy );
    }

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

    public static void put1f( FloatBuffer buffer, double a )
    {
        buffer.put( ( float ) a );
    }

    public static void put2f( FloatBuffer buffer, double a, double b )
    {
        buffer.put( ( float ) a )
              .put( ( float ) b );
    }

    public static FloatBuffer flipped( FloatBuffer buffer )
    {
        FloatBuffer flipped = buffer.duplicate( );
        flipped.flip( );
        return flipped;
    }

    public static FloatBuffer ensureAdditionalCapacity( FloatBuffer buffer, int additionalFloats, boolean deallocOldBuffer )
    {
        int minCapacity = buffer.position( ) + additionalFloats;
        if ( buffer.capacity( ) >= minCapacity )
        {
            return buffer;
        }
        else if ( minCapacity > Integer.MAX_VALUE )
        {
            throw new RuntimeException( "Cannot create a buffer larger than MAX_INT: requested-capacity = " + minCapacity );
        }
        else
        {
            long newCapacity = min( Integer.MAX_VALUE, max( minCapacity, ( long ) ceil( 1.618 * buffer.capacity( ) ) ) );
            FloatBuffer newBuffer = newDirectFloatBuffer( ( int ) newCapacity );

            if ( deallocOldBuffer )
            {
                buffer.flip( );
                newBuffer.put( buffer );
                deallocateDirectBuffers( buffer );
            }
            else
            {
                FloatBuffer dupe = buffer.duplicate( );
                dupe.flip( );
                newBuffer.put( dupe );
            }

            return newBuffer;
        }
    }

}
