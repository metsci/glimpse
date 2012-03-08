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
package com.metsci.glimpse.charts.vector.parser.autogen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.metsci.glimpse.charts.vector.parser.ENCAttributeInterpreter;
import com.metsci.glimpse.charts.vector.parser.ENCObjectInterpreter;
import com.metsci.glimpse.charts.vector.parser.ENCUnitInterpreter;
import com.metsci.glimpse.charts.vector.parser.ENCAttributeInterpreter.ENCAttributeHeader;
import com.metsci.glimpse.charts.vector.parser.ENCObjectInterpreter.ENCObjectHeader;
import com.metsci.glimpse.charts.vector.parser.ENCUnitInterpreter.ENCUnitHeader;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCAttributeClass;

/**
 * This is an old class that hasn't been run or maintained sitting in a code
 * base that has had many changes.  It is probably eligible for deletion.  
 * It will most likely not work if used.  
 * 
 *
 */
public class ENCLibraryMaker {

    public static void generateENCLibrary(ENCObjectInterpreter objInterp, ENCAttributeInterpreter attributeInterp, ENCUnitInterpreter uInterp) throws IOException{

        BufferedWriter _fout = null;

        Iterator<Integer> _iterator = null;

        // Create folders if necessary

        File _mainLocation 		= new File(location);
        File _instanceLocation 	= new File(instanceLocation);
        File _accessLocation 	= new File(accessLocation);


        _mainLocation.mkdirs();
        _instanceLocation.mkdirs();
        _accessLocation.mkdirs();



        // Generate the object files

        _iterator = objInterp.getObjectHeaders().keySet().iterator();

        while(_iterator.hasNext())
            generateObject(objInterp.getHeader_byCode(_iterator.next()), attributeInterp);


        // Generate the attribute files

        _iterator = attributeInterp.getAttributeHeaders().keySet().iterator();

        while(_iterator.hasNext())
            generateEnumeratedAttribute(attributeInterp.getHeader_byCode(_iterator.next()));


        // Generate Object List

         _fout = new BufferedWriter(new FileWriter(location + "/ENCObjectType.java"));

        // Header stuff
        _fout.write("package " + packageName + ";\n\n");
        _fout.write("import java.util.HashMap;\n");
        _fout.write("import java.io.DataInputStream;\n");
        _fout.write("import java.io.DataOutputStream;\n");
        _fout.write("import java.io.IOException;\n");


        // Generate file
        generateObjectList(_fout, objInterp);

        _fout.close();

        //

        // Generate Attribute List

         _fout = new BufferedWriter(new FileWriter(location + "/ENCAttributeType.java"));

        // Header stuff
        _fout.write("package " + packageName + ";\n\n");
        _fout.write("import java.util.HashMap;\n");
        _fout.write("import java.io.DataInputStream;\n");
        _fout.write("import java.io.DataOutputStream;\n");
        _fout.write("import java.io.IOException;\n");
        _fout.write("import " + encPackageLocation + ".*;\n\n");


        // Generate file
        generateAttributeList(_fout, attributeInterp);

        _fout.close();

        // --

        // Generate Attribute Value List


         _fout = new BufferedWriter(new FileWriter(location + "/ENCAttributeValues.java"));

        // Header stuff
        _fout.write("package " + packageName + ";\n\n");
        _fout.write("import java.util.HashMap;\n");
        _fout.write("import java.io.DataInputStream;\n");
        _fout.write("import java.io.DataOutputStream;\n");
        _fout.write("import java.io.IOException;\n");
        _fout.write("import " + encPackageLocation + ".*;\n\n");


        // Generate file
        generateUniversalAttribute(_fout, attributeInterp);

        _fout.close();

        // --


        // Generate Unit Value List


         _fout = new BufferedWriter(new FileWriter(location + "/ENCUnit.java"));

        // Header stuff
        _fout.write("package " + packageName + ";\n\n");
        _fout.write("import java.util.HashMap;\n");
        _fout.write("import java.io.DataInputStream;\n");
        _fout.write("import java.io.DataOutputStream;\n");
        _fout.write("import java.io.IOException;\n");


        // Generate file
        generateUniversalUnit(_fout, uInterp);

        _fout.close();

        // --


        // Generate Object Enumerable Class

        _iterator = objInterp.getObjectHeaders().keySet().iterator();

        while(_iterator.hasNext()){


             ENCObjectHeader _currentHeader = objInterp.getHeader_byCode(_iterator.next());

            String _className 	 = formatString(_currentHeader.objectClass, StringFormatType.OBJECTNAME);
            String _subClassName = formatString(_currentHeader.objectSubclass, StringFormatType.OBJECTNAME);



            if(_subClassName != null && _subClassName.length() > 0)
                _className += "_" + _subClassName;

             _fout = new BufferedWriter(new FileWriter(accessLocation + "/" + _className + ".java"));


            // Header stuff
            _fout.write("package " + accessPackageName + ";\n\n");
            _fout.write("import java.util.HashMap;\n");
            _fout.write("import " + packageName + ".*;\n");
            _fout.write("import " + encPackageLocation + ".*;\n\n");




            // Generate class file


            generateObjectAttributes(_fout, _currentHeader , attributeInterp);



            _fout.close();
        }

        // --


    }

    private enum ValuationType{
        SingleParam,
        ListParam,
        CodedParam;
    }


    private static void generateObject(ENCObjectHeader header, ENCAttributeInterpreter attributeInterp) throws IOException{

        String _className 	 = formatString(header.objectClass, StringFormatType.OBJECTNAME);
        String _subClassName = formatString(header.objectSubclass, StringFormatType.OBJECTNAME);

        ArrayList<String> _attributeClassNames 				= new ArrayList<String>();
        ArrayList<String> _attributeInstanceNames 			= new ArrayList<String>();
        ArrayList<ValuationType> _attributeValutationList 	= new ArrayList<ValuationType>();

        if(_subClassName != null && _subClassName.length() > 0)
            _className += "_" + _subClassName;

        Iterator<String> _iterator = header.objectAttributes.iterator();

        while(_iterator.hasNext()){

            String _currentAttributeName = _iterator.next();

            ENCAttributeHeader _aHeader = attributeInterp.getHeader_byAcronym(_currentAttributeName);

            switch(_aHeader.attributeClass){
            case Enumerated:
                _attributeClassNames.add(formatString(_aHeader.attributeName, StringFormatType.OBJECTNAME));
                _attributeValutationList.add(ValuationType.SingleParam);
                break;
            case Free:
                _attributeClassNames.add("String");
                _attributeValutationList.add(ValuationType.SingleParam);
                break;
            case Float:
                _attributeClassNames.add("float");
                _attributeValutationList.add(ValuationType.SingleParam);
                break;
            case Integer:
                _attributeClassNames.add("int");
                _attributeValutationList.add(ValuationType.SingleParam);
                break;
            case List:
                _attributeClassNames.add(formatString(_aHeader.attributeName, StringFormatType.OBJECTNAME));
                _attributeValutationList.add(ValuationType.ListParam);
                break;
            case Coded:
                _attributeClassNames.add("String");
                _attributeValutationList.add(ValuationType.SingleParam);
                break;

            }

            _attributeInstanceNames.add(formatString(_aHeader.attributeName, StringFormatType.INSTANCE));
        }



        BufferedWriter _fout = new BufferedWriter(new FileWriter(location + "/" + _className + ".java"));

        // Header stuff
        _fout.write("package " + packageName + ";\n\n");
        _fout.write("import " + encPackageLocation + ";\n\n");


        // Start the class definition
        _fout.write("public class" + _className + " { \n\n");

        // Create the constructor
        _fout.write("\t public " + _className + "(" + abstractObjectName + abstractObjectInstance + ")\n");

        for(int i = 0, n = _attributeClassNames.size(); i < n; i++){
            _fout.write("\t\t // Evaluate: " + _attributeClassNames.get(i) + " \n");
            _fout.write(abstractEvaluation(attributeInterp.getHeader_byAcronym(header.objectAttributes.get(i)),
                        _attributeClassNames.get(i), _attributeInstanceNames.get(i)));
        }

        _fout.write("\t }\n\n");

        // Create member variables
        _fout.write("\n\n\n\n\t // Attributes\n\n");
        for(int i = 0, n = _attributeClassNames.size(); i < n; i++){
            if(_attributeValutationList.get(i) == ValuationType.ListParam){
                _fout.write("\t public final " + _attributeClassNames.get(i) + "[]\t\t" + _attributeInstanceNames.get(i) + "\n");
            }
            else{
                _fout.write("\t public final" + _attributeClassNames.get(i) + "\t\t" + _attributeInstanceNames.get(i) + "\n");
            }
        }


        _fout.write("\n }");
        _fout.close();
    }

    private static String abstractEvaluation(ENCAttributeHeader header, String className, String instanceName){
        String _retVal 		= "";

        _retVal += "// Evaluate only if the value will not be null\n";
        _retVal += "\t\t if(!" + abstractObjectInstance + ".attributeMap.get(\"" + header.attributeAcronym + "\").value.equals(\"(null)\")){\n";

        switch(header.attributeClass){
        case Enumerated:
            _retVal += "\t\t\t " + instanceName + " = ";
            _retVal += className + ".getInstance(";
            _retVal += "Integer.valueOf(" + abstractObjectInstance + ".attributeMap.get(\"" + header.attributeAcronym + "\").value));\n";
            break;
        case Free:
            _retVal += "\t\t\t " + instanceName + " = ";
            _retVal += abstractObjectInstance + ".attributeMap.get(\"" + header.attributeAcronym + "\").value;\n";
            break;
        case Float:
            _retVal += "\t\t\t " + instanceName + " = ";
            _retVal += "Float.valueOf(" + abstractObjectInstance + ".attributeMap.get(\"" + header.attributeAcronym + "\").value);\n";
            break;
        case Integer:
            _retVal += "\t\t\t " + instanceName + " = ";
            _retVal += "Integer.valueOf(" + abstractObjectInstance + ".attributeMap.get(\"" + header.attributeAcronym + "\").value);\n";
            break;
        case List:
            _retVal += "\t\t\t String[] _temp = " + abstractObjectInstance + ".attributeMap.get(\"" + header.attributeAcronym + "\").value.split(\",\");\n";
            _retVal += "\t\t\t " + instanceName + "= new " + className + "[_temp.length]";
            _retVal += "\t\t\t for(int i = 0, n = _temp.length; i < n; i++){";
            _retVal += "\t\t\t\t " + instanceName + "[i] = " + className + ".getInstance(" + "Integer.valueOf(" + abstractObjectInstance + ".attributeMap.get(\"" + header.attributeAcronym + "\").value));\n";
            _retVal += "\t\t\t }";
            break;
        case Coded:
            break;
        }

        _retVal += "\t\t }\n\n";


        return _retVal;
    }

    private static void generateEnumeratedAttribute(ENCAttributeHeader header) throws IOException{

        // We only build enums for listed or enumerable types
        if(header.attributeClass != ENCAttributeClass.Enumerated && header.attributeClass != ENCAttributeClass.List)
            return;

        // If there are no attributes then we cannot do anything!
        if(header.valueTable == null){
            System.out.println("Attribute: " + header.attributeAcronym + "[" + String.valueOf(header.attributeCode) + "] has no lookup table.");
            return;
        }

        String _attributeClassName = formatString(header.attributeName, StringFormatType.OBJECTNAME);

        BufferedWriter _fout = new BufferedWriter(new FileWriter(location + "/" + _attributeClassName + ".java"));

        _fout.write("package " + packageName + "\n\n");


        _fout.write("// Autogenerated ENC Enumerated Attribute File \n");

        _fout.write("public enum " + _attributeClassName + " { \n\n");


        Iterator<Integer> _iterator = header.valueTable.keySet().iterator();
        boolean _hasStarted = true;

        while(_iterator.hasNext()){

            int _currentValueID = _iterator.next();

            String _value 		= header.valueTable.get(_currentValueID);

            // Scrub the string
            _value = formatString(_value, StringFormatType.OBJECTNAME);

            // Start a new line if necessary
            if(!_hasStarted)
                _fout.write(",\n");
            else
                _hasStarted = false;


            // Create the entry
            _fout.write("\t" + _value + "\t (" + String.valueOf(_currentValueID) + ")");
        }
        _fout.write(";\n\n");


        _fout.write("\t private int id;\n\n");

        _fout.write("\t" + _attributeClassName + "(int value){this.id = int;} \n");

        _fout.write("\t public int getID(){return id;}\n");

        _fout.write("\t public static" + _attributeClassName + " getInstance(int val) throws Exception{ \n");
        _fout.write("\t \t for(" + _attributeClassName + " p : " + _attributeClassName + ".values()){\n");
        _fout.write("\t \t \t if(p.getID() == val){return p;}}\n");
        _fout.write("\t throw new Exception(\"Invalid input code: \" + String.valueOf(value));}");

        _fout.write("}\n");
        _fout.close();
    }


    /**
     * Creates the enumerated list of all ENC object types.
     * @param fout
     * @param objInterp
     * @throws IOException
     */
    private static void generateObjectList(BufferedWriter fout, ENCObjectInterpreter objInterp) throws IOException{

        fout.write("public enum ENCObjectType{\n");

        Iterator<Integer> _iterator = objInterp.getObjectHeaders().keySet().iterator();

        while(_iterator.hasNext()){
            int _currentCode = _iterator.next();

            ENCObjectHeader _currentHeader = objInterp.getHeader_byCode(_currentCode);

            String _className 	 = formatString(_currentHeader.objectClass, StringFormatType.OBJECTNAME);
            String _subClassName = formatString(_currentHeader.objectSubclass, StringFormatType.OBJECTNAME);


            if(_subClassName != null && _subClassName.length() > 0)
                _className += "_" + _subClassName;

            fout.write("\t " +  _className + " (" + String.valueOf(_currentHeader.objectCode) + ")");

            if(_iterator.hasNext())
                fout.write(",\n");
            else
                fout.write(";\n\n");
        }

        fout.write("\t public final int code;\n");
        fout.write("\t private static boolean populate = true;\n");
        fout.write("\t private static HashMap<Integer, ENCObjectType> lookupMap = new HashMap<Integer, ENCObjectType>();\n\n");

        fout.write("\t ENCObjectType(int c){\n");
        fout.write("\t\t code = c;\n");
        fout.write("\t }\n\n");

        fout.write("\t public static ENCObjectType getInstance(int c){\n");
        fout.write("\t\t if(populate){\n");
        fout.write("\t\t\t for(ENCObjectType p : ENCObjectType.values())\n");
        fout.write("\t\t\t\t lookupMap.put(p.code, p);\n");
        fout.write("\t\t\t populate = false;\n");
        fout.write("\t\t }\n");
        fout.write("\t\t return lookupMap.get(c);\n");
        fout.write("\t }\n\n");


        fout.write("\t public static void write(DataOutputStream fout, ENCObjectType obj) throws IOException{\n");
        fout.write("\t\t fout.writeInt(obj.code);\n");
        fout.write("\t }\n\n");

        fout.write("\t public static ENCObjectType read(DataInputStream fin) throws IOException{\n");
        fout.write("\t\t return getInstance(fin.readInt());\n");
        fout.write("\t }\n\n");


        fout.write("}\n\n");

    }

    /**
     * Creates the enumerated list of all ENC attribute types
     * @param fout
     * @param attribInterp
     * @throws IOException
     */
    private static void generateAttributeList(BufferedWriter fout, ENCAttributeInterpreter attribInterp) throws IOException{

        fout.write("public enum ENCAttributeType{\n");


        Iterator<Integer> _iterator = attribInterp.getAttributeHeaders().keySet().iterator();

        while(_iterator.hasNext()){
            int _currentCode = _iterator.next();

            ENCAttributeHeader _currentHeader = attribInterp.getHeader_byCode(_currentCode);

            String _attributeName 	 = formatString(_currentHeader.attributeName, StringFormatType.OBJECTNAME);

            fout.write("\t " +  _attributeName + " (" + String.valueOf(_currentHeader.attributeCode) + ")");

            if(_iterator.hasNext())
                fout.write(",\n");
            else
                fout.write(";\n\n");
        }


        fout.write("\t private int code;\n");
        fout.write("\t private static boolean populate = true;\n");
        fout.write("\t private static HashMap<Integer, ENCAttributeType> lookupMap = new HashMap<Integer, ENCAttributeType>();\n\n");

        fout.write("\t ENCAttributeType(int c){\n");
        fout.write("\t\t code = c;\n");
        fout.write("\t }\n\n");

        fout.write("\t public static ENCAttributeType getInstance(int c){\n");
        fout.write("\t\t if(populate){\n");
        fout.write("\t\t\t for(ENCAttributeType p : ENCAttributeType.values())\n");
        fout.write("\t\t\t\t lookupMap.put(p.code, p);\n");
        fout.write("\t\t\t populate = false;\n");
        fout.write("\t\t }\n");
        fout.write("\t\t return lookupMap.get(c);\n");
        fout.write("\t }\n\n");

        fout.write("\t public static ENCAttributeType read(DataInputStream fin) throws IOException{\n");
        fout.write("\t\t return getInstance(fin.readInt());\n");
        fout.write("\t }\n\n");

        fout.write("\t public static void write(DataOutputStream fout, ENCAttributeType attrib) throws IOException{\n");
        fout.write("\t\t fout.writeInt(attrib.code);\n");
        fout.write("\t }\n\n");

        fout.write("}\n\n");

    }


    /**
     * Creates the enumerated list of all ENC attribute values
     * @param fout
     * @param attribInterp
     * @throws IOException
     */
    private static void generateUniversalAttribute(BufferedWriter fout, ENCAttributeInterpreter attribInterp) throws IOException{


        fout.write("public enum ENCAttributeValues{\n");

        // List all of the attributes and values

        Iterator<Integer> _iterator = attribInterp.getAttributeHeaders().keySet().iterator();

        boolean _hasStarted 	= true;
        int     _blankCounter 	= 0;

        while(_iterator.hasNext()){

            int _currentCode = _iterator.next();
            ENCAttributeHeader _currentHeader = attribInterp.getHeader_byCode(_currentCode);

            // If the type has no lookup table then we move onto the next entry
            if(_currentHeader.valueTable == null || _currentHeader.valueTable.size() == 0){

                continue;
            }


            String _attributeName = formatString(_currentHeader.attributeName, StringFormatType.OBJECTNAME);

            Iterator<Integer> _valueIterator = _currentHeader.valueTable.keySet().iterator();

            while(_valueIterator.hasNext()){
                int _currentValue = _valueIterator.next();
                String _valueName = formatString(_currentHeader.valueTable.get(_currentValue), StringFormatType.OBJECTNAME);

                if(_valueName.length() == 0){
                    _valueName = "BLANK" + String.valueOf(++_blankCounter);
                }

                if(!_hasStarted)
                    fout.write(",\n");
                else
                    _hasStarted = false;

                fout.write("\t " + _attributeName + "_" + _valueName + "  (" + String.valueOf(_currentCode) + ", " + String.valueOf(_currentValue) + ")");

            }

        }
        // Close it up
        fout.write(";\n\n");



        fout.write("\t private int attributeID;\n");
        fout.write("\t private int attributeValueID;\n");
        fout.write("\t private static boolean populate = true;\n");
        fout.write("\t private static HashMap<Long, ENCAttributeValues> lookupMap = new HashMap<Long, ENCAttributeValues>();\n\n");

        fout.write("\t ENCAttributeValues(int aid, int avid){\n");
        fout.write("\t\t attributeID = aid;\n");
        fout.write("\t\t attributeValueID = avid;\n");
        fout.write("\t }\n\n");

        fout.write("\t public static ENCAttributeValues getInstance(int aid, int avid){\n");
        fout.write("\t\t long v1 = aid;\n");
        fout.write("\t\t long v2 = avid;\n");
        fout.write("\t\t if(populate){\n");
        fout.write("\t\t\t for(ENCAttributeValues p : ENCAttributeValues.values())\n");
        fout.write("\t\t\t\t lookupMap.put(((long)p.attributeID << 32) + (long)p.attributeValueID, p);\n");
        fout.write("\t\t\t populate = false;\n");
        fout.write("\t\t }\n");
        fout.write("\t\t return lookupMap.get((v1 << 32) + v2);\n");
        fout.write("\t }\n\n");

        fout.write("\t public static void write(DataOutputStream fout, ENCAttributeValues attrib) throws IOException{\n");
        fout.write("\t\t fout.writeInt(attrib.attributeID);\n");
        fout.write("\t\t fout.writeInt(attrib.attributeValueID);\n");
        fout.write("\t }\n\n");

        fout.write("\t public static ENCAttributeValues read(DataInputStream fin) throws IOException{\n");
        fout.write("\t\t int _id  = fin.readInt();\n");
        fout.write("\t\t int _vid = fin.readInt();\n");
        fout.write("\t\t return getInstance(_id, _vid);");
        fout.write("\t }\n\n");

        fout.write("}\n\n");
    }




    private static void generateObjectAttributes(BufferedWriter fout, ENCObjectHeader header, ENCAttributeInterpreter attribInterp) throws IOException{

        String _className 	 = formatString(header.objectClass, StringFormatType.OBJECTNAME);
        String _subClassName = formatString(header.objectSubclass, StringFormatType.OBJECTNAME);


        if(_subClassName != null && _subClassName.length() > 0)
            _className += "_" + _subClassName;

        fout.write("public interface " + _className + "{\n");

        // Create the attributes that the object has

        fout.write("\t public enum Attributes{\n");

        Iterator<String> _iterator = header.objectAttributes.iterator();

        while(_iterator.hasNext()){
            ENCAttributeHeader _currentAttributeHeader = attribInterp.getHeader_byAcronym(_iterator.next());
            String _currentAttributeName = formatString(_currentAttributeHeader.attributeName, StringFormatType.OBJECTNAME);

            fout.write("\t\t " + _currentAttributeName + "_" + _currentAttributeHeader.attributeClass.getTypeCode() + "(){\n");
            fout.write("\t\t\t @Override\n");
            fout.write("\t\t\t public ENCAttributeType lift(){\n");
            fout.write("\t\t\t\t return ENCAttributeType." + _currentAttributeName + ";\n");
            fout.write("\t\t\t }}");

            if(_iterator.hasNext())
                fout.write(",\n");
            else
                fout.write(";\n\n");
        }


        fout.write("\t\t public abstract ENCAttributeType lift();\n\n");
        fout.write("\t}\n\n");

        // Create attribute enum for each attribute that the object has

        _iterator = header.objectAttributes.iterator();

        while(_iterator.hasNext()){
            ENCAttributeHeader _currentAttributeHeader = attribInterp.getHeader_byAcronym(_iterator.next());
            String _currentAttributeClass = formatString(_currentAttributeHeader.attributeName, StringFormatType.OBJECTNAME);
            String _currentAttributeName  = formatString(_currentAttributeHeader.attributeName, StringFormatType.OBJECTNAME);

            // If there are no attribute values then we move on
            if(_currentAttributeHeader.valueTable == null || _currentAttributeHeader.valueTable.size() == 0)
                continue;

            Iterator<Integer> _valueIterator = _currentAttributeHeader.valueTable.keySet().iterator();

            fout.write("\t public enum " + _currentAttributeClass + "{\n");

            while(_valueIterator.hasNext()){

                int _currentValue = _valueIterator.next();

                String _currentValueName = formatString(_currentAttributeHeader.valueTable.get(_currentValue), StringFormatType.OBJECTNAME);

                fout.write("\t\t " + _currentValueName + "(){\n");
                fout.write("\t\t\t @Override\n");
                fout.write("\t\t\t public ENCAttributeValues lift(){\n");
                fout.write("\t\t\t\t return ENCAttributeValues." + _currentAttributeName + "_" + _currentValueName + ";\n");
                fout.write("\t\t\t }}\n");

                if(_valueIterator.hasNext())
                    fout.write(",\n");
                else
                    fout.write(";\n\n");

            }

            fout.write("\t\t public abstract ENCAttributeValues lift();\n\n");
            fout.write("\t}");
        }

        fout.write("}\n\n");
    }


    private static void generateUniversalUnit(BufferedWriter fout, ENCUnitInterpreter unitInterp) throws IOException {


        ArrayList<ENCUnitHeader> _headerList = unitInterp.getUnitHeaders();

        HashMap<Integer, Integer> _globalToLocalIdx = new HashMap<Integer, Integer>();

        for(int i = 0, n = _headerList.size(); i < n; i++){
            _globalToLocalIdx.put(_headerList.get(i).code, i);
        }

        double[][] _conversionMatrixA = unitInterp.getAMatrix();
        double[][] _conversionMatrixB = unitInterp.getBMatrix();



        fout.write("public enum ENCUnit{\n");

        // Print out the types of units

        for(int i = 0, n = _headerList.size(); i < n; i++){

            if(i != 0){
                fout.write(",\n");
            }

            fout.write("\t " + formatString(_headerList.get(i).name, StringFormatType.OBJECTNAME) + "_"
                             + formatString(_headerList.get(i).type, StringFormatType.OBJECTNAME) + "("
                             + String.valueOf(i) + ", " + String.valueOf(_headerList.get(i).code) + ")");
        }
        fout.write(";\n\n\n");



        fout.write("\t private int localID;\n");
        fout.write("\t private int ENCID;\n");
        fout.write("\t private static boolean populate = true;\n");
        fout.write("\t private static HashMap<Integer, ENCUnit> lookupMap = new HashMap<Integer, ENCUnit>();\n");
        fout.write("\t private static double[][] conversionMatrixA = null;\n");
        fout.write("\t private static double[][] conversionMatrixB = null;\n\n");



        // Create Methods

        // Constructor
        fout.write("\t ENCUnit(int lid, int gid){\n");
        fout.write("\t\t localID = lid; \n");
        fout.write("\t\t ENCID   = gid; \n");
        fout.write("\t }\n\n");


        // Instance generator
        fout.write("\t public static ENCUnit getInstance(int gid){\n");
        fout.write("\t\t if(populate){\n");
        fout.write("\t\t\t for(ENCUnit p : ENCUnit.values())\n");
        fout.write("\t\t\t\t lookupMap.put(p.ENCID, p);\n");
        fout.write("\t\t\t populate = false;\n");
        fout.write("\t\t\t }\n");
        fout.write("\t\t // There are conflicting numbering schemes -- this is a fix\n");
        fout.write("\t\t int _adjValue = (gid % 9000) + 9000;\n");
        fout.write("\t\t return lookupMap.get(_adjValue);\n");
        fout.write("\t }\n\n");

        fout.write("\t public static ENCUnit read(DataInputStream fin) throws IOException{\n");
        fout.write("\t\t return getInstance(fin.readInt());\n");
        fout.write("\t }\n\n");

        fout.write("\t public static void write(DataOutputStream fout, ENCUnit unit) throws IOException{\n");
        fout.write("\t\t fout.writeInt(unit.ENCID);\n");
        fout.write("\t }\n\n");

        // Conversion methods
        fout.write("\t public double convertTo(double value, ENCUnit type) throws Exception{\n");
        fout.write("\t\t if(conversionMatrixA == null){populateTableA(); populateTableB();}\n");
        fout.write("\t\t if(conversionMatrixA[this.localID][type.localID] == 0)\n");
        fout.write("\t\t\t throw new Exception(\"Cannot perform conversion.\"); \n");
        fout.write("\t\t return conversionMatrixB[this.localID][type.localID] * value / conversionMatrixA[this.localID][type.localID];\n");
        fout.write("\t }\n\n");

        fout.write("\t public double convertFrom(double value, ENCUnit type) throws Exception{\n");
        fout.write("\t\t if(conversionMatrixA == null){populateTableA(); populateTableB();}\n");
        fout.write("\t\t if(conversionMatrixA[type.localID][this.localID] == 0)\n");
        fout.write("\t\t\t throw new Exception(\"Cannot perform conversion.\"); \n");
        fout.write("\t\t return conversionMatrixB[type.localID][this.localID] * value / conversionMatrixA[type.localID][this.localID];\n");
        fout.write("\t }\n\n");

        fout.write("\t private static void populateTableA(){\n");
        // Write A-Matrix
        fout.write("\t\t conversionMatrixA = new double[][]{\n");

        for(int i = 0, n = _conversionMatrixA.length; i < n; i++){
            fout.write("\t\t\t{");
            for(int j = 0; j < n; j++){
                fout.write(String.valueOf(_conversionMatrixA[i][j]));

                if(j != (n - 1)){
                    fout.write(", ");
                }
            }
            if(i != (n - 1))
                fout.write("},\n");
            else
                fout.write("}\n");
        }

        fout.write("\t\t};\n");
        fout.write("\t}\n\n");

        fout.write("\t private static void populateTableB(){\n");
        // Write B-Matrix
        fout.write("\t\t conversionMatrixB = new double[][]{\n");

        for(int i = 0, n = _conversionMatrixB[i].length; i < n; i++){
            fout.write("\t\t\t{");
            for(int j = 0; j < n; j++){
                fout.write(String.valueOf(_conversionMatrixB[i][j]));

                if(j != (n - 1)){
                    fout.write(", ");
                }
            }
            if(i != (n - 1))
                fout.write("},\n");
            else
                fout.write("}\n");
        }

        fout.write("\t\t};\n");
        fout.write("\t}");


        fout.write("}");

    }




    private enum StringFormatType{
        ENUMVALUE,
        OBJECTNAME,
        INSTANCE;
    }

    private static String formatString(String value, StringFormatType type){

        // If the string is empty we cannot do anything
        if(value == null || value.length() == 0)
            return value;

        String _retVal   = new String(value);
        char[] _valArray = null;



        // Make upper case
        _retVal = _retVal.toLowerCase();


        // Remove unwanted contents
        _valArray = _retVal.toCharArray();

        for(int i = 0, n = _valArray.length; i < n; i++){

            /*
            // Contents in parenthesis
            if(_valArray[i] == '('){
                _valArray[i] = ' ';
                i++;
                while(_valArray[i] != ')'){
                    _valArray[i] = ' ';
                    i++;
                }
                _valArray[i] = ' ';
            }
            */

            // Unwanted characters
            if(_valArray[i] == '\'' || _valArray[i] == '`'){

                // For apostrophes we perform a backward shift
                for(int j = i; j < _valArray.length - 1; j++){
                    _valArray[j] = _valArray[j + 1];
                }
                _valArray[_valArray.length - 1] = ' ';
            }
            else if((_valArray[i] < 'a' || _valArray[i] > 'z') && (_valArray[i] < '0' || _valArray[i] > '9')){
                _valArray[i] = ' ';
            }
        }
        _retVal = new String(_valArray);


        // Remove redundant spaces
        String[] _contents = _retVal.split(" ");

        _retVal = "";

        if(type == StringFormatType.OBJECTNAME){
            for(int i = 0; i < _contents.length; i++){
                if(_contents[i].length() > 0){

                    String _firstLetter = "";
                    _firstLetter += _contents[i].charAt(0);
                    _firstLetter = _firstLetter.toUpperCase();
                    _firstLetter += _contents[i].substring(1);

                    _retVal += _firstLetter;
                }
            }
        }
        else if(type == StringFormatType.ENUMVALUE){
            for(int i = 0; i < _contents.length; i++){
                if(_contents[i].length() > 0){
                    // Add some padding space
                    if(i != 0)
                        _retVal += '_';

                    _retVal += _contents[i];
                }
            }
            _retVal = _retVal.toUpperCase();
        }
        else if(type == StringFormatType.INSTANCE){
            for(int i = 0; i < _contents.length; i++){
                if(_contents[i].length() > 0){

                    if(i > 0 && _retVal.length() > 0){
                        String _firstLetter = "";
                        _firstLetter += _contents[i].charAt(0);
                        _firstLetter = _firstLetter.toUpperCase();
                        _firstLetter += _contents[i].substring(1);

                        _retVal += _firstLetter;
                    }
                    else{
                        _retVal += _contents[i];
                    }
                }
            }
        }


        return _retVal;
    }



    // Root Location
    public static String location				= "/path/to/location";
    public static String encPackageLocation     = "com.metsci.glimpse.charts.vector.parser";
    public static String packageName			= "com.metsci.glimpse.charts.vector.parser.autogen";

    // Instance Classes
    public static String instanceLocation		= "/path/to/instanceLocation";
    public static String instancePackageName	= "com.metsci.glimpse.charts.vector.parser.autogen.instance";

    // Accessing Classes
    public static String accessLocation			= "/path/to/accessLocation";
    public static String accessPackageName		= "com.metsci.glimpse.charts.vector.parser.autogen.access";

    // Outside Class Names
    public static String abstractObjectInstance = "obj";
    public static String abstractObjectName 	= "ENCAbstractObject";
    public static String abstractAttributeName 	= "ENCAbstractAttribute";


    public static void main(String[] args){

        try{
            ENCUnitInterpreter _uint		= new ENCUnitInterpreter();
            ENCAttributeInterpreter _aint 	= new ENCAttributeInterpreter(_uint);
            ENCObjectInterpreter _oint 		= new ENCObjectInterpreter(_aint);



            ENCLibraryMaker.generateENCLibrary(_oint, _aint, _uint);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

}
