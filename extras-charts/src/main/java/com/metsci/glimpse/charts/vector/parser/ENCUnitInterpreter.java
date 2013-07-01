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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.metsci.glimpse.charts.vector.parser.attributes.GenericAttribute;
import com.metsci.glimpse.charts.vector.parser.autogen.ENCUnit;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;
import com.metsci.glimpse.util.io.StreamOpener;

public class ENCUnitInterpreter {

    public class ENCUnitHeader{

        public ENCUnitHeader(int c, String n, String t, int tc, double afac, double bfac){
            code 		= c;
            name 		= n;
            type 		= t;
            targetCode 	= tc;
            aFactor 	= afac;
            bFactor 	= bfac;

            convertable = true;
        }

        public ENCUnitHeader(int c, String n, String t){
            code 		= c;
            name 		= n;
            type 		= t;

            convertable = false;
        }

        public int 		code;
        public String 	name;
        public String	type;

        public boolean  convertable;
        public int 		targetCode;
        public double 	aFactor;
        public double 	bFactor;

    }

    public class UnitInterpreterException extends Exception{

        public UnitInterpreterException(String s){
            super(s);
        }


        private static final long serialVersionUID = 1L;

    }

    public ENCUnitInterpreter(){
        unitMap 				= new HashMap<Integer, ENCUnitHeader>();
        unitList 				= new ArrayList<ENCUnitHeader>();
        unitTypeMap 			= new TreeMap<String, ArrayList<ENCUnitHeader>>();
        attributeUnitBinding 	= new TreeMap<Integer, String>();
        attributeFixedUnit 		= new TreeMap<Integer, Integer>();

        try {
            buildClassInformation();
            buildBindingInformation();

        } catch (IOException e) {
            e.printStackTrace();
        }

        buildConversionMatrices();
    }

    public ArrayList<ENCUnitHeader> getUnitHeaders(){
        return unitList;
    }

    public ENCUnit resolveUnitType(int attributeCode, GenericObject obj) throws UnitInterpreterException{

        // Find the attribute's unit type
        // Check to see if the attribute has a fixed unit type
        if(attributeFixedUnit.containsKey(attributeCode)){
            Integer fixedUnitKey = attributeFixedUnit.get(attributeCode);
            if (fixedUnitKey == null)
                throw new UnitInterpreterException("Cannot resolve unit type: Code = " + String.valueOf(attributeCode));

            ENCUnitHeader unitHeader = unitMap.get(fixedUnitKey);
            if (unitHeader == null)
                throw new UnitInterpreterException("Cannot resolve unit type: Code = " + String.valueOf(attributeCode) + ", unitKey: " + fixedUnitKey);

            return ENCUnit.getInstance(unitHeader.code);

        }
        else if(attributeUnitBinding.containsKey(attributeCode)){

            // Find the meta object
            GenericAttribute _metaAttribute = GenericObject.getMetaAttribute(attributeUnitBinding.get(attributeCode));

            if(_metaAttribute == null){
                throw new UnitInterpreterException("ENCUnitInterpreter -- Insufficient metadata for library.");
            }

            // The value it contains refers to the unit type
            return ENCUnit.getInstance(Integer.valueOf(_metaAttribute.getValue()));
        }
        else{
            throw new UnitInterpreterException("Cannot resolve unit type: Code = " + String.valueOf(attributeCode));
        }
    }

    public double[][] getAMatrix(){
        return conversionMatrixA;
    }
    public double[][] getBMatrix(){
        return conversionMatrixB;
    }

    private void buildConversionMatrices(){

        HashMap<Integer, Integer> _globalToLocalIdx = new HashMap<Integer, Integer>();
        boolean[] _convertableList 					= new boolean[unitList.size()];

        for(int i = 0, n = unitList.size(); i < n; i++){
            _globalToLocalIdx.put(unitList.get(i).code, i);
            _convertableList[i] = unitList.get(i).convertable;
        }

        double[][] _conversionMatrixA = new double[unitList.size()][unitList.size()];
        double[][] _conversionMatrixB = new double[unitList.size()][unitList.size()];

        for(int i = 0, n = unitList.size(); i < n; i++){

            for(int j = 0; j < n; j++){

                if(i == j && _convertableList[i]){
                    _conversionMatrixA[i][j] = 1.0;
                    _conversionMatrixB[i][j] = 1.0;
                }
                else{
                    _conversionMatrixA[i][j] = 0;
                    _conversionMatrixB[i][j] = 0;
                }
            }
        }

        // Population the initial table entries
        for(int i = 0, n = unitList.size(); i < n; i++){

            if(!_convertableList[i])
                continue;

            ENCUnitHeader _currentHeader = unitList.get(i);

            _conversionMatrixA[i][_globalToLocalIdx.get(_currentHeader.targetCode)] = _currentHeader.aFactor;
            _conversionMatrixB[i][_globalToLocalIdx.get(_currentHeader.targetCode)] = _currentHeader.bFactor;
        }


        // Solve for the rest of the table

        boolean _hasChanged = true;

        while(_hasChanged){

            _hasChanged = false;

            for(int i = 0, n = unitList.size(); i < n; i++){
                if(!_convertableList[i])
                    continue;

                for(int j = 0; j < n; j++){

                    if(!_convertableList[j])
                        continue;

                    // Make sure that solution is required
                    if(_conversionMatrixA[i][j] == 0){

                        // Make sure that the target can convert
                        // Case 1: The unit can be converted by reversing roles
                        if(_conversionMatrixA[j][i] != 0){

                            _conversionMatrixA[i][j] = 1.0/_conversionMatrixB[j][i];
                            _conversionMatrixB[i][j] = 1.0/_conversionMatrixA[j][i];

                            _hasChanged = true;

                            continue;
                        }
                        // Case 2: See if there exists a common unit between the two
                        else{

                            for(int k = 0; k < n; k++){

                                if(!_convertableList[k])
                                    continue;

                                if(_conversionMatrixA[k][j] != 0 && _conversionMatrixA[k][i] != 0){
                                    _conversionMatrixA[i][j] = _conversionMatrixA[k][i]*_conversionMatrixB[k][j];
                                    _conversionMatrixB[i][j] = _conversionMatrixA[k][j]*_conversionMatrixB[k][i];

                                    _hasChanged = true;

                                    break;
                                }

                            }

                        }
                    }

                }

            }
        }


        conversionMatrixA = _conversionMatrixA;
        conversionMatrixB = _conversionMatrixB;

    }

    private void buildClassInformation() throws IOException{
        InputStream unitOfMeasureStream = StreamOpener.fileThenResource.openForRead(unitOfMeasurementFile);
        BufferedReader _fin = new BufferedReader(new InputStreamReader(unitOfMeasureStream));
        // Ignore the first line
        String _line = _fin.readLine();

        CsvTokenizer csvTokenizer = new CsvTokenizer(_fin);

        List<String> _contents = new ArrayList<String>();
        try {
            while (csvTokenizer.readStringList(_contents)) {
                ENCUnitHeader _currentUnit = null;

                try{
                 _currentUnit = new ENCUnitHeader(Integer.valueOf(_contents.get(0)), _contents.get(1), _contents.get(2), Integer.valueOf(_contents.get(3)),
                                                               Double.valueOf(_contents.get(4)), Double.valueOf(_contents.get(5)));
                 //System.out.println("success parsing line " + lineNum);
                }
                catch(NumberFormatException e){

                    System.out.println("Unconvertable Line at line " + csvTokenizer.getLineNumber() + " - " + _contents.toString());
                    System.out.println("-- Creating unconvertable object");

                     _currentUnit = new ENCUnitHeader(Integer.valueOf(_contents.get(0)), _contents.get(1), _contents.get(2));
                }


                unitList.add(_currentUnit);
                unitMap.put(_currentUnit.code, _currentUnit);

                ArrayList<ENCUnitHeader> _currentHeaderList = null;

                if(unitTypeMap.containsKey(_currentUnit.type)){
                    _currentHeaderList = unitTypeMap.get(_currentUnit.type);
                }
                else{
                    _currentHeaderList = new ArrayList<ENCUnitHeader>();
                    unitTypeMap.put(_currentUnit.type, _currentHeaderList);
                }

                _currentHeaderList.add(_currentUnit);
            }
        } catch (java.text.ParseException pe) {
            throw new IOException(null, pe);
        }
        _fin.close();
    }


    /**
     * Loads and parses the file which contains the unit types of attributes with units. This
     * places attributes with fixed types and dynamic types into two different categories.
     * @throws IOException
     */
    private void buildBindingInformation() throws IOException{
        InputStream inputStream = StreamOpener.fileThenResource.openForRead(unitBindingFile);
        BufferedReader _fin = new BufferedReader(new InputStreamReader(inputStream));

        String _line = null;

        while((_line = _fin.readLine()) != null){

            String[] _contents = _line.split(",");

            // Fixed unit types
            if(_contents.length == 2){
                attributeFixedUnit.put(Integer.valueOf(_contents[0]), Integer.valueOf(_contents[1]));
            }
            // Dynamic unit types
            else{
                attributeUnitBinding.put(Integer.valueOf(_contents[0]), _contents[2] + "_" + _contents[3]);
            }
        }

        _fin.close();
    }

    private double[][] conversionMatrixA;
    private double[][] conversionMatrixB;

    private HashMap<Integer, ENCUnitHeader> 			unitMap;
    private ArrayList<ENCUnitHeader>					unitList;
    private TreeMap<String, ArrayList<ENCUnitHeader>> 	unitTypeMap;
    private TreeMap<Integer, String>					attributeUnitBinding;
    private TreeMap<Integer, Integer>					attributeFixedUnit;

    private static String unitOfMeasurementFile = "enc/unit_of_measure.csv";
    private static String unitBindingFile 		= "enc/s57_unitvalues.txt";

}
