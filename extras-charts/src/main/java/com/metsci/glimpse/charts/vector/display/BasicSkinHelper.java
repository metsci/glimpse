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

import java.util.List;

import javax.xml.validation.Schema;

import org.xml.sax.SAXException;

import com.metsci.glimpse.charts.vector.display.xmlgen.AnnotationPainterSpec;
import com.metsci.glimpse.charts.vector.iteration.StreamToGeoObjectConverter;
import com.metsci.glimpse.charts.vector.parser.objects.GeoFeatureType;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;

/**
 * Most of the vector chart classes work on generalized GeoObjects.
 *    
 * This is helper class for functionality that is not offered via the GeoObject 
 * interface and whose implementation differs among chart types (enc, dnc).
 * 
 * @author Cunningham
 */
public interface BasicSkinHelper<V extends GeoObject> {

    /**
     * Returns the skin schema, or null if no validation needs to be performed
     * @return
     */
    Schema getXsdSchema() throws SAXException;

    /**
     * Returns a simple, default, hardcoded skin for the given chart type  
     */
    Skin<V> createDefaultHardCodedSkin( );

    /**
     * Returns a simple, default, hardcoded skin with the givne color palette 
     * for the given chart type  
     */
    Skin<V> createDefaultHardCodedSkin( ColorPalette colorPalette );

    /**
     * Returns a ContentHandler 
     * @param annotationSpec
     * @return
     */
    ContentHandler<V> createAnnotationPainterSpec( AnnotationPainterSpec annotationSpec );

    /**
     * Returns true if the given feature's vertices make up the coverage of the map
     *  
     */
    boolean isBestBoundaryType( V feature );

    /**
     * Returns true if the given feature's vertices should make up the coverage of the map
     *  
     */
    boolean isSecondBestBoundaryType( V feature );

    /**
     * Returns a StreamToGeoObjectConverter capable of reading objects of type V 
     * out of a given DataInputStream.
     * 
     */
    StreamToGeoObjectConverter<V> createGeoObjectLoader( );

    /**
     * Given a string from a skin xml file describing a feature type from a skin xml file, 
     * this method will return the feature type the string is describing.
     * 
     * @param type string describing a feature type from a skin xml file
     * @return the feature type the string is describing
     */
    GeoFeatureType convertGeoFeatureType( String type );

    /**
     * Given a list of strings from a skin xml file that specify feature types, 
     * this method will return a parallel list of feature types specified by the 
     * list of strings.
     */
    List<? extends GeoFeatureType> convertGeoFeatureType( List<String> featureTypeList );

    /**
     * Returns a String describing the chart type, "ENC", "DNC", etc
     */
    String geoName();

}
