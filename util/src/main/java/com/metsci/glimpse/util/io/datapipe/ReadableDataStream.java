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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * An implementation of {@link ReadableDataPipe} that uses standard io (not nio).
 *
 * @author hogye
 */
public class ReadableDataStream implements ReadableDataPipe
{
    private final DataInputStream _stream;

    public ReadableDataStream( DataInputStream stream )
    {
        _stream = stream;
    }

    public boolean readBoolean( ) throws IOException
    {
        return _stream.readBoolean( );
    }

    public byte readByte( ) throws IOException
    {
        return _stream.readByte( );
    }

    public double readDouble( ) throws IOException
    {
        return _stream.readDouble( );
    }

    public float readFloat( ) throws IOException
    {
        return _stream.readFloat( );
    }

    public int readInt( ) throws IOException
    {
        return _stream.readInt( );
    }

    public long readLong( ) throws IOException
    {
        return _stream.readLong( );
    }

    public short readShort( ) throws IOException
    {
        return _stream.readShort( );
    }

    public String readString( ) throws IOException
    {
        // Note: do not use DataInputStream.readUTF for compatibility with channel implementation
        byte[] bytes = new byte[_stream.readInt( )];
        _stream.readFully( bytes );

        return new String( bytes, "UTF-8" );
    }

    public byte[] readByteArray( byte[] bytes ) throws IOException
    {
        _stream.readFully( bytes );
        return bytes;
    }

    public boolean[] readBooleanArray( boolean[] values ) throws IOException
    {
        byte[] values2 = readByteArray( new byte[values.length] );

        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            values[i] = values2[i] != 0;
        }

        return values;
    }

    public short[] readShortArray( short[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            values[i] = _stream.readShort( );
        }

        return values;
    }

    public int[] readIntArray( int[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            values[i] = _stream.readInt( );
        }

        return values;
    }

    public long[] readLongArray( long[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            values[i] = _stream.readLong( );
        }

        return values;
    }

    public float[] readFloatArray( float[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            values[i] = _stream.readFloat( );
        }

        return values;
    }

    public double[] readDoubleArray( double[] values ) throws IOException
    {
        for ( int i = 0, ni = values.length; i < ni; i++ )
        {
            values[i] = _stream.readDouble( );
        }

        return values;
    }

    public void close( ) throws IOException
    {
        _stream.close( );
    }

    public DataInputStream getInputStream( )
    {
        return _stream;
    }
}
