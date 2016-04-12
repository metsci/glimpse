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
package com.metsci.glimpse.util.quadtree;

import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;
import static java.lang.Math.max;

import com.metsci.glimpse.util.primitives.Longs;
import com.metsci.glimpse.util.primitives.LongsArray;
import com.metsci.glimpse.util.primitives.LongsModifiable;
import com.metsci.glimpse.util.quadtree.QuadTreeLongs2.Bucket;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * @author hogye
 */
public abstract class QuadTreeLongs2 extends QuadTree<Bucket>
{

    protected static class Bucket
    {
        public final LongsArray singles;
        public final Long2ObjectOpenHashMap<LongsArray> dupes;

        public Bucket( )
        {
            singles = new LongsArray( );
            dupes = new Long2ObjectOpenHashMap<LongsArray>( );
            dupes.defaultReturnValue( singles );
        }
    }

    protected final int maxBucketSize;

    public QuadTreeLongs2( int maxBucketSize )
    {
        super( new Bucket( ) );
        this.maxBucketSize = maxBucketSize;
    }

    public abstract float x( long v1, long v2 );

    public abstract float y( long v1, long v2 );

    public Longs search( float xMin, float xMax, float yMin, float yMax )
    {
        LongsModifiable results = new LongsArray( );
        search( xMin, xMax, yMin, yMax, results );
        return results;
    }

    /**
     * @return The number of elements appended to {@code results}.
     */
    public int search( final float xMin, final float xMax, final float yMin, final float yMax, final LongsModifiable results )
    {
        int nBefore = results.n( );

        accumulate( xMin, xMax, yMin, yMax, new Accumulator<Bucket>( )
        {
            public void accumulate( Bucket bucket, float xMinBucket, float xMaxBucket, float yMinBucket, float yMaxBucket )
            {
                boolean xAll = ( xMin <= xMinBucket && xMaxBucket <= xMax );
                boolean yAll = ( yMin <= yMinBucket && yMaxBucket <= yMax );
                Long2ObjectOpenHashMap<LongsArray> dupes = bucket.dupes;
                LongsArray singles = bucket.singles;
                long[] a = singles.a;
                int n = singles.n;

                if ( xAll && yAll )
                {
                    results.append( singles );

                    for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        LongsArray vs = en.getValue( );
                        results.append( vs );
                    }
                }

                else if ( xAll )
                {
                    for ( int i = 0; i < n; i += 2 )
                    {
                        long v1 = a[i];
                        long v2 = a[i + 1];

                        float y = y( v1, v2 );
                        if ( y < yMin || y > yMax ) continue;

                        results.append( a, i, i + 2 );
                    }

                    for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float y = yFromKey( xyKey );
                        if ( y < yMin || y > yMax ) continue;

                        LongsArray vs = en.getValue( );
                        results.append( vs );
                    }
                }

                else if ( yAll )
                {
                    for ( int i = 0; i < n; i += 2 )
                    {
                        long v1 = a[i];
                        long v2 = a[i + 1];

                        float x = x( v1, v2 );
                        if ( x < xMin || x > xMax ) continue;

                        results.append( a, i, i + 2 );
                    }

                    for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float x = xFromKey( xyKey );
                        if ( x < xMin || x > xMax ) continue;

                        LongsArray vs = en.getValue( );
                        results.append( vs );
                    }
                }

                else
                {
                    for ( int i = 0; i < n; i += 2 )
                    {
                        long v1 = a[i];
                        long v2 = a[i + 1];

                        float x = x( v1, v2 );
                        if ( x < xMin || x > xMax ) continue;

                        float y = y( v1, v2 );
                        if ( y < yMin || y > yMax ) continue;

                        results.append( a, i, i + 2 );
                    }

                    for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float x = xFromKey( xyKey );
                        if ( x < xMin || x > xMax ) continue;

                        float y = yFromKey( xyKey );
                        if ( y < yMin || y > yMax ) continue;

                        LongsArray vs = en.getValue( );
                        results.append( vs );
                    }
                }
            }
        } );

        return ( results.n( ) - nBefore ) / 2;
    }

    public Longs search( float xMin, float xMax, float yMin, float yMax, FilterLong2 vFilter )
    {
        LongsModifiable results = new LongsArray( );
        search( xMin, xMax, yMin, yMax, vFilter, results );
        return results;
    }

    /**
     * @return The number of elements appended to {@code results}.
     */
    public int search( final float xMin, final float xMax, final float yMin, final float yMax, final FilterLong2 vFilter, final LongsModifiable results )
    {
        int nBefore = results.n( );

        accumulate( xMin, xMax, yMin, yMax, new Accumulator<Bucket>( )
        {
            public void accumulate( Bucket bucket, float xMinBucket, float xMaxBucket, float yMinBucket, float yMaxBucket )
            {
                boolean xAll = ( xMin <= xMinBucket && xMaxBucket <= xMax );
                boolean yAll = ( yMin <= yMinBucket && yMaxBucket <= yMax );
                Long2ObjectOpenHashMap<LongsArray> dupes = bucket.dupes;
                LongsArray singles = bucket.singles;
                long[] a = singles.a;
                int n = singles.n;

                if ( xAll && yAll )
                {
                    appendFiltered( singles, vFilter, results );

                    for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        LongsArray vs = en.getValue( );
                        appendFiltered( vs, vFilter, results );
                    }
                }

                else if ( xAll )
                {
                    for ( int i = 0; i < n; i += 2 )
                    {
                        long v1 = a[i];
                        long v2 = a[i + 1];
                        if ( !vFilter.include( v1, v2 ) ) continue;

                        float y = y( v1, v2 );
                        if ( y < yMin || y > yMax ) continue;

                        results.append( a, i, i + 2 );
                    }

                    for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float y = yFromKey( xyKey );
                        if ( y < yMin || y > yMax ) continue;

                        LongsArray vs = en.getValue( );
                        appendFiltered( vs, vFilter, results );
                    }
                }

                else if ( yAll )
                {
                    for ( int i = 0; i < n; i += 2 )
                    {
                        long v1 = a[i];
                        long v2 = a[i + 1];
                        if ( !vFilter.include( v1, v2 ) ) continue;

                        float x = x( v1, v2 );
                        if ( x < xMin || x > xMax ) continue;

                        results.append( a, i, i + 2 );
                    }

                    for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float x = xFromKey( xyKey );
                        if ( x < xMin || x > xMax ) continue;

                        LongsArray vs = en.getValue( );
                        appendFiltered( vs, vFilter, results );
                    }
                }

                else
                {
                    for ( int i = 0; i < n; i += 2 )
                    {
                        long v1 = a[i];
                        long v2 = a[i + 1];
                        if ( !vFilter.include( v1, v2 ) ) continue;

                        float x = x( v1, v2 );
                        if ( x < xMin || x > xMax ) continue;

                        float y = y( v1, v2 );
                        if ( y < yMin || y > yMax ) continue;

                        results.append( a, i, i + 2 );
                    }

                    for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float x = xFromKey( xyKey );
                        if ( x < xMin || x > xMax ) continue;

                        float y = yFromKey( xyKey );
                        if ( y < yMin || y > yMax ) continue;

                        LongsArray vs = en.getValue( );
                        appendFiltered( vs, vFilter, results );
                    }
                }
            }
        } );

        return ( results.n( ) - nBefore ) / 2;
    }

    /**
     * If {@code x(v1,v2)} or {@code y(v1,v2)} returns {@code NaN}, this method returns
     * immediately without adding {@code v1,v2} to the tree.
     */
    public void add( long v1, long v2 )
    {
        float x = x( v1, v2 );
        if ( Float.isNaN( x ) ) return;

        float y = y( v1, v2 );
        if ( Float.isNaN( y ) ) return;

        LeafNode<Bucket> leaf = leaf( x, y );
        Bucket bucket = leaf.bucket;

        // The default return value for bucket.dupes is set to bucket.singles,
        // so iff bucket.dupes does not contain xyKey, we will end up appending
        // to bucket.singles -- which is exactly what we want.
        //
        // This is confusing to read, but it avoids double-checking the "contains
        // key?" condition. The map is already doing the check for us -- not worth
        // doing the same check again.
        //
        long xyKey = xyToKey( x, y );
        append2( bucket.dupes.get( xyKey ), v1, v2 );

        if ( bucketSize( bucket ) > maxBucketSize )
        {
            compactBucket( bucket );
            if ( bucketSize( bucket ) > 0.9 * maxBucketSize ) splitLeaf( leaf );
        }
    }

    protected void compactBucket( Bucket bucket )
    {
        LongsArray singles = bucket.singles;
        Long2ObjectOpenHashMap<LongsArray> dupes = bucket.dupes;
        long[] a = singles.a;
        int n = singles.n;

        Long2ObjectOpenHashMap<LongsArray> dupesNew = new Long2ObjectOpenHashMap<LongsArray>( );
        dupesNew.defaultReturnValue( null );
        for ( int i = 0; i < n; i += 2 )
        {
            long v1 = a[i];
            long v2 = a[i + 1];
            long xyKey = xyToKey( x( v1, v2 ), y( v1, v2 ) );

            LongsArray vs = dupesNew.get( xyKey );
            if ( vs == null )
            {
                vs = new LongsArray( new long[16], 0 );
                dupesNew.put( xyKey, vs );
            }
            vs.append( a, i, i + 2 );
        }

        singles.n = 0;
        int dupeThreshold = 2 * max( 2, ( int ) ( 0.1 * maxBucketSize ) );
        for ( Entry<LongsArray> en : dupesNew.long2ObjectEntrySet( ) )
        {
            LongsArray vs = en.getValue( );
            if ( vs.n >= dupeThreshold )
            {
                long xyKey = en.getLongKey( );
                dupes.put( xyKey, vs );
            }
            else
            {
                singles.append( vs );
            }
        }
    }

    @Override
    protected void chooseDividers( float xMin, float xMax, float yMin, float yMax, Bucket bucket, float[] result )
    {
        Long2ObjectOpenHashMap<LongsArray> dupes = bucket.dupes;
        LongsArray singles = bucket.singles;
        long[] a = singles.a;
        int n = singles.n;

        double oneOverSize = 1.0 / ( n / 2 + dupes.size( ) );

        double xMean = 0;
        double yMean = 0;

        for ( int i = 0; i < n; i += 2 )
        {
            long v1 = a[i];
            long v2 = a[i + 1];
            xMean += truncInf( x( v1, v2 ) ) * oneOverSize;
            yMean += truncInf( y( v1, v2 ) ) * oneOverSize;
        }

        for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
        {
            long xyKey = en.getLongKey( );
            xMean += truncInf( xFromKey( xyKey ) ) * oneOverSize;
            yMean += truncInf( yFromKey( xyKey ) ) * oneOverSize;
        }

        result[0] = truncInf( ( float ) xMean );
        result[1] = truncInf( ( float ) yMean );
    }

    @Override
    protected Bucket[] splitBucket( Bucket bucket, float xDivider, float yDivider )
    {
        Bucket[] newBuckets = new Bucket[4];
        for ( int q = 0; q < newBuckets.length; q++ )
            newBuckets[q] = new Bucket( );

        long[] a = bucket.singles.a;
        int n = bucket.singles.n;
        for ( int i = 0; i < n; i += 2 )
        {
            long v1 = a[i];
            long v2 = a[i + 1];
            int q = quadrant( xDivider, yDivider, x( v1, v2 ), y( v1, v2 ) );
            newBuckets[q].singles.append( a, i, i + 2 );
        }

        Long2ObjectOpenHashMap<LongsArray> dupes = bucket.dupes;

        for ( Entry<LongsArray> en : dupes.long2ObjectEntrySet( ) )
        {
            long xyKey = en.getLongKey( );
            LongsArray vs = en.getValue( );
            int q = quadrant( xDivider, yDivider, xFromKey( xyKey ), yFromKey( xyKey ) );
            newBuckets[q].dupes.put( xyKey, vs );
        }

        return newBuckets;
    }

    @Override
    protected int bucketSize( Bucket bucket )
    {
        return bucket.singles.n / 2 + bucket.dupes.size( );
    }

    public void remove( long v1, long v2 )
    {
        float x = x( v1, v2 );
        float y = y( v1, v2 );
        Bucket bucket = leaf( x, y ).bucket;

        // The default return value for bucket.dupes is set to bucket.singles.
        // See note in add() for the reasoning behind this confusing choice.
        //
        long xyKey = xyToKey( x, y );
        LongsArray vs = bucket.dupes.get( xyKey );
        remove2( vs, v1, v2 );
        if ( vs != bucket.singles && vs.n == 0 ) bucket.dupes.remove( xyKey );
    }

    public static void appendFiltered( LongsArray from, FilterLong2 filter, LongsModifiable to )
    {
        long[] a = from.a;
        int n = from.n;

        for ( int i = 0; i < n; i += 2 )
        {
            long v1 = a[i];
            long v2 = a[i + 1];
            if ( filter.include( v1, v2 ) ) to.append( a, i, i + 2 );
        }
    }

    protected static long xyToKey( float x, float y )
    {
        long xBits = ( ( long ) floatToRawIntBits( x ) ) << 32;
        long yBits = ( ( long ) floatToRawIntBits( y ) ) & 0x00000000FFFFFFFFL;
        return ( xBits | yBits );
    }

    protected static float xFromKey( long xyKey )
    {
        return intBitsToFloat( ( int ) ( xyKey >>> 32 ) );
    }

    protected static float yFromKey( long xyKey )
    {
        return intBitsToFloat( ( int ) xyKey );
    }

    protected static void append2( LongsArray longs, long v1, long v2 )
    {
        longs.prepForAppend( 2 );
        longs.a[longs.n - 2] = v1;
        longs.a[longs.n - 1] = v2;
    }

    protected static void remove2( LongsArray longs, long v1, long v2 )
    {
        long[] a = longs.a;
        int n = longs.n;

        for ( int i = 0; i < n; i += 2 )
        {
            if ( a[i] == v1 && a[i + 1] == v2 )
            {
                System.arraycopy( a, i + 2, a, i, n - ( i + 2 ) );
                longs.n -= 2;
                return;
            }
        }
    }

}
