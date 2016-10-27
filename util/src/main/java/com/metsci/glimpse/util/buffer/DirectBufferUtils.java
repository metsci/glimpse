/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.util.buffer;

import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static java.lang.Math.*;
import static java.nio.ByteBuffer.*;
import static java.nio.ByteOrder.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class DirectBufferUtils
{

    public static ByteBuffer cleared( ByteBuffer buffer )
    {
        ByteBuffer cleared = buffer.duplicate( ).order( buffer.order( ) );
        cleared.clear( );
        return cleared;
    }

    public static FloatBuffer readonly( FloatBuffer buffer )
    {
        return buffer.asReadOnlyBuffer( );
    }

    public static DoubleBuffer readonly( DoubleBuffer buffer )
    {
        return buffer.asReadOnlyBuffer( );
    }

    public static IntBuffer readonly( IntBuffer buffer )
    {
        return buffer.asReadOnlyBuffer( );
    }

    public static ByteBuffer readonly( ByteBuffer buffer )
    {
        return buffer.asReadOnlyBuffer( ).order( buffer.order( ) );
    }

    public static FloatBuffer sliced( FloatBuffer buffer, int first, int count )
    {
        FloatBuffer buffer2 = buffer.duplicate( );
        buffer2.limit( first + count );
        buffer2.position( first );
        return buffer2.slice( );
    }

    public static DoubleBuffer sliced( DoubleBuffer buffer, int first, int count )
    {
        DoubleBuffer buffer2 = buffer.duplicate( );
        buffer2.limit( first + count );
        buffer2.position( first );
        return buffer2.slice( );
    }

    public static IntBuffer sliced( IntBuffer buffer, int first, int count )
    {
        IntBuffer buffer2 = buffer.duplicate( );
        buffer2.limit( first + count );
        buffer2.position( first );
        return buffer2.slice( );
    }

    public static ByteBuffer sliced( ByteBuffer buffer, int first, int count )
    {
        ByteBuffer buffer2 = buffer.duplicate( ).order( buffer.order( ) );
        buffer2.limit( first + count );
        buffer2.position( first );
        return buffer2.slice( ).order( buffer.order( ) );
    }

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
     * Calls {@link DoubleBuffer#duplicate()}, flips the result, and returns it.
     * <p>
     * This is a convenient way to get a flipped version of a buffer, without
     * modifying the original buffer's position or limit.
     */
    public static DoubleBuffer flipped( DoubleBuffer buffer )
    {
        DoubleBuffer flipped = buffer.duplicate( );
        flipped.flip( );
        return flipped;
    }

    /**
     * Calls {@link IntBuffer#duplicate()}, flips the result, and returns it.
     * <p>
     * This is a convenient way to get a flipped version of a buffer, without
     * modifying the original buffer's position or limit.
     */
    public static IntBuffer flipped( IntBuffer buffer )
    {
        IntBuffer flipped = buffer.duplicate( );
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
        ByteBuffer flipped = buffer.duplicate( ).order( buffer.order( ) );
        flipped.flip( );
        return flipped;
    }

    public static FloatBuffer put1f( FloatBuffer buffer, float a )
    {
        return buffer.put( a );
    }

    public static FloatBuffer put2f( FloatBuffer buffer, float a, float b )
    {
        return buffer.put( a ).put( b );
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
     * If a new buffer is created, it may be created with a capacity larger than is
     * required. This is useful for the common situation where, over and over again,
     * a few values need to be appended, but we don't want to allocate a new buffer
     * every single time.
     */
    public static FloatBuffer ensureAdditionalCapacity( FloatBuffer buffer, int additionalFloats, boolean deallocOldBuffer )
    {
        long minFloats = ( ( long ) buffer.position( ) ) + additionalFloats;
        if ( minFloats > Integer.MAX_VALUE )
        {
            throw new RuntimeException( "Cannot create a buffer larger than MAX_INT floats: requested-capacity = " + minFloats + " floats" );
        }

        return ensureCapacity( buffer, ( int ) minFloats, deallocOldBuffer );
    }

    /**
     * If the supplied buffer has at least the specified capacity, then the supplied
     * buffer is returned.
     * <p>
     * Otherwise, a new buffer is created, with at least the specified capacity. The
     * new buffer will be direct, and will have native byte ordering. The contents of
     * the original buffer (up to its position) will be copied into the new buffer.
     * <p>
     * If a new buffer is created, it may be created with a capacity larger than is
     * required. This is useful for the common situation where, over and over again,
     * a few values need to be appended, but we don't want to allocate a new buffer
     * every single time.
     */
    public static FloatBuffer ensureCapacity( FloatBuffer buffer, int minFloats, boolean deallocOldBuffer )
    {
        int bytesPerFloat = Float.SIZE / Byte.SIZE;
        long minBytes = minFloats * bytesPerFloat;
        if ( minBytes > Integer.MAX_VALUE )
        {
            throw new RuntimeException( "Cannot create a buffer larger than MAX_INT bytes: requested-capacity = " + minBytes + " bytes" );
        }

        if ( buffer.capacity( ) >= minFloats )
        {
            return buffer;
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
     * If a new buffer is created, it may be created with a capacity larger than is
     * required. This is useful for the common situation where, over and over again,
     * a few values need to be appended, but we don't want to allocate a new buffer
     * every single time.
     */
    public static ByteBuffer ensureAdditionalCapacity( ByteBuffer buffer, int additionalBytes, boolean deallocOldBuffer )
    {
        long minBytes = ( ( long ) buffer.position( ) ) + additionalBytes;
        if ( minBytes > Integer.MAX_VALUE )
        {
            throw new RuntimeException( "Cannot create a buffer larger than MAX_INT bytes: requested-capacity = " + minBytes + " bytes" );
        }

        return ensureCapacity( buffer, ( int ) minBytes, deallocOldBuffer );
    }

    /**
     * If the supplied buffer has at least the specified capacity, then the supplied
     * buffer is returned.
     * <p>
     * Otherwise, a new buffer is created, with at least the specified capacity. The
     * new buffer will be direct, and will have native byte ordering. The contents of
     * the original buffer (up to its position) will be copied into the new buffer.
     * <p>
     * If a new buffer is created, it may be created with a capacity larger than is
     * required. This is useful for the common situation where, over and over again,
     * a few values need to be appended, but we don't want to allocate a new buffer
     * every single time.
     */
    public static ByteBuffer ensureCapacity( ByteBuffer buffer, int minBytes, boolean deallocOldBuffer )
    {
        if ( buffer.capacity( ) >= minBytes )
        {
            return buffer;
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
