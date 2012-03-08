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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import com.metsci.glimpse.charts.vector.parser.attributes.GenericAttribute;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObjectType;
import com.metsci.glimpse.charts.vector.parser.objects.ENCShape;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShapeType;
import com.metsci.glimpse.util.io.StreamOpener;

import java.io.InputStream;
import java.io.InputStreamReader;


public class ENCObjectInterpreter {
    private TreeMap<String, ENCObjectHeader>  classList_byAcronym;
    private HashMap<Integer, ENCObjectHeader> classList_byCode;
    private final ENCAttributeInterpreter     attributeInterpreter;

    private String s57ObjectClassList = "enc/s57objectclasses.csv";


    public class ENCObjectHeader{

        public ENCObjectHeader(){
            objectAttributes 	= new ArrayList<String>();
            attributeMap 		= new TreeMap<String, Integer>();
        }

        public int    objectCode;
        public int    primitiveType;
        public String objectClass;
        public String objectSubclass;
        public String objectAcronym;

        public ENCClassType classType;

        public ArrayList<String> 		objectAttributes;
        public TreeMap<String, Integer> attributeMap;

        @Override
        public String toString(){
            String _retVal = "";

            _retVal += objectAcronym + "(" + String.valueOf(objectCode) + ")\n";
            _retVal += objectClass + " -- " + objectSubclass + "\n";

            _retVal += "Attributes: ";

            for(int i = 0; i < objectAttributes.size(); i++){
                _retVal += objectAttributes.get(i) + " ";
            }
            _retVal += "\n";

            _retVal += "Primitive Code: " + String.valueOf(primitiveType);

            return _retVal;
        }
    }

    public enum ENCClassType{
        Meta,
        Geographical,
        Carteographic,
        Symbolic;

    }


    public ENCObjectInterpreter(ENCAttributeInterpreter attribInterp) throws IOException{

        classList_byAcronym = new TreeMap<String, ENCObjectHeader>();
        classList_byCode    = new HashMap<Integer, ENCObjectHeader>();

        attributeInterpreter = attribInterp;

        buildClassInformation();
    }

    public HashMap<Integer, ENCObjectHeader> getObjectHeaders(){
        return classList_byCode;
    }


    public ENCObjectHeader getHeader_byCode(int code){
        return classList_byCode.get(code);
    }

    public ENCObjectHeader getHeader_byAcronym(String acronym){
        return classList_byAcronym.get(acronym);
    }


    public ENCObject convertObject(GenericObject obj) throws Exception{
        if(!obj.hasAttribute("OBJL"))
            return null;

        ENCObject _retVal = new ENCObject();


        // Specify the object type
        _retVal.setObjectType(ENCObjectType.getInstance(Integer.valueOf(obj.getAttribute("OBJL").getValue())));

        GenericAttribute rcidAttrib = obj.getAttribute("RCID");
        if(rcidAttrib != null) {
            //System.out.println("rcid value: " + Integer.valueOf(obj.hasAttribute("RCID").value));
            _retVal.setRcid(Integer.valueOf(rcidAttrib.getValue()));
        }

        GenericAttribute rcnmAttrib = obj.getAttribute("RCNM");
        if(rcnmAttrib != null)
            _retVal.setRcnm(Integer.valueOf(rcnmAttrib.getValue()));


        // Attach the attributes to the object
        for (GenericAttribute _currentAttribute : obj.getAttributes()) {
            // Since some of the attributes are not S-57 attributes we must ignore these.
            if(attributeInterpreter.isValidAcronym(_currentAttribute.getName()))
                _retVal.addAttribute(attributeInterpreter.convertAttribute(_currentAttribute, obj));
        }

        // Attach the shapes
        for (GenericShape shape : obj.getShapes()) {
            _retVal.addShape(convertShape(shape));
        }

        return _retVal;
    }

    public ENCShape convertShape(GenericShape shape) throws Exception{

        ENCShape _retVal = new ENCShape();

        if(shape.getType().equals("POINT"))
            _retVal.setShapeType(GeoShapeType.Point);
        else if(shape.getType().equals("POLYGON"))
            _retVal.setShapeType(GeoShapeType.Polygon);
        else if(shape.getType().equals("MULTIPOINT"))
            _retVal.setShapeType(GeoShapeType.Multipoint);
        else if(shape.getType().equals("LINESTRING"))
            _retVal.setShapeType(GeoShapeType.Linestring);
        else{
            throw new Exception("ENCShape -- Unknown primitive type");
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

    private void buildClassInformation() throws IOException{
        InputStream inputStream = StreamOpener.fileThenResource.openForRead(s57ObjectClassList);
        BufferedReader _fin = new BufferedReader(new InputStreamReader(inputStream));

        // Ignore the first line
        String _line = _fin.readLine();

        while((_line = _fin.readLine()) != null){

            ENCObjectHeader _objId = new ENCObjectHeader();
            int _currentIndex = 0;

            String[] _contentsSections = _line.split(",");

            // Pick off the main things

            // Object Code
            _objId.objectCode = Integer.valueOf(_contentsSections[_currentIndex]);
            _currentIndex++;

            // Object Name

            if(_contentsSections[_currentIndex].toCharArray()[0] == '\"'){
                _objId.objectClass  = _contentsSections[_currentIndex].substring(1, _contentsSections[_currentIndex].length());
                _currentIndex++;
                _objId.objectSubclass =  _contentsSections[_currentIndex].substring(1, _contentsSections[_currentIndex].length() - 1);
                _currentIndex++;
            }
            else{
                _objId.objectClass = _contentsSections[_currentIndex];
                _currentIndex++;
            }

            // Object Acronym
            _objId.objectAcronym = _contentsSections[_currentIndex];
            _currentIndex++;


            for(int i = _currentIndex, n = _contentsSections.length; i < n; i++){

                String[] _sectionValues = _contentsSections[i].split(";");

                if((i == n - 2 && _objId.objectCode < 400) || (i == n - 1 && _objId.objectCode >= 400)){
                    if(_objId.objectCode < 300)
                        _objId.classType = ENCClassType.Geographical;
                    else if(_objId.objectCode < 400)
                        _objId.classType = ENCClassType.Meta;
                    else if(_objId.objectCode < 500)
                        _objId.classType = ENCClassType.Carteographic;
                    else
                        _objId.classType = ENCClassType.Symbolic;
                }
                else if(i == n - 1 && _objId.objectCode < 400){
                    int _value = 0;


                    for(int j = 0; j < _sectionValues.length; j++){
                        if(_sectionValues[j].equals("Point"))
                            _value = _value | 1;
                        else if(_sectionValues[j].equals("Line"))
                            _value = _value | 2;
                        else if(_sectionValues[j].equals("Area"))
                            _value = _value | 4;
                    }
                    _objId.primitiveType = _value;
                }
                else{
                    for(int j = 0; j < _sectionValues.length; j++){
                        if(!_sectionValues[j].equals("")){
                            _objId.objectAttributes.add(_sectionValues[j]);
                            _objId.attributeMap.put(_sectionValues[j], _objId.objectAttributes.size() - 1);
                        }
                    }
                }

            }

            classList_byAcronym.put(_objId.objectAcronym, _objId);
            classList_byCode.put(_objId.objectCode, _objId);

        }

        _fin.close();
    }

}
