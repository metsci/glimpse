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
package com.metsci.glimpse.util.primitives.sorted;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Arrays;

import com.metsci.glimpse.util.primitives.Longs;
import com.metsci.glimpse.util.primitives.LongsArray;

/**
 * @author hogye
 */
public class SortedLongsArray extends LongsArray implements SortedLongsModifiable
{

    /**
     * If the values of {@code a} are not in ascending order, the behavior of
     * this instance is undefined.
     *
     * For efficiency, does <em>not</em> clone the array arg.
     */
    public SortedLongsArray( long[] a )
    {
        super( a );
    }

    public SortedLongsArray( int n )
    {
        super( n );
    }

    public SortedLongsArray( )
    {
        super( );
    }

    /**
     * If the values of {@code a} are not in ascending order, the behavior of
     * this instance is undefined.
     *
     * For efficiency, does <em>not</em> clone the array arg.
     */
    public SortedLongsArray( long[] a, int n )
    {
        super( a, n );
    }

    /**
     * If the values of {@code xs} are not in ascending order, the behavior of
     * this instance is undefined.
     *
     * Clones the sequence arg.
     */
    public SortedLongsArray( Longs xs )
    {
        super( xs );
    }

    @Override
    public int indexOf( long x )
    {
        return Arrays.binarySearch( a, 0, n, x );
    }

    @Override
    public int indexNearest( long x )
    {
        int i = indexOf( x );

        // Exact value found
        if ( i >= 0 ) return i;

        // Find the closer of the adjacent values
        int iAfter = -i - 1;
        int iBefore = iAfter - 1;

        if ( iAfter >= this.n ) return iBefore;
        if ( iBefore < 0 ) return iAfter;

        long[] a = this.a;
        long diffAfter = a[iAfter] - x;
        long diffBefore = x - a[iBefore];

        return ( diffAfter <= diffBefore ? iAfter : iBefore );
    }

    @Override
    public int indexAfter( long x )
    {
        int i = indexOf( x );

        // Exact value not found
        if ( i < 0 ) return ( -i - 1 );

        // If the exact value was found, find the value's
        // last occurrence
        long[] a = this.a;
        int n = this.n;
        for ( int j = i + 1; j < n; j++ )
        {
            if ( a[j] > x ) return j;
        }
        return n;
    }

    @Override
    public int indexAtOrAfter( long x )
    {
        int i = indexOf( x );

        // Exact value not found
        if ( i < 0 ) return ( -i - 1 );

        // If the exact value was found, find the value's
        // first occurrence
        long[] a = this.a;
        for ( int j = i; j > 0; j-- )
        {
            if ( a[j - 1] < x ) return j;
        }
        return 0;
    }

    @Override
    public int indexBefore( long x )
    {
        return indexAtOrAfter( x ) - 1;
    }

    @Override
    public int indexAtOrBefore( long x )
    {
        return indexAfter( x ) - 1;
    }

    /**
     * @throws RuntimeException if n is less than 2 and the exact value is not found
     */
    @Override
    public void continuousIndexOf( long x, ContinuousIndex result )
    {
        int i = indexOf( x );
        if ( i >= 0 )
        {
            // Exact value found
            result.set( i, 0 );
        }
        else
        {
            // Find the continuous index between values
            int n = this.n;
            int iAfter = max( 1, min( n - 1, ( -i - 1 ) ) );
            int iBefore = iAfter - 1;

            long[] a = this.a;
            long vBefore = a[iBefore];
            long vAfter = a[iAfter];
            float f = ( ( float ) ( x - vBefore ) ) / ( ( float ) ( vAfter - vBefore ) );

            result.set( iBefore, f );
        }
    }

    /**
     * @throws RuntimeException if n is less than 2 and the exact value is not found
     */
    @Override
    public ContinuousIndex continuousIndexOf( long x )
    {
        ContinuousIndex h = new ContinuousIndex( );
        continuousIndexOf( x, h );
        return h;
    }

    /**
     * @throws RuntimeException if n is less than 2
     */
    @Override
    public void continuousIndicesOf( Longs xs, ContinuousIndexArray result )
    {
        int n = this.n;
        if ( n < 2 ) throw new RuntimeException( );

        int nx = xs.n( );

        ContinuousIndex h = new ContinuousIndex( );
        for ( int ix = 0; ix < nx; ix++ )
        {
            long x = xs.v( ix );
            continuousIndexOf( x, h );
            result.put( ix, h );
        }
    }

    /**
     * @throws RuntimeException if n is less than 2
     */
    @Override
    public ContinuousIndexArray continuousIndicesOf( Longs xs )
    {
        ContinuousIndexArray hs = new ContinuousIndexArray( xs.n( ) );
        continuousIndicesOf( xs, hs );
        return hs;
    }

    /**
     * @throws RuntimeException if n is less than 2
     */
    @Override
    public void continuousIndicesOf( SortedLongs xs, ContinuousIndexArray result )
    {
        long[] a = this.a;
        int n = this.n;
        if ( n < 2 ) throw new RuntimeException( );

        int nx = xs.n( );
        int ix = 0;

        // Zip through any xs smaller than v(1)
        long v0 = a[0];
        long v1 = a[1];
        float oneOverDenom1 = 1f / ( ( float ) ( v1 - v0 ) );
        for ( ; ix < nx; ix++ )
        {
            long x = xs.v( ix );
            if ( x >= v1 ) break;

            float f = ( ( float ) ( x - v0 ) ) * oneOverDenom1;
            result.put( ix, 0, f );
        }

        // Walk through the window where xs and vs overlap
        if ( ix >= nx ) return;
        int i = indexAtOrAfter( xs.v( ix ) );
        long vNextToLast = a[n - 2];
        for ( ; ix < nx; ix++ )
        {
            long x = xs.v( ix );
            if ( x >= vNextToLast ) break;

            while ( a[i] < x )
                i++;

            long v = a[i];

            // It's tempting to put a special case here for when v == x.
            // However, that case is probably not super-common, and the
            // extra if statement slows down the common case.

            int iBefore = i - 1;
            long vBefore = a[iBefore];
            float f = ( ( float ) ( x - vBefore ) ) / ( ( float ) ( v - vBefore ) );
            result.put( ix, iBefore, f );
        }

        // Zip through any xs larger than or equal to v(n-2)
        long vLast = a[n - 1];
        float oneOverDenom2 = 1f / ( ( float ) ( vLast - vNextToLast ) );
        for ( ; ix < nx; ix++ )
        {
            long x = xs.v( ix );

            float f = ( ( float ) ( x - vNextToLast ) ) * oneOverDenom2;
            result.put( ix, n - 2, f );
        }
    }

    /**
     * @throws RuntimeException if n is less than 2
     */
    @Override
    public ContinuousIndexArray continuousIndicesOf( SortedLongs xs )
    {
        ContinuousIndexArray hs = new ContinuousIndexArray( xs.n( ) );
        continuousIndicesOf( xs, hs );
        return hs;
    }

    @Override
    public int add( long v )
    {
        int i = indexAfter( v );
        insert( i, v );
        return i;
    }

    @Override
    public SortedLongsArray copy( )
    {
        return new SortedLongsArray( this.a.clone( ), this.n );
    }

}
