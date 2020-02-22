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
package com.metsci.glimpse.charts.bathy;

import static com.metsci.glimpse.core.support.color.GlimpseColor.getBlack;
import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseUserCacheDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static com.metsci.glimpse.util.units.Angle.fromDeg;
import static com.metsci.glimpse.util.units.Length.fromNauticalMiles;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.metsci.glimpse.core.gl.texture.DrawableTexture;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter;
import com.metsci.glimpse.core.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.core.support.color.GlimpseColor;
import com.metsci.glimpse.core.support.projection.LatLonProjection;
import com.metsci.glimpse.core.support.texture.FloatTextureProjected2D;
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

    public static final Map<Float, float[]> BATHYMETRY_LIGHT_COLORS;

    static
    {
        try
        {
            BATHYMETRY_LIGHT_COLORS = ImmutableMap.<Float, float[]> builder( )
                    .put( Float.POSITIVE_INFINITY, GlimpseColor.fromColorHex( "#c9dfef" ) )
                    .put( -20f, GlimpseColor.fromColorHex( "#bbd9f0" ) )
                    .put( -100f, GlimpseColor.fromColorHex( "#b0cee8" ) )
                    .put( -500f, GlimpseColor.fromColorHex( "#a3c9e6" ) )
                    .put( -1_000f, GlimpseColor.fromColorHex( "#81acd6" ) )
                    .put( -2_000f, GlimpseColor.fromColorHex( "#76a5cf" ) )
                    .put( -4_000f, GlimpseColor.fromColorHex( "#6499c1" ) )
                    .put( -8_000f, GlimpseColor.fromColorHex( "#3c6e98" ) )
                    .build( );
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( ex );
        }
    }

    protected static final int VERSION_ID = 3;
    protected static final double COS_LIGHT_ZENITH = cos( fromDeg( 45 ) );
    protected static final double SIN_LIGHT_ZENITH = sin( fromDeg( 45 ) );
    protected static final double LIGHT_AZIMUTH = fromDeg( -135 );

    protected TopoTileProvider tileProvider;
    protected ShadedTexturePainter topoImagePainter;
    protected ShadedReliefProgram shadedReliefProgram;

    public ShadedReliefTiledPainter( GeoProjection projection, TopoTileProvider tileProvider )
    {
        super( projection );
        this.tileProvider = tileProvider;

        topoImagePainter = new ShadedTexturePainter( );
        shadedReliefProgram = new ShadedReliefProgram( ELEVATION_TEXTURE_UNIT, HILLSHADE_TEXTURE_UNIT );
        topoImagePainter.setProgram( shadedReliefProgram );
        addPainter( topoImagePainter );

        setAlpha( 1 );
        setColors( BATHYMETRY_LIGHT_COLORS );

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

    public void setAlpha( float alpha )
    {
        shadedReliefProgram.setAlpha( alpha );
    }

    /**
     * Sets color scale.  The map from the maximum elevation to the color for that elevation. The color
     * shade is changed with the hillshade value.
     */
    public void setColors( Map<Float, float[]> elevation2Colors )
    {
        List<Entry<Float, float[]>> entries = new ArrayList<>( elevation2Colors.entrySet( ) );
        entries.sort( Comparator.comparing( Entry::getKey ) );

        float[][] newColors = new float[entries.size( )][4];
        for ( int i = 0; i < entries.size( ); i++ )
        {
            Color awt = GlimpseColor.toColorAwt( entries.get( i ).getValue( ) );
            Color.RGBtoHSB( awt.getRed( ), awt.getGreen( ), awt.getBlue( ), newColors[i] );
            newColors[i][3] = newColors[i][2];
            newColors[i][2] = newColors[i][1];
            newColors[i][1] = newColors[i][0];
            newColors[i][0] = entries.get( i ).getKey( );
        }

        shadedReliefProgram.setColors( newColors );
    }

    @Override
    protected DrawableTexture[] loadTileData( TileKey key )
    {
        String hash = Hashing.murmur3_128( ).newHasher( )
                .putString( tileProvider.getAttribution( ), Charset.defaultCharset( ) )
                .putDouble( key.lengthScale )
                .putDouble( key.minLat )
                .putDouble( key.maxLat )
                .putDouble( key.minLon )
                .putDouble( key.maxLon )
                .hash( )
                .toString( );

        CachedTileData tile = null;
        String name = String.format( "topo/tile_v%d_%s.bin", VERSION_ID, hash );
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
                tile = new CachedTileData( );
                tile.imageWidth = data.getImageWidth( );
                tile.imageHeight = data.getImageHeight( );
                tile.heightStep = data.getHeightStep( );
                tile.widthStep = data.getWidthStep( );
                tile.startLat = data.getStartLat( );
                tile.startLon = data.getStartLon( );
                tile.data = data.data;

                float[][] shaded = new float[data.getImageWidth( )][data.getImageHeight( )];
                hillshade( data, shaded );
                tile.shaded = shaded;

                cacheFile.getParentFile( ).mkdirs( );
                writeCachedTile( cacheFile, tile );
            }
            catch ( IOException ex )
            {
                throw new RuntimeException( ex );
            }
        }

        FloatTextureProjected2D shadeTexture = new FloatTextureProjected2D( tile.imageWidth, tile.imageHeight );
        FloatTextureProjected2D elevationTexture = new FloatTextureProjected2D( tile.imageWidth, tile.imageHeight );
        shadeTexture.setProjection( tile.getProjection( projection ) );
        elevationTexture.setProjection( tile.getProjection( projection ) );

        shadeTexture.setData( tile.shaded );
        elevationTexture.setData( tile.data );

        return new DrawableTexture[] { shadeTexture, elevationTexture };
    }

    protected CachedTileData readCachedTile( File cacheFile ) throws IOException
    {
        RandomAccessFile rf = new RandomAccessFile( cacheFile, "r" );

        CachedTileData cached = new CachedTileData( );
        cached.imageWidth = rf.readInt( );
        cached.imageHeight = rf.readInt( );
        cached.startLat = rf.readDouble( );
        cached.startLon = rf.readDouble( );
        cached.widthStep = rf.readDouble( );
        cached.heightStep = rf.readDouble( );

        FileChannel ch = rf.getChannel( );
        ByteBuffer bbuf = ByteBuffer.allocateDirect( cached.imageHeight * Float.BYTES );
        FloatBuffer fb = bbuf.asFloatBuffer( );

        cached.data = new float[cached.imageWidth][cached.imageHeight];
        for ( float[] row : cached.data )
        {
            bbuf.rewind( );
            ch.read( bbuf );
            fb.rewind( );
            fb.get( row );
        }

        cached.shaded = new float[cached.imageWidth][cached.imageHeight];
        for ( float[] row : cached.shaded )
        {
            bbuf.rewind( );
            ch.read( bbuf );
            fb.rewind( );
            fb.get( row );
        }

        rf.close( );
        return cached;
    }

    protected void writeCachedTile( File cacheFile, CachedTileData tile ) throws IOException
    {
        RandomAccessFile rf = new RandomAccessFile( cacheFile, "rw" );
        rf.setLength( 0 );

        rf.writeInt( tile.imageWidth );
        rf.writeInt( tile.imageHeight );
        rf.writeDouble( tile.startLat );
        rf.writeDouble( tile.startLon );
        rf.writeDouble( tile.widthStep );
        rf.writeDouble( tile.heightStep );

        FileChannel ch = rf.getChannel( );
        ByteBuffer bbuf = ByteBuffer.allocateDirect( tile.imageHeight * Float.BYTES );
        FloatBuffer fb = bbuf.asFloatBuffer( );
        for ( float[] row : tile.data )
        {
            bbuf.rewind( );
            fb.rewind( );
            fb.put( row );
            ch.write( bbuf );
        }

        for ( float[] row : tile.shaded )
        {
            bbuf.rewind( );
            fb.rewind( );
            fb.put( row );
            ch.write( bbuf );
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

    /**
     * From http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
     */
    protected float hillshade0( float[][] data, int x, int y, double dx, double dy )
    {
        float a = -data[x - 1][y + 1];
        float b = -data[x + 0][y + 1];
        float c = -data[x + 1][y + 1];
        float d = -data[x - 1][y + 0];
        float f = -data[x + 1][y + 0];
        float g = -data[x - 1][y - 1];
        float h = -data[x + 0][y - 1];
        float i = -data[x + 1][y - 1];
        double dzdx = ( ( 3 * c + 10 * f + 3 * i ) - ( 3 * a + 10 * d + 3 * g ) ) / ( 32 * dx );
        double dzdy = ( ( 3 * g + 10 * h + 3 * i ) - ( 3 * a + 10 * b + 3 * c ) ) / ( 32 * dy );
        double slope = atan( sqrt( dzdx * dzdx ) + ( dzdy * dzdy ) );
        double aspect = atan2( dzdy, -dzdx );

        double hillshade = ( COS_LIGHT_ZENITH * cos( slope ) ) + ( SIN_LIGHT_ZENITH * sin( slope ) * cos( LIGHT_AZIMUTH - aspect ) );
        return ( float ) hillshade;
    }

    protected class CachedTileData
    {
        protected double widthStep;
        protected double heightStep;

        protected double startLon;
        protected double startLat;

        protected int imageHeight;
        protected int imageWidth;

        /**
         * Data should be positive up.
         */
        protected float[][] data;

        float[][] shaded;

        public LatLonProjection getProjection( GeoProjection projection )
        {
            double endLat = startLat + heightStep * imageHeight;
            double endLon = startLon + widthStep * imageWidth;
            return new LatLonProjection( projection, clampNorthSouth( startLat ), clampNorthSouth( endLat ), clampAntiMeridian( startLon ), clampAntiMeridian( endLon ), false );
        }
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
        return tileProvider.keys( );
    }
}
