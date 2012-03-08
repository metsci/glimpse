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

import com.metsci.glimpse.charts.vector.display.xmlgen.AnnotationPainterSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.EncAnnotationPainterSpec;
import com.metsci.glimpse.charts.vector.painter.EncPainterUtils;
import com.metsci.glimpse.charts.vector.parser.attributes.GeoAttribute;
import com.metsci.glimpse.charts.vector.parser.objects.GeoAttributeType;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;
import com.metsci.glimpse.painter.info.AnnotationPainter;
import com.metsci.glimpse.painter.info.AnnotationPainter.Annotation;
import com.metsci.glimpse.painter.info.AnnotationPainter.AnnotationFont;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.math.stat.StatCollectorNDim;
import com.metsci.glimpse.util.vector.Vector2d;

import java.util.Arrays;
import java.util.logging.Logger;


/**
 * This content handler renders text attributes from the GeoObjects. It
 * delegates the text painting to the
 * com.metsci.glimpse.painter.info.AnnotationPainter. The font and text
 * characteristics of the text are determined by the AnnotationPainterSpec
 * passed in at the constructor. The specific text attribute being rendered is
 * delegated to the super class via the getAttributeTypeToAnnotate() abstract
 * method.
 * 
 * This is a crude first cut. Currently text is placed at the center point. No
 * effort is done to eliminate text overlap.
 * 
 * @author Cunningham
 */
public abstract class AnnotationHandler<V extends GeoObject> implements ContentHandler<V>
{
    private static final int centerPointCalculationCap = -1;
    private static Logger logger = Logger.getLogger( AnnotationHandler.class.toString( ) );

    private AnnotationPainterSpec textAttributeSpec;


    public AnnotationHandler( )
    {
        this( new EncAnnotationPainterSpec( ) );
    }

    public AnnotationHandler( AnnotationPainterSpec spec )
    {
        assert ( spec != null );
        if ( spec == null ) throw new AssertionError( );
        textAttributeSpec = spec;
    }

    public abstract GeoAttributeType getAttributeTypeToAnnotate( );

    public void reset( )
    {
    }

    @Override
    public void paintEnc( GeoContext geoContext, V object )
    {
        if ( object.getGeoShapes( ).isEmpty( ) ) return;

        GeoAttribute attributeValue = object.getGeoAttribute( getAttributeTypeToAnnotate( ) );
        if ( attributeValue == null )
        {
            return;
        }
        String attributeValueText = attributeValue.getAttributeValueAsString( );
        if ( attributeValueText == null )
        {
            return;
        }

        AnnotationPainter annotationPainter = geoContext.getAnnotationPainter( );
        double[] centerXY = findCenterPoint( geoContext.getProjection( ), object );
        Annotation annotation = new Annotation( attributeValueText, ( float ) centerXY[0], ( float ) centerXY[1], true, true );
        polishAnnotation( object, annotation );

        assert ( annotation.getText( ) != null ) : "null text";
        annotationPainter.addAnnotation( annotation );
    }

    @Override
    public Integer getScaleMin( V object )
    {
        return null;
    }

    private double[] findCenterPoint( GeoProjection projection, V object )
    {
        GeoShape shape = object.getGeoShapes( ).iterator( ).next( );

        int numVertexes = shape.getNumCoordinates( );
        int vertexIncrement = 1;
        if ( centerPointCalculationCap > 0 && numVertexes > centerPointCalculationCap ) 
        	vertexIncrement = ( int ) ( shape.getNumCoordinates( ) / centerPointCalculationCap );

        StatCollectorNDim center = new StatCollectorNDim( 2 );
        double [ ] vectorXYValues = new double[ 2 ];
        for ( int v = 0; v < numVertexes; v += vertexIncrement )
        {
            double lon = shape.getVertex( 0, v );
            double lat = shape.getVertex( 1, v );
            Vector2d vector = projection.project( LatLonGeo.fromDeg( lat, lon ) );
            vectorXYValues[ 0 ] = vector.getX( );
            vectorXYValues[ 1 ] = vector.getY( );
            center.addElement( vectorXYValues );
        }

        return center.getMean( );
    }

    private void polishAnnotation( V object, Annotation annotation )
    {
        if ( textAttributeSpec.isCenterX( ) != null ) annotation.setCenterX( textAttributeSpec.isCenterX( ) );
        if ( textAttributeSpec.isCenterY( ) != null ) annotation.setCenterY( textAttributeSpec.isCenterY( ) );

        if ( textAttributeSpec.getColor( ) != null ) annotation.setColor( EncPainterUtils.convertColorSpecToFloatArray( textAttributeSpec.getColor( ) ) );

        if ( textAttributeSpec.getFont( ) != null )
        {
            AnnotationFont font = AnnotationFont.valueOf( textAttributeSpec.getFont( ).value( ) );
            annotation.setFont( font.getFont( ) );
        }
    }


}
