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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.metsci.glimpse.charts.vector.parser.attributes.GenericAttribute;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;
import com.metsci.glimpse.util.io.StreamOpener;

/**
 *
 * A very simple parser for the output of the sogrinfo program.
 * There is a list of shape names in shapeSet that may need to be augmented to handle additional shapes.
 *
 * @author gboquet
 *
 */
public class OGRReader {
    private static String[] shapes = new String[]{"POINT", "POLYGON", "LINESTRING", "MULTIPOINT"};
    private static Set<String> shapeSet = new HashSet<String>(Arrays.<String>asList(shapes));

    /**
     * Parses the output of the s57dump program and produces a set of ENC Objects
     * @param resource Path to file that is to parsed.
     * @return A list of the objects contained in the file.
     * @throws IOException
     */
    public static List<GenericObject> read(File file) throws IOException{
        BufferedReader _fin = new BufferedReader(new FileReader(file));
        return read(_fin);
    }

    public static List<GenericObject> read(String resource) throws IOException{
        InputStream inputStream = StreamOpener.fileThenResource.openForRead(resource);
        BufferedReader _fin = new BufferedReader(new InputStreamReader(inputStream));
        return read(_fin);
    }

    public static List<GenericObject> read(BufferedReader reader) throws IOException {
        List<GenericObject> objectList = new LinkedList<GenericObject>();
        GenericObject currentObject = null;

        String currentLine = null;

        // Read in each line

        int lineNo = 0;
        String featureLine = null;
        String priorLine= null;
        while((currentLine = reader.readLine()) != null){
            lineNo++;
            try {
                // We found a new feature
                if(currentLine.contains("OGRFeature")){
                    featureLine = currentLine;
                    // Add the object to our list
                    currentObject = new GenericObject();
                    objectList.add(currentObject);

                    // Grab all of the associated attributes
                    List<String> _attributes = new ArrayList<String>();

                    currentLine = reader.readLine();
                    while(currentLine != null && !currentLine.isEmpty()) { //  !(_line = _fin.readLine()).equals("")){
                        _attributes.add(new String(currentLine));
                        currentLine = reader.readLine();
                    }

                    try {
                    // Append the attributes
                    appendAttributes(currentObject, _attributes);
                    } catch (ArrayIndexOutOfBoundsException aie) {
                        System.out.println("OGRReader exception on line: " + lineNo);
                        System.out.println("For feature: " + featureLine);
                        System.out.println("Prior line: " + priorLine);
                        System.out.println("For object: " + currentObject);
                        throw aie;
                    }
                }
                priorLine = currentLine;
            } catch (NumberFormatException nfe) {
                System.out.println("caught and ignoring: ");
                nfe.printStackTrace();
            }

        }

        reader.close();

        return objectList;
    }


    private static void appendAttributes(GenericObject obj, List<String> attributes){
        GenericAttribute lastAttribute = null;
        for(int i = 0, n = attributes.size(); i < n; i++){

            String _currentLine = attributes.get(i);

            String[] _entries = _currentLine.split(" ");

            if (_entries.length < 3) {
                if (_currentLine.trim().isEmpty()) {
                    System.out.println("ignoring blank line");
                    continue;
                } else if (lastAttribute != null && lastAttribute.getType().equals("(String)")) {
                    // Some text based attributes can be multiple lines long
                    boolean hasEquals = _currentLine.indexOf("=") >= 0;
                    if (! hasEquals) {
                        lastAttribute.appendValue(_currentLine);
                        System.out.println("Assuming " + _currentLine + " is multiline.  Added to make " + lastAttribute.getValue());
                    }
                    continue;
                } else {
                    throw new IllegalStateException("Attribute line missing key value entries: " + _currentLine +", priorLine: " + attributes.get(i-1));
                }
            }

            boolean isShape = isItAShape( _entries[2] );
            if(! isShape ) {
                boolean hasEquals = _currentLine.indexOf("=") >= 0;
                if (!hasEquals && lastAttribute != null && lastAttribute.getType().equals("(String)")) {
                    // The txt attribute can be multiple lines
                    lastAttribute.appendValue(_currentLine);
                    System.out.println("Assuming " + _currentLine + " is multiline.  Added to make " + lastAttribute.getValue());
                } else {
                    if (_entries.length < 4) {
                        throw new IllegalStateException("non shape attribute with less than 3 key value entries: " + _currentLine +", Entries: " + _entries);
                    }
                    String _name = _entries[2];
                    String _type = _entries[3];

                    String[] _splitAtEquals = _currentLine.split(" = ");

                    String _value = "";
                    if(_splitAtEquals.length > 1)
                        _value = _splitAtEquals[1];

                    GenericAttribute _newAttribute = new GenericAttribute(_type, _name, _value);
                    lastAttribute = _newAttribute;
                    obj.addAttribute(_newAttribute);
                }
            }
            else{

                String _shapeType = _entries[2];

                // Concatenate the rest of the lines

                StringBuilder _lineBuilder = new StringBuilder();

                for(int j = i; j < n; j++){
                    _lineBuilder.append(attributes.get(j));
                }

                String _allLines = _lineBuilder.toString();

                // Parse outer containment and remove outer parenthesis

                int _startPoint = 0;

                char[] _allChars = _allLines.toCharArray();

                while(_startPoint < _allChars.length){

                    if(_allChars[_startPoint] == '('){
                        _startPoint++;
                        break;
                    }
                    _startPoint++;
                }

                int _endPoint = _allChars.length - 1;

                // Parse inner containment

                parseShape(obj, _shapeType, _allLines.substring(_startPoint, _endPoint));
            }

        }


    }
    /**
     * If we encounter a shape, then we need to parse the coordinates. Some shapes will
     * contain multiple shapes. In the output, these are wrapped in parenthesis. If, however,
     * the shape only contains one shape, then there are no parenthesis. As an example:
     * 	POINT = (1.0 1.0) // One point
     * 	POLYGON = ((1.0 2.0, 1.0 3.0) (2.0 4.0, 5.0 7.0)) // Two polygons
     * The input value must have the outer parenthesis removed first.
     *
     * @param obj The object to which the shapes will be added.
     * @param type The type of shape.
     * @param value The string of coordinates.
     */
    private static void parseShape(GenericObject obj, String type, String value){


        char[] _allChars = value.toCharArray();


        boolean _mayHaveMultipleParts = false;
        for(int i = 0, n = _allChars.length; i < n; i++){

            if(_allChars[i] == '(') {
                _mayHaveMultipleParts = true;
                break;
            }
        }

        if(_mayHaveMultipleParts){

            int _counter = 0;

            while(_counter < _allChars.length){

                if(_allChars[_counter] == '('){

                    int _startPoint = _counter + 1;

                    while(_allChars[++_counter] != ')'){}

                    int _endPoint = _counter;

                    GenericShape _newShape = new GenericShape(type, value.substring(_startPoint, _endPoint));
                    obj.addShape(_newShape);
                }

                _counter++;
            }
        }
        else{

            GenericShape _newShape = new GenericShape(type, value);
            obj.addShape(_newShape);

        }

    }

    private static boolean isItAShape(String value){
        return shapeSet.contains(value);
    }
}
