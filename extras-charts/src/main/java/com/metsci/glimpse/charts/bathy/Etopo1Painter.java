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
import static com.metsci.glimpse.util.GeneralUtils.clamp;
import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseUserCacheDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static com.metsci.glimpse.util.units.Angle.fromDeg;
import static com.metsci.glimpse.util.units.Length.fromMeters;
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
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.media.jai.PlanarImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.referencing.CRS;
import org.hsqldb.lib.DataOutputStream;
import org.opengis.referencing.FactoryException;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.DrawableTexture;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.support.PainterCache;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D.MutatorByte2D;
import com.metsci.glimpse.support.texture.RGBATextureProjected2D;
import com.metsci.glimpse.util.GlimpseDataPaths;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * @author borkholder
 */
public class Etopo1Painter extends DelegatePainter
{
    private static final Logger LOGGER = Logger.getLogger( Etopo1Painter.class.getName( ) );

    private static final long VERSION_ID = 1;
    private static final double COS_LIGHT_ZENITH = cos( fromDeg( 45 ) );
    private static final double SIN_LIGHT_ZENITH = sin( fromDeg( 45 ) );
    private static final double LIGHT_AZIMUTH = fromDeg( -135 );
    private static final float BATHY_HUE = 0.63f;

    public static final String ETOPO_URL = "https://www.ngdc.noaa.gov/mgg/global/relief/ETOPO1/data/ice_surface/grid_registered/georeferenced_tiff/ETOPO1_Ice_g_geotiff.zip";

    private GeoProjection projection;
    private GridCoverage2D topoData;
    private Map<BathyTileKey, Area> tileBounds;

    private PainterCache<BathyTileKey, DrawableTexture> bathyTextures;
    private ShadedTexturePainter bathyImagePainter;
    private Rectangle2D.Double lastAxis;

    private Executor executor;

    public Etopo1Painter( GeoProjection projection, Axis1D bathyAxis )
    {
        this.projection = projection;
        this.executor = newFixedThreadPool( clamp( getRuntime( ).availableProcessors( ) - 2, 1, 3 ) );
        bathyTextures = new PainterCache<>( this::newBathyTexture, executor );
        lastAxis = new Rectangle2D.Double( );

        bathyImagePainter = new ShadedTexturePainter( );
        addPainter( bathyImagePainter );

        executor.execute( this::initializeBathySourceData );
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

        if ( topoData == null )
        {
            return;
        }

        if ( tileBounds == null )
        {
            tileBounds = createTileKeys( topoData );
        }

        if ( lastAxis.getMinX( ) != axis.getMinX( ) ||
                lastAxis.getMaxX( ) != axis.getMaxX( ) ||
                lastAxis.getMinY( ) != axis.getMinY( ) ||
                lastAxis.getMaxY( ) != axis.getMaxY( ) )
        {
            lastAxis = new Rectangle2D.Double( axis.getMinX( ), axis.getMinY( ), axis.getMaxX( ) - axis.getMinX( ), axis.getMaxY( ) - axis.getMinY( ) );

            Collection<BathyTileKey> tiles = getVisibleTiles( lastAxis );

            bathyImagePainter.removeAllDrawableTextures( );
            boolean anyMissed = false;
            for ( BathyTileKey key : tiles )
            {
                DrawableTexture tex = bathyTextures.get( key );
                if ( tex == null )
                {
                    anyMissed = true;
                }
                else
                {
                    bathyImagePainter.addDrawableTexture( tex );
                }
            }

            if ( anyMissed )
            {
                lastAxis = new Rectangle2D.Double( );
            }
        }
    }

    private Map<BathyTileKey, Area> createTileKeys( GridCoverage2D grid )
    {
        PlanarImage img = ( PlanarImage ) grid.getRenderedImage( );
        int[] nLonLat = grid.getGridGeometry( ).getGridRange( ).getHigh( ).getCoordinateValues( );
        double px2Lon = 360.0 / nLonLat[0];
        double px2Lat = 180.0 / nLonLat[1];

        int tilePixelsX = img.getWidth( ) / 30;
        int tilePixelsY = img.getHeight( ) / 15;

        Map<BathyTileKey, Area> keys = new HashMap<>( );
        for ( int pxX = 0; pxX < img.getWidth( ); pxX += tilePixelsX )
        {
            for ( int pxY = 0; pxY < img.getHeight( ); pxY += tilePixelsY )
            {
                int pixelX0 = max( 0, pxX - 2 );
                int pixelY0 = max( 0, pxY - 2 );
                int pixelWidth = min( img.getWidth( ) - pixelX0, tilePixelsX + 4 );
                int pixelHeight = min( img.getHeight( ) - pixelY0, tilePixelsY + 4 );
                BathyTileKey key = new BathyTileKey( pixelX0, pixelY0, pixelWidth, pixelHeight );

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

    private Collection<BathyTileKey> getVisibleTiles( Rectangle2D bounds )
    {
        // Pad for irregular projections
        double padX = bounds.getWidth( ) * 0.02;
        double padY = bounds.getHeight( ) * 0.02;
        bounds = new Rectangle2D.Double( bounds.getMinX( ) - padX, bounds.getMinY( ) - padY, bounds.getWidth( ) + 2 * padX, bounds.getHeight( ) + 2 * padY );

        Collection<BathyTileKey> keys = new ArrayList<>( );
        for ( Entry<BathyTileKey, Area> e : tileBounds.entrySet( ) )
        {
            if ( e.getValue( ).intersects( bounds ) )
            {
                keys.add( e.getKey( ) );
            }
        }

        return keys;
    }

    private DrawableTexture newBathyTexture( BathyTileKey key )
    {
        BathymetryData tile = loadBathyTileData( key );
        float[][] hill = hillshade( tile );

        RGBATextureProjected2D texture = new RGBATextureProjected2D( tile.getImageWidth( ), tile.getImageHeight( ) );
        texture.setProjection( tile.getProjection( ) );

        texture.mutate( new MutatorByte2D( )
        {
            @Override
            public void mutate( ByteBuffer data, int dataSizeX, int dataSizeY )
            {
                for ( int r = 0; r < dataSizeY; r++ )
                {
                    for ( int c = 0; c < dataSizeX; c++ )
                    {
                        float h = clamp( ( hill[c][r] - 0.4f ) / 0.5f, 0, 1 );
                        float bri = 0.1f + 0.9f * h;
                        float sat = 0.5f + 0.1f * ( 1 - h );
                        int rgb = Color.HSBtoRGB( BATHY_HUE, sat, bri );
                        data.put( ( byte ) ( rgb >> 16 & 0xff ) );
                        data.put( ( byte ) ( rgb >> 8 & 0xff ) );
                        data.put( ( byte ) ( rgb >> 0 & 0xff ) );
                        data.put( ( byte ) 255 );
                    }
                }
            }
        } );

        return texture;
    }

    private void initializeBathySourceData( )
    {
        try
        {
            File file = getCachedDataFile( );
            topoData = GeotiffTopoData.readGrid( file );
        }
        catch ( IOException ex )
        {
            logWarning( LOGGER, "Could not load TOPO1 data", ex );
        }
    }

    private BathymetryData loadBathyTileData( BathyTileKey key )
    {
        BathymetryData data = null;
        File cacheFile = new File( glimpseUserCacheDir, String.format( "etopo/tile_%s.bin", key.id ) );
        if ( cacheFile.isFile( ) )
        {
            logFine( LOGGER, "Loading cached bathy tile from %s", cacheFile );
            try (InputStream is = new BufferedInputStream( new FileInputStream( cacheFile ) ))
            {
                data = new CachedTopoData( is, projection );
            }
            catch ( IOException ex )
            {
                throw new RuntimeException( ex );
            }
        }

        if ( data == null )
        {
            cacheFile.getParentFile( ).mkdirs( );
            logFine( LOGGER, "Building bathy tile for %s", key );
            data = GeotiffTopoData.getTile( topoData, projection, key );
            logFine( LOGGER, "Writing cached bathy tile to %s", cacheFile );
            try (OutputStream os = new BufferedOutputStream( new FileOutputStream( cacheFile ) ))
            {
                CachedTopoData.write( os, data );
            }
            catch ( IOException ex )
            {
                throw new RuntimeException( ex );
            }
        }

        return data;
    }

    private float[][] hillshade( BathymetryData data )
    {
        float[][] hill = new float[data.imageWidth][data.imageHeight];

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
                hill[x][y] = hillshade0( data.data, x, y, dx, dy );
            }

            hill[x][0] = hill[x][1];
            hill[x][data.imageHeight - 1] = hill[x][data.imageHeight - 2];
        }

        System.arraycopy( hill[1], 0, hill[0], 0, data.imageHeight );
        System.arraycopy( hill[data.imageWidth - 2], 0, hill[data.imageWidth - 1], 0, data.imageHeight );

        return hill;
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
        double dzdx = ( ( 3 * a + 10 * d + 3 * g ) - ( 3 * c + 10 * f + 3 * i ) ) / ( 32 * dx );
        double dzdy = ( ( 3 * a + 10 * b + 3 * c ) - ( 3 * g + 10 * h + 3 * i ) ) / ( 32 * dy );
        double slope = atan( sqrt( dzdx * dzdx ) + ( dzdy * dzdy ) );
        double aspect = atan2( dzdy, -dzdx );

        double hillshade = ( COS_LIGHT_ZENITH * cos( slope ) ) + ( SIN_LIGHT_ZENITH * sin( slope ) * cos( LIGHT_AZIMUTH - aspect ) );
        return ( float ) hillshade;
    }

    public static File getCachedDataFile( ) throws IOException
    {
        String name = "etopo/ETOPO1_Ice_g_geotiff.tif";
        File file = new File( GlimpseDataPaths.glimpseSharedDataDir, name );
        if ( file.isFile( ) )
        {
            return file;
        }

        file = new File( GlimpseDataPaths.glimpseUserDataDir, name );
        if ( file.isFile( ) )
        {
            return file;
        }

        file = new File( GlimpseDataPaths.glimpseUserCacheDir, name );
        if ( file.isFile( ) )
        {
            return file;
        }

        throw new IOException( "Must download data from " + ETOPO_URL );
    }

    private static class BathyTileKey
    {
        final String id;
        final int pixelX0;
        final int pixelY0;
        final int pixelWidth;
        final int pixelHeight;

        BathyTileKey( int pixelX0, int pixelY0, int pixelWidth, int pixelHeight )
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
            if ( obj instanceof BathyTileKey )
            {
                BathyTileKey other = ( BathyTileKey ) obj;
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
            return String.format( "BathyTileKey[%d,%d width=%d,height=%d]", pixelX0, pixelY0, pixelWidth, pixelHeight );
        }
    }

    private static class GeotiffTopoData extends BathymetryData
    {
        public GeotiffTopoData( GridCoverage2D grid, GeoProjection projection, int pixelX0, int pixelY0, int pixelWidth, int pixelHeight ) throws IOException
        {
            super( null, projection );

            int nLon = grid.getGridGeometry( ).getGridRange( ).getHigh( 0 );
            int nLat = grid.getGridGeometry( ).getGridRange( ).getHigh( 1 );
            widthStep = 360.0 / nLon;
            heightStep = 180.0 / nLat;

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
                        data[x][y] = ( float ) fromMeters( v );
                    }
                }
            }
        }

        public static GeotiffTopoData getTile( GridCoverage2D grid, GeoProjection projection, BathyTileKey key )
        {
            try
            {
                return new GeotiffTopoData( grid, projection, key.pixelX0, key.pixelY0, key.pixelWidth, key.pixelHeight );
            }
            catch ( IOException ex )
            {
                throw new RuntimeException( ex );
            }
        }

        public static GridCoverage2D readGrid( File file ) throws IOException
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

        @Override
        protected void read( InputStream in, GeoProjection tp ) throws IOException
        {
            // nop
        }
    }

    private static class CachedTopoData extends BathymetryData
    {
        public CachedTopoData( InputStream in, GeoProjection projection ) throws IOException
        {
            super( in, projection );
        }

        @Override
        protected void read( InputStream in, GeoProjection tp ) throws IOException
        {
            DataInputStream is = new DataInputStream( in );

            imageWidth = is.readInt( );
            imageHeight = is.readInt( );
            startLat = is.readDouble( );
            startLon = is.readDouble( );
            widthStep = is.readDouble( );
            heightStep = is.readDouble( );

            data = new float[imageWidth][imageHeight];
            for ( int i = 0; i < imageWidth; i++ )
            {
                for ( int j = 0; j < imageHeight; j++ )
                {
                    data[i][j] = is.readShort( );
                }
            }
        }

        public static void write( OutputStream out, BathymetryData data ) throws IOException
        {
            @SuppressWarnings( "resource" )
            DataOutputStream os = new DataOutputStream( out );

            os.writeInt( data.getImageWidth( ) );
            os.writeInt( data.getImageHeight( ) );
            os.writeDouble( data.getStartLat( ) );
            os.writeDouble( data.getStartLon( ) );
            os.writeDouble( data.getWidthStep( ) );
            os.writeDouble( data.getHeightStep( ) );

            float[][] vals = data.getData( );
            for ( int i = 0; i < data.getImageWidth( ); i++ )
            {
                for ( int j = 0; j < data.getImageHeight( ); j++ )
                {
                    os.writeShort( ( short ) vals[i][j] );
                }
            }

            os.flush( );
        }
    }
}
