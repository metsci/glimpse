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
package com.metsci.glimpse.support.selection;

import java.nio.FloatBuffer;

import com.metsci.glimpse.util.quadtree.QuadTreeInts;

/**
 * A QuadTree backed by a FloatBuffer containing x/y coordinates of points stored in tree.
 * Points are references by the QuadTree using their index into the FloatBuffer.
 *
 * Points are assumed to be packed into the FloatBuffer like: [ x0, y0, x1, y1, ... ]
 *
 * @author ulman
 */
public class QuadTreeFloatBuffer
{
    protected static final int MAX_BUCKET_SIZE = 500;

    protected QuadTreeInts xyIndex;
    protected FloatBuffer buffer;

    public QuadTreeFloatBuffer( FloatBuffer buffer )
    {
        this.buffer = buffer;
        this.initQuadTree( );
    }

    protected void initQuadTree( )
    {
        this.xyIndex = new QuadTreeInts( MAX_BUCKET_SIZE )
        {
            @Override
            public final float x( int i )
            {
                return buffer.get( i * 2 );
            }

            @Override
            public final float y( int i )
            {
                return buffer.get( i * 2 + 1 );
            }
        };

        for ( int i = 0; i < buffer.limit( ) / 2; i++ )
        {
            xyIndex.add( i );
        }
    }

    public void addIndices( int[] indices )
    {
        for ( int index : indices )
        {
            xyIndex.add( index );
        }
    }

    public void addIndex( int startIndex )
    {
        this.addIndex( startIndex, this.buffer.limit( ) / 2 );
    }

    /**
     * Add points at indices [startIndex,endIndex) in the backing FloatBuffer
     * to the QuadTreeInts.
     */
    public void addIndex( int startIndex, int endIndex )
    {
        for ( int i = startIndex; i < endIndex; i++ )
        {
            xyIndex.add( i );
        }
    }

    public void removeIndices( int[] indices )
    {
        for ( int index : indices )
        {
            xyIndex.remove( index );
        }
    }

    public void removeIndex( int startIndex )
    {
        this.removeIndex( startIndex, this.buffer.limit( ) / 2 );
    }

    /**
     * Remove points at indices [startIndex,endIndex) in the backing FloatBuffer
     * to the QuadTreeInts.
     */
    public void removeIndex( int startIndex, int endIndex )
    {
        for ( int i = startIndex; i < endIndex; i++ )
        {
            xyIndex.remove( i );
        }
    }

    /**
     * Sets a new FloatBuffer as the backing store and recreates the QuadTreeInts.
     * @param buffer
     */
    public void setBuffer( FloatBuffer buffer )
    {
        this.buffer = buffer;
        this.initQuadTree( );
    }

    public QuadTreeInts getIndex( )
    {
        return this.xyIndex;
    }
}
