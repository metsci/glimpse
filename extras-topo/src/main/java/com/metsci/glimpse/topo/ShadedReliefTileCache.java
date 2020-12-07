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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.topo.TopoLevelSet.createTopoLevels;
import static com.metsci.glimpse.topo.io.TopoCache.topoConfigString;
import static com.metsci.glimpse.topo.io.TopoDataPaths.glimpseTopoCacheDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static com.metsci.glimpse.util.units.Angle.fromDeg;
import static com.metsci.glimpse.util.units.Angle.unwrap;
import static com.metsci.glimpse.util.units.Length.fromNauticalMiles;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import com.google.common.hash.Hashing;
import com.metsci.glimpse.core.painter.geo.TileKey;
import com.metsci.glimpse.topo.io.TopoDataset;

/**
 * Provides tiled topography along with the relief (hillshaded) data for each tile.
 *
 * @author borkholder
 */
public class ShadedReliefTileCache
{
    private static final Logger LOGGER = Logger.getLogger( ShadedReliefTileCache.class.getName( ) );

    public static final int CACHE_VERSION_ID = 5;

    private static final int PIXELS_PER_TILE_LAT = 1_024;
    private static final int PIXELS_PER_TILE_LON = 1_024;

    public static final double COS_LIGHT_ZENITH = cos( fromDeg( 45 ) );
    public static final double SIN_LIGHT_ZENITH = sin( fromDeg( 45 ) );
    public static final double LIGHT_AZIMUTH = fromDeg( -135 );

    protected final TopoLevelSet topoLevelSet;

    public ShadedReliefTileCache( TopoDataset topoDataset )
    {
        this.topoLevelSet = createTopoLevels( topoDataset, PIXELS_PER_TILE_LAT, PIXELS_PER_TILE_LON );
    }

    public Collection<TileKey> getAllTileKeys( )
    {
        Collection<TileKey> tileKeys = new ArrayList<>( );

        for ( int levelIdx = 0; levelIdx < topoLevelSet.size( ); levelIdx++ )
        {
            TopoLevel level = topoLevelSet.levels.get( levelIdx );

            // Aim for 1 cell per pixel in a 1600x1200 screen
            int expectedNumPixels = 1_600;
            double nmPerDegreeAtEquator = 60;
            double lengthScale = fromNauticalMiles( nmPerDegreeAtEquator * expectedNumPixels * topoLevelSet.cellSizes_DEG.v( levelIdx ) );

            for ( int bandIdx = 0; bandIdx < level.numBands; bandIdx++ )
            {
                for ( int tileIdx = 0; tileIdx < level.numTiles; tileIdx++ )
                {
                    TopoTileBounds bounds = level.tileBounds( bandIdx, tileIdx );
                    tileKeys.add( new ReliefTileKey( levelIdx, bandIdx, tileIdx, bounds, lengthScale ) );
                }
            }
        }

        return tileKeys;
    }

    public CachedTileData readOrBuildTile( ReliefTileKey key )
    {
        String configString = topoConfigString( topoLevelSet.levels.get( 0 ).file );
        String hash = Hashing.murmur3_128( ).newHasher( )
                .putString( configString, Charset.defaultCharset( ) )
                .putDouble( key.lengthScale )
                .putDouble( key.minLat_DEG )
                .putDouble( key.maxLat_DEG )
                .putDouble( key.minLon_DEG )
                .putDouble( key.maxLon_DEG )
                .hash( )
                .toString( );

        CachedTileData tile = null;
        String name = String.format( "%s/tile_v%d_%s.bin", ShadedReliefTiledPainter.class.getSimpleName( ), CACHE_VERSION_ID, hash );
        File cacheFile = new File( glimpseTopoCacheDir, name );
        if ( cacheFile.isFile( ) )
        {
            logFine( LOGGER, "Loading cached topo tile from %s", cacheFile );
            try
            {
                tile = readCachedTile( cacheFile );
            }
            catch ( IOException ex )
            {
                logWarning( LOGGER, "Failed to read cache file", ex );
            }
        }

        if ( tile == null )
        {
            logFine( LOGGER, "Building topo tile for %s", key );
            try
            {
                TopoHostTile data = readTopoData( key );
                tile = new CachedTileData( data );
                copyElevationData( data, tile );

                hillshade( tile );

                writeCachedTile( cacheFile, tile );
            }
            catch ( IOException ex )
            {
                throw new UncheckedIOException( ex );
            }
        }

        return tile;
    }

    public TopoHostTile readTopoData( ReliefTileKey key )
    {
        return topoLevelSet.get( key.level ).copyTile( key.bandNum, key.tileNum, 2 );
    }

    protected void copyElevationData( TopoHostTile source, CachedTileData target )
    {
        switch ( source.dataType )
        {
            case TOPO_I2:
            {
                source.dataBytes.rewind( );
                target.elevation.rewind( );

                ShortBuffer srcBuf = source.dataBytes.asShortBuffer( );
                FloatBuffer tgtBuf = target.elevation.asFloatBuffer( );

                for ( int y = 0; y < target.numLat; y++ )
                {
                    for ( int x = 0; x < target.numLon; x++ )
                    {
                        int srcIdx = y * source.numDataCols + x;
                        float elevation = srcBuf.get( srcIdx );
                        tgtBuf.put( elevation );
                    }
                }

                break;
            }

            case TOPO_F4:
            {
                source.dataBytes.rewind( );
                target.elevation.rewind( );

                FloatBuffer srcBuf = source.dataBytes.asFloatBuffer( );
                FloatBuffer tgtBuf = target.elevation.asFloatBuffer( );

                for ( int y = 0; y < target.numLat; y++ )
                {
                    for ( int x = 0; x < target.numLon; x++ )
                    {
                        int srcIdx = y * source.numDataCols + x;
                        float elevation = srcBuf.get( srcIdx );
                        tgtBuf.put( elevation );
                    }
                }

                break;
            }

            default:
                throw new AssertionError( "Unsupported format" );
        }
    }

    public void hillshade( CachedTileData tile )
    {
        tile.elevation.rewind( );
        tile.shaded.rewind( );

        FloatBuffer elevation = tile.elevation.asFloatBuffer( );
        FloatBuffer dest = tile.shaded.asFloatBuffer( );

        /*
         * dx certainly changes as we change latitude, but the transition is
         * uneven and visually disturbing. Also found that tweaking by 0.5
         * helps increase visual separation.
         */
        double dy = 60 * fromNauticalMiles( 1 ) * 0.5 * tile.latStep_DEG;
        double dx = 60 * fromNauticalMiles( 1 ) * 0.5 * tile.lonStep_DEG;

        for ( int x = 1; x < tile.numLon - 1; x++ )
        {
            for ( int y = 1; y < tile.numLat - 1; y++ )
            {
                float value = hillshadeBuffer( elevation, x, y, dx, dy, tile.numLat );
                int idx = x * tile.numLat + y;
                dest.put( idx, value );
            }

            // Can't hillshade the very edges, so just copy out
            int idxDst = x * tile.numLat + 0;
            int idxSrc = x * tile.numLat + 1;
            dest.put( idxDst, dest.get( idxSrc ) );

            idxDst = x * tile.numLat + ( tile.numLat - 1 );
            idxSrc = x * tile.numLat + ( tile.numLat - 2 );
            dest.put( idxDst, dest.get( idxSrc ) );
        }

        // Can't hillshade the very edges, so just copy out
        for ( int y = 0; y < tile.numLat; y++ )
        {
            int idxDst = 0 * tile.numLat + y;
            int idxSrc = 1 * tile.numLat + y;
            dest.put( idxDst, dest.get( idxSrc ) );

            idxDst = ( tile.numLon - 1 ) * tile.numLat + y;
            idxSrc = ( tile.numLon - 2 ) * tile.numLat + y;
            dest.put( idxDst, dest.get( idxSrc ) );
        }
    }

    /**
     * From http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
     */
    protected float hillshadeBuffer( FloatBuffer data, int x, int y, double dx, double dy, int stride )
    {
        int ai = ( x - 1 ) * stride + ( y + 1 );
        int bi = ( x + 0 ) * stride + ( y + 1 );
        int ci = ( x + 1 ) * stride + ( y + 1 );
        int di = ( x - 1 ) * stride + ( y + 0 );
        // skip e middle
        int fi = ( x + 0 ) * stride + ( y + 0 );
        int gi = ( x - 1 ) * stride + ( y - 1 );
        int hi = ( x + 0 ) * stride + ( y - 1 );
        int ii = ( x + 1 ) * stride + ( y - 1 );

        float a = data.get( ai );
        float b = data.get( bi );
        float c = data.get( ci );
        float d = data.get( di );
        float f = data.get( fi );
        float g = data.get( gi );
        float h = data.get( hi );
        float i = data.get( ii );
        double dzdx = ( ( 3 * c + 10 * f + 3 * i ) - ( 3 * a + 10 * d + 3 * g ) ) / ( 32 * dx );
        double dzdy = ( ( 3 * g + 10 * h + 3 * i ) - ( 3 * a + 10 * b + 3 * c ) ) / ( 32 * dy );
        double slope = atan( sqrt( dzdx * dzdx ) + ( dzdy * dzdy ) );
        double aspect = atan2( dzdy, -dzdx );

        double hillshade = ( COS_LIGHT_ZENITH * cos( slope ) ) + ( SIN_LIGHT_ZENITH * sin( slope ) * cos( LIGHT_AZIMUTH - aspect ) );
        return ( float ) hillshade;
    }

    public CachedTileData readCachedTile( File cacheFile ) throws IOException
    {
        RandomAccessFile rf = new RandomAccessFile( cacheFile, "r" );

        int numLat = rf.readInt( );
        int numLon = rf.readInt( );
        double startLat_DEG = rf.readDouble( );
        double startLon_DEG = rf.readDouble( );
        double latStep_DEG = rf.readDouble( );
        double lonStep_DEG = rf.readDouble( );

        FileChannel ch = rf.getChannel( );

        CachedTileData cached = new CachedTileData( latStep_DEG, lonStep_DEG, startLat_DEG, startLon_DEG, numLat, numLon );

        ch.read( cached.elevation );
        ch.read( cached.shaded );

        rf.close( );
        return cached;
    }

    public void writeCachedTile( File cacheFile, CachedTileData tile ) throws IOException
    {
        cacheFile.getParentFile( ).mkdirs( );
        RandomAccessFile rf = new RandomAccessFile( cacheFile, "rw" );

        // Get an exclusive lock while writing
        FileLock lock = rf.getChannel( ).lock( );

        try
        {
            rf.setLength( 0 );

            rf.writeInt( tile.numLat );
            rf.writeInt( tile.numLon );
            rf.writeDouble( tile.startLat_DEG );
            rf.writeDouble( tile.startLon_DEG );
            rf.writeDouble( tile.latStep_DEG );
            rf.writeDouble( tile.lonStep_DEG );

            FileChannel ch = rf.getChannel( );

            tile.elevation.rewind( );
            tile.shaded.rewind( );

            ch.write( tile.elevation );
            ch.write( tile.shaded );
        }
        finally
        {
            rf.close( );
        }
    }

    public static class ReliefTileKey extends TileKey
    {
        public final int tileNum;
        public final int bandNum;
        public final int level;

        public ReliefTileKey( int levelIdx, int bandIdx, int tileIdx, TopoTileBounds bounds, double lengthScale )
        {
            super( lengthScale, bounds.southLat_DEG, bounds.northLat_DEG, bounds.westLon_DEG, bounds.eastLon_DEG );

            this.level = levelIdx;
            this.bandNum = bandIdx;
            this.tileNum = tileIdx;
        }
    }

    public static class CachedTileData
    {
        protected final double latStep_DEG;
        protected final double lonStep_DEG;

        protected final double startLat_DEG;
        protected final double startLon_DEG;

        protected final int numLat;
        protected final int numLon;

        /*
         * Byte buffer for the elevation, but underlying data is floats.
         * Data is packed as a linearized row-major matrix of [lon][lat].
         */
        protected ByteBuffer elevation;
        /*
         * Byte buffer for the shaded relief, but underlying data is floats.
         * Data is packed as a linearized row-major matrix of [lon][lat].
         */
        protected ByteBuffer shaded;

        protected CachedTileData( double latStep_DEG, double lonStep_DEG, double startLat_DEG, double startLon_DEG, int numLat, int numLon )
        {
            this.latStep_DEG = latStep_DEG;
            this.lonStep_DEG = lonStep_DEG;
            this.startLat_DEG = startLat_DEG;
            this.startLon_DEG = startLon_DEG;
            this.numLat = numLat;
            this.numLon = numLon;

            this.elevation = ByteBuffer.allocateDirect( numLat * numLon * Float.BYTES ).order( ByteOrder.nativeOrder( ) );
            this.shaded = ByteBuffer.allocateDirect( numLat * numLon * Float.BYTES ).order( ByteOrder.nativeOrder( ) );
        }

        protected CachedTileData( TopoHostTile data )
        {
            this.startLon_DEG = toDegrees( data.westLon_RAD );
            this.startLat_DEG = toDegrees( data.southLat_RAD );
            this.numLat = data.numDataRows;
            this.numLon = data.numDataCols;
            this.lonStep_DEG = toDegrees( unwrap( data.westLon_RAD, data.eastLon_RAD ) - data.westLon_RAD ) / numLon;
            this.latStep_DEG = toDegrees( data.northLat_RAD - data.southLat_RAD ) / numLat;

            this.elevation = ByteBuffer.allocateDirect( numLat * numLon * Float.BYTES ).order( ByteOrder.nativeOrder( ) );
            this.shaded = ByteBuffer.allocateDirect( numLat * numLon * Float.BYTES ).order( ByteOrder.nativeOrder( ) );
        }
    }
}
