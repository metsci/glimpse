/*
 * Copyright (c) 2016 Metron, Inc.
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
package com.metsci.glimpse.charts.bathy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.support.projection.LatLonProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.units.Angle;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * @author ulman
 */
public class BathymetryData
{
    protected double widthStep;
    protected double heightStep;

    protected double startLon;
    protected double startLat;

    protected int imageHeight;
    protected int imageWidth;

    protected GeoProjection projection;

    protected float[][] data;

    public BathymetryData( InputStream in, GeoProjection projection ) throws IOException
    {
        super( );
        this.projection = projection;
        read( in, projection );
    }

    private static class Row
    {
        public float centerLat;
        public float centerLon;
        public float depth;

        public Row( float centerLat, float centerLon, float depth )
        {
            this.centerLat = centerLat;
            this.centerLon = centerLon;
            this.depth = depth;
        }
    }

    protected void read( InputStream in, GeoProjection tp ) throws IOException
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

        // create a sorted set to store all the rows from the data input file of the form:
        // latitude longitude depth
        NavigableSet<Row> rows = new TreeSet<Row>( new Comparator<Row>( )
        {
            @Override
            public int compare( Row o1, Row o2 )
            {
                int latComparison = Double.compare( o1.centerLat, o2.centerLat );
                return ( latComparison != 0 ? latComparison : Double.compare( o1.centerLon, o2.centerLon ) );
            }
        } );

        // read lines from the input stream into the set
        String line = null;

        while ( ( line = reader.readLine( ) ) != null )
        {
            if ( line.trim( ).isEmpty( ) ) continue;

            String[] tokens = line.trim( ).split( "[ ]+" );

            float lon = Float.parseFloat( tokens[0] );
            float lat = Float.parseFloat( tokens[1] );
            float depth = Float.parseFloat( tokens[2] );

            rows.add( new Row( lat, lon, depth ) );
        }

        // create a set with only unique latitudes (in sorted order)
        NavigableSet<Row> uniqueLatitudes = new TreeSet<Row>( new Comparator<Row>( )
        {
            @Override
            public int compare( Row o1, Row o2 )
            {
                return Double.compare( o1.centerLat, o2.centerLat );
            }
        } );

        // create a set with only unique longitudes (in sorted order)
        NavigableSet<Row> uniqueLongitudes = new TreeSet<Row>( new Comparator<Row>( )
        {
            @Override
            public int compare( Row o1, Row o2 )
            {
                return Double.compare( o1.centerLon, o2.centerLon );
            }
        } );

        uniqueLatitudes.addAll( rows );
        uniqueLongitudes.addAll( rows );

        // retrieve the number of unique latitudes and longitudes from the sets
        imageHeight = uniqueLatitudes.size( );
        imageWidth = uniqueLongitudes.size( );

        // calculate the average step size moving along latitude and longitude
        widthStep = 0;
        Row prevRow = null;
        for ( Row row : uniqueLongitudes )
        {
            if ( prevRow != null )
            {
                widthStep += row.centerLon - prevRow.centerLon;
            }

            prevRow = row;
        }
        widthStep = widthStep / ( uniqueLongitudes.size( ) - 1 );

        heightStep = 0;
        prevRow = null;
        for ( Row row : uniqueLatitudes )
        {
            if ( prevRow != null )
            {
                heightStep += row.centerLat - prevRow.centerLat;
            }

            prevRow = row;
        }
        heightStep = heightStep / ( uniqueLatitudes.size( ) - 1 );

        // find the lat and lon of the starting corner
        startLon = uniqueLongitudes.first( ).centerLon - 0.5 * widthStep;
        startLat = uniqueLatitudes.first( ).centerLat - 0.5 * heightStep;

        data = new float[imageWidth][imageHeight];

        for ( Row row : rows )
        {
            int x = ( int ) Math.floor( ( row.centerLon - startLon ) / widthStep );
            int y = ( int ) Math.floor( ( row.centerLat - startLat ) / heightStep );

            if ( x < 0 ) x = 0;
            if ( x >= imageWidth ) x = imageWidth - 1;

            if ( y < 0 ) y = 0;
            if ( y >= imageHeight ) y = imageHeight - 1;

            data[x][y] = row.depth;
        }

        startLon = Angle.normalizeAngle180( startLon );
    }

    public FloatTextureProjected2D getTexture( )
    {
        // create an OpenGL texture wrapper object
        FloatTextureProjected2D texture = new FloatTextureProjected2D( imageWidth, imageHeight );

        Projection projection = getProjection( );

        texture.setProjection( projection );
        texture.setData( data );

        return texture;
    }

    public LatLonProjection getProjection( )
    {
        double endLat = startLat + heightStep * imageHeight;
        double endLon = startLon + widthStep * imageWidth;

        return new LatLonProjection( projection, startLat, endLat, startLon, endLon, false );
    }

    public void setAxisBounds( Axis2D axis )
    {
        axis.getAxisX( ).setMin( getMinX( ) );
        axis.getAxisX( ).setMax( getMaxX( ) );

        axis.getAxisY( ).setMin( getMinY( ) );
        axis.getAxisY( ).setMax( getMaxY( ) );
    }

    public double getStartLon( )
    {
        return startLon;
    }

    public double getStartLat( )
    {
        return startLat;
    }

    public double getWidthStep( )
    {
        return widthStep;
    }

    public double getHeightStep( )
    {
        return heightStep;
    }

    public int getImageHeight( )
    {
        return imageHeight;
    }

    public int getImageWidth( )
    {
        return imageWidth;
    }

    public double getMinX( )
    {
        Vector2d swCorner = projection.project( LatLonGeo.fromDeg( startLat, startLon ) );
        Vector2d neCorner = projection.project( LatLonGeo.fromDeg( startLat + heightStep * imageHeight, startLon + widthStep * imageWidth ) );

        return Math.min( swCorner.getX( ), neCorner.getX( ) );
    }

    public double getMaxX( )
    {
        Vector2d swCorner = projection.project( LatLonGeo.fromDeg( startLat, startLon ) );
        Vector2d neCorner = projection.project( LatLonGeo.fromDeg( startLat + heightStep * imageHeight, startLon + widthStep * imageWidth ) );

        return Math.max( swCorner.getX( ), neCorner.getX( ) );
    }

    public double getMinY( )
    {
        Vector2d swCorner = projection.project( LatLonGeo.fromDeg( startLat, startLon ) );
        Vector2d neCorner = projection.project( LatLonGeo.fromDeg( startLat + heightStep * imageHeight, startLon + widthStep * imageWidth ) );

        return Math.min( swCorner.getY( ), neCorner.getY( ) );
    }

    public double getMaxY( )
    {
        Vector2d swCorner = projection.project( LatLonGeo.fromDeg( startLat, startLon ) );
        Vector2d neCorner = projection.project( LatLonGeo.fromDeg( startLat + heightStep * imageHeight, startLon + widthStep * imageWidth ) );

        return Math.max( swCorner.getY( ), neCorner.getY( ) );
    }

    public float[][] getData( )
    {
        return data;
    }
}
