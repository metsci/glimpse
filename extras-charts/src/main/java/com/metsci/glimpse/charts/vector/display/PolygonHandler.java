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
package com.metsci.glimpse.charts.vector.display;

import com.metsci.glimpse.charts.vector.display.xmlgen.PolygonPainterSpec;
import com.metsci.glimpse.charts.vector.painter.EncPainterUtils;
import com.metsci.glimpse.charts.vector.parser.objects.GeoFeatureType;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;
import com.metsci.glimpse.painter.shape.PolygonPainter;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * This content handler renders GeoObjects by drawing polygons. It delegates the
 * painting of the geo objects to the
 * com.metsci.glimpse.painter.shape.PolygonPainter. The drawing characteristics
 * of the rendered polygons are determined by the feature type of the geoobject
 * being rendered and the PolygonPainterSpec passed to this class in the
 * constructor.  The values in the PolygonPainterSpec are determined by the skin
 * xml config file.  In a nutshell, this class gets a GeoObject to render, looks
 * up the drawing specs from the PolygonPainterSpec, configures the PolygonPainter
 * to those specs, and invokes the PolygonPainter to render the geoobject.
 *   
 * 
 * @author Cunningham 
 */
public class PolygonHandler<V extends GeoObject> implements ContentHandler<V>
{
    private static final int defaultLineStippleFactor = 1;
    private static final short defaultLineStipplePattern = ( short ) 0x00FF;

    private PolygonPainterSpec polygonGroupAttributes;

    private Set<GeoFeatureType> groupInitSet = new HashSet<GeoFeatureType>( );

    private int polyId = 0;

    public PolygonHandler( )
    {
        this( new PolygonPainterSpec( ) );
    }

    public PolygonHandler( PolygonPainterSpec spec )
    {
        assert ( spec != null );
        if ( spec == null ) throw new AssertionError( );
        this.polygonGroupAttributes = spec;
    }

    private int determineGroupId( GeoFeatureType objectType )
    {
        return objectType.ordinal( );
    }

    @Override
    public void paintEnc( GeoContext encContext, V object )
    {
        Collection<? extends GeoShape> shapeList = object.getGeoShapes( );
        if ( shapeList.isEmpty( ) ) return;

        PolygonPainter polygonPainter = encContext.getPolyPainter( );

        GeoProjection projection = encContext.getProjection( );
        GeoFeatureType featureType = object.getGeoFeatureType();
        if ( !groupInitSet.contains( featureType ) )
        {
            initializeGroupPainterSettings( featureType.ordinal( ), polygonPainter );
            groupInitSet.add( featureType );
        }

        Polygon polygon = EncPainterUtils.convertGeoVertexesToPolgon( projection, object );
        polygonPainter.addPolygon( featureType.ordinal( ), polyId++, polygon, 1 );
    }

    @Override
    public Integer getScaleMin(V object) {
        return null;
    }

    @Override
    public void reset( )
    {
        groupInitSet.clear( );
        polyId = 0;
    }

    private void initializeGroupPainterSettings( int groupId, PolygonPainter polygonPainter )
    {
        if ( polygonGroupAttributes.isShowLines( ) != null )
        {
            polygonPainter.setShowLines( groupId, polygonGroupAttributes.isShowLines( ).booleanValue( ) );
        }

        if ( polygonGroupAttributes.getLineColor( ) != null )
        {
            polygonPainter.setLineColor( groupId, EncPainterUtils.convertColorSpecToFloatArray( polygonGroupAttributes.getLineColor( ) ) );
        }

        if ( polygonGroupAttributes.getLineWidth( ) != null )
        {
            polygonPainter.setLineWidth( groupId, polygonGroupAttributes.getLineWidth( ).intValue( ) );
        }

        if ( polygonGroupAttributes.isLineDotted( ) != null )
        {
            boolean isLineDotted = polygonGroupAttributes.isLineDotted( ).booleanValue( );
            polygonPainter.setLineDotted( groupId, isLineDotted );

            if ( isLineDotted )
            {
                int lineStippleFactor = defaultLineStippleFactor;

                if ( polygonGroupAttributes.getLineStippleFactor( ) != null )
                {
                    lineStippleFactor = polygonGroupAttributes.getLineStippleFactor( ).intValue( );
                }

                short lineStipplePattern = defaultLineStipplePattern;

                if ( polygonGroupAttributes.getLineStipplePattern( ) != null )
                {
                    lineStipplePattern = polygonGroupAttributes.getLineStipplePattern( ).shortValue( );
                }

                polygonPainter.setLineDotted( groupId, lineStippleFactor, lineStipplePattern );
            }
        }

        if ( polygonGroupAttributes.isPolyDotted( ) != null )
        {
            boolean isPolyDotted = polygonGroupAttributes.isPolyDotted( ).booleanValue( );
            polygonPainter.setPolyDotted( groupId, isPolyDotted );

            if ( isPolyDotted && polygonGroupAttributes.getPolyStipplePattern( ) != null )
            {
                polygonPainter.setPolyDotted( groupId, polygonGroupAttributes.getPolyStipplePattern( ) );
            }
        }

        if ( polygonGroupAttributes.isFill( ) != null )
        {
            boolean isFill = polygonGroupAttributes.isFill( );
            polygonPainter.setFill( groupId, isFill );
            if ( isFill && polygonGroupAttributes.getFillColor( ) != null )
            {
                polygonPainter.setFillColor( groupId, EncPainterUtils.convertColorSpecToFloatArray( polygonGroupAttributes.getFillColor( ) ) );
            }
        }
    }

    public void setShowLines( boolean show )
    {
        polygonGroupAttributes.setShowLines( Boolean.valueOf( show ) );
    }

    public void setLineColor( float r, float g, float b, float a )
    {
        polygonGroupAttributes.setLineColor( EncPainterUtils.convertFloatsToColorSpec( r, g, b, a ) );
    }

    public void setLineColor( float[] rgba )
    {
        polygonGroupAttributes.setLineColor( EncPainterUtils.convertFloatArrayToColorSpec( rgba ) );
    }

    public void setLineWidth( int width )
    {
        polygonGroupAttributes.setLineWidth( Integer.valueOf( width ) );
    }

    public void setPolyDotted( byte[] stipple )
    {
        polygonGroupAttributes.setPolyStipplePattern( stipple );
    }

    public void setPolyDotted( boolean dotted )
    {
        polygonGroupAttributes.setPolyDotted( Boolean.valueOf( dotted ) );
    }

    public void setLineDotted( boolean dotted )
    {
        polygonGroupAttributes.setLineDotted( Boolean.valueOf( dotted ) );
    }

    public void setLineDotted( int stippleFactor, short stipplePattern )
    {
        polygonGroupAttributes.setLineStippleFactor( Integer.valueOf( stippleFactor ) );
        polygonGroupAttributes.setLineStipplePattern( Short.valueOf( stipplePattern ) );
    }

    public void setFill( boolean show )
    {
        polygonGroupAttributes.setFill( Boolean.valueOf( show ) );
    }

    public void setFillColor( float r, float g, float b, float a )
    {
        polygonGroupAttributes.setFillColor( EncPainterUtils.convertFloatsToColorSpec( r, g, b, a ) );
    }

    public void setFillColor( float[] rgba )
    {
        polygonGroupAttributes.setFillColor( EncPainterUtils.convertFloatArrayToColorSpec( rgba ) );
    }

}
