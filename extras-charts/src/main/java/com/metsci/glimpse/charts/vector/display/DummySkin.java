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

import com.metsci.glimpse.charts.vector.parser.objects.ENCShape;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShapeType;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Logger;

public class DummySkin < V extends GeoObject > implements Skin< V >
{
    private static Logger logger = Logger.getLogger( DummySkin.class.toString( ) );

    private EnumMap<GeoShapeType, ContentHandler<V>> lookupMap;
    private Color color;

    public DummySkin( )
    {
        this( Color.gray );
    }

    public DummySkin( Color color )
    {
        this.color = color;
        reset( );
    }

    public ContentHandler<V> getHandlerForShape( V geoObject, ENCShape shape, int shapeIndex )
    {
        return lookupMap.get( shape.getShapeType( ) );
    }

    @Override
    public List<ContentHandler<V>> getHandlersForGeoObject( V geoObject )
    {
        Collection<? extends GeoShape> shapeList = geoObject.getGeoShapes( );
        if ( shapeList.isEmpty( ) )
            return Collections.<ContentHandler<V>> emptyList( );
        else
        {
            ContentHandler<V> singleHandler = lookupMap.get( geoObject.getFirstGeoShape( ).getShapeType( ) );
            return Collections.<ContentHandler<V>> singletonList( singleHandler );
        }
    }

    @Override
    public void reset( )
    {
        lookupMap = new EnumMap<GeoShapeType, ContentHandler<V>>( GeoShapeType.class );
        lookupMap.put( GeoShapeType.Polygon, createDefaultPolygonHandlerForPolygonType( ) );
        lookupMap.put( GeoShapeType.Linestring, createDefaultTrackHandlerForLinestringType( ) );
        lookupMap.put( GeoShapeType.Point, createDefaultTrackHandlerForPointType( ) );
        lookupMap.put( GeoShapeType.Multipoint, createDefaultTrackHandlerForMultiPointType( ) );
    }

    private PolygonHandler<V> createDefaultPolygonHandlerForPolygonType( )
    {
        PolygonHandler<V> polygonHandler = new PolygonHandler<V>( );
        polygonHandler.setShowLines( Boolean.TRUE );
        polygonHandler.setFill( true );
        polygonHandler.setFillColor( 1, 0, 0, 0.25f );
        polygonHandler.setLineColor( color.getComponents( null ) );
        return polygonHandler;
    }

    private TrackHandler<V> createDefaultTrackHandlerForLinestringType( )
    {
        TrackHandler<V> trackHandler = new TrackHandler<V>( );
        trackHandler.setShowLines( true );
        trackHandler.setLineColor( color.getComponents( null ) );
        trackHandler.setPointColor( color.getComponents( null ) );
        return trackHandler;
    }

    private TrackHandler<V> createDefaultTrackHandlerForMultiPointType( )
    {
        return createDefaultTrackHandlerForPointType( );
    }

    private TrackHandler<V> createDefaultTrackHandlerForPointType( )
    {
        TrackHandler<V> trackHandler = new TrackHandler<V>( );
        trackHandler.setShowLines( false );
        trackHandler.setPointColor( color.getComponents( null ) );
        return trackHandler;
    }
}
