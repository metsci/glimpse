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
package com.metsci.glimpse.util.io.datapipe;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channel;

/**
 * @author hogye
 */
public class AbstractChannel
{
    // Size of buffer (bytes) should be a multiple of 8
    protected static final int BUFFER_SIZE = 8192;

    protected static final String STRING_ENCODING = "UTF-8";

    protected static final byte FALSE = 0;
    protected static final byte TRUE = 1;

    protected final ByteBuffer _byteBuffer = ByteBuffer.allocateDirect( BUFFER_SIZE );
    protected final ShortBufferWrapper _shortBuffer = new ShortBufferWrapper( _byteBuffer.asShortBuffer( ) );
    protected final IntBufferWrapper _intBuffer = new IntBufferWrapper( _byteBuffer.asIntBuffer( ) );
    protected final LongBufferWrapper _longBuffer = new LongBufferWrapper( _byteBuffer.asLongBuffer( ) );
    protected final FloatBufferWrapper _floatBuffer = new FloatBufferWrapper( _byteBuffer.asFloatBuffer( ) );
    protected final DoubleBufferWrapper _doubleBuffer = new DoubleBufferWrapper( _byteBuffer.asDoubleBuffer( ) );

    protected final Channel _channel;

    /**
     * @param channel   an nio channel which must be set to blocking mode. If it is not in blocking
     *                  mode, an <code>IllegalArgumentException</code> will be thrown.
     */
    protected AbstractChannel( Channel channel )
    {
        //NOTE: don't use native order but default big-endian order for interoperability with
        //      DataInputStream/DataOutputStream. [ x86 is little-endian ]
        //      This may be revisited later if performance suffers.
        //      _byteBuffer.order(ByteOrder.nativeOrder());

        _channel = channel;
    }

    public void close( ) throws IOException
    {
        _channel.close( );
    }

    public Channel getChannel( )
    {
        return _channel;
    }

    /**
     * A simple buffer wrapper to avoid code duplication with the various ByteBuffer views.
     */
    protected abstract static class BufferWrapper
    {
        protected final Buffer _buffer;

        public BufferWrapper( Buffer buffer )
        {
            _buffer = buffer;
        }

        public int position( )
        {
            return _buffer.position( );
        }

        public Buffer position( int position )
        {
            return _buffer.position( position );
        }

        public int limit( )
        {
            return _buffer.limit( );
        }

        public Buffer limit( int limit )
        {
            return _buffer.limit( limit );
        }

        public int capacity( )
        {
            return _buffer.capacity( );
        }

        public int remaining( )
        {
            return _buffer.remaining( );
        }

        public boolean hasRemaining( )
        {
            return _buffer.hasRemaining( );
        }

        public Buffer clear( )
        {
            return _buffer.clear( );
        }

        public Buffer flip( )
        {
            return _buffer.flip( );
        }

        public Buffer mark( )
        {
            return _buffer.mark( );
        }

        public Buffer reset( )
        {
            return _buffer.reset( );
        }

        public Buffer rewind( )
        {
            return _buffer.rewind( );
        }

        public boolean isReadOnlyt( )
        {
            return _buffer.isReadOnly( );
        }

        public Buffer positionAndLimit( int position, int limit )
        {
            // IMPORTANT: set limit to capacity, to ensure pos <= lim in the line that sets the position
            _buffer.limit( _buffer.capacity( ) );
            _buffer.position( position );
            return _buffer.limit( limit );
        }

        /**
         * @param array an array of primitive types (short[], int[], long[], float[], double[])
         * @return      the wrapped buffer
         */
        public abstract Object get( Object array, int offset, int length );

        /**
         * @param array an array of primitive types (short[], int[], long[], float[], double[])
         * @return      the wrapped buffer
         */
        public abstract Object put( Object array, int offset, int length );

        /**
         * @return the shift count needed to convert a position in this buffer to a position
         *         in a ByteBuffer. For example, for DoubleBufferWrapper, this method would
         *         return 3 (1 double = 8 bytes.)
         */
        public abstract int getShift( );
    }

    protected static class ShortBufferWrapper extends BufferWrapper
    {
        public ShortBufferWrapper( ShortBuffer buffer )
        {
            super( buffer );
        }

        @Override
        public ShortBuffer get( Object array, int offset, int length )
        {
            return ( ( ShortBuffer ) _buffer ).get( ( short[] ) array, offset, length );
        }

        @Override
        public ShortBuffer put( Object array, int offset, int length )
        {
            return ( ( ShortBuffer ) _buffer ).put( ( short[] ) array, offset, length );
        }

        @Override
        public int getShift( )
        {
            return 1;
        }
    }

    protected static class IntBufferWrapper extends BufferWrapper
    {
        public IntBufferWrapper( IntBuffer buffer )
        {
            super( buffer );
        }

        @Override
        public IntBuffer get( Object array, int offset, int length )
        {
            return ( ( IntBuffer ) _buffer ).get( ( int[] ) array, offset, length );
        }

        @Override
        public IntBuffer put( Object array, int offset, int length )
        {
            return ( ( IntBuffer ) _buffer ).put( ( int[] ) array, offset, length );
        }

        @Override
        public int getShift( )
        {
            return 2;
        }
    }

    protected static class LongBufferWrapper extends BufferWrapper
    {
        public LongBufferWrapper( LongBuffer buffer )
        {
            super( buffer );
        }

        @Override
        public LongBuffer get( Object array, int offset, int length )
        {
            return ( ( LongBuffer ) _buffer ).get( ( long[] ) array, offset, length );
        }

        @Override
        public LongBuffer put( Object array, int offset, int length )
        {
            return ( ( LongBuffer ) _buffer ).put( ( long[] ) array, offset, length );
        }

        @Override
        public int getShift( )
        {
            return 3;
        }
    }

    protected static class FloatBufferWrapper extends BufferWrapper
    {
        public FloatBufferWrapper( FloatBuffer buffer )
        {
            super( buffer );
        }

        @Override
        public FloatBuffer get( Object array, int offset, int length )
        {
            return ( ( FloatBuffer ) _buffer ).get( ( float[] ) array, offset, length );
        }

        @Override
        public FloatBuffer put( Object array, int offset, int length )
        {
            return ( ( FloatBuffer ) _buffer ).put( ( float[] ) array, offset, length );
        }

        @Override
        public int getShift( )
        {
            return 2;
        }
    }

    protected static class DoubleBufferWrapper extends BufferWrapper
    {
        public DoubleBufferWrapper( DoubleBuffer buffer )
        {
            super( buffer );
        }

        @Override
        public DoubleBuffer get( Object array, int offset, int length )
        {
            return ( ( DoubleBuffer ) _buffer ).get( ( double[] ) array, offset, length );
        }

        @Override
        public DoubleBuffer put( Object array, int offset, int length )
        {
            return ( ( DoubleBuffer ) _buffer ).put( ( double[] ) array, offset, length );
        }

        @Override
        public int getShift( )
        {
            return 3;
        }
    }

}
