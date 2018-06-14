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
        GshhsFile f = new GshhsFile( new File( GlimpseDataPaths.glimpseUserDataDir, "gshhs/gshhs_i.b" ), box, PolygonType.land );
        LandShape shape = f.toShape( );

        File file = new File( "/home/borkholder/Desktop/tmp.bin" );
        DataOutputStream o = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( file ) ) );

        int tileWidth = 15;
        int tileHeight = 10;
        for ( int lon = -180; lon < 180; lon += tileWidth )
        {
            for ( int lat = -90; lat < 90; lat += tileHeight )
            {
                Rectangle2D bounds = new Rectangle2D.Double( lon - 1, lat - 1, tileWidth + 2, tileHeight + 2 );
                logInfo( LOGGER, "Tesselating tile for lat =[%f,%f], lon=[%f,%f]", bounds.getMinY( ), bounds.getMaxY( ), bounds.getMinX( ), bounds.getMaxX( ) );

                Collection<float[]> tess = shape.getSegments( ).stream( ).parallel( )
                        .map( s -> toArea( s ) )
                        .map( s -> tile( bounds, s ) )
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

                o.writeFloat( ( float ) toRadians( bounds.getMinY( ) ) );
                o.writeFloat( ( float ) toRadians( bounds.getMaxY( ) ) );
                o.writeFloat( ( float ) toRadians( bounds.getMinX( ) ) );
                o.writeFloat( ( float ) toRadians( bounds.getMaxX( ) ) );
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
        Path2D.Double p = new Path2D.Double( Path2D.WIND_NON_ZERO );
        for ( int i = 0; i < segment.vertices.size( ); i++ )
        {
            LandVertex v = segment.vertices.get( i );
            if ( i == 0 )
            {
                p.moveTo( v.lon, v.lat );
            }
            else
            {
                p.lineTo( v.lon, v.lat );
            }
        }
        p.closePath( );

        return new Area( p );
    }

    static Area tile( Rectangle2D bounds, Area shape )
    {
        shape = ( Area ) shape.clone( );
        shape.intersect( new Area( bounds ) );
        return shape;
    }

    static Stream<Shape> split( Shape s )
    {
        List<Shape> all = new ArrayList<>( );

        double[] v = new double[2];
        PathIterator itr = s.getPathIterator( null, 10 );

        Path2D.Double p = new Path2D.Double( );
        while ( !itr.isDone( ) )
        {
            switch ( itr.currentSegment( v ) )
            {
                case PathIterator.SEG_MOVETO:
                    p.moveTo( v[0], v[1] );
                    break;
                case PathIterator.SEG_LINETO:
                    p.lineTo( v[0], v[1] );
                    break;
                case PathIterator.SEG_CLOSE:
                    p.closePath( );
                    all.add( p );
                    p = new Path2D.Double( );
                    break;
            }

            itr.next( );
        }

        return all.stream( );
    }

    static float[] tesselate( Shape s )
    {
        double[] v = new double[2];
        LoopBuilder bldr = new LoopBuilder( );
        PathIterator itr = s.getPathIterator( null );
        while ( itr.currentSegment( v ) != PathIterator.SEG_CLOSE )
        {
            bldr.addVertices( v, 1 );
            itr.next( );
        }

        Loop loop = bldr.complete( Interior.onLeft );

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
