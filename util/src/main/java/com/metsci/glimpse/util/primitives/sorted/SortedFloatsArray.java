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

import java.util.Arrays;

import com.metsci.glimpse.util.primitives.Floats;
import com.metsci.glimpse.util.primitives.FloatsArray;

/**
 * @author hogye
 */
public class SortedFloatsArray extends FloatsArray implements SortedFloatsModifiable
{

    // Instantiation

    /**
     * If the values of {@code a} are not in ascending order, the behavior of
     * this instance is undefined.
     *
     * For efficiency, does <em>not</em> clone the array arg.
     */
    public SortedFloatsArray( float[] a )
    {
        super( a );
    }

    public SortedFloatsArray( int n )
    {
        super( n );
    }

    public SortedFloatsArray( )
    {
        super( );
    }

    /**
     * If the values of {@code a} are not in ascending order, the behavior of
     * this instance is undefined.
     *
     * For efficiency, does <em>not</em> clone the array arg.
     */
    public SortedFloatsArray( float[] a, int n )
    {
        super( a, n );
    }

    /**
     * If the values of {@code xs} are not in ascending order, the behavior of
     * this instance is undefined.
     *
     * Clones the sequence arg.
     */
    public SortedFloatsArray( Floats xs )
    {
        super( xs );
    }

    // Search

    @Override
    public int indexOf( float x )
    {
        return Arrays.binarySearch( a, 0, n, x );
    }

    @Override
    public int indexNearest( float x )
    {
        int i = indexOf( x );

        // Exact value found
        if ( i >= 0 ) return i;

        // Find the closer of the adjacent values
        int iAfter = -i - 1;
        int iBefore = iAfter - 1;

        if ( iAfter >= this.n ) return iBefore;
        if ( iBefore < 0 ) return iAfter;

        float[] a = this.a;
        float diffAfter = a[iAfter] - x;
        float diffBefore = x - a[iBefore];

        return ( diffAfter <= diffBefore ? iAfter : iBefore );
    }

    @Override
    public int indexAfter( float x )
    {
        int i = indexOf( x );

        // Exact value not found
        if ( i < 0 ) return ( -i - 1 );

        // If the exact value was found, find the value's
        // last occurrence
        float[] a = this.a;
        int n = this.n;
        for ( int j = i + 1; j < n; j++ )
        {
            if ( a[j] > x ) return j;
        }
        return n;
    }

    @Override
    public int indexAtOrAfter( float x )
    {
        int i = indexOf( x );

        // Exact value not found
        if ( i < 0 ) return ( -i - 1 );

        // If the exact value was found, find the value's
        // first occurrence
        float[] a = this.a;
        for ( int j = i; j > 0; j-- )
        {
            if ( a[j - 1] < x ) return j;
        }
        return 0;
    }

    @Override
    public int indexBefore( float x )
    {
        return indexAtOrAfter( x ) - 1;
    }

    @Override
    public int indexAtOrBefore( float x )
    {
        return indexAfter( x ) - 1;
    }

    /**
     * @throws RuntimeException if n is less than 2 and the exact value is not found
     */
    @Override
    public void continuousIndexOf( float x, ContinuousIndex result )
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

            float[] a = this.a;
            float vBefore = a[iBefore];
            float vAfter = a[iAfter];
            float f = ( ( x - vBefore ) / ( vAfter - vBefore ) );

            result.set( iBefore, f );
        }
    }

    /**
     * @throws RuntimeException if n is less than 2 and the exact value is not found
     */
    @Override
    public ContinuousIndex continuousIndexOf( float x )
    {
        ContinuousIndex h = new ContinuousIndex( );
        continuousIndexOf( x, h );
        return h;
    }

    /**
     * @throws RuntimeException if n is less than 2
     */
    @Override
    public void continuousIndicesOf( Floats xs, ContinuousIndexArray result )
    {
        int n = this.n;
        if ( n < 2 ) throw new RuntimeException( );

        int nx = xs.n( );

        ContinuousIndex h = new ContinuousIndex( );
        for ( int ix = 0; ix < nx; ix++ )
        {
            float x = xs.v( ix );
            continuousIndexOf( x, h );
            result.put( ix, h );
        }
    }

    /**
     * @throws RuntimeException if n is less than 2
     */
    @Override
    public ContinuousIndexArray continuousIndicesOf( Floats xs )
    {
        ContinuousIndexArray hs = new ContinuousIndexArray( xs.n( ) );
        continuousIndicesOf( xs, hs );
        return hs;
    }

    /**
     * @throws RuntimeException if n is less than 2
     */
    @Override
    public void continuousIndicesOf( SortedFloats xs, ContinuousIndexArray result )
    {
        float[] a = this.a;
        int n = this.n;
        if ( n < 2 ) throw new RuntimeException( );

        int nx = xs.n( );
        int ix = 0;

        // Zip through any xs smaller than v(1)
        float v0 = a[0];
        float v1 = a[1];
        float oneOverVStep1 = 1.0f / ( v1 - v0 );
        for ( ; ix < nx; ix++ )
        {
            float x = xs.v( ix );
            if ( x >= v1 ) break;

            float f = ( ( x - v0 ) * oneOverVStep1 );
            result.put( ix, 0, f );
        }

        // Walk through the window where xs and vs overlap
        if ( ix >= nx ) return;
        int i = indexAtOrAfter( xs.v( ix ) );
        float vNextToLast = a[n - 2];
        for ( ; ix < nx; ix++ )
        {
            float x = xs.v( ix );
            if ( x >= vNextToLast ) break;

            while ( a[i] < x )
                i++;

            float v = a[i];

            // It's tempting to put a special case here for when v == x.
            // However, that case is probably not super-common, and the
            // extra if statement slows down the common case.

            int iBefore = i - 1;
            float vBefore = a[iBefore];
            float f = ( ( x - vBefore ) / ( v - vBefore ) );
            result.put( ix, iBefore, f );
        }

        // Zip through any xs larger than or equal to v(n-2)
        float vLast = a[n - 1];
        float oneOverVStepLast = 1.0f / ( vLast - vNextToLast );
        for ( ; ix < nx; ix++ )
        {
            float x = xs.v( ix );

            float f = ( ( x - vNextToLast ) * oneOverVStepLast );
            result.put( ix, n - 2, f );
        }
    }

    /**
     * @throws RuntimeException if n is less than 2
     */
    @Override
    public ContinuousIndexArray continuousIndicesOf( SortedFloats xs )
    {
        ContinuousIndexArray hs = new ContinuousIndexArray( xs.n( ) );
        continuousIndicesOf( xs, hs );
        return hs;
    }

    // Mutators

    @Override
    public int add( float v )
    {
        int i = indexAfter( v );
        insert( i, v );
        return i;
    }

}
