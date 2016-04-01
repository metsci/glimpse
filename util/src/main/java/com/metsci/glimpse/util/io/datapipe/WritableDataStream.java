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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An implementation of {@link WritableDataPipe} that uses standard io (not nio).
 *
 * @author hogye
 */
public class WritableDataStream implements WritableDataPipe
{
    private final DataOutputStream _stream;

    public WritableDataStream( DataOutputStream stream )
    {
        _stream = stream;
    }

    public void writeBoolean( boolean value ) throws IOException
    {
        _stream.writeBoolean( value );
    }

    public void writeByte( byte value ) throws IOException
    {
        _stream.writeByte( value );
    }

    public void writeDouble( double value ) throws IOException
    {
        _stream.writeDouble( value );
    }

    public void writeFloat( float value ) throws IOException
    {
        _stream.writeFloat( value );
    }

    public void writeInt( int value ) throws IOException
    {
        _stream.writeInt( value );
    }

    public void writeLong( long value ) throws IOException
    {
        _stream.writeLong( value );
    }

    public void writeShort( short value ) throws IOException
    {
        _stream.writeShort( value );
    }

    public void writeString( String value ) throws IOException
    {
        // NOTE: do not use DataOutputStream.writeUTF for interoperability with channel implementation
        byte[] bytes = value.getBytes( "UTF-8" );
        _stream.writeInt( bytes.length );
        _stream.write( bytes );
    }

    public void writeBooleanArray( boolean[] values ) throws IOException
    {
        byte[] values2 = new byte[values.length];

        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            values2[i] = values[i] ? AbstractChannel.TRUE : AbstractChannel.FALSE;
        }

        writeByteArray( values2 );
    }

    public void writeByteArray( byte[] values ) throws IOException
    {
        _stream.write( values );
    }

    public void writeShortArray( short[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            _stream.writeShort( values[i] );
        }
    }

    public void writeIntArray( int[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            _stream.writeInt( values[i] );
        }
    }

    public void writeLongArray( long[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            _stream.writeLong( values[i] );
        }
    }

    public void writeFloatArray( float[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            _stream.writeFloat( values[i] );
        }
    }

    public void writeDoubleArray( double[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            _stream.writeDouble( values[i] );
        }
    }

    public void flush( ) throws IOException
    {
        _stream.flush( );
    }

    public void close( ) throws IOException
    {
        _stream.close( );
    }

    public DataOutputStream getOutputStream( )
    {
        return _stream;
    }
}
