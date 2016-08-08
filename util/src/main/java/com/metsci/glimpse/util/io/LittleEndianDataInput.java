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
package com.metsci.glimpse.util.io;

import java.io.DataInput;
import java.io.IOException;

/**
 * LittleEndianDataInput is a wrapper around a DataInput object
 * that converts formatted input from little-endian to Java standard
 * big-endian after reading it in.
 *
 * @author hogye
 */
public class LittleEndianDataInput implements DataInput
{
    private final DataInput in;

    public LittleEndianDataInput( DataInput in )
    {
        this.in = in;
    }

    @Override
    public void readFully( byte[] arg0 ) throws IOException
    {
        in.readFully( arg0 );
    }

    @Override
    public void readFully( byte[] arg0, int arg1, int arg2 ) throws IOException
    {
        in.readFully( arg0, arg1, arg2 );
    }

    @Override
    public int skipBytes( int arg0 ) throws IOException
    {
        return in.skipBytes( arg0 );
    }

    @Override
    public boolean readBoolean( ) throws IOException
    {
        return in.readBoolean( );
    }

    @Override
    public byte readByte( ) throws IOException
    {
        return in.readByte( );
    }

    @Override
    public int readUnsignedByte( ) throws IOException
    {
        return in.readUnsignedByte( );
    }

    @Override
    public short readShort( ) throws IOException
    {
        int a = in.readUnsignedByte( );
        int b = in.readByte( );
        return ( short ) ( ( b << 8 ) | a );
    }

    @Override
    public int readUnsignedShort( ) throws IOException
    {
        int a = in.readUnsignedByte( );
        int b = in.readUnsignedByte( );
        return ( ( b << 8 ) | a );
    }

    @Override
    public char readChar( ) throws IOException
    {
        return in.readChar( );
    }

    @Override
    public int readInt( ) throws IOException
    {
        int a = in.readUnsignedByte( );
        int b = in.readUnsignedByte( );
        int c = in.readUnsignedByte( );
        int d = in.readByte( );
        return ( ( d << 24 ) | ( c << 16 ) | ( b << 8 ) | a );
    }

    @Override
    public long readLong( ) throws IOException
    {
        long a = in.readUnsignedByte( );
        long b = in.readUnsignedByte( );
        long c = in.readUnsignedByte( );
        long d = in.readUnsignedByte( );
        long e = in.readUnsignedByte( );
        long f = in.readUnsignedByte( );
        long g = in.readUnsignedByte( );
        long h = in.readByte( );
        return ( ( h << 56 ) | ( g << 48 ) | ( f << 40 ) | ( e << 32 ) | ( d << 24 ) | ( c << 16 ) | ( b << 8 ) | a );
    }

    @Override
    public float readFloat( ) throws IOException
    {
        return Float.intBitsToFloat( readInt( ) );
    }

    @Override
    public double readDouble( ) throws IOException
    {
        return Double.longBitsToDouble( readLong( ) );
    }

    @Override
    public String readLine( ) throws IOException
    {
        return in.readLine( );
    }

    @Override
    public String readUTF( ) throws IOException
    {
        return in.readUTF( );
    }

}
