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

import java.io.Serializable;

/**
 * A generic class for pairs of objects.<p>
 *
 * @author hogye
 */
public class Pair<A, B> implements Serializable
{

    private static final long serialVersionUID = -7315628434083418938L;

    public static <A, B> Pair<A, B> newPair( A first, B second )
    {
        return new Pair<A, B>( first, second );
    }

    private final A first;
    private final B second;

    public Pair( A first, B second )
    {
        this.first = first;
        this.second = second;
    }

    /**
     * @return The first element of the pair
     */
    public A first( )
    {
        return first;
    }

    /**
     * @return The second element of the pair
     */
    public B second( )
    {
        return second;
    }

    @Override
    public String toString( )
    {
        return getClass( ).getSimpleName( ) + "[" + first + "," + second + "]";
    }

    @Override
    public int hashCode( )
    {
        final int prime = 967;
        int result = 1;
        result = prime * result + ( first == null ? 0 : first.hashCode( ) );
        result = prime * result + ( second == null ? 0 : second.hashCode( ) );
        return result;
    }

    protected static boolean areEqual( Object a, Object b )
    {
        return ( a == null ? b == null : a.equals( b ) );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        Pair<?, ?> other = ( Pair<?, ?> ) o;
        return areEqual( other.first, first ) && areEqual( other.second, second );
    }

}
