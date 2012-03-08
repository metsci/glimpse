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
import com.metsci.glimpse.charts.vector.display.xmlgen.DncAnnotationPainterSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.Geoskinspec;
import com.metsci.glimpse.charts.vector.iteration.DNCObjectLoader;
import com.metsci.glimpse.charts.vector.iteration.StreamToGeoObjectConverter;
import com.metsci.glimpse.charts.vector.parser.objects.DNCFeatureCode;
import com.metsci.glimpse.charts.vector.parser.objects.DNCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoFeatureType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;


public class DNCBasicSkinHelper implements BasicSkinHelper<DNCObject>
{
    private static Logger logger = Logger.getLogger( DNCBasicSkinHelper.class.toString( ) );


    @Override
    public Schema getXsdSchema( ) throws SAXException
    {
        SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        return schemaFactory.newSchema( getClass( ).getClassLoader( ).getResource( "data/dncskin.xsd" ) );
    }


    @Override
    public Skin<DNCObject> createDefaultHardCodedSkin( )
    {
        return createDefaultHardCodedSkin( ColorPalette.createDefaultColorPalette( ) );
    }

    @Override
    public Skin<DNCObject> createDefaultHardCodedSkin( ColorPalette colorPalette )
    {
        Geoskinspec skinSpec = new HardCodedDNCSkinSpecFactory( colorPalette ).createSkinSpec( );
        return new BasicSkin<DNCObject>( new DNCBasicSkinHelper(), skinSpec );
    }

    @Override
    public ContentHandler<DNCObject> createAnnotationPainterSpec( AnnotationPainterSpec annotationSpec )
    {
        return new DNCAnnotationHandler( ( DncAnnotationPainterSpec ) annotationSpec);
    }

    @Override
    public boolean isBestBoundaryType( DNCObject dnc )
    {
        // to do, find a better coverage feature code
        return ( dnc.getFeatureCode() == DNCFeatureCode.Coastline );
    }

    @Override
    public boolean isSecondBestBoundaryType( DNCObject dnc )
    {
        return false;
    }

    @Override
    public StreamToGeoObjectConverter<DNCObject> createGeoObjectLoader( )
    {
        return new DNCObjectLoader( );
    }

    @Override
    public GeoFeatureType convertGeoFeatureType( String type )
    {
        return DNCFeatureCode.fromKey( type );
    }

    @Override
    public List<? extends GeoFeatureType> convertGeoFeatureType(List<String> featureTypeList)
    {
        List<DNCFeatureCode> realList = new ArrayList<DNCFeatureCode>( featureTypeList.size( ) );
        for ( String featureType : featureTypeList )
        {
            //realList.add( internalConvertGeoFeatureType( featureType ) );
            DNCFeatureCode code = internalConvertGeoFeatureType( featureType );
            assert (code != null) : "No feature code for type " + featureType;
            realList.add( code );
        }
        return realList;
    }

    public String geoName() {
        return "DNC";
    }

    private DNCFeatureCode internalConvertGeoFeatureType( String type )
    {
        return DNCFeatureCode.fromKey( type );
    }

}
