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
package com.metsci.glimpse.examples.dspl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.parser.table.SliceTableData;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.util.DsplSliceUtils.ConceptPattern;
import com.metsci.glimpse.dspl.util.DsplSliceUtils.SimpleConceptPattern;
import com.metsci.glimpse.dspl.util.DsplSliceUtils.SimpleSlicePattern;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseMotionListener;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.geo.ScalePainter;
import com.metsci.glimpse.painter.track.TrackPainter;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.Pair;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.units.Length;
import com.metsci.glimpse.util.units.time.Time;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.metsci.glimpse.util.vector.Vector2d;

public class DsplTrackExample implements GlimpseLayoutProvider
{
    public static final Logger logger = Logger.getLogger( DsplExample.class.getName( ) );

    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new DsplTrackExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        // create a painter for displaying track data
        final TrackPainter trackPainter = new TrackPainter( );

        // TrackPainter requires tracks have integer ids, so we need to keep a
        // map from the dspl track ids to the integer ids we assign
        Map<String, Integer> trackIdMap = new HashMap<String, Integer>( );

        // create a counter to use to assign track ids
        int nextTrackId = 0;

        // used to calculate the bounds of the data
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        // create a dspl parser
        DsplParser parser = new DsplParser( );

        // tell the parser not to grab canonical concept files from the web
        parser.setNetworkMode( false );

        // tell the parser not to create cached copies of the csv data files it loads
        // if true, loaded dspl files will be converted behind the scenes to an efficient
        // binary file format and will load much faster the second time they are loaded
        parser.setCacheMode( false );

        // load the example dataset
        DataSet dataset = parser.loadDataset( "src/main/resources/dspl/track/metadata.xml" );

        // define patterns which will help us look for particular Concepts in the dataset
        ConceptPattern identifierPattern = new SimpleConceptPattern( "http://www.metsci.com/dspl/track", "identifier" );
        ConceptPattern timePattern = new SimpleConceptPattern( "http://www.metsci.com/dspl/time", "millisecond" );

        List<ConceptPattern> dimensionPatterns = new ArrayList<ConceptPattern>( );
        dimensionPatterns.add( identifierPattern );
        dimensionPatterns.add( timePattern );

        ConceptPattern latPattern = new SimpleConceptPattern( "http://www.metsci.com/dspl/track", "latitude_degrees" );
        ConceptPattern lonPattern = new SimpleConceptPattern( "http://www.metsci.com/dspl/track", "longitude_degrees" );

        List<ConceptPattern> metricPatterns = new ArrayList<ConceptPattern>( );
        metricPatterns.add( latPattern );
        metricPatterns.add( lonPattern );

        SimpleSlicePattern slicePattern = new SimpleSlicePattern( dimensionPatterns, metricPatterns );

        // a projection for converting between planar x/y and lat/lon coordinates
        GeoProjection projection = null;

        // iterate through the Slices found in the DSPL Dataset which contain
        // the data concepts that we know how to handle
        // this is a polymorphic operation, so we will find Slices which contain
        // sub-concepts of our concepts of interest as well
        for ( Slice slice : slicePattern.find( dataset ) )
        {
            SliceTableData data = slice.getTableData( );

            // retrieve Concepts describing the data contained in the DSPL Slice
            Concept identifierConcept = identifierPattern.findDimension( slice );
            Concept timeConcept = timePattern.findDimension( slice );
            Concept latConcept = latPattern.findMetric( slice );
            Concept lonConcept = lonPattern.findMetric( slice );

            // find data columns using associated with the Slice Concepts
            TableColumn identifierColumn = data.getDimensionColumn( identifierConcept );
            TableColumn timeColumn = data.getDimensionColumn( timeConcept );
            TableColumn latColumn = data.getMetricColumn( latConcept );
            TableColumn lonColumn = data.getMetricColumn( lonConcept );

            // iterate through the data rows in the dspl data file
            for ( int i = 0; i < data.getNumRows( ); i++ )
            {
                // track identifier
                String identifier = identifierColumn.getStringData( i );

                // absolute timestamp in posix milliseconds (since epoch)
                long time = timeColumn.getDateData( i );

                // latitude and longitude of track point
                float lat_deg = latColumn.getFloatData( i );
                float lon_deg = lonColumn.getFloatData( i );

                // project track point lat/lon onto a TangentPlane
                LatLonGeo latlon = LatLonGeo.fromDeg( lat_deg, lon_deg );

                if ( projection == null )
                {
                    projection = new TangentPlane( latlon );
                }

                Vector2d point = projection.project( latlon );

                // update the bounding box for the data
                if ( minX > point.getX( ) ) minX = point.getX( );
                if ( minY > point.getY( ) ) minY = point.getY( );
                if ( maxX < point.getX( ) ) maxX = point.getX( );
                if ( maxY < point.getY( ) ) maxY = point.getY( );
                if ( minTime > time ) minTime = time;
                if ( maxTime < time ) maxTime = time;

                // retrieve the integer track id associated with the string identifier
                // or assign one if none exists yet
                Integer trackId = trackIdMap.get( identifier );
                if ( trackId == null )
                {
                    trackId = nextTrackId++;
                    trackIdMap.put( identifier, trackId );
                    initializeGeoTrack( trackPainter, trackId );
                }

                // add the point from the dspl data set to the track painter
                trackPainter.addPoint( trackId, 0, point.getX( ), point.getY( ), time );
            }
        }

        // create an Axis2D to define the bounds of the plotting area
        Axis2D axis = new Axis2D( );

        // set the axis bounds (display all the data initially)
        axis.set( minX, maxX, minY, maxY );
        axis.lockAspectRatioXY( 1.0 );

        // set time bounds (display all the data initially)
        trackPainter.displayTimeRange( minTime, maxTime );

        // create a layout to draw the geographic data
        GlimpseAxisLayout2D geoPlot = new GlimpseAxisLayout2D( "DsplTrackExampleGeo", axis );

        // add an axis mouse listener to the layout so that it responds to user pans/zooms
        geoPlot.addGlimpseMouseAllListener( new AxisMouseListener2D( ) );

        // add a solid color background painter to the plot
        geoPlot.addPainter( new BackgroundPainter( ).setColor( GlimpseColor.getBlack( ) ) );

        // add grid lines to the plot
        geoPlot.addPainter( new GridPainter( ).setLineColor( GlimpseColor.getGray( 0.7f ) ).setShowMinorGrid( true ) );

        // add the track painter with dspl data loaded into it into the plot
        geoPlot.addPainter( trackPainter );

        // add a scale indicator to the plot
        ScalePainter scale = new ScalePainter( );
        scale.setPixelBufferX( 8 );
        scale.setPixelBufferY( 8 );
        scale.setUnitLabel( "ft" );
        scale.setUnitConverter( new AxisUnitConverter( )
        {
            @Override
            public double fromAxisUnits( double value )
            {
                return Length.fromFeet( value );
            }

            @Override
            public double toAxisUnits( double value )
            {
                return Length.toFeet( value );
            }
        } );
        geoPlot.addPainter( scale );

        // convert the minimum and maximum timestamps from the data set to TimeStamps
        TimeStamp minTimestamp = TimeStamp.fromPosixMillis( minTime );
        TimeStamp maxTimestamp = TimeStamp.fromPosixMillis( maxTime );

        // create an Epoch defining what absolute timestamp corresponds to 0 on the timeline
        // here we use the minimum timestamp from the data set
        // the Epoch should always be chosen to be close to the data set times
        final Epoch epoch = new Epoch( TimeStamp.fromPosixMillis( minTime ) );

        // create a timeline plot
        final StackedTimePlot2D timePlot = new StackedTimePlot2D( epoch );

        // customize the coloring on the timeline
        timePlot.setBackgroundColor( GlimpseColor.fromColorRgb( 25, 42, 62 ) );
        timePlot.getDefaultTimeline( ).setAxisColor( GlimpseColor.getWhite( ) );

        // set the selected time range on the timeline
        timePlot.setTimeSelection( minTimestamp, maxTimestamp );

        // set the bounds of the timeline to slightly beyond the selected time range
        timePlot.setTimeAxisBounds( minTimestamp.subtract( Time.fromHours( 1 ) ), maxTimestamp.add( Time.fromHours( 1 ) ) );

        // add a mouse listener which updates the visible section of the geographic track
        // based on the selected time region
        timePlot.getOverlayLayout( ).addGlimpseMouseMotionListener( new GlimpseMouseMotionListener( )
        {
            @Override
            public void mouseMoved( GlimpseMouseEvent e )
            {
                Tag minTag = timePlot.getTimeSelectionMinTag( );
                Tag maxTag = timePlot.getTimeSelectionMaxTag( );

                TimeStamp selectionMin = epoch.toTimeStamp( minTag.getValue( ) );
                TimeStamp selectionMax = epoch.toTimeStamp( maxTag.getValue( ) );

                trackPainter.displayTimeRange( selectionMin.toPosixMillis( ), selectionMax.toPosixMillis( ) );
            }
        } );

        // create a parent plot to contain the geographic and time plots
        GlimpseLayout parentLayout = new GlimpseLayout( "DsplTrackExampleParent" );

        // add the geographic and time plots to the parent
        parentLayout.addLayout( geoPlot );
        parentLayout.addLayout( timePlot );

        // set layout constraints to position the geographic and time plots within the parent layout
        geoPlot.setLayoutData( "cell 0 0 1 1, push, grow" );
        timePlot.setLayoutData( "cell 0 1 1 1, pushx, growx, height 200!" );

        // iterate through the dataset again, this time looking for other
        // data series which we can display on the timeline

        // should really use Concept as key, but Concept doesn't have a good hash code at the moment
        Map<String, Pair<TrackPainter, TimePlotInfo>> plotMap = new HashMap<String, Pair<TrackPainter, TimePlotInfo>>( );

        for ( Slice slice : slicePattern.find( dataset ) )
        {
            SliceTableData data = slice.getTableData( );

            // we still need the track identifier and time columns
            Concept identifierConcept = identifierPattern.findDimension( slice );
            Concept timeConcept = timePattern.findDimension( slice );

            TableColumn identifierColumn = data.getDimensionColumn( identifierConcept );
            TableColumn timeColumn = data.getDimensionColumn( timeConcept );

            // iterate through all the other metric columns in the data set
            for ( String id : data.getMetricColumnIds( ) )
            {
                // get the column and concept
                TableColumn column = data.getMetricColumn( id );
                Concept concept = column.getConcept( );

                // don't display latitude or longitude on timeline plots
                if ( concept == null || latPattern.matches( concept ) || lonPattern.matches( concept ) ) continue;

                DataType type = concept.getType( ).getRef( );

                // we only display FLOAT and INTEGER type data on the lineplot
                if ( type != DataType.FLOAT && type == DataType.INTEGER ) continue;

                // we will create a timeline plot for each additional concept in the data set
                Pair<TrackPainter, TimePlotInfo> pair = plotMap.get( id );
                if ( pair == null )
                {
                    TimePlotInfo plotInfo = timePlot.createTimePlot( id );
                    TrackPainter plotPainter = new TrackPainter( );

                    for ( Integer trackId : trackIdMap.values( ) )
                    {
                        initializeTimelinePlot( plotPainter, trackId );
                    }

                    plotInfo.addPainter( plotPainter );

                    initializePlot( concept.getNameEnglish( ), plotInfo );

                    pair = new Pair<TrackPainter, TimePlotInfo>( plotPainter, plotInfo );

                    plotMap.put( id, pair );
                }

                TrackPainter plotPainter = pair.first( );
                TimePlotInfo plotInfo = pair.second( );

                minY = Double.POSITIVE_INFINITY;
                maxY = Double.NEGATIVE_INFINITY;

                // iterate through the data set rows
                for ( int i = 0; i < data.getNumRows( ); i++ )
                {
                    String identifier = identifierColumn.getStringData( i );
                    Integer trackId = trackIdMap.get( identifier );

                    double dataY = 0.0;

                    if ( type == DataType.INTEGER )
                    {
                        dataY = column.getIntegerData( i );
                    }
                    else if ( type == DataType.FLOAT )
                    {
                        dataY = column.getFloatData( i );
                    }

                    if ( minY > dataY ) minY = dataY;
                    if ( maxY < dataY ) maxY = dataY;

                    long time = timeColumn.getDateData( i );
                    TimeStamp timestamp = TimeStamp.fromPosixMillis( time );
                    double timeX = epoch.fromTimeStamp( timestamp );

                    plotPainter.addPoint( trackId, 0, timeX, dataY, time );
                }

                Axis1D axisY = plotInfo.getOrthogonalAxis( );
                axisY.setMin( minY );
                axisY.setMax( maxY );
            }
        }

        // if there were no additional data columns, create an empty timeline plot anyway
        if ( plotMap.isEmpty( ) )
        {
            TimePlotInfo plot = timePlot.createTimePlot( "default" );
            initializePlot( "default", plot );
        }

        return parentLayout;
    }

    protected void initializePlot( String name, TimePlotInfo plot )
    {
        plot.setLabelText( name );
        plot.setAxisColor( GlimpseColor.getWhite( ) );
        plot.setLabelColor( GlimpseColor.getWhite( ) );
    }

    // setup the visual characteristics (color, thickness, etc...) of the track
    protected void initializeGeoTrack( TrackPainter trackPainter, int trackId )
    {
        trackPainter.setLineColor( trackId, GlimpseColor.getBlue( ) );
        trackPainter.setLineWidth( trackId, 2.0f );
        trackPainter.setShowLines( trackId, true );
        trackPainter.setShowPoints( trackId, false );
        trackPainter.setShowHeadPoint( trackId, false );
    }

    protected void initializeTimelinePlot( TrackPainter trackPainter, int trackId )
    {
        trackPainter.setLineColor( trackId, GlimpseColor.getRed( ) );
        trackPainter.setLineWidth( trackId, 2.0f );
        trackPainter.setShowLines( trackId, true );
        trackPainter.setShowPoints( trackId, false );
        trackPainter.setShowHeadPoint( trackId, false );
    }
}
