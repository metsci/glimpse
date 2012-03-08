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
package com.metsci.glimpse.charts.shoreline;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.charts.shoreline.LandShape.VertexConverter;
import com.metsci.glimpse.charts.shoreline.ndgc.NgdcFile2;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.painter.shape.PolygonPainter;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
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
public class LandShapePainter extends GlimpsePainter2D
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

    /**
     * Deprecated in favor of loadNdgcLandFile( InputStream in, GeoProjection tangentPlane )
     *  
     * @param in
     * @param tangentPlane
     * @throws IOException    
     */
    @Deprecated
    public void loadLandFile( InputStream in, GeoProjection tangentPlane ) throws IOException
    {
    	loadNdgcLandFile( in, tangentPlane );
    }
    
    public void loadNdgcLandFile( InputStream in, GeoProjection tangentPlane ) throws IOException
    {
    	loadNgdcLandFile0( in, tangentPlane );
    }
    
    /**
     * Deprecated in favor of loadNgdcLandFile( String file, GeoProjection tangentPlane )
     * 
     * @param file
     * @param tangentPlane
     * @throws IOException
     */
    @Deprecated
    public void loadLandFile( String file, GeoProjection tangentPlane ) throws IOException
    {
    	loadNgdcLandFile( file, tangentPlane );
    }
    
    public void loadNgdcLandFile( String file, GeoProjection tangentPlane ) throws IOException
    {
    	loadNgdcLandFile0( new FileInputStream( file ), tangentPlane );
    }

    /**
     * Deprecated in favor of loadNgdcLandFile( File file, GeoProjection tangentPlane )
     * @param file
     * @param tangentPlane
     * @throws IOException
     */
    @Deprecated
    public void loadLandFile( File file, GeoProjection tangentPlane ) throws IOException
    {
    	loadNgdcLandFile( file, tangentPlane );
    }
    
    public void loadNgdcLandFile( File file, GeoProjection tangentPlane ) throws IOException
    {
    	loadNgdcLandFile0( new FileInputStream( file ), tangentPlane );
    }

    /**
     * Deprecated in favor of loadNgdcLandFileAndCenterAxis( File file, GeoProjection tangentPlane, Axis2D axis )
     * @param file
     * @param tangentPlane
     * @param axis
     * @throws IOException
     */
    @Deprecated
    public void loadLandFileAndCenterAxis( File file, GeoProjection tangentPlane, Axis2D axis ) throws IOException
    {
    	loadNgdcLandFileAndCenterAxis( file, tangentPlane, axis );
    }
    
    public void loadNgdcLandFileAndCenterAxis( File file, GeoProjection tangentPlane, Axis2D axis ) throws IOException
    {
        NgdcFile2 ngdcFile = new NgdcFile2(  new FileInputStream( file ) );
        loadLandFileAndCenterAxis( ngdcFile, tangentPlane, axis );
    }

    public void loadLandFileAndCenterAxis( LandShapeCapable landFile, GeoProjection projection, Axis2D axis ) throws IOException
    {
    	loadLandFileAndCenterAxis(landFile.toShape( ), projection, axis);
    }

    public void loadLandFileAndCenterAxis( LandShape landShape, GeoProjection projection, Axis2D axis ) throws IOException
    {
        Shape shape = loadLandFile0( landShape, projection );
        Rectangle2D localBounds = shape.getBounds2D( );
        if (bounds == null) {
        	bounds = new Rectangle2D.Double();
        	bounds.setRect(localBounds);
        } else { 
        	bounds.add(localBounds);
        }
		
        axis.getAxisX( ).setMin( bounds.getMinX( ) );
        axis.getAxisX( ).setMax( bounds.getMaxX( ) );
        axis.getAxisY( ).setMin( bounds.getMinY( ) );
        axis.getAxisY( ).setMax( bounds.getMaxY( ) );
    }

    protected Shape loadNgdcLandFile0( InputStream in, final GeoProjection tangentPlane ) throws IOException
    {
    	NgdcFile2 ngdcFile = new NgdcFile2( in );
    	return loadLandFile0( ngdcFile.toShape(), tangentPlane );
    }
    
    protected Shape loadLandFile0( LandFile landFile, final GeoProjection tangentPlane ) throws IOException 
    {
    	return loadLandFile0( landFile.toShape( ), tangentPlane);
    }
    
    protected Shape loadLandFile0( LandShape landShape, final GeoProjection tangentPlane ) 
    {
        Shape shape = landShape.getFillShape( new VertexConverter( )
        {
            @Override
            public void toXY( double lat, double lon, Double xy )
            {
                Vector2d vector = tangentPlane.project( LatLonGeo.fromDeg( lat, lon ) );
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
    public void dispose( GLContext context )
    {
        polygonPainter.dispose( context );
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        polygonPainter.paintTo( context, bounds, axis );
    }
}
