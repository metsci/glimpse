package com.metsci.glimpse.charts.shoreline;

import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;
import static com.metsci.glimpse.util.logging.LoggerUtils.setTerseConsoleLogger;
import static java.lang.Math.toRadians;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.google.common.io.Files;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.UnrecognizedValueException;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.support.polygon.PolygonTessellator;
import com.metsci.glimpse.support.polygon.PolygonTessellator.TessellationException;
import com.metsci.glimpse.support.polygon.VertexAccumulator;
import com.metsci.glimpse.util.Pair;
import com.metsci.glimpse.util.units.Length;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class ShorelineTiler
{
    private static final Logger LOGGER = Logger.getLogger( ShorelineTiler.class.getName( ) );

    public static void main( String[] args ) throws IOException, UnrecognizedValueException, TessellationException
    {
        setTerseConsoleLogger( Level.FINE );

        File destFile = new File( "./osm_tiled.bin" );

        File file = new File( "land-polygons-complete-4326/land_polygons.shp" );
        File destDir = new File( "." );
        int nLevels = 1;

        DataStore dataStore = DataStoreFinder.getDataStore( Collections.singletonMap( "url", file.toURI( ).toURL( ) ) );
        String typeName = dataStore.getTypeNames( )[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource( typeName );
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures( Filter.INCLUDE );

        Collection<TileOutInfo> tiles = createTiles( 0, destDir, 30, 10 );

        FeatureIterator<SimpleFeature> features = collection.features( );
        toStream( features )
                .parallel( )
                .map( ShorelineTiler::toArea )
                .flatMap( a -> tiles.stream( ).map( t -> new Pair<>( t, a ) ) )
                .forEach( p -> write( p.first( ), p.second( ) ) );

        features.close( );

        Collection<TileOutInfo> validTiles = new ArrayList<>( tiles );
        validTiles.removeIf( t -> !t.tmpFile.isFile( ) );

        try (DataOutputStream out = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( destFile ) ) ))
        {
            long dataOffset = Byte.BYTES + Long.BYTES + Integer.BYTES + Float.BYTES * nLevels + validTiles.size( ) * ( Byte.BYTES + 4 * Float.BYTES + Long.BYTES );

            // version
            out.writeByte( 0 );
            out.writeLong( dataOffset );
            out.writeInt( nLevels );
            out.writeFloat( ( float ) Length.fromNauticalMiles( 2 ) );

            for ( TileOutInfo info : validTiles )
            {
                out.writeByte( ( byte ) info.level );
                out.writeFloat( ( float ) toRadians( info.bounds[0].getMinY( ) ) );
                out.writeFloat( ( float ) toRadians( info.bounds[0].getMaxY( ) ) );
                out.writeFloat( ( float ) toRadians( info.bounds[0].getMinX( ) ) );
                out.writeFloat( ( float ) toRadians( info.bounds[0].getMaxX( ) ) );
                out.writeLong( dataOffset );

                dataOffset += Integer.BYTES + info.tmpFile.length( );
            }

            for ( TileOutInfo info : validTiles )
            {
                int nVertices = ( int ) ( info.tmpFile.length( ) / Float.BYTES / 2 );
                out.writeInt( nVertices );
                Files.copy( info.tmpFile, out );
                info.tmpFile.delete( );
            }
        }
    }

    static Stream<com.vividsolutions.jts.geom.Polygon> toStream( FeatureIterator<SimpleFeature> features )
    {
        Iterator<MultiPolygon> itr = new Iterator<MultiPolygon>( )
        {
            @Override
            public boolean hasNext( )
            {
                return features.hasNext( );
            }

            @Override
            public synchronized MultiPolygon next( )
            {
                SimpleFeature feat = features.next( );
                MultiPolygon poly = ( MultiPolygon ) feat.getDefaultGeometryProperty( ).getValue( );
                return poly;
            }
        };

        Spliterator<MultiPolygon> spliterator = Spliterators.spliteratorUnknownSize( itr, Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.ORDERED );
        return StreamSupport.stream( spliterator, false )
                .flatMap( m -> {
                    com.vividsolutions.jts.geom.Polygon[] g = new com.vividsolutions.jts.geom.Polygon[m.getNumGeometries( )];
                    for ( int i = 0; i < g.length; i++ )
                    {
                        g[i] = ( com.vividsolutions.jts.geom.Polygon ) m.getGeometryN( i );
                    }
                    return Stream.of( g );
                } );
    }

    static class TileOutInfo
    {
        final int level;
        final long tileKey;
        final Rectangle2D[] bounds;
        final File tmpFile;

        TileOutInfo( int level, long tileKey, Rectangle2D[] bounds, File tmpFile )
        {
            this.level = level;
            this.tileKey = tileKey;
            this.bounds = bounds;
            this.tmpFile = tmpFile;
        }
    }

    static Area toArea( com.vividsolutions.jts.geom.Polygon poly )
    {
        LineString ring = poly.getExteriorRing( );

        double lastLon = 0;
        Path2D.Double p = new Path2D.Double( Path2D.WIND_NON_ZERO );
        for ( int i = 0; i < ring.getNumPoints( ); i++ )
        {
            Coordinate c = ring.getCoordinateN( i );
            double x = c.x;
            double y = c.y;

            // unroll the longitude so it doesn't wrap
            while ( x - lastLon > 180 )
            {
                x -= 360;
            }
            while ( lastLon - x > 180 )
            {
                x += 360;
            }
            lastLon = x;

            if ( i == 0 )
            {
                p.moveTo( x, y );
            }
            else
            {
                p.lineTo( x, y );
            }
        }
        p.closePath( );

        return new Area( p );
    }

    static void write( TileOutInfo info, Area area )
    {
        Area tiled = tile( area, info.bounds );
        if ( tiled.isEmpty( ) )
        {
            return;
        }

        Collection<double[]> tess = split( tiled )
                .map( ShorelineTiler::tesselate )
                .collect( Collectors.toList( ) );

        if ( tess.isEmpty( ) )
        {
            return;
        }

        try
        {
            int nVertices = ( int ) tess.stream( ).mapToLong( l -> l.length ).sum( ) / 2;
            logInfo( LOGGER, "Found %,d polygons in tile level %d with %,d vertices", tess.size( ), info.level, nVertices );

            synchronized ( info )
            {
                try (DataOutputStream out = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( info.tmpFile, true ) ) ))
                {
                    for ( double[] verts : tess )
                    {
                        for ( int i = 0; i < verts.length; i += 2 )
                        {
                            out.writeFloat( ( float ) toRadians( verts[i + 1] ) );
                            out.writeFloat( ( float ) toRadians( verts[i] ) );
                        }
                    }
                }
            }
        }
        catch ( IOException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    static Collection<TileOutInfo> createTiles( int level, File destDir, int tileWidth_DEG, int tileHeight_DEG )
    {
        long key = ( tileWidth_DEG * 1_000 + tileHeight_DEG ) * 1_000_000;

        Collection<TileOutInfo> tiles = new ArrayList<>( );
        for ( int lon = -180; lon < 180; lon += tileWidth_DEG )
        {
            for ( int lat = -90; lat < 90; lat += tileHeight_DEG )
            {
                Rectangle2D bounds0 = new Rectangle2D.Double( lon - 1, lat - 1, tileWidth_DEG + 2, tileHeight_DEG + 2 );
                Rectangle2D bounds1 = new Rectangle2D.Double( lon - 1 - 360, lat - 1, tileWidth_DEG + 2, tileHeight_DEG + 2 );
                Rectangle2D bounds2 = new Rectangle2D.Double( lon - 1 + 360, lat - 1, tileWidth_DEG + 2, tileHeight_DEG + 2 );
                File tmpFile = new File( destDir, String.format( "data_%x", key++ ) );
                tiles.add( new TileOutInfo( level, key, new Rectangle2D[] { bounds0, bounds1, bounds2 }, tmpFile ) );
            }
        }

        return tiles;
    }

    static Area toArea( LandSegment segment )
    {
        double lastLon = 0;

        Path2D.Double p = new Path2D.Double( Path2D.WIND_NON_ZERO );
        for ( int i = 0; i < segment.vertices.size( ); i++ )
        {
            LandVertex v = segment.vertices.get( i );
            double x = v.lon;
            double y = v.lat;

            // unroll the longitude so it doesn't wrap
            while ( x - lastLon > 180 )
            {
                x -= 360;
            }
            while ( lastLon - x > 180 )
            {
                x += 360;
            }
            lastLon = x;

            if ( i == 0 )
            {
                p.moveTo( x, y );
            }
            else
            {
                p.lineTo( x, y );
            }
        }
        p.closePath( );

        return new Area( p );
    }

    static Area tile( Area shape, Rectangle2D... bounds )
    {
        for ( Rectangle2D b : bounds )
        {
            if ( shape.intersects( b ) )
            {
                Area a = new Area( b );
                a.intersect( shape );
                if ( !a.isEmpty( ) )
                {
                    return a;
                }
            }
        }

        return new Area( );
    }

    static Stream<Loop> split( Shape s )
    {
        List<Loop> all = new ArrayList<>( );

        double[] v = new double[2];
        PathIterator itr = s.getPathIterator( null, 10 );

        LoopBuilder bldr = null;
        while ( !itr.isDone( ) )
        {
            switch ( itr.currentSegment( v ) )
            {
                case PathIterator.SEG_MOVETO:
                    bldr = new LoopBuilder( );
                case PathIterator.SEG_LINETO:
                    bldr.addVertices( v, 1 );
                    break;
                case PathIterator.SEG_CLOSE:
                    all.add( bldr.complete( Interior.onLeft ) );
                    bldr = null;
                    break;
                default:
                    throw new AssertionError( );
            }

            itr.next( );
        }

        return all.stream( );
    }

    static double[] tesselate( Loop loop )
    {
        Polygon p = new Polygon( );
        p.add( loop );

        DoubleList list = new DoubleArrayList( );
        try
        {
            PolygonTessellator tess = new PolygonTessellator( );
            tess.tessellate( p, new VertexAccumulator( )
            {
                @Override
                public void addVertices( float[] vertexData, int nVertices )
                {
                    for ( int i = 0; i < nVertices; i++ )
                    {
                        list.add( vertexData[i * 2] );
                        list.add( vertexData[i * 2 + 1] );
                    }
                }

                @Override
                public void addVertices( double[] vertexData, int nVertices )
                {
                    for ( int i = 0; i < nVertices; i++ )
                    {
                        list.add( vertexData[i * 2] );
                        list.add( vertexData[i * 2 + 1] );
                    }
                }
            } );
            tess.destroy( );
        }
        catch ( TessellationException ex )
        {
            throw new RuntimeException( ex );
        }

        return list.toDoubleArray( );
    }
}
