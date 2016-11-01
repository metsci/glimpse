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
package com.metsci.glimpse.painter.shape;

import static com.metsci.glimpse.painter.shape.DynamicLineSetPainter.*;
import static javax.media.opengl.GL.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.google.common.collect.Sets;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.selection.QuadTreeFloatBuffer;
import com.metsci.glimpse.support.shader.point.PointArrayColorProgram;
import com.metsci.glimpse.util.primitives.FloatsArray;
import com.metsci.glimpse.util.primitives.IntsArray;

/**
 * Efficiently paints dynamically changing groups of colored points. Support is provided
 * for very efficiently changing the color of existing points, as well as for adding
 * to existing sets of points.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.misc.DynamicPointPainterExample
 */
public class DynamicPointSetPainter extends GlimpsePainterBase
{
    protected static final double GROWTH_FACTOR = 1.3;

    protected static final float DEFAULT_FEATHER_SIZE = 2.0f;
    protected static final float DEFAULT_POINT_SIZE = 5.0f;
    protected static final int DEFAULT_INITIAL_SIZE = 2000;
    protected static final float[] DEFAULT_COLOR = GlimpseColor.getBlack( );

    protected float pointSize;
    protected float featherSize;

    protected QuadTreeFloatBuffer quadTree;

    protected FloatBuffer rgbaBuffer;
    protected FloatBuffer xyBuffer;
    protected FloatBuffer tempBuffer;

    protected GLStreamingBuffer rgbaStreamingBuffer;
    protected GLStreamingBuffer xyStreamingBuffer;
    protected boolean bufferDirty = false;

    // point id (which can be any object) -> index into pointBuffer
    // good place for Guava BiMap here...
    protected Map<Object, Integer> idMap;
    protected Map<Integer, Object> indexMap;

    protected IntsArray searchResults;

    protected int initialSize;

    protected PointArrayColorProgram prog;

    public DynamicPointSetPainter( )
    {
        this( DEFAULT_INITIAL_SIZE );
    }

    public DynamicPointSetPainter( int initialSize )
    {
        this.initialSize = initialSize;
        this.pointSize = DEFAULT_POINT_SIZE;
        this.featherSize = DEFAULT_FEATHER_SIZE;

        this.idMap = new LinkedHashMap<Object, Integer>( );
        this.indexMap = new LinkedHashMap<Integer, Object>( );

        this.xyBuffer = FloatBuffer.allocate( initialSize * 2 );
        this.rgbaBuffer = FloatBuffer.allocate( initialSize * 4 );
        this.quadTree = new QuadTreeFloatBuffer( this.xyBuffer );

        this.searchResults = new IntsArray( );

        this.prog = new PointArrayColorProgram( );
        this.rgbaStreamingBuffer = new GLStreamingBuffer( GL_DYNAMIC_DRAW, 5 );
        this.xyStreamingBuffer = new GLStreamingBuffer( GL_DYNAMIC_DRAW, 5 );
    }

    public Collection<Object> getGeoRange( double minX, double maxX, double minY, double maxY )
    {
        painterLock.lock( );
        try
        {
            this.searchResults.n = 0; // clear the search results
            this.quadTree.getIndex( ).search( ( float ) minX, ( float ) maxX, ( float ) minY, ( float ) maxY, searchResults );

            final List<Object> resultList = new LinkedList<Object>( );
            for ( int i = 0; i < this.searchResults.n; i++ )
            {
                int index = this.searchResults.a[i];
                Object id = this.indexMap.get( index );
                if ( id != null )
                {
                    resultList.add( id );
                }
            }

            return resultList;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setFeatherSize( float size )
    {
        painterLock.lock( );
        try
        {
            this.featherSize = size;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setPointSize( float size )
    {
        painterLock.lock( );
        try
        {
            this.pointSize = size;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void putPoints( BulkPointAccumulator accumulator )
    {
        painterLock.lock( );
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

            bufferDirty = true;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void putColors( BulkColorAccumulator accumulator )
    {
        painterLock.lock( );
        try
        {
            mutateColors( accumulator );

            bufferDirty = true;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void putPoint( Object id, float posX, float posY )
    {
        putPoint( id, posX, posY, DEFAULT_COLOR );
    }

    public void putPoint( Object id, float posX, float posY, float[] color )
    {
        painterLock.lock( );
        try
        {
            int currentSize = getSize( );
            if ( getCapacity( ) < currentSize + 1 )
            {
                growBuffers( currentSize + 1 );
            }

            int index = getIndex( id, true );
            mutatePosition( index, posX, posY );
            mutateColor( index, color );

            bufferDirty = true;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void putColor( Object id, float[] color )
    {
        painterLock.lock( );
        try
        {
            int index = getIndex( id, false );
            mutateColor( index, color );

            bufferDirty = true;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void removeAll( )
    {
        painterLock.lock( );
        try
        {
            this.idMap.clear( );
            this.indexMap.clear( );
            this.xyBuffer = FloatBuffer.allocate( initialSize * 2 );
            this.rgbaBuffer = FloatBuffer.allocate( initialSize * 4 );
            this.quadTree.setBuffer( this.xyBuffer );

            this.bufferDirty = true;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void removePoint( Object id )
    {
        painterLock.lock( );
        try
        {
            int index = getIndex( id, false );
            if ( index == -1 ) return; // nothing to remove, the point does not exist
            deletePosition( index );

            bufferDirty = true;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    protected int getSize( )
    {
        return this.idMap.size( );
    }

    protected int getCapacity( )
    {
        // divide by 2 in order to count points, not vertices
        // ( 2 vertices per point )
        return this.xyBuffer.limit( ) / 2;
    }

    protected void deletePositions( final Set<Integer> indices )
    {
        if ( indices.isEmpty( ) ) return;

        final int size = this.getSize( );
        final int first = indices.iterator( ).next( );

        shiftMaps( idMap, indexMap, indices, size );

        shift( this.rgbaBuffer, tempBuffer, 4, size, indices );

        this.quadTree.removeIndex( first );
        shift( this.xyBuffer, tempBuffer, 2, size, indices );
        this.quadTree.addIndex( first );
    }

    protected void deletePositions( BulkPointAccumulator accum )
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
        this.rgbaBuffer.position( index * 4 );
        this.rgbaBuffer.put( color[0] );
        this.rgbaBuffer.put( color[1] );
        this.rgbaBuffer.put( color[2] );
        this.rgbaBuffer.put( color.length == 4 ? color[3] : 1.0f );
    }

    protected void mutatePosition( final int index, final float posX, final float posY )
    {
        this.quadTree.removeIndex( index, index + 1 );
        this.xyBuffer.position( index * 2 );
        this.xyBuffer.put( posX );
        this.xyBuffer.put( posY );
        this.quadTree.addIndex( index, index + 1 );
    }

    protected int getIndexArray( List<Object> ids, boolean grow, int[] listIndex )
    {
        int size = ids.size( );
        int minIndex = size;

        for ( int i = 0; i < size; i++ )
        {
            int index = getIndex( ids.get( i ), grow );
            listIndex[i] = index;
            if ( minIndex > index ) minIndex = index;
        }

        return minIndex;
    }

    protected void mutatePositions( BulkPointAccumulator accumulator )
    {
        final int currentSize = getSize( );
        final List<Object> ids = accumulator.getAddedIds( );
        final float[] v = accumulator.getAddedVertices( );
        final int stride = accumulator.getStride( );
        final int size = accumulator.getAddedSize( );

        final int[] indexList = new int[size];
        getIndexArray( ids, true, indexList );

        for ( int i = 0; i < size; i++ )
        {
            int index = indexList[i];

            // If index < currentSize, then the point already existed (was not assiged a new index).
            // In this case its position is being changed.
            // Remove it from the quadTree before mutating the position.
            // It will be re-added after the for loop.
            // If it is a new point, do nothing here.
            if ( index < currentSize )
            {
                this.quadTree.removeIndex( index, index + 1 );
            }

            this.xyBuffer.position( index * 2 );
            this.xyBuffer.put( v, i * stride, 2 );
        }
        this.quadTree.addIndices( indexList );

        for ( int i = 0; i < size; i++ )
        {
            this.rgbaBuffer.position( indexList[i] * 4 );
            this.rgbaBuffer.put( v, i * stride + 2, 4 );
        }
    }

    protected void mutateColors( BulkColorAccumulator accumulator )
    {
        final List<Object> ids = accumulator.getIds( );
        final float[] v = accumulator.getVertices( );
        final int stride = accumulator.getStride( );
        final int size = accumulator.getSize( );

        final int[] indexList = new int[size];
        getIndexArray( ids, false, indexList );

        for ( int i = 0; i < size; i++ )
        {
            this.rgbaBuffer.position( indexList[i] * 4 );
            this.rgbaBuffer.put( v, i * stride, 4 );
        }
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

        this.xyBuffer = DynamicLineSetPainter.growBuffer( this.xyBuffer, minSize * 2 );
        this.rgbaBuffer = DynamicLineSetPainter.growBuffer( this.rgbaBuffer, minSize * 4 );

        this.quadTree.setBuffer( this.xyBuffer );
    }

    public static class BulkColorAccumulator
    {
        List<Object> ids;
        FloatsArray v;

        public BulkColorAccumulator( )
        {
            ids = new ArrayList<Object>( );
            v = new FloatsArray( );
        }

        public void add( Object id, float[] color )
        {
            if ( color.length != 3 && color.length != 4 )
            {
                throw new IllegalArgumentException( "Color array must be size 3 or 4" );
            }

            // grow the FloatsArray if necessary (4 for color)
            if ( v.n == v.a.length )
            {
                v.ensureCapacity( ( int ) Math.max( v.n + getStride( ), v.n * GROWTH_FACTOR ) );
            }

            ids.add( id );
            v.append( color );
            if ( color.length == 3 ) v.append( 1.0f );
        }

        int getStride( )
        {
            return 4;
        }

        List<Object> getIds( )
        {
            return ids;
        }

        float[] getVertices( )
        {
            return v.a;
        }

        int getSize( )
        {
            return ids.size( );
        }
    }

    public static class BulkPointAccumulator
    {
        List<Object> removedIds;
        List<Object> addedIds;
        FloatsArray addedVertices;

        public BulkPointAccumulator( )
        {
            removedIds = new ArrayList<Object>( );
            addedIds = new ArrayList<Object>( );
            addedVertices = new FloatsArray( );
        }

        public void add( Object id, float x, float y, float[] color )
        {
            if ( color.length != 3 && color.length != 4 )
            {
                throw new IllegalArgumentException( "Color array must be size 3 or 4" );
            }

            // grow the FloatsArray if necessary (2 for x/y and 4 for color)
            if ( addedVertices.n == addedVertices.a.length )
            {
                addedVertices.ensureCapacity( ( int ) Math.max( addedVertices.n + getStride( ), addedVertices.n * GROWTH_FACTOR ) );
            }

            addedIds.add( id );
            addedVertices.append( x );
            addedVertices.append( y );
            addedVertices.append( color );
            if ( color.length == 3 ) addedVertices.append( 1.0f );
        }

        public void add( Object id, float x, float y )
        {
            add( id, x, y, DEFAULT_COLOR );
        }

        public void remove( Object id )
        {
            removedIds.add( id );
        }

        int getStride( )
        {
            return 6;
        }

        List<Object> getRemovedIds( )
        {
            return removedIds;
        }

        List<Object> getAddedIds( )
        {
            return addedIds;
        }

        float[] getAddedVertices( )
        {
            return addedVertices.a;
        }

        int getAddedSize( )
        {
            return addedIds.size( );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        GL3 gl = getGL3( context );

        this.rgbaStreamingBuffer.dispose( gl );
        this.xyStreamingBuffer.dispose( gl );
        this.prog.dispose( gl );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GL3 gl = getGL3( context );
        Axis2D axis = requireAxis2D( context );

        int size = getSize( );

        if ( size == 0 ) return;

        if ( bufferDirty )
        {
            rgbaBuffer.position( 0 );
            rgbaBuffer.limit( size * 4 );
            rgbaStreamingBuffer.setFloats( gl, rgbaBuffer );
            rgbaBuffer.clear( ); // doesn't actually erase data, just resets position/limit/mark

            xyBuffer.position( 0 );
            xyBuffer.limit( size * 2 );
            xyStreamingBuffer.setFloats( gl, xyBuffer );
            xyBuffer.clear( ); // doesn't actually erase data, just resets position/limit/mark
        }

        GLUtils.enableStandardBlending( gl );
        prog.begin( gl );
        try
        {
            prog.setAxisOrtho( gl, axis );
            prog.setPointSize( gl, pointSize );
            prog.setFeatherThickness( gl, featherSize );

            prog.draw( gl, GL.GL_POINTS, xyStreamingBuffer, rgbaStreamingBuffer, 0, size );
        }
        finally
        {
            prog.end( gl );
            GLUtils.disableBlending( gl );
        }
    }
}
