package com.metsci.glimpse.charts.shoreline;

import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;
import static java.lang.Math.toDegrees;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.primitives.Floats;
import com.metsci.glimpse.charts.bathy.TileKey;
import com.metsci.glimpse.charts.bathy.TilePainter;
import com.metsci.glimpse.painter.shape.PolygonPainter;
import com.metsci.glimpse.painter.shape.PolygonPainter.TessellatedPolygon;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

public class ShorelineTilePainter extends TilePainter<TessellatedPolygon[]>
{
    private static final Logger LOGGER = Logger.getLogger( ShorelineTilePainter.class.getName( ) );

    protected final File file;
    protected final Map<TileKey, Long> keys;
    protected final PolygonPainter painter;

    private Set<TileKey> loadedGroups;

    public ShorelineTilePainter( GeoProjection projection, File file ) throws IOException
    {
        super( projection );
        this.file = file;
        painter = new PolygonPainter( );
        painter.displayTimeRange( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
        addPainter( painter );
        loadedGroups = new HashSet<>( );
        keys = loadOffsets( file );
        logInfo( LOGGER, "Found %,d files in %s", keys.size( ), file );
    }

    protected Map<TileKey, Long> loadOffsets( File file ) throws IOException
    {
        try (RandomAccessFile shorelineTileFile = new RandomAccessFile( file, "r" ))
        {
            Map<TileKey, Long> offsets = new HashMap<>( );
            long len = shorelineTileFile.length( );
            while ( shorelineTileFile.getFilePointer( ) < len )
            {
                long pos = shorelineTileFile.getFilePointer( );
                float minLat = ( float ) toDegrees( shorelineTileFile.readFloat( ) );
                float maxLat = ( float ) toDegrees( shorelineTileFile.readFloat( ) );
                float minLon = ( float ) toDegrees( shorelineTileFile.readFloat( ) );
                float maxLon = ( float ) toDegrees( shorelineTileFile.readFloat( ) );
                offsets.put( new TileKey( minLat, maxLat, minLon, maxLon ), pos );

                int numPolys = shorelineTileFile.readInt( );
                for ( int i = 0; i < numPolys; i++ )
                {
                    int numFloats = shorelineTileFile.readInt( );
                    shorelineTileFile.seek( shorelineTileFile.getFilePointer( ) + numFloats * Floats.BYTES );
                }
            }

            return offsets;
        }
    }

    @Override
    protected TessellatedPolygon[] loadTileData( TileKey key )
    {
        try (RandomAccessFile rf = new RandomAccessFile( file, "r" ))
        {
            return readTile( rf, key );
        }
        catch ( IOException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    protected TessellatedPolygon[] readTile( RandomAccessFile rf, TileKey key ) throws IOException
    {
        long offset = keys.get( key );
        logFine( LOGGER, "Reading tile at offset %,d", offset );
        rf.seek( offset + 4 * Float.BYTES );

        int numPolys = rf.readInt( );
        logFine( LOGGER, "Reading %,d polygons in %s", numPolys, key );
        TessellatedPolygon[] polys = new TessellatedPolygon[numPolys];

        ByteBuffer buf = null;
        for ( int i = 0; i < numPolys; i++ )
        {
            int numFloats = rf.readInt( );
            if ( buf == null || buf.capacity( ) < numFloats * Float.BYTES )
            {
                buf = ByteBuffer.allocate( numFloats * Float.BYTES );
            }

            buf.limit( numFloats * Float.BYTES );
            buf.rewind( );
            rf.getChannel( ).read( buf );
            buf.rewind( );
            FloatBuffer fbuf = buf.asFloatBuffer( );
            float[] verts = new float[fbuf.limit( )];
            fbuf.get( verts );

            for ( int j = 0; j < verts.length; j += 2 )
            {
                float lat = verts[j];
                float lon = verts[j + 1];
                Vector2d v = projection.project( LatLonGeo.fromRad( lat, lon ) );
                verts[j] = ( float ) v.getX( );
                verts[j + 1] = ( float ) v.getY( );
            }

            polys[i] = new TessellatedPolygon( new Polygon( ), verts );
        }

        return polys;
    }

    @Override
    protected void replaceTileData( Collection<Entry<TileKey, TessellatedPolygon[]>> tileData )
    {
        for ( TileKey old : loadedGroups )
        {
            painter.setFill( old, false );
        }

        for ( Entry<TileKey, TessellatedPolygon[]> e : tileData )
        {
            TileKey groupId = e.getKey( );
            if ( loadedGroups.contains( groupId ) )
            {
                painter.setFill( groupId, true );
            }
            else
            {
                loadedGroups.add( groupId );
                configurePainter( groupId );
                for ( TessellatedPolygon p : e.getValue( ) )
                {
                    painter.addPolygon( groupId, p, p, 0 );
                }
            }
        }
    }

    private void configurePainter( Object groupId )
    {
        painter.setFill( groupId, true );
        painter.setFillColor( groupId, GlimpseColor.getGreen( ) );
        painter.setShowLines( groupId, false );
    }

    @Override
    protected Collection<TileKey> allKeys( )
    {
        return Collections.unmodifiableCollection( keys.keySet( ) );
    }
}
