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

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
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
    protected static final Comparator<IdPolygon> startTimeComparator = new Comparator<IdPolygon>( )
    {
        @Override
        public int compare( IdPolygon p1, IdPolygon p2 )
        {
            if ( p1.startTime < p2.startTime )
            {
                return -1;
            }
            else if ( p1.startTime > p2.startTime )
            {
                return 1;
            }
            else
            {
                if ( p1.groupId < p2.groupId )
                {
                    return -1;
                }
                else if ( p1.groupId > p2.groupId )
                {
                    return 1;
                }
                else
                {
                    if ( p1.polygonId < p2.polygonId )
                    {
                        return -1;
                    }
                    else if ( p1.polygonId > p2.polygonId )
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            }
        }
    };

    protected static final Comparator<IdPolygon> endTimeComparator = new Comparator<IdPolygon>( )
    {
        @Override
        public int compare( IdPolygon p1, IdPolygon p2 )
        {
            if ( p1.endTime < p2.endTime )
            {
                return -1;
            }
            else if ( p1.endTime > p2.endTime )
            {
                return 1;
            }
            else
            {
                if ( p1.groupId < p2.groupId )
                {
                    return -1;
                }
                else if ( p1.groupId > p2.groupId )
                {
                    return 1;
                }
                else
                {
                    if ( p1.polygonId < p2.polygonId )
                    {
                        return -1;
                    }
                    else if ( p1.polygonId > p2.polygonId )
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            }
        }
    };

    protected byte halftone[] = { ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0xAA, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55, ( byte ) 0x55 };

    protected PolygonTessellator tessellator;

    protected int dataBufferSize = 0;
    protected FloatBuffer dataBuffer = null;

    // mapping from id to Group
    protected Map<Integer, Group> groups;
    // true indicates that new data must be loaded onto the GPU
    protected volatile boolean newData = false;
    // groups with new data which must be loaded onto the GPU
    protected Set<Group> updatedGroups;
    // mapping from id to LoadedGroup (GPU-side group information)
    protected Map<Integer, LoadedGroup> loadedGroups;

    protected ReentrantLock updateLock;

    protected IdPolygon globalSelectionStart;
    protected IdPolygon globalSelectionEnd;

    public PolygonPainter( )
    {
        this.tessellator = new PolygonTessellator( glu );

        this.groups = new LinkedHashMap<Integer, Group>( );
        this.updatedGroups = new LinkedHashSet<Group>( );
        this.loadedGroups = new LinkedHashMap<Integer, LoadedGroup>( );

        this.updateLock = new ReentrantLock( );
    }

    public void addPolygon( int groupId, int polygonId, float[] dataX, float[] dataY, float z )
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

    public void addPolygon( int groupId, int polygonId, Polygon geometry, float z )
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

    public void addPolygon( int groupId, int polygonId, Shape shape, float z )
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

    public void addPolygon( int groupId, int polygonId, long startTime, long endTime, float[] dataX, float[] dataY, float z )
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

    public void addPolygon( int groupId, int polygonId, long startTime, long endTime, Polygon geometry, float z )
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

    public void addPolygon( int groupId, int polygonId, long startTime, long endTime, Shape shape, float z )
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

    public void displayTimeRange( int groupId, double startTime, double endTime )
    {
        displayTimeRange( groupId, ( long ) Math.ceil( startTime ), ( long ) Math.floor( endTime ) );
    }

    public void displayTimeRange( double startTime, double endTime )
    {
        displayTimeRange( ( long ) Math.ceil( startTime ), ( long ) Math.floor( endTime ) );
    }

    public void displayTimeRange( int groupId, long startTime, long endTime )
    {
        IdPolygon startPoint = createSearchBoundStart( startTime );
        IdPolygon endPoint = createSearchBoundEnd( endTime );

        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.setTimeRange( startPoint, endPoint );

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
        globalSelectionStart = createSearchBoundStart( startTime );
        globalSelectionEnd = createSearchBoundEnd( endTime );

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

    // create a dummy IdPolygon representing the end of a search time window
    protected IdPolygon createSearchBoundEnd( long time )
    {
        return new IdPolygon( Integer.MAX_VALUE, Integer.MAX_VALUE, time, time, null, 0 );
    }

    // create a dummy IdPolygon representing the start of a search time window
    protected IdPolygon createSearchBoundStart( long time )
    {
        return new IdPolygon( Integer.MIN_VALUE, Integer.MIN_VALUE, time, time, null, 0 );
    }

    public void setLineColor( int groupId, float[] rgba )
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

    public void setLineColor( int groupId, float r, float g, float b, float a )
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

    public void setLineWidth( int groupId, float width )
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

    public void setShowLines( int groupId, boolean show )
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

    public void setPolyDotted( int groupId, byte[] stipple )
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

    public void setPolyDotted( int groupId, boolean dotted )
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

    public void setLineDotted( int groupId, boolean dotted )
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

    public void setLineDotted( int groupId, int stippleFactor, short stipplePattern )
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

    public void setFill( int groupId, boolean show )
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

    public void setFillColor( int groupId, float[] rgba )
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

    public void setFillColor( int groupId, float r, float g, float b, float a )
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
                group.delete( );
            }

            this.updatedGroups.addAll( groups.values( ) );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }
    
    public void deleteGroups( )
    {
        this.updateLock.lock( );
        try
        {
            for ( Group group : groups.values( ) )
            {
                group.delete( );
    
                this.updatedGroups.add( group );
            }
            
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
    public void deleteGroup( int groupId )
    {
        this.updateLock.lock( );
        try
        {
            if ( !groups.containsKey( groupId ) ) return;

            Group group = groups.get( groupId );

            group.delete( );

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
    public void clearGroup( int groupId )
    {
        this.updateLock.lock( );
        try
        {
            if ( !groups.containsKey( groupId ) ) return;

            Group group = groups.get( groupId );

            group.clear( );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    public void deletePolygon( int groupId, int polygonId )
    {
        throw new UnsupportedOperationException( "Deletion of single polygons is not currently supported. Use deleteGroup() to remove an entire group." );
    }

    protected void addPolygon( int groupId, IdPolygon polygon )
    {
        this.updateLock.lock( );
        try
        {
            Group group = getOrCreateGroup( groupId );

            group.add( polygon );

            this.updatedGroups.add( group );
            this.newData = true;
        }
        finally
        {
            this.updateLock.unlock( );
        }
    }

    // must be called while holding trackUpdateLock
    protected Group getOrCreateGroup( int groupId )
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

    protected LoadedGroup getOrCreateLoadedGroup( int id, Group group )
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
        GL gl = context.getGL( );

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
                    int id = group.groupId;

                    if ( group.isDeletePending( ) || group.isClearPending( ) )
                    {
                        // if the corresponding LoadedGroup does not exist, create it
                        LoadedGroup loaded = getOrCreateLoadedGroup( id, group );
                        loaded.dispose( gl );
                        loadedGroups.remove( id );

                        // If the group was deleted then recreated in between calls to display0(),
                        // (both isDataInserted() and isDeletePending() are true) then don't remove the group
                        if ( group.isDeletePending( ) && !group.isDataInserted( ) )
                        {
                            groups.remove( id );
                            continue;
                        }
                    }

                    // if the corresponding LoadedGroup does not exist, create it
                    LoadedGroup loaded = getOrCreateLoadedGroup( id, group );

                    // copy settings from the Group to the LoadedGroup
                    loaded.loadSettings( group );

                    if ( group.isDataInserted( ) )
                    {
                        ///////////////////////////////////////
                        //// load polygon outline geometry ////
                        ///////////////////////////////////////

                        if ( !loaded.glLineBufferInitialized || loaded.glLineBufferMaxSize < group.getTotalLineVertices( ) )
                        {
                            // if the track doesn't have a gl buffer or it is too small we must
                            // copy all the track's data into a new, larger buffer

                            // if this is the first time we have allocated memory for this track
                            // don't allocate any extra, it may never get added to
                            // however, once a track has been updated once, we assume it is likely
                            // to be updated again and give it extra memory
                            if ( loaded.glLineBufferInitialized )
                            {
                                gl.glDeleteBuffers( 1, new int[] { loaded.glLineBufferHandle }, 0 );
                                loaded.glLineBufferMaxSize = Math.max( ( int ) ( loaded.glLineBufferMaxSize * 1.5 ), group.getTotalLineVertices( ) );
                            }
                            else
                            {
                                loaded.glLineBufferMaxSize = group.getTotalLineVertices( );
                            }

                            // copy all the track data into a host buffer
                            ensureDataBufferSize( loaded.glLineBufferMaxSize );
                            loaded.loadLineVerticesIntoBuffer( group, dataBuffer, 0, 0, group.getPolygonCount( ) );

                            // create a new device buffer handle
                            int[] bufferHandle = new int[1];
                            gl.glGenBuffers( 1, bufferHandle, 0 );
                            loaded.glLineBufferHandle = bufferHandle[0];

                            // load the offset and count values for all selected polygons
                            loaded.loadLineSelectionIntoBuffer( group.selectedPolygons, group.selectedLinePrimitiveCount, 0 );

                            // copy data from the host buffer into the device buffer
                            gl.glBindBuffer( GL.GL_ARRAY_BUFFER, loaded.glLineBufferHandle );
                            glHandleError( gl, "glBindBuffer Line Error (Case 1)" );
                            gl.glBufferData( GL.GL_ARRAY_BUFFER, loaded.glLineBufferMaxSize * 3 * BYTES_PER_FLOAT, dataBuffer.rewind( ), GL.GL_DYNAMIC_DRAW );
                            glHandleError( gl, "glBufferData Line Error" );

                            loaded.glLineBufferInitialized = true;
                        }
                        else
                        {
                            // there is enough empty space in the device buffer to accommodate all the new data
                            int insertOffset = group.getOffsetInsertPolygons( );
                            int insertCount = group.getCountInsertPolygons( );
                            int insertVertices = group.getLineInsertCountVertices( );

                            // copy all the new track data into a host buffer
                            ensureDataBufferSize( insertVertices );
                            loaded.loadLineVerticesIntoBuffer( group, dataBuffer, loaded.glLineBufferCurrentSize, insertOffset, insertCount );

                            // load the offset and count values for the newly selected polygons
                            loaded.loadLineSelectionIntoBuffer( group.newSelectedPolygons, group.selectedLinePrimitiveCount );

                            // update the device buffer with the new data
                            gl.glBindBuffer( GL.GL_ARRAY_BUFFER, loaded.glLineBufferHandle );
                            glHandleError( gl, "glBindBuffer Line Error  (Case 2)" );
                            gl.glBufferSubData( GL.GL_ARRAY_BUFFER, loaded.glLineBufferCurrentSize * 3 * BYTES_PER_FLOAT, insertVertices * 3 * BYTES_PER_FLOAT, dataBuffer.rewind( ) );
                            glHandleError( gl, "glBufferSubData Line Error" );
                        }

                        loaded.glLineBufferCurrentSize = group.getTotalLineVertices( );

                        ////////////////////////////////////
                        //// load polygon fill geometry ////
                        ////////////////////////////////////

                        if ( !loaded.glFillBufferInitialized || loaded.glFillBufferMaxSize < group.getTotalFillVertices( ) )
                        {
                            // if the track doesn't have a gl buffer or it is too small we must
                            // copy all the track's data into a new, larger buffer

                            // if this is the first time we have allocated memory for this track
                            // don't allocate any extra, it may never get added to
                            // however, once a track has been updated once, we assume it is likely
                            // to be updated again and give it extra memory
                            if ( loaded.glFillBufferInitialized )
                            {
                                gl.glDeleteBuffers( 1, new int[] { loaded.glFillBufferHandle }, 0 );
                                loaded.glFillBufferMaxSize = Math.max( ( int ) ( loaded.glFillBufferMaxSize * 1.5 ), group.getTotalFillVertices( ) );
                            }
                            else
                            {
                                loaded.glFillBufferMaxSize = group.getTotalFillVertices( );
                            }

                            // copy all the track data into a host buffer
                            ensureDataBufferSize( loaded.glFillBufferMaxSize );
                            loaded.loadFillVerticesIntoBuffer( group, dataBuffer, 0, 0, group.getPolygonCount( ) );

                            // create a new device buffer handle
                            int[] bufferHandle = new int[1];
                            gl.glGenBuffers( 1, bufferHandle, 0 );
                            loaded.glFillBufferHandle = bufferHandle[0];

                            // load the offset and count values for all selected polygons
                            loaded.loadFillSelectionIntoBuffer( group.selectedPolygons, group.selectedFillPrimitiveCount, 0 );

                            // copy data from the host buffer into the device buffer
                            gl.glBindBuffer( GL.GL_ARRAY_BUFFER, loaded.glFillBufferHandle );
                            glHandleError( gl, "glBindBuffer Fill Error  (Case 1)" );
                            gl.glBufferData( GL.GL_ARRAY_BUFFER, loaded.glFillBufferMaxSize * 3 * BYTES_PER_FLOAT, dataBuffer.rewind( ), GL.GL_DYNAMIC_DRAW );
                            glHandleError( gl, "glBufferData Fill Error" );

                            loaded.glFillBufferInitialized = true;
                        }
                        else
                        {
                            // there is enough empty space in the device buffer to accommodate all the new data
                            int insertOffset = group.getOffsetInsertPolygons( );
                            int insertCount = group.getCountInsertPolygons( );
                            int insertVertices = group.getFillInsertCountVertices( );

                            // copy all the new track data into a host buffer
                            ensureDataBufferSize( insertVertices );
                            loaded.loadFillVerticesIntoBuffer( group, dataBuffer, loaded.glFillBufferCurrentSize, insertOffset, insertCount );

                            // load the offset and count values for the newly selected polygons
                            loaded.loadFillSelectionIntoBuffer( group.newSelectedPolygons, group.selectedFillPrimitiveCount );

                            // update the device buffer with the new data
                            gl.glBindBuffer( GL.GL_ARRAY_BUFFER, loaded.glFillBufferHandle );
                            glHandleError( gl, "glBindBuffer Fill Error  (Case 2)" );
                            gl.glBufferSubData( GL.GL_ARRAY_BUFFER, loaded.glFillBufferCurrentSize * 3 * BYTES_PER_FLOAT, insertVertices * 3 * BYTES_PER_FLOAT, dataBuffer.rewind( ) );
                            glHandleError( gl, "glBufferSubData Fill Error" );
                        }

                        loaded.glFillBufferCurrentSize = group.getTotalFillVertices( );
                    }

                    if ( group.selectionChanged && loaded.glLineBufferInitialized && loaded.glFillBufferInitialized )
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

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( axis.getMinX( ), axis.getMaxX( ), axis.getMinY( ), axis.getMaxY( ), -1 << 23, 1 );

        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_BLEND );
        gl.glEnable( GL.GL_LINE_SMOOTH );

        gl.glEnableClientState( GL.GL_VERTEX_ARRAY );

        for ( LoadedGroup loaded : loadedGroups.values( ) )
        {
            if ( !loaded.glFillBufferInitialized || !loaded.glLineBufferInitialized ) continue;

            if ( loaded.fillOn )
            {
                gl.glColor4fv( loaded.fillColor, 0 );

                if ( loaded.polyStippleOn )
                {
                    gl.glEnable( GL.GL_POLYGON_STIPPLE );
                    gl.glPolygonStipple( loaded.polyStipplePattern, 0 );
                }

                gl.glBindBuffer( GL.GL_ARRAY_BUFFER, loaded.glFillBufferHandle );
                gl.glVertexPointer( 3, GL.GL_FLOAT, 0, 0 );

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
                        gl.glDrawArrays( GL.GL_TRIANGLES, offset, fillCount );
                        fillCountRemaining -= fillCount;
                    }
                }

                // XXX: Old way uses glMultiDrawArrays, but if one of the arrays is > 65535 in size, it will render incorrectly on some machines.
                // gl.glMultiDrawArrays( GL.GL_TRIANGLES, loaded.glFillOffsetBuffer, loaded.glFillCountBuffer, loaded.glTotalFillPrimitives );

                if ( loaded.polyStippleOn )
                {
                    gl.glDisable( GL.GL_POLYGON_STIPPLE );
                }
            }

            if ( loaded.linesOn )
            {
                gl.glColor4fv( loaded.lineColor, 0 );
                gl.glLineWidth( loaded.lineWidth );

                if ( loaded.lineStippleOn )
                {
                    gl.glEnable( GL.GL_LINE_STIPPLE );
                    gl.glLineStipple( loaded.lineStippleFactor, loaded.lineStipplePattern );
                }

                gl.glBindBuffer( GL.GL_ARRAY_BUFFER, loaded.glLineBufferHandle );
                gl.glVertexPointer( 3, GL.GL_FLOAT, 0, 0 );

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
                        gl.glDrawArrays( GL.GL_LINE_LOOP, offset, fillCount );
                        fillCountRemaining -= fillCount;
                    }
                }

                // XXX: Old way uses glMultiDrawArrays, but if one of the arrays is > 65535 in size, it will render incorrectly on some machines.
                // gl.glMultiDrawArrays( GL.GL_LINE_LOOP, loaded.glLineOffsetBuffer, loaded.glLineCountBuffer, loaded.glTotalLinePrimitives );

                if ( loaded.lineStippleOn )
                {
                    gl.glDisable( GL.GL_LINE_STIPPLE );
                }
            }
        }

        glHandleError( gl, "Draw Error" );

        gl.glDisable( GL.GL_DEPTH_TEST );
        gl.glDisable( GL.GL_BLEND );
        gl.glDisable( GL.GL_LINE_SMOOTH );
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
        int groupId;
        int polygonId;

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

        protected IdPolygon( int groupId, int polygonId, long startTime, long endTime, Polygon geometry, float depth )
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

        protected IdPolygon( int groupId, int polygonId, Polygon geometry, float z )
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

        public long getStartTime( )
        {
            return startTime;
        }

        public long getEndTime( )
        {
            return endTime;
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
            result = prime * result + groupId;
            result = prime * result + polygonId;
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
            if ( groupId != other.groupId ) return false;
            if ( polygonId != other.polygonId ) return false;
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

        byte[] polyStipplePattern = new byte[32];
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
         * @param offset the offset from the start of the buffer array of the first polygon to add
         * @param size the number of polygons from the polygon array to add
         */
        public void loadLineVerticesIntoBuffer( Group group, FloatBuffer vertexBuffer, int offsetVertex, int offset, int size )
        {
            int vertexCount = 0;
            for ( int i = 0; i < size; i++ )
            {
                IdPolygon polygon = group.polygonIndices.get( offset + i );
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
            ensureLineOffsetBufferSize( size );
            ensureLineCountBufferSize( size );

            if ( size <= 0 || offset < 0 ) return;

            glLineOffsetBuffer.position( offset );
            glLineCountBuffer.position( offset );

            for ( IdPolygon polygon : polygons )
            {
                polygon.loadLineIntoBuffer( glLineOffsetBuffer, glLineCountBuffer );
            }
        }

        public void loadFillVerticesIntoBuffer( Group group, FloatBuffer vertexBuffer, int offsetVertex, int offset, int size )
        {
            if ( size <= 0 ) return;

            int vertexCount = 0;
            for ( int i = 0; i < size; i++ )
            {
                IdPolygon polygon = group.polygonIndices.get( offset + i );
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
                gl.glDeleteBuffers( 1, new int[] { glLineBufferHandle }, 0 );
                gl.glDeleteBuffers( 1, new int[] { glFillBufferHandle }, 0 );
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
        //// group display attributes ////
        float[] lineColor = new float[] { 1.0f, 1.0f, 0.0f, 1.0f };
        float lineWidth = 1;
        boolean linesOn = true;

        int lineStippleFactor = 1;
        short lineStipplePattern = ( short ) 0x00FF;;
        boolean lineStippleOn = false;

        byte[] polyStipplePattern = halftone;
        boolean polyStippleOn = false;

        float[] fillColor = new float[] { 1.0f, 0.0f, 0.0f, 1.0f };
        boolean fillOn = false;
        //// group display attributes ////

        int groupId;

        //TODO: allow deletions, there is no need to compact the buffer after
        // each deletion, perhaps wait a configurable number of deletions
        // before doing it automatically
        List<IdPolygon> polygonIndices;
        // view of the IdPolygons in the polygons list sorted by startTime
        NavigableSet<IdPolygon> startTimes;
        // view of the IdPolygons in the polygons list sorted by endTime
        NavigableSet<IdPolygon> endTimes;
        // the polygons selected based on selectionStart and selectionEnd
        Set<IdPolygon> selectedPolygons;
        // polygons selected since the last display( ) call
        Set<IdPolygon> newSelectedPolygons;

        IdPolygon selectionStart;
        IdPolygon selectionEnd;

        int selectedFillPrimitiveCount;
        int selectedLinePrimitiveCount;

        int totalFillVertexCount;
        int totalLineVertexCount;

        //        int totalFillPrimitiveCount;
        //        int totalLinePrimitiveCount;

        int fillInsertVertexCount;
        int lineInsertVertexCount;

        int offsetInsertPolygons;

        // if true, the contents of the selectedPolygons set has changed
        boolean selectionChanged = false;
        // if true, new polygons have been added to the group
        boolean dataInserted = false;
        // if true, this group is waiting to be deleted
        boolean deletePending = false;
        // if true, this group is waiting to be cleared
        boolean clearPending = false;

        public Group( int groupId )
        {
            this.groupId = groupId;
            this.selectedPolygons = new LinkedHashSet<IdPolygon>( );
            this.newSelectedPolygons = new LinkedHashSet<IdPolygon>( );
            this.polygonIndices = new LinkedList<IdPolygon>( );
            this.startTimes = new TreeSet<IdPolygon>( startTimeComparator );
            this.endTimes = new TreeSet<IdPolygon>( endTimeComparator );
            this.selectionStart = createSearchBoundStart( -Long.MAX_VALUE );
            this.selectionEnd = createSearchBoundEnd( Long.MAX_VALUE );
        }

        public void delete( )
        {
            this.deletePending = true;
            this.clear( );
        }

        public void clear( )
        {
            this.polygonIndices.clear( );
            this.startTimes.clear( );
            this.endTimes.clear( );

            this.totalLineVertexCount = 0;
            this.lineInsertVertexCount = 0;
            //            this.totalLinePrimitiveCount = 0;

            this.totalFillVertexCount = 0;
            this.fillInsertVertexCount = 0;
            //            this.totalFillPrimitiveCount = 0;

            this.selectedPolygons.clear( );
            this.newSelectedPolygons.clear( );

            this.selectedFillPrimitiveCount = 0;
            this.selectedLinePrimitiveCount = 0;

            this.offsetInsertPolygons = 0;
            this.dataInserted = false;
            this.selectionChanged = false;

            this.clearPending = true;
        }

        public void add( IdPolygon polygon )
        {
            int polygonInsertIndex = this.polygonIndices.size( );

            this.polygonIndices.add( polygon );
            this.startTimes.add( polygon );
            this.endTimes.add( polygon );

            int lineVertexCount = polygon.lineVertexCount;
            //            int linePrimitiveCount = polygon.linePrimitiveCount;
            this.totalLineVertexCount += lineVertexCount;
            this.lineInsertVertexCount += lineVertexCount;
            //            this.totalLinePrimitiveCount += linePrimitiveCount;

            int fillVertexCount = polygon.fillVertexCount;
            //            int fillPrimitiveCount = polygon.fillPrimitiveCount;
            this.totalFillVertexCount += fillVertexCount;
            this.fillInsertVertexCount += fillVertexCount;
            //            this.totalFillPrimitiveCount += fillPrimitiveCount;

            //TODO using IdPolygons to hold start/end times of window is awkward
            if ( polygon.getStartTime( ) <= selectionEnd.endTime && polygon.getEndTime( ) >= selectionStart.startTime )
            {
                selectedFillPrimitiveCount += polygon.fillPrimitiveCount;
                selectedLinePrimitiveCount += polygon.linePrimitiveCount;
                selectedPolygons.add( polygon );
                newSelectedPolygons.add( polygon );
            }

            if ( !dataInserted || polygonInsertIndex < offsetInsertPolygons )
            {
                offsetInsertPolygons = polygonInsertIndex;
                dataInserted = true;
            }
        }

        public void setTimeRange( IdPolygon startPoint, IdPolygon endPoint )
        {
            selectionStart = startPoint;
            selectionEnd = endPoint;

            checkTimeRange( );
        }

        public void checkTimeRange( )
        {
            if ( selectionStart == null || selectionEnd == null ) return;

            SortedSet<IdPolygon> startSet = startTimes.headSet( selectionEnd, true );
            SortedSet<IdPolygon> endSet = endTimes.tailSet( selectionStart, true );

            // selectedPolys contains the set intersection of startSet and endSet
            selectedPolygons.clear( );
            selectedPolygons.addAll( startSet );
            selectedPolygons.retainAll( endSet );

            selectedFillPrimitiveCount = 0;
            selectedLinePrimitiveCount = 0;
            for ( IdPolygon polygon : selectedPolygons )
            {
                selectedFillPrimitiveCount += polygon.fillPrimitiveCount;
                selectedLinePrimitiveCount += polygon.linePrimitiveCount;
            }

            selectionChanged = true;
        }

        public void setLineColor( float[] rgba )
        {
            lineColor = rgba;
        }

        public void setLineColor( float r, float g, float b, float a )
        {
            lineColor[0] = r;
            lineColor[1] = g;
            lineColor[2] = b;
            lineColor[3] = a;
        }

        public void setFillColor( float[] rgba )
        {
            fillColor = rgba;
        }

        public void setFillColor( float r, float g, float b, float a )
        {
            fillColor[0] = r;
            fillColor[1] = g;
            fillColor[2] = b;
            fillColor[3] = a;
        }

        public void setLineWidth( float width )
        {
            lineWidth = width;
        }

        public void setShowLines( boolean show )
        {
            linesOn = show;
        }

        public void setShowPoly( boolean show )
        {
            fillOn = show;
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

        public boolean isDataInserted( )
        {
            return dataInserted;
        }

        public boolean isDeletePending( )
        {
            return deletePending;
        }

        public boolean isClearPending( )
        {
            return clearPending;
        }

        public void reset( )
        {
            newSelectedPolygons.clear( );
            lineInsertVertexCount = 0;
            fillInsertVertexCount = 0;
            dataInserted = false;
            selectionChanged = false;
            clearPending = false;
            deletePending = false;
        }

        /**
         * @return the number of vertices making up the polygon outline to insert
         */
        public int getLineInsertCountVertices( )
        {
            return lineInsertVertexCount;
        }

        /**
         * @return the number of vertices making up the polygon fill to insert
         */
        public int getFillInsertCountVertices( )
        {
            return fillInsertVertexCount;
        }

        /**
         * @return the index of the first polygon in the group to insert
         */
        public int getOffsetInsertPolygons( )
        {
            return offsetInsertPolygons;
        }

        /**
         * @return the number of polygons in the group to insert
         */
        public int getCountInsertPolygons( )
        {
            return getPolygonCount( ) - getOffsetInsertPolygons( );
        }

        /**
         * @return the total number of edge vertices for all polygons in the group.
         */
        public int getTotalLineVertices( )
        {
            return totalLineVertexCount;
        }

        /**
         * @return the total number of tesselated triangle vertices for all polygons in the group.
         */
        public int getTotalFillVertices( )
        {
            return totalFillVertexCount;
        }

        /**
         * @return the total number of polygons in the group.
         */
        public int getPolygonCount( )
        {
            return polygonIndices.size( );
        }
    }
}
