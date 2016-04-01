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

import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.math.BigDecimal;
import java.nio.FloatBuffer;

import com.metsci.glimpse.util.primitives.Floats;

/**
 * @author hogye
 */
public class SortedFloatsArithmetic implements SortedFloats
{

    public final float v0;
    public final float vStep;
    public final int n;

    public final float oneOverVStep;
    public final float halfVStep;

    /**
     * @throws IllegalArgumentException if
     *         {@code v0} is NaN, or
     *         {@code v0} is infinite, or
     *         {@code vStep} is NaN, or
     *         {@code vStep} is infinite, or
     *         {@code vStep} is non-positive, or
     *         {@code n} is negative, or
     *         {@code (v0 + (n-1)*vStep)} is greater than {@link Float#MAX_VALUE}
     */
    public SortedFloatsArithmetic( float v0, float vStep, int n )
    {
        if ( Float.isNaN( v0 ) ) throw new IllegalArgumentException( "v0 must be not be NaN" );
        if ( Float.isInfinite( v0 ) ) throw new IllegalArgumentException( "v0 must be finite: v0 = " + v0 );
        if ( Float.isNaN( vStep ) ) throw new IllegalArgumentException( "vStep must be not be NaN" );
        if ( Float.isInfinite( vStep ) ) throw new IllegalArgumentException( "vStep must be finite: vStep = " + vStep );
        if ( vStep <= 0 ) throw new IllegalArgumentException( "vStep must be positive: vStep = " + vStep );
        if ( n < 0 ) throw new IllegalArgumentException( "n must be non-negative: n = " + n );

        if ( n > 0 )
        {
            // vMax = v0 + (n-1)*vStep
            BigDecimal vMax = big( v0 ).add( big( n - 1 ).multiply( big( vStep ) ) );
            if ( vMax.compareTo( big( Float.MAX_VALUE ) ) > 0 ) throw new IllegalArgumentException( "Max value is larger than Float.MAX_VALUE: v0 = " + v0 + ", vStep = " + vStep + ", n = " + n );
        }

        this.v0 = v0;
        this.vStep = vStep;
        this.n = n;

        this.oneOverVStep = 1.0f / vStep;
        this.halfVStep = 0.5f * vStep;
    }

    public static BigDecimal big( float x )
    {
        return BigDecimal.valueOf( x );
    }

    @Override
    public float v( int i )
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
    public void copyTo( int i, float[] dest, int iDest, int c )
    {
        float v = v0 + i * vStep;
        for ( int j = 0; j < c; j++ )
        {
            dest[iDest + j] = v;
            v += vStep;
        }
    }

    @Override
    public void copyTo( int i, FloatBuffer dest, int c )
    {
        float v = v0 + i * vStep;
        for ( int j = 0; j < c; j++ )
        {
            dest.put( v );
            v += vStep;
        }
    }

    @Override
    public void copyTo( FloatBuffer dest )
    {
        copyTo( 0, dest, n );
    }

    @Override
    public float[] copyOf( int i, int c )
    {
        float[] copy = new float[c];
        copyTo( i, copy, 0, c );
        return copy;
    }

    @Override
    public float[] copyOf( )
    {
        float[] copy = new float[n];
        copyTo( 0, copy, 0, n );
        return copy;
    }

    @Override
    public boolean isEmpty( )
    {
        return ( n == 0 );
    }

    @Override
    public float first( )
    {
        return v0;
    }

    @Override
    public float last( )
    {
        return v( n - 1 );
    }

    @Override
    public int indexOf( float x )
    {
        float offset = x - v0;
        if ( offset < 0 ) return -1;

        long i = ( long ) ceil( offset * oneOverVStep );
        if ( i >= n ) return - ( n + 1 );

        boolean exact = ( x == ( v0 + i * vStep ) );
        return ( int ) ( exact ? i : - ( i + 1 ) );
    }

    @Override
    public int indexNearest( float x )
    {
        float offset = x - v0;
        if ( offset <= 0 ) return 0;

        int i = ( int ) ( ( offset + halfVStep ) * oneOverVStep );
        return min( i, n - 1 );
    }

    @Override
    public int indexBefore( float x )
    {
        return indexAtOrBefore( x - 1 );
    }

    @Override
    public int indexAfter( float x )
    {
        return indexAtOrBefore( x ) + 1;
    }

    @Override
    public int indexAtOrBefore( float x )
    {
        float offset = x - v0;
        if ( offset < 0 ) return -1;

        int i = ( int ) ( offset * oneOverVStep );
        return min( i, n - 1 );
    }

    @Override
    public int indexAtOrAfter( float x )
    {
        return indexAtOrBefore( x - 1 ) + 1;
    }

    @Override
    public void continuousIndexOf( float x, ContinuousIndex result )
    {
        float offset = x - v0;
        long i = max( 0, min( n - 2, ( int ) ( offset * oneOverVStep ) ) );
        float f = ( ( offset - i * vStep ) * oneOverVStep );

        result.set( ( int ) i, f );
    }

    @Override
    public ContinuousIndex continuousIndexOf( float x )
    {
        ContinuousIndex h = new ContinuousIndex( );
        continuousIndexOf( x, h );
        return h;
    }

    @Override
    public void continuousIndicesOf( Floats xs, ContinuousIndexArray result )
    {
        int nx = xs.n( );
        for ( int ix = 0; ix < nx; ix++ )
        {
            float x = xs.v( ix );

            float offset = x - v0;
            long i = max( 0, min( n - 2, ( int ) ( offset * oneOverVStep ) ) );
            float f = ( float ) ( ( offset - i * vStep ) * oneOverVStep );

            result.put( ix, ( int ) i, f );
        }
    }

    @Override
    public ContinuousIndexArray continuousIndicesOf( Floats xs )
    {
        ContinuousIndexArray hs = new ContinuousIndexArray( xs.n( ) );
        continuousIndicesOf( xs, hs );
        return hs;
    }

    @Override
    public void continuousIndicesOf( SortedFloats xs, ContinuousIndexArray result )
    {
        float v0 = this.v0;
        float vStep = this.vStep;
        int n = this.n;
        float oneOverVStep = this.oneOverVStep;

        int nx = xs.n( );
        int ix = 0;

        // Zip through any xs smaller than v(1)
        float v1 = v( 1 );
        for ( ; ix < nx; ix++ )
        {
            float x = xs.v( ix );
            if ( x >= v1 ) break;

            float f = ( ( x - v0 ) * oneOverVStep );
            result.put( ix, 0, f );
        }

        // Walk through the window where xs and vs overlap
        float vNextToLast = v( n - 2 );
        for ( ; ix < nx; ix++ )
        {
            float x = xs.v( ix );
            if ( x >= vNextToLast ) break;

            float offset = x - v0;
            long i = ( int ) ( offset * oneOverVStep );
            float f = ( float ) ( ( offset - i * vStep ) * oneOverVStep );

            result.put( ix, ( int ) i, f );
        }

        // Zip through any xs larger than or equal to v(n-2)
        for ( ; ix < nx; ix++ )
        {
            float x = xs.v( ix );

            float f = ( ( x - vNextToLast ) * oneOverVStep );
            result.put( ix, n - 2, f );
        }
    }

    @Override
    public ContinuousIndexArray continuousIndicesOf( SortedFloats xs )
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
    public ContinuousIndexArray continuousIndicesIn( SortedFloats xs )
    {
        float vStep = this.vStep;
        int n = this.n;
        ContinuousIndexArray hs = new ContinuousIndexArray( n );

        int i = 0;

        // Zip through any vs smaller than x(1)
        float x0 = xs.v( 0 );
        float x1 = xs.v( 1 );
        float oneOverXStep1 = 1.0f / ( x1 - x0 );
        for ( ; i < n; i++ )
        {
            float v = v( i );
            if ( v >= x1 ) break;

            float fx = ( ( v - x0 ) * oneOverXStep1 );
            hs.put( i, 0, fx );
        }

        // Walk through the window where xs and vs overlap
        float xb = xs.v( 1 );
        int ib = indexAtOrAfter( xb );
        int nx = xs.n( );
        for ( int ixb = 2; ixb < nx - 1 && ib < n; ixb++ )
        {
            int ixa = ixb - 1;
            float xa = xb;
            int ia = ib;

            xb = xs.v( ixb );
            ib = indexAtOrAfter( xb );

            float oneOverXStep = 1.0f / ( xb - xa );

            float va = v( ia );
            float vb = v( ib );
            for ( float v = va; v < vb; v += vStep )
            {
                float fx = ( ( v - xa ) * oneOverXStep );
                hs.put( i, ixa, fx );
                i++;
            }
        }

        // Zip through any vs larger than or equal to x(nx-2)
        float xLast = xs.v( nx - 1 );
        float xNextToLast = xs.v( nx - 2 );
        float oneOverXStepLast = 1.0f / ( xLast - xNextToLast );
        for ( ; i < n; i++ )
        {
            float v = v( i );

            float fx = ( ( v - xNextToLast ) * oneOverXStepLast );
            hs.put( i, nx - 2, fx );
        }

        return hs;
    }

}
