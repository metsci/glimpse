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
package com.metsci.glimpse.util;

/**
 * A generic class for triplets modeled after Pair.java.
 *
 * @author hogye
 */
public class Triplet<A, B, C>
{
    private final A _first;
    private final B _second;
    private final C _third;

    public Triplet( A first, B second, C third )
    {
        _first = first;
        _second = second;
        _third = third;
    }

    /**
     * Returns the first element.
     */
    public A first( )
    {
        return _first;
    }

    /**
     * Returns the second element.
     */
    public B second( )
    {
        return _second;
    }

    /**
     * Returns the third element.
     */
    public C third( )
    {
        return _third;
    }

    @Override
    public String toString( )
    {
        return Triplet.class.getSimpleName( ) + "[" + _first + "," + _second + "," + _third + "]";
    }

    private static boolean equals( Object x, Object y )
    {
        return ( x == null && y == null ) || ( x != null && x.equals( y ) );
    }

    @Override
    public boolean equals( Object other )
    {
        if ( other instanceof Triplet<?, ?, ?> )
        {
            Triplet<?, ?, ?> t = ( Triplet<?, ?, ?> ) other;
            return equals( _first, t._first ) && equals( _second, t._second ) && equals( _third, t._third );
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode( )
    {
        int hashcode = 0;

        if ( _first != null )
        {
            hashcode += _first.hashCode( );
        }

        if ( _second != null )
        {
            hashcode += _second.hashCode( ) * 17;
        }

        if ( _third != null )
        {
            hashcode += _third.hashCode( ) * 29;
        }

        return hashcode;
    }

    public static final <A, B, C> Triplet<A, B, C> make( A a, B b, C c )
    {
        return new Triplet<A, B, C>( a, b, c );
    }
}
