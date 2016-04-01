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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * An implementation of {@link ReadableDataPipe} that uses nio.
 *
 * @author hogye
 */
public class ReadableDataChannel extends AbstractChannel implements ReadableDataPipe
{
    private final DataInputStream _stream;
    private long _totalBytesRead;

    protected final ReadableByteChannel _channel;

    public ReadableDataChannel( ReadableByteChannel channel ) throws IOException
    {
        super( channel );

        _channel = channel;

        _stream = new DataInputStream( new BufferedInputStream( Channels.newInputStream( channel ) ) );

        _byteBuffer.flip( );
    }

    /**
     * @return the total number of bytes read since this channel was created.
     */
    public long getTotalBytesRead( )
    {
        return _totalBytesRead;
    }

    /**
     * Postcondition: byte buffer's position is 0.
     *
     * @return the number of bytes read from the channel.
     * @throws EOFException if end of stream is reached.
     */
    protected int fillBuffer( ) throws IOException
    {
        _byteBuffer.compact( );

        if ( !_byteBuffer.hasRemaining( ) ) return 0;

        int nBytesRead = _channel.read( _byteBuffer );
        if ( nBytesRead == -1 ) throw new EOFException( );

        _totalBytesRead += nBytesRead;
        _byteBuffer.flip( );

        return nBytesRead;
    }

    public boolean readBoolean( ) throws IOException
    {
        return readByte( ) != 0;
    }

    public byte readByte( ) throws IOException
    {
        while ( _byteBuffer.remaining( ) < 1 )
            fillBuffer( );

        return _byteBuffer.get( );
    }

    public short readShort( ) throws IOException
    {
        while ( _byteBuffer.remaining( ) < 2 )
            fillBuffer( );

        return _byteBuffer.getShort( );
    }

    public int readInt( ) throws IOException
    {
        while ( _byteBuffer.remaining( ) < 4 )
            fillBuffer( );

        return _byteBuffer.getInt( );
    }

    public long readLong( ) throws IOException
    {
        while ( _byteBuffer.remaining( ) < 8 )
            fillBuffer( );

        return _byteBuffer.getLong( );
    }

    public float readFloat( ) throws IOException
    {
        while ( _byteBuffer.remaining( ) < 4 )
            fillBuffer( );

        return _byteBuffer.getFloat( );
    }

    public double readDouble( ) throws IOException
    {
        while ( _byteBuffer.remaining( ) < 8 )
            fillBuffer( );

        return _byteBuffer.getDouble( );
    }

    public boolean[] readBooleanArray( boolean[] values ) throws IOException
    {
        byte[] values2 = new byte[values.length];
        readByteArray( values2 );

        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            values[i] = ( values2[i] != 0 );
        }

        return values;
    }

    public byte[] readByteArray( byte[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; )
        {
            if ( !_byteBuffer.hasRemaining( ) ) fillBuffer( );

            int length = Math.min( ni - i, _byteBuffer.remaining( ) );
            _byteBuffer.get( values, i, length );
            i += length;
        }

        return values;
    }

    public short[] readShortArray( short[] values ) throws IOException
    {
        readArray( values, values.length, _shortBuffer );
        return values;
    }

    public int[] readIntArray( int[] values ) throws IOException
    {
        readArray( values, values.length, _intBuffer );
        return values;
    }

    public long[] readLongArray( long[] values ) throws IOException
    {
        readArray( values, values.length, _longBuffer );
        return values;
    }

    public float[] readFloatArray( float[] values ) throws IOException
    {
        readArray( values, values.length, _floatBuffer );
        return values;
    }

    public double[] readDoubleArray( double[] values ) throws IOException
    {
        readArray( values, values.length, _doubleBuffer );
        return values;
    }

    public void readArray( Object array, int arrayLength, BufferWrapper buffer ) throws IOException
    {
        int shift = buffer.getShift( );
        int mask = ( 1 << shift ) - 1;

        if ( ( _byteBuffer.position( ) & mask ) != 0 )
        {
            _byteBuffer.compact( ).flip( );
        }

        buffer.positionAndLimit( _byteBuffer.position( ) >> shift, _byteBuffer.limit( ) >> shift );

        for ( int i = 0; i < arrayLength; )
        {
            if ( !buffer.hasRemaining( ) )
            {
                _byteBuffer.position( buffer.position( ) << shift );
                fillBuffer( );
                buffer.positionAndLimit( 0, _byteBuffer.limit( ) >> shift );
            }

            int length = Math.min( arrayLength - i, buffer.remaining( ) );
            buffer.get( array, i, length );

            i += length;
        }

        _byteBuffer.position( buffer.position( ) << shift );
    }

    public String readString( ) throws IOException
    {
        byte[] bytes = new byte[readInt( )];
        readByteArray( bytes );

        return new String( bytes, STRING_ENCODING );
    }

    public DataInputStream getInputStream( )
    {
        return _stream;
    }
}
