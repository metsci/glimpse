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
package com.metsci.glimpse.painter.shape;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;

import com.google.common.collect.Sets;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer.Mutator;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer2D;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer2D.IndexedMutator;
import com.metsci.glimpse.gl.attribute.GLVertexAttribute;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.painter.shape.DynamicPointSetPainter.BulkColorAccumulator;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.primitives.FloatsArray;
import com.metsci.glimpse.util.primitives.IntsArray;

/**
 * Efficiently paints dynamically changing groups of colored lines. Support is provided
 * for very efficiently changing the color of existing lines, as well as for adding 
 * to existing sets of lines.
 * 
 * @author ulman
 * @see com.metsci.glimpse.examples.misc.DynamicLinePainterExample
 */
public class DynamicLineSetPainter extends GlimpseDataPainter2D
{
    protected static final double GROWTH_FACTOR = 1.3;

    protected static final float DEFAULT_LINE_WIDTH = 2.0f;
    protected static final int DEFAULT_INITIAL_SIZE = 2000;
    protected static final float[] DEFAULT_COLOR = GlimpseColor.getBlack( );

    protected boolean lineStipple = false;
    protected int stippleFactor = 1;
    protected short stipplePattern = ( short ) 0x00FF;

    protected float lineWidth;

    protected GLFloatBuffer colorBuffer;
    protected GLFloatBuffer2D pointBuffer;

    protected FloatBuffer tempBuffer;

    // point id (which can be any object) -> index into pointBuffer
    // good place for Guava BiMap here...
    protected Map<Object, Integer> idMap;
    protected Map<Integer, Object> indexMap;

    protected ReentrantLock lock;

    protected IntsArray searchResults;

    protected int initialSize;

    public DynamicLineSetPainter( )
    {
        this( DEFAULT_INITIAL_SIZE );
    }

    public DynamicLineSetPainter( int initialSize )
    {
        this.initialSize = initialSize;
        this.lineWidth = DEFAULT_LINE_WIDTH;

        this.lock = new ReentrantLock( );

        this.idMap = new LinkedHashMap<Object, Integer>( );
        this.indexMap = new LinkedHashMap<Integer, Object>( );

        this.pointBuffer = new GLFloatBuffer2D( initialSize * 2, false );
        this.colorBuffer = new GLFloatBuffer( initialSize * 2, 4 );

        this.searchResults = new IntsArray( );
    }

    public void setDotted( boolean dotted )
    {
        lock.lock( );
        try
        {
            this.lineStipple = dotted;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setDotted( int stippleFactor, short stipplePattern )
    {
        lock.lock( );
        try
        {
            this.lineStipple = true;
            this.stippleFactor = stippleFactor;
            this.stipplePattern = stipplePattern;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setLineWidth( float size )
    {
        lock.lock( );
        try
        {
            this.lineWidth = size;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putLines( BulkLineAccumulator accumulator )
    {
        lock.lock( );
        try
        {
            int newPoints = accumulator.getAddedSize( );
            int currentSize = getSize( );
            if ( getCapacity( ) < currentSize + newPoints )
            {
                growBuffers( currentSize + newPoints );
            }

            deletePositions( accumulator );

            mutatePositions( accumulator );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putColors( BulkColorAccumulator accumulator )
    {
        lock.lock( );
        try
        {
            mutateColors( accumulator );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putLine( Object id, float posX1, float posY1, float posX2, float posY2 )
    {
        putLine( id, posX1, posY1, posX2, posY2, DEFAULT_COLOR );
    }

    public void putLine( Object id, float posX1, float posY1, float posX2, float posY2, float[] color )
    {
        lock.lock( );
        try
        {
            int currentSize = getSize( );
            if ( getCapacity( ) < currentSize + 1 )
            {
                growBuffers( currentSize + 1 );
            }

            int index = getIndex( id, true );
            mutatePosition( index, posX1, posY1, posX2, posY2 );
            mutateColor( index, color );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putColor( Object id, float[] color )
    {
        lock.lock( );
        try
        {
            int index = getIndex( id, false );
            mutateColor( index, color );
        }
        finally
        {
            lock.unlock( );
        }
    }

    //NOTE: currently doesn't capture any of the space allocated in gl vertex arrays
    public void removeAll( )
    {
        lock.lock( );
        try
        {
            this.idMap.clear( );
            this.indexMap.clear( );
            this.pointBuffer = new GLFloatBuffer2D( initialSize, true );
            this.colorBuffer = new GLFloatBuffer( initialSize, 4 );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeLine( Object id )
    {
        lock.lock( );
        try
        {
            int index = getIndex( id, false );
            if ( index == -1 ) return; // nothing to remove, the point does not exist
            deletePosition( index );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {
        lock.lock( );
        try
        {
            colorBuffer.bind( GLVertexAttribute.ATTRIB_COLOR_4D, gl );
            pointBuffer.bind( GLVertexAttribute.ATTRIB_POSITION_2D, gl );
            try
            {
                if ( lineStipple )
                {
                    gl.glEnable( GL2.GL_LINE_STIPPLE );
                    gl.glLineStipple( stippleFactor, stipplePattern );
                }

                gl.glLineWidth( lineWidth );
                gl.glDrawArrays( GL2.GL_LINES, 0, idMap.size( ) * 2 );
            }
            finally
            {
                colorBuffer.unbind( gl );
                pointBuffer.unbind( gl );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected int getSize( )
    {
        return this.idMap.size( );
    }

    protected int getCapacity( )
    {
        // divide by 2 in order to count lines, not vertices
        return this.pointBuffer.getMaxVertices( ) / 2;
    }
    
    protected static void shiftMaps( Map<Object, Integer> idMap, Map<Integer, Object> indexMap, Set<Integer> indices, int size )
    {
        for ( Integer index : indices )
        {
            Object id = indexMap.remove( index );
            idMap.remove( id );
        }

        //XXX this is inefficient for low index values
        // shift everything down in the index map
        int lastDelete = -1;
        int nextDelete = -1;
        int deleteCount = 0;
        for ( Integer index : indices )
        {
            lastDelete = nextDelete;
            nextDelete = index;
            deleteCount += 1;

            if ( lastDelete == -1 ) continue;

            shiftMaps( idMap, indexMap, lastDelete, nextDelete, deleteCount - 1 );
        }

        shiftMaps( idMap, indexMap, nextDelete, size, deleteCount );
    }

    protected static void shiftMaps( Map<Object, Integer> idMap, Map<Integer, Object> indexMap, int lastDelete, int nextDelete, int deleteCount )
    {
        for ( int i = lastDelete + 1; i < nextDelete; i++ )
        {
            Object id = indexMap.remove( i );
            indexMap.put( i - deleteCount, id );
            idMap.put( id, i - deleteCount );
        }
    }
    
    protected static void shift( FloatBuffer data, FloatBuffer tempBuffer, int length, int size, Set<Integer> indices )
    {
        int lastDelete = -1;
        int nextDelete = -1;
        int deleteCount = 0;
        for ( Integer index : indices )
        {
            lastDelete = nextDelete;
            nextDelete = index;

            if ( lastDelete != -1 )
            {
                shift( data, tempBuffer, nextDelete-lastDelete-1, lastDelete-deleteCount+1, lastDelete+1, length );
            }
            
            deleteCount += 1;
        }
        
        nextDelete += 1;

        shift( data, tempBuffer, size - nextDelete, nextDelete - deleteCount, nextDelete, length );
    }
    
    /**
     * @param data buffer to shift
     * @param shiftCount number of logical indices to shift (each index represents 'length' buffer entries)
     * @param toIndex the logical index to start copying data to
     * @param fromIndex the logical index to start copying data from
     * @param length the number of buffer entries per logical index
     */
    protected static void shift( FloatBuffer data, FloatBuffer tempBuffer, int shiftCount, int toIndex, int fromIndex, int length )
    {
        if ( shiftCount == 0 || toIndex == fromIndex ) return;
        
        // lazy load tempBuffer (only needed if removePoint is called)
        if ( tempBuffer == null || tempBuffer.capacity( ) < shiftCount * length )
        {
            tempBuffer = FloatBuffer.allocate( shiftCount * length );
        }

        // copy the data to shift into tempBuffer
        tempBuffer.limit( shiftCount * length );
        tempBuffer.position( 0 );
        data.limit( ( fromIndex + shiftCount ) * length );
        data.position( fromIndex * length );
        tempBuffer.put( data );

        // copy the data back, shifted left by one, to data buffer
        tempBuffer.rewind( );
        data.limit( ( toIndex + shiftCount ) * length );
        data.position( toIndex * length );
        data.put( tempBuffer );
    }

    protected void deletePositions( final Set<Integer> indices )
    {
        if ( indices.isEmpty( ) ) return;

        final int size = this.getSize( );
        final int first = indices.iterator( ).next( );

        shiftMaps( idMap, indexMap, indices, size );

        this.colorBuffer.mutate( new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                shift( data, tempBuffer, length * 2, size, indices );
            }
        } );

        this.pointBuffer.mutateIndexed( new IndexedMutator( )
        {
            @Override
            public int getUpdateIndex( )
            {
                return first * 2;
            }

            @Override
            public void mutate( FloatBuffer data, int length )
            {
                shift( data, tempBuffer, length * 2, size, indices );
            }
        } );
    }

    protected void deletePositions( BulkLineAccumulator accum )
    {
        Set<Integer> indices = Sets.newTreeSet( );

        for ( Object id : accum.getRemovedIds( ) )
        {
            Integer index = this.idMap.get( id );
            if ( index != null )
            {
                indices.add( index );
            }
        }

        deletePositions( indices );
    }

    protected void deletePosition( int index )
    {
        deletePositions( Collections.singleton( index ) );
    }

    protected void mutateColor( final int index, final float[] color )
    {
        this.colorBuffer.mutate( new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                data.position( index * 2 * length );

                for ( int i = 0; i < 2; i++ )
                {
                    data.put( color[0] );
                    data.put( color[1] );
                    data.put( color[2] );
                    data.put( color.length == 4 ? color[3] : 1.0f );
                }
            }
        } );
    }

    protected void mutatePosition( final int index, final float posX1, final float posY1, final float posX2, final float posY2 )
    {
        this.pointBuffer.mutateIndexed( new IndexedMutator( )
        {
            @Override
            public int getUpdateIndex( )
            {
                return index * 2;
            }

            @Override
            public void mutate( FloatBuffer data, int length )
            {
                data.position( index * 2 * length );
                data.put( posX1 );
                data.put( posY1 );
                data.put( posX2 );
                data.put( posY2 );
            }
        } );
    }

    protected int getIndexArray( List<Object> ids, int[] listIndex )
    {
        int size = ids.size( );
        int minIndex = size;

        for ( int i = 0; i < size; i++ )
        {
            int index = getIndex( ids.get( i ), true );
            listIndex[i] = index;
            if ( minIndex > index ) minIndex = index;
        }

        return minIndex;
    }

    protected void mutatePositions( BulkLineAccumulator accumulator )
    {
        final List<Object> ids = accumulator.getAddedIds( );
        final float[] v = accumulator.getVertices( );
        final int stride = accumulator.getStride( );
        final int size = accumulator.getAddedSize( );

        final int[] indexList = new int[size];
        final int minIndex = getIndexArray( ids, indexList );

        this.pointBuffer.mutateIndexed( new IndexedMutator( )
        {
            @Override
            public int getUpdateIndex( )
            {
                return minIndex * 2;
            }

            @Override
            public void mutate( FloatBuffer data, int length )
            {
                for ( int i = 0; i < size; i++ )
                {
                    data.position( indexList[i] * 2 * length );
                    data.put( v, i * stride, 2 * length );
                }
            }
        } );

        this.colorBuffer.mutate( new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                for ( int i = 0; i < size; i++ )
                {
                    data.position( indexList[i] * 2 * length );

                    for ( int j = 0; j < 2; j++ )
                    {
                        data.put( v, i * stride + 4, length );
                    }
                }
            }
        } );
    }

    protected void mutateColors( BulkColorAccumulator accumulator )
    {
        final List<Object> ids = accumulator.getIds( );
        final float[] v = accumulator.getVertices( );
        final int stride = accumulator.getStride( );
        final int size = accumulator.getSize( );

        final int[] indexList = new int[size];
        getIndexArray( ids, indexList );

        this.colorBuffer.mutate( new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                for ( int i = 0; i < size; i++ )
                {
                    data.position( indexList[i] * 2 * length );

                    for ( int j = 0; j < 2; j++ )
                    {
                        data.put( v, i * stride, length );
                    }
                }
            }
        } );
    }

    protected int getIndex( Object id, boolean grow )
    {
        Integer index = this.idMap.get( id );
        if ( index == null )
        {
            if ( grow )
            {
                index = idMap.size( );
                idMap.put( id, index );
                indexMap.put( index, id );
            }
            else
            {
                return -1;
            }
        }

        return index;
    }

    protected void growBuffers( int minSize )
    {
        minSize = Math.max( ( int ) ( getCapacity( ) * GROWTH_FACTOR ), minSize );

        this.pointBuffer.ensureCapacity( minSize * 2 );
        this.colorBuffer.ensureCapacity( minSize * 2 );
    }

    public static class BulkLineAccumulator
    {
        List<Object> removedIds;
        List<Object> addedIds;
        FloatsArray addedVertices;

        public BulkLineAccumulator( )
        {
            removedIds = new ArrayList<Object>( );
            addedIds = new ArrayList<Object>( );
            addedVertices = new FloatsArray( );
        }

        public void add( Object id, float x1, float y1, float x2, float y2, float[] color )
        {
            if ( color.length != 3 && color.length != 4 )
            {
                throw new IllegalArgumentException( "Color array must be size 3 or 4" );
            }

            // grow the FloatsArray if necessary (4 for x/y and 4 for color)
            if ( addedVertices.n == addedVertices.a.length )
            {
                addedVertices.ensureCapacity( ( int ) Math.max( addedVertices.n + getStride( ), addedVertices.n * GROWTH_FACTOR ) );
            }

            addedIds.add( id );

            addedVertices.append( x1 );
            addedVertices.append( y1 );
            addedVertices.append( x2 );
            addedVertices.append( y2 );
            addedVertices.append( color );

            if ( color.length == 3 ) addedVertices.append( 1.0f );
        }

        public void add( Object id, float x1, float y1, float x2, float y2 )
        {
            add( id, x1, y1, x2, y2, DEFAULT_COLOR );
        }
        
        public void remove( Object id )
        {
            removedIds.add( id );
        }

        int getStride( )
        {
            return 8;
        }

        List<Object> getRemovedIds( )
        {
            return removedIds;
        }

        List<Object> getAddedIds( )
        {
            return addedIds;
        }

        float[] getVertices( )
        {
            return addedVertices.a;
        }

        int getAddedSize( )
        {
            return addedIds.size( );
        }
    }
}
