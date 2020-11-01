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

import static com.metsci.glimpse.core.support.color.GlimpseColor.getBlack;
import static com.metsci.glimpse.core.support.color.GlimpseColor.toColorAwt;
import static com.metsci.glimpse.topo.TopoColorUtils.bathyColors2;
import static com.metsci.glimpse.topo.TopoLevelSet.createTopoLevels;
import static com.metsci.glimpse.topo.io.TopoCache.topoConfigString;
import static com.metsci.glimpse.topo.io.TopoDataPaths.glimpseTopoCacheDir;
import static com.metsci.glimpse.util.io.FileSync.lockFile;
import static com.metsci.glimpse.util.io.FileSync.unlockFile;
import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static com.metsci.glimpse.util.units.Angle.fromDeg;
import static com.metsci.glimpse.util.units.Angle.unwrap;
import static com.metsci.glimpse.util.units.Length.fromNauticalMiles;
import static java.awt.Color.RGBtoHSB;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.util.Arrays.sort;
import static java.util.Comparator.comparing;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.hash.Hashing;
import com.metsci.glimpse.core.gl.texture.DrawableTexture;
import com.metsci.glimpse.core.painter.geo.TileKey;
import com.metsci.glimpse.core.painter.geo.TilePainter;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter;
import com.metsci.glimpse.core.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.core.support.colormap.ColorGradientUtils.ValueAndColor;
import com.metsci.glimpse.core.support.projection.LatLonProjection;
import com.metsci.glimpse.core.support.texture.FloatTextureProjected2D;
import com.metsci.glimpse.topo.io.TopoDataset;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * Paints topography and bathymetry with a discrete set of colors and the Hillshade algorithm.
 *
 * @author borkholder
 */
public class ShadedReliefTiledPainter extends TilePainter<DrawableTexture[]>
{
    private static final Logger LOGGER = Logger.getLogger( ShadedReliefTiledPainter.class.getName( ) );

    public static final int HILLSHADE_TEXTURE_UNIT = 0;
    public static final int ELEVATION_TEXTURE_UNIT = 1;

    protected static final double COS_LIGHT_ZENITH = cos( fromDeg( 45 ) );
    protected static final double SIN_LIGHT_ZENITH = sin( fromDeg( 45 ) );
    protected static final double LIGHT_AZIMUTH = fromDeg( -135 );

    protected TopoLevelSet topoLevelSet;
    protected TopoReliefTileCache tileFileCache;
    protected ShadedTexturePainter topoImagePainter;
    protected ShadedReliefProgram shadedReliefProgram;

    public ShadedReliefTiledPainter( GeoProjection projection, TopoDataset topoDataset, String attributionText )
    {
        super( projection );

        this.topoLevelSet = createTopoLevels( topoDataset, 1_024, 1_024 );
        tileFileCache = new TopoReliefTileCache( );

        topoImagePainter = new ShadedTexturePainter( );
        shadedReliefProgram = new ShadedReliefProgram( ELEVATION_TEXTURE_UNIT, HILLSHADE_TEXTURE_UNIT );
        topoImagePainter.setProgram( shadedReliefProgram );
        addPainter( topoImagePainter );

        setAlpha( 1 );
        setColors( bathyColors2 );

        if ( attributionText != null )
        {
            SimpleTextPainter attributionPainter = new SimpleTextPainter( );
            attributionPainter.setPaintBackground( false );
            attributionPainter.setPaintBorder( false );
            attributionPainter.setFont( 10, false );
            attributionPainter.setColor( getBlack( 0.4f ) );
            attributionPainter.setText( attributionText );
            addPainter( attributionPainter );
        }
    }

    public void setAlpha( float alpha )
    {
        shadedReliefProgram.setAlpha( alpha );
    }

    /**
     * Sets the color steps. The value provided for each step is in elevation SU (meters).
     * The color shade (hue) is changed with the hillshade value.
     */
    public void setColors( ValueAndColor... levelColors )
    {
        levelColors = levelColors.clone( );
        // Sort in ascending order
        sort( levelColors, comparing( vc -> vc.v ) );

        float[][] newColors = new float[levelColors.length][4];
        for ( int i = 0; i < levelColors.length; i++ )
        {
            ValueAndColor vc = levelColors[i];
            Color awt = toColorAwt( new float[] { vc.r, vc.g, vc.b, vc.a } );
            RGBtoHSB( awt.getRed( ), awt.getGreen( ), awt.getBlue( ), newColors[i] );

            // Shift to add the elevation value
            newColors[i][3] = newColors[i][2];
            newColors[i][2] = newColors[i][1];
            newColors[i][1] = newColors[i][0];
            newColors[i][0] = vc.v;
        }

        shadedReliefProgram.setColors( newColors );
    }

    @Override
    protected DrawableTexture[] loadTileData( TileKey key )
    {
        CachedTileData tile = tileFileCache.readOrBuildTile( ( ReliefTileKey ) key );

        FloatTextureProjected2D shadeTexture = new FloatTextureProjected2D( tile.numLon, tile.numLat );
        FloatTextureProjected2D elevationTexture = new FloatTextureProjected2D( tile.numLon, tile.numLat );

        shadeTexture.setProjection( getProjection( projection, tile ) );
        elevationTexture.setProjection( getProjection( projection, tile ) );

        tile.shaded.rewind( );
        tile.elevation.rewind( );

        shadeTexture.mutate( ( data, sizeX, sizeY ) -> data.put( tile.shaded.asFloatBuffer( ) ) );
        elevationTexture.mutate( ( data, sizeX, sizeY ) -> data.put( tile.elevation.asFloatBuffer( ) ) );

        return new DrawableTexture[] { shadeTexture, elevationTexture };
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
                        int dstIdx = ( target.numLat - y - 1 ) * target.numLon + x;
                        float elevation = srcBuf.get( srcIdx );
                        tgtBuf.put( dstIdx, elevation );
                    }
                }

                break;
            }

//            case TOPO_F4:
//            {
//                source.dataBytes.rewind( );
//                target.elevation.rewind( );
//
//                FloatBuffer srcBuf = source.dataBytes.asFloatBuffer( );
//                FloatBuffer tgtBuf = target.elevation.asFloatBuffer( );
//
//                for ( int y = 0; y < target.numLat; y++ )
//                {
//                    for ( int x = 0; x < target.numLon; x++ )
//                    {
//                        int srcIdx = y * source.numDataCols + x;
//                        float elevation = srcBuf.get( srcIdx );
//                        tgtBuf.put( elevation );
//                    }
//                }
//
//                break;
//            }

            default:
                throw new AssertionError( "Unsupported format" );
        }
    }

    protected void hillshade( CachedTileData tile )
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
                float value = hillshade0( elevation, x, y, dx, dy, tile.numLat );
                int idx = x * tile.numLat + y;
                dest.put( idx, value );
            }

            // Can't hillshade the very edges, so just copy out
            int idxCol0 = x * tile.numLat + 0;
            int idxCol1 = x * tile.numLat + 1;
            dest.put( idxCol0, dest.get( idxCol1 ) );

            int idxEnd0 = x * tile.numLat + ( tile.numLon - 1 );
            int idxEnd1 = x * tile.numLat + ( tile.numLon - 2 );
            dest.put( idxEnd0, dest.get( idxEnd1 ) );
        }

        //        System.arraycopy( dest[1], 0, dest[0], 0, data.imageHeight );
        //        System.arraycopy( dest[data.imageWidth - 2], 0, dest[data.imageWidth - 1], 0, data.imageHeight );
    }

    /**
     * From http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
     */
    protected float hillshade0( FloatBuffer data, int x, int y, double dx, double dy, int stride )
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

        float a = -data.get( ai );
        float b = -data.get( bi );
        float c = -data.get( ci );
        float d = -data.get( di );
        float f = -data.get( fi );
        float g = -data.get( gi );
        float h = -data.get( hi );
        float i = -data.get( ii );
        double dzdx = ( ( 3 * c + 10 * f + 3 * i ) - ( 3 * a + 10 * d + 3 * g ) ) / ( 32 * dx );
        double dzdy = ( ( 3 * g + 10 * h + 3 * i ) - ( 3 * a + 10 * b + 3 * c ) ) / ( 32 * dy );
        double slope = atan( sqrt( dzdx * dzdx ) + ( dzdy * dzdy ) );
        double aspect = atan2( dzdy, -dzdx );

        double hillshade = ( COS_LIGHT_ZENITH * cos( slope ) ) + ( SIN_LIGHT_ZENITH * sin( slope ) * cos( LIGHT_AZIMUTH - aspect ) );
        return ( float ) hillshade;
    }

    /**
     * Gets the texture projection for a particular tile.
     */
    protected LatLonProjection getProjection( GeoProjection projection, CachedTileData data )
    {
        double endLat = data.startLat_DEG + data.latStep_DEG * data.numLat;
        double endLon = data.startLon_DEG + data.lonStep_DEG * data.numLon;

        return new LatLonProjection( projection, clampNorthSouth( data.startLat_DEG ), clampNorthSouth( endLat ), clampAntiMeridian( data.startLon_DEG ), clampAntiMeridian( endLon ), false );
    }

    @Override
    protected void replaceTileData( Collection<Entry<TileKey, DrawableTexture[]>> tileData )
    {
        topoImagePainter.removeAllDrawableTextures( );
        for ( Entry<TileKey, DrawableTexture[]> e : tileData )
        {
            topoImagePainter.addDrawableTexture( e.getValue( )[0], HILLSHADE_TEXTURE_UNIT );
            topoImagePainter.addNonDrawableTexture( e.getValue( )[0], e.getValue( )[1], ELEVATION_TEXTURE_UNIT );
        }
    }

    @Override
    protected Collection<TileKey> allKeys( )
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

    protected static class CachedTileData
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

            this.elevation = ByteBuffer.allocateDirect( numLat * numLon * Float.BYTES );
            this.shaded = ByteBuffer.allocateDirect( numLat * numLon * Float.BYTES );
        }

        protected CachedTileData( TopoHostTile data )
        {
            this.startLon_DEG = toDegrees( data.westLon_RAD );
            this.startLat_DEG = toDegrees( data.southLat_RAD );
            this.numLat = data.numDataRows;
            this.numLon = data.numDataCols;
            this.lonStep_DEG = toDegrees( unwrap( data.westLon_RAD, data.eastLon_RAD ) - data.westLon_RAD ) / numLon;
            this.latStep_DEG = toDegrees( data.northLat_RAD - data.southLat_RAD ) / numLat;

            this.elevation = ByteBuffer.allocateDirect( numLat * numLon * Float.BYTES );
            this.shaded = ByteBuffer.allocateDirect( numLat * numLon * Float.BYTES );
        }
    }

    protected class TopoReliefTileCache
    {
        public static final int VERSION_ID = 4;

        public TopoHostTile readTopoData( ReliefTileKey key )
        {
            return topoLevelSet.get( key.level ).copyTile( key.bandNum, key.tileNum, 2 );
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
            String name = String.format( "%s/tile_v%d_%s.bin", ShadedReliefTiledPainter.class.getSimpleName( ), VERSION_ID, hash );
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

        protected CachedTileData readCachedTile( File cacheFile ) throws IOException
        {
            RandomAccessFile rf = new RandomAccessFile( cacheFile, "r" );

            lockFile( cacheFile );
            try
            {
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
            finally
            {
                unlockFile( cacheFile );
            }
        }

        protected void writeCachedTile( File cacheFile, CachedTileData tile ) throws IOException
        {
            cacheFile.getParentFile( ).mkdirs( );
            RandomAccessFile rf = new RandomAccessFile( cacheFile, "rw" );

            lockFile( cacheFile );
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

                rf.close( );
            }
            finally
            {
                unlockFile( cacheFile );
            }
        }
    }

    protected static class ReliefTileKey extends TileKey
    {
        public int tileNum;
        public int bandNum;
        public int level;

        public ReliefTileKey( int levelIdx, int bandIdx, int tileIdx, TopoTileBounds bounds, double lengthScale )
        {
            super( lengthScale, bounds.southLat_DEG, bounds.northLat_DEG, bounds.westLon_DEG, bounds.eastLon_DEG );

            this.level = levelIdx;
            this.bandNum = bandIdx;
            this.tileNum = tileIdx;
        }
    }
}
