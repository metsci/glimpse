/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.charts.vector.painter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.metsci.glimpse.charts.vector.display.xmlgen.ColorSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.FeatureSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.PainterSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.PolygonPainterSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.ShapeSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.ShapeTypeSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.TrackPainterSpec;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.ENCShape;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;
import com.metsci.glimpse.painter.track.Point;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * @author cunningham
 */
public class EncPainterUtils
{

    private static Logger logger = Logger.getLogger( EncPainterUtils.class.toString( ) );

    private EncPainterUtils( )
    {
        throw new IllegalStateException( );
    }

    public static float[][] convertEncVertexesToLonLatFloatVertexes( double[] encVertexes, boolean connectLastToFirst )
    {
        assert ( encVertexes.length % 2 == 0 );
        int dimSize = encVertexes.length / 2;
        if ( connectLastToFirst ) dimSize++;
        float[] x = new float[dimSize];
        float[] y = new float[dimSize];
        int j = 0;
        for ( int i = 1; i < encVertexes.length; i += 2, j++ )
        {
            x[j] = ( float ) encVertexes[i - 1];
            y[j] = ( float ) encVertexes[i];
        }
        if ( connectLastToFirst )
        {
            x[j] = ( float ) encVertexes[0];
            y[j] = ( float ) encVertexes[1];
        }

        return new float[][] { x, y };
    }

    // at one point we were getting faulty lonlat values, hence this method for sanity checks.
    // turns out, mistake was on collection routine, not in parsing.  This method may no longer
    // be needed.
    public static boolean validateLongitude( double longitude )
    {
        return ( Math.abs( longitude ) < 182 );
    }

    public static boolean validateLatitude( double latitude )
    {
        return ( Math.abs( latitude ) < 91 );
    }

    public static float[][] convertEncVertexesToXYVertexes( GeoProjection projection, ENCShape shape )
    {
        assert ( shape.passSanityCheck( ) );
        if ( !shape.passSanityCheck( ) ) return new float[0][0];

        int numCoords = shape.getNumCoordinates( );
        float[] x = new float[numCoords];
        float[] y = new float[numCoords];

        int i = 0;
        for ( ; i < numCoords; i++ )
        {
            double lon = shape.getVertex( 0, i );
            if ( !validateLongitude( lon ) )
            {
                logger.info( "Ignoring bad longitude " + lon );
                continue;
            }

            double lat = shape.getVertex( 1, i );
            if ( !validateLatitude( lat ) )
            {
                logger.info( "Ignoring bad latitude " + lat );
                continue;
            }

            Vector2d vector = projection.project( LatLonGeo.fromDeg( lat, lon ) );
            x[i] = ( float ) vector.getX( );
            y[i] = ( float ) vector.getY( );
        }

        return new float[][] { x, y };
    }

    public static double[] convertEncVertexesToSingleXYArray( GeoProjection projection, ENCShape shape )
    {
        assert ( shape.passSanityCheck( ) );
        if ( !shape.passSanityCheck( ) ) return new double[0];

        int numVertexes = shape.getNumCoordinates( );
        double[] xy = new double[numVertexes * 2];
        int xyIndex = 0;
        for ( int vertexIndex = 0; vertexIndex < numVertexes; vertexIndex++ )
        {
            double lon = shape.getVertex( 0, vertexIndex );
            if ( !validateLongitude( lon ) )
            {
                logger.info( "Ignoring bad longitude " + lon );
                continue;
            }

            double lat = shape.getVertex( 1, vertexIndex );
            if ( !validateLatitude( lat ) )
            {
                logger.info( "Ignoring bad latitude " + lat );
                continue;
            }

            Vector2d vector = projection.project( LatLonGeo.fromDeg( lat, lon ) );
            xy[xyIndex++] = vector.getX( );
            xy[xyIndex++] = vector.getY( );
        }

        return xy;
    }

    public static double[] convertGeoVertexesToSingleXYArray( GeoProjection projection, GeoShape shape )
    {
        int numVertexes = shape.getNumCoordinates( );
        double[] xy = new double[numVertexes * 2];
        int xyIndex = 0;
        for ( int vertexIndex = 0; vertexIndex < numVertexes; vertexIndex++ )
        {
            double lon = shape.getVertex( 0, vertexIndex );
            if ( !validateLongitude( lon ) )
            {
                logger.info( "Ignoring bad longitude " + lon );
                continue;
            }

            double lat = shape.getVertex( 1, vertexIndex );
            if ( !validateLatitude( lat ) )
            {
                logger.info( "Ignoring bad latitude " + lat );
                continue;
            }

            Vector2d vector = projection.project( LatLonGeo.fromDeg( lat, lon ) );
            xy[xyIndex++] = vector.getX( );
            xy[xyIndex++] = vector.getY( );
        }

        return xy;
    }

    /**
     * Given an enc object, will return a polygon containing the vertices in the enc object.
     * Method will:
     * - project each vertex using the given projection parameter
     * - will construct the polygon so that enc object shape0 is the outer shape, and shapes
     *   1-N are the inner shapes that are to be subtracted out.  So the return shape consists
     *   of shape0 - shape1 .. - shapeN
     */
    public static Polygon convertEncVertexesToPolgon( GeoProjection projection, ENCObject object )
    {
        Polygon p = new Polygon( );
        List<ENCShape> shapeList = object.getShapeList( );
        if ( shapeList.isEmpty( ) ) return p;

        // outer shape
        ENCShape outerShape = shapeList.get( 0 );
        int outerVertexCount = outerShape.getNumCoordinates( );
        LoopBuilder loopBuilder1 = new LoopBuilder( );
        double[] outerXYVertices = convertEncVertexesToSingleXYArray( projection, outerShape );
        loopBuilder1.addVertices( outerXYVertices, outerVertexCount );
        p.add( loopBuilder1.complete( Interior.onLeft ) );

        // inner shapes
        for ( int shapeIndex = 1; shapeIndex < shapeList.size( ); shapeIndex++ )
        {
            ENCShape innerShape = shapeList.get( shapeIndex );
            int innerVertexCount = innerShape.getNumCoordinates( );
            LoopBuilder loopBuilder2 = new LoopBuilder( );
            double[] innerXYVertices = convertEncVertexesToSingleXYArray( projection, innerShape );
            loopBuilder2.addVertices( reverseXYArray( innerXYVertices ), innerVertexCount );
            p.add( loopBuilder2.complete( Interior.onLeft ) );
        }

        return p;
    }

    public static Polygon convertGeoVertexesToPolgon( GeoProjection projection, GeoObject object )
    {
        Polygon p = new Polygon( );
        Collection<? extends GeoShape> shapeList = object.getGeoShapes( );
        if ( shapeList.isEmpty( ) ) return p;

        // outer shape
        Iterator<? extends GeoShape> shapeIt = shapeList.iterator( );
        GeoShape outerShape = shapeIt.next( );
        int outerVertexCount = outerShape.getNumCoordinates( );
        LoopBuilder loopBuilder1 = new LoopBuilder( );
        double[] outerXYVertices = convertGeoVertexesToSingleXYArray( projection, outerShape );
        loopBuilder1.addVertices( outerXYVertices, outerVertexCount );
        p.add( loopBuilder1.complete( Interior.onLeft ) );

        // inner shapes
        while ( shapeIt.hasNext( ) )
        {
            GeoShape innerShape = shapeIt.next( );
            int innerVertexCount = innerShape.getNumCoordinates( );
            LoopBuilder loopBuilder2 = new LoopBuilder( );
            double[] innerXYVertices = convertGeoVertexesToSingleXYArray( projection, innerShape );
            loopBuilder2.addVertices( reverseXYArray( innerXYVertices ), innerVertexCount );
            p.add( loopBuilder2.complete( Interior.onLeft ) );
        }

        return p;
    }

    /**
     * Returns a new float array where the vertex elements are flipped.  The first vertex is
     * now the last, the last the first, and so on.
     *
     * This is not a straight forward reverse array.  It is vertex knowledgeable.
     * For example: given { ax, ay, bx, by, cx, cy}
     * will return        { cx, cy, bx, by, ax, ay}
     * will NOT return    { cy, cx, by, bx, ay, ax}
     */
    private static double[] reverseXYArray( double[] array )
    {
        double[] reversed = new double[array.length];
        int j = array.length - 1;
        for ( int i = 1; i < array.length; i += 2, j -= 2 )
        {
            reversed[i - 1] = array[j - 1];
            reversed[i] = array[j];
        }
        return reversed;
    }

    public static List<Point> convertEncVertexesToPoints( GeoProjection projection, ENCShape shape, int trackId )
    {
        assert ( shape.passSanityCheck( ) );
        if ( !shape.passSanityCheck( ) ) return Collections.emptyList( );

        int numCoords = shape.getNumCoordinates( );
        List<Point> points = new ArrayList<Point>( numCoords + 1 );
        for ( int i = 0; i < numCoords; i++ )
        {
            double lon = shape.getVertex( 0, i );
            if ( !validateLongitude( lon ) )
            {
                logger.info( "Ignoring bad longitude " + lon );
                continue;
            }

            double lat = shape.getVertex( 1, i );
            if ( !validateLatitude( lat ) )
            {
                logger.info( "Ignoring bad latitude " + lat );
                continue;
            }

            Vector2d vector = projection.project( LatLonGeo.fromDeg( lat, lon ) );
            points.add( new Point( trackId, i, vector.getX( ), vector.getY( ), i ) );
        }

        return points;
    }

    public static List<Point> convertGeoVertexesToPoints( GeoProjection projection, GeoShape shape, int trackId )
    {
        int numCoords = shape.getNumCoordinates( );
        List<Point> points = new ArrayList<Point>( numCoords + 1 );
        for ( int i = 0; i < numCoords; i++ )
        {
            double lon = shape.getVertex( 0, i );
            if ( !validateLongitude( lon ) )
            {
                logger.info( "Ignoring bad longitude " + lon );
                continue;
            }

            double lat = shape.getVertex( 1, i );
            if ( !validateLatitude( lat ) )
            {
                logger.info( "Ignoring bad latitude " + lat );
                continue;
            }

            Vector2d vector = projection.project( LatLonGeo.fromDeg( lat, lon ) );
            points.add( new Point( trackId, i, vector.getX( ), vector.getY( ), i ) );
        }

        return points;
    }

    public static float[] convertColorSpecToFloatArray( ColorSpec color )
    {
        if ( color.getR( ) > 1 || color.getG( ) > 1 || color.getB( ) > 1 || color.getA( ) > 1 )
        {
            return new float[] { color.getR( ) / 255, color.getG( ) / 255, color.getB( ) / 255, color.getA( ) / 255 };
        }
        else
        {
            return new float[] { color.getR( ), color.getG( ), color.getB( ), color.getA( ) };
        }
    }

    public static ColorSpec convertFloatsToColorSpec( float r, float g, float b, float a )
    {
        ColorSpec color = new ColorSpec( );
        color.setR( r );
        color.setG( g );
        color.setB( b );
        color.setA( a );
        return color;
    }

    public static ColorSpec convertFloatArrayToColorSpec( float[] rgba )
    {
        ColorSpec color = new ColorSpec( );
        color.setR( rgba[0] );
        color.setG( rgba[1] );
        color.setB( rgba[2] );
        color.setA( rgba[3] );
        return color;
    }

    public static FeatureSpec createEnc( String objectType, ShapeSpec polyShape, ShapeSpec... shapes )
    {
        FeatureSpec enc = new FeatureSpec( );
        List<String> objectTypeList = enc.getFeaturetype( );
        objectTypeList.add( objectType );
        List<ShapeSpec> shapeList = enc.getShapespec( );
        shapeList.addAll( Arrays.<ShapeSpec> asList( shapes ) );
        return enc;
    }

    public static FeatureSpec createEnc( String[] featureTypes, ShapeSpec polyShape, ShapeSpec... shapes )
    {
        FeatureSpec enc = new FeatureSpec( );
        List<String> objectTypeList = enc.getFeaturetype( );
        objectTypeList.addAll( Arrays.asList( featureTypes ) );
        List<ShapeSpec> shapeList = enc.getShapespec( );
        shapeList.addAll( Arrays.<ShapeSpec> asList( shapes ) );
        return enc;
    }

    public static ShapeSpec createShape( ShapeTypeSpec shapeType, TrackPainterSpec trackSpec )
    {
        PainterSpec painterSpec = new PainterSpec( );
        painterSpec.getPolygonpainterspecOrTrackpainterspecOrAnnotationpainterspec( ).add( trackSpec );
        ShapeSpec shape = new ShapeSpec( );
        shape.setShapetypespec( shapeType );
        shape.setPainterspec( painterSpec );
        return shape;
    }

    public static ShapeSpec createShape( ShapeTypeSpec shapeType, PolygonPainterSpec polySpec )
    {
        PainterSpec painterSpec = new PainterSpec( );
        painterSpec.getPolygonpainterspecOrTrackpainterspecOrAnnotationpainterspec( ).add( polySpec );
        ShapeSpec shape = new ShapeSpec( );
        shape.setShapetypespec( shapeType );
        shape.setPainterspec( painterSpec );
        return shape;
    }

}
