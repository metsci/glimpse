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
