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
package com.metsci.glimpse.examples.track;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.RateLimitedAxisListener1D;
import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseMotionListener;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.info.CursorTextPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.painter.track.Point;
import com.metsci.glimpse.painter.track.Pulsator;
import com.metsci.glimpse.painter.track.TrackPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.selection.SpatialSelectionListener;
import com.metsci.glimpse.support.shader.line.LineStyle;

/**
 * Demonstrates the dynamic update capability of the TrackPainter.
 *
 * @author ulman
 */
public class TrackPainterExample implements GlimpseLayoutProvider
{
    public static void main( String args[] ) throws Exception
    {
        Example.showWithSwing( new TrackPainterExample( ) );
    }

    public static final int NUMBER_OF_TRACKS = 2000;

    @Override
    public GlimpseLayout getLayout( )
    {
        // create a premade geoplot
        final SimplePlot2D plot = new SimplePlot2D( );

        // show the z axis and set its width to 50 pixels
        plot.setAxisSizeZ( 50 );

        // hide the x and y axes and the plot title
        plot.setAxisSizeX( 0 );
        plot.setAxisSizeY( 0 );
        plot.setTitleHeight( 0 );

        // set axis labels
        plot.setAxisLabelZ( "time", "hours", false );

        // set the x, y, and z initial axis bounds
        plot.setMinX( -20.0 );
        plot.setMaxX( 20.0 );

        plot.setMinY( -20.0 );
        plot.setMaxY( 20.0 );

        plot.setMinZ( 0.0 );
        plot.setMaxZ( 1000.0 );
        plot.setAxisSizeZ( 65 );
        plot.getAxisZ( ).setSelectionCenter( 1000.0 );

        plot.getAxisX( ).setSelectionCenter( 10 );
        plot.getAxisY( ).setSelectionCenter( 10 );

        // lock the aspect ratio of the x and y axis to 1 to 1
        plot.lockAspectRatioXY( 1.0 );

        // set the size of the selection box to 50000.0 units
        plot.setSelectionSize( 5.0 );

        // show minor tick marks on all the plot axes
        plot.setShowMinorTicksX( true );
        plot.setShowMinorTicksY( true );
        plot.setShowMinorTicksZ( true );

        // add a painter to manage and draw track data
        //final LineStripPainter trackPainter = new LineStripPainter( true );
        final TrackPainter trackPainter = new TrackPainter( true );
        plot.addPainter( trackPainter );

        // add a custom manager class to keep track of the tracks
        TrackManager trackManager = new TrackManager( trackPainter, NUMBER_OF_TRACKS );

        // add a custom listener to the z axis which changes the selected time range for
        // all GeoPlot tracks based on the min and max values of the z axis
        plot.getAxisZ( ).addAxisListener( new TimeAxisListener( trackPainter ) );
        plot.getAxisPainterZ( ).setShowMarker( true );

        // start a thread which manages the animation, continually adding new points to the tracks
        trackManager.start( );

        // add a custom listener which is notified when the track points inside the plot's selection box change
        trackPainter.addSpatialSelectionListener( plot.getAxis( ), new TrackSelectionListener( trackManager, trackPainter ) );

        // create a new TrackPainter which will be used to highlight the point closest to the cursor
        final TrackPainter selectionDotPainter = new TrackPainter( false );
        plot.addPainter( selectionDotPainter );
        selectionDotPainter.setShowPoints( 0, true );
        selectionDotPainter.setPointSize( 0, 10f );
        selectionDotPainter.setPointColor( 0, GlimpseColor.getYellow( ) );

        // create a painter to display information about the selected point
        final CustomCursorTextPainter cursorText = new CustomCursorTextPainter( );
        plot.addPainter( cursorText );

        // create a pulsator, a threaded convenience class which pulses
        // the point size of a given set of track ids in a TrackPainter
        final Pulsator pulsator = new Pulsator( selectionDotPainter );
        pulsator.addId( 0 );
        pulsator.start( );

        // add a mouse listener which selects the point closest to the cursor
        plot.addGlimpseMouseMotionListener( new GlimpseMouseMotionListener( )
        {
            @Override
            public void mouseMoved( GlimpseMouseEvent e )
            {
                // find the closest track point to the mouse event which
                // is within 20 pixels of the cursor
                Point point = trackPainter.getNearestPoint( e, 20 );

                // update the cursor text painter for the newly selected point
                cursorText.setPoint( point );

                // use track id 0 to draw a large dot on the selected point
                selectionDotPainter.clearTrack( 0 );
                pulsator.resetSize( );

                if ( point != null )
                {
                    selectionDotPainter.addPoint( 0, 0, point.getX( ), point.getY( ), point.getTime( ) );
                }
            }
        } );

        plot.addPainter( new NumericXYAxisPainter( ) );

        plot.addPainter( new FpsPainter( ) );

        return plot;
    }

    private static class CustomCursorTextPainter extends CursorTextPainter
    {
        protected Point point;

        public void setPoint( Point point )
        {
            this.point = point;

        }

        @Override
        public String getTextX( Axis2D axis )
        {
            Point temp = point;

            if ( temp != null )
            {
                return String.format( "Track: %d Point: %d", temp.getTrackId( ), temp.getPointId( ) );
            }
            else
            {
                return "Id: (none)";
            }
        }

        @Override
        public String getTextY( Axis2D axis )
        {
            Point temp = point;

            if ( temp != null )
            {
                return String.format( "(%.2f, %.2f)", temp.getX( ), temp.getY( ) );
            }
            else
            {
                return "";
            }
        }
    };

    // a custom listener which changes the selected time range for
    // all GeoPlot tracks based on the min and max values of the z axis
    private static class TimeAxisListener extends RateLimitedAxisListener1D
    {
        private long prevMinTime = -1;
        private long prevMaxTime = -1;
        private long prevSelectedTime = -1;
        private TrackPainter trackPainter;

        public TimeAxisListener( TrackPainter trackPainter )
        {
            this.trackPainter = trackPainter;
        }

        @Override
        public void axisUpdatedRateLimited( Axis1D axis )
        {
            long minTime = ( long ) axis.getMin( );
            long maxTime = ( long ) axis.getMax( );
            long selectedTime = ( long ) axis.getSelectionCenter( );

            if ( prevMinTime != minTime || prevMaxTime != maxTime || prevSelectedTime != selectedTime )
            {
                trackPainter.displayTimeRange( minTime, maxTime, selectedTime );

                prevMinTime = minTime;
                prevMaxTime = maxTime;
                prevSelectedTime = selectedTime;
            }
        }
    }

    // a custom listener which is notified when the track points inside the plot's selection box change
    private static class TrackSelectionListener implements SpatialSelectionListener<Point>
    {
        private Set<Object> selectedTrackIds;
        private Set<Object> newSelectedTrackIds;
        private TrackPainter trackPainter;
        private TrackManager trackManager;

        public TrackSelectionListener( TrackManager trackManager, TrackPainter trackPainter )
        {
            this.selectedTrackIds = new HashSet<>( );
            this.newSelectedTrackIds = new HashSet<>( );

            this.trackManager = trackManager;
            this.trackPainter = trackPainter;
        }

        /**
         * Show the track name and change the color of all the selected tracks. A track
         * is selected if at least one of its points falls within the spatial selection
         * defined by the cursor and the time selection defined by the z axis.
         */
        @Override
        public void selectionChanged( Collection<Point> newSelectedPoints )
        {
            // store the track ids of the newly selected track here
            newSelectedTrackIds.clear( );

            // iterate over each selected point, adding its track id
            // to the set of newly selected tracks
            for ( Point p : newSelectedPoints )
            {
                newSelectedTrackIds.add( p.getTrackId( ) );
            }

            // change various display characteristics of the selected tracks
            Iterator<Object> iter = newSelectedTrackIds.iterator( );
            while ( iter.hasNext( ) )
            {
                Object trackId = iter.next( );

                trackPainter.setPointColor( trackId, 0.0f, 1.0f, 0.0f, 1.0f );
                trackPainter.setLineColor( trackId, 0.0f, 1.0f, 0.0f, 1.0f );
                trackPainter.setShowLabel( trackId, true );
                trackPainter.setHeadPointSize( trackId, 14.0f );
            }

            // change back to normal the display characteristics of any tracks
            // which were previously selected, but have become unselected
            iter = selectedTrackIds.iterator( );
            while ( iter.hasNext( ) )
            {
                Object trackId = iter.next( );

                if ( newSelectedTrackIds.contains( trackId ) ) continue;

                Track track = trackManager.getTrack( trackId );

                if ( track != null ) track.setColor( trackPainter );
                trackPainter.setShowLabel( trackId, false );
                trackPainter.setHeadPointSize( trackId, 10.0f );
            }

            // swap the sets storing previously selected and newly selected tracks
            Set<Object> temp = selectedTrackIds;
            selectedTrackIds = newSelectedTrackIds;
            newSelectedTrackIds = temp;
        }
    }

    // a manager class which handles periodically adding points each Track
    private static class TrackManager extends Thread
    {
        private int time = 0;
        private Map<Object, Track> tracks;
        private TrackPainter trackPainter;
        private int numberOfTracks;

        public TrackManager( TrackPainter trackPainter, int numberOfTracks )
        {
            this.trackPainter = trackPainter;
            this.numberOfTracks = numberOfTracks;
            this.tracks = Collections.synchronizedMap( new HashMap<Object, Track>( numberOfTracks ) );
        }

        @Override
        public void run( )
        {
            while ( true )
            {
                time = time + 1;

                if ( time > 1000 ) return;

                if ( tracks.size( ) < numberOfTracks )
                {
                    for ( int j = 0; j < 20; j++ )
                    {
                        int i = tracks.size( );

                        // add some randomness to the track color
                        float r = ( float ) ( Math.random( ) * 0.2 + 0.7 );
                        float g = ( float ) ( Math.random( ) * 0.2 + 0.3 );
                        float b = ( float ) ( Math.random( ) * 0.2 + 0.3 );

                        Track track = new Track( i, r, g, b );
                        tracks.put( i, track );

                        LineStyle style = new LineStyle( );
                        style.thickness_PX = 5f;
                        style.stippleEnable = true;
                        style.stippleScale = 5.0f;
                        trackPainter.setLineStyle( i, style );

                        track.setColor( trackPainter );

                        trackPainter.setShowPoints( i, false );
                        trackPainter.setLabel( i, "Track " + i );
                        trackPainter.setLabelColor( i, GlimpseColor.getBlack( ) );
                        trackPainter.setShowLabel( i, false );
                        trackPainter.setShowLabelLine( i, false );

                        trackPainter.setHeadPointSize( i, ( float ) Math.random( ) * 10.0f );
                        trackPainter.setHeadPointColor( i, new float[] { r, g, b, 1.0f } );
                        trackPainter.setShowHeadPoint( i, true );
                    }
                }

                for ( Track track : tracks.values( ) )
                {
                    track.tick( );
                    track.addPoint( trackPainter, time );
                }

                try
                {
                    Thread.sleep( 50 );
                }
                catch ( InterruptedException e )
                {
                }
            }
        }

        public Track getTrack( Object id )
        {
            return tracks.get( id );
        }
    }

    // a helper class which simply remembers the characteristics of each Track
    private static class Track
    {
        public static double TWO_PI = 2 * Math.PI;
        public static double CHANGE_SCALE = TWO_PI / 20.0;

        private int pointId;
        private int trackId;
        private double y;
        private double x;
        private double direction;
        private double speed;

        private float r, g, b;

        public Track( int _trackId, float r, float g, float b )
        {
            this.trackId = _trackId;
            this.pointId = 0;
            this.y = Math.random( ) * 10;
            this.x = Math.random( ) * 10;
            this.speed = 0.1;
            this.direction = Math.random( ) * TWO_PI;

            this.r = r;
            this.g = g;
            this.b = b;
        }

        public void tick( )
        {
            direction = direction + Math.random( ) * CHANGE_SCALE - CHANGE_SCALE / 2.0;

            x += Math.cos( direction ) * speed;
            y += Math.sin( direction ) * speed;
        }

        public void setColor( TrackPainter trackPainter )
        {
            trackPainter.setLineColor( trackId, r, g, b, 0.6f );
            trackPainter.setPointColor( trackId, r, g, b, 0.6f );
        }

        public void addPoint( TrackPainter trackPainter, long time )
        {
            trackPainter.addPoint( trackId, pointId++, x, y, time );
        }
    }
}
