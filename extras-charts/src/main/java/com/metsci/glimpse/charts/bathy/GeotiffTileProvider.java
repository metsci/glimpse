package com.metsci.glimpse.charts.bathy;

import static com.metsci.glimpse.util.units.Length.fromMeters;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Collections.unmodifiableCollection;

import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.media.jai.PlanarImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;

import com.metsci.glimpse.util.GlimpseDataPaths;

/**
 * Reads a Geotiff file in pieces and provides tiles.
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
    private static final int DEF_NUM_TILES_X = 30;
    private static final int DEF_NUM_TILES_Y = 15;

    private final GridCoverage2D topoData;
    private final Collection<TileKey> tileKeys;
    private final String attribution;

    public GeotiffTileProvider( GridCoverage2D grid, String attribution, int nTilesX, int nTilesY )
    {
        this.attribution = attribution;
        topoData = grid;
        tileKeys = createTileKeys( nTilesX, nTilesY );
    }

    protected Collection<TileKey> createTileKeys( int nTilesX, int nTilesY )
    {
        int[] nLonLat = topoData.getGridGeometry( ).getGridRange( ).getHigh( ).getCoordinateValues( );
        int pxWidth = nLonLat[0];
        int pxHeight = nLonLat[1];

        double px2Lon = 360.0 / pxWidth;
        double px2Lat = 180.0 / pxHeight;

        int tilePixelsX = pxWidth / nTilesX;
        int tilePixelsY = pxHeight / nTilesY;

        Collection<TileKey> keys = new ArrayList<>( );
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

                keys.add( new TileKey( minLat, maxLat, minLon, maxLon ) );
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
        return new GeoToolsTopoData( topoData, key );
    }

    @Override
    public String getAttribution( )
    {
        return attribution;
    }

    /**
     * To get the cached file
     *
     * <ol>
     * <li>Go to {@link #GEBCO2014_URL} and download the 2D NetCDF global grid</li>
     * <li>You'll need to setup an account to download.</li>
     * <li>Extract the zip and run {@code gdal_translate GEBCO_2014_2D.nc GEBCO_2014_2D.tif}</li>
     * <li>Put the geotiff file into {@link #GEBCO2014_CACHE_FILE} in your Glimpse user data dir</li>
     * </ol>
     */
    public static GeotiffTileProvider getGebco2014( ) throws IOException
    {
        File file = getCachedDataFile( GEBCO2014_CACHE_FILE, GEBCO2014_URL );
        GridCoverage2D grid = readGeotiff( file );
        return new GeotiffTileProvider( grid, GEBCO2014_ATTRIBUTION, DEF_NUM_TILES_X, DEF_NUM_TILES_Y );
    }

    /**
     * To get the cached file
     *
     * <ol>
     * <li>Go to {@link #ETOPO1_URL} and download the zip file</li>
     * <li>Extract the zip and the geotiff file into {@link #ETOPO1_CACHE_FILE} in your Glimpse user data dir</li>
     * </ol>
     */
    public static GeotiffTileProvider getEtopo1( ) throws IOException
    {
        File file = getCachedDataFile( ETOPO1_CACHE_FILE, ETOPO1_URL );
        GridCoverage2D grid = readGeotiff( file );
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

    public static GridCoverage2D readGeotiff( File file ) throws IOException
    {
        // initialize the EPSG CRS
        try
        {
            CRS.decode( "EPSG:4326" );
        }
        catch ( FactoryException ex )
        {
            throw new IOException( ex );
        }

        GeoTiffFormat format = new GeoTiffFormat( );
        GeoTiffReader reader = format.getReader( file );
        String[] names = reader.getGridCoverageNames( );
        GridCoverage2D grid = reader.read( names[0], null );
        reader.dispose( );

        return grid;
    }

    private class GeoToolsTopoData extends TopographyData
    {
        public GeoToolsTopoData( GridCoverage2D grid, TileKey key ) throws IOException
        {
            super( null );

            int[] nLonLat = grid.getGridGeometry( ).getGridRange( ).getHigh( ).getCoordinateValues( );
            widthStep = 360.0 / nLonLat[0];
            heightStep = 180.0 / nLonLat[1];

            startLon = key.minLon;
            startLat = key.minLat;

            int pixelX0 = ( int ) round( ( key.minLon + 180 ) / widthStep );
            int pixelX1 = ( int ) round( ( key.maxLon + 180 ) / widthStep );
            int pixelY0 = ( int ) round( ( 180 - ( key.maxLat + 90 ) ) / heightStep );
            int pixelY1 = ( int ) round( ( 180 - ( key.minLat + 90 ) ) / heightStep );

            imageWidth = pixelX1 - pixelX0 + 1;
            imageHeight = pixelY1 - pixelY0 + 1;

            data = new float[imageWidth][imageHeight];

            PlanarImage img = ( PlanarImage ) grid.getRenderedImage( );
            int tileX0 = img.XToTileX( pixelX0 );
            int tileX1 = img.XToTileX( pixelX1 );
            int tileY0 = img.YToTileY( pixelY0 );
            int tileY1 = img.YToTileY( pixelY1 );

            for ( int tileX = tileX0; tileX <= tileX1; tileX++ )
            {
                for ( int tileY = tileY0; tileY <= tileY1; tileY++ )
                {
                    Raster tile = img.getTile( tileX, tileY );
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

        @Override
        protected void read( InputStream in ) throws IOException
        {
            // nop
        }
    }
}
