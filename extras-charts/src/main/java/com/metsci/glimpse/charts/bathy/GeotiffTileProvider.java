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

import static com.metsci.glimpse.util.units.Length.fromKilometers;
import static com.metsci.glimpse.util.units.Length.fromMeters;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Collections.unmodifiableCollection;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.metsci.glimpse.core.painter.geo.TileKey;
import com.metsci.glimpse.util.GlimpseDataPaths;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Reads a Geotiff file in pieces and provides tiles.
 *
 * <p>
 * Note: An assumption of this tile provider is that the TIFF is equirectangular
 * and covers the entire globe starting at -90 Latitude and -180 Longitude.
 * </p>
 *
 * @author borkholder
 */
public class GeotiffTileProvider implements TopoTileProvider
{
    public static final String ETOPO1_URL = "https://www.ngdc.noaa.gov/mgg/global/relief/ETOPO1/data/ice_surface/grid_registered/georeferenced_tiff/ETOPO1_Ice_g_geotiff.zip";
    public static final String ETOPO1_CACHE_FILE = "ETOPO1_Ice_g_geotiff.tif";
    public static final String ETOPO1_ATTRIBUTION = "ETOPO1 Global Relief Model - NOAA";
    public static final String GEBCO2014_URL = "https://www.gebco.net/data_and_products/gridded_bathymetry_data/gebco_30_second_grid/";
    public static final String GEBCO2014_CACHE_FILE = "GEBCO_2014_2D.tif";
    public static final String GEBCO2014_ATTRIBUTION = "The GEBCO_2014 Grid, version 20150318, www.gebco.net";
    public static final int DEF_NUM_TILES_X = 30;
    public static final int DEF_NUM_TILES_Y = 15;
    public static final double GLOBAL_TILE_LEVEL = fromKilometers( 2_000 );

    private final RenderedImage topoData;
    private final Collection<TileKey> tileKeys;
    private final String attribution;

    public GeotiffTileProvider( RenderedImage grid, String attribution, int nTilesX, int nTilesY )
    {
        this.attribution = attribution;
        topoData = grid;
        tileKeys = createTileKeys( nTilesX, nTilesY );
    }

    protected Collection<TileKey> createTileKeys( int nTilesX, int nTilesY )
    {
        int pxWidth = topoData.getWidth( );
        int pxHeight = topoData.getHeight( );

        double px2Lon = 360.0 / pxWidth;
        double px2Lat = 180.0 / pxHeight;

        int tilePixelsX = pxWidth / nTilesX;
        int tilePixelsY = pxHeight / nTilesY;

        Collection<TileKey> keys = new ArrayList<>( );
        for ( int lat = -90; lat < 90; lat += 30 )
        {
            for ( int lon = -180; lon < 180; lon += 30 )
            {
                keys.add( new TileKey( GLOBAL_TILE_LEVEL, lat, lat + 30, lon, lon + 30 ) );
            }
        }

        for ( int pxX = 0; pxX < pxWidth; pxX += tilePixelsX )
        {
            for ( int pxY = 0; pxY < pxHeight; pxY += tilePixelsY )
            {
                int pixelX0 = max( 0, pxX - 2 );
                int pixelY0 = max( 0, pxY - 2 );
                int pixelX1 = min( pxWidth, pxX + tilePixelsX + 2 );
                int pixelY1 = min( pxHeight, pxY + tilePixelsY + 2 );

                double minLat = pixelY0 * px2Lat - 90;
                double minLon = pixelX0 * px2Lon - 180;
                double maxLat = pixelY1 * px2Lat - 90;
                double maxLon = pixelX1 * px2Lon - 180;

                keys.add( new TileKey( 0, minLat, maxLat, minLon, maxLon ) );
            }
        }

        return keys;
    }

    @Override
    public Collection<TileKey> keys( )
    {
        return unmodifiableCollection( tileKeys );
    }

    @Override
    public TopographyData getTile( TileKey key ) throws IOException
    {
        if ( key.lengthScale == GLOBAL_TILE_LEVEL )
        {
            return getTile( key, 10 );
        }
        else
        {
            return new GeoToolsTopoData( topoData, key );
        }
    }

    /**
     * Read the topography tile data with a specified stride of the underlying data.
     */
    public TopographyData getTile( TileKey key, int dataStride ) throws IOException
    {
        if ( dataStride > 1 )
        {
            return new GeoToolsSampledGlobalTopoData( topoData, key, dataStride );
        }
        else
        {
            return new GeoToolsTopoData( topoData, key );
        }
    }


    @Override
    public String getAttribution( )
    {
        return attribution;
    }

    public static GeotiffTileProvider getGlobalGeoTiff( File file, String attribution ) throws IOException
    {
        RenderedImage grid = readGeotiff( file );
        return new GeotiffTileProvider( grid, attribution, DEF_NUM_TILES_X, DEF_NUM_TILES_Y );
    }

    /**
     * To get the cached file
     *
     * <ol>
     * <li>Go to {@link #GEBCO2014_URL} and download the 2D NetCDF global grid</li>
     * <li>You'll need to setup an account to download.</li>
     * <li>Extract the zip and run
     * {@code gdal_translate GEBCO_2014_2D.nc GEBCO_2014_2D.tif}</li>
     * <li>Put the geotiff file into {@link #GEBCO2014_CACHE_FILE} in your Glimpse
     * user data dir</li>
     * </ol>
     */
    public static GeotiffTileProvider getGebco2014( ) throws IOException
    {
        File file = getCachedDataFile( GEBCO2014_CACHE_FILE, GEBCO2014_URL );
        RenderedImage grid = readGeotiff( file );
        return new GeotiffTileProvider( grid, GEBCO2014_ATTRIBUTION, DEF_NUM_TILES_X, DEF_NUM_TILES_Y );
    }

    /**
     * To get the cached file
     *
     * <ol>
     * <li>Go to {@link #ETOPO1_URL} and download the zip file</li>
     * <li>Extract the zip and the geotiff file into {@link #ETOPO1_CACHE_FILE} in
     * your Glimpse user data dir</li>
     * </ol>
     */
    public static GeotiffTileProvider getEtopo1( ) throws IOException
    {
        File file = getCachedDataFile( ETOPO1_CACHE_FILE, ETOPO1_URL );
        RenderedImage grid = readGeotiff( file );
        return new GeotiffTileProvider( grid, ETOPO1_ATTRIBUTION, DEF_NUM_TILES_X, DEF_NUM_TILES_Y );
    }

    public static File getCachedDataFile( String cacheFile, String topoUrl ) throws IOException
    {
        File file = new File( GlimpseDataPaths.glimpseSharedDataDir, cacheFile );
        if ( file.isFile( ) )
        {
            return file;
        }

        file = new File( GlimpseDataPaths.glimpseUserDataDir, cacheFile );
        if ( file.isFile( ) )
        {
            return file;
        }

        throw new IOException( "Must download data from " + topoUrl );
    }

    public static RenderedImage readGeotiff( File file ) throws IOException
    {
        ImageInputStream iis = ImageIO.createImageInputStream( file );
        ImageReader reader = ImageIO.getImageReaders( iis ).next( );
        reader.setInput( iis );
        return reader.readAsRenderedImage( 0, null );
    }

    private static int XToTileX( RenderedImage img, int x )
    {
        x -= img.getTileGridXOffset( );
        if ( x < 0 )
        {
            x += 1 - img.getTileWidth( );
        }

        return ( x / img.getTileWidth( ) );
    }

    private static int YToTileY( RenderedImage img, int y )
    {
        y -= img.getTileGridYOffset( );
        if ( y < 0 )
        {
            y += 1 - img.getTileHeight( );
        }
        return ( y / img.getTileHeight( ) );
    }

    public static class GeoToolsSampledGlobalTopoData extends TopographyData
    {
        public GeoToolsSampledGlobalTopoData( RenderedImage grid, TileKey key, int decimate ) throws IOException
        {
            widthStep = 360.0 / grid.getWidth( ) * decimate;
            heightStep = 180.0 / grid.getHeight( ) * decimate;

            startLon = key.minLon;
            startLat = key.minLat;

            double lon2px = grid.getWidth( ) / 360.0;
            double lat2px = grid.getHeight( ) / 180.0;
            int pixelX0 = ( int ) round( ( key.minLon + 180 ) * lon2px );
            int pixelX1 = ( int ) round( ( key.maxLon + 180 ) * lon2px );
            int pixelY0 = ( int ) round( ( 180 - ( key.maxLat + 90 ) ) * lat2px );
            int pixelY1 = ( int ) round( ( 180 - ( key.minLat + 90 ) ) * lat2px );
            pixelY1 = min( pixelY1, grid.getHeight( ) - 1 );
            pixelX1 = min( pixelX1, grid.getWidth( ) - 1 );

            imageWidth = ( int ) ceil( ( pixelX1 - pixelX0 + 1 ) / ( double ) decimate );
            imageHeight = ( int ) ceil( ( pixelY1 - pixelY0 + 1 ) / ( double ) decimate );

            data = new float[imageWidth][imageHeight];

            Int2ObjectMap<Raster> tileCache = new Int2ObjectOpenHashMap<>( );
            for ( int j = pixelY0; j <= pixelY1; j += decimate )
            {
                int tileY = YToTileY( grid, j );
                for ( int i = pixelX0; i <= pixelX1; i += decimate )
                {
                    int tileX = XToTileX( grid, i );

                    int k = tileX * grid.getNumYTiles( ) + tileY;
                    Raster tile = tileCache.get( k );
                    if ( tile == null )
                    {
                        tile = grid.getTile( tileX, tileY );
                        tileCache.put( k, tile );
                    }

                    double v = tile.getSampleDouble( i, j, 0 );
                    int x = ( i - pixelX0 ) / decimate;
                    int y = imageHeight - ( j - pixelY0 ) / decimate - 1;
                    data[x][y] = ( float ) fromMeters( v );
                }
            }
        }
    }

    public static class GeoToolsTopoData extends TopographyData
    {
        public GeoToolsTopoData( RenderedImage grid, TileKey key ) throws IOException
        {
            widthStep = 360.0 / grid.getWidth( );
            heightStep = 180.0 / grid.getHeight( );

            startLon = key.minLon;
            startLat = key.minLat;

            int pixelX0 = ( int ) round( ( key.minLon + 180 ) / widthStep );
            int pixelX1 = ( int ) round( ( key.maxLon + 180 ) / widthStep );
            int pixelY0 = ( int ) round( ( 180 - ( key.maxLat + 90 ) ) / heightStep );
            int pixelY1 = ( int ) round( ( 180 - ( key.minLat + 90 ) ) / heightStep );
            pixelY1 = min( pixelY1, grid.getHeight( ) - 1 );
            pixelX1 = min( pixelX1, grid.getWidth( ) - 1 );

            imageWidth = pixelX1 - pixelX0 + 1;
            imageHeight = pixelY1 - pixelY0 + 1;

            data = new float[imageWidth][imageHeight];

            int tileX0 = XToTileX( grid, pixelX0 );
            int tileX1 = XToTileX( grid, pixelX1 );
            int tileY0 = YToTileY( grid, pixelY0 );
            int tileY1 = YToTileY( grid, pixelY1 );

            for ( int tileX = tileX0; tileX <= tileX1; tileX++ )
            {
                for ( int tileY = tileY0; tileY <= tileY1; tileY++ )
                {
                    Raster tile = grid.getTile( tileX, tileY );
                    int minX = max( pixelX0, tile.getMinX( ) );
                    int maxX = min( pixelX1, tile.getMinX( ) + tile.getWidth( ) - 1 );
                    int minY = max( pixelY0, tile.getMinY( ) );
                    int maxY = min( pixelY1, tile.getMinY( ) + tile.getHeight( ) - 1 );

                    for ( int i = minX; i <= maxX; i++ )
                    {
                        for ( int j = minY; j <= maxY; j++ )
                        {
                            double v = tile.getSampleDouble( i, j, 0 );
                            int x = i - pixelX0;
                            int y = imageHeight - ( j - pixelY0 ) - 1;
                            data[x][y] = ( float ) fromMeters( v );
                        }
                    }
                }
            }
        }
    }
}
