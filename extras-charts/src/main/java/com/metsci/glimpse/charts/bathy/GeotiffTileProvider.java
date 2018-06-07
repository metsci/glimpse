package com.metsci.glimpse.charts.bathy;

import static com.metsci.glimpse.util.units.Length.fromMeters;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
    public static final String ETOPO1_CACHE_FILE = "etopo/ETOPO1_Ice_g_geotiff.tif";
    public static final String ETOPO1_ATTRIBUTION = "ETOPO1 Global Relief Model - NOAA";
    public static final String GEBCO2014_URL = "https://www.gebco.net/data_and_products/gridded_bathymetry_data/gebco_30_second_grid/";
    public static final String GEBCO2014_CACHE_FILE = "GEBCO/GEBCO_2014_2D.tif";
    public static final String GEBCO2014_ATTRIBUTION = "The GEBCO_2014 Grid, version 20150318, www.gebco.net";

    private final GridCoverage2D topoData;
    private final int width;
    private final int height;
    private final String attribution;

    public GeotiffTileProvider( GridCoverage2D grid, String attribution )
    {
        this.attribution = attribution;
        topoData = grid;
        int[] nLonLat = grid.getGridGeometry( ).getGridRange( ).getHigh( ).getCoordinateValues( );
        width = nLonLat[0];
        height = nLonLat[1];
    }

    @Override
    public int getPixelsX( )
    {
        return width;
    }

    @Override
    public int getPixelsY( )
    {
        return height;
    }

    @Override
    public TopographyData getTile( int pixelX0, int pixelY0, int pixelWidth, int pixelHeight ) throws IOException
    {
        return new GeoToolsTopoData( topoData, pixelX0, pixelY0, pixelWidth, pixelHeight );
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
        return new GeotiffTileProvider( grid, GEBCO2014_ATTRIBUTION );
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
        return new GeotiffTileProvider( grid, ETOPO1_ATTRIBUTION );
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
        public GeoToolsTopoData( GridCoverage2D grid, int pixelX0, int pixelY0, int pixelWidth, int pixelHeight ) throws IOException
        {
            super( null );

            widthStep = 360.0 / width;
            heightStep = 180.0 / height;

            startLon = pixelX0 * widthStep - 180;
            startLat = 90 - ( pixelY0 + pixelHeight ) * heightStep;

            imageWidth = pixelWidth;
            imageHeight = pixelHeight;

            data = new float[imageWidth][imageHeight];

            PlanarImage img = ( PlanarImage ) grid.getRenderedImage( );
            for ( int tileY = pixelY0; tileY < pixelY0 + pixelHeight; tileY++ )
            {
                int y = pixelHeight - ( tileY - pixelY0 ) - 1;
                for ( int tileX = img.getMinTileX( ); tileX <= img.getMaxTileX( ); tileX++ )
                {
                    Raster tile = img.getTile( tileX, tileY );
                    int minX = max( pixelX0, tile.getMinX( ) );
                    int maxX = min( pixelX0 + pixelWidth - 1, tile.getMinX( ) + tile.getWidth( ) - 1 );

                    for ( int i = minX; i <= maxX; i++ )
                    {
                        double v = tile.getSampleDouble( i, tile.getMinY( ), 0 );
                        int x = i - pixelX0;
                        data[x][y] = ( float ) -fromMeters( v );
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
