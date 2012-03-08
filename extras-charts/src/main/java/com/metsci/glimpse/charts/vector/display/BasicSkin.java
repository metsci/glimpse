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
import com.metsci.glimpse.charts.vector.display.xmlgen.FeatureSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.Geoskinspec;
import com.metsci.glimpse.charts.vector.display.xmlgen.PainterSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.PolygonPainterSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.ShapeSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.ShapeTypeSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.TrackPainterSpec;
import com.metsci.glimpse.charts.vector.parser.objects.GeoFeatureType;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShapeType;
import com.metsci.glimpse.util.io.StreamOpener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import org.xml.sax.SAXException;

public class BasicSkin< V extends GeoObject > implements Skin< V >
{
    private static Logger logger = Logger.getLogger( BasicSkin.class.toString( ) );

    private static final GeoShapeType[] shapeTypes = GeoShapeType.values( );

    private Map<Integer, List<ContentHandler<V>>> lookupMap;
    private Set<GeoFeatureType> debugWarningMap;
    private Geoskinspec skinSpec;
    private BasicSkinHelper<V> helper;

    public BasicSkin( BasicSkinHelper<V> helper, String resource ) throws JAXBException, IOException, SAXException
    {
        this( helper, StreamOpener.fileThenResource.openForRead( resource ) );
    }

    public BasicSkin( BasicSkinHelper<V> helper, InputStream xmlStream ) throws JAXBException, SAXException
    {
        assert( helper != null );
        this.helper= helper;

        JAXBContext jaxbContext = JAXBContext.newInstance( Geoskinspec.class );
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller( );

        Schema schema = helper.getXsdSchema();
        if ( schema != null )
            unmarshaller.setSchema( schema );

        unmarshaller.setEventHandler( new javax.xml.bind.helpers.DefaultValidationEventHandler( ) );
        Object oSkin = unmarshaller.unmarshal( xmlStream );
        skinSpec = ( Geoskinspec ) oSkin;
        buildHandlersFromXmlSkin( );
    }

    public BasicSkin( BasicSkinHelper<V> helper, Geoskinspec skin )
    {
        assert( helper != null );
        this.helper= helper;
        this.skinSpec = skin;
        buildHandlersFromXmlSkin( );
    }

    private void buildHandlersFromXmlSkin( )
    {
        lookupMap = new HashMap<Integer, List<ContentHandler<V>>>( );
        debugWarningMap = new HashSet<GeoFeatureType>( );
        List<FeatureSpec> encList = skinSpec.getFeature( );
        for ( FeatureSpec enc : encList )
        {
            List<? extends GeoFeatureType> featureTypeList = helper.convertGeoFeatureType( enc.getFeaturetype() );
            debugWarningMap.addAll( featureTypeList );
            List<ShapeSpec> shapeSpecList = enc.getShapespec( );
            for ( ShapeSpec shapeSpec : shapeSpecList )
            {
                GeoShapeType shapeType = convertShapeType( shapeSpec.getShapetypespec( ) );
                PainterSpec painterSpecs = shapeSpec.getPainterspec( );
                List<ContentHandler<V>> contentHandlerList = new ArrayList<ContentHandler<V>>( );
                // The following method name would not have been my first choice.  It was all jaxb.
                List<Object> painterSpecList = painterSpecs.getPolygonpainterspecOrTrackpainterspecOrAnnotationpainterspec( );
                for ( Object painterSpec : painterSpecList )
                {
                    if ( painterSpec instanceof PolygonPainterSpec )
                    {
                        PolygonPainterSpec polygonSpec = ( PolygonPainterSpec ) painterSpec;
                        contentHandlerList.add( new PolygonHandler<V>( polygonSpec ) );
                    }
                    else if ( painterSpec instanceof TrackPainterSpec )
                    {
                        TrackPainterSpec trackSpec = ( TrackPainterSpec ) painterSpec;
                        contentHandlerList.add( new TrackHandler<V>( trackSpec ) );
                    }
                    else if ( painterSpec instanceof AnnotationPainterSpec )
                    {
                        contentHandlerList.add( helper.createAnnotationPainterSpec( ( AnnotationPainterSpec ) painterSpec ) );
                    }
                    else
                    {
                        assert ( false ) : "Unknown PainterSpec " + painterSpec.getClass( );
                    }
                }
                for ( GeoFeatureType featureType : featureTypeList )
                {
                    lookupMap.put( determineGroupId( featureType, shapeType ), contentHandlerList );
                }
            }
        }
    }

    private GeoShapeType convertShapeType( ShapeTypeSpec type )
    {
        return GeoShapeType.valueOf( type.value( ) );
    }

    /*
    private ENCObjectType convertENCObjectType( String type )
    {
        return ENCObjectType.valueOf( type );
    }


    private List<ENCObjectType> convertENCObjectType(List<String> featureTypeList)
    {
        List<ENCObjectType> realList = new ArrayList<ENCObjectType>( featureTypeList.size( ) );
        for ( String featureType : featureTypeList )
        {
            realList.add( convertENCObjectType( featureType ) );
        }
        return realList;
    } */

    private int determineGroupId( GeoFeatureType featureType, GeoShapeType shapeType )
    {
        assert(featureType != null);
        assert(shapeType != null);
        return shapeType.ordinal( ) + ( featureType.ordinal( ) * shapeTypes.length );
        //return featureType.ordinal( ) + 10000 * ( shapeType.ordinal( ) + shapeTypes.length + 1 );
    }

    @Override
    public List<ContentHandler<V>> getHandlersForGeoObject(V geoObject)
    {
        Collection<? extends GeoShape> shapeList = geoObject.getGeoShapes( );
        if ( shapeList.isEmpty( ) )
        {
            return Collections.<ContentHandler<V>> emptyList( );
        }

        List<ContentHandler<V>> handlers = getHandlersOfType( geoObject.getGeoFeatureType( ), geoObject.getFirstGeoShape( ).getShapeType( ) );
        if ( handlers == null )
            return Collections.<ContentHandler<V>> emptyList( );
        else
            return handlers;
    }


    private List<ContentHandler<V>> getHandlersOfType( GeoFeatureType featureType, GeoShapeType shapeType )
    {
        int handlerId = determineGroupId( featureType, shapeType );
        List<ContentHandler<V>> handlerList = lookupMap.get( handlerId );
        if ( handlerList == null )
        {
            if ( debugWarningMap.contains( featureType ) ) logger.warning( "No handler for known enc object type " + featureType + ", shapeType: " + shapeType );
        }
        return handlerList;
    }

    @Override
    public void reset( )
    {
        buildHandlersFromXmlSkin( );
    }
}
