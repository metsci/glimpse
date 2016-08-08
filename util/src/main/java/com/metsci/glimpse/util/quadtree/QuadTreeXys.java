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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Like {@link QuadTreeObjects}, but sacrificing generality for search speed.
 *
 * Values inserted into a {@code QuadTreeXys} must be instances of the {@link Xy}
 * interface.
 *
 * HotSpot does a better job optimizing {@code v.x()} than {@code this.x(v)}.
 * This can improve search speed by 10% or more.
 *
 * @author hogye
 */
public class QuadTreeXys<V extends Xy> extends QuadTree<Collection<V>>
{

    protected final int maxBucketSize;

    public QuadTreeXys( int maxBucketSize )
    {
        super( new ArrayList<V>( ) );
        this.maxBucketSize = maxBucketSize;
    }

    public Collection<V> search( float xMin, float xMax, float yMin, float yMax )
    {
        Collection<V> results = new ArrayList<V>( );
        search( xMin, xMax, yMin, yMax, results );
        return results;
    }

    /**
     * @return The number of elements appended to {@code results}.
     */
    public int search( final float xMin, final float xMax, final float yMin, final float yMax, final Collection<V> results )
    {
        int nBefore = results.size( );

        accumulate( xMin, xMax, yMin, yMax, new Accumulator<Collection<V>>( )
        {
            public void accumulate( Collection<V> bucket, float xMinBucket, float xMaxBucket, float yMinBucket, float yMaxBucket )
            {
                boolean xAll = ( xMin <= xMinBucket && xMaxBucket <= xMax );
                boolean yAll = ( yMin <= yMinBucket && yMaxBucket <= yMax );

                if ( xAll && yAll )
                {
                    results.addAll( bucket );
                }
                else if ( xAll )
                {
                    for ( V v : bucket )
                    {
                        float y = v.y( );
                        if ( y < yMin || y > yMax ) continue;

                        results.add( v );
                    }
                }
                else if ( yAll )
                {
                    for ( V v : bucket )
                    {
                        float x = v.x( );
                        if ( x < xMin || x > xMax ) continue;

                        results.add( v );
                    }
                }
                else
                {
                    for ( V v : bucket )
                    {
                        float x = v.x( );
                        if ( x < xMin || x > xMax ) continue;

                        float y = v.y( );
                        if ( y < yMin || y > yMax ) continue;

                        results.add( v );
                    }
                }
            }
        } );

        return results.size( ) - nBefore;
    }

    public Collection<V> search( float xMin, float xMax, float yMin, float yMax, FilterObject<V> vFilter )
    {
        Collection<V> results = new ArrayList<V>( );
        search( xMin, xMax, yMin, yMax, vFilter, results );
        return results;
    }

    /**
     * @return The number of elements appended to {@code results}.
     */
    public int search( final float xMin, final float xMax, final float yMin, final float yMax, final FilterObject<V> vFilter, final Collection<V> results )
    {
        int nBefore = results.size( );

        accumulate( xMin, xMax, yMin, yMax, new Accumulator<Collection<V>>( )
        {
            public void accumulate( Collection<V> bucket, float xMinBucket, float xMaxBucket, float yMinBucket, float yMaxBucket )
            {
                boolean xAll = ( xMin <= xMinBucket && xMaxBucket <= xMax );
                boolean yAll = ( yMin <= yMinBucket && yMaxBucket <= yMax );

                if ( xAll && yAll )
                {
                    for ( V v : bucket )
                    {
                        if ( !vFilter.include( v ) ) continue;

                        results.add( v );
                    }
                }
                else if ( xAll )
                {
                    for ( V v : bucket )
                    {
                        if ( !vFilter.include( v ) ) continue;

                        float y = v.y( );
                        if ( y < yMin || y > yMax ) continue;

                        results.add( v );
                    }
                }
                else if ( yAll )
                {
                    for ( V v : bucket )
                    {
                        if ( !vFilter.include( v ) ) continue;

                        float x = v.x( );
                        if ( x < xMin || x > xMax ) continue;

                        results.add( v );
                    }
                }
                else
                {
                    for ( V v : bucket )
                    {
                        if ( !vFilter.include( v ) ) continue;

                        float x = v.x( );
                        if ( x < xMin || x > xMax ) continue;

                        float y = v.y( );
                        if ( y < yMin || y > yMax ) continue;

                        results.add( v );
                    }
                }
            }
        } );

        return results.size( ) - nBefore;
    }

    /**
     * If {@code v.x()} or {@code v.y()} returns {@code NaN}, this method returns
     * immediately without adding {@code v} to the tree.
     */
    public void add( V v )
    {
        float x = v.x( );
        if ( Float.isNaN( x ) ) return;

        float y = v.y( );
        if ( Float.isNaN( y ) ) return;

        LeafNode<Collection<V>> leaf = leaf( x, y );
        Collection<V> bucket = leaf.bucket;

        bucket.add( v );

        if ( bucket.size( ) > maxBucketSize ) splitLeaf( leaf );
    }

    @Override
    protected void chooseDividers( float xMin, float xMax, float yMin, float yMax, Collection<V> bucket, float[] result )
    {
        double oneOverSize = 1.0 / bucket.size( );
        double xMean = 0;
        double yMean = 0;
        for ( V v : bucket )
        {
            xMean += truncInf( v.x( ) ) * oneOverSize;
            yMean += truncInf( v.y( ) ) * oneOverSize;
        }
        result[0] = truncInf( ( float ) xMean );
        result[1] = truncInf( ( float ) yMean );
    }

    @Override
    protected Collection<V>[] splitBucket( Collection<V> bucket, float xDivider, float yDivider )
    {
        @SuppressWarnings( "unchecked" )
        Collection<V>[] newBuckets = new Collection[4];
        for ( int q = 0; q < 4; q++ )
            newBuckets[q] = new ArrayList<V>( );

        for ( V v : bucket )
        {
            int q = quadrant( xDivider, yDivider, v.x( ), v.y( ) );
            newBuckets[q].add( v );
        }

        return newBuckets;
    }

    @Override
    protected int bucketSize( Collection<V> bucket )
    {
        return bucket.size( );
    }

    public void remove( V v )
    {
        float x = v.x( );
        float y = v.y( );
        leaf( x, y ).bucket.remove( v );
    }

}
