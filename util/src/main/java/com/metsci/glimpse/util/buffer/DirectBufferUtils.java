package com.metsci.glimpse.util.buffer;

import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.deallocateDirectBuffers;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.ByteOrder.nativeOrder;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class DirectBufferUtils
{

    /**
     * Calls {@link FloatBuffer#duplicate()}, flips the result, and returns it.
     * <p>
     * This is a convenient way to get a flipped version of a buffer, without
     * modifying the original buffer's position or limit.
     */
    public static FloatBuffer flipped( FloatBuffer buffer )
    {
        FloatBuffer flipped = buffer.duplicate( );
        flipped.flip( );
        return flipped;
    }

    /**
     * Calls {@link ByteBuffer#duplicate()}, flips the result, and returns it.
     * <p>
     * This is a convenient way to get a flipped version of a buffer, without
     * modifying the original buffer's position or limit.
     */
    public static ByteBuffer flipped( ByteBuffer buffer )
    {
        ByteBuffer flipped = buffer.duplicate( );
        flipped.flip( );
        return flipped;
    }

    public static FloatBuffer grow1f( FloatBuffer buffer, float a )
    {
        buffer = ensureAdditionalCapacity( buffer, 1, true );
        buffer.put( a );
        return buffer;
    }

    public static FloatBuffer grow2f( FloatBuffer buffer, float a, float b )
    {
        buffer = ensureAdditionalCapacity( buffer, 2, true );
        buffer.put( a ).put( b );
        return buffer;
    }

    public static FloatBuffer grow4f( FloatBuffer buffer, float a, float b, float c, float d )
    {
        buffer = ensureAdditionalCapacity( buffer, 4, true );
        buffer.put( a ).put( b ).put( c ).put( d );
        return buffer;
    }

    public static FloatBuffer grow4fv( FloatBuffer buffer, float[] rgba )
    {
        buffer = ensureAdditionalCapacity( buffer, 4, true );
        buffer.put( rgba );
        return buffer;
    }

    public static ByteBuffer grow1b( ByteBuffer buffer, byte a )
    {
        buffer = ensureAdditionalCapacity( buffer, 1, true );
        buffer.put( a );
        return buffer;
    }

    public static ByteBuffer grow2b( ByteBuffer buffer, byte a, byte b )
    {
        buffer = ensureAdditionalCapacity( buffer, 2, true );
        buffer.put( a ).put( b );
        return buffer;
    }

    public static ByteBuffer grow4b( ByteBuffer buffer, byte a, byte b, byte c, byte d )
    {
        buffer = ensureAdditionalCapacity( buffer, 4, true );
        buffer.put( a ).put( b ).put( c ).put( d );
        return buffer;
    }

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
            throw new RuntimeException( "Cannot create a buffer larger than MAX_INT bytes: requested-capacity = " + minBytes + " bytes" );
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
    public static ByteBuffer ensureAdditionalCapacity( ByteBuffer buffer, int additionalBytes, boolean deallocOldBuffer )
    {
        long minBytes = buffer.position( ) + additionalBytes;

        if ( buffer.capacity( ) >= minBytes )
        {
            return buffer;
        }
        else if ( minBytes > Integer.MAX_VALUE )
        {
            throw new RuntimeException( "Cannot create a buffer larger than MAX_INT bytes: requested-capacity = " + minBytes + " bytes" );
        }
        else
        {
            long newBytes = min( Integer.MAX_VALUE, max( minBytes, ( long ) ceil( 1.618 * buffer.capacity( ) ) ) );
            ByteBuffer newBuffer = allocateDirect( ( int ) newBytes ).order( nativeOrder( ) );

            if ( deallocOldBuffer )
            {
                buffer.flip( );
                newBuffer.put( buffer );
                deallocateDirectBuffers( buffer );
            }
            else
            {
                ByteBuffer dupe = buffer.duplicate( );
                dupe.flip( );
                newBuffer.put( dupe );
            }

            return newBuffer;
        }
    }

}
