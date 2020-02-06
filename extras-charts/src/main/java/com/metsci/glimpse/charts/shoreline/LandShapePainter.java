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
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;

import com.metsci.glimpse.charts.shoreline.LandShape.VertexConverter;
import com.metsci.glimpse.charts.shoreline.ndgc.NgdcFile2;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.core.painter.shape.PolygonPainter;
import com.metsci.glimpse.core.support.polygon.Polygon;
import com.metsci.glimpse.core.support.polygon.Polygon.Interior;
import com.metsci.glimpse.core.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Originally designed to display shoreline data available from the NOAA/NGDC Coastline Extractor
 * tool at http://www.ngdc.noaa.gov/mgg_coastline/</p>
 *
 * Now capable of painting all LandShapes, NdgcFile shapes as wells as Gshhs shapes.
 *
 * When downloading ndgc data, choose the following options:
 * World Vector Shoreline
 * Compression Method: None
 * Coast Format: Mapgen
 * Coast Preview: No Preview
 *
 *
 * @author ulman
 * @author cunningham
 */
public class LandShapePainter extends GlimpsePainterBase
{
    protected static final int LAND_GROUP_ID = 1337;

    protected PolygonPainter polygonPainter;
    protected int landPolygonCounter = 0;
    protected Rectangle2D bounds;

    public LandShapePainter( )
    {
        this.polygonPainter = new PolygonPainter( );
        this.setFillColor( 151 / 255.0f, 152 / 255.0f, 107 / 255.0f, 1.0f );
        this.setLineColor( 112 / 255.0f, 140 / 255.0f, 76 / 255.0f, 1.0f );
        this.setLineWidth( 2 );
        this.setFill( true );
    }

    public void loadNgdcLandFileAndCenterAxis( URL url, GeoProjection geoProjection, Axis2D axis ) throws IOException
    {
        Shape shape = this.loadNgdcLandFile( url, geoProjection );
        centerAxesOnShape( shape, axis );
    }

    public Shape loadNgdcLandFile( URL url, GeoProjection geoProjection ) throws IOException
    {
        NgdcFile2 ngdcFile = new NgdcFile2( url );
        return loadLandFile0( ngdcFile.toShape( ), geoProjection );
    }

    public void centerAxesOnShape( Shape shape, Axis2D axis )
    {
        Rectangle2D localBounds = shape.getBounds2D( );

        if ( bounds == null )
        {
            bounds = new Rectangle2D.Double( );
            bounds.setRect( localBounds );
        }
        else
        {
            bounds.add( localBounds );
        }

        axis.getAxisX( ).setMin( bounds.getMinX( ) );
        axis.getAxisX( ).setMax( bounds.getMaxX( ) );
        axis.getAxisY( ).setMin( bounds.getMinY( ) );
        axis.getAxisY( ).setMax( bounds.getMaxY( ) );
    }

    protected Shape loadNgdcLandFile0( URL url, final GeoProjection geoProjection ) throws IOException
    {
        NgdcFile2 ngdcFile = new NgdcFile2( url );
        return loadLandFile0( ngdcFile.toShape( ), geoProjection );
    }

    protected Shape loadLandFile0( LandFile landFile, final GeoProjection geoProjection ) throws IOException
    {
        return loadLandFile0( landFile.toShape( ), geoProjection );
    }

    protected Shape loadLandFile0( LandShape landShape, final GeoProjection geoProjection )
    {
        Shape shape = landShape.getFillShape( new VertexConverter( )
        {
            @Override
            public void toXY( double lat, double lon, Double xy )
            {
                Vector2d vector = geoProjection.project( LatLonGeo.fromDeg( lat, lon ) );
                xy.x = vector.getX( );
                xy.y = vector.getY( );
            }
        } );

        //XXX Here we load every Shape segment as a different polygon
        //XXX This won't work for shapes with holes, still need to figure this out
        Polygon p = new Polygon( );
        PathIterator iter = shape.getPathIterator( null );
        double[] vertices = new double[6];
        LoopBuilder b = new LoopBuilder( );

        while ( !iter.isDone( ) )
        {
            iter.next( );

            int type = iter.currentSegment( vertices );

            if ( type == PathIterator.SEG_CLOSE )
            {
                p.add( b.complete( Interior.onRight ) );
                addPolygon( p );
                p = new Polygon( );
                b = new LoopBuilder( );
            }
            else if ( type == PathIterator.SEG_LINETO )
            {
                b.addVertices( vertices, 1 );
            }
            else if ( type == PathIterator.SEG_MOVETO )
            {
                p.add( b.complete( Interior.onRight ) );
                addPolygon( p );
                p = new Polygon( );
                b = new LoopBuilder( );
                b.addVertices( vertices, 1 );
            }
            else
            {
                throw new UnsupportedOperationException( "Shape Not Supported." );
            }
        }

        return shape;
    }

    protected void addPolygon( Polygon p )
    {
        polygonPainter.addPolygon( LAND_GROUP_ID, landPolygonCounter++, Long.MIN_VALUE, Long.MAX_VALUE, p, 0.0f );
    }

    public void setLineColor( float r, float g, float b, float a )
    {
        polygonPainter.setLineColor( LAND_GROUP_ID, r, g, b, a );
    }

    public void setLineColor( float[] rgba )
    {
        polygonPainter.setLineColor( LAND_GROUP_ID, rgba );
    }

    public void setLineWidth( int width )
    {
        polygonPainter.setLineWidth( LAND_GROUP_ID, width );
    }

    public void setShowLines( boolean show )
    {
        polygonPainter.setShowLines( LAND_GROUP_ID, show );
    }

    public void setPolyDotted( byte[] stipple )
    {
        polygonPainter.setPolyDotted( LAND_GROUP_ID, stipple );
    }

    public void setPolyDotted( boolean dotted )
    {
        polygonPainter.setPolyDotted( LAND_GROUP_ID, dotted );
    }

    public void setLineDotted( boolean dotted )
    {
        polygonPainter.setLineDotted( LAND_GROUP_ID, dotted );
    }

    public void setLineDotted( int stippleFactor, short stipplePattern )
    {
        polygonPainter.setLineDotted( LAND_GROUP_ID, stippleFactor, stipplePattern );
    }

    public void setFill( boolean show )
    {
        polygonPainter.setFill( LAND_GROUP_ID, show );
    }

    public void setFillColor( float[] rgba )
    {
        polygonPainter.setFillColor( LAND_GROUP_ID, rgba );
    }

    public void setFillColor( float r, float g, float b, float a )
    {
        polygonPainter.setFillColor( LAND_GROUP_ID, r, g, b, a );
    }

    public void deleteAll( )
    {
        polygonPainter.deleteAll( );
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        polygonPainter.doDispose( context );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        polygonPainter.doPaintTo( context );
    }
}
