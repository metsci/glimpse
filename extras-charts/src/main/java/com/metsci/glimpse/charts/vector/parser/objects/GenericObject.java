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

import java.util.ArrayList;
import java.util.Iterator;

import com.metsci.glimpse.charts.vector.parser.ENCMetaObject;
import com.metsci.glimpse.charts.vector.parser.GenericShape;
import com.metsci.glimpse.charts.vector.parser.attributes.GenericAttribute;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Used in the process of parsing raw ENC/DNC files and converting them to our
 * own internal format. Not meant to be used outside of that.
 * 
 * Basically contains the data as read in from the raw files, but without any
 * interpretation on that data - all string values.
 * 
 * @author Cunningham
 */
public class GenericObject {
    private static List<ENCMetaObject> metaObjects = new ArrayList<ENCMetaObject>();

    public static GenericAttribute getMetaAttribute(String name){
        GenericAttribute _retVal = null;

        Iterator<ENCMetaObject> _iterator = metaObjects.iterator();

        while(_retVal == null && _iterator.hasNext()){
            _retVal = _iterator.next().getAttribute(name);
        }

        System.out.println("resolving meta name: " + name + "; attrib: " + _retVal + " from a list of " + metaObjects.size() + " objects");
        return _retVal;
    }

    public static void linkMetaObject(ENCMetaObject obj){
        System.out.println("adding meta object " + obj);
        metaObjects.add(obj);
    }
    public static void clearMetaObjects(){
        metaObjects.clear();
    }


    private Map<String, GenericAttribute> attributeMap;
    private List<GenericShape> shapes;


    public GenericObject(){
        attributeMap = new LinkedHashMap<String, GenericAttribute>();
        shapes = new ArrayList<GenericShape>();
    }

    public boolean hasAttribute(String attributeKey) {
        return attributeMap.containsKey(attributeKey);
    }

    public GenericAttribute getAttribute(String attributeKey) {
        return attributeMap.get(attributeKey);
    }

    public Collection<GenericAttribute> getAttributes() {
        return attributeMap.values();
    }

    public void addAttribute(GenericAttribute enca) {
        attributeMap.put(enca.getName(), enca);
    }

    public Collection<GenericShape> getShapes() {
        return shapes;
    }

    public void addShape(GenericShape encs) {
        shapes.add(encs);
    }

    public ENCMetaObject createMetaObject() {
        return new ENCMetaObject(attributeMap);
    }

    @Override
    public String toString() {
        StringBuilder _retVal = new StringBuilder();

        for(GenericAttribute attribute : attributeMap.values()) {
            _retVal.append(attribute.toString());
            _retVal.append("\n");
        }

        _retVal.append("\n");

        for(int i = 0, n = shapes.size(); i < n; i++){
            _retVal.append(shapes.get(i).toString());
            _retVal.append("\n");
        }

        return _retVal.toString();
    }
}
