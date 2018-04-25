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
import static com.metsci.glimpse.support.colormap.ColorGradients.bathymetry;
import static com.metsci.glimpse.support.colormap.ColorGradients.topography;
import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseUserCacheDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;
import static com.metsci.glimpse.util.units.Length.fromKilometers;
import static com.metsci.glimpse.util.units.Length.toMeters;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.media.jai.PlanarImage;
import javax.swing.SwingWorker;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.hsqldb.lib.DataOutputStream;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.texture.HeatMapPainter;
import com.metsci.glimpse.support.texture.mutator.ColorGradientConcatenator;
import com.metsci.glimpse.util.GlimpseDataPaths;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * @author borkholder
 */
public class Etopo1Painter extends DelegatePainter
{
    private static final Logger LOGGER = Logger.getLogger( Etopo1Painter.class.getName( ) );

    private static final String ETOPO_URL = "https://www.ngdc.noaa.gov/mgg/global/relief/ETOPO1/data/ice_surface/grid_registered/georeferenced_tiff/ETOPO1_Ice_g_geotiff.zip";

    private static final int[] HI_RES_CONTOURS = new int[] { -20, -50, -100, -250, -500, -1_000, -2_000, -4_000, -5_000, -6_000, -8_000, -10_000 };
    private static final int[] LOW_RES_CONTOURS = new int[] { -100, -500, -1_000, -5_000, -10_000 };
    private static final int[] MIN_CONTOURS = new int[] { -1_000, -5_000, -10_000 };

    private GeoProjection projection;
    private GridCoverage2D topoData;
    private Map<BathyTileKey, HeatMapPainter> bathyPainters;
    private Map<ContourTileKey, ContourPainter> contourPainters;
    private ColorTexture1D elevationHeatMapColors;

    private Rectangle2D.Double lastAxis;

    public Etopo1Painter( GeoProjection projection )
    {
        this.projection = projection;
        bathyPainters = new ConcurrentHashMap<>( );
        contourPainters = new ConcurrentHashMap<>( );
        lastAxis = new Rectangle2D.Double( 0, 0, 0, 0 );

        // create a color map which is half bathymetry color scale and half topography color scale
        elevationHeatMapColors = new ColorTexture1D( 1024 );
        elevationHeatMapColors.mutate( new ColorGradientConcatenator( bathymetry, topography ) );

        new SwingWorker<GridCoverage2D, Void>( )
        {
            @Override
            protected GridCoverage2D doInBackground( ) throws Exception
            {
                File file = getCachedDataFile( );
                GridCoverage2D grid = GeotiffTopoData.readGrid( file );
                return grid;
            }

            @Override
            protected void done( )
            {
                try
                {
                    topoData = get( );
                }
                catch ( InterruptedException | ExecutionException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }.execute( );
    }

    @Override
    public void paintTo( GlimpseContext context )
    {
        Axis2D axis = getAxis2D( context );

        if ( topoData == null )
        {
            return;
        }

        if ( lastAxis.getMinX( ) != axis.getMinX( ) ||
                lastAxis.getMaxX( ) != axis.getMaxX( ) ||
                lastAxis.getMinY( ) != axis.getMinY( ) ||
                lastAxis.getMaxY( ) != axis.getMaxY( ) )
        {
            lastAxis = new Rectangle2D.Double( axis.getMinX( ), axis.getMinY( ), axis.getMaxX( ) - axis.getMinX( ), axis.getMaxY( ) - axis.getMinY( ) );

            bathyPainters.values( ).forEach( p -> p.setVisible( false ) );
            contourPainters.values( ).forEach( p -> p.setVisible( false ) );

            int[] levels = getVisibleContourLevels( axis );
            Collection<BathyTileKey> keys = getVisibleTiles( axis );
            for ( BathyTileKey k : keys )
            {
                loadTile( k, levels );
            }
        }

        super.paintTo( context );
    }

    private Collection<BathyTileKey> getVisibleTiles( Axis2D axis )
    {
        PlanarImage img = ( PlanarImage ) topoData.getRenderedImage( );

        double spanX = axis.getMaxX( ) - axis.getMinX( );
        double spanY = axis.getMaxY( ) - axis.getMinY( );

        /*
         * TODO
         * These are specific to the ETOPO1 file
         */
        int nPixelsX = img.getTileWidth( ) / 10;
        int nPixelsY = img.getTileHeight( ) * 1_000;
        int nTilesX = img.getNumXTiles( ) * 10;

        Collection<BathyTileKey> keys = new HashSet<>( );

        /*
         * TODO
         * This is slow, but sure way to get all tiles in any projection.
         * Maybe there's a better way to do this.
         */
        int steps = 31;
        double xStep = spanX / ( steps - 1 );
        double yStep = spanY / ( steps - 1 );
        for ( int i = 0; i < steps; i++ )
        {
            for ( int j = 0; j < steps; j++ )
            {
                double x = axis.getMinX( ) - xStep / 2 + xStep * i;
                double y = axis.getMinY( ) - yStep / 2 + yStep * j;
                LatLonGeo ll = projection.unproject( x, y );
                int[] px = GeotiffTopoData.getPixelXY( topoData, ll );
                int tileX = px[0] / nPixelsX;
                int tileY = px[1] / nPixelsY;

                // Pad the image slightly to ensure contouring is ok, won't work at -180
                int pixelX0 = max( 0, tileX * nPixelsX - 2 );
                int pixelY0 = max( 0, tileY * nPixelsY - 2 );
                int pixelWidth = min( img.getWidth( ) - pixelX0, nPixelsX + 4 );
                int pixelHeight = min( img.getHeight( ) - pixelY0, nPixelsY + 4 );
                int tileIdx = tileY * nTilesX + tileX;
                keys.add( new BathyTileKey( tileIdx, pixelX0, pixelY0, pixelWidth, pixelHeight ) );
            }
        }

        return keys;
    }

    private int[] getVisibleContourLevels( Axis2D axis )
    {
        double span = min( axis.getMaxX( ) - axis.getMinX( ), axis.getMaxY( ) - axis.getMinY( ) );

        int[] levels;
        if ( span < fromKilometers( 200 ) )
        {
            levels = HI_RES_CONTOURS;
        }
        else if ( span < fromKilometers( 1_000 ) )
        {
            levels = LOW_RES_CONTOURS;
        }
        else
        {
            levels = MIN_CONTOURS;
        }

        return levels;
    }

    private synchronized void addPainter( BathyTileKey key, GlimpsePainter painter )
    {
        painter.setVisible( false );
        if ( painter instanceof HeatMapPainter )
        {
            bathyPainters.put( key, ( HeatMapPainter ) painter );
            super.addPainter( painter, 0 );
        }
        else if ( painter instanceof ContourPainter )
        {
            contourPainters.put( ( ContourTileKey ) key, ( ContourPainter ) painter );
            super.addPainter( painter );
        }
    }

    private void loadTile( BathyTileKey key, int[] contourLevels )
    {
        HeatMapPainter bathyPainter = bathyPainters.get( key );
        if ( bathyPainter == null )
        {
            bathyPainter = newBathyImagePainter( key );
        }

        bathyPainter.setVisible( true );

        for ( int lvl : contourLevels )
        {
            ContourTileKey ckey = new ContourTileKey( key, lvl );
            ContourPainter contourPainter = contourPainters.get( ckey );
            if ( contourPainter == null )
            {
                contourPainter = newContourPainter( ckey, lvl );
            }

            contourPainter.setVisible( true );
        }
    }

    private HeatMapPainter newBathyImagePainter( BathyTileKey key )
    {
        BathymetryData data = loadBathyTileData( key );

        Axis1D bathyAxis = new Axis1D( );
        bathyAxis.setMin( -10_000 );
        bathyAxis.setMax( +10_000 );
        HeatMapPainter p = new HeatMapPainter( bathyAxis );
        p.setData( data.getTexture( ) );
        p.setColorScale( elevationHeatMapColors );

        addPainter( key, p );

        return p;
    }

    private ContourPainter newContourPainter( BathyTileKey key, int level )
    {
        File cacheFile = new File( glimpseUserCacheDir, String.format( "etopo/contour_%d_%05d.bin", level, key.tileIdx ) );

        float[] x, y;
        try (RandomAccessFile rf = new RandomAccessFile( cacheFile, "r" ))
        {
            int nVertices = rf.readInt( );
            x = new float[nVertices];
            y = new float[nVertices];

            ByteBuffer bbuf = ByteBuffer.allocateDirect( nVertices * Float.BYTES );
            rf.getChannel( ).read( bbuf );
            bbuf.rewind( );
            bbuf.asFloatBuffer( ).get( x );
            rf.getChannel( ).read( bbuf );
            bbuf.rewind( );
            bbuf.asFloatBuffer( ).get( y );
        }
        catch ( IOException ex )
        {
            throw new RuntimeException( ex );
        }

        // create a painter to display the contour lines
        ContourPainter p = new ContourPainter( x, y );

        p.setLineColor( 0.8f, 0.8f, 0.8f, 1f );
        p.setLineWidth( 0.7f );

        addPainter( key, p );

        return p;
    }

    private BathymetryData loadBathyTileData( BathyTileKey key )
    {
        BathymetryData data = null;
        File cacheFile = new File( glimpseUserCacheDir, String.format( "etopo/tile_%05d.bin", key.tileIdx ) );
        if ( cacheFile.isFile( ) )
        {
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
            data = GeotiffTopoData.getTile( topoData, projection, key );
            try (OutputStream os = new BufferedOutputStream( new FileOutputStream( cacheFile ) ))
            {
                CachedTopoData.write( os, data );
            }
            catch ( IOException ex )
            {
                throw new RuntimeException( ex );
            }

            BathymetryData bathyData = data;
            IntStream.of( HI_RES_CONTOURS ).parallel( )
                    .forEach( lvl -> computeAndWriteContour( key, bathyData, lvl ) );
        }

        return data;
    }

    private void computeAndWriteContour( BathyTileKey key, BathymetryData data, int level )
    {
        logInfo( LOGGER, "Computing contour for level %.0fm", toMeters( level ) );
        long t0 = currentTimeMillis( );
        ContourData contourData = new ContourData( data, projection, new double[] { level } );
        logInfo( LOGGER, "Computed contour of %,d points in %,dms", contourData.getCoordsX( ).length, currentTimeMillis( ) - t0 );

        File cacheFile = new File( glimpseUserCacheDir, String.format( "etopo/contour_%d_%05d.bin", level, key.tileIdx ) );
        float[] x = contourData.getCoordsX( );
        float[] y = contourData.getCoordsY( );

        int nBytes = x.length * 2 * Float.BYTES + Integer.BYTES;
        ByteBuffer bbuf = ByteBuffer.allocateDirect( nBytes );

        bbuf.clear( );
        bbuf.asIntBuffer( ).put( x.length );
        bbuf.position( bbuf.position( ) + Integer.BYTES );
        bbuf.asFloatBuffer( ).put( x ).put( y );
        bbuf.rewind( );
        bbuf.limit( nBytes );

        try (FileChannel fc = FileChannel.open( cacheFile.toPath( ), WRITE, CREATE, TRUNCATE_EXISTING ))
        {
            fc.write( bbuf );
        }
        catch ( IOException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    private File getCachedDataFile( ) throws IOException
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
        final int tileIdx;
        final int pixelX0;
        final int pixelY0;
        final int pixelWidth;
        final int pixelHeight;

        BathyTileKey( int tileIdx, int pixelX0, int pixelY0, int pixelWidth, int pixelHeight )
        {
            this.tileIdx = tileIdx;
            this.pixelX0 = pixelX0;
            this.pixelY0 = pixelY0;
            this.pixelWidth = pixelWidth;
            this.pixelHeight = pixelHeight;
        }

        @Override
        public int hashCode( )
        {
            return tileIdx;
        }

        @Override
        public boolean equals( Object obj )
        {
            return obj instanceof BathyTileKey && ( ( BathyTileKey ) obj ).tileIdx == tileIdx;
        }
    }

    private static class ContourTileKey extends BathyTileKey
    {
        final int contourIdx;

        ContourTileKey( BathyTileKey parent, int level )
        {
            super( parent.tileIdx, parent.pixelX0, parent.pixelY0, parent.pixelWidth, parent.pixelHeight );
            contourIdx = tileIdx * 100_000 + ( level + 20_000 );
        }

        @Override
        public int hashCode( )
        {
            return contourIdx;
        }

        @Override
        public boolean equals( Object obj )
        {
            return obj instanceof ContourTileKey && ( ( ContourTileKey ) obj ).contourIdx == contourIdx;
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
                        data[x][y] = ( float ) v;
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

        public static int[] getPixelXY( GridCoverage2D grid, LatLonGeo ll )
        {
            try
            {
                DirectPosition2D pos = new DirectPosition2D( ll.getLonDeg( ), ll.getLatDeg( ) );
                GridCoordinates2D coord = grid.getGridGeometry( ).worldToGrid( pos );
                return new int[] { coord.x, coord.y };
            }
            catch ( InvalidGridGeometryException | TransformException ex )
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
