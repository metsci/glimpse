package com.metsci.glimpse.util.buffer;

import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static java.lang.Math.*;
import static java.nio.ByteBuffer.*;
import static java.nio.ByteOrder.*;

import java.nio.FloatBuffer;

public class DirectBufferUtils
{

    /**
     * If the supplied buffer has enough capacity to put the specified number of
     * additional values (starting at its position), then the supplied buffer is
     * returned.
     * <p>
     * Otherwise, a new buffer is created, with enough capacity enough for the
     * contents of the original buffer (up to its position) plus the specified
     * number of additional values. The new buffer will be direct, and will have
     * native byte ordering. The contents of the original buffer (up to its position)
     * will be copied into the new buffer.
     * <p>
     * If a new buffer is created, it may be created with a capacity larger than
     * is required. This is useful for the common situation where, over and over
     * again, a few values need to be appended, but we don't want to allocate a
     * new buffer every single time.
     */
    public static FloatBuffer ensureAdditionalCapacity( FloatBuffer buffer, int additionalFloats, boolean deallocOldBuffer )
    {
        int bytesPerFloat = Float.SIZE / Byte.SIZE;
        long minFloats = buffer.position( ) + additionalFloats;
        long minBytes = minFloats * bytesPerFloat;

        if ( buffer.capacity( ) >= minFloats )
        {
            return buffer;
        }
        else if ( minBytes > Integer.MAX_VALUE )
        {
            throw new RuntimeException( "Cannot create a buffer larger than MAX_INT bytes: requested-capacity = " + minBytes + " bytes");
        }
        else
        {
            long newBytes = min( Integer.MAX_VALUE, max( minBytes, ( long ) ceil( 1.618 * buffer.capacity( ) * bytesPerFloat ) ) );
            FloatBuffer newBuffer = allocateDirect( ( int ) newBytes ).order( nativeOrder( ) ).asFloatBuffer( );

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
