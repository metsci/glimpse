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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.metsci.glimpse.charts.shoreline.gshhs.GshhsFile;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.PolygonType;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.UnrecognizedValueException;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.support.polygon.PolygonTessellator;
import com.metsci.glimpse.support.polygon.PolygonTessellator.TessellationException;
import com.metsci.glimpse.support.polygon.VertexAccumulator;
import com.metsci.glimpse.util.GlimpseDataPaths;
import com.metsci.glimpse.util.units.Length;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class ShorelineTiler
{
    private static final Logger LOGGER = Logger.getLogger( ShorelineTiler.class.getName( ) );

    public static void main( String[] args ) throws IOException, UnrecognizedValueException, TessellationException
    {
        setTerseConsoleLogger( Level.FINE );

        LandBox box = new LandBox( 90, -90, -180, 180, false );

        File destFile = new File( "/home/borkholder/Desktop/tmp.bin" );
        DataOutputStream o = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( destFile ) ) );
        // version
        o.writeByte( 0 );

        o.writeInt( 4 );
        o.writeFloat( ( float ) Length.fromNauticalMiles( 10 ) );
        o.writeFloat( ( float ) Length.fromNauticalMiles( 100 ) );
        o.writeFloat( ( float ) Length.fromNauticalMiles( 500 ) );
        o.writeFloat( ( float ) Length.fromNauticalMiles( 1000 ) );

        File gshhsF = new File( GlimpseDataPaths.glimpseUserDataDir, "gshhs/gshhs_f.b" );
        LandShape land = new GshhsFile( gshhsF, box, PolygonType.land ).toShape( );
        write( o, land, 0, 2, 1 );

        File gshhsH = new File( GlimpseDataPaths.glimpseUserDataDir, "gshhs/gshhs_h.b" );
        land = new GshhsFile( gshhsH, box, PolygonType.land ).toShape( );
        write( o, land, 1, 5, 3 );

        File gshhsI = new File( GlimpseDataPaths.glimpseUserDataDir, "gshhs/gshhs_i.b" );
        land = new GshhsFile( gshhsI, box, PolygonType.land ).toShape( );
        write( o, land, 2, 20, 10 );

        File gshhsL = new File( GlimpseDataPaths.glimpseUserDataDir, "gshhs/gshhs_l.b" );
        land = new GshhsFile( gshhsL, box, PolygonType.land ).toShape( );
        write( o, land, 3, 30, 10 );

        o.close( );
    }

    static void write( DataOutputStream out, LandShape land, int level, int tileWidth_DEG, int tileHeight_DEG ) throws IOException
    {
        createTiles( tileWidth_DEG, tileHeight_DEG ).parallel( )
                .forEach( b -> {
                    Collection<double[]> tess = land.getSegments( ).stream( ).parallel( )
                            .map( s -> toArea( s ) )
                            .map( s -> tile( s, b ) )
                            .filter( a -> !a.isEmpty( ) )
                            .flatMap( s -> split( s ) )
                            .map( s -> tesselate( s ) )
                            .collect( Collectors.toList( ) );

                    if ( tess.isEmpty( ) )
                    {
                        return;
                    }

                    try
                    {
                        Rectangle2D bounds0 = b[0];
                        long nVerts = tess.stream( ).mapToLong( l -> l.length ).sum( ) / 2;
                        logInfo( LOGGER, "Found %,d polygons in tile level %d with %,d vertices", tess.size( ), level, nVerts );

                        synchronized ( out )
                        {
                            out.writeByte( ( byte ) level );
                            out.writeFloat( ( float ) toRadians( bounds0.getMinY( ) ) );
                            out.writeFloat( ( float ) toRadians( bounds0.getMaxY( ) ) );
                            out.writeFloat( ( float ) toRadians( bounds0.getMinX( ) ) );
                            out.writeFloat( ( float ) toRadians( bounds0.getMaxX( ) ) );
                            out.writeInt( tess.size( ) );
                            for ( double[] verts : tess )
                            {
                                out.writeInt( verts.length );
                                for ( int i = 0; i < verts.length; i += 2 )
                                {
                                    out.writeDouble( toRadians( verts[i + 1] ) );
                                    out.writeDouble( toRadians( verts[i] ) );
                                }
                            }
                        }
                    }
                    catch ( IOException ex )
                    {
                        throw new RuntimeException( ex );
                    }
                } );
    }

    static Stream<Rectangle2D[]> createTiles( int tileWidth_DEG, int tileHeight_DEG )
    {
        Builder<Rectangle2D[]> bldr = Stream.builder( );
        for ( int lon = -180; lon < 180; lon += tileWidth_DEG )
        {
            for ( int lat = -90; lat < 90; lat += tileHeight_DEG )
            {
                Rectangle2D bounds0 = new Rectangle2D.Double( lon - 1, lat - 1, tileWidth_DEG + 2, tileHeight_DEG + 2 );
                Rectangle2D bounds1 = new Rectangle2D.Double( lon - 1 - 360, lat - 1, tileWidth_DEG + 2, tileHeight_DEG + 2 );
                Rectangle2D bounds2 = new Rectangle2D.Double( lon - 1 + 360, lat - 1, tileWidth_DEG + 2, tileHeight_DEG + 2 );
                bldr.add( new Rectangle2D[] { bounds0, bounds1, bounds2 } );
            }
        }

        return bldr.build( );
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
