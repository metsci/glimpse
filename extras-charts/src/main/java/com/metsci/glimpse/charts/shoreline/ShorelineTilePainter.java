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
package com.metsci.glimpse.charts.shoreline;

import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.io.CountingInputStream;
import com.metsci.glimpse.charts.bathy.TileKey;
import com.metsci.glimpse.charts.bathy.TilePainter;
import com.metsci.glimpse.painter.shape.PolygonPainter;
import com.metsci.glimpse.painter.shape.PolygonPainter.TessellatedPolygon;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

public class ShorelineTilePainter extends TilePainter<TessellatedPolygon>
{
    private static final Logger LOGGER = Logger.getLogger( ShorelineTilePainter.class.getName( ) );

    protected final File file;
    protected final Map<TileKey, Long> offsets;
    protected final PolygonPainter painter;

    protected Set<TileKey> loadedGroups;
    protected float[] landColor;

    public ShorelineTilePainter( GeoProjection projection, File file ) throws IOException
    {
        super( projection );
        this.file = file;
        loadedGroups = new HashSet<>( );
        offsets = loadOffsets( file );
        logInfo( LOGGER, "Found %,d tiles in %s", offsets.size( ), file );

        painter = new PolygonPainter( );
        painter.displayTimeRange( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
        addPainter( painter );
        setLandColor( GlimpseColor.getBlack( ) );
    }

    public void setLandColor( float[] landColor )
    {
        this.landColor = landColor;
        for ( TileKey groupId : loadedGroups )
        {
            painter.setFillColor( groupId, landColor );
        }
    }

    protected Map<TileKey, Long> loadOffsets( File file ) throws IOException
    {
        try (CountingInputStream cis = new CountingInputStream( new BufferedInputStream( new FileInputStream( file ) ) ))
        {
            DataInputStream dis = new DataInputStream( cis );

            @SuppressWarnings( "unused" )
            int version = dis.readByte( );
            long dataStart = dis.readLong( );
            int numLevels = dis.readInt( );
            double[] levels = new double[numLevels];
            for ( int i = 0; i < levels.length; i++ )
            {
                levels[i] = dis.readFloat( );
            }

            Map<TileKey, Long> offsets = new HashMap<>( );
            while ( cis.getCount( ) < dataStart )
            {
                int level = dis.readByte( );
                float minLat = ( float ) toDegrees( dis.readFloat( ) );
                float maxLat = ( float ) toDegrees( dis.readFloat( ) );
                float minLon = ( float ) toDegrees( dis.readFloat( ) );
                float maxLon = ( float ) toDegrees( dis.readFloat( ) );
                long offset = dis.readLong( );
                offsets.put( new TileKey( levels[level], minLat, maxLat, minLon, maxLon ), offset );
            }

            return offsets;
        }
    }

    @Override
    protected TessellatedPolygon loadTileData( TileKey key )
    {
        try (RandomAccessFile rf = new RandomAccessFile( file, "r" ))
        {
            return readTile( rf, key );
        }
        catch ( IOException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    protected TessellatedPolygon readTile( RandomAccessFile rf, TileKey key ) throws IOException
    {
        long offset = offsets.get( key );
        logFine( LOGGER, "Reading tile at offset %,d", offset );
        rf.seek( offset );

        int numVertices = rf.readInt( );
        logFine( LOGGER, "Reading polygon with %,d vertices in %s", numVertices, key );
        ByteBuffer bbuf = ByteBuffer.allocate( numVertices * 2 * Float.BYTES );

        bbuf.rewind( );
        rf.getChannel( ).read( bbuf );
        bbuf.rewind( );
        FloatBuffer buf = bbuf.asFloatBuffer( );

        float[] verts = new float[buf.limit( )];
        for ( int j = 0; j < verts.length; j += 2 )
        {
            float lat = buf.get( );
            float lon = buf.get( );
            lon = ( float ) toRadians( clampAntiMeridian( toDegrees( lon ) ) );
            Vector2d v = projection.project( LatLonGeo.fromRad( lat, lon ) );
            verts[j] = ( float ) v.getX( );
            verts[j + 1] = ( float ) v.getY( );
        }

        return new TessellatedPolygon( new Polygon( ), verts );
    }

    @Override
    protected void replaceTileData( Collection<Entry<TileKey, TessellatedPolygon>> tileData )
    {
        for ( TileKey old : loadedGroups )
        {
            painter.setFill( old, false );
        }

        for ( Entry<TileKey, TessellatedPolygon> e : tileData )
        {
            TileKey groupId = e.getKey( );
            if ( loadedGroups.contains( groupId ) )
            {
                painter.setFill( groupId, true );
            }
            else
            {
                loadedGroups.add( groupId );
                configurePainter( groupId );
                TessellatedPolygon p = e.getValue( );
                painter.addPolygon( groupId, p, p, 0 );
            }
        }
    }

    protected void configurePainter( Object groupId )
    {
        painter.setFill( groupId, true );
        painter.setFillColor( groupId, landColor );
        painter.setShowLines( groupId, false );
    }

    @Override
    protected Collection<TileKey> allKeys( )
    {
        return Collections.unmodifiableCollection( offsets.keySet( ) );
    }
}
