package com.metsci.glimpse.charts.shoreline;

import static java.lang.Math.toRadians;

import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    public static void main( String[] args ) throws IOException, UnrecognizedValueException, TessellationException
    {
        LandBox box = new LandBox( 90, -90, -180, 180, false );
        GshhsFile f = new GshhsFile( new File( GlimpseDataPaths.glimpseUserDataDir, "gshhs/gshhs_i.b" ), box, PolygonType.land );
        LandShape shape = f.toShape( );

        File file = new File( "/home/borkholder/Desktop/tmp.bin" );
        DataOutputStream o = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( file ) ) );

        int tileWidth = 10;
        int tileHeight = 5;
        for ( int x = -180; x < 180; x += tileWidth )
        {
            for ( int y = -90; y < 90; y += tileHeight )
            {
                Rectangle2D bounds = new Rectangle2D.Double( x - 1, y - 1, tileWidth + 2, tileHeight + 2 );
                List<float[]> polys = shape.getSegments( ).stream( ).parallel( )
                        .map( seg -> tileAndTesselate( bounds, seg ) )
                        .filter( v -> v.length > 0 )
                        .collect( Collectors.toList( ) );

                o.writeFloat( ( float ) toRadians( bounds.getMinY( ) ) );
                o.writeFloat( ( float ) toRadians( bounds.getMaxY( ) ) );
                o.writeFloat( ( float ) toRadians( bounds.getMinX( ) ) );
                o.writeFloat( ( float ) toRadians( bounds.getMaxX( ) ) );
                o.writeInt( polys.size( ) );
                for ( float[] verts : polys )
                {
                    o.writeInt( verts.length );
                    for ( float v : verts )
                    {
                        o.writeFloat( ( float ) toRadians( v ) );
                    }
                }
            }
        }

        o.close( );
    }

    static float[] tileAndTesselate( Rectangle2D bounds, LandSegment segment )
    {
        final double minX = bounds.getMinX( );
        final double maxX = bounds.getMaxX( );
        final double minY = bounds.getMinY( );
        final double maxY = bounds.getMaxY( );

        boolean any = false;
        double[] v2 = new double[2];
        LoopBuilder bldr = new LoopBuilder( );
        for ( LandVertex v : segment.vertices )
        {
            if ( minY <= v.lat && v.lat <= maxY &&
                    ( ( minX - 360 <= v.lon && v.lon <= maxX - 360 ) ||
                            ( minX <= v.lon && v.lon <= maxX ) ||
                            ( minX + 360 <= v.lon && v.lon <= maxX + 360 ) ) )
            {
                any = true;
                break;
            }
        }

        for ( LandVertex v : segment.vertices )
        {
            if ( any )
            {
                v2[0] = v.lat;
                v2[1] = v.lon;
                bldr.addVertices( v2, 1 );
            }
        }

        Loop loop = bldr.complete( Interior.onLeft );
        if ( loop.size( ) == 0 )
        {
            return new float[0];
        }

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
