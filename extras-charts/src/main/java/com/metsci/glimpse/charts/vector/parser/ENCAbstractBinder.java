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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import com.metsci.glimpse.charts.vector.parser.attributes.GenericAttribute;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;
import com.metsci.glimpse.util.io.StreamOpener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.NavigableMap;

public class ENCAbstractBinder {
    private NavigableMap<String, String> attachments;


    public ENCAbstractBinder(){
        attachments = new TreeMap<String, String>();
    }

    public void resolveDependency(List<GenericObject> objList, String directory) throws IOException{

        Iterator<GenericObject> _iterator = objList.iterator();

        while(_iterator.hasNext())
            resolveFileDependency(_iterator.next(), directory);

    }

    public void resolveMetaObjects(List<GenericObject> objList){

        List<GenericObject> _toRemove = new ArrayList<GenericObject>();

        Iterator<GenericObject> _iterator = objList.iterator();

        while(_iterator.hasNext()){

            GenericObject _currentObject = _iterator.next();

            // All actual objects contain this key
            if(!_currentObject.hasAttribute("OBJL"))
                _toRemove.add(_currentObject);
        }

        _iterator = _toRemove.iterator();
        while(_iterator.hasNext()){

            GenericObject _currentObject = _iterator.next();

            // Remove the object from the list
            objList.remove(_currentObject);

            System.out.println("creating meta from generic object " + _currentObject);
            // Link the object as a metaobject
            GenericObject.linkMetaObject(_currentObject.createMetaObject());

        }
    }

    public List<ENCMetaObject> resolveMetaObjects2(List<GenericObject> objList){
        List<ENCMetaObject> metaObjects = new ArrayList<ENCMetaObject>(50);

        Iterator<GenericObject> _iterator = objList.iterator();
        while(_iterator.hasNext()){
            GenericObject _currentObject = _iterator.next();

            // All actual objects contain this key
            if(!_currentObject.hasAttribute("OBJL")) {
                metaObjects.add(_currentObject.createMetaObject());
                _iterator.remove();
                //_toRemove.add(_currentObject);
            }
        }

        return metaObjects;
    }

    private void resolveFileDependency(GenericObject obj, String directory) throws IOException{
        for (GenericAttribute _currentAttribute : obj.getAttributes()) {
            // Certain attributes rely on attached files
            if(_currentAttribute.getValue().endsWith(".TXT")){

                if(attachments.containsKey(_currentAttribute.getValue())){
                    _currentAttribute.setValue(attachments.get(_currentAttribute.getValue()));
                }
                else{
                    //System.out.println("ENCAbstractBinder opening file " + (directory + "/" + _currentAttribute.value) + " for reading");
                    String resource = directory + "/" + _currentAttribute.getValue();
                    InputStream inputStream = StreamOpener.fileThenResource.openForRead(resource);
                    BufferedReader _fin  = new BufferedReader(new InputStreamReader(inputStream));
                    //#BufferedReader _fin  = new BufferedReader(new FileReader(directory + "/" + _currentAttribute.value));
                    StringBuilder _fileContents = new StringBuilder();


                    String _line = "";
                    while((_line = _fin.readLine()) != null){
                        _fileContents.append(_line + "\n");
                    }

                    _fin.close();

                    attachments.put(_currentAttribute.getValue(), _fileContents.toString());

                    _currentAttribute.setValue(attachments.get(_currentAttribute.getValue()));
                }
            }
        }
    }



}
