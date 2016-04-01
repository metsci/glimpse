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

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @author hogye
 */
public abstract class QuadTree<B>
{

    public static interface Accumulator<B>
    {
        /**
         * Every point in {@code bucket} will have {@code x} in {@code [xMinBucket,xMaxBucket)}
         * and {@code y} in {@code [yMinBucket,yMaxBucket)}.
         */
        void accumulate( B bucket, float xMinBucket, float xMaxBucket, float yMinBucket, float yMaxBucket );
    }

    public static interface Node<B>
    {
        LeafNode<B> leaf( float x, float y );

        void accumulate( float xMin, float xMax, float yMin, float yMax, Accumulator<B> accumulator );
    }

    protected static class InternalNode<B> implements Node<B>
    {
        public final float xDivider;
        public final float yDivider;

        /**
         * Indexed using {@link QuadTree#quadrant(float, float, float, float)}
         */
        @SuppressWarnings( "unchecked" )
        public final Node<B>[] children = new Node[4];

        public InternalNode( float xDivider, float yDivider )
        {
            this.xDivider = xDivider;
            this.yDivider = yDivider;
        }

        @Override
        public LeafNode<B> leaf( float x, float y )
        {
            int q = quadrant( xDivider, yDivider, x, y );
            return children[q].leaf( x, y );
        }

        @Override
        public void accumulate( float xMin, float xMax, float yMin, float yMax, Accumulator<B> accumulator )
        {
            boolean includeSmallX = ( xMin < xDivider );
            boolean includeLargeX = ( xMax >= xDivider );
            boolean includeSmallY = ( yMin < yDivider );
            boolean includeLargeY = ( yMax >= yDivider );

            if ( includeSmallX && includeSmallY ) children[0].accumulate( xMin, xMax, yMin, yMax, accumulator );
            if ( includeLargeX && includeSmallY ) children[1].accumulate( xMin, xMax, yMin, yMax, accumulator );
            if ( includeSmallX && includeLargeY ) children[2].accumulate( xMin, xMax, yMin, yMax, accumulator );
            if ( includeLargeX && includeLargeY ) children[3].accumulate( xMin, xMax, yMin, yMax, accumulator );
        }
    }

    protected static class LeafNode<B> implements Node<B>
    {
        public B bucket;

        // Used to check whether a leaf is too small to split,
        // and to choose dividers when splitting this leaf
        public final float xMin;
        public final float xMax;
        public final float yMin;
        public final float yMax;

        // Used to replace this leaf with a replacement node
        public final Node<B>[] referringArray;
        public final int referringIndex;

        protected LeafNode( B bucket, Node<B>[] referringArray, int referringIndex, float xMin, float xMax, float yMin, float yMax )
        {
            this.bucket = bucket;

            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;

            this.referringArray = referringArray;
            this.referringIndex = referringIndex;
        }

        @Override
        public LeafNode<B> leaf( float x, float y )
        {
            return this;
        }

        @Override
        public void accumulate( float xMin, float xMax, float yMin, float yMax, Accumulator<B> accumulator )
        {
            accumulator.accumulate( bucket, this.xMin, this.xMax, this.yMin, this.yMax );
        }
    }

    /**
     * 0 = small-x small-y
     * 1 = large-x small-y
     * 2 = small-x large-y
     * 3 = large-x large-y
     */
    public static int quadrant( float xDivider, float yDivider, float x, float y )
    {
        int h = ( x < xDivider ? 0 : 1 );
        int v = ( y < yDivider ? 0 : 2 );
        return ( h | v );
    }

    public static float truncInf( float x )
    {
        return max( min( x, Float.MAX_VALUE ), -Float.MAX_VALUE );
    }

    @SuppressWarnings( "unchecked" )
    protected final Node<B>[] root = new Node[1];

    public QuadTree( B rootBucket )
    {
        root[0] = new LeafNode<B>( rootBucket, root, 0, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY );
    }

    public LeafNode<B> leaf( float x, float y )
    {
        return root[0].leaf( x, y );
    }

    public void accumulate( float xMin, float xMax, float yMin, float yMax, Accumulator<B> accumulator )
    {
        root[0].accumulate( xMin, xMax, yMin, yMax, accumulator );
    }

    /**
     * For subclasses to call when a leaf's bucket gets full.
     *
     * This method may call {@link QuadTree#chooseDividers(Object)}, passing {@code leaf}'s
     * bucket as the argument. If a subclass's {@code chooseDividers()} method can't handle
     * an empty bucket, then the subclass should avoid calling this method with a leaf whose
     * bucket is empty.
     */
    protected void splitLeaf( LeafNode<B> leaf )
    {
        float xMin = leaf.xMin;
        float xMax = leaf.xMax;
        float yMin = leaf.yMin;
        float yMax = leaf.yMax;
        B bucket = leaf.bucket;

        // 2 * machine epsilon for 32-bit floats
        float minDividerSpacing = ( float ) Math.pow( 2.0, -23.0 );
        boolean xSplittable = ( xMax - xMin > minDividerSpacing );
        boolean ySplittable = ( yMax - yMin > minDividerSpacing );
        if ( !xSplittable && !ySplittable ) return;

        // Find new dividers

        float[] dividers = new float[2];
        chooseDividers( xMin, xMax, yMin, yMax, bucket, dividers );

        // If x is too small to split, use xMin as xDivider.
        // This way, (x < xDivider) will always be false for
        // any x coming into this node, so everything will
        // fall to the right of the divider, leaving the left
        // side empty.
        //
        // Same thing for the y dimension.
        //
        float xDivider = ( xSplittable ? dividers[0] : xMin );
        float yDivider = ( ySplittable ? dividers[1] : yMin );

        // Replace leaf with new subtree

        B[] newBuckets = splitBucket( bucket, xDivider, yDivider );

        B newSolitaryBucket = findSolitaryBucket( newBuckets );
        if ( newSolitaryBucket != null )
        {
            LeafNode<B> newLeaf = new LeafNode<B>( newSolitaryBucket, leaf.referringArray, leaf.referringIndex, xMin, xMax, yMin, yMax );
            replaceLeaf( leaf, newLeaf );
        }
        else
        {
            InternalNode<B> newInternal = new InternalNode<B>( xDivider, yDivider );
            Node<B>[] newLeaves = newInternal.children;
            newLeaves[0] = new LeafNode<B>( newBuckets[0], newLeaves, 0, xMin, xDivider, yMin, yDivider ); // [0] small-x small-y
            newLeaves[1] = new LeafNode<B>( newBuckets[1], newLeaves, 1, xDivider, xMax, yMin, yDivider ); // [1] large-x small-y
            newLeaves[2] = new LeafNode<B>( newBuckets[2], newLeaves, 2, xMin, xDivider, yDivider, yMax ); // [2] small-x large-y
            newLeaves[3] = new LeafNode<B>( newBuckets[3], newLeaves, 3, xDivider, xMax, yDivider, yMax ); // [3] large-x large-y

            replaceLeaf( leaf, newInternal );
        }
    }

    /**
     * A bucket is "solitary" if its siblings are all empty. Return the
     * solitary bucket, or null if there isn't one.
     */
    protected B findSolitaryBucket( B[] buckets )
    {
        // If we have no buckets, there is no solitary bucket
        int nb = buckets.length;
        if ( nb < 1 ) return null;

        int[] sizes = new int[nb];
        for ( int b = 0; b < nb; b++ )
            sizes[b] = bucketSize( buckets[b] );

        // If we have 2 or more non-empty buckets, there is no solitary bucket
        int numNonEmpty = 0;
        for ( int b = 0; b < nb; b++ )
            if ( sizes[b] > 0 ) numNonEmpty++;
        if ( numNonEmpty > 1 ) return null;

        // Return the only non-empty bucket, if there is one
        for ( int b = 0; b < nb; b++ )
            if ( sizes[b] > 0 ) return buckets[b];

        // If all buckets are empty, return one of them
        return buckets[0];
    }

    protected void replaceLeaf( LeafNode<B> leaf, Node<B> replacement )
    {
        leaf.referringArray[leaf.referringIndex] = replacement;
    }

    protected abstract void chooseDividers( float xMin, float xMax, float yMin, float yMax, B bucket, float[] result );

    protected abstract B[] splitBucket( B bucket, float xDivider, float yDivider );

    protected abstract int bucketSize( B bucket );

}
