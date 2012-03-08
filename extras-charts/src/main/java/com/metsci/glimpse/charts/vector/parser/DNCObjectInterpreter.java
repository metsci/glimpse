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
package com.metsci.glimpse.charts.vector.parser;

import java.io.IOException;

import com.metsci.glimpse.charts.vector.parser.attributes.GenericAttribute;
import com.metsci.glimpse.charts.vector.parser.objects.DNCFeatureCode;
import com.metsci.glimpse.charts.vector.parser.objects.DNCObject;
import com.metsci.glimpse.charts.vector.parser.objects.DNCShape;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShapeType;


public class DNCObjectInterpreter {

    private final DNCAttributeInterpreter attributeInterpreter;

    public DNCObjectInterpreter(DNCAttributeInterpreter attribInterp) throws IOException{
        attributeInterpreter = attribInterp;
    }

    public DNCObject convertObject(GenericObject obj)  {
        GenericAttribute attribute = obj.getAttribute("f_code");

        if(attribute == null)
            return null;

        DNCFeatureCode featureCode = DNCFeatureCode.fromKey(attribute.getValue());
        DNCObject dncObject = new DNCObject(featureCode);


        // Attach the attributes to the object
        for (GenericAttribute _currentAttribute : obj.getAttributes()) {
            dncObject.addAttribute(attributeInterpreter.convertAttribute(_currentAttribute, obj));
        }

        // Attach the shapes
        for (GenericShape shape : obj.getShapes()) {
            dncObject.addShape(convertShape(shape));
        }

        return dncObject;
    }

    public DNCShape convertShape(GenericShape shape) {

        DNCShape _retVal = new DNCShape();

        if(shape.getType().equals("POINT"))
            _retVal.setShapeType(GeoShapeType.Point);
        else if(shape.getType().equals("POLYGON"))
            _retVal.setShapeType(GeoShapeType.Polygon);
        else if(shape.getType().equals("MULTIPOINT"))
            _retVal.setShapeType(GeoShapeType.Multipoint);
        else if(shape.getType().equals("LINESTRING"))
            _retVal.setShapeType(GeoShapeType.Linestring);
        else{
            throw new IllegalStateException("DNCShape -- Unknown primitive type");
        }

        if(! shape.isEmpty()) {
            _retVal.setPointSize(shape.getPointSize());

            double[] _values = new double[shape.getPointSize() * shape.numCoordinates()];

            int _currentIndex = 0;

            for(int i = 0; i < shape.numCoordinates(); i++) {
                double [] shapeCoordinate = shape.getCoordinate(i);
                for(int j = 0; j < shapeCoordinate.length; j++){
                    _values[_currentIndex] = shapeCoordinate[j];
                    _currentIndex++;
                }
            }

            _retVal.setVertexPoints(_values);
        }

        return _retVal;

    }




}
