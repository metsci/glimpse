/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.gl.attribute;

import java.nio.FloatBuffer;

import com.metsci.glimpse.util.primitives.IntsModifiable;
import com.metsci.glimpse.util.quadtree.FilterInt;
import com.metsci.glimpse.util.quadtree.QuadTreeInts;


public class GLFloatBuffer2D extends GLFloatBuffer
{
    protected static final int MAX_BUCKET_SIZE = 500;

    protected QuadTreeInts xyIndex;
    protected boolean indexEnabled;

    public GLFloatBuffer2D( int length, boolean indexEnabled )
    {
        super( length, 2 );
        this.indexEnabled = indexEnabled;
    }

    public GLFloatBuffer2D( int length )
    {
        this( length, false );
    }

    protected void pruneIndex( int updateIndex )
    {
        lock.lock();
        try
        {
            if( ! indexEnabled )
            {
                xyIndex = null;
                return;
            }

            if ( xyIndex == null || updateIndex == 0 )
            {
                xyIndex = null;
            }
            else
            {
                for ( int i = updateIndex ; i < getNumVertices( ) ;i++ )
                {
                    xyIndex.remove( i );
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    protected void updateIndex( int updateIndex )
    {
        lock.lock();
        try
        {
            if( ! indexEnabled )
            {
                xyIndex = null;
                return;
            }

            if ( xyIndex == null )
            {
                // mutators should optionally return a list of changed
                // points to speed this up when only a few points change
                xyIndex = new QuadTreeInts( MAX_BUCKET_SIZE )
                {
                    @Override
                    public final float x( int i )
                    {
                        return data.asFloatBuffer( ).get( i*2 );
                    }

                    @Override
                    public final float y( int i )
                    {
                        return data.asFloatBuffer( ).get( i*2+1 );
                    }
                };

                  for ( int i = 0 ; i < getNumVertices( ) ;i++ )
                {
                    xyIndex.add( i );
                }
            }
            else
            {
                  for ( int i = updateIndex ; i < getNumVertices( ) ;i++ )
                {
                    xyIndex.add( i );
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    protected void createIndex( )
    {
        lock.lock();
        try
        {
            if( ! indexEnabled )
            {
                xyIndex = null;
                return;
            }

            // mutators should optionally return a list of changed
            // points to speed this up when only a few points change
            xyIndex = new QuadTreeInts( MAX_BUCKET_SIZE )
            {
                @Override
                public final float x( int i )
                {
                    return data.asFloatBuffer( ).get( i*2 );
                }

                @Override
                public final float y( int i )
                {
                    return data.asFloatBuffer( ).get( i*2+1 );
                }
            };

            for( int i = 0; i < getNumVertices( ); i++ )
            {
                xyIndex.add( i );
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean isIndexEnabled()
    {
        return indexEnabled;
    }

    public void setIndexEnabled( boolean enabled )
    {
        lock.lock();
        try
        {
            if( enabled == indexEnabled )
                return;

            indexEnabled = enabled;
            createIndex();
        }
        finally
        {
            lock.unlock();
        }
    }

    public int search( float xMin, float xMax, float yMin, float yMax, IntsModifiable result )
    {
        lock.lock();
        try
        {
            if( xyIndex == null )
                return 0;

            return xyIndex.search( xMin, xMax, yMin, yMax, result );
        }
        finally
        {
            lock.unlock();
        }
    }

    public int search( float xMin, float xMax, float yMin, float yMax, FilterInt filter, IntsModifiable result )
    {
        lock.lock();
        try
        {
            if( xyIndex == null )
                return 0;

            return xyIndex.search( xMin, xMax, yMin, yMax, filter, result );
        }
        finally
        {
            lock.unlock();
        }
    }

    public void mutateIndexed( IndexedMutator mutator )
    {
        lock.lock();
        try
        {
            pruneIndex( mutator.getUpdateIndex( ) );

            data.clear( );
            FloatBuffer floatData = data.asFloatBuffer( );

            mutator.mutate( floatData, elementSize );

            // the limit/position of floatData and data are independent
            // update data.limit() to reflect changes made to floatData
            data.position( 0 );
            data.limit( floatData.limit( ) * getBytesPerElement( ) );
            
            updateIndex( mutator.getUpdateIndex( ) );

            makeDirty();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void mutate( Mutator mutator )
    {
        lock.lock();
        try
        {
            data.clear( );
            FloatBuffer floatData = data.asFloatBuffer( );

            mutator.mutate( floatData, elementSize );

            // the limit/position of floatData and data are independent
            // update data.limit() to reflect changes made to floatData
            data.position( 0 );
            data.limit( floatData.limit( ) * getBytesPerElement( ) );

            createIndex();

            makeDirty();
        }
        finally
        {
            lock.unlock();
        }
    }

    public static interface IndexedMutator extends Mutator
    {
        /**
         * Indicates that the mutator will delete everything currently existing in the
         * buffer at this index and beyond.
         * @return the lowest changed index in the buffer
         */
        public int getUpdateIndex( );
        public void mutate( FloatBuffer data, int elementSize );
    }
}
