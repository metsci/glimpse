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

import static com.metsci.glimpse.painter.base.GlimpsePainterBase.getAxis2D;
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
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.DrawableTexture;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.support.PainterCache;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D.MutatorByte2D;
import com.metsci.glimpse.support.texture.RGBATextureProjected2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * @author borkholder
 */
public class ShadedReliefTiledPainter extends DelegatePainter
{
    private static final Logger LOGGER = Logger.getLogger( ShadedReliefTiledPainter.class.getName( ) );

    private static final long VERSION_ID = 1;
    private static final double COS_LIGHT_ZENITH = cos( fromDeg( 45 ) );
    private static final double SIN_LIGHT_ZENITH = sin( fromDeg( 45 ) );
    private static final double LIGHT_AZIMUTH = fromDeg( -135 );
    private static final float HUE = 0.63f;

    private GeoProjection projection;
    private TileProvider tileProvider;
    private Map<TopoTileKey, Area> tileBounds;

    private PainterCache<TopoTileKey, DrawableTexture> topoTextures;
    private ShadedTexturePainter topoImagePainter;
    private Rectangle2D.Double lastAxis;

    private Executor executor;

    public ShadedReliefTiledPainter( GeoProjection projection, TileProvider tileProvider )
    {
        this.projection = projection;
        this.tileProvider = tileProvider;
        this.executor = newFixedThreadPool( clamp( getRuntime( ).availableProcessors( ) - 2, 1, 3 ) );
        topoTextures = new PainterCache<>( this::newTopoTexture, executor );
        lastAxis = new Rectangle2D.Double( );

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

    @Override
    public void paintTo( GlimpseContext context )
    {
        checkNewState( context );
        super.paintTo( context );
    }

    private void checkNewState( GlimpseContext context )
    {
        Axis2D axis = getAxis2D( context );

        if ( tileBounds == null )
        {
            tileBounds = createTileKeys( tileProvider );
        }

        if ( lastAxis.getMinX( ) != axis.getMinX( ) ||
                lastAxis.getMaxX( ) != axis.getMaxX( ) ||
                lastAxis.getMinY( ) != axis.getMinY( ) ||
                lastAxis.getMaxY( ) != axis.getMaxY( ) )
        {
            lastAxis = new Rectangle2D.Double( axis.getMinX( ), axis.getMinY( ), axis.getMaxX( ) - axis.getMinX( ), axis.getMaxY( ) - axis.getMinY( ) );

            Collection<TopoTileKey> tiles = getVisibleTiles( lastAxis );

            topoImagePainter.removeAllDrawableTextures( );
            boolean anyMissed = false;
            for ( TopoTileKey key : tiles )
            {
                DrawableTexture tex = topoTextures.get( key );
                if ( tex == null )
                {
                    anyMissed = true;
                }
                else
                {
                    topoImagePainter.addDrawableTexture( tex );
                }
            }

            if ( anyMissed )
            {
                lastAxis = new Rectangle2D.Double( );
            }
        }
    }

    private Map<TopoTileKey, Area> createTileKeys( TileProvider grid )
    {
        int pxWidth = grid.getPixelsX( );
        int pxHeight = grid.getPixelsY( );
        double px2Lon = 360.0 / pxWidth;
        double px2Lat = 180.0 / pxHeight;

        int tilePixelsX = pxWidth / 30;
        int tilePixelsY = pxHeight / 15;

        Map<TopoTileKey, Area> keys = new HashMap<>( );
        for ( int pxX = 0; pxX < pxWidth; pxX += tilePixelsX )
        {
            for ( int pxY = 0; pxY < pxHeight; pxY += tilePixelsY )
            {
                int pixelX0 = max( 0, pxX - 2 );
                int pixelY0 = max( 0, pxY - 2 );
                int pixelWidth = min( pxWidth - pixelX0, tilePixelsX + 4 );
                int pixelHeight = min( pxHeight - pixelY0, tilePixelsY + 4 );
                TopoTileKey key = new TopoTileKey( pixelX0, pixelY0, pixelWidth, pixelHeight );

                double lon = pixelX0 * px2Lon - 180;
                double lat = 90 - ( pixelY0 + pixelHeight ) * px2Lat;
                Vector2d sw = projection.project( LatLonGeo.fromDeg( lat, lon ) );
                lat = 90 - pixelY0 * px2Lat;
                Vector2d nw = projection.project( LatLonGeo.fromDeg( lat, lon ) );
                lon = ( pixelX0 + pixelWidth ) * px2Lon - 180;
                Vector2d ne = projection.project( LatLonGeo.fromDeg( lat, lon ) );
                lat = 90 - ( pixelY0 + pixelHeight ) * px2Lat;
                Vector2d se = projection.project( LatLonGeo.fromDeg( lat, lon ) );

                /*
                 * If the border is clockwise, the tile is valid in the current
                 * projection. This test will fail for tiles at the edges of a
                 * TangentPlane because of how skewed they are.
                 */
                double sumOverEdge = 0;
                sumOverEdge += ( se.getX( ) - ne.getX( ) ) * ( se.getY( ) + ne.getY( ) );
                sumOverEdge += ( sw.getX( ) - se.getX( ) ) * ( sw.getY( ) + se.getY( ) );
                sumOverEdge += ( nw.getX( ) - sw.getX( ) ) * ( nw.getY( ) + sw.getY( ) );
                sumOverEdge += ( ne.getX( ) - nw.getX( ) ) * ( ne.getY( ) + nw.getY( ) );
                if ( sumOverEdge > 0 )
                {
                    Path2D path = new Path2D.Double( Path2D.WIND_EVEN_ODD );
                    path.moveTo( sw.getX( ), sw.getY( ) );
                    path.lineTo( nw.getX( ), nw.getY( ) );
                    path.lineTo( ne.getX( ), ne.getY( ) );
                    path.lineTo( se.getX( ), se.getY( ) );
                    path.closePath( );
                    keys.put( key, new Area( path ) );
                }
            }
        }

        return keys;
    }

    private Collection<TopoTileKey> getVisibleTiles( Rectangle2D bounds )
    {
        // Pad for irregular projections
        double padX = bounds.getWidth( ) * 0.02;
        double padY = bounds.getHeight( ) * 0.02;
        bounds = new Rectangle2D.Double( bounds.getMinX( ) - padX, bounds.getMinY( ) - padY, bounds.getWidth( ) + 2 * padX, bounds.getHeight( ) + 2 * padY );

        Collection<TopoTileKey> keys = new ArrayList<>( );
        for ( Entry<TopoTileKey, Area> e : tileBounds.entrySet( ) )
        {
            if ( e.getValue( ).intersects( bounds ) )
            {
                keys.add( e.getKey( ) );
            }
        }

        return keys;
    }

    private DrawableTexture newTopoTexture( TopoTileKey key )
    {
        CachedTileData rgba = null;
        File cacheFile = new File( glimpseUserCacheDir, String.format( "topo/tile_%s.bin", key.id ) );
        if ( cacheFile.isFile( ) )
        {
            logFine( LOGGER, "Loading cached topo tile from %s", cacheFile );
            try
            {
                rgba = readCachedTile( cacheFile );
            }
            catch ( IOException ex )
            {
                logWarning( LOGGER, "Failed to read cache file", ex );
            }
        }

        if ( rgba == null )
        {
            logFine( LOGGER, "Building topo tile for %s", key );
            try
            {
                TopographyData data = tileProvider.getTile( key.pixelX0, key.pixelY0, key.pixelWidth, key.pixelHeight );
                int[][] colored = new int[data.getImageWidth( )][data.getImageHeight( )];
                hillshade( data, colored );
                rgba = new CachedTileData( data, colored );

                cacheFile.getParentFile( ).mkdirs( );
                writeCachedTile( cacheFile, rgba );
            }
            catch ( IOException ex )
            {
                throw new RuntimeException( ex );
            }
        }

        RGBATextureProjected2D texture = new RGBATextureProjected2D( rgba.getImageWidth( ), rgba.getImageHeight( ) );
        texture.setProjection( rgba.getProjection( projection ) );

        int[][] pixels = rgba.rgba;
        texture.mutate( new MutatorByte2D( )
        {
            @Override
            public void mutate( ByteBuffer data, int dataSizeX, int dataSizeY )
            {
                for ( int r = 0; r < dataSizeY; r++ )
                {
                    for ( int c = 0; c < dataSizeX; c++ )
                    {
                        int x = pixels[c][r];
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

    private CachedTileData readCachedTile( File cacheFile ) throws IOException
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
        IntBuffer ib = mmap.asIntBuffer( );

        rgba.rgba = new int[rgba.getImageWidth( )][rgba.getImageHeight( )];
        for ( int[] row : rgba.rgba )
        {
            ib.get( row );
        }

        rf.close( );
        return rgba;
    }

    private void writeCachedTile( File cacheFile, CachedTileData rgba ) throws IOException
    {
        long size = rgba.getImageWidth( ) * rgba.getImageHeight( ) * Integer.BYTES;
        RandomAccessFile rf = new RandomAccessFile( cacheFile, "rw" );
        rf.setLength( 0 );

        rf.writeInt( rgba.getImageWidth( ) );
        rf.writeInt( rgba.getImageHeight( ) );
        rf.writeDouble( rgba.getStartLat( ) );
        rf.writeDouble( rgba.getStartLon( ) );
        rf.writeDouble( rgba.getWidthStep( ) );
        rf.writeDouble( rgba.getHeightStep( ) );

        MappedByteBuffer mmap = rf.getChannel( ).map( MapMode.READ_WRITE, rf.getFilePointer( ), size );
        IntBuffer fb = mmap.asIntBuffer( );

        for ( int[] row : rgba.rgba )
        {
            fb.put( row );
        }

        rf.close( );
    }

    private void hillshade( TopographyData data, int[][] dest )
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
                float shaded = hillshade0( data.data, x, y, dx, dy );
                int rgba = colorize( shaded );
                dest[x][y] = rgba;
            }

            dest[x][0] = dest[x][1];
            dest[x][data.imageHeight - 1] = dest[x][data.imageHeight - 2];
        }

        System.arraycopy( dest[1], 0, dest[0], 0, data.imageHeight );
        System.arraycopy( dest[data.imageWidth - 2], 0, dest[data.imageWidth - 1], 0, data.imageHeight );
    }

    private int colorize( float hillshade )
    {
        float h = clamp( ( hillshade - 0.4f ) / 0.5f, 0, 1 );
        float bri = 0.1f + 0.9f * h;
        float sat = 0.5f + 0.1f * ( 1 - h );
        int rgb = Color.HSBtoRGB( HUE, sat, bri );
        return ( rgb << 8 ) | 0xff;
    }

    /**
     * From http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
     */
    private float hillshade0( float[][] data, int x, int y, double dx, double dy )
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

    private static class TopoTileKey
    {
        final String id;
        final int pixelX0;
        final int pixelY0;
        final int pixelWidth;
        final int pixelHeight;

        TopoTileKey( int pixelX0, int pixelY0, int pixelWidth, int pixelHeight )
        {
            this.pixelX0 = pixelX0;
            this.pixelY0 = pixelY0;
            this.pixelWidth = pixelWidth;
            this.pixelHeight = pixelHeight;

            id = String.format( "%x-%x%x-%x%x", VERSION_ID, pixelX0, pixelY0, pixelWidth, pixelHeight );
        }

        @Override
        public int hashCode( )
        {
            return id.hashCode( );
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( obj instanceof TopoTileKey )
            {
                TopoTileKey other = ( TopoTileKey ) obj;
                return pixelHeight == other.pixelHeight &&
                        pixelWidth == other.pixelWidth &&
                        pixelX0 == other.pixelX0 &&
                        pixelY0 == other.pixelY0;
            }
            else
            {
                return false;
            }
        }

        @Override
        public String toString( )
        {
            return String.format( "TileKey[%d,%d width=%d,height=%d]", pixelX0, pixelY0, pixelWidth, pixelHeight );
        }
    }

    private class CachedTileData extends TopographyData
    {
        private int[][] rgba;

        CachedTileData( ) throws IOException
        {
            super( null );
        }

        public CachedTileData( TopographyData src, int[][] rgba ) throws IOException
        {
            this( );
            this.data = null;
            this.rgba = rgba;
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
}
