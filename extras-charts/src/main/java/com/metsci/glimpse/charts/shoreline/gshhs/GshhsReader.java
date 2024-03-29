/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.charts.shoreline.gshhs;

import static com.metsci.glimpse.util.io.IoUtils.url;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.charts.shoreline.LandVertex;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.UnrecognizedValueException;
import com.metsci.glimpse.util.Pair;
import com.metsci.glimpse.util.io.LittleEndianDataInput;

/**
 * The GSHHS dataset can be downloaded from ftp://ftp.soest.hawaii.edu/pwessel/gshhs/ .
 *
 * @author hogye
 */
public class GshhsReader
{

    public static Pair<List<List<LandVertex>>, List<GshhsPolygonHeader>> readSegments( File file ) throws IOException
    {
        return readSegments( url( file ) );
    }

    public static Pair<List<List<LandVertex>>, List<GshhsPolygonHeader>> readSegments( URL url ) throws IOException
    {
        try ( InputStream stream = url.openStream( ) )
        {
            return readSegments( stream );
        }
    }

    /**
     * Closing the stream is left up to the caller.
     */
    public static Pair<List<List<LandVertex>>, List<GshhsPolygonHeader>> readSegments( InputStream stream ) throws IOException
    {
        List<List<LandVertex>> segments = new ArrayList<List<LandVertex>>( );
        List<GshhsPolygonHeader> headers = new ArrayList<GshhsPolygonHeader>( );
        try
        {
            DataInputStream dataStream = new DataInputStream( new BufferedInputStream( stream ) );
            DataInput in = ( isLittleEndian( dataStream ) ? new LittleEndianDataInput( dataStream ) : dataStream );
            while ( true )
            {
                GshhsPolygonHeader header = new GshhsPolygonHeader( in );

                // If the polygon crosses Greenwich, the header lists its westernmost lon
                // as negative, but all its vertices still have lons between 0 and 360. We
                // need to shift some vertices (those just west of Greenwich) by -360.
                //
                // If the polygon surrounds the north or south pole, we need to shift some
                // vertices (those between 180 and 360) by -360. We also need to reorder
                // the vertices so that those shifted by -360 come first. (Actually, this
                // is a tough problem in general, since rebreaking the polygon at 180/-180
                // could create new polygons. In practice, Antarctica is the only polygon
                // we have to worry about, and rebreaking it is straightforward.)
                //
                // For any other polygon, we want to shift all the vertices by the same
                // amount, so that the whole polygon is shifted together.

                // North America, e.g.
                boolean shiftWholePolygon = ( !header.crossesGreenwich && header.westLon_DEG >= 180 );

                // Antarctica
                boolean isAntarctica = ( header.southLat_DEG == -90 && header.westLon_DEG == 0 && header.eastLon_DEG == 360 );

                List<LandVertex> segment = new ArrayList<LandVertex>( header.numVertices );
                for ( int i = 0; i < header.numVertices; i++ )
                {
                    // Longitude comes first.
                    double vertexLon_DEG = 1e-6 * in.readInt( );
                    double vertexLat_DEG = 1e-6 * in.readInt( );

                    // Western tip of Africa, e.g.
                    if ( header.crossesGreenwich && vertexLon_DEG >= 270 ) vertexLon_DEG -= 360;

                    // North America, e.g.
                    if ( shiftWholePolygon ) vertexLon_DEG -= 360;

                    // Antarctica
                    if ( isAntarctica && vertexLon_DEG >= 180 ) vertexLon_DEG -= 360;

                    segment.add( new LandVertex( vertexLat_DEG, vertexLon_DEG ) );
                }

                // Antarctica
                if ( isAntarctica ) segment = rebreakAntarcticaSegment( segment );

                segments.add( segment );
                headers.add( header );
            }
        }
        catch ( EOFException e )
        {
            return new Pair<List<List<LandVertex>>, List<GshhsPolygonHeader>>( segments, headers );
        }
        catch ( UnrecognizedValueException e )
        {
            throw new RuntimeException( e );
        }
    }

    private static boolean isLittleEndian( DataInputStream stream ) throws IOException
    {
        stream.mark( 1024 );
        try
        {
            new GshhsPolygonHeader( stream );
            return false;
        }
        catch ( UnrecognizedValueException e )
        {
        }
        finally
        {
            stream.reset( );
        }

        stream.mark( 1024 );
        try
        {
            new GshhsPolygonHeader( new LittleEndianDataInput( stream ) );
            return true;
        }
        catch ( UnrecognizedValueException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            stream.reset( );
        }
    }

    private static List<LandVertex> rebreakAntarcticaSegment( List<LandVertex> segment )
    {
        for ( int i = 0; i < segment.size( ); i++ )
        {
            LandVertex vertex = segment.get( i );
            if ( vertex.lon > 0 )
            {
                List<LandVertex> newSegment = segment.subList( i, segment.size( ) );
                newSegment.addAll( segment.subList( 0, i ) );
                return newSegment;
            }
        }

        throw new RuntimeException( "Failed to rebreak Antarctica segment" );
    }

}
