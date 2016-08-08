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

import java.io.DataOutput;
import java.io.IOException;

/**
 * LittleEndianDataInput is a wrapper around a DataOutput object
 * that converts data from Java standard big-endian to little-endian
 * before outputting it.
 *
 * @author hogye
 */
public class LittleEndianDataOutput implements DataOutput
{
    private DataOutput out;

    public LittleEndianDataOutput( DataOutput out )
    {
        this.out = out;
    }

    @Override
    public void write( int arg0 ) throws IOException
    {
        out.write( arg0 );
    }

    @Override
    public void write( byte[] arg0 ) throws IOException
    {
        out.write( arg0 );
    }

    @Override
    public void write( byte[] arg0, int arg1, int arg2 ) throws IOException
    {
        out.write( arg0, arg1, arg2 );
    }

    @Override
    public void writeBoolean( boolean arg0 ) throws IOException
    {
        out.writeBoolean( arg0 );
    }

    @Override
    public void writeByte( int arg0 ) throws IOException
    {
        out.writeByte( arg0 );
    }

    @Override
    public void writeShort( int arg0 ) throws IOException
    {
        out.write( arg0 );
        out.write( arg0 >>> 8 );
    }

    @Override
    public void writeChar( int arg0 ) throws IOException
    {
        out.writeChar( arg0 );
    }

    @Override
    public void writeInt( int arg0 ) throws IOException
    {
        out.write( arg0 );
        out.write( arg0 >>> 8 );
        out.write( arg0 >>> 16 );
        out.write( arg0 >>> 24 );
    }

    @Override
    public void writeLong( long arg0 ) throws IOException
    {
        out.write( ( int ) arg0 );
        out.write( ( int ) ( arg0 >>> 8 ) );
        out.write( ( int ) ( arg0 >>> 16 ) );
        out.write( ( int ) ( arg0 >>> 24 ) );
        out.write( ( int ) ( arg0 >>> 32 ) );
        out.write( ( int ) ( arg0 >>> 40 ) );
        out.write( ( int ) ( arg0 >>> 48 ) );
        out.write( ( int ) ( arg0 >>> 56 ) );
    }

    @Override
    public void writeFloat( float arg0 ) throws IOException
    {
        writeInt( Float.floatToIntBits( arg0 ) );
    }

    @Override
    public void writeDouble( double arg0 ) throws IOException
    {
        writeLong( Double.doubleToLongBits( arg0 ) );
    }

    @Override
    public void writeBytes( String arg0 ) throws IOException
    {
        out.writeBytes( arg0 );
    }

    @Override
    public void writeChars( String arg0 ) throws IOException
    {
        out.writeChars( arg0 );
    }

    @Override
    public void writeUTF( String arg0 ) throws IOException
    {
        out.writeUTF( arg0 );
    }

}
