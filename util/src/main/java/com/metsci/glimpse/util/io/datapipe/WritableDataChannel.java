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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * An implementation of {@link WritableDataPipe} that uses nio.
 *
 * @author hogye
 */
public class WritableDataChannel extends AbstractChannel implements WritableDataPipe
{
    private final DataOutputStream _stream;
    private long _totalBytesWritten;

    protected final WritableByteChannel _channel;

    public WritableDataChannel( WritableByteChannel channel ) throws IOException
    {
        super( channel );

        _channel = channel;

        _stream = new DataOutputStream( new BufferedOutputStream( Channels.newOutputStream( channel ) ) );
    }

    /**
     * @return the total number of bytes written since this channel was created.
     */
    public long getTotalBytesWritten( )
    {
        return _totalBytesWritten;
    }

    /**
     * @return the number of bytes written to the channel.
     */
    public int flushBuffer( ) throws IOException
    {
        _byteBuffer.flip( );
        int nBytesWritten = 0;

        while ( _byteBuffer.hasRemaining( ) )
        {
            nBytesWritten += _channel.write( _byteBuffer );
        }

        _totalBytesWritten += nBytesWritten;
        _byteBuffer.clear( );

        return nBytesWritten;
    }

    public void writeBoolean( boolean value ) throws IOException
    {
        writeByte( value ? TRUE : FALSE );
    }

    public void writeByte( byte value ) throws IOException
    {
        assert ( _byteBuffer.limit( ) == _byteBuffer.capacity( ) );

        if ( _byteBuffer.remaining( ) < 1 )
        {
            flushBuffer( );
        }

        _byteBuffer.put( value );
    }

    public void writeShort( short value ) throws IOException
    {
        assert ( _byteBuffer.limit( ) == _byteBuffer.capacity( ) );

        if ( _byteBuffer.remaining( ) < 2 )
        {
            flushBuffer( );
        }

        _byteBuffer.putShort( value );
    }

    public void writeInt( int value ) throws IOException
    {
        assert ( _byteBuffer.limit( ) == _byteBuffer.capacity( ) );

        if ( _byteBuffer.remaining( ) < 4 )
        {
            flushBuffer( );
        }

        _byteBuffer.putInt( value );
    }

    public void writeLong( long value ) throws IOException
    {
        assert ( _byteBuffer.limit( ) == _byteBuffer.capacity( ) );

        if ( _byteBuffer.remaining( ) < 8 )
        {
            flushBuffer( );
        }

        _byteBuffer.putLong( value );
    }

    public void writeFloat( float value ) throws IOException
    {
        assert ( _byteBuffer.limit( ) == _byteBuffer.capacity( ) );

        if ( _byteBuffer.remaining( ) < 4 )
        {
            flushBuffer( );
        }

        _byteBuffer.putFloat( value );
    }

    public void writeDouble( double value ) throws IOException
    {
        assert ( _byteBuffer.limit( ) == _byteBuffer.capacity( ) );

        if ( _byteBuffer.remaining( ) < 8 )
        {
            flushBuffer( );
        }

        _byteBuffer.putDouble( value );
    }

    public void writeBooleanArray( boolean[] values ) throws IOException
    {
        assert ( _byteBuffer.limit( ) == _byteBuffer.capacity( ) );

        byte[] values2 = new byte[values.length];

        for ( int i = 0, ni = values2.length; i < ni; i++ )
        {
            values2[i] = ( values[i] ? TRUE : FALSE );
        }

        writeByteArray( values2 );
    }

    public void writeByteArray( byte[] values ) throws IOException
    {
        assert ( _byteBuffer.limit( ) == _byteBuffer.capacity( ) );

        for ( int i = 0, ni = values.length; i < ni; )
        {
            if ( !_byteBuffer.hasRemaining( ) )
            {
                flushBuffer( );
            }

            int length = Math.min( ni - i, _byteBuffer.remaining( ) );
            _byteBuffer.put( values, i, length );

            i += length;
        }
    }

    public void writeShortArray( short[] values ) throws IOException
    {
        writeArray( values, values.length, _shortBuffer );
    }

    public void writeIntArray( int[] values ) throws IOException
    {
        writeArray( values, values.length, _intBuffer );
    }

    public void writeLongArray( long[] values ) throws IOException
    {
        writeArray( values, values.length, _longBuffer );
    }

    public void writeFloatArray( float[] values ) throws IOException
    {
        writeArray( values, values.length, _floatBuffer );
    }

    public void writeDoubleArray( double[] values ) throws IOException
    {
        writeArray( values, values.length, _doubleBuffer );
    }

    public void writeArray( Object array, int arrayLength, BufferWrapper buffer ) throws IOException
    {
        assert ( _byteBuffer.limit( ) == _byteBuffer.capacity( ) );

        int shift = buffer.getShift( );
        int mask = ( 1 << shift ) - 1;

        if ( ( _byteBuffer.position( ) & mask ) != 0 )
        {
            flushBuffer( );
        }

        buffer.positionAndLimit( _byteBuffer.position( ) >> shift, _byteBuffer.limit( ) >> shift );

        for ( int i = 0; i < arrayLength; )
        {
            if ( !buffer.hasRemaining( ) )
            {
                _byteBuffer.position( buffer.position( ) << shift );
                flushBuffer( );
                buffer.clear( );
            }

            int length = Math.min( arrayLength - i, buffer.remaining( ) );
            buffer.put( array, i, length );

            i += length;
        }

        _byteBuffer.position( buffer.position( ) << shift );
    }

    public void writeString( String value ) throws IOException
    {
        byte[] bytes = value.getBytes( STRING_ENCODING );
        writeInt( bytes.length );
        writeByteArray( bytes );
    }

    public void flush( ) throws IOException
    {
        flushBuffer( );
    }

    @Override
    public void close( ) throws IOException
    {
        flushBuffer( );
        super.close( );
    }

    public DataOutputStream getOutputStream( )
    {
        return _stream;
    }
}
