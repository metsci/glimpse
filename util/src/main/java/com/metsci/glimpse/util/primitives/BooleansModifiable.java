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

/**
 * @author hogye
 */
public interface BooleansModifiable extends Booleans
{

    void set( int i, boolean v );

    void set( int i, boolean[] vs );

    void set( int i, boolean[] vs, int from, int to );

    void insert( int i, boolean v );

    void insert( int i, boolean[] vs );

    void insert( int i, Booleans vs );

    void insert( int i, boolean[] vs, int from, int to );

    void insert( int i, Booleans vs, int from, int to );

    void append( boolean v );

    void append( boolean[] vs );

    void append( Booleans vs );

    void append( boolean[] vs, int from, int to );

    void append( Booleans vs, int from, int to );

    void prepend( boolean v );

    void prepend( boolean[] vs );

    void prepend( Booleans vs );

    void prepend( boolean[] vs, int from, int to );

    void prepend( Booleans vs, int from, int to );

    /**
     * Removes a single copy of the specified value. If multiple copies
     * are present, there is no guarantee which one will be removed.
     */
    void remove( boolean v );

    /**
     * Removes values starting at index from (inclusive) to index to (exclusive).
     * All other values with indices greater than or equal to index to have
     * their index in the array decreased by to-from and the size of the array is
     * decreased by to-from.
     */
    void removeRange( int from, int to );

    /**
     * Remove value at index. All other values with indices greater than index have
     * their index in the array decreased by one and the size of the array is
     * decreased by one.
     */
    void removeIndex( int index );

    void clear( );

    void ensureCapacity( int minCapacity );

    void compact( );

}
