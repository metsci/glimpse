/*
 * Copyright (c) 2020, Metron, Inc.
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

import java.util.function.Predicate;

public class PredicateUtils
{

    /**
     * Call {@code pred.test( t )}. If the result is true, return the {@code t} argument. Otherwise, throw
     * a {@link RuntimeException}.
     */
    public static <T> T require( T t, Predicate<? super T> pred )
    {
        if ( !pred.test( t ) )
        {
            throw new RuntimeException( );
        }
        return t;
    }


    public static final Predicate<Object> alwaysTrue = ( v ) -> true;

    public static final Predicate<Object> alwaysFalse = ( v ) -> false;

    public static final Predicate<Object> notNull = ( v ) -> ( v != null );


    public static Predicate<Double> greaterThan( double x )
    {
        return ( v ) -> ( v != null && v > x );
    }

    public static Predicate<Double> atLeast( double x )
    {
        return ( v ) -> ( v != null && v >= x );
    }

    public static Predicate<Double> lessThan( double x )
    {
        return ( v ) -> ( v != null && v < x );
    }

    public static Predicate<Double> atMost( double x )
    {
        return ( v ) -> ( v != null && v <= x );
    }

    public static Predicate<Double> between( double min, boolean minInclusive, double max, boolean maxInclusive )
    {
        return ( v ) -> ( v != null && ( ( min < v && v < max ) || ( minInclusive && v == min ) || ( maxInclusive && v == max ) ) );
    }


    public static Predicate<Integer> greaterThan( int x )
    {
        return ( v ) -> ( v != null && v > x );
    }

    public static Predicate<Integer> atLeast( int x )
    {
        return ( v ) -> ( v != null && v >= x );
    }

    public static Predicate<Integer> lessThan( int x )
    {
        return ( v ) -> ( v != null && v < x );
    }

    public static Predicate<Integer> atMost( int x )
    {
        return ( v ) -> ( v != null && v <= x );
    }

    public static Predicate<Integer> between( int min, boolean minInclusive, int max, boolean maxInclusive )
    {
        return ( v ) -> ( v != null && ( ( min < v && v < max ) || ( minInclusive && v == min ) || ( maxInclusive && v == max ) ) );
    }

}
