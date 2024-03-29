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
package com.metsci.glimpse.extras.examples.charts.shoreline;

import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;
import static com.metsci.glimpse.util.units.Length.fromKilometers;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.toRadians;
import static java.util.stream.StreamSupport.stream;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.io.Files;
import com.metsci.glimpse.core.support.polygon.Polygon.Interior;
import com.metsci.glimpse.core.support.polygon.Polygon.Loop;
import com.metsci.glimpse.core.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.core.support.polygon.PolygonTessellator;
import com.metsci.glimpse.core.support.polygon.PolygonTessellator.TessellationException;
import com.metsci.glimpse.core.support.polygon.VertexAccumulator;
import com.metsci.glimpse.util.Pair;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

/**
 * This class takes shoreline polygons and tiles and tessellates them for faster loading in a painter.
 *
 * <p>
 * One possible implementation is to read OSM land polygons from <a href="http://openstreetmapdata.com/data">
 * http://openstreetmapdata.com/data</a>.
 * </p>
 */
public class ShorelineTiler
{
    private static final Logger LOGGER = Logger.getLogger( ShorelineTiler.class.getName( ) );

    public static void generate( File destFile, Iterable<Polygon> landPolygons ) throws Exception
    {
        File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );

        FloatList levels = new FloatArrayList( );
        Collection<TileOutInfo> allTiles = new ArrayList<>( );

        /*
         * 4. Add one block here for each level you want to generate. Set the length scale for the zoom, the size of the tiles, and the factor to decimate by.
         */
        {
            levels.add( ( float ) fromKilometers( 100 ) );
            Collection<TileOutInfo> tiles = createTiles( levels.size( ) - 1, tmpDir, 2, 1 );
            allTiles.addAll( tiles );

            stream( landPolygons.spliterator( ), true )
                    .map( ShorelineTiler::toArea )
                    .filter( a -> !a.isEmpty( ) )
                    .flatMap( a -> tiles.stream( ).map( t -> new Pair<>( t, a ) ) )
                    .forEach( p -> write( p.first( ), p.second( ) ) );
        }

        {
            levels.add( ( float ) fromKilometers( 500 ) );
            Collection<TileOutInfo> tiles = createTiles( levels.size( ) - 1, tmpDir, 3, 2 );
            allTiles.addAll( tiles );

            stream( landPolygons.spliterator( ), true )
                    .map( f -> toArea( f, 10 ) )
                    .filter( a -> !a.isEmpty( ) )
                    .flatMap( a -> tiles.stream( ).map( t -> new Pair<>( t, a ) ) )
                    .forEach( p -> write( p.first( ), p.second( ) ) );
        }

        {
            levels.add( ( float ) fromKilometers( 1_000 ) );
            Collection<TileOutInfo> tiles = createTiles( levels.size( ) - 1, tmpDir, 5, 3 );
            allTiles.addAll( tiles );

            stream( landPolygons.spliterator( ), true )
                    .map( f -> toArea( f, 100 ) )
                    .filter( a -> !a.isEmpty( ) )
                    .flatMap( a -> tiles.stream( ).map( t -> new Pair<>( t, a ) ) )
                    .forEach( p -> write( p.first( ), p.second( ) ) );
        }

        {
            levels.add( ( float ) fromKilometers( 10_000 ) );
            Collection<TileOutInfo> tiles = createTiles( levels.size( ) - 1, tmpDir, 30, 10 );
            allTiles.addAll( tiles );

            stream( landPolygons.spliterator( ), true )
                    .map( f -> toArea( f, 1_000 ) )
                    .filter( a -> !a.isEmpty( ) )
                    .flatMap( a -> tiles.stream( ).map( t -> new Pair<>( t, a ) ) )
                    .forEach( p -> write( p.first( ), p.second( ) ) );
        }

        Collection<TileOutInfo> validTiles = new ArrayList<>( allTiles );
        validTiles.removeIf( t -> !t.tmpFile.isFile( ) );
        int nLevels = levels.size( );

        try (DataOutputStream out = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( destFile ) ) ))
        {
            long dataOffset = Byte.BYTES + Long.BYTES + Integer.BYTES + Float.BYTES * nLevels + validTiles.size( ) * ( Byte.BYTES + 4 * Float.BYTES + Long.BYTES );

            // version
            out.writeByte( 0 );
            out.writeLong( dataOffset );
            out.writeInt( nLevels );
            for ( float v : levels )
            {
                out.writeFloat( v );
            }

            for ( TileOutInfo info : validTiles )
            {
                out.writeByte( ( byte ) info.level );
                out.writeFloat( ( float ) toRadians( info.bounds[0].getMinY( ) ) );
                out.writeFloat( ( float ) toRadians( info.bounds[0].getMaxY( ) ) );
                out.writeFloat( ( float ) toRadians( info.bounds[0].getMinX( ) ) );
                out.writeFloat( ( float ) toRadians( info.bounds[0].getMaxX( ) ) );
                out.writeLong( dataOffset );

                dataOffset += Integer.BYTES + info.tmpFile.length( );
            }

            for ( TileOutInfo info : validTiles )
            {
                int nVertices = ( int ) ( info.tmpFile.length( ) / Float.BYTES / 2 );
                out.writeInt( nVertices );
                Files.copy( info.tmpFile, out );
                info.tmpFile.delete( );
            }
        }
    }

    static class TileOutInfo
    {
        final int level;
        final long tileKey;
        final Rectangle2D[] bounds;
        final File tmpFile;

        TileOutInfo( int level, long tileKey, Rectangle2D[] bounds, File tmpFile )
        {
            this.level = level;
            this.tileKey = tileKey;
            this.bounds = bounds;
            this.tmpFile = tmpFile;
        }
    }

    static Area toArea( Polygon poly )
    {
        return toArea( poly, 1 );
    }

    static Area toArea( Polygon poly, int decimate )
    {
        LineString ring = poly.getExteriorRing( );

        if ( ring.getNumPoints( ) / decimate < 3 )
        {
            return new Area( );
        }

        double lastLon = 0;
        Path2D.Double p = new Path2D.Double( Path2D.WIND_NON_ZERO );
        for ( int i = 0; i < ring.getNumPoints( ); i++ )
        {
            if ( decimate > 1 && i % decimate != 0 )
            {
                continue;
            }

            Coordinate c = ring.getCoordinateN( i );
            double x = c.x;
            double y = c.y;

            // unroll the longitude so it doesn't wrap
            while ( x - lastLon > 180 )
            {
                x -= 360;
            }
            while ( lastLon - x > 180 )
            {
                x += 360;
            }
            lastLon = x;

            if ( i == 0 )
            {
                p.moveTo( x, y );
            }
            else
            {
                p.lineTo( x, y );
            }
        }

        p.closePath( );

        return new Area( p );
    }

    static void write( TileOutInfo info, Area area )
    {
        Area tiled = tile( area, info.bounds );
        if ( tiled.isEmpty( ) )
        {
            return;
        }

        Collection<double[]> tess = split( tiled )
                .map( ShorelineTiler::tessellate )
                .collect( Collectors.toList( ) );

        if ( tess.isEmpty( ) )
        {
            return;
        }

        try
        {
            int nVertices = ( int ) tess.stream( ).mapToLong( l -> l.length ).sum( ) / 2;
            logInfo( LOGGER, "Found %,d polygons in tile level %d with %,d vertices", tess.size( ), info.level, nVertices );

            synchronized ( info )
            {
                try (DataOutputStream out = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( info.tmpFile, true ) ) ))
                {
                    for ( double[] verts : tess )
                    {
                        for ( int i = 0; i < verts.length; i += 2 )
                        {
                            out.writeFloat( ( float ) toRadians( verts[i + 1] ) );
                            out.writeFloat( ( float ) toRadians( verts[i] ) );
                        }
                    }
                }
            }
        }
        catch ( IOException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    static Collection<TileOutInfo> createTiles( int level, File destDir, int tileWidth_DEG, int tileHeight_DEG )
    {
        long key = ( ( tileWidth_DEG * 1_000 + tileHeight_DEG ) * 100 + level ) * 1_000_000;

        Collection<TileOutInfo> tiles = new ArrayList<>( );
        double pad_DEG = 0.01 * max( tileWidth_DEG, tileHeight_DEG );
        for ( int lon = -180; lon < 180; lon += tileWidth_DEG )
        {
            for ( int lat = -90; lat < 90; lat += tileHeight_DEG )
            {
                double minLon = max( lon - pad_DEG, -180 );
                double width = min( tileWidth_DEG + 2 * pad_DEG, 180 - minLon );
                double minLat = max( lat - pad_DEG, -90 );
                double height = min( tileHeight_DEG + 2 * pad_DEG, 90 - minLat );

                Rectangle2D bounds0 = new Rectangle2D.Double( minLon, minLat, width, height );
                Rectangle2D bounds1 = new Rectangle2D.Double( minLon - 360, minLat, width, height );
                Rectangle2D bounds2 = new Rectangle2D.Double( minLon + 360, minLat, width, height );
                File tmpFile = new File( destDir, String.format( "data_%x", key++ ) );
                tiles.add( new TileOutInfo( level, key, new Rectangle2D[] { bounds0, bounds1, bounds2 }, tmpFile ) );
            }
        }

        return tiles;
    }

    static Area tile( Area shape, Rectangle2D... bounds )
    {
        for ( Rectangle2D b : bounds )
        {
            if ( shape.intersects( b ) )
            {
                Area a = new Area( b );
                a.intersect( shape );
                if ( !a.isEmpty( ) )
                {
                    return a;
                }
            }
        }

        return new Area( );
    }

    static Stream<Loop> split( Shape s )
    {
        List<Loop> all = new ArrayList<>( );

        double[] v = new double[2];
        PathIterator itr = s.getPathIterator( null, 10 );

        LoopBuilder bldr = null;
        while ( !itr.isDone( ) )
        {
            switch ( itr.currentSegment( v ) )
            {
                case PathIterator.SEG_MOVETO:
                    bldr = new LoopBuilder( );
                case PathIterator.SEG_LINETO:
                    bldr.addVertices( v, 1 );
                    break;
                case PathIterator.SEG_CLOSE:
                    all.add( bldr.complete( Interior.onLeft ) );
                    bldr = null;
                    break;
                default:
                    throw new AssertionError( );
            }

            itr.next( );
        }

        return all.stream( );
    }

    static double[] tessellate( Loop loop )
    {
        com.metsci.glimpse.core.support.polygon.Polygon p = new com.metsci.glimpse.core.support.polygon.Polygon( );
        p.add( loop );

        DoubleList list = new DoubleArrayList( );
        try
        {
            PolygonTessellator tess = new PolygonTessellator( );
            tess.tessellate( p, new VertexAccumulator( )
            {
                @Override
                public void addVertices( float[] vertexData, int nVertices )
                {
                    for ( int i = 0; i < nVertices; i++ )
                    {
                        list.add( vertexData[i * 2] );
                        list.add( vertexData[i * 2 + 1] );
                    }
                }

                @Override
                public void addVertices( double[] vertexData, int nVertices )
                {
                    for ( int i = 0; i < nVertices; i++ )
                    {
                        list.add( vertexData[i * 2] );
                        list.add( vertexData[i * 2 + 1] );
                    }
                }
            } );
            tess.destroy( );
        }
        catch ( TessellationException ex )
        {
            throw new RuntimeException( ex );
        }

        return list.toDoubleArray( );
    }
}
