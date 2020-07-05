/*
 * Copyright (c) 2019 Metron, Inc.
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
package com.metsci.glimpse.charts.shoreline;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.metsci.glimpse.util.geo.LatLonGeo;

/**
 * Representation of land (shoreline).
 *
 * Note: LandShape is typically created from a LandFile by calling the toShape() method.  This
 * shape is often cached using a LandManager class which allows the filename to be specified
 * as a parameter and then loads the file to create the LandShape in a static initializer.  An
 * example is shown below:
 * <code>
 * public class SampleLandManager <br>
 * { <br>
 *     private static final Params PARAMS = SampleLandManager.getParams(); <br>
 *     private static final String landFile = PARAMS.getValue(new StringParam("landFile", "")); <br>
 *     private static final LandShape landShape; <br>
 *     static <br>
 *     { <br>
 *        if (!landFile.isEmpty()) <br>
 *        { <br>
 *           landShape = (new NgdcFile(new File(landFilename))).toShape(); <br>
 *        } <br>
 *        else <br>
 *        { <br>
 *           landShape = null; <br>
 *        } <br>
 *     } <br>
 *     public static LandShape getLandShape() <br>
 *     { <br>
 *        return landShape; <br>
 *     } <br>
 * } <br>
 * </code>
 */
public class LandShape
{
    public static interface VertexConverter
    {
        void toXY( double lat, double lon, Point2D.Double xy );
    }

    private final List<LandSegment> segments;
    private final LandVertex swCorner;
    private final LandVertex neCorner;
    private final LandBox box;

    private final Shape suShape;
    private final VertexConverter suConverter;

    private final boolean invertFill;

    public LandShape( List<LandSegment> segments, LandBox box )
    {
        assert segments != null;

        this.box = box;
        this.segments = Collections.unmodifiableList( new ArrayList<LandSegment>( segments ) );
        this.swCorner = new LandVertex( box.southLat, box.westLon );
        this.neCorner = new LandVertex( box.northLat, box.eastLon );
        this.suConverter = new VertexConverter( )
        {
            public void toXY( double lat, double lon, Point2D.Double xy )
            {
                xy.x = ( swCorner.getDistanceX_SU( lon ) );
                xy.y = ( swCorner.getDistanceY_SU( lat ) );
            }
        };

        Shape rawSuShape = getRawFillShape( suConverter );
        this.invertFill = ( rawSuShape.contains( 0, 0 ) != box.isSwCornerLand );
        this.suShape = ( invertFill ? invert( rawSuShape, suConverter ) : rawSuShape );
    }

    public boolean isLand( double latDeg, double lonDeg )
    {
        Point2D.Double xy = new Point2D.Double( );
        suConverter.toXY( latDeg, lonDeg, xy );
        return suShape.contains( xy.getX( ), xy.getY( ) );
    }

    public Shape getStrokeShape( VertexConverter converter )
    {
        Path2D stroke = new Path2D.Double( );
        Point2D.Double xy = new Point2D.Double( );

        for ( LandSegment segment : segments )
        {
            LandVertex vertex0 = segment.vertices.get( 0 );
            converter.toXY( vertex0.lat, vertex0.lon, xy );
            stroke.moveTo( xy.getX( ), xy.getY( ) );

            for ( int i = 1; i < segment.vertices.size( ); i++ )
            {
                LandVertex vertex = segment.vertices.get( i );
                converter.toXY( vertex.lat, vertex.lon, xy );
                stroke.lineTo( xy.getX( ), xy.getY( ) );
            }
        }
        return stroke;
    }

    public Shape getFillShape( VertexConverter converter )
    {
        Shape fill = getRawFillShape( converter );
        return ( invertFill ? invert( fill, converter ) : fill );
    }

    private Shape getRawFillShape( VertexConverter converter )
    {
        Path2D fill = new Path2D.Double( Path2D.WIND_EVEN_ODD );
        Point2D.Double xy = new Point2D.Double( );

        for ( LandSegment segment : segments )
        {
            if ( !segment.isFillable ) continue;

            LandVertex vertex0 = segment.vertices.get( 0 );
            converter.toXY( vertex0.lat, vertex0.lon, xy );
            fill.moveTo( xy.getX( ), xy.getY( ) );

            for ( int i = 1; i < segment.vertices.size( ); i++ )
            {
                LandVertex vertex = segment.vertices.get( i );
                converter.toXY( vertex.lat, vertex.lon, xy );
                fill.lineTo( xy.getX( ), xy.getY( ) );
            }

            for ( LandVertex ghostVertex : segment.ghostVertices )
            {
                converter.toXY( ghostVertex.lat, ghostVertex.lon, xy );
                fill.lineTo( xy.getX( ), xy.getY( ) );
            }

            converter.toXY( vertex0.lat, vertex0.lon, xy );
            fill.lineTo( xy.getX( ), xy.getY( ) );
        }
        return fill;
    }

    private Shape invert( Shape shape, VertexConverter converter )
    {
        Point2D.Double sw = new Point2D.Double( );
        converter.toXY( swCorner.lat, swCorner.lon, sw );

        Point2D.Double ne = new Point2D.Double( );
        converter.toXY( neCorner.lat, neCorner.lon, ne );

        double x = Math.min( sw.getX( ), ne.getX( ) );
        double y = Math.min( sw.getY( ), ne.getY( ) );
        double w = Math.abs( sw.getX( ) - ne.getX( ) );
        double h = Math.abs( sw.getY( ) - ne.getY( ) );

        Area inverted = new Area( new Rectangle2D.Double( x, y, w, h ) );
        inverted.subtract( new Area( shape ) );
        return inverted;
    }

    public LatLonGeo getSwCorner( )
    {
        return LatLonGeo.fromDeg( swCorner.lat, swCorner.lon );
    }

    public LatLonGeo getNeCorner( )
    {
        return LatLonGeo.fromDeg( neCorner.lat, neCorner.lon );
    }

    public List<LandSegment> getSegments( )
    {
        return segments;
    }

    public LandBox getLandBox( )
    {
        return box;
    }
}
