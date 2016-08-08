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
package com.metsci.glimpse.charts.shoreline.ndgc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.metsci.glimpse.charts.shoreline.LandBox;
import com.metsci.glimpse.charts.shoreline.LandFile;
import com.metsci.glimpse.charts.shoreline.LandVertex;
import com.metsci.glimpse.util.StringUtils;

public class NgdcFile extends LandFile
{
    private static final Logger _logger = Logger.getLogger( NgdcFile.class.getName( ) );

    private static final String _headerLinePrefix = "# HEADER: ";

    private static final String _northLatKey = "northLat";
    private static final String _southLatKey = "southLat";
    private static final String _eastLonKey = "eastLon";
    private static final String _westLonKey = "westLon";
    private static final String _latOffsetKey = "latOffset";
    private static final String _lonOffsetKey = "lonOffset";
    private static final String _isSwCornerLandKey = "isSwCornerLand";

    public NgdcFile( File file )
    {
        this( file, readSegments( file ) );
    }

    private NgdcFile( File file, List<List<LandVertex>> segments )
    {
        super( segments, newLandBox( file, segments ), true );
    }

    private static List<List<LandVertex>> readSegments( File file )
    {
        double latOffset = 0.0;
        double lonOffset = 0.0;

        Map<String, String> headerMap = readHeader( file );
        if ( headerMap.containsKey( _latOffsetKey ) ) try
        {
            latOffset = Double.parseDouble( headerMap.get( _latOffsetKey ) );
        }
        catch ( NumberFormatException e )
        {
        }
        if ( headerMap.containsKey( _lonOffsetKey ) ) try
        {
            lonOffset = Double.parseDouble( headerMap.get( _lonOffsetKey ) );
        }
        catch ( NumberFormatException e )
        {
        }

        BufferedReader reader = null;
        long lineNumber = 0;
        try
        {
            List<List<LandVertex>> segments = new ArrayList<List<LandVertex>>( );
            List<LandVertex> segment = null;
            reader = new BufferedReader( new FileReader( file ) );
            while ( true )
            {
                String line = reader.readLine( );
                lineNumber++;
                if ( line == null ) break;
                if ( line.startsWith( _headerLinePrefix ) ) continue;

                double lat;
                double lon;
                if ( line.matches( "^[\\s]*[nN]a[nN][\\s]+[nN]a[nN][\\s]*$|^# -b$" ) )
                {
                    lat = Double.NaN;
                    lon = Double.NaN;
                }
                else
                {
                    String[] tokens = StringUtils.split( line.trim( ), '\t' );
                    if ( tokens.length != 2 ) throw new IOException( String.format( "Line %d: %s", lineNumber, line ) );

                    lat = Double.parseDouble( tokens[1] ) + latOffset;
                    lon = Double.parseDouble( tokens[0] ) + lonOffset;
                }

                if ( Double.isNaN( lat ) || Double.isNaN( lon ) )
                {
                    segment = new ArrayList<LandVertex>( );
                    segments.add( segment );
                }
                else
                {
                    segment.add( new LandVertex( lat, lon ) );
                }
            }
            return segments;
        }
        catch ( Exception e )
        {
            _logger.warning( "Error reading land file (line " + lineNumber + "): " + e );
            return null;
        }
        finally
        {
            try
            {
                if ( reader != null ) reader.close( );
            }
            catch ( IOException e )
            {
            }
        }
    }

    private static LandBox newLandBox( File file, List<List<LandVertex>> segments )
    {
        if ( segments == null ) return null;

        // First set fallback values in case header values are missing
        double northLat = Double.NEGATIVE_INFINITY;
        double southLat = Double.POSITIVE_INFINITY;
        double eastLon = Double.NEGATIVE_INFINITY;
        double westLon = Double.POSITIVE_INFINITY;
        for ( List<LandVertex> vertices : segments )
        {
            for ( LandVertex vertex : vertices )
            {
                northLat = Math.max( northLat, vertex.lat );
                southLat = Math.min( southLat, vertex.lat );
                eastLon = Math.max( eastLon, vertex.lon );
                westLon = Math.min( westLon, vertex.lon );
            }
        }
        boolean isSwCornerLand = false;

        // Now override with header values
        Map<String, String> headerMap = readHeader( file );
        if ( headerMap.containsKey( _northLatKey ) ) try
        {
            northLat = Double.parseDouble( headerMap.get( _northLatKey ) );
        }
        catch ( NumberFormatException e )
        {
        }
        if ( headerMap.containsKey( _southLatKey ) ) try
        {
            southLat = Double.parseDouble( headerMap.get( _southLatKey ) );
        }
        catch ( NumberFormatException e )
        {
        }
        if ( headerMap.containsKey( _eastLonKey ) ) try
        {
            eastLon = Double.parseDouble( headerMap.get( _eastLonKey ) );
        }
        catch ( NumberFormatException e )
        {
        }
        if ( headerMap.containsKey( _westLonKey ) ) try
        {
            westLon = Double.parseDouble( headerMap.get( _westLonKey ) );
        }
        catch ( NumberFormatException e )
        {
        }
        if ( headerMap.containsKey( _isSwCornerLandKey ) ) isSwCornerLand = Boolean.parseBoolean( headerMap.get( _isSwCornerLandKey ) );

        return new LandBox( northLat, southLat, eastLon, westLon, isSwCornerLand );
    }

    private static Map<String, String> readHeader( File file )
    {
        BufferedReader reader = null;
        long lineNumber = 0;
        try
        {
            Map<String, String> headerMap = new HashMap<String, String>( );
            reader = new BufferedReader( new FileReader( file ) );
            while ( true )
            {
                String line = reader.readLine( );
                lineNumber++;
                if ( line == null || !line.startsWith( _headerLinePrefix ) ) break;

                String[] tokens = StringUtils.split( line.substring( _headerLinePrefix.length( ) ), ' ' );
                if ( tokens.length != 2 ) throw new IOException( String.format( "Line %d: %s", lineNumber, line ) );

                headerMap.put( tokens[0], tokens[1] );
            }
            return headerMap;
        }
        catch ( Exception e )
        {
            _logger.warning( "Error reading land file (line " + lineNumber + "): " + e );
            return null;
        }
        finally
        {
            try
            {
                if ( reader != null ) reader.close( );
            }
            catch ( IOException e )
            {
            }
        }
    }
}
