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
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.System.currentTimeMillis;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.metsci.glimpse.gl.texture.DrawableTexture;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D.MutatorByte2D;
import com.metsci.glimpse.support.texture.RGBATextureProjected2D;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * @author borkholder
 */
public class ShadedReliefTiledPainter extends TilePainter<DrawableTexture>
{
    private static final Logger LOGGER = Logger.getLogger( ShadedReliefTiledPainter.class.getName( ) );

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

    /**
     * The first dimension is the number of colors, the second is {elevation threshold, hue, saturation, brightness}.
     */
    protected float[][] colors;

    public ShadedReliefTiledPainter( GeoProjection projection, TopoTileProvider tileProvider )
    {
        super( projection );
        this.tileProvider = tileProvider;

        setColors( BATHYMETRY_LIGHT_COLORS );

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

        colors = newColors;

        // force recalculate textures
        cacheData.clear( );
        lastAxis = new Rectangle2D.Double( );
    }

    @Override
    protected DrawableTexture loadTileData( TileKey key )
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

        RGBATextureProjected2D texture = new RGBATextureProjected2D( tile.getImageWidth( ), tile.getImageHeight( ) )
        {
            @Override
            protected void prepare_setTexParameters( GL gl )
            {
                super.prepare_setTexParameters( gl );

                GL3 gl3 = gl.getGL3( );
                gl3.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
                gl3.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST );
            }
        };
        texture.setProjection( tile.getProjection( projection ) );

        float[][] shaded = tile.shaded;
        float[][] elev = tile.data;
        long start = currentTimeMillis( );
        texture.mutate( new MutatorByte2D( )
        {
            @Override
            public void mutate( ByteBuffer data, int dataSizeX, int dataSizeY )
            {
                data.order( ByteOrder.BIG_ENDIAN );
                IntBuffer buf = data.asIntBuffer( );
                for ( int r = 0; r < dataSizeY; r++ )
                {
                    for ( int c = 0; c < dataSizeX; c++ )
                    {
                        int rgba = colorize( shaded[c][r], elev[c][r] );
                        buf.put( rgba );
                    }
                }
            }
        } );
        long stop = currentTimeMillis( );
        logFine( LOGGER, "Took %,dms to load texture", stop - start );

        return texture;
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
        ByteBuffer bbuf = ByteBuffer.allocateDirect( cached.getImageWidth( ) * Float.BYTES );
        FloatBuffer fb = bbuf.asFloatBuffer( );

        cached.data = new float[cached.getImageWidth( )][cached.getImageHeight( )];
        for ( float[] row : cached.data )
        {
            bbuf.rewind( );
            ch.read( bbuf );
            fb.rewind( );
            fb.get( row );
        }

        cached.shaded = new float[cached.getImageWidth( )][cached.getImageHeight( )];
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

        rf.writeInt( tile.getImageWidth( ) );
        rf.writeInt( tile.getImageHeight( ) );
        rf.writeDouble( tile.getStartLat( ) );
        rf.writeDouble( tile.getStartLon( ) );
        rf.writeDouble( tile.getWidthStep( ) );
        rf.writeDouble( tile.getHeightStep( ) );

        FileChannel ch = rf.getChannel( );
        ByteBuffer bbuf = ByteBuffer.allocateDirect( tile.getImageWidth( ) * Float.BYTES );
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

    protected int colorize( float hillshade, float elevation )
    {
        float h = 0;
        float s = 0;
        float b = 0;
        for ( int i = 0; i < colors.length; i++ )
        {
            if ( elevation <= colors[i][0] )
            {
                h = colors[i][1];
                s = colors[i][2];
                b = colors[i][3];
                break;
            }
        }

        float alpha = clamp( ( hillshade - 0.4f ), 0, 0.6f ) + 0.7f;
        b = clamp( b * alpha, 0, 1 );
        return ( Color.HSBtoRGB( h, s, b ) << 8 ) | 0xff;
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

    protected class CachedTileData extends TopographyData
    {
        float[][] shaded;

        CachedTileData( ) throws IOException
        {
            super( null );
        }

        public CachedTileData( TopographyData src, float[][] shaded ) throws IOException
        {
            this( );
            this.data = src.data;
            this.shaded = shaded;
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
