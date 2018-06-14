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

import com.metsci.glimpse.charts.shoreline.gshhs.GshhsFile;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.PolygonType;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.UnrecognizedValueException;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.support.polygon.PolygonTessellator;
import com.metsci.glimpse.support.polygon.PolygonTessellator.TessellationException;
import com.metsci.glimpse.support.polygon.SimpleVertexAccumulator;
import com.metsci.glimpse.util.GlimpseDataPaths;

public class ShorelineTiler
{
    private static final Logger LOGGER = Logger.getLogger( ShorelineTiler.class.getName( ) );

    public static void main( String[] args ) throws IOException, UnrecognizedValueException, TessellationException
    {
        setTerseConsoleLogger( Level.FINE );

        LandBox box = new LandBox( 90, -90, -180, 180, false );
        GshhsFile f = new GshhsFile( new File( GlimpseDataPaths.glimpseUserDataDir, "gshhs/gshhs_l.b" ), box, PolygonType.land );
        LandShape shape = f.toShape( );

        File file = new File( "/home/borkholder/Desktop/tmp.bin" );
        DataOutputStream o = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( file ) ) );

        int tileWidth = 20;
        int tileHeight = 10;
        for ( int lon = -180; lon < 180; lon += tileWidth )
        {
            for ( int lat = -90; lat < 90; lat += tileHeight )
            {
                Rectangle2D bounds0 = new Rectangle2D.Double( lon - 1, lat - 1, tileWidth + 2, tileHeight + 2 );
                Rectangle2D bounds1 = new Rectangle2D.Double( lon - 1 - 360, lat - 1, tileWidth + 2, tileHeight + 2 );
                Rectangle2D bounds2 = new Rectangle2D.Double( lon - 1 + 360, lat - 1, tileWidth + 2, tileHeight + 2 );
                logInfo( LOGGER, "Tesselating tile for lat =[%f,%f], lon=[%f,%f]", bounds0.getMinY( ), bounds0.getMaxY( ), bounds0.getMinX( ), bounds0.getMaxX( ) );

                Collection<float[]> tess = shape.getSegments( ).stream( ).parallel( )
                        .map( s -> toArea( s ) )
                        .map( s -> tile( s, bounds0, bounds1, bounds2 ) )
                        .filter( a -> !a.isEmpty( ) )
                        .flatMap( s -> split( s ) )
                        .map( s -> tesselate( s ) )
                        .collect( Collectors.toList( ) );
                long nVerts = tess.stream( ).mapToLong( l -> l.length ).sum( ) / 2;
                logInfo( LOGGER, "Found %,d polygons in tile with %,d vertices", tess.size( ), nVerts );

                if ( tess.isEmpty( ) )
                {
                    continue;
                }

                o.writeFloat( ( float ) toRadians( bounds0.getMinY( ) ) );
                o.writeFloat( ( float ) toRadians( bounds0.getMaxY( ) ) );
                o.writeFloat( ( float ) toRadians( bounds0.getMinX( ) ) );
                o.writeFloat( ( float ) toRadians( bounds0.getMaxX( ) ) );
                o.writeInt( tess.size( ) );
                for ( float[] verts : tess )
                {
                    o.writeInt( verts.length );
                    for ( int i = 0; i < verts.length; i += 2 )
                    {
                        o.writeFloat( ( float ) toRadians( verts[i + 1] ) );
                        o.writeFloat( ( float ) toRadians( verts[i] ) );
                    }
                }
            }
        }

        o.close( );
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

    static float[] tesselate( Loop loop )
    {
        Polygon p = new Polygon( );
        p.add( loop );
        SimpleVertexAccumulator accum = new SimpleVertexAccumulator( );
        try
        {
            PolygonTessellator tess = new PolygonTessellator( );
            tess.tessellate( p, accum );
            tess.destroy( );
        }
        catch ( TessellationException ex )
        {
            throw new RuntimeException( ex );
        }

        return accum.getVertices( );
    }
}
