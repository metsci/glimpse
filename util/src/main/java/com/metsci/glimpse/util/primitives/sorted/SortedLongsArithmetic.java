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
package com.metsci.glimpse.util.primitives.sorted;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.math.BigInteger;
import java.nio.LongBuffer;

import com.metsci.glimpse.util.primitives.Longs;

/**
 * @author hogye
 */
public class SortedLongsArithmetic implements SortedLongs
{

    public final long v0;
    public final long vStep;
    public final int n;

    public final double oneOverVStep;

    /**
     * @throws IllegalArgumentException if
     *         {@code vStep} is non-positive, or
     *         {@code n} is negative, or
     *         {@code (v0 + (n-1)*vStep)} is greater than {@link Long#MAX_VALUE}
     */
    public SortedLongsArithmetic( long v0, long vStep, int n )
    {
        if ( vStep <= 0 ) throw new IllegalArgumentException( "vStep must be positive: vStep = " + vStep );
        if ( n < 0 ) throw new IllegalArgumentException( "n must be non-negative: n = " + n );

        if ( n > 0 )
        {
            // vMax = v0 + (n-1)*vStep
            BigInteger vMax = big( v0 ).add( big( n - 1 ).multiply( big( vStep ) ) );
            if ( vMax.compareTo( big( Long.MAX_VALUE ) ) > 0 ) throw new IllegalArgumentException( "Max value is larger than Long.MAX_VALUE: v0 = " + v0 + ", vStep = " + vStep + ", n = " + n );
        }

        this.v0 = v0;
        this.vStep = vStep;
        this.n = n;

        this.oneOverVStep = 1.0 / ( ( double ) vStep );
    }

    public static BigInteger big( long x )
    {
        return BigInteger.valueOf( x );
    }

    public static BigInteger big( int x )
    {
        return BigInteger.valueOf( x );
    }

    @Override
    public long v( int i )
    {
        // Skip bounds checks for speed
        //if (i < 0) throw new ArrayIndexOutOfBoundsException("Array index out of range: index = " + i);
        //if (i >= n) throw new ArrayIndexOutOfBoundsException("Array index out of range: index = " + i + ", length = " + n);

        return v0 + i * vStep;
    }

    @Override
    public int n( )
    {
        return n;
    }

    @Override
    public void copyTo( int i, long[] dest, int iDest, int c )
    {
        long v = v0 + i * vStep;
        for ( int j = 0; j < c; j++ )
        {
            dest[iDest + j] = v;
            v += vStep;
        }
    }

    @Override
    public long[] copyOf( int i, int c )
    {
        long[] copy = new long[c];
        copyTo( i, copy, 0, c );
        return copy;
    }

    @Override
    public void copyTo( int i, LongBuffer dest, int c )
    {
        long v = v0 + i * vStep;
        for ( int j = 0; j < c; j++ )
        {
            dest.put( v );
            v += vStep;
        }
    }

    @Override
    public void copyTo( LongBuffer dest )
    {
        copyTo( 0, dest, n );
    }

    @Override
    public long[] copyOf( )
    {
        long[] copy = new long[n];
        copyTo( 0, copy, 0, n );
        return copy;
    }

    @Override
    public boolean isEmpty( )
    {
        return ( n == 0 );
    }

    @Override
    public long first( )
    {
        return v0;
    }

    @Override
    public long last( )
    {
        return v( n - 1 );
    }

    @Override
    public int indexOf( long x )
    {
        long offset = x - v0;
        if ( offset < 0 ) return -1;

        long i = ( offset + vStep - 1 ) / vStep;
        if ( i >= n ) return - ( n + 1 );

        boolean divisible = ( i * vStep == offset );
        return ( int ) ( divisible ? i : - ( i + 1 ) );
    }

    @Override
    public int indexNearest( long x )
    {
        long offset = x - v0;
        if ( offset <= 0 ) return 0;

        long i = ( offset + ( vStep >>> 1 ) ) / vStep;
        return min( ( int ) i, n - 1 );
    }

    @Override
    public int indexBefore( long x )
    {
        return indexAtOrBefore( x - 1 );
    }

    @Override
    public int indexAfter( long x )
    {
        return indexAtOrBefore( x ) + 1;
    }

    @Override
    public int indexAtOrBefore( long x )
    {
        long offset = x - v0;
        if ( offset < 0 ) return -1;

        long i = offset / vStep;
        return min( ( int ) i, n - 1 );
    }

    @Override
    public int indexAtOrAfter( long x )
    {
        return indexAtOrBefore( x - 1 ) + 1;
    }

    @Override
    public void continuousIndexOf( long x, ContinuousIndex result )
    {
        long offset = x - v0;
        long i = max( 0, min( n - 2, ( offset / vStep ) ) );
        float f = ( float ) ( ( offset - i * vStep ) * oneOverVStep );

        result.set( ( int ) i, f );
    }

    @Override
    public ContinuousIndex continuousIndexOf( long x )
    {
        ContinuousIndex h = new ContinuousIndex( );
        continuousIndexOf( x, h );
        return h;
    }

    @Override
    public void continuousIndicesOf( Longs xs, ContinuousIndexArray result )
    {
        int nx = xs.n( );
        for ( int ix = 0; ix < nx; ix++ )
        {
            long x = xs.v( ix );

            long offset = x - v0;
            long i = max( 0, min( n - 2, ( offset / vStep ) ) );
            float f = ( float ) ( ( offset - i * vStep ) * oneOverVStep );

            result.put( ix, ( int ) i, f );
        }
    }

    @Override
    public ContinuousIndexArray continuousIndicesOf( Longs xs )
    {
        ContinuousIndexArray hs = new ContinuousIndexArray( xs.n( ) );
        continuousIndicesOf( xs, hs );
        return hs;
    }

    @Override
    public void continuousIndicesOf( SortedLongs xs, ContinuousIndexArray result )
    {
        long v0 = this.v0;
        long vStep = this.vStep;
        int n = this.n;
        double oneOverVStep = this.oneOverVStep;

        int nx = xs.n( );
        int ix = 0;

        // Zip through any xs smaller than v(1)
        long v1 = v( 1 );
        for ( ; ix < nx; ix++ )
        {
            long x = xs.v( ix );
            if ( x >= v1 ) break;

            float f = ( float ) ( ( x - v0 ) * oneOverVStep );
            result.put( ix, 0, f );
        }

        // Walk through the window where xs and vs overlap
        long vNextToLast = v( n - 2 );
        for ( ; ix < nx; ix++ )
        {
            long x = xs.v( ix );
            if ( x >= vNextToLast ) break;

            long offset = x - v0;
            long i = offset / vStep;
            float f = ( float ) ( ( offset - i * vStep ) * oneOverVStep );

            result.put( ix, ( int ) i, f );
        }

        // Zip through any xs larger than or equal to v(n-2)
        for ( ; ix < nx; ix++ )
        {
            long x = xs.v( ix );

            float f = ( float ) ( ( x - vNextToLast ) * oneOverVStep );
            result.put( ix, n - 2, f );
        }
    }

    @Override
    public ContinuousIndexArray continuousIndicesOf( SortedLongs xs )
    {
        ContinuousIndexArray hs = new ContinuousIndexArray( xs.n( ) );
        continuousIndicesOf( xs, hs );
        return hs;
    }

    /**
     * Like continuousIndicesOf, but in reverse: for each v in this sequence,
     * the continuous index at which v falls in xs.
     *
     * This may be faster than continuousIndicesOf when going from a sparser
     * sequence to a dense arithmetic sequence.
     */
    public ContinuousIndexArray continuousIndicesIn( SortedLongs xs )
    {
        long vStep = this.vStep;
        int n = this.n;
        ContinuousIndexArray hs = new ContinuousIndexArray( n );

        int i = 0;

        // Zip through any vs smaller than x(1)
        long x0 = xs.v( 0 );
        long x1 = xs.v( 1 );
        double oneOverXStep1 = 1.0 / ( ( double ) ( x1 - x0 ) );
        for ( ; i < n; i++ )
        {
            long v = v( i );
            if ( v >= x1 ) break;

            float fx = ( float ) ( ( v - x0 ) * oneOverXStep1 );
            hs.put( i, 0, fx );
        }

        // Walk through the window where xs and vs overlap
        long xb = xs.v( 1 );
        int ib = indexAtOrAfter( xb );
        int nx = xs.n( );
        for ( int ixb = 2; ixb < nx - 1 && ib < n; ixb++ )
        {
            int ixa = ixb - 1;
            long xa = xb;
            int ia = ib;

            xb = xs.v( ixb );
            ib = indexAtOrAfter( xb );

            double oneOverXStep = 1.0 / ( ( double ) ( xb - xa ) );

            long va = v( ia );
            long vb = v( ib );
            for ( long v = va; v < vb; v += vStep )
            {
                float fx = ( float ) ( ( v - xa ) * oneOverXStep );
                hs.put( i, ixa, fx );
                i++;
            }
        }

        // Zip through any vs larger than or equal to x(nx-2)
        long xLast = xs.v( nx - 1 );
        long xNextToLast = xs.v( nx - 2 );
        double oneOverXStepLast = 1.0 / ( ( double ) ( xLast - xNextToLast ) );
        for ( ; i < n; i++ )
        {
            long v = v( i );

            float fx = ( float ) ( ( v - xNextToLast ) * oneOverXStepLast );
            hs.put( i, nx - 2, fx );
        }

        return hs;
    }

}
