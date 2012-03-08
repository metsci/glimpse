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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.metsci.glimpse.charts.vector.parser.attributes.DNCAbstractAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.DNCFloatAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.DNCFreeAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.DNCIntAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.GeoAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.GeoIntAttribute;

import java.io.DataInput;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Stores all information for a particular DNC feature.
 * 
 * @author Cunningham
 */
public class DNCObject implements GeoObject {

    private Map<DNCAttributeType, DNCAbstractAttribute> attributes;
    private List<DNCShape> shapeList;
    private final DNCFeatureCode dncFeatureCode;

    public DNCObject(DNCFeatureCode dncFeatureCode) {
        attributes = new HashMap<DNCAttributeType, DNCAbstractAttribute>();
        shapeList = new ArrayList<DNCShape>();
        this.dncFeatureCode = dncFeatureCode;
        assert(dncFeatureCode != null);
        if (dncFeatureCode == null)
            throw new IllegalStateException("Null feature code");
    }


    public DNCFeatureCode getFeatureCode() {
        return dncFeatureCode;
    }

    @Override
    public GeoFeatureType getGeoFeatureType() {
        return dncFeatureCode;
    }

    @Override
    public GeoAttribute getGeoAttribute(GeoAttributeType attributeType) {
        return attributes.get(attributeType);
    }

    @Override
    public GeoIntAttribute getGeoIntAttribute(GeoAttributeType type) {
        return (GeoIntAttribute) attributes.get(type);
    }

    public DNCIntAttribute getIntAttribute(DNCAttributeType type){
        return (DNCIntAttribute)attributes.get(type);
    }
    public DNCFloatAttribute getFloatAttribute(DNCAttributeType type){
        return (DNCFloatAttribute)attributes.get(type);
    }
    public DNCFreeAttribute getFreeAttribute(DNCAttributeType type){
        return (DNCFreeAttribute)attributes.get(type);
    }
    public DNCAbstractAttribute getAbstractAttribute(DNCAttributeType type){
        return attributes.get(type);
    }

    public void addAttribute(DNCAbstractAttribute attrib){
        attributes.put(attrib.getAttributeType(), attrib);
    }

    public void addShape(DNCShape shape){
        shapeList.add(shape);
    }

    public List<DNCShape> getShapeList(){
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

    public static void write(DataOutputStream fout, DNCObject obj) throws IOException{
        obj.write(fout);
    }

    public void write(DataOutputStream fout) throws IOException{
        //System.out.println("writing version 1");
        // write version
        fout.writeInt(1);

        dncFeatureCode.write(fout);

        // Write the attributes
        fout.writeInt(attributes.size());

        for (DNCAbstractAttribute attrib : attributes.values()) {
            attrib.write(fout);
        }

        // Write the shapes
        fout.writeInt(shapeList.size());
        Iterator<DNCShape> _shapeIterator = shapeList.iterator();
        while(_shapeIterator.hasNext()){
            DNCShape.write(fout, _shapeIterator.next());
        }
    }


    public static DNCObject read(DataInput fin) throws IOException{
        int version = fin.readInt();
        if (version != 1)
            throw new IllegalStateException("Unsupported version " + version + "; expecting value 1");

        DNCFeatureCode featureCode = DNCFeatureCode.read(fin);

        DNCObject _retVal = new DNCObject(featureCode);

        int _numOfAttributes = fin.readInt();

        // Read in the attributes
        //int _numOfAttributes = fin.readInt();
        for(int i = 0; i < _numOfAttributes; i++){
            _retVal.addAttribute(DNCAbstractAttribute.read(fin));

        }

        // Read in the shapes
        int _numOfShapes = fin.readInt();
        for(int i = 0; i < _numOfShapes; i++){
            _retVal.addShape(DNCShape.read(fin));
        }

        return _retVal;
    }


    @Override
    public String toString() {
        StringBuilder _retVal = new StringBuilder();
        _retVal.append("\nAttributes:\n");

        for(Entry<DNCAttributeType, DNCAbstractAttribute> entry : attributes.entrySet()) {
            DNCAttributeType attributeType = entry.getKey();
            DNCAbstractAttribute attribute = entry.getValue();
            if (attribute.isNullValued())
                continue;

            _retVal.append(attribute.getAttributeType());
            _retVal.append(": (");
            _retVal.append(attribute.getAttributeClass());
            _retVal.append(") ");
            _retVal.append(attribute.getAttributeValueAsString());
            _retVal.append('\n');

        }

        return _retVal.toString();
    }



}
