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
