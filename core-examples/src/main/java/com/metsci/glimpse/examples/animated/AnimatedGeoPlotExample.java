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
package com.metsci.glimpse.examples.animated;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.RateLimitedAxisListener1D;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.track.Point;
import com.metsci.glimpse.painter.track.TrackPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.selection.SpatialSelectionListener;

/**
 * Demonstrates the dynamic update capability of the TrackPainter.
 *
 * @author ulman
 */
public class AnimatedGeoPlotExample implements GlimpseLayoutProvider
{
    public static void main( String args[] ) throws Exception
    {
        Example.showWithSwing( new AnimatedGeoPlotExample( ) );
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
        final TrackPainter trackPainter = new TrackPainter( true );
        plot.addPainter( trackPainter );

        // add a custom manager class to keep track of the tracks
        TrackManager trackManager = new TrackManager( trackPainter, NUMBER_OF_TRACKS );
        
        // add a custom listener to the z axis which changes the selected time range for
        // all GeoPlot tracks based on the min and max values of the z axis
        plot.getAxisZ( ).addAxisListener( new TimeAxisListener( trackPainter ) );

        // add a custom listener which is notified when the track points inside the plot's selection box change
        trackPainter.addSpatialSelectionListener( plot.getAxis( ), new TrackSelectionListener( trackManager, trackPainter ) );
        
        // start a thread which manages the animation, continually adding new points to the tracks
        trackManager.start( );

        return plot;
    }
    
    // a custom listener which changes the selected time range for
    // all GeoPlot tracks based on the min and max values of the z axis
    private static class TimeAxisListener extends RateLimitedAxisListener1D
    {
        private long prevMinTime = -1;
        private long prevMaxTime = -1;
        private TrackPainter trackPainter;
        
        public TimeAxisListener( TrackPainter trackPainter )
        {
            this.trackPainter = trackPainter;
        }
        
        @Override
        public void axisUpdatedRateLimited( Axis1D handler )
        {
            long minTime = ( long ) handler.getMin( );
            long maxTime = ( long ) handler.getMax( );

            if ( prevMinTime != minTime || prevMaxTime != maxTime )
            {
                trackPainter.displayTimeRange( minTime, maxTime );

                prevMinTime = minTime;
                prevMaxTime = maxTime;
            }
        }
    }
    
    // a custom listener which is notified when the track points inside the plot's selection box change
    private static class TrackSelectionListener implements SpatialSelectionListener<Point>
    {
        private IntAVLTreeSet selectedTrackIds;
        private IntAVLTreeSet newSelectedTrackIds;
        private TrackPainter trackPainter;
        private TrackManager trackManager;
        
        public TrackSelectionListener( TrackManager trackManager, TrackPainter trackPainter )
        {
            this.selectedTrackIds = new IntAVLTreeSet( );
            this.newSelectedTrackIds = new IntAVLTreeSet( );
            
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
            IntBidirectionalIterator iter = newSelectedTrackIds.iterator( );
            while( iter.hasNext( ) )
            {
                int trackId = iter.nextInt( );
                
                trackPainter.setPointColor( trackId, 0.0f, 1.0f, 0.0f, 1.0f );
                trackPainter.setLineColor( trackId, 0.0f, 1.0f, 0.0f, 1.0f );
                trackPainter.setShowLabel( trackId, true );
                trackPainter.setHeadPointSize( trackId, 8.0f );
            }
            
            // change back to normal the display characteristics of any tracks
            // which were previously selected, but have become unselected
            iter = selectedTrackIds.iterator( );
            while( iter.hasNext( ) )
            {
                int trackId = iter.nextInt( );
                
                if ( newSelectedTrackIds.contains( trackId ) ) continue;
                
                Track track = trackManager.getTrack( trackId );
                
                if ( track != null ) track.setColor( trackPainter );
                trackPainter.setShowLabel( trackId, false );
                trackPainter.setHeadPointSize( trackId, 4.0f );
            }
            
            // swap the sets storing previously selected and newly selected tracks
            IntAVLTreeSet temp = selectedTrackIds;
            selectedTrackIds = newSelectedTrackIds;
            newSelectedTrackIds = temp;
        }
    }

    // a manager class which handles periodically adding points each Track
    private static class TrackManager extends Thread
    {
        private int time = 0;
        private Map<Integer, Track> tracks;
        private TrackPainter trackPainter;
        private int numberOfTracks;

        public TrackManager( TrackPainter trackPainter, int numberOfTracks )
        {
            this.trackPainter = trackPainter;
            this.numberOfTracks = numberOfTracks;
            this.tracks = Collections.synchronizedMap( new HashMap<Integer, Track>( numberOfTracks ) );
        }

        @Override
        public void run( )
        {

            for ( int i = 0; i < numberOfTracks; i++ )
            {
                // add some randomness to the track color
                float r = ( float ) ( Math.random( ) * 0.2 + 0.7 );
                float g = ( float ) ( Math.random( ) * 0.2 + 0.3 );
                float b = ( float ) ( Math.random( ) * 0.2 + 0.3 );

                Track track = new Track( i, r, g, b );

                tracks.put( i, track );

                track.setColor( trackPainter );

                trackPainter.setPointSize( i, 0.5f );
                trackPainter.setLineWidth( i, 2f );

                trackPainter.setLabel( i, "Track " + i );
                trackPainter.setLabelColor( i, GlimpseColor.getBlack( ) );
                trackPainter.setShowLabel( i, false );
                trackPainter.setShowLabelLine( i, false );
                
                trackPainter.setHeadPointSize( i, 4.0f );
                trackPainter.setShowHeadPoint( i, true );
            }

            while ( true )
            {
                Runnable r = new Runnable( )
                {
                    public void run( )
                    {
                        time = time + 1;

                        for ( Track track : tracks.values( ) )
                        {
                            track.tick( );
                            track.addPoint( trackPainter, time );
                        }
                    }
                };
                r.run( );

                try
                {
                    Thread.sleep( 50 );
                }
                catch ( InterruptedException e )
                {
                }
            }
        }

        public Track getTrack( int id )
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
