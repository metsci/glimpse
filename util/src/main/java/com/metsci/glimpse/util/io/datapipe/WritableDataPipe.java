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
 * Interface that can have either a regular io or an nio implementation.
 *
 * @author hogye
 */
public interface WritableDataPipe
{
    void writeBoolean( boolean value ) throws IOException;

    void writeByte( byte value ) throws IOException;

    void writeShort( short value ) throws IOException;

    void writeInt( int value ) throws IOException;

    void writeLong( long value ) throws IOException;

    void writeFloat( float value ) throws IOException;

    void writeDouble( double value ) throws IOException;

    /**
     * Writes a string encoded in true UTF-8 format (not Java's modified UTF).
     */
    void writeString( String value ) throws IOException;

    void writeBooleanArray( boolean[] values ) throws IOException;

    void writeByteArray( byte[] values ) throws IOException;

    void writeShortArray( short[] values ) throws IOException;

    void writeIntArray( int[] values ) throws IOException;

    void writeLongArray( long[] values ) throws IOException;

    void writeFloatArray( float[] values ) throws IOException;

    void writeDoubleArray( double[] values ) throws IOException;

    void close( ) throws IOException;

    void flush( ) throws IOException;

    DataOutputStream getOutputStream( );
}
