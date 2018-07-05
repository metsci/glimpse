/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.examples.charts.bathy;

import static com.metsci.glimpse.examples.Example.showWithSwing;
import static com.metsci.glimpse.util.logging.LoggerUtils.setTerseConsoleLogger;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.metsci.glimpse.charts.bathy.GeotiffTileProvider;
import com.metsci.glimpse.charts.bathy.ShadedReliefTiledPainter;
import com.metsci.glimpse.charts.bathy.TopoTileProvider;
import com.metsci.glimpse.charts.shoreline.LandBox;
import com.metsci.glimpse.charts.shoreline.LandSegment;
import com.metsci.glimpse.charts.shoreline.LandShape;
import com.metsci.glimpse.charts.shoreline.LandVertex;
import com.metsci.glimpse.charts.shoreline.ShorelineTilePainter;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsFile;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.PolygonType;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.UnrecognizedValueException;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.MapPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.support.polygon.PolygonTessellator.TessellationException;
import com.metsci.glimpse.util.GlimpseDataPaths;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.units.Length;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * @author borkholder
 */
public class Etopo1Example implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        setTerseConsoleLogger( Level.FINE );
        showWithSwing( new Etopo1Example( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        try
        {
            GeoProjection projection = new TangentPlane( LatLonGeo.fromDeg( 20.14, -79.23 ) );
            MapPlot2D plot = new MapPlot2D( projection );
                        TopoTileProvider tileProvider = GeotiffTileProvider.getGebco2014( );
                        plot.getLayoutCenter( ).addPainter( new ShadedReliefTiledPainter( projection, tileProvider ) );
            //            plot.getLayoutCenter( ).addPainter( new UnderseaFeatureNamesPainter( projection ) );

                        ShorelineTilePainter landPainter = new ShorelineTilePainter( projection, new File( "../extras-charts/osm_tiled.bin" ) );
                        landPainter.setLandColor( GlimpseColor.fromColorHex( "#ece4d2" ) );
//                        landPainter.setLandColor( GlimpseColor.fromColorHex( "#e8eae0" ) );
//                        landPainter.setLandColor( GlimpseColor.fromColorHex( "#d3dcc8" ) );
                        plot.getLayoutCenter( ).addPainter( landPainter );

//            PolygonPainter p = new PolygonPainter( );
//            int i = 0;
//            ColorGenerator g = new ColorGenerator( );
//            for ( Polygon p0 : load( projection ) )
//            {
//                p.addPolygon( i, 0, p0, 0 );
//
//                p.setFill( i, false );
//                float[] rgba = new float[4];
//                g.next( rgba );
//                p.setLineColor( i, rgba );
//                p.setShowLines( i, true );
//                p.setLineWidth( i, 4 );
//
//                i++;
//            }
//
//            plot.getLayoutCenter( ).addPainter( p );

            plot.getAxis( ).set( 0, Length.fromNauticalMiles( 300 ), 0, Length.fromNauticalMiles( 300 ) );
            plot.getAxis( ).validate( );
            return plot;
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( ex );
        }
    }

    public static Polygon[] load( GeoProjection p ) throws IOException, UnrecognizedValueException, TessellationException
    {
        setTerseConsoleLogger( Level.FINE );

        LandBox box = new LandBox( 90, -90, -180, 180, false );
        GshhsFile f = new GshhsFile( new File( GlimpseDataPaths.glimpseUserDataDir, "gshhs/gshhs_l.b" ), box, PolygonType.land );
        LandShape shape = f.toShape( );

        List<Polygon> list = new ArrayList<>( );

        int tileWidth = 10;
        int tileHeight = 5;
        for ( int x = -180; x < 180; x += tileWidth )
        {
            for ( int y = -90; y < 90; y += tileHeight )
            {
                Rectangle2D bounds = new Rectangle2D.Double( x - 1, y - 1, tileWidth + 2, tileHeight + 2 );

                List<Polygon> polys = shape.getSegments( ).stream( ).parallel( )
                        .map( seg -> tileAndTesselate( p, bounds, seg ) )
                        .filter( p0 -> p0.getIterator( ).hasNext( ) )
                        .collect( Collectors.toList( ) );
                list.addAll( polys );
            }
        }

        return list.toArray( new Polygon[0] );
    }

    static Polygon tileAndTesselate( GeoProjection proj, Rectangle2D bounds, LandSegment segment )
    {
        final double minX = bounds.getMinX( );
        final double maxX = bounds.getMaxX( );
        final double minY = bounds.getMinY( );
        final double maxY = bounds.getMaxY( );

        double[] v2 = new double[2];
        LoopBuilder bldr = new LoopBuilder( );
        for ( LandVertex v : segment.vertices )
        {
            if ( minY <= v.lat && v.lat <= maxY &&
                    ( ( minX - 360 <= v.lon && v.lon <= maxX - 360 ) ||
                            ( minX <= v.lon && v.lon <= maxX ) ||
                            ( minX + 360 <= v.lon && v.lon <= maxX + 360 ) ) )
            {
                Vector2d s = proj.project( LatLonGeo.fromDeg( v.lat, v.lon ) );
                v2[0] = s.getX( );
                v2[1] = s.getY( );
                bldr.addVertices( v2, 1 );
            }
        }

        Loop loop = bldr.complete( Interior.onLeft );
        Polygon p = new Polygon( );
        if ( loop.size( ) > 0 )
        {
            p.add( loop );
        }

        return p;
    }
}
