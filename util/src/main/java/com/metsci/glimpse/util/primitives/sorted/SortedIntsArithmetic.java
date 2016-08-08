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
import java.nio.IntBuffer;

import com.metsci.glimpse.util.primitives.Ints;

/**
 * @author hogye
 */
public class SortedIntsArithmetic implements SortedInts
{

    public final int v0;
    public final int vStep;
    public final int n;

    public final double oneOverVStep;

    /**
     * @throws IllegalArgumentException if
     *         {@code vStep} is non-positive, or
     *         {@code n} is negative, or
     *         {@code (v0 + (n-1)*vStep)} is greater than {@link Integer#MAX_VALUE}
     */
    public SortedIntsArithmetic( int v0, int vStep, int n )
    {
        if ( vStep <= 0 ) throw new IllegalArgumentException( "vStep must be positive: vStep = " + vStep );
        if ( n < 0 ) throw new IllegalArgumentException( "n must be non-negative: n = " + n );

        if ( n > 0 )
        {
            // vMax = v0 + (n-1)*vStep
            BigInteger vMax = big( v0 ).add( big( n - 1 ).multiply( big( vStep ) ) );
            if ( vMax.compareTo( big( Integer.MAX_VALUE ) ) > 0 ) throw new IllegalArgumentException( "Max value is larger than Integer.MAX_VALUE: v0 = " + v0 + ", vStep = " + vStep + ", n = " + n );
        }

        this.v0 = v0;
        this.vStep = vStep;
        this.n = n;

        this.oneOverVStep = 1.0 / ( ( double ) vStep );
    }

    public static BigInteger big( int x )
    {
        return BigInteger.valueOf( x );
    }

    @Override
    public int v( int i )
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
    public void copyTo( int i, int[] dest, int iDest, int c )
    {
        int v = v0 + i * vStep;
        for ( int j = 0; j < c; j++ )
        {
            dest[iDest + j] = v;
            v += vStep;
        }
    }

    @Override
    public void copyTo( int i, IntBuffer dest, int c )
    {
        int v = v0 + i * vStep;
        for ( int j = 0; j < c; j++ )
        {
            dest.put( v );
            v += vStep;
        }
    }

    @Override
    public void copyTo( IntBuffer dest )
    {
        copyTo( 0, dest, n );
    }

    @Override
    public int[] copyOf( int i, int c )
    {
        int[] copy = new int[c];
        copyTo( i, copy, 0, c );
        return copy;
    }

    @Override
    public int[] copyOf( )
    {
        int[] copy = new int[n];
        copyTo( 0, copy, 0, n );
        return copy;
    }

    @Override
    public boolean isEmpty( )
    {
        return ( n == 0 );
    }

    @Override
    public int first( )
    {
        return v0;
    }

    @Override
    public int last( )
    {
        return v( n - 1 );
    }

    @Override
    public int indexOf( int x )
    {
        int offset = x - v0;
        if ( offset < 0 ) return -1;

        int i = ( offset + vStep - 1 ) / vStep;
        if ( i >= n ) return - ( n + 1 );

        boolean divisible = ( i * vStep == offset );
        return ( divisible ? i : - ( i + 1 ) );
    }

    @Override
    public int indexNearest( int x )
    {
        int offset = x - v0;
        if ( offset <= 0 ) return 0;

        int i = ( offset + ( vStep >>> 1 ) ) / vStep;
        return min( i, n - 1 );
    }

    @Override
    public int indexBefore( int x )
    {
        return indexAtOrBefore( x - 1 );
    }

    @Override
    public int indexAfter( int x )
    {
        return indexAtOrBefore( x ) + 1;
    }

    @Override
    public int indexAtOrBefore( int x )
    {
        int offset = x - v0;
        if ( offset < 0 ) return -1;

        int i = offset / vStep;
        return min( i, n - 1 );
    }

    @Override
    public int indexAtOrAfter( int x )
    {
        return indexAtOrBefore( x - 1 ) + 1;
    }

    @Override
    public void continuousIndexOf( int x, ContinuousIndex result )
    {
        int offset = x - v0;
        int i = max( 0, min( n - 2, ( offset / vStep ) ) );
        float f = ( float ) ( ( offset - i * vStep ) * oneOverVStep );

        result.set( i, f );
    }

    @Override
    public ContinuousIndex continuousIndexOf( int x )
    {
        ContinuousIndex h = new ContinuousIndex( );
        continuousIndexOf( x, h );
        return h;
    }

    @Override
    public void continuousIndicesOf( Ints xs, ContinuousIndexArray result )
    {
        int nx = xs.n( );
        for ( int ix = 0; ix < nx; ix++ )
        {
            int x = xs.v( ix );

            int offset = x - v0;
            int i = max( 0, min( n - 2, ( offset / vStep ) ) );
            float f = ( float ) ( ( offset - i * vStep ) * oneOverVStep );

            result.put( ix, i, f );
        }
    }

    @Override
    public ContinuousIndexArray continuousIndicesOf( Ints xs )
    {
        ContinuousIndexArray hs = new ContinuousIndexArray( xs.n( ) );
        continuousIndicesOf( xs, hs );
        return hs;
    }

    @Override
    public void continuousIndicesOf( SortedInts xs, ContinuousIndexArray result )
    {
        int v0 = this.v0;
        int vStep = this.vStep;
        int n = this.n;
        double oneOverVStep = this.oneOverVStep;

        int nx = xs.n( );
        int ix = 0;

        // Zip through any xs smaller than v(1)
        int v1 = v( 1 );
        for ( ; ix < nx; ix++ )
        {
            int x = xs.v( ix );
            if ( x >= v1 ) break;

            float f = ( float ) ( ( x - v0 ) * oneOverVStep );
            result.put( ix, 0, f );
        }

        // Walk through the window where xs and vs overlap
        int vNextToLast = v( n - 2 );
        for ( ; ix < nx; ix++ )
        {
            int x = xs.v( ix );
            if ( x >= vNextToLast ) break;

            int offset = x - v0;
            int i = offset / vStep;
            float f = ( float ) ( ( offset - i * vStep ) * oneOverVStep );

            result.put( ix, i, f );
        }

        // Zip through any xs larger than or equal to v(n-2)
        for ( ; ix < nx; ix++ )
        {
            int x = xs.v( ix );

            float f = ( float ) ( ( x - vNextToLast ) * oneOverVStep );
            result.put( ix, n - 2, f );
        }
    }

    @Override
    public ContinuousIndexArray continuousIndicesOf( SortedInts xs )
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
    public ContinuousIndexArray continuousIndicesIn( SortedInts xs )
    {
        int vStep = this.vStep;
        int n = this.n;
        ContinuousIndexArray hs = new ContinuousIndexArray( n );

        int i = 0;

        // Zip through any vs smaller than x(1)
        int x0 = xs.v( 0 );
        int x1 = xs.v( 1 );
        double oneOverXStep1 = 1.0 / ( ( double ) ( x1 - x0 ) );
        for ( ; i < n; i++ )
        {
            int v = v( i );
            if ( v >= x1 ) break;

            float fx = ( float ) ( ( v - x0 ) * oneOverXStep1 );
            hs.put( i, 0, fx );
        }

        // Walk through the window where xs and vs overlap
        int xb = xs.v( 1 );
        int ib = indexAtOrAfter( xb );
        int nx = xs.n( );
        for ( int ixb = 2; ixb < nx - 1 && ib < n; ixb++ )
        {
            int ixa = ixb - 1;
            int xa = xb;
            int ia = ib;

            xb = xs.v( ixb );
            ib = indexAtOrAfter( xb );

            double oneOverXStep = 1.0 / ( ( double ) ( xb - xa ) );

            int va = v( ia );
            int vb = v( ib );
            for ( int v = va; v < vb; v += vStep )
            {
                float fx = ( float ) ( ( v - xa ) * oneOverXStep );
                hs.put( i, ixa, fx );
                i++;
            }
        }

        // Zip through any vs larger than or equal to x(nx-2)
        int xLast = xs.v( nx - 1 );
        int xNextToLast = xs.v( nx - 2 );
        double oneOverXStepLast = 1.0 / ( ( double ) ( xLast - xNextToLast ) );
        for ( ; i < n; i++ )
        {
            int v = v( i );

            float fx = ( float ) ( ( v - xNextToLast ) * oneOverXStepLast );
            hs.put( i, nx - 2, fx );
        }

        return hs;
    }

}
