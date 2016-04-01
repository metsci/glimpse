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
package com.metsci.glimpse.painter.track;

import java.awt.Color;
import java.awt.Font;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.selection.SpatialSelectionAxisListener;
import com.metsci.glimpse.support.selection.SpatialSelectionListener;
import com.metsci.glimpse.support.selection.TemporalSelectionListener;
import com.metsci.glimpse.util.quadtree.QuadTreeXys;

/**
 * Paints groups of line segments of points with associated timestamps.
 * Often these points represent the locations of objects moving over time
 * (here referred to as a track). {@code TrackPainter} allows very fast
 * selection of specified time segments within the set of tracks, hiding
 * all segments outside this time window.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.animated.AnimatedGeoPlotExample
 */
public class TrackPainter extends GlimpseDataPainter2D
{
    public static final int QUAD_TREE_BIN_MAX = 1000;

    public static final long SPATIAL_SELECTION_UPDATE_RATE = 50;

    public static final int TRACK_SIZE_ESTIMATE = 100;
    public static final int TRACK_LABEL_OFFSET_X = 8;
    public static final int TRACK_LABEL_OFFSET_Y = 8;

    public static final Comparator<Point> comparator = Point.getTimeComparator( );

    public static final Font textFont = FontUtils.getDefaultBold( 12 );

    protected int dataBufferSize = 0;
    protected FloatBuffer dataBuffer = null;
    protected ReentrantLock trackUpdateLock = null;

    // mapping from id to Track
    protected Map<Object, Track> tracks;
    // true indicates that new data must be loaded onto the GPU
    protected volatile boolean newData = false;
    // tracks with new data which must be loaded onto the GPU
    protected Set<Track> updatedTracks;
    // mapping from id to LoadedTrack (GPU-side track information)
    protected Map<Object, LoadedTrack> loadedTracks;
    // spatial index on Points
    protected QuadTreeXys<Point> spatialIndex;

    // the overall start and end times set by displayTimeRange
    // when new tracks are created, they inherit these time bounds
    protected Point startTimeRange = getStartPoint( Long.MIN_VALUE );
    protected Point selectedTimeRange = getEndPoint( Long.MAX_VALUE );
    protected Point endTimeRange = getEndPoint( Long.MAX_VALUE );

    protected Collection<TemporalSelectionListener<Point>> temporalSelectionListeners;

    protected TextRenderer fontRenderer;

    public TrackPainter( )
    {
        this( false );
    }

    public TrackPainter( boolean enableSpatialIndex )
    {
        if ( enableSpatialIndex ) this.spatialIndex = new QuadTreeXys<Point>( QUAD_TREE_BIN_MAX );

        this.temporalSelectionListeners = new CopyOnWriteArrayList<TemporalSelectionListener<Point>>( );

        this.tracks = new HashMap<>( );
        this.updatedTracks = new HashSet<>( );
        this.loadedTracks = new HashMap<>( );
        this.trackUpdateLock = new ReentrantLock( );

        this.fontRenderer = new TextRenderer( textFont );
    }

    public void addTemporalSelectionListener( TemporalSelectionListener<Point> listener )
    {
        this.temporalSelectionListeners.add( listener );
    }

    public void removeTemporalSelectionListener( TemporalSelectionListener<Point> listener )
    {
        this.temporalSelectionListeners.remove( listener );
    }

    public void addSpatialSelectionListener( Axis2D axis, SpatialSelectionListener<Point> listener )
    {
        axis.addAxisListener( new SpatialSelectionAxisListener( this, listener ) );
    }

    public Collection<Point> getTrackHeads( )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Collection<Point> trackHeads = new ArrayList<Point>( tracks.size( ) );

            for ( Track track : tracks.values( ) )
            {
                if ( track != null )
                {
                    trackHeads.add( track.getTrackHead( ) );
                }
            }

            return trackHeads;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public Point getTrackHead( Object trackId )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = this.tracks.get( trackId );

            if ( track != null )
            {
                return track.getTrackHead( );
            }
            else
            {
                return null;
            }
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void deleteAll( )
    {
        this.trackUpdateLock.lock( );
        try
        {
            for ( Track track : tracks.values( ) )
            {
                track.delete( );
            }

            if ( this.spatialIndex != null ) this.spatialIndex = new QuadTreeXys<Point>( QUAD_TREE_BIN_MAX );

            this.updatedTracks.addAll( tracks.values( ) );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void deleteTrack( Object trackId )
    {
        this.trackUpdateLock.lock( );
        try
        {
            if ( !tracks.containsKey( trackId ) ) return;

            Track track = tracks.get( trackId );

            if ( this.spatialIndex != null )
            {
                for ( Point p : track.points )
                {
                    this.spatialIndex.remove( p );
                }
            }

            track.delete( );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void clearTrack( Object trackId )
    {
        this.trackUpdateLock.lock( );
        try
        {
            if ( !tracks.containsKey( trackId ) ) return;

            Track track = tracks.get( trackId );

            if ( this.spatialIndex != null )
            {
                for ( Point p : track.points )
                {
                    this.spatialIndex.remove( p );
                }
            }

            track.clear( );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void addPoint( Object trackId, Object pointId, double x, double y, long time )
    {
        addPoint( trackId, new Point( trackId, pointId, x, y, time ) );
    }

    public void addPoints( Object trackId, List<Point> points )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.add( points );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setLineColor( Object trackId, float[] color )
    {
        setLineColor( trackId, color[0], color[1], color[2], color[3] );
    }

    public void setLineColor( Object trackId, float r, float g, float b, float a )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setLineColor( r, g, b, a );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setLineWidth( Object trackId, float width )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setLineWidth( width );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setPointColor( Object trackId, float[] color )
    {
        setPointColor( trackId, color[0], color[1], color[2], color[3] );
    }

    public void setPointColor( Object trackId, float r, float g, float b, float a )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setPointColor( r, g, b, a );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setPointSize( Object trackId, float size )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setPointSize( size );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setShowPoints( Object trackId, boolean show )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setShowPoints( show );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setHeadPointColor( Object trackId, float[] color )
    {
        setHeadPointColor( trackId, color[0], color[1], color[2], color[3] );
    }

    public void setHeadPointColor( Object trackId, float r, float g, float b, float a )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setHeadPointColor( r, g, b, a );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setHeadPointSize( Object trackId, float size )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setHeadPointSize( size );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setShowHeadPoint( Object trackId, boolean show )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setShowHeadPoint( show );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setShowLines( Object trackId, boolean show )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setShowLines( show );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setDotted( Object trackId, boolean dotted )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setTrackStipple( dotted );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setDotted( Object trackId, int stippleFactor, short stipplePattern )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setTrackStipple( true );
            track.setTrackStipple( stippleFactor, stipplePattern );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setLabelColor( Object trackId, float[] color )
    {
        setLabelColor( trackId, color[0], color[1], color[2], color[3] );
    }

    public void setLabelColor( Object trackId, float r, float g, float b, float a )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setLabelColor( r, g, b, a );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setLabelLineColor( Object trackId, float[] color )
    {
        setLabelLineColor( trackId, color[0], color[1], color[2], color[3] );
    }

    public void setLabelLineColor( Object trackId, float r, float g, float b, float a )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setLabelLineColor( r, g, b, a );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setShowLabelLine( Object trackId, boolean show )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setShowLabelLine( show );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setLabel( Object trackId, String label )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setShowLabel( true );
            track.setLabel( label );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void setShowLabel( Object trackId, boolean show )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setShowLabel( show );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void displayTimeRange( Object trackId, double startTime, double endTime )
    {
        displayTimeRange( trackId, ( long ) Math.ceil( startTime ), ( long ) Math.floor( endTime ) );
    }

    public void displayTimeRange( double startTime, double endTime )
    {
        displayTimeRange( ( long ) Math.ceil( startTime ), ( long ) Math.floor( endTime ) );
    }

    public void displayTimeRange( Object trackId, long startTime, long endTime )
    {
        displayTimeRange( trackId, startTime, endTime, endTime );
    }

    public void displayTimeRange( Object trackId, long startTime, long endTime, long selectedTime )
    {
        Point startPoint = getStartPoint( startTime );
        Point endPoint = getEndPoint( endTime );
        Point selectedPoint = getEndPoint( selectedTime );

        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.setTimeRange( startPoint, endPoint, selectedPoint );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    public void displayTimeRange( long startTime, long endTime )
    {
        displayTimeRange( startTime, endTime, endTime );
    }

    public void displayTimeRange( long startTime, long endTime, long selectedTime )
    {
        startTimeRange = getStartPoint( startTime );
        endTimeRange = getEndPoint( endTime );
        selectedTimeRange = getEndPoint( selectedTime );

        this.trackUpdateLock.lock( );
        try
        {
            for ( Track track : tracks.values( ) )
            {
                track.setTimeRange( startTimeRange, endTimeRange, selectedTimeRange );
            }

            this.updatedTracks.addAll( tracks.values( ) );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    /**
     * Returns all Points within the given bounding box in axis coordinates (regardless
     * of time stamp).
     *
     * @param minX left edge of the bounding box
     * @param maxX right edge of the bounding box
     * @param minY bottom edge of the bounding box
     * @param maxY top edge of the bounding box
     * @return all Points within the bounding box
     */
    public Collection<Point> getGeoRange( double minX, double maxX, double minY, double maxY )
    {
        if ( spatialIndex != null )
        {
            this.trackUpdateLock.lock( );
            try
            {
                return spatialIndex.search( ( float ) minX, ( float ) maxX, ( float ) minY, ( float ) maxY );
            }
            finally
            {
                this.trackUpdateLock.unlock( );
            }
        }
        else
        {
            return Collections.emptyList( );
        }
    }

    /**
     * @see #getPixelRange(Axis2D, double, double, int, int)
     */
    public Collection<Point> getTimePixelRange( Axis2D axis, double minTime, double maxTime, double centerX, double centerY, int pixelWidth, int pixelHeight )
    {
        return filter( getPixelRange( axis, centerX, centerY, pixelWidth, pixelHeight ) );
    }

    /**
     * <p>Returns all the Points within the bounding box specified with a center in axis coordinates
     * and width/height specified in pixels.</p>
     *
     * <p>This is useful for querying for points near the cursor. When used in this way, the pixelWidth
     * and pixelHeight arguments control how close the user must get to a point before it is selected.
     * The Axis2D argument is usually obtained from a GlimpseMouseListener.</p>
     *
     * @param axis the axis to use to convert pixel values into axis coordinates.
     * @param centerX the x center of the query box in axis coordinates
     * @param centerY the y center of the query box in axis coordinates
     * @param pixelWidth the width of the query box in pixels
     * @param pixelHeight the height of the query box in pixels
     * @return all the Points within the specified bounding box
     */
    public Collection<Point> getPixelRange( Axis2D axis, double centerX, double centerY, int pixelWidth, int pixelHeight )
    {
        double width = pixelWidth / axis.getAxisX( ).getPixelsPerValue( );
        double height = pixelHeight / axis.getAxisY( ).getPixelsPerValue( );

        return getGeoRange( centerX - width / 2, centerX + width / 2, centerY - height / 2, centerY + height / 2 );
    }

    /**
     * Returns the closest point to the cursor position. Distance is measured in pixels, not
     * in axis units. However, the cursor position is specified in axis coordinates. If the closest
     * point is further away than maxPixelDistance then null is returned.
     *
     * @param axis the axis to use to convert pixel values into axis coordinates
     * @param centerX the x center of the query box in axis coordinates
     * @param centerY the y center of the query box in axis coordinates
     * @param maxPixelDistance only search for nearby points within this pixel radius
     * @return the closest point to the given axis coordinates, or null if none exists
     */
    public Point getNearestPoint( Axis2D axis, double centerX, double centerY, int maxPixelDistance )
    {
        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        int centerPixelX = axisX.valueToScreenPixel( centerX );
        int centerPixelY = axisY.getSizePixels( ) - axisY.valueToScreenPixel( centerY );

        return getNearestPoint( axis, centerPixelX, centerPixelY, maxPixelDistance );
    }

    /**
     * Returns the closest Point to the mouse position specified in the given GlimpseMouseEvent.
     * If the closest point is farther away than maxPixelDistance pixels, then null is returned.
     *
     * @param mouseEvent event containing a mouse position
     * @param maxPixelDistance the farthest point allowed
     * @return the closest point to the provided mouse position
     */
    public Point getNearestPoint( GlimpseMouseEvent mouseEvent, int maxPixelDistance )
    {
        Axis2D axis = mouseEvent.getAxis2D( );
        int centerPixelX = mouseEvent.getX( );
        int centerPixelY = mouseEvent.getY( );

        return getNearestPoint( axis, centerPixelX, centerPixelY, maxPixelDistance );
    }

    /**
     * Returns the closest point to the cursor position. Distance is measured in pixels, not
     * in axis units. The cursor position is specified in pixel/screen coordinates.
     * If the closest point is further away than maxPixelDistance then null is returned.
     *
     * @param axis the axis to use to convert pixel values into axis coordinates
     * @param centerPixelX the x center of the query box in pixel coordinates
     * @param centerPixelY the y center of the query box in pixel coordinates
     * @param maxPixelDistance only search for nearby points within this pixel radius
     * @return the closest point to the given axis coordinates, or null if none exists
     */
    public Point getNearestPoint( Axis2D axis, int centerPixelX, int centerPixelY, int maxPixelDistance )
    {
        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        double centerX = axisX.screenPixelToValue( centerPixelX );
        double centerY = axisY.screenPixelToValue( axisY.getSizePixels( ) - centerPixelY );

        Collection<Point> points = getTimePixelRange( axis, startTimeRange.time, endTimeRange.time, centerX, centerY, maxPixelDistance * 2, maxPixelDistance * 2 );

        Point minPoint = null;
        double minDistance = 0;

        for ( Point point : points )
        {
            double pixelX = axisX.valueToScreenPixel( point.x );
            double pixelY = axisY.getSizePixels( ) - axisY.valueToScreenPixel( point.y );

            double diffX = pixelX - centerPixelX;
            double diffY = pixelY - centerPixelY;

            double dist = Math.sqrt( diffX * diffX + diffY * diffY );

            if ( minPoint == null || dist < minDistance )
            {
                minPoint = point;
                minDistance = dist;
            }
        }

        return minPoint;
    }

    public Collection<Point> getTimeGeoRange( double minTime, double maxTime, double minX, double maxX, double minY, double maxY )
    {
        return getTimeGeoRange( ( long ) Math.ceil( minTime ), ( long ) Math.floor( maxTime ), minX, maxX, minY, maxY );
    }

    /**
     * @return all the points within a specified bounding box which fall between the specified times.
     */
    public Collection<Point> getTimeGeoRange( long minTime, long maxTime, double minX, double maxX, double minY, double maxY )
    {
        if ( spatialIndex != null )
        {
            this.trackUpdateLock.lock( );
            try
            {
                return filter( spatialIndex.search( ( float ) minX, ( float ) maxX, ( float ) minY, ( float ) maxY ), minTime, maxTime );
            }
            finally
            {
                this.trackUpdateLock.unlock( );
            }
        }
        else
        {
            return Collections.emptyList( );
        }
    }

    /**
     * @return all the points within a specified bounding box which fall between the time
     *         span specified for their track using displayTimeRange.
     */
    public Collection<Point> getTimeGeoRange( double minX, double maxX, double minY, double maxY )
    {
        if ( spatialIndex != null )
        {
            this.trackUpdateLock.lock( );
            try
            {
                return filter( spatialIndex.search( ( float ) minX, ( float ) maxX, ( float ) minY, ( float ) maxY ) );
            }
            finally
            {
                this.trackUpdateLock.unlock( );
            }
        }
        else
        {
            return Collections.emptyList( );
        }
    }

    /**
     * Reclaims direct host memory used to move track vertices between the host and device.
     * By default, this memory is never reclaimed because it is slow to allocate. However,
     * if the vertex data will not change in the near future, it may be beneficial to call
     * this method to free up memory in the meantime.
     */
    public void gcDataBuffer( )
    {
        this.trackUpdateLock.lock( );
        try
        {
            this.dataBuffer = null;
            this.dataBufferSize = 0;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    protected Point getStartPoint( long time )
    {
        return new Point( Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0, time );
    }

    protected Point getEndPoint( long time )
    {
        return new Point( Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0, time );
    }

    protected Collection<Point> filter( Collection<Point> points )
    {
        Collection<Point> result = new ArrayList<Point>( );

        Iterator<Point> iter = points.iterator( );
        while ( iter.hasNext( ) )
        {
            Point point = iter.next( );
            Track track = tracks.get( point.getTrackId( ) );

            if ( track == null )
            {
                continue;
            }

            if ( comparator.compare( point, track.selectionStart ) >= 0 && comparator.compare( point, track.selectionEnd ) < 0 )
            {
                result.add( point );
            }
        }

        return result;
    }

    protected Collection<Point> filter( Collection<Point> points, long minTime, long maxTime )
    {
        Collection<Point> result = new ArrayList<Point>( );

        Iterator<Point> iter = points.iterator( );
        while ( iter.hasNext( ) )
        {
            Point point = iter.next( );

            if ( point.getTime( ) <= minTime || point.getTime( ) > maxTime ) continue;

            result.add( point );
        }

        return result;
    }

    protected void addPoint( Object trackId, Point point )
    {
        this.trackUpdateLock.lock( );
        try
        {
            Track track = getOrCreateTrack( trackId );

            track.add( point );

            this.updatedTracks.add( track );
            this.newData = true;
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }
    }

    // must be called while holding trackUpdateLock
    protected Track getOrCreateTrack( Object trackId )
    {
        Track track = this.tracks.get( trackId );

        if ( track == null )
        {
            track = new Track( trackId );
            track.setTimeRange( startTimeRange, endTimeRange, selectedTimeRange );
            this.tracks.put( trackId, track );
        }

        return track;
    }

    protected void ensureDataBufferSize( int needed )
    {
        if ( dataBuffer == null || dataBufferSize < needed )
        {
            dataBufferSize = needed;
            dataBuffer = ByteBuffer.allocateDirect( needed * 2 * 4 ).order( ByteOrder.nativeOrder( ) ).asFloatBuffer( );
        }
    }

    protected void notifyTemporalSelectionListeners( Map<Object, Point> newTrackHeads )
    {
        for ( TemporalSelectionListener<Point> listener : temporalSelectionListeners )
        {
            listener.selectionChanged( newTrackHeads );
        }
    }

    protected LoadedTrack getOrCreateLoadedTrack( Object id, Track track )
    {
        LoadedTrack loaded = loadedTracks.get( id );
        if ( loaded == null )
        {
            loaded = new LoadedTrack( track );
            loadedTracks.put( id, loaded );
        }

        return loaded;
    }

    @Override
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( this.newData )
        {
            this.trackUpdateLock.lock( );
            try
            {
                // loop through all tracks with new posits
                for ( Track track : updatedTracks )
                {
                    Object id = track.trackId;

                    if ( track.isDeletePending( ) || track.isClearPending( ) )
                    {
                        LoadedTrack loaded = getOrCreateLoadedTrack( id, track );
                        loaded.dispose( gl );
                        loadedTracks.remove( id );

                        // If the track was deleted then recreated in between calls to display0(),
                        // (both isDataInserted() and isDeletePending() are true) then don't remove the track
                        if ( track.isDeletePending( ) && !track.isDataInserted( ) )
                        {
                            tracks.remove( id );
                            continue;
                        }
                    }

                    LoadedTrack loaded = getOrCreateLoadedTrack( id, track );
                    loaded.loadSettings( track );

                    int trackSize = track.getSize( );

                    if ( track.isDataInserted( ) )
                    {
                        if ( !loaded.glBufferInitialized || loaded.glBufferMaxSize < trackSize )
                        {
                            // if the track doesn't have a gl buffer or it is too small we must
                            // copy all the track's data into a new, larger buffer

                            // if this is the first time we have allocated memory for this track
                            // don't allocate any extra, it may never get added to
                            // however, once a track has been updated once, we assume it is likely
                            // to be updated again and give it extra memory
                            if ( loaded.glBufferInitialized )
                            {
                                gl.glDeleteBuffers( 1, new int[] { loaded.glBufferHandle }, 0 );
                                loaded.glBufferMaxSize = Math.max( ( int ) ( loaded.glBufferMaxSize * 1.5 ), trackSize );
                            }
                            else
                            {
                                loaded.glBufferMaxSize = trackSize;
                            }

                            // copy all the track data into a host buffer
                            ensureDataBufferSize( loaded.glBufferMaxSize );
                            dataBuffer.rewind( );
                            track.loadIntoBuffer( dataBuffer, 0, trackSize );

                            // create a new device buffer handle
                            int[] bufferHandle = new int[1];
                            gl.glGenBuffers( 1, bufferHandle, 0 );
                            loaded.glBufferHandle = bufferHandle[0];

                            loaded.glBufferInitialized = true;

                            // copy data from the host buffer into the device buffer
                            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, loaded.glBufferHandle );
                            gl.glBufferData( GL2.GL_ARRAY_BUFFER, loaded.glBufferMaxSize * 2 * BYTES_PER_FLOAT, dataBuffer.rewind( ), GL2.GL_DYNAMIC_DRAW );
                        }
                        else
                        {
                            // there is enough empty space in the device buffer to accommodate all the new data

                            int insertOffset = track.getInsertOffset( );
                            int insertCount = track.getInsertCount( );

                            // copy all the new track data into a host buffer
                            ensureDataBufferSize( insertCount );
                            dataBuffer.rewind( );
                            track.loadIntoBuffer( dataBuffer, insertOffset, trackSize );

                            // update the device buffer with the new data
                            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, loaded.glBufferHandle );
                            gl.glBufferSubData( GL2.GL_ARRAY_BUFFER, insertOffset * 2 * BYTES_PER_FLOAT, insertCount * 2 * BYTES_PER_FLOAT, dataBuffer.rewind( ) );
                        }
                    }

                    track.reset( );
                }

                this.updatedTracks.clear( );
                this.newData = false;
            }
            finally
            {
                this.trackUpdateLock.unlock( );
            }

            glHandleError( gl );
        }

        if ( loadedTracks.isEmpty( ) ) return;

        gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );

        boolean labelOn = false;

        for ( LoadedTrack loaded : loadedTracks.values( ) )
        {
            if ( !loaded.glBufferInitialized ) continue;

            int glOffset = loaded.glSelectedOffset;
            int glSize = loaded.glSelectedSize;

            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, loaded.glBufferHandle );
            gl.glVertexPointer( 2, GL2.GL_FLOAT, 0, 0 );

            if ( loaded.linesOn )
            {
                gl.glColor4fv( loaded.lineColor, 0 );
                gl.glLineWidth( loaded.lineWidth );

                if ( loaded.stippleOn )
                {
                    gl.glEnable( GL2.GL_LINE_STIPPLE );
                    gl.glLineStipple( loaded.stippleFactor, loaded.stipplePattern );
                }

                gl.glDrawArrays( GL2.GL_LINE_STRIP, glOffset, glSize );

                if ( loaded.stippleOn )
                {
                    gl.glDisable( GL2.GL_LINE_STIPPLE );
                }
            }

            if ( loaded.pointsOn )
            {
                gl.glColor4fv( loaded.pointColor, 0 );
                gl.glPointSize( loaded.pointSize );
                gl.glDrawArrays( GL2.GL_POINTS, glOffset, glSize );
            }

            if ( loaded.headPointOn )
            {
                gl.glColor4fv( loaded.headPointColor, 0 );
                gl.glPointSize( loaded.headPointSize );
                gl.glBegin( GL2.GL_POINTS );
                try
                {
                    gl.glVertex2d( loaded.headPosX, loaded.headPosY );
                }
                finally
                {
                    gl.glEnd( );
                }
            }

            if ( loaded.labelOn ) labelOn = true;
        }

        // don't bother iterating through all the tracks again if none have labels turned on
        if ( labelOn && fontRenderer != null )
        {
            fontRenderer.beginRendering( width, height );
            try
            {
                for ( LoadedTrack loaded : loadedTracks.values( ) )
                {
                    if ( loaded.labelOn && loaded.label != null )
                    {
                        int posX = axis.getAxisX( ).valueToScreenPixel( loaded.headPosX );
                        int posY = axis.getAxisY( ).valueToScreenPixel( loaded.headPosY );
                        fontRenderer.setColor( loaded.labelColor );
                        fontRenderer.draw( loaded.label, posX + TRACK_LABEL_OFFSET_X, posY + TRACK_LABEL_OFFSET_Y );
                    }
                }
            }
            finally
            {
                fontRenderer.endRendering( );
            }

            gl.glMatrixMode( GL2.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( 0, width, 0, height, -1, 1 );
            gl.glMatrixMode( GL2.GL_MODELVIEW );
            gl.glLoadIdentity( );

            for ( LoadedTrack loaded : loadedTracks.values( ) )
            {
                if ( loaded.labelOn && loaded.labelLineOn && loaded.label != null )
                {
                    int posX = axis.getAxisX( ).valueToScreenPixel( loaded.headPosX );
                    int posY = axis.getAxisY( ).valueToScreenPixel( loaded.headPosY );

                    gl.glColor3fv( loaded.labelLineColor, 0 );
                    gl.glBegin( GL2.GL_LINES );
                    try
                    {
                        gl.glVertex2i( posX, posY );
                        gl.glVertex2i( posX + TRACK_LABEL_OFFSET_X, posY + TRACK_LABEL_OFFSET_Y );
                    }
                    finally
                    {
                        gl.glEnd( );
                    }
                }
            }
        }
    }

    @Override
    public void dispose( GLContext context )
    {
        GL gl = context.getGL( );

        this.trackUpdateLock.lock( );
        try
        {
            for ( LoadedTrack track : loadedTracks.values( ) )
            {
                track.dispose( gl );
            }
        }
        finally
        {
            this.trackUpdateLock.unlock( );
        }

        if ( fontRenderer != null )
        {
            fontRenderer.dispose( );
            fontRenderer = null;
        }
    }

    ////////////////////////////////////////
    ///// Internal Data Structures     /////
    ///// not intended for use outside /////
    ///// of TrackPainter              /////
    ////////////////////////////////////////

    // A Track modified only on the gl display() thread
    // (so no locking is required when calling its methods
    // and accessing its data)
    protected static class LoadedTrack
    {
        // the unique identifier of the track
        public Object trackId;

        // track display attributes
        public float[] lineColor = new float[4];
        public float lineWidth;
        public boolean linesOn;

        public float[] pointColor = new float[4];
        public float pointSize;
        public boolean pointsOn;

        public int stippleFactor;
        public short stipplePattern;
        public boolean stippleOn;

        public String label;
        public boolean labelOn;
        public double headPosX;
        public double headPosY;
        public Color labelColor;

        public float[] labelLineColor = new float[4];
        public boolean labelLineOn;

        public float headPointSize;
        public float[] headPointColor = new float[4];
        public boolean headPointOn;

        public boolean glBufferInitialized = false;
        // a reference to the device buffer for this track
        public int glBufferHandle;
        // the maximum allocated size of the device buffer for this track
        public int glBufferMaxSize;
        // the currently used size of the device buffer for this track
        public int glBufferCurrentSize;

        // the offset into the device buffer to begin displaying track vertices
        public int glSelectedOffset;
        // the number of bytes from the device buffer to display
        public int glSelectedSize;

        // LoadedTrack isn't intended to be used outside of TrackPainter
        protected LoadedTrack( Track track )
        {
            this.trackId = track.trackId;
            this.loadSettings( track );
        }

        public void loadSettings( Track track )
        {

            this.glSelectedSize = track.selectedSize;
            this.glSelectedOffset = track.selectedOffset;

            this.copyColor( this.lineColor, track.lineColor );
            this.copyColor( this.pointColor, track.pointColor );

            this.lineWidth = track.lineWidth;
            this.pointSize = track.pointSize;

            this.pointsOn = track.pointsOn;
            this.linesOn = track.linesOn;
            this.stippleOn = track.stippleOn;

            this.stippleFactor = track.stippleFactor;
            this.stipplePattern = track.stipplePattern;

            this.glBufferCurrentSize = track.getSize( );

            if ( glBufferCurrentSize == 0 || track.selectedSize == 0 || track.trackHead == null )
            {
                this.headPointOn = false;
                this.labelOn = false;
            }
            else
            {
                this.copyColor( this.labelLineColor, track.labelLineColor );
                this.labelLineOn = track.labelLineOn;

                this.copyColor( this.headPointColor, track.headPointColor );
                this.headPointSize = track.headPointSize;
                this.headPointOn = track.headPointOn;

                this.label = track.label;
                this.labelOn = track.labelOn;
                this.headPosX = track.headPosX;
                this.headPosY = track.headPosY;
                this.labelColor = track.labelColor;
            }
        }

        protected void copyColor( float[] to, float[] from )
        {
            to[0] = from[0];
            to[1] = from[1];
            to[2] = from[2];
            to[3] = from[3];
        }

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( trackId == null ) ? 0 : trackId.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            LoadedTrack other = ( LoadedTrack ) obj;
            if ( trackId == null )
            {
                if ( other.trackId != null ) return false;
            }
            else if ( !trackId.equals( other.trackId ) ) return false;
            return true;
        }

        public void dispose( GL gl )
        {
            if ( glBufferInitialized )
            {
                glBufferInitialized = false;
                gl.glDeleteBuffers( 1, new int[] { glBufferHandle }, 0 );
            }
        }
    }

    // A Track modified in the gl display() thread as well as
    // by the user, all methods should be called while holding
    // trackUpdateLock
    protected class Track
    {
        // the unique identifier of the track
        protected Object trackId;
        // the points making up the track
        protected List<Point> points;
        // the lowest index of the last change made to the track
        // when the track data is copied to a device buffer, all
        // data from here to the end of the track must be copied
        protected int insertIndex;
        // if true, insert index is valid
        protected boolean dataInserted = false;
        // if true, this track is waiting to be deleted
        protected boolean deletePending = false;
        // if true, this track is waiting to be cleared
        protected boolean clearPending = false;

        // the offset into the points list of the first point to display
        protected int selectedOffset;
        // the number of points in the points list to display
        protected int selectedSize;

        protected Point selectionStart;
        protected Point selectionEnd;
        protected Point selectionCurrent;

        protected Point trackHead;

        // track display attributes
        protected float[] lineColor = new float[] { 1.0f, 1.0f, 0.0f, 1.0f };
        protected float lineWidth = 2;
        protected boolean linesOn = true;
        protected float[] pointColor = new float[] { 1.0f, 0.0f, 0.0f, 1.0f };
        protected float pointSize = 4;
        protected boolean pointsOn = true;
        protected int stippleFactor = 1;
        protected short stipplePattern = ( short ) 0x00FF;
        protected boolean stippleOn = false;

        protected String label = null;
        protected boolean labelOn = false;
        protected double headPosX;
        protected double headPosY;
        protected Color labelColor = new Color( 1.0f, 1.0f, 0.0f, 1.0f );

        protected boolean labelLineOn = true;
        protected float[] labelLineColor = new float[] { 1.0f, 1.0f, 0.0f, 1.0f };

        protected float headPointSize = 7;
        protected float[] headPointColor = new float[] { 1.0f, 0.0f, 0.0f, 1.0f };
        protected boolean headPointOn = false;

        // Track isn't intended to be used outside of TrackPainter
        protected Track( Object trackId )
        {
            this.trackId = trackId;
            this.points = new ArrayList<Point>( TRACK_SIZE_ESTIMATE );
        }

        public void setTimeRange( Point startPoint, Point endPoint, Point selectedPoint )
        {
            selectionStart = startPoint;
            selectionEnd = endPoint;
            selectionCurrent = selectedPoint;

            checkTimeRange( );
        }

        public void checkTimeRange( )
        {
            if ( selectionStart == null || selectionEnd == null || selectionCurrent == null ) return;

            int startIndex = firstIndexBeforeTime( selectionStart ) + 1;
            int endIndex = firstIndexAfterTime( selectionEnd ) - 1;
            int selectedIndex = firstIndexAfterTime( selectionCurrent ) - 1;

            Point previousTrackHead = trackHead;

            if ( endIndex < startIndex )
            {
                selectedOffset = 0;
                selectedSize = 0;

                trackHead = null;

                if ( previousTrackHead != null ) notifyTemporalSelectionListeners( Collections.singletonMap( trackId, trackHead ) );
            }
            else
            {
                selectedOffset = startIndex;
                selectedSize = endIndex - startIndex + 1;

                if ( selectedIndex > endIndex ) selectedIndex = endIndex;
                if ( selectedIndex < startIndex ) selectedIndex = startIndex;

                trackHead = points.get( selectedIndex );
                headPosX = trackHead.getX( );
                headPosY = trackHead.getY( );

                if ( !trackHead.equals( previousTrackHead ) ) notifyTemporalSelectionListeners( Collections.singletonMap( trackId, trackHead ) );
            }
        }

        public void setHeadPointColor( float r, float g, float b, float a )
        {
            headPointColor[0] = r;
            headPointColor[1] = g;
            headPointColor[2] = b;
            headPointColor[3] = a;
        }

        public void setHeadPointSize( float size )
        {
            headPointSize = size;
        }

        public void setShowHeadPoint( boolean show )
        {
            headPointOn = show;
        }

        public void setPointColor( float r, float g, float b, float a )
        {
            pointColor[0] = r;
            pointColor[1] = g;
            pointColor[2] = b;
            pointColor[3] = a;
        }

        public void setPointSize( float size )
        {
            pointSize = size;
        }

        public void setShowPoints( boolean show )
        {
            pointsOn = show;
        }

        public void setLineColor( float r, float g, float b, float a )
        {
            lineColor[0] = r;
            lineColor[1] = g;
            lineColor[2] = b;
            lineColor[3] = a;
        }

        public void setLineWidth( float width )
        {
            lineWidth = width;
        }

        public void setShowLines( boolean show )
        {
            linesOn = show;
        }

        public void setTrackStipple( boolean activate )
        {
            this.stippleOn = activate;
        }

        public void setTrackStipple( int stippleFactor, short stipplePattern )
        {
            this.stippleFactor = stippleFactor;
            this.stipplePattern = stipplePattern;
        }

        public void setLabelColor( float r, float g, float b, float a )
        {
            labelColor = new Color( r, g, b, a );
        }

        public void setLabelLineColor( float r, float g, float b, float a )
        {
            labelLineColor[0] = r;
            labelLineColor[1] = g;
            labelLineColor[2] = b;
            labelLineColor[3] = a;
        }

        public void setShowLabelLine( boolean show )
        {
            labelLineOn = show;
        }

        public void setLabel( String label )
        {
            this.label = label;
        }

        public void setShowLabel( boolean show )
        {
            this.labelOn = show;
        }

        public void add( List<Point> _points )
        {
            if ( _points == null || _points.size( ) == 0 ) return;

            List<Point> sortedPoints = new ArrayList<Point>( _points );
            Collections.sort( sortedPoints, comparator );
            Point firstPoint = sortedPoints.get( 0 );

            // add the point to the temporal and spatial indexes
            int index = firstIndexAfterTime( firstPoint );

            if ( index == points.size( ) )
            {
                points.addAll( index, sortedPoints );
            }
            else
            {
                for ( Point point : sortedPoints )
                {
                    index = firstIndexAfterTime( firstPoint );
                    points.add( index, point );
                }
            }

            if ( spatialIndex != null )
            {
                for ( Point point : _points )
                    spatialIndex.add( point );
            }

            // determine if the new point resides inside the selected time range
            checkTimeRange( );

            // set flag indicating this track contains new data
            insertIndex = 0;
            dataInserted = true;
        }

        public void add( Point point )
        {
            // add the point to the temporal and spatial indexes
            int index = firstIndexAfterTime( point );
            points.add( index, point );
            if ( spatialIndex != null ) spatialIndex.add( point );

            // determine if the new point resides inside the selected time range
            checkTimeRange( );

            // set flag indicating this track contains new data
            if ( !dataInserted || index < insertIndex )
            {
                insertIndex = index;
                dataInserted = true;
            }
        }

        public void delete( )
        {
            deletePending = true;
            clear( );
        }

        public void clear( )
        {
            clearPending = true;

            dataInserted = false;
            trackHead = null;
            points.clear( );

            checkTimeRange( );
        }

        public int firstIndexAfterTime( Point point )
        {
            int index = Collections.binarySearch( points, point, comparator );
            if ( index < 0 ) index = - ( index + 1 );

            while ( index < points.size( ) && points.get( index ).time == point.time )
            {
                index++;
            }

            return index;
        }

        public int firstIndexBeforeTime( Point point )
        {
            int index = Collections.binarySearch( points, point, comparator );
            if ( index < 0 ) index = - ( index + 1 ) - 1;

            while ( 0 <= index && points.get( index ).time == point.time )
            {
                index--;
            }

            return index;
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

        public int getInsertCount( )
        {
            return getSize( ) - getInsertOffset( );
        }

        public int getInsertOffset( )
        {
            return insertIndex;
        }

        public void reset( )
        {
            dataInserted = false;
            clearPending = false;
            deletePending = false;
        }

        public int getSize( )
        {
            return points.size( );
        }

        public Point getTrackHead( )
        {
            return trackHead;
        }

        public void loadIntoBuffer( FloatBuffer buffer, int offset, int size )
        {
            for ( int i = offset; i < size; i++ )
            {
                points.get( i ).loadIntoBuffer( buffer );
            }
        }

        public Object getTrackId( )
        {
            return trackId;
        }

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType( ).hashCode( );
            result = prime * result + ( ( trackId == null ) ? 0 : trackId.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            Track other = ( Track ) obj;
            if ( !getOuterType( ).equals( other.getOuterType( ) ) ) return false;
            if ( trackId == null )
            {
                if ( other.trackId != null ) return false;
            }
            else if ( !trackId.equals( other.trackId ) ) return false;
            return true;
        }

        private TrackPainter getOuterType( )
        {
            return TrackPainter.this;
        }
    }
}
