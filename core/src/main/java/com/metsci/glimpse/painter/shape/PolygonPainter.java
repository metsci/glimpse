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

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.support.interval.IntervalQuadTree;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.support.polygon.PolygonTessellator;
import com.metsci.glimpse.support.polygon.PolygonTessellator.TessellationException;
import com.metsci.glimpse.support.polygon.SimpleVertexAccumulator;

/**
 * Paints large collections of arbitrary polygons (including concave polygons).
 * Polygons can have timestamps associated with them, and can be efficiently filtered
 * by time (only drawing those polygons which fall within a particular time window.
 *
 * @author ulman
 */
public class PolygonPainter extends GlimpsePainter2D
{
    protected static final double DELETE_EXPAND_FACTOR = 1.2;

    //@formatter:off
    protected byte halftone[] = {
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55,
            (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55,
            (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
            (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55,
            (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55,
            (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
            (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55,
            (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55,
            (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
            (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55,
            (byte) 0x55, (byte) 0x55, (byte) 0x55 };
    //@formatter:on

    protected PolygonTessellator tessellator;

    protected int dataBufferSize = 0;
    protected FloatBuffer dataBuffer = null;

    // mapping from id to Group
    protected Map<Object, Group> groups;
    // true indicates that new data must be loaded onto the GPU
    protected volatile boolean newData = false;
    // groups with new data which must be loaded onto the GPU
    protected Set<Group> updatedGroups;
    // mapping from id to LoadedGroup (GPU-side group information)
    protected Map<Object, LoadedGroup> loadedGroups;

    protected ReentrantLock updateLock;

    protected Long globalSelectionStart;
    protected Long globalSelectionEnd;

    public PolygonPainter( )
    {
        this.tessellator = new PolygonTessellator( );

        this.groups = new LinkedHashMap<Object, Group>( );
        this.updatedGroups = new LinkedHashSet<Group>( );
        this.loadedGroups = new LinkedHashMap<Object, LoadedGroup>( );

        this.updateLock = new ReentrantLock( );
    }

    public void addPolygon( Object groupId, Object polygonId, float[] dataX, float[] dataY, float z )
    {
        this.updateLock.lock( );
        try
        {
            addPolygon( groupId, new IdPolygon( groupId, polygonId, buildPolygon( dataX, dataY ), z ) );
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void addPolygon( Object groupId, Object polygonId, Polygon geometry, float z )
    {
        this.updateLock.lock( );
        try
        {
            addPolygon( groupId, new IdPolygon( groupId, polygonId, geometry, z ) );

        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void addPolygon( Object groupId, Object polygonId, Shape shape, float z )
    {
        this.updateLock.lock( );
        try
        {
            addPolygon( groupId, new IdPolygon( groupId, polygonId, buildPolygon( shape ), z ) );

        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void addPolygon( Object groupId, Object polygonId, long startTime, long endTime, float[] dataX, float[] dataY, float z )
    {
        this.updateLock.lock( );
        try
        {
            addPolygon( groupId, new IdPolygon( groupId, polygonId, startTime, endTime, buildPolygon( dataX, dataY ), z ) );
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void addPolygon( Object groupId, Object polygonId, long startTime, long endTime, Polygon geometry, float z )
    {
        this.updateLock.lock( );
        try
        {
            addPolygon( groupId, new IdPolygon( groupId, polygonId, startTime, endTime, geometry, z ) );
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void addPolygon( Object groupId, Object polygonId, long startTime, long endTime, Shape shape, float z )
    {
        this.updateLock.lock( );
        try
        {
            addPolygon( groupId, new IdPolygon( groupId, polygonId, startTime, endTime, buildPolygon( shape ), z ) );
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void displayTimeRange( Object groupId, double startTime, double endTime )
    {
        displayTimeRange( groupId, ( long ) Math.ceil( startTime ), ( long ) Math.floor( endTime ) );
    }

    public void displayTimeRange( double startTime, double endTime )
    {
        displayTimeRange( ( long ) Math.ceil( startTime ), ( long ) Math.floor( endTime ) );
    }

    public void displayTimeRange( int groupId, long startTime, long endTime )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setTimeRange( startTime, endTime );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void displayTimeRange( long startTime, long endTime )
    {
        globalSelectionStart = startTime;
        globalSelectionEnd = endTime;

        this.updateLock.lock( );
        try
        {
            for ( Group group : groups.values( ) )
            {
                group.setTimeRange( globalSelectionStart, globalSelectionEnd );
            }

            this.updatedGroups.addAll( groups.values( ) );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setLineColor( Object groupId, float[] rgba )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setLineColor( rgba );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setLineColor( Object groupId, float r, float g, float b, float a )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setLineColor( r, g, b, a );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setLineWidth( Object groupId, float width )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setLineWidth( width );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setShowLines( Object groupId, boolean show )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setShowLines( show );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setPolyDotted( Object groupId, byte[] stipple )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setPolyStipple( true );
            group.setPolyStipple( stipple );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setPolyDotted( Object groupId, boolean dotted )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setPolyStipple( dotted );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setLineDotted( Object groupId, boolean dotted )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setLineStipple( dotted );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setLineDotted( Object groupId, int stippleFactor, short stipplePattern )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setLineStipple( true );
            group.setLineStipple( stippleFactor, stipplePattern );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setFill( Object groupId, boolean show )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setShowPoly( show );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setFillColor( Object groupId, float[] rgba )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setFillColor( rgba );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void setFillColor( Object groupId, float r, float g, float b, float a )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setFillColor( r, g, b, a );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    /**
     * Deletes all Polygon groups, removing their display settings and reclaiming memory.
     */
    public void deleteAll( )
    {
        this.updateLock.lock( );
        try
        {
            for ( Group group : groups.values( ) )
            {
                group.deleteGroup( );
            }

            this.updatedGroups.addAll( groups.values( ) );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    /**
     * Deletes an individual Polygon group, removing its display settings and reclaiming memory.
     * @param groupId the id of the group to delete
     */
    public void deleteGroup( Object groupId )
    {
        this.updateLock.lock( );
        try
        {
            if ( !groups.containsKey( groupId ) ) return;

            Group group = groups.get( groupId );

            group.deleteGroup( );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    /**
     * Clears an individual Polygon group, deleting all the polygons its contains but
     * retaining its display settings.
     * @param groupId the id of the group to clear
     */
    public void clearGroup( Object groupId )
    {
        this.updateLock.lock( );
        try
        {
            if ( !groups.containsKey( groupId ) ) return;

            Group group = groups.get( groupId );

            group.clearGroup( );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void deletePolygon( Object groupId, Object polygonId )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.deletePolygon( polygonId );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    protected void addPolygon( Object groupId, IdPolygon polygon )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.addPolygon( polygon );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    // must be called while holding trackUpdateLock
    protected Group getOrCreateGroup( Object groupId )
    {
        Group group = this.groups.get( groupId );

        if ( group == null )
        {
            group = new Group( groupId );

            if ( globalSelectionStart != null && globalSelectionEnd != null ) group.setTimeRange( globalSelectionStart, globalSelectionEnd );

            this.groups.put( groupId, group );
        }

        return group;
    }

    // must be called while holding trackUpdateLock
    protected void ensureDataBufferSize( int needed )
    {
        if ( dataBuffer == null || dataBufferSize < needed )
        {
            dataBufferSize = needed;
            dataBuffer = ByteBuffer.allocateDirect( needed * 3 * 4 ).order( ByteOrder.nativeOrder( ) ).asFloatBuffer( );
        }

        dataBuffer.rewind( );
    }

    protected LoadedGroup getOrCreateLoadedGroup( Object id, Group group )
    {
        LoadedGroup loaded = loadedGroups.get( id );
        if ( loaded == null )
        {
            loaded = new LoadedGroup( group );
            loadedGroups.put( id, loaded );
        }
        return loaded;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        GL2 gl = context.getGL( ).getGL2( );

        // something in a Group has changed so we must copy these
        // changes to the corresponding LoadedGroup (which is accessed
        // only from display0 and can be used on subsequent display0 calls
        // to render the polygon updates without synchronizing on updateLock
        // because the changes have been copied from the Group to its
        // corresponding LoadedGroup).
        if ( this.newData )
        {
            // groups are modified by the user and protected by updateLock
            this.updateLock.lock( );
            try
            {
                // loop through all Groups with updates
                for ( Group group : updatedGroups )
                {
                    Object id = group.groupId;

                    if ( group.groupDeleted || group.groupCleared )
                    {
                        // if the corresponding LoadedGroup does not exist, create it
                        LoadedGroup loaded = getOrCreateLoadedGroup( id, group );
                        loaded.dispose( gl );
                        loadedGroups.remove( id );

                        // If the group was deleted then recreated in between calls to display0(),
                        // (both isDataInserted() and isDeletePending() are true) then don't remove the group
                        if ( group.groupDeleted && !group.polygonsInserted )
                        {
                            groups.remove( id );
                            continue;
                        }
                    }

                    // if the corresponding LoadedGroup does not exist, create it
                    LoadedGroup loaded = getOrCreateLoadedGroup( id, group );

                    // copy settings from the Group to the LoadedGroup
                    loaded.loadSettings( group );

                    if ( group.polygonsInserted )
                    {
                        updateVertices( gl, loaded, group, true );
                        updateVertices( gl, loaded, group, false );
                    }

                    if ( group.polygonsSelected )
                    {
                        loaded.loadLineSelectionIntoBuffer( group.selectedPolygons, group.selectedLinePrimitiveCount, 0 );
                        loaded.loadFillSelectionIntoBuffer( group.selectedPolygons, group.selectedFillPrimitiveCount, 0 );
                    }

                    group.reset( );
                }

                this.updatedGroups.clear( );
                this.newData = false;
            }
            finally
            {
                this.updateLock.unlock( );
            }

            glHandleError( gl, "Update Error" );
        }

        if ( loadedGroups.isEmpty( ) ) return;

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( axis.getMinX( ), axis.getMaxX( ), axis.getMinY( ), axis.getMaxY( ), -1 << 23, 1 );

        gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL2.GL_BLEND );
        gl.glEnable( GL2.GL_LINE_SMOOTH );

        gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );

        for ( LoadedGroup loaded : loadedGroups.values( ) )
        {
            drawGroup( gl, loaded );
        }

        glHandleError( gl, "Draw Error" );

        gl.glDisable( GL2.GL_DEPTH_TEST );
        gl.glDisable( GL2.GL_BLEND );
        gl.glDisable( GL2.GL_LINE_SMOOTH );
    }

    protected void updateVertices( GL gl, LoadedGroup loaded, Group group, boolean fill )
    {
        boolean initialized = fill ? loaded.glFillBufferInitialized : loaded.glLineBufferInitialized;
        int maxSize = fill ? loaded.glFillBufferMaxSize : loaded.glLineBufferMaxSize;
        int currentSize = fill ? loaded.glFillBufferCurrentSize : loaded.glLineBufferCurrentSize;
        int insertSize = fill ? group.fillInsertVertexCount : group.lineInsertVertexCount;
        int totalSize = fill ? group.totalFillVertexCount : group.totalLineVertexCount;
        int handle = fill ? loaded.glFillBufferHandle : loaded.glLineBufferHandle;

        // the size needed is the current buffer location plus new inserts (we cannot use
        // group.totalLineVertexCount because that will be smaller than lineSizeNeeded if
        // polygons have been deleted
        int sizeNeeded = currentSize + insertSize;

        if ( !initialized || maxSize < sizeNeeded )
        {
            // if we've deleted vertices, but are still close to the max buffer size, then
            // go ahead and expand the max buffer size anyway
            // if we're far below the max because of deletions, don't expand the array
            if ( !initialized || maxSize < DELETE_EXPAND_FACTOR * totalSize )
            {
                // if the track doesn't have a gl buffer or it is too small we must
                // copy all the track's data into a new, larger buffer

                // if this is the first time we have allocated memory for this track
                // don't allocate any extra, it may never get added to
                // however, once a track has been updated once, we assume it is likely
                // to be updated again and give it extra memory
                if ( initialized )
                {
                    if ( handle > 0 ) gl.glDeleteBuffers( 1, new int[] { handle }, 0 );
                    maxSize = Math.max( ( int ) ( maxSize * 1.5 ), totalSize );
                }
                else
                {
                    maxSize = totalSize;
                }

                // create a new device buffer handle
                int[] bufferHandle = new int[1];
                gl.glGenBuffers( 1, bufferHandle, 0 );
                handle = bufferHandle[0];
            }

            // copy all the track data into a host buffer
            ensureDataBufferSize( maxSize );

            if ( fill )
            {
                loaded.loadFillVerticesIntoBuffer( group, dataBuffer, 0, group.polygonMap.values( ) );
                loaded.loadFillSelectionIntoBuffer( group.selectedPolygons, group.selectedFillPrimitiveCount, 0 );
            }
            else
            {
                loaded.loadLineVerticesIntoBuffer( group, dataBuffer, 0, group.polygonMap.values( ) );
                loaded.loadLineSelectionIntoBuffer( group.selectedPolygons, group.selectedLinePrimitiveCount, 0 );
            }

            // copy data from the host buffer into the device buffer
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, handle );
            glHandleError( gl, "glBindBuffer Error  (Case 1)" );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, maxSize * 3 * BYTES_PER_FLOAT, dataBuffer.rewind( ), GL2.GL_DYNAMIC_DRAW );
            glHandleError( gl, "glBufferData Error" );

            if ( fill )
            {
                loaded.glFillBufferInitialized = true;
                loaded.glFillBufferCurrentSize = totalSize;
                loaded.glFillBufferHandle = handle;
                loaded.glFillBufferMaxSize = maxSize;
            }
            else
            {
                loaded.glLineBufferInitialized = true;
                loaded.glLineBufferCurrentSize = totalSize;
                loaded.glLineBufferHandle = handle;
                loaded.glLineBufferMaxSize = maxSize;
            }
        }
        else
        {
            // there is enough empty space in the device buffer to accommodate all the new data

            // copy all the new track data into a host buffer
            ensureDataBufferSize( insertSize );

            if ( fill )
            {
                loaded.loadFillVerticesIntoBuffer( group, dataBuffer, currentSize, group.newPolygons );
                loaded.loadFillSelectionIntoBuffer( group.newSelectedPolygons, group.selectedFillPrimitiveCount );
            }
            else
            {
                loaded.loadLineVerticesIntoBuffer( group, dataBuffer, currentSize, group.newPolygons );
                loaded.loadLineSelectionIntoBuffer( group.newSelectedPolygons, group.selectedLinePrimitiveCount );
            }

            // update the device buffer with the new data
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, handle );
            glHandleError( gl, "glBindBuffer Error  (Case 2)" );
            gl.glBufferSubData( GL2.GL_ARRAY_BUFFER, currentSize * 3 * BYTES_PER_FLOAT, insertSize * 3 * BYTES_PER_FLOAT, dataBuffer.rewind( ) );
            glHandleError( gl, "glBufferSubData Error" );

            if ( fill )
            {
                loaded.glFillBufferCurrentSize = sizeNeeded;
            }
            else
            {
                loaded.glLineBufferCurrentSize = sizeNeeded;
            }
        }
    }

    protected void drawGroup( GL2 gl, LoadedGroup loaded )
    {
        if ( !isGroupReady( loaded ) ) return;

        if ( loaded.fillOn )
        {
            gl.glColor4fv( loaded.fillColor, 0 );

            if ( loaded.polyStippleOn )
            {
                gl.glEnable( GL2.GL_POLYGON_STIPPLE );
                gl.glPolygonStipple( loaded.polyStipplePattern, 0 );
            }

            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, loaded.glFillBufferHandle );
            gl.glVertexPointer( 3, GL2.GL_FLOAT, 0, 0 );

            loaded.glFillOffsetBuffer.rewind( );
            loaded.glFillCountBuffer.rewind( );

            // A count > 65535 causes problems on some ATI cards, so we must loop through the
            // groups of primitives and split them up where necessary.  An alternate way would be
            // to construct the count and offset arrays so that groups are less then 65535 in size.
            // There is some evidence on web forums that this may provide performance benefits as well
            // when dynamic data is being used.
            for ( int i = 0; i < loaded.glTotalFillPrimitives; i++ )
            {
                int fillCountTotal = loaded.glFillCountBuffer.get( i );
                int fillCountRemaining = fillCountTotal;
                while ( fillCountRemaining > 0 )
                {
                    int fillCount = Math.min( 60000, fillCountRemaining ); // divisible by 3
                    int offset = loaded.glFillOffsetBuffer.get( i ) + ( fillCountTotal - fillCountRemaining );
                    gl.glDrawArrays( GL2.GL_TRIANGLES, offset, fillCount );
                    fillCountRemaining -= fillCount;
                }
            }

            // XXX: Old way uses glMultiDrawArrays, but if one of the arrays is > 65535 in size, it will render incorrectly on some machines.
            // gl.glMultiDrawArrays( GL2.GL_TRIANGLES, loaded.glFillOffsetBuffer, loaded.glFillCountBuffer, loaded.glTotalFillPrimitives );

            if ( loaded.polyStippleOn )
            {
                gl.glDisable( GL2.GL_POLYGON_STIPPLE );
            }
        }

        if ( loaded.linesOn )
        {
            gl.glColor4fv( loaded.lineColor, 0 );
            gl.glLineWidth( loaded.lineWidth );

            if ( loaded.lineStippleOn )
            {
                gl.glEnable( GL2.GL_LINE_STIPPLE );
                gl.glLineStipple( loaded.lineStippleFactor, loaded.lineStipplePattern );
            }

            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, loaded.glLineBufferHandle );
            gl.glVertexPointer( 3, GL2.GL_FLOAT, 0, 0 );

            loaded.glLineOffsetBuffer.rewind( );
            loaded.glLineCountBuffer.rewind( );

            // A count > 65535 causes problems on some ATI cards, so we must loop through the
            // groups of primitives and split them up where necessary.  An alternate way would be
            // to construct the count and offset arrays so that groups are less then 65535 in size.
            // There is some evidence on web forums that this may provide performance benefits as well
            // when dynamic data is being used.
            for ( int i = 0; i < loaded.glTotalLinePrimitives; i++ )
            {
                int fillCountTotal = loaded.glLineCountBuffer.get( i );
                int fillCountRemaining = fillCountTotal;
                while ( fillCountRemaining > 0 )
                {
                    int fillCount = Math.min( 60000, fillCountRemaining ); // divisible by 2
                    int offset = loaded.glLineOffsetBuffer.get( i ) + ( fillCountTotal - fillCountRemaining );
                    gl.glDrawArrays( GL2.GL_LINE_LOOP, offset, fillCount );
                    fillCountRemaining -= fillCount;
                }
            }

            // XXX: Old way uses glMultiDrawArrays, but if one of the arrays is > 65535 in size, it will render incorrectly on some machines.
            // gl.glMultiDrawArrays( GL2.GL_LINE_LOOP, loaded.glLineOffsetBuffer, loaded.glLineCountBuffer, loaded.glTotalLinePrimitives );

            if ( loaded.lineStippleOn )
            {
                gl.glDisable( GL2.GL_LINE_STIPPLE );
            }
        }
    }

    protected boolean isGroupReady( LoadedGroup loaded )
    {
        return loaded.glFillBufferInitialized && loaded.glLineBufferInitialized && loaded.glLineOffsetBuffer != null && loaded.glLineCountBuffer != null && loaded.glFillOffsetBuffer != null && loaded.glFillCountBuffer != null;
    }

    protected static Polygon buildPolygon( float[] geometryX, float[] geometryY )
    {
        Polygon p = new Polygon( );

        int size = Math.min( geometryX.length, geometryY.length );

        double[] geometry = new double[size * 2];
        for ( int i = 0; i < size; i++ )
        {
            geometry[2 * i] = geometryX[i];
            geometry[2 * i + 1] = geometryY[i];
        }

        LoopBuilder b = new LoopBuilder( );
        b.addVertices( geometry, size );

        p.add( b.complete( Interior.onRight ) );

        return p;
    }

    protected static Polygon buildPolygon( Shape shape )
    {
        Polygon p = new Polygon( );
        PathIterator iter = shape.getPathIterator( null );
        double[] vertices = new double[6];
        LoopBuilder b = new LoopBuilder( );

        while ( !iter.isDone( ) )
        {
            int type = iter.currentSegment( vertices );

            if ( type == PathIterator.SEG_CLOSE )
            {
                p.add( b.complete( Interior.onLeft ) );
                b = new LoopBuilder( );
            }
            else if ( type == PathIterator.SEG_LINETO )
            {
                b.addVertices( vertices, 1 );
            }
            else if ( type == PathIterator.SEG_MOVETO )
            {
                p.add( b.complete( Interior.onLeft ) );
                b = new LoopBuilder( );
                b.addVertices( vertices, 1 );
            }
            else
            {
                throw new UnsupportedOperationException( "Shape Not Supported." );
            }

            iter.next( );
        }

        return p;
    }

    @Override
    public void dispose( GLContext context )
    {
        GL gl = context.getGL( );

        this.updateLock.lock( );
        try
        {
            for ( LoadedGroup group : loadedGroups.values( ) )
            {
                group.dispose( gl );
            }
        }
        finally
        {
            this.updateLock.unlock( );
        }

        tessellator.destroy( );
    }

    /**
     * An internal data structure containing geometry information about a single polygon.
     *
     * @author ulman
     */
    private class IdPolygon
    {
        Object groupId;
        Object polygonId;

        long startTime;
        long endTime;

        Polygon geometry;

        float[] fillVertices;
        float depth;

        int lineVertexCount;
        int fillVertexCount;

        int linePrimitiveCount;
        int fillPrimitiveCount;

        int[] lineOffsets;
        int[] lineSizes;

        int[] fillOffsets;
        int[] fillSizes;

        protected IdPolygon( Object groupId, Object polygonId, long startTime, long endTime, Polygon geometry, float depth )
        {
            this.groupId = groupId;
            this.polygonId = polygonId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.geometry = geometry;
            this.depth = depth;

            if ( this.geometry != null )
            {
                this.calculateLineCounts( );
                this.calculateFillCounts( );

                this.lineOffsets = new int[linePrimitiveCount];
                this.lineSizes = new int[linePrimitiveCount];
                this.fillOffsets = new int[fillPrimitiveCount];
                this.fillSizes = new int[fillPrimitiveCount];
            }
        }

        protected IdPolygon( Object groupId, Object polygonId, Polygon geometry, float z )
        {
            this( groupId, polygonId, Long.MIN_VALUE, Long.MAX_VALUE, geometry, z );
        }

        protected void calculateLineCounts( )
        {
            int vertexCount = 0;
            int primitiveCount = 0;

            Iterator<Loop> iter = geometry.getIterator( );
            while ( iter.hasNext( ) )
            {
                Loop loop = iter.next( );
                int size = loop.size( );
                vertexCount += size;
                primitiveCount += 1;
            }

            lineVertexCount = vertexCount;
            linePrimitiveCount = primitiveCount;
        }

        protected void calculateFillCounts( )
        {
            fillVertices = tessellate( );
            fillVertexCount = fillVertices.length / 2;
            fillPrimitiveCount = 1;
        }

        /**
         * Load the geometry for the outline of this polygon into
         * the provided FloatBuffer.
         *
         * @param zCoord the z-value to use for this polygon
         * @param vertexBuffer the buffer to load into
         * @param offsetVertex the offset of the Polygon vertices from the start of the vertexBuffer
         * @return the number of vertices added to the buffer
         */
        public int loadLineVerticesIntoBuffer( float zCoord, FloatBuffer vertexBuffer, int offsetVertex )
        {
            int totalSize = 0;
            int primitiveCount = 0;
            Iterator<Loop> iter = geometry.getIterator( );
            while ( iter.hasNext( ) )
            {
                Loop loop = iter.next( );
                int size = loop.size( );

                for ( int i = 0; i < size; i++ )
                {
                    double[] vertex = loop.get( i );
                    vertexBuffer.put( ( float ) vertex[0] ).put( ( float ) vertex[1] ).put( zCoord );
                }

                lineOffsets[primitiveCount] = offsetVertex + totalSize;
                lineSizes[primitiveCount] = size;

                primitiveCount++;
                totalSize += size;
            }

            return lineVertexCount;
        }

        public void loadLineIntoBuffer( IntBuffer offsetBuffer, IntBuffer sizeBuffer )
        {
            for ( int index = 0; index < linePrimitiveCount; index++ )
            {
                offsetBuffer.put( lineOffsets[index] );
                sizeBuffer.put( lineSizes[index] );
            }
        }

        public int loadFillVerticesIntoBuffer( float zCoord, FloatBuffer vertexBuffer, int offsetVertex )
        {
            for ( int i = 0; i < fillVertexCount * 2; i++ )
            {
                vertexBuffer.put( fillVertices[i] );

                if ( i % 2 != 0 ) vertexBuffer.put( zCoord );
            }

            fillOffsets[0] = offsetVertex;
            fillSizes[0] = fillVertexCount;

            return fillVertexCount;
        }

        public void loadFillIntoBuffer( IntBuffer offsetBuffer, IntBuffer sizeBuffer )
        {
            // there is always only one fill primitive for a polygon
            offsetBuffer.put( fillOffsets[0] );
            sizeBuffer.put( fillSizes[0] );
        }

        protected float[] tessellate( )
        {
            try
            {
                SimpleVertexAccumulator accumulator = new SimpleVertexAccumulator( );
                tessellator.tessellate( geometry, accumulator );
                return accumulator.getVertices( );
            }
            catch ( TessellationException e )
            {
                logWarning( logger, "Problem tessellating polygon.", e );
                return new float[0];
            }
        }

        private PolygonPainter getOuterType( )
        {
            return PolygonPainter.this;
        }

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType( ).hashCode( );
            result = prime * result + ( ( groupId == null ) ? 0 : groupId.hashCode( ) );
            result = prime * result + ( ( polygonId == null ) ? 0 : polygonId.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            IdPolygon other = ( IdPolygon ) obj;
            if ( !getOuterType( ).equals( other.getOuterType( ) ) ) return false;
            if ( groupId == null )
            {
                if ( other.groupId != null ) return false;
            }
            else if ( !groupId.equals( other.groupId ) ) return false;
            if ( polygonId == null )
            {
                if ( other.polygonId != null ) return false;
            }
            else if ( !polygonId.equals( other.polygonId ) ) return false;
            return true;
        }

        @Override
        public String toString( )
        {
            return "[ " + groupId + ", " + polygonId + ", " + startTime + ", " + endTime + " ]";
        }
    }

    /**
     * An internal data structure containing display data copied from
     * a Group. Because LoadedGroups are modified and accessed only inside
     * the opengl display loop, no synchronization is necessary, allowing
     * fast access to display data.
     *
     * @author ulman
     */
    private static class LoadedGroup
    {
        //// group display attributes ////
        float[] lineColor = new float[4];
        float lineWidth;
        boolean linesOn;

        int lineStippleFactor;
        short lineStipplePattern;
        boolean lineStippleOn;

        byte[] polyStipplePattern = new byte[128];
        boolean polyStippleOn;

        float[] fillColor = new float[4];
        boolean fillOn;
        //// group display attributes ////

        // true when glFillBufferHandle is initialized
        boolean glFillBufferInitialized = false;
        // true when glLineBufferHandle is initialized
        boolean glLineBufferInitialized = false;

        // a reference to the device buffer holding tesselated geometry for this polygon
        int glFillBufferHandle;
        // a reference to the device buffer holding line outline geometry for this polygon
        int glLineBufferHandle;

        // the maximum allocated size of the device buffer for this track
        int glFillBufferMaxSize;
        int glLineBufferMaxSize;
        // the currently used size of the device buffer for this track
        int glFillBufferCurrentSize;
        int glLineBufferCurrentSize;

        // offset into geometry buffers for each shape
        IntBuffer glLineOffsetBuffer;
        IntBuffer glFillOffsetBuffer;

        // number of vertices to read from geometry buffers for each shape
        IntBuffer glLineCountBuffer;
        IntBuffer glFillCountBuffer;

        // the number of elements in glLineOffsetBuffer and glLineCountBuffer
        int glTotalLinePrimitives;
        // the number of elements in glFillOffsetBuffer and glFillCountBuffer
        int glTotalFillPrimitives;

        public LoadedGroup( Group group )
        {
            this.loadSettings( group );
        }

        public void loadSettings( Group group )
        {
            this.glTotalLinePrimitives = group.selectedLinePrimitiveCount;
            this.glTotalFillPrimitives = group.selectedFillPrimitiveCount;

            this.lineColor[0] = group.lineColor[0];
            this.lineColor[1] = group.lineColor[1];
            this.lineColor[2] = group.lineColor[2];
            this.lineColor[3] = group.lineColor[3];

            this.fillColor[0] = group.fillColor[0];
            this.fillColor[1] = group.fillColor[1];
            this.fillColor[2] = group.fillColor[2];
            this.fillColor[3] = group.fillColor[3];

            this.lineWidth = group.lineWidth;

            this.fillOn = group.fillOn;
            this.linesOn = group.linesOn;
            this.lineStippleOn = group.lineStippleOn;
            this.polyStippleOn = group.polyStippleOn;

            this.lineStippleFactor = group.lineStippleFactor;
            this.lineStipplePattern = group.lineStipplePattern;

            System.arraycopy( group.polyStipplePattern, 0, this.polyStipplePattern, 0, this.polyStipplePattern.length );
        }

        protected void ensureLineOffsetBufferSize( int neededSize )
        {
            glLineOffsetBuffer = ensureBufferSize( glLineOffsetBuffer, neededSize );
        }

        protected void ensureLineCountBufferSize( int neededSize )
        {
            glLineCountBuffer = ensureBufferSize( glLineCountBuffer, neededSize );
        }

        protected void ensureFillOffsetBufferSize( int neededSize )
        {
            glFillOffsetBuffer = ensureBufferSize( glFillOffsetBuffer, neededSize );
        }

        protected void ensureFillCountBufferSize( int neededSize )
        {
            glFillCountBuffer = ensureBufferSize( glFillCountBuffer, neededSize );
        }

        protected IntBuffer ensureBufferSize( IntBuffer buffer, int neededSize )
        {
            if ( buffer == null || buffer.capacity( ) < neededSize )
            {
                int newCapacity = getNewBufferCapacity( buffer, neededSize );

                // copy all the offset information into a host buffer
                IntBuffer temp = ByteBuffer.allocateDirect( newCapacity * 4 ).order( ByteOrder.nativeOrder( ) ).asIntBuffer( );
                if ( buffer != null )
                {
                    buffer.rewind( );
                    temp.put( buffer );
                }
                buffer = temp;
            }

            buffer.rewind( );

            return buffer;
        }

        protected int getNewBufferCapacity( IntBuffer buffer, int neededSize )
        {
            if ( buffer != null )
            {
                return Math.max( ( int ) ( buffer.capacity( ) * 1.5 ), neededSize );
            }
            else
            {
                return neededSize;
            }
        }

        /**
         * Loads polygon outlines from the group into the provided buffer.
         *
         * @param vertexBuffer the buffer to place polygon vertices into
         * @param offsetVertex the offset of the vertices from the start of the vertexBuffer
         */
        public void loadLineVerticesIntoBuffer( Group group, FloatBuffer vertexBuffer, int offsetVertex, Collection<IdPolygon> polygons )
        {
            int vertexCount = 0;
            for ( IdPolygon polygon : polygons )
            {
                vertexCount += polygon.loadLineVerticesIntoBuffer( polygon.depth, vertexBuffer, offsetVertex + vertexCount );
            }
        }

        public void loadLineSelectionIntoBuffer( Collection<IdPolygon> polygons, int size )
        {
            int primitiveCount = 0;
            for ( IdPolygon polygon : polygons )
            {
                primitiveCount += polygon.linePrimitiveCount;
            }

            loadLineSelectionIntoBuffer( polygons, size, size - primitiveCount );
        }

        public void loadLineSelectionIntoBuffer( Collection<IdPolygon> polygons, int size, int offset )
        {
            if ( size <= 0 || offset < 0 ) return;

            ensureLineOffsetBufferSize( size );
            ensureLineCountBufferSize( size );

            glLineOffsetBuffer.position( offset );
            glLineCountBuffer.position( offset );

            for ( IdPolygon polygon : polygons )
            {
                polygon.loadLineIntoBuffer( glLineOffsetBuffer, glLineCountBuffer );
            }
        }

        public void loadFillVerticesIntoBuffer( Group group, FloatBuffer vertexBuffer, int offsetVertex, Collection<IdPolygon> polygons )
        {
            int vertexCount = 0;
            for ( IdPolygon polygon : polygons )
            {
                vertexCount += polygon.loadFillVerticesIntoBuffer( polygon.depth + 0.5f, vertexBuffer, offsetVertex + vertexCount );
            }
        }

        public void loadFillSelectionIntoBuffer( Collection<IdPolygon> polygons, int size )
        {
            if ( size <= 0 ) return;

            int primitiveCount = 0;
            for ( IdPolygon polygon : polygons )
            {
                primitiveCount += polygon.fillPrimitiveCount;
            }

            loadFillSelectionIntoBuffer( polygons, size, size - primitiveCount );
        }

        public void loadFillSelectionIntoBuffer( Collection<IdPolygon> polygons, int size, int offset )
        {
            ensureFillOffsetBufferSize( size );
            ensureFillCountBufferSize( size );

            if ( size <= 0 || offset < 0 ) return;

            glFillOffsetBuffer.position( offset );
            glFillCountBuffer.position( offset );

            for ( IdPolygon polygon : polygons )
            {
                polygon.loadFillIntoBuffer( glFillOffsetBuffer, glFillCountBuffer );
            }
        }

        public void dispose( GL gl )
        {
            // release opengl vertex buffers
            if ( glLineBufferInitialized )
            {
                // zero is a reserved buffer object name and is never returned by glGenBuffers
                if ( glLineBufferHandle > 0 ) gl.glDeleteBuffers( 1, new int[] { glLineBufferHandle }, 0 );
                if ( glFillBufferHandle > 0 ) gl.glDeleteBuffers( 1, new int[] { glFillBufferHandle }, 0 );
            }
        }
    }

    /**
     * An internal data structure representing a collection of IdPolygons who share
     * display characteristics (all IdPolygons in a Group appear identical except for
     * their geometry).</p>
     *
     * End users interact with this class only through its numeric id.</p>
     *
     * This class is accessed both by outside user threads and by the opengl display
     * thread. Because of this, all access to this class is controlled by a
     * ReentrantLock. When fields of this class change, the relevant information
     * is copied into a LoadedGroup which can then be accessed by opengl without
     * synchronization.</p>
     *
     * @author ulman
     */
    private class Group
    {
        float[] lineColor = new float[] { 1.0f, 1.0f, 0.0f, 1.0f };
        float lineWidth = 1;
        boolean linesOn = true;

        int lineStippleFactor = 1;
        short lineStipplePattern = ( short ) 0x00FF;
        boolean lineStippleOn = false;

        byte[] polyStipplePattern = halftone;
        boolean polyStippleOn = false;

        float[] fillColor = new float[] { 1.0f, 0.0f, 0.0f, 1.0f };
        boolean fillOn = false;

        Object groupId;

        // mapping from polygonId to IdPolygon object
        Map<Object, IdPolygon> polygonMap;
        // polygons added since last display( ) call 
        Set<IdPolygon> newPolygons;
        // polygons selected since the last display( ) call
        // (always a subset of newPolygons)
        Set<IdPolygon> newSelectedPolygons;
        // all selected polygons (based on selectionStart and selectionEnd)
        Set<IdPolygon> selectedPolygons;

        IntervalQuadTree<IdPolygon> map;

        Long selectionStart;
        Long selectionEnd;

        int selectedFillPrimitiveCount;
        int selectedLinePrimitiveCount;

        // vertices in the above counts refer to tesselated triangle vertices

        // current total vertex count
        int totalFillVertexCount;
        int totalLineVertexCount;

        // vertex count of inserts since last display() call
        int fillInsertVertexCount;
        int lineInsertVertexCount;

        // if true, the contents of the selectedPolygons set has changed
        boolean polygonsSelected = false;
        // if true, new polygons have been added to the group
        boolean polygonsInserted = false;
        // if true, this group is waiting to be deleted
        boolean groupDeleted = false;
        // if true, this group is waiting to be cleared
        boolean groupCleared = false;

        public Group( Object groupId )
        {
            this.groupId = groupId;
            this.selectedPolygons = new LinkedHashSet<IdPolygon>( );
            this.newSelectedPolygons = new LinkedHashSet<IdPolygon>( );
            this.newPolygons = new LinkedHashSet<IdPolygon>( );
            this.polygonMap = new HashMap<Object, IdPolygon>( );

            this.map = new IntervalQuadTree<IdPolygon>( 100 )
            {

                @Override
                public long getStartTimeMillis( IdPolygon v )
                {
                    return v.startTime;
                }

                @Override
                public long getEndTimeMillis( IdPolygon v )
                {
                    return v.endTime;
                }

            };

            this.selectionStart = -Long.MAX_VALUE;
            this.selectionEnd = Long.MAX_VALUE;
        }

        public void deleteGroup( )
        {
            this.groupDeleted = true;
            this.clearGroup( );
        }

        public void clearGroup( )
        {
            this.polygonMap.clear( );

            this.newPolygons.clear( );
            this.selectedPolygons.clear( );
            this.newSelectedPolygons.clear( );

            this.map.clear( );

            this.totalLineVertexCount = 0;
            this.lineInsertVertexCount = 0;

            this.totalFillVertexCount = 0;
            this.fillInsertVertexCount = 0;

            this.selectedFillPrimitiveCount = 0;
            this.selectedLinePrimitiveCount = 0;

            this.polygonsInserted = false;
            this.polygonsSelected = false;

            this.groupCleared = true;
        }

        public void deletePolygon( Object polygonId )
        {
            IdPolygon polygon = this.polygonMap.remove( polygonId );

            if ( polygon != null )
            {
                this.newPolygons.remove( polygon );
                this.map.remove( polygon );

                // if the polygon was selected when it is deleted, mark the selection changed
                this.polygonsSelected = this.selectedPolygons.remove( polygon );
                boolean newDeleted = this.newSelectedPolygons.remove( polygon );

                int lineVertexCount = polygon.lineVertexCount;
                this.totalLineVertexCount -= lineVertexCount;
                if ( newDeleted ) this.lineInsertVertexCount -= lineVertexCount;

                int fillVertexCount = polygon.fillVertexCount;
                this.totalFillVertexCount -= fillVertexCount;
                if ( newDeleted ) this.fillInsertVertexCount -= fillVertexCount;

                this.selectedFillPrimitiveCount -= polygon.fillPrimitiveCount;
                this.selectedLinePrimitiveCount -= polygon.linePrimitiveCount;
            }
        }

        public void addPolygon( IdPolygon polygon )
        {
            this.polygonMap.put( polygon.polygonId, polygon );

            this.newPolygons.add( polygon );

            this.map.add( polygon );

            int lineVertexCount = polygon.lineVertexCount;
            this.totalLineVertexCount += lineVertexCount;
            this.lineInsertVertexCount += lineVertexCount;

            int fillVertexCount = polygon.fillVertexCount;
            this.totalFillVertexCount += fillVertexCount;
            this.fillInsertVertexCount += fillVertexCount;

            if ( polygon.startTime <= selectionEnd && polygon.endTime >= selectionStart )
            {
                this.selectedFillPrimitiveCount += polygon.fillPrimitiveCount;
                this.selectedLinePrimitiveCount += polygon.linePrimitiveCount;
                this.selectedPolygons.add( polygon );
                this.newSelectedPolygons.add( polygon );
            }

            this.polygonsInserted = true;
        }

        public void setTimeRange( Long startTime, Long endTime )
        {
            this.selectionStart = startTime;
            this.selectionEnd = endTime;

            checkTimeRange( );
        }

        public void checkTimeRange( )
        {
            if ( this.selectionStart == null || this.selectionEnd == null ) return;

            this.selectedPolygons.clear( );
            this.selectedPolygons.addAll( this.map.get( this.selectionStart, true, this.selectionEnd, true ) );

            this.selectedFillPrimitiveCount = 0;
            this.selectedLinePrimitiveCount = 0;
            for ( IdPolygon polygon : this.selectedPolygons )
            {
                this.selectedFillPrimitiveCount += polygon.fillPrimitiveCount;
                this.selectedLinePrimitiveCount += polygon.linePrimitiveCount;
            }

            this.polygonsSelected = true;
        }

        public void setLineColor( float[] rgba )
        {
            this.lineColor = rgba;
        }

        public void setLineColor( float r, float g, float b, float a )
        {
            this.lineColor[0] = r;
            this.lineColor[1] = g;
            this.lineColor[2] = b;
            this.lineColor[3] = a;
        }

        public void setFillColor( float[] rgba )
        {
            this.fillColor = rgba;
        }

        public void setFillColor( float r, float g, float b, float a )
        {
            this.fillColor[0] = r;
            this.fillColor[1] = g;
            this.fillColor[2] = b;
            this.fillColor[3] = a;
        }

        public void setLineWidth( float width )
        {
            this.lineWidth = width;
        }

        public void setShowLines( boolean show )
        {
            this.linesOn = show;
        }

        public void setShowPoly( boolean show )
        {
            this.fillOn = show;
        }

        public void setPolyStipple( boolean activate )
        {
            this.polyStippleOn = activate;
        }

        public void setPolyStipple( byte[] pattern )
        {
            this.polyStipplePattern = pattern;
        }

        public void setLineStipple( boolean activate )
        {
            this.lineStippleOn = activate;
        }

        public void setLineStipple( int stippleFactor, short stipplePattern )
        {
            this.lineStippleFactor = stippleFactor;
            this.lineStipplePattern = stipplePattern;
        }

        public void reset( )
        {
            this.newSelectedPolygons.clear( );
            this.newPolygons.clear( );

            this.lineInsertVertexCount = 0;
            this.fillInsertVertexCount = 0;
            this.polygonsInserted = false;
            this.polygonsSelected = false;
            this.groupCleared = false;
            this.groupDeleted = false;
        }
    }
}
