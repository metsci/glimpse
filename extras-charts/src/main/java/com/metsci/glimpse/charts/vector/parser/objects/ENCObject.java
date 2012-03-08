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
package com.metsci.glimpse.charts.vector.parser.objects;

import com.metsci.glimpse.charts.vector.parser.attributes.AttributeDescription;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCAbstractAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCCodedAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCEnumAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCFloatAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCFreeAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCIntAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCListAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.GeoAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.GeoIntAttribute;
import com.metsci.glimpse.charts.vector.parser.autogen.ENCAttributeType;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.io.DataInput;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Stores all information for a particular ENC feature.
 * 
 * @author Cunningham
 * @author Boquet
 */
public class ENCObject implements GeoObject {

    private ENCObjectType objectType;
    private Map<ENCAttributeType, ENCAbstractAttribute> attributes;
    private List<ENCShape> shapeList;
    private int rcid = -1;
    private int rcnm = 100;

    public ENCObject(){
        attributes = new HashMap<ENCAttributeType, ENCAbstractAttribute>();
        shapeList = new ArrayList<ENCShape>();
    }


    public GeoFeatureType getGeoFeatureType() {
        return objectType;
    }

    public ENCObjectType getObjectType(){
        return objectType;
    }

    public void setObjectType(ENCObjectType type){
        assert(type!= null);
        objectType = type;
    }

    public int getRcid() {
        return rcid;
    }

    public void setRcid(int rcid) {
        this.rcid = rcid;
    }

    public int getRcnm() {
        return rcnm;
    }

    public void setRcnm(int rcnm) {
        this.rcnm = rcnm;
    }

    @Override
    public GeoAttribute getGeoAttribute(GeoAttributeType type){
        return attributes.get(type);
    }

    @Override
    public GeoIntAttribute getGeoIntAttribute(GeoAttributeType type) {
        return (GeoIntAttribute) attributes.get(type);
    }

    public ENCEnumAttribute getEnumAttribute(ENCAttributeType type){
        return (ENCEnumAttribute)attributes.get(type);
    }
    public ENCIntAttribute getIntAttribute(ENCAttributeType type){
        return (ENCIntAttribute)attributes.get(type);
    }
    public ENCFloatAttribute getFloatAttribute(ENCAttributeType type){
        return (ENCFloatAttribute)attributes.get(type);
    }
    public ENCListAttribute getListAttribute(ENCAttributeType type){
        return (ENCListAttribute)attributes.get(type);
    }
    public ENCFreeAttribute getFreeAttribute(ENCAttributeType type){
        return (ENCFreeAttribute)attributes.get(type);
    }
    public ENCCodedAttribute getCodedAttribute(ENCAttributeType type){
        return (ENCCodedAttribute)attributes.get(type);
    }
    public ENCAbstractAttribute getAbstractAttribute(ENCAttributeType type){
        return attributes.get(type);
    }

    public void addAttribute(ENCAbstractAttribute attrib){
        attributes.put(attrib.getAttributeType(), attrib);
    }

    public void addShape(ENCShape shape){
        shapeList.add(shape);
    }

    public List<ENCShape> getShapeList(){
        return shapeList;
    }

    @Override
    public Collection<? extends GeoShape> getGeoShapes() {
        return shapeList;
    }

    @Override
    public GeoShape getFirstGeoShape() {
        return shapeList.get(0);
    }

    public static void write(DataOutputStream fout, ENCObject obj) throws IOException{
        assert(obj.objectType != null);
        // Write the object type
        ENCObjectType.write(fout, obj.objectType);

        fout.writeInt(-1);
        fout.writeInt(obj.rcnm);
        fout.writeInt(obj.rcid);

        // Write the attributes
        fout.writeInt(obj.attributes.size());
        for (ENCAbstractAttribute attrib : obj.attributes.values()) {
            attrib.write(fout);
        }

        // Write the shapes
        fout.writeInt(obj.shapeList.size());
        Iterator<ENCShape> _shapeIterator = obj.shapeList.iterator();
        while(_shapeIterator.hasNext()){
            ENCShape.write(fout, _shapeIterator.next());
        }
    }


    public static ENCObject read(DataInput fin) throws IOException{

        ENCObject _retVal = new ENCObject();

        // Read in the object type
        _retVal.objectType = ENCObjectType.read(fin);

        // in old versions, next value is num of attributes, which should be >= 0
        // in new version, next value is -1, which indicates new version.  Than the
        // next three values are rcnm, rcid, and then numAttributes
        int nextInt = fin.readInt();

        int _numOfAttributes;
        if (nextInt != -1) {
            _numOfAttributes = nextInt;
        } else {
            _retVal.rcnm = fin.readInt();
            _retVal.rcid = fin.readInt();
            _numOfAttributes = fin.readInt();
        }

        // Read in the attributes
        //int _numOfAttributes = fin.readInt();
        for(int i = 0; i < _numOfAttributes; i++){
            _retVal.addAttribute(ENCAbstractAttribute.read(fin));

        }

        // Read in the shapes
        int _numOfShapes = fin.readInt();
        for(int i = 0; i < _numOfShapes; i++){
            _retVal.addShape(ENCShape.read(fin));
        }

        return _retVal;
    }

    public static void main(String[] args){

        try{
            DataInputStream _fin = new DataInputStream(new BufferedInputStream(new FileInputStream("US2AK70M0_bin.txt")));

            ENCObject _tempObj = null;

            try{
                while(true){
                    _tempObj = ENCObject.read(_fin);

                    System.out.println(_tempObj.toString());
                }
            }
            catch(EOFException e2){
                // Fall through
            }


            _fin.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }


    }

    @Override
    public String toString() {
        StringBuilder _retVal = new StringBuilder();

        _retVal.append("Object Type: ");
        _retVal.append(objectType.toString());
        _retVal.append("\nRCNM/RCID:");
        _retVal.append(rcnm).append(':').append(rcid);
        _retVal.append("\nAttributes:\n");

        for(Entry<ENCAttributeType, ENCAbstractAttribute> entry : attributes.entrySet()) {
            ENCAttributeType attributeType = entry.getKey();
            ENCAbstractAttribute attribute = entry.getValue();
            if (attribute.isNullValued())
                continue;

            AttributeDescription description = attribute.getDescription();
            if (description.getValueAsString() != null) {
                _retVal.append(description.getAttributeType());
                _retVal.append(": (");
                _retVal.append(description.getAttributeClass());
                _retVal.append(") ");
                _retVal.append(description.getValueAsString());
                _retVal.append('\n');
            }
            //_retVal.append(attribute.toString()).append('\n');
            //_retVal.append("isNullValued: ");
            //_retVal.append(attribute.isNullValued());
            //_retVal.append('\n');
        }

        return _retVal.toString();
    }
}
