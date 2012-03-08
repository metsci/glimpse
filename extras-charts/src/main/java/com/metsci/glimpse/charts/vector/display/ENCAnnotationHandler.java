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

import com.metsci.glimpse.charts.vector.display.xmlgen.EncAnnotationPainterSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.EncAttributeTypeSpec;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCIntAttribute;
import com.metsci.glimpse.charts.vector.parser.autogen.ENCAttributeType;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoAttributeType;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This content handler renders text attributes for ENCObjects. It delegates
 * most of the work to the base class (see class for more info). This classes
 * sole value add is to determine which attribute to render. This is determined
 * from EncAnnotationPainterSpec passed into this class at the constructor.
 * 
 * @author Cunningham
 */
public class ENCAnnotationHandler extends AnnotationHandler<ENCObject> implements ContentHandler<ENCObject>
{
    private static Logger logger = Logger.getLogger( ENCAnnotationHandler.class.toString( ) );

    private GeoAttributeType attributeTypeToAnnotate;

    public ENCAnnotationHandler( )
    {
        this( new EncAnnotationPainterSpec( ) );
    }

    public ENCAnnotationHandler( EncAnnotationPainterSpec spec )
    {
        super(spec);

        EncAttributeTypeSpec attributeTypeSpec = spec.getAttributeKey( );
        if ( attributeTypeSpec == null )
        {
            logger.log( Level.WARNING, "XML has missing or invalid ENCAttributeTypeSpec for EncAnnotationPainterSpec" );
            return;
        }
        attributeTypeToAnnotate = ENCAttributeType.valueOf( attributeTypeSpec.value( ) );
    }

    public GeoAttributeType getAttributeTypeToAnnotate( )
    {
        return attributeTypeToAnnotate;
    }

    @Override
    public Integer getScaleMin( ENCObject object )
    {
        ENCIntAttribute attributeValue = object.getIntAttribute( ENCAttributeType.ScaleMinimum );
        if ( attributeValue != null && !attributeValue.isNullValued( ) ) return attributeValue.getAttributeValue( );
        return null;
    }
}
