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
package com.metsci.glimpse.util.quadtree.longvalued;

import java.util.ArrayList;
import java.util.Collection;

import com.metsci.glimpse.util.quadtree.FilterObject;

/**
 * @author hogye
 * @author ulman
 */
public abstract class LongQuadTreeObjects<V> extends LongQuadTree<Collection<V>>
{
    protected final int maxBucketSize;

    public LongQuadTreeObjects( int maxBucketSize )
    {
        super( new ArrayList<V>( ) );
        this.maxBucketSize = maxBucketSize;
    }

    public abstract long x( V v );

    public abstract long y( V v );

    public Collection<V> search( long xMin, long xMax, long yMin, long yMax )
    {
        Collection<V> results = new ArrayList<V>( );
        search( xMin, xMax, yMin, yMax, results );
        return results;
    }

    /**
     * @return The number of elements appended to {@code results}.
     */
    public int search( final long xMin, final long xMax, final long yMin, final long yMax, final Collection<V> results )
    {
        int nBefore = results.size( );

        accumulate( xMin, xMax, yMin, yMax, new Accumulator<Collection<V>>( )
        {
            public void accumulate( Collection<V> bucket, long xMinBucket, long xMaxBucket, long yMinBucket, long yMaxBucket )
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
                        long y = y( v );
                        if ( y < yMin || y > yMax ) continue;

                        results.add( v );
                    }
                }
                else if ( yAll )
                {
                    for ( V v : bucket )
                    {
                        long x = x( v );
                        if ( x < xMin || x > xMax ) continue;

                        results.add( v );
                    }
                }
                else
                {
                    for ( V v : bucket )
                    {
                        long x = x( v );
                        if ( x < xMin || x > xMax ) continue;

                        long y = y( v );
                        if ( y < yMin || y > yMax ) continue;

                        results.add( v );
                    }
                }
            }
        } );

        return results.size( ) - nBefore;
    }

    public Collection<V> search( long xMin, long xMax, long yMin, long yMax, FilterObject<V> vFilter )
    {
        Collection<V> results = new ArrayList<V>( );
        search( xMin, xMax, yMin, yMax, vFilter, results );
        return results;
    }

    /**
     * @return The number of elements appended to {@code results}.
     */
    public int search( final long xMin, final long xMax, final long yMin, final long yMax, final FilterObject<V> vFilter, final Collection<V> results )
    {
        int nBefore = results.size( );

        accumulate( xMin, xMax, yMin, yMax, new Accumulator<Collection<V>>( )
        {
            public void accumulate( Collection<V> bucket, long xMinBucket, long xMaxBucket, long yMinBucket, long yMaxBucket )
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

                        long y = y( v );
                        if ( y < yMin || y > yMax ) continue;

                        results.add( v );
                    }
                }
                else if ( yAll )
                {
                    for ( V v : bucket )
                    {
                        if ( !vFilter.include( v ) ) continue;

                        long x = x( v );
                        if ( x < xMin || x > xMax ) continue;

                        results.add( v );
                    }
                }
                else
                {
                    for ( V v : bucket )
                    {
                        if ( !vFilter.include( v ) ) continue;

                        long x = x( v );
                        if ( x < xMin || x > xMax ) continue;

                        long y = y( v );
                        if ( y < yMin || y > yMax ) continue;

                        results.add( v );
                    }
                }
            }
        } );

        return results.size( ) - nBefore;
    }

    /**
     * If {@code x(v)} or {@code y(v)} returns {@code NaN}, this method returns
     * immediately without adding {@code v} to the tree.
     */
    public void add( V v )
    {
        long x = x( v );
        long y = y( v );

        LeafNode<Collection<V>> leaf = leaf( x, y );
        Collection<V> bucket = leaf.bucket;

        bucket.add( v );

        if ( bucket.size( ) > maxBucketSize ) splitLeaf( leaf );
    }

    @Override
    protected void chooseDividers( long xMin, long xMax, long yMin, long yMax, Collection<V> bucket, long[] result )
    {
        double oneOverSize = 1.0 / bucket.size( );
        double xMean = 0;
        double yMean = 0;
        for ( V v : bucket )
        {
            xMean += x( v ) * oneOverSize;
            yMean += y( v ) * oneOverSize;
        }
        result[0] = ( long ) xMean;
        result[1] = ( long ) yMean;
    }

    @Override
    protected Collection<V>[] splitBucket( Collection<V> bucket, long xDivider, long yDivider )
    {
        @SuppressWarnings( "unchecked" )
        Collection<V>[] newBuckets = new Collection[4];
        for ( int q = 0; q < 4; q++ )
            newBuckets[q] = new ArrayList<V>( );

        for ( V v : bucket )
        {
            int q = quadrant( xDivider, yDivider, x( v ), y( v ) );
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
        long x = x( v );
        long y = y( v );
        leaf( x, y ).bucket.remove( v );
    }

    public static class Event
    {
        public long start;
        public long end;
        public String id;

        public Event( long start, long end, String id )
        {
            this.start = start;
            this.end = end;
            this.id = id;
        }

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( id == null ) ? 0 : id.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            Event other = ( Event ) obj;
            if ( id == null )
            {
                if ( other.id != null ) return false;
            }
            else if ( !id.equals( other.id ) ) return false;
            return true;
        }

        @Override
        public String toString( )
        {
            return id;
        }
    }

    public static void main( String[] args )
    {
        LongQuadTreeObjects<Event> tree = new LongQuadTreeObjects<Event>( 10 )
        {

            @Override
            public long x( com.metsci.glimpse.util.quadtree.longvalued.LongQuadTreeObjects.Event v )
            {
                return v.start;
            }

            @Override
            public long y( com.metsci.glimpse.util.quadtree.longvalued.LongQuadTreeObjects.Event v )
            {
                return v.end;
            }

        };

        tree.add( new Event( 1, 2, "a" ) );
        tree.add( new Event( 1, 10, "b" ) );
        tree.add( new Event( 2, 22, "c" ) );
        tree.add( new Event( 3, 6, "d" ) );
        tree.add( new Event( 4, 17, "e" ) );
        tree.add( new Event( 4, 18, "f" ) );
        tree.add( new Event( 5, 13, "g" ) );
        tree.add( new Event( 5, 17, "h" ) );
        tree.add( new Event( 7, 14, "i" ) );
        tree.add( new Event( 8, 9, "j" ) );
        tree.add( new Event( 8, 13, "k" ) );
        tree.add( new Event( 10, 17, "l" ) );
        tree.add( new Event( 13, 22, "m" ) );

        // find events overlapping [6,12]
        // expect: [b, c, d, e, f, g, h, i, j, k, l]
        System.out.println( tree.search( Long.MIN_VALUE, 12, 6, Long.MAX_VALUE ) );

        // find events contained in [6,12]
        // expect: [j]
        System.out.println( tree.search( 6, 12, 6, 12 ) );
    }

}
