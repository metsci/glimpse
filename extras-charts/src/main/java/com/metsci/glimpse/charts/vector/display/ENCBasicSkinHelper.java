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
import com.metsci.glimpse.charts.vector.display.xmlgen.Geoskinspec;
import com.metsci.glimpse.charts.vector.iteration.ENCObjectLoader;
import com.metsci.glimpse.charts.vector.iteration.StreamToGeoObjectConverter;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCEnumAttribute;
import com.metsci.glimpse.charts.vector.parser.autogen.ENCAttributeType;
import com.metsci.glimpse.charts.vector.parser.autogen.ENCAttributeValues;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObjectType;
import com.metsci.glimpse.charts.vector.parser.objects.GeoFeatureType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;


public class ENCBasicSkinHelper implements BasicSkinHelper<ENCObject>
{
    private static Logger logger = Logger.getLogger( ENCBasicSkinHelper.class.toString( ) );

   // encobjectypes whose vertexes tend to completely surround the whole map
    private static final Set<GeoFeatureType> bestMapBoundaryTypes = new HashSet<GeoFeatureType>( );
    static
    {
        bestMapBoundaryTypes.add( ENCObjectType.NauticalPublicationInformation );
        bestMapBoundaryTypes.add( ENCObjectType.NavigationalSystemOfMarks );
        bestMapBoundaryTypes.add( ENCObjectType.Coverage );
        bestMapBoundaryTypes.add( ENCObjectType.QualityOfData );
    }

    @Override
    public Schema getXsdSchema( ) throws SAXException
    {
        SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        return schemaFactory.newSchema( getClass( ).getClassLoader( ).getResource( "data/encskin.xsd" ) );
    }


    @Override
    public Skin<ENCObject> createDefaultHardCodedSkin( )
    {
        return createDefaultHardCodedSkin( ColorPalette.createDefaultColorPalette( ) );
    }

    @Override
    public Skin<ENCObject> createDefaultHardCodedSkin( ColorPalette colorPalette )
    {
        Geoskinspec skinSpec = new HardCodedENCSkinSpecFactory( colorPalette ).createSkinSpec( );
        return new BasicSkin<ENCObject>( new ENCBasicSkinHelper(), skinSpec );
    }

    @Override
    public ContentHandler<ENCObject> createAnnotationPainterSpec( AnnotationPainterSpec annotationSpec )
    {
        return new ENCAnnotationHandler( ( EncAnnotationPainterSpec ) annotationSpec);
    }

    @Override
    public boolean isBestBoundaryType( ENCObject enc )
    {
        if (enc.getObjectType() == ENCObjectType.Coverage) {
            ENCEnumAttribute coverage = enc.getEnumAttribute(ENCAttributeType.CategoryOfCoverage);
            return(coverage != null && coverage.getENCAttributeValue() == ENCAttributeValues.CategoryOfCoverage_CoverageAvailable);
        }
        return false;
    }

    @Override
    public boolean isSecondBestBoundaryType( ENCObject enc )
    {
        return bestMapBoundaryTypes.contains( enc.getObjectType() );
    }

    @Override
    public StreamToGeoObjectConverter<ENCObject> createGeoObjectLoader( )
    {
        return new ENCObjectLoader( );
    }

    @Override
    public GeoFeatureType convertGeoFeatureType( String type )
    {
        return ENCObjectType.valueOf( type );
    }

    @Override
    public List<? extends GeoFeatureType> convertGeoFeatureType(List<String> featureTypeList)
    {
        List<ENCObjectType> realList = new ArrayList<ENCObjectType>( featureTypeList.size( ) );
        for ( String featureType : featureTypeList )
        {
            realList.add( internalConvertGeoFeatureType( featureType ) );
        }
        return realList;
    }

    public String geoName() {
        return "ENC";
    }

    private ENCObjectType internalConvertGeoFeatureType( String type )
    {
        return ENCObjectType.valueOf( type );
    }

}
