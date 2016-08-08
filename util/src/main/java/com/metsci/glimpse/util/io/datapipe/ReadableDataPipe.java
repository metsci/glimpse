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
 * Interface that can have either a regular io or an nio implementation.
 *
 * @author hogye
 */
public interface ReadableDataPipe
{
    boolean readBoolean( ) throws IOException;

    byte readByte( ) throws IOException;

    short readShort( ) throws IOException;

    int readInt( ) throws IOException;

    long readLong( ) throws IOException;

    float readFloat( ) throws IOException;

    double readDouble( ) throws IOException;

    /**
     * Reads a string encoded in true UTF-8 format (not Java's modified UTF).
     */
    String readString( ) throws IOException;

    boolean[] readBooleanArray( boolean[] values ) throws IOException;

    byte[] readByteArray( byte[] values ) throws IOException;

    short[] readShortArray( short[] values ) throws IOException;

    int[] readIntArray( int[] values ) throws IOException;

    long[] readLongArray( long[] values ) throws IOException;

    float[] readFloatArray( float[] values ) throws IOException;

    double[] readDoubleArray( double[] values ) throws IOException;

    void close( ) throws IOException;

    DataInputStream getInputStream( );

}
