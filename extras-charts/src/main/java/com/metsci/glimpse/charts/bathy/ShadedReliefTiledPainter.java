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

import static com.metsci.glimpse.support.color.GlimpseColor.getBlack;
import static com.metsci.glimpse.util.GeneralUtils.clamp;
import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseUserCacheDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static com.metsci.glimpse.util.units.Angle.fromDeg;
import static com.metsci.glimpse.util.units.Length.fromNauticalMiles;
import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.metsci.glimpse.gl.texture.DrawableTexture;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D.MutatorByte2D;
import com.metsci.glimpse.support.texture.RGBATextureProjected2D;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * @author borkholder
 */
public class ShadedReliefTiledPainter extends TilePainter<DrawableTexture>
{
    private static final Logger LOGGER = Logger.getLogger( ShadedReliefTiledPainter.class.getName( ) );

    private static final long VERSION_ID = 1;
    private static final double COS_LIGHT_ZENITH = cos( fromDeg( 45 ) );
    private static final double SIN_LIGHT_ZENITH = sin( fromDeg( 45 ) );
    private static final double LIGHT_AZIMUTH = fromDeg( -135 );

    protected float hue;
    protected TopoTileProvider tileProvider;
    protected ShadedTexturePainter topoImagePainter;

    public ShadedReliefTiledPainter( GeoProjection projection, TopoTileProvider tileProvider )
    {
        super( projection );
        this.tileProvider = tileProvider;
        setHue( 0.63f );

        topoImagePainter = new ShadedTexturePainter( );
        addPainter( topoImagePainter );

        if ( tileProvider.getAttribution( ) != null )
        {
            SimpleTextPainter attributionPainter = new SimpleTextPainter( );
            attributionPainter.setPaintBackground( false );
            attributionPainter.setPaintBorder( false );
            attributionPainter.setFont( 10, false );
            attributionPainter.setColor( getBlack( 0.4f ) );
            attributionPainter.setText( tileProvider.getAttribution( ) );
            addPainter( attributionPainter );
        }
    }

    public void setHue( float hue )
    {
        this.hue = hue;
        cacheData.clear( );
    }

    @Override
    protected DrawableTexture loadTileData( TileKey key )
    {
        CachedTileData tile = null;
        String name = String.format( "topo/tile_v%d_%x%x%x%x.bin", VERSION_ID, doubleToRawLongBits( key.minLat ), doubleToRawLongBits( key.minLon ),
                doubleToRawLongBits( key.maxLat ), doubleToRawLongBits( key.maxLon ) );
        File cacheFile = new File( glimpseUserCacheDir, name );
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
                TopographyData data = tileProvider.getTile( key );
                float[][] shaded = new float[data.getImageWidth( )][data.getImageHeight( )];
                hillshade( data, shaded );
                tile = new CachedTileData( data, shaded );

                cacheFile.getParentFile( ).mkdirs( );
                writeCachedTile( cacheFile, tile );
            }
            catch ( IOException ex )
            {
                throw new RuntimeException( ex );
            }
        }

        RGBATextureProjected2D texture = new RGBATextureProjected2D( tile.getImageWidth( ), tile.getImageHeight( ) );
        texture.setProjection( tile.getProjection( projection ) );

        float[][] shaded = tile.data;
        texture.mutate( new MutatorByte2D( )
        {
            @Override
            public void mutate( ByteBuffer data, int dataSizeX, int dataSizeY )
            {
                for ( int r = 0; r < dataSizeY; r++ )
                {
                    for ( int c = 0; c < dataSizeX; c++ )
                    {
                        float shade = shaded[c][r];
                        int x = colorize( shade );
                        data.put( ( byte ) ( ( x >> 24 ) & 0xff ) );
                        data.put( ( byte ) ( ( x >> 16 ) & 0xff ) );
                        data.put( ( byte ) ( ( x >> 8 ) & 0xff ) );
                        data.put( ( byte ) ( ( x >> 0 ) & 0xff ) );
                    }
                }
            }
        } );

        return texture;
    }

    protected CachedTileData readCachedTile( File cacheFile ) throws IOException
    {
        RandomAccessFile rf = new RandomAccessFile( cacheFile, "r" );

        CachedTileData rgba = new CachedTileData( );
        rgba.imageWidth = rf.readInt( );
        rgba.imageHeight = rf.readInt( );
        rgba.startLat = rf.readDouble( );
        rgba.startLon = rf.readDouble( );
        rgba.widthStep = rf.readDouble( );
        rgba.heightStep = rf.readDouble( );

        long size = rgba.getImageWidth( ) * rgba.getImageHeight( ) * Integer.BYTES;
        MappedByteBuffer mmap = rf.getChannel( ).map( MapMode.READ_ONLY, rf.getFilePointer( ), size );
        FloatBuffer fb = mmap.asFloatBuffer( );

        rgba.data = new float[rgba.getImageWidth( )][rgba.getImageHeight( )];
        for ( float[] row : rgba.data )
        {
            fb.get( row );
        }

        rf.close( );
        return rgba;
    }

    protected void writeCachedTile( File cacheFile, CachedTileData tile ) throws IOException
    {
        long size = tile.getImageWidth( ) * tile.getImageHeight( ) * Integer.BYTES;
        RandomAccessFile rf = new RandomAccessFile( cacheFile, "rw" );
        rf.setLength( 0 );

        rf.writeInt( tile.getImageWidth( ) );
        rf.writeInt( tile.getImageHeight( ) );
        rf.writeDouble( tile.getStartLat( ) );
        rf.writeDouble( tile.getStartLon( ) );
        rf.writeDouble( tile.getWidthStep( ) );
        rf.writeDouble( tile.getHeightStep( ) );

        MappedByteBuffer mmap = rf.getChannel( ).map( MapMode.READ_WRITE, rf.getFilePointer( ), size );
        FloatBuffer fb = mmap.asFloatBuffer( );

        for ( float[] row : tile.data )
        {
            fb.put( row );
        }

        rf.close( );
    }

    protected void hillshade( TopographyData data, float[][] dest )
    {
        /*
         * dx certainly changes as we change latitude, but the transition is
         * uneven and visually disturbing. Also found that tweaking by 0.5
         * helps increase visual separation.
         */
        double dy = 60 * fromNauticalMiles( 1 ) * data.heightStep / 2;
        double dx = 60 * fromNauticalMiles( 1 ) * data.widthStep / 2;

        for ( int x = 1; x < data.imageWidth - 1; x++ )
        {
            for ( int y = 1; y < data.imageHeight - 1; y++ )
            {
                dest[x][y] = hillshade0( data.data, x, y, dx, dy );
            }

            dest[x][0] = dest[x][1];
            dest[x][data.imageHeight - 1] = dest[x][data.imageHeight - 2];
        }

        System.arraycopy( dest[1], 0, dest[0], 0, data.imageHeight );
        System.arraycopy( dest[data.imageWidth - 2], 0, dest[data.imageWidth - 1], 0, data.imageHeight );
    }

    protected int colorize( float hillshade )
    {
        float h = clamp( ( hillshade - 0.4f ) / 0.5f, 0, 1 );
        float bri = 0.1f + 0.9f * h;
        float sat = 0.5f + 0.1f * ( 1 - h );
        int rgb = Color.HSBtoRGB( hue, sat, bri );
        return ( rgb << 8 ) | 0xff;
    }

    /**
     * From http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
     */
    protected float hillshade0( float[][] data, int x, int y, double dx, double dy )
    {
        float a = data[x - 1][y + 1];
        float b = data[x + 0][y + 1];
        float c = data[x + 1][y + 1];
        float d = data[x - 1][y + 0];
        float f = data[x + 1][y + 0];
        float g = data[x - 1][y - 1];
        float h = data[x + 0][y - 1];
        float i = data[x + 1][y - 1];
        double dzdx = ( ( 3 * c + 10 * f + 3 * i ) - ( 3 * a + 10 * d + 3 * g ) ) / ( 32 * dx );
        double dzdy = ( ( 3 * g + 10 * h + 3 * i ) - ( 3 * a + 10 * b + 3 * c ) ) / ( 32 * dy );
        double slope = atan( sqrt( dzdx * dzdx ) + ( dzdy * dzdy ) );
        double aspect = atan2( dzdy, -dzdx );

        double hillshade = ( COS_LIGHT_ZENITH * cos( slope ) ) + ( SIN_LIGHT_ZENITH * sin( slope ) * cos( LIGHT_AZIMUTH - aspect ) );
        return ( float ) hillshade;
    }

    protected class CachedTileData extends TopographyData
    {
        CachedTileData( ) throws IOException
        {
            super( null );
        }

        public CachedTileData( TopographyData src, float[][] shaded ) throws IOException
        {
            this( );
            this.data = shaded;
            this.imageWidth = src.getImageWidth( );
            this.imageHeight = src.getImageHeight( );
            this.heightStep = src.getHeightStep( );
            this.widthStep = src.getWidthStep( );
            this.startLat = src.getStartLat( );
            this.startLon = src.getStartLon( );
        }

        @Override
        protected void read( InputStream in ) throws IOException
        {
            // nop
        }
    }

    @Override
    protected void replaceTileData( Collection<Entry<TileKey, DrawableTexture>> tileData )
    {
        topoImagePainter.removeAllDrawableTextures( );
        for ( Entry<TileKey, DrawableTexture> e : tileData )
        {
            topoImagePainter.addDrawableTexture( e.getValue( ) );
        }
    }

    @Override
    protected Collection<TileKey> allKeys( )
    {
        return tileProvider.keys( );
    }
}
