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
package com.metsci.glimpse.util.primitives;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Strings are encoded and decoded using the UTF-8 charset (multi-byte
 * charsets just aren't worth the increased complication in indexing).
 *
 * @author hogye
 */
public interface Bytes
{

    public static final Charset utf8 = Charset.forName( "UTF-8" );

    /**
     * Value at index i
     */
    byte v( int i );

    /**
     * Length of the sequence
     */
    int n( );

    boolean isEmpty( );

    byte first( );

    byte last( );

    void copyTo( int i, byte[] dest, int iDest, int c );

    void copyTo( int i, ByteBuffer dest, int c );

    void copyTo( ByteBuffer dest );

    byte[] copyOf( int i, int c );

    byte[] copyOf( );

    /**
     * Strings are encoded and decoded using the UTF-8 charset (multi-byte
     * charsets just aren't worth the increased complication in indexing).
     *
     * XXX: This is probably broken, because UTF-8 *is* a multi-byte charset
     */
    String string( int i, int c );

    String string( );

}
