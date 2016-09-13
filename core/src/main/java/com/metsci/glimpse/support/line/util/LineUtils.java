package com.metsci.glimpse.support.line.util;

import static com.jogamp.common.nio.Buffers.newDirectFloatBuffer;
import static com.metsci.glimpse.support.line.util.DirectBufferDealloc.deallocateDirectBuffers;
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
     *
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
