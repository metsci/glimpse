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

import com.metsci.glimpse.util.primitives.Ints;
import com.metsci.glimpse.util.primitives.IntsArray;
import com.metsci.glimpse.util.primitives.IntsModifiable;
import com.metsci.glimpse.util.quadtree.QuadTreeInts.Bucket;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * @author hogye
 */
public abstract class QuadTreeInts extends QuadTree<Bucket>
{

    protected static class Bucket
    {
        public final IntsArray singles;
        public final Long2ObjectOpenHashMap<IntsArray> dupes;

        public Bucket( )
        {
            singles = new IntsArray( );
            dupes = new Long2ObjectOpenHashMap<IntsArray>( );
            dupes.defaultReturnValue( singles );
        }
    }

    protected final int maxBucketSize;

    public QuadTreeInts( int maxBucketSize )
    {
        super( new Bucket( ) );
        this.maxBucketSize = maxBucketSize;
    }

    public abstract float x( int v );

    public abstract float y( int v );

    public Ints search( float xMin, float xMax, float yMin, float yMax )
    {
        IntsModifiable results = new IntsArray( );
        search( xMin, xMax, yMin, yMax, results );
        return results;
    }

    /**
     * @return The number of elements appended to {@code results}.
     */
    public int search( final float xMin, final float xMax, final float yMin, final float yMax, final IntsModifiable results )
    {
        int nBefore = results.n( );

        accumulate( xMin, xMax, yMin, yMax, new Accumulator<Bucket>( )
        {
            public void accumulate( Bucket bucket, float xMinBucket, float xMaxBucket, float yMinBucket, float yMaxBucket )
            {
                boolean xAll = ( xMin <= xMinBucket && xMaxBucket <= xMax );
                boolean yAll = ( yMin <= yMinBucket && yMaxBucket <= yMax );
                Long2ObjectOpenHashMap<IntsArray> dupes = bucket.dupes;
                IntsArray singles = bucket.singles;
                int[] a = singles.a;
                int n = singles.n;

                if ( xAll && yAll )
                {
                    results.append( singles );

                    for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        IntsArray vs = en.getValue( );
                        results.append( vs );
                    }
                }

                else if ( xAll )
                {
                    for ( int i = 0; i < n; i++ )
                    {
                        int v = a[i];

                        float y = y( v );
                        if ( y < yMin || y > yMax ) continue;

                        results.append( v );
                    }

                    for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float y = yFromKey( xyKey );
                        if ( y < yMin || y > yMax ) continue;

                        IntsArray vs = en.getValue( );
                        results.append( vs );
                    }
                }

                else if ( yAll )
                {
                    for ( int i = 0; i < n; i++ )
                    {
                        int v = a[i];

                        float x = x( v );
                        if ( x < xMin || x > xMax ) continue;

                        results.append( v );
                    }

                    for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float x = xFromKey( xyKey );
                        if ( x < xMin || x > xMax ) continue;

                        IntsArray vs = en.getValue( );
                        results.append( vs );
                    }
                }

                else
                {
                    for ( int i = 0; i < n; i++ )
                    {
                        int v = a[i];

                        float x = x( v );
                        if ( x < xMin || x > xMax ) continue;

                        float y = y( v );
                        if ( y < yMin || y > yMax ) continue;

                        results.append( v );
                    }

                    for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float x = xFromKey( xyKey );
                        if ( x < xMin || x > xMax ) continue;

                        float y = yFromKey( xyKey );
                        if ( y < yMin || y > yMax ) continue;

                        IntsArray vs = en.getValue( );
                        results.append( vs );
                    }
                }
            }
        } );

        return results.n( ) - nBefore;
    }

    public Ints search( float xMin, float xMax, float yMin, float yMax, FilterInt vFilter )
    {
        IntsModifiable results = new IntsArray( );
        search( xMin, xMax, yMin, yMax, vFilter, results );
        return results;
    }

    /**
     * @return The number of elements appended to {@code results}.
     */
    public int search( final float xMin, final float xMax, final float yMin, final float yMax, final FilterInt vFilter, final IntsModifiable results )
    {
        int nBefore = results.n( );

        accumulate( xMin, xMax, yMin, yMax, new Accumulator<Bucket>( )
        {
            public void accumulate( Bucket bucket, float xMinBucket, float xMaxBucket, float yMinBucket, float yMaxBucket )
            {
                boolean xAll = ( xMin <= xMinBucket && xMaxBucket <= xMax );
                boolean yAll = ( yMin <= yMinBucket && yMaxBucket <= yMax );
                Long2ObjectOpenHashMap<IntsArray> dupes = bucket.dupes;
                IntsArray singles = bucket.singles;
                int[] a = singles.a;
                int n = singles.n;

                if ( xAll && yAll )
                {
                    appendFiltered( singles, vFilter, results );

                    for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        IntsArray vs = en.getValue( );
                        appendFiltered( vs, vFilter, results );
                    }
                }

                else if ( xAll )
                {
                    for ( int i = 0; i < n; i++ )
                    {
                        int v = a[i];
                        if ( !vFilter.include( v ) ) continue;

                        float y = y( v );
                        if ( y < yMin || y > yMax ) continue;

                        results.append( v );
                    }

                    for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float y = yFromKey( xyKey );
                        if ( y < yMin || y > yMax ) continue;

                        IntsArray vs = en.getValue( );
                        appendFiltered( vs, vFilter, results );
                    }
                }

                else if ( yAll )
                {
                    for ( int i = 0; i < n; i++ )
                    {
                        int v = a[i];
                        if ( !vFilter.include( v ) ) continue;

                        float x = x( v );
                        if ( x < xMin || x > xMax ) continue;

                        results.append( v );
                    }

                    for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float x = xFromKey( xyKey );
                        if ( x < xMin || x > xMax ) continue;

                        IntsArray vs = en.getValue( );
                        appendFiltered( vs, vFilter, results );
                    }
                }

                else
                {
                    for ( int i = 0; i < n; i++ )
                    {
                        int v = a[i];
                        if ( !vFilter.include( v ) ) continue;

                        float x = x( v );
                        if ( x < xMin || x > xMax ) continue;

                        float y = y( v );
                        if ( y < yMin || y > yMax ) continue;

                        results.append( v );
                    }

                    for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
                    {
                        long xyKey = en.getLongKey( );

                        float x = xFromKey( xyKey );
                        if ( x < xMin || x > xMax ) continue;

                        float y = yFromKey( xyKey );
                        if ( y < yMin || y > yMax ) continue;

                        IntsArray vs = en.getValue( );
                        appendFiltered( vs, vFilter, results );
                    }
                }
            }
        } );

        return results.n( ) - nBefore;
    }

    /**
     * If {@code x(v)} or {@code y(v)} returns {@code NaN}, this method returns
     * immediately without adding {@code v} to the tree.
     */
    public void add( int v )
    {
        float x = x( v );
        if ( Float.isNaN( x ) ) return;

        float y = y( v );
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
        bucket.dupes.get( xyKey ).append( v );

        if ( bucketSize( bucket ) > maxBucketSize )
        {
            compactBucket( bucket );
            if ( bucketSize( bucket ) > 0.9 * maxBucketSize ) splitLeaf( leaf );
        }
    }

    protected void compactBucket( Bucket bucket )
    {
        IntsArray singles = bucket.singles;
        Long2ObjectOpenHashMap<IntsArray> dupes = bucket.dupes;
        int[] a = singles.a;
        int n = singles.n;

        Long2ObjectOpenHashMap<IntsArray> dupesNew = new Long2ObjectOpenHashMap<IntsArray>( );
        dupesNew.defaultReturnValue( null );
        for ( int i = 0; i < n; i++ )
        {
            int v = a[i];
            long xyKey = xyToKey( x( v ), y( v ) );

            IntsArray vs = dupesNew.get( xyKey );
            if ( vs == null )
            {
                vs = new IntsArray( new int[8], 0 );
                dupesNew.put( xyKey, vs );
            }
            vs.append( v );
        }

        singles.n = 0;
        int dupeThreshold = max( 2, ( int ) ( 0.1 * maxBucketSize ) );
        for ( Entry<IntsArray> en : dupesNew.long2ObjectEntrySet( ) )
        {
            IntsArray vs = en.getValue( );
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
        Long2ObjectOpenHashMap<IntsArray> dupes = bucket.dupes;
        IntsArray singles = bucket.singles;
        int[] a = singles.a;
        int n = singles.n;

        double oneOverSize = 1.0 / ( n + dupes.size( ) );

        double xMean = 0;
        double yMean = 0;

        for ( int i = 0; i < n; i++ )
        {
            int v = a[i];
            xMean += truncInf( x( v ) ) * oneOverSize;
            yMean += truncInf( y( v ) ) * oneOverSize;
        }

        for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
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

        int[] a = bucket.singles.a;
        int n = bucket.singles.n;
        for ( int i = 0; i < n; i++ )
        {
            int v = a[i];
            int q = quadrant( xDivider, yDivider, x( v ), y( v ) );
            newBuckets[q].singles.append( v );
        }

        Long2ObjectOpenHashMap<IntsArray> dupes = bucket.dupes;

        for ( Entry<IntsArray> en : dupes.long2ObjectEntrySet( ) )
        {
            long xyKey = en.getLongKey( );
            IntsArray vs = en.getValue( );
            int q = quadrant( xDivider, yDivider, xFromKey( xyKey ), yFromKey( xyKey ) );
            newBuckets[q].dupes.put( xyKey, vs );
        }

        return newBuckets;
    }

    @Override
    protected int bucketSize( Bucket bucket )
    {
        return bucket.singles.n + bucket.dupes.size( );
    }

    public void remove( int v )
    {
        float x = x( v );
        float y = y( v );
        Bucket bucket = leaf( x, y ).bucket;

        // The default return value for bucket.dupes is set to bucket.singles.
        // See note in add() for the reasoning behind this confusing choice.
        //
        long xyKey = xyToKey( x, y );
        IntsArray vs = bucket.dupes.get( xyKey );
        vs.remove( v );
        if ( vs != bucket.singles && vs.n == 0 ) bucket.dupes.remove( xyKey );
    }

    public static void appendFiltered( IntsArray from, FilterInt filter, IntsModifiable to )
    {
        int[] a = from.a;
        int n = from.n;

        for ( int i = 0; i < n; i++ )
        {
            int v = a[i];
            if ( filter.include( v ) ) to.append( v );
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

}
