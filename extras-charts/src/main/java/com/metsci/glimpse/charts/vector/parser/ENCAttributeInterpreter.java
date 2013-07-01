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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.metsci.glimpse.charts.vector.parser.ENCUnitInterpreter.UnitInterpreterException;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCAbstractAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCAttributeClass;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCCodedAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCEnumAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCFloatAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCFreeAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCIntAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.ENCListAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.GenericAttribute;
import com.metsci.glimpse.charts.vector.parser.autogen.ENCAttributeType;
import com.metsci.glimpse.charts.vector.parser.autogen.ENCAttributeValues;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;
import com.metsci.glimpse.util.io.StreamOpener;


public class ENCAttributeInterpreter {
    private static Logger logger = Logger.getLogger(ENCAttributeInterpreter.class.toString());

    private Map<Integer, ENCAttributeHeader> attributeHeader_byID;
    private NavigableMap<String, ENCAttributeHeader> attributeHeader_byAcronym;
    private final ENCUnitInterpreter unitInterpreter;
    private String s57AttributesFile = "enc/s57attributes.csv";
    private String s57ExpectedInputFile = "enc/s57expectedinput.csv";


    public class ENCAttributeHeader {

        public ENCAttributeHeader(int code, String acronym, String name, char type) throws Exception {

            attributeAcronym = acronym;
            attributeName = name;
            attributeCode = code;
            attributeClass = ENCAttributeClass.getAttribute(type);
            attributeType = ENCAttributeType.getInstance(code);

            valueTable = null;
        }

        public void setTableValue(int id, String value) {
            if (valueTable == null) {
                valueTable = new HashMap<Integer, String>();
            }


            valueTable.put(id, value);
        }
        public String attributeAcronym;
        public String attributeName;
        public int attributeCode;
        public ENCAttributeClass attributeClass;
        public ENCAttributeType attributeType;
        public HashMap<Integer, String> valueTable;
    }

    public class AttributeInterpreterException extends Exception {

        public AttributeInterpreterException(String s) {
            super(s);
        }
        private static final long serialVersionUID = 1L;
    }

    public enum AttributeType {

        Enumerated('E'),
        List('L'),
        Float('F'),
        Integer('I'),
        Coded('A'),
        Free('S');
        private char attributeType;

        AttributeType(char atype) {
            this.attributeType = atype;
        }

        public char getTypeCode() {
            return attributeType;
        }

        public static AttributeType getAttribute(char atype) throws Exception {
            for (AttributeType p : AttributeType.values()) {
                if (p.getTypeCode() == atype) {
                    return p;
                }
            }

            throw new Exception("Invalid input code: " + atype);
        }
    }

    public ENCAttributeInterpreter(ENCUnitInterpreter unitInterp) {
        attributeHeader_byID = new HashMap<Integer, ENCAttributeHeader>();
        attributeHeader_byAcronym = new TreeMap<String, ENCAttributeHeader>();

        unitInterpreter = unitInterp;

        try {
            buildAttributeHeaders();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isValidAcronym(String s) {
        return attributeHeader_byAcronym.containsKey(s);
    }

    public ENCAbstractAttribute convertAttribute(GenericAttribute attrib, GenericObject obj) throws AttributeInterpreterException {

        ENCAttributeHeader _header = getHeader_byAcronym(attrib.getName());
        ENCAbstractAttribute _retVal = null;

        try {
            switch (_header.attributeClass) {
                case Coded:
                    _retVal = new ENCCodedAttribute();
                    if (!attrib.getValue().equals("(null)")) {
                        ((ENCCodedAttribute) _retVal).setENCAttributeValue(attrib.getValue());
                    }
                    break;
                case Enumerated:
                    _retVal = new ENCEnumAttribute();
                    if (!attrib.getValue().equals("(null)")) {
                        ((ENCEnumAttribute) _retVal).setENCAttributeValue(ENCAttributeValues.getInstance(_header.attributeCode, Integer.valueOf(attrib.getValue())));

                        if (((ENCEnumAttribute) _retVal).getENCAttributeValue() == null) {
                            logger.warning("Unable to parse attribute -----> Code = " + String.valueOf(_header.attributeCode) + ", Value = " + attrib.getValue());
                        }
                    }
                    break;
                case Float:
                    _retVal = new ENCFloatAttribute();
                    if (!attrib.getValue().equals("(null)")) {
                        ((ENCFloatAttribute) _retVal).setENCAttributeValue(Float.valueOf(attrib.getValue()), unitInterpreter.resolveUnitType(_header.attributeCode, obj));
                    }
                    break;
                case Free:
                    _retVal = new ENCFreeAttribute();
                    if (!attrib.getValue().equals("(null)")) {
                        ((ENCFreeAttribute) _retVal).setENCAttributeValue(attrib.getValue());
                    }
                    break;
                case Integer:
                    _retVal = new ENCIntAttribute();
                    if (!attrib.getValue().equals("(null)")) {
                        ((ENCIntAttribute) _retVal).setAttributeValue(Integer.valueOf(attrib.getValue()), unitInterpreter.resolveUnitType(_header.attributeCode, obj));
                    }
                    break;
                case List:
                    _retVal = new ENCListAttribute();

                    if (attrib.getValue().equals("(null)")) {
                        break;
                    }


                    String[] _contents = attrib.getValue().split(",");
                    int[] _intValueList = new int[_contents.length];
                    int _correctValues = 0;
                    int _nullCounter = 0;
                    int _tempIdx = 0;

                    for (int i = 0; i < _contents.length; i++) {
                        try {
                            _intValueList[_correctValues] = Integer.valueOf(_contents[i]);
                            _correctValues++;
                        } catch (NumberFormatException e) {
                            // fall through
                        }
                    }

                    if (_correctValues == 0) {
                        break;
                    }

                    ENCAttributeValues _listValues[] = new ENCAttributeValues[_correctValues];

                    for (int i = 0; i < _correctValues; i++) {
                        _listValues[i] = ENCAttributeValues.getInstance(_header.attributeCode, _intValueList[i]);

                        if (_listValues[i] == null) {
                            logger.warning("Unable to parse attribute -----> Code = " + String.valueOf(_header.attributeCode) + ", Value = " + String.valueOf(_intValueList[i]));
                            _nullCounter++;
                        }
                    }

                    ENCAttributeValues _updatedListValues[] = new ENCAttributeValues[_correctValues - _nullCounter];
                    for (int i = 0; i < _correctValues; i++) {
                        if (_listValues[i] != null) {
                            _updatedListValues[_tempIdx] = _listValues[i];
                            _tempIdx++;
                        }
                    }

                    ((ENCListAttribute) _retVal).setENCAttributeValue(_updatedListValues);

                    break;
                default:
                    throw new AttributeInterpreterException("Cannot establish attribute context -- unknown attribute class.");
            }
        } catch (UnitInterpreterException e) {
            e.printStackTrace();
            return null;
        }

        _retVal.setAttributeType(_header.attributeType);

        return _retVal;
    }

    public Map<Integer, ENCAttributeHeader> getAttributeHeaders() {
        return attributeHeader_byID;
    }

    public ENCAttributeHeader getHeader_byAcronym(String acronym) {
        return attributeHeader_byAcronym.get(acronym);
    }

    public ENCAttributeHeader getHeader_byCode(int code) {
        return attributeHeader_byID.get(code);
    }

    private void buildAttributeHeaders() throws Exception {
        InputStream inputStream = StreamOpener.fileThenResource.openForRead(s57AttributesFile);
        BufferedReader _fin = new BufferedReader(new InputStreamReader(inputStream));

        // Ignore the first line
        String _line = _fin.readLine();

        CsvTokenizer csvTokenizer = new CsvTokenizer(_fin);
        List<String> _fields = new ArrayList<String>();

        while (csvTokenizer.readStringList(_fields)) {

            int _currentCode = Integer.valueOf(_fields.get(0));

            ENCAttributeHeader _newHeader = null;
            try {
                _newHeader = new ENCAttributeHeader(_currentCode, _fields.get(2), _fields.get(1), _fields.get(3).charAt(0));
            } catch (Exception e) {
                System.out.println("ENCAttributeInterpreter caught exception at line " + csvTokenizer.getLineNumber());
                throw e;
            }

            attributeHeader_byID.put(_currentCode, _newHeader);
            attributeHeader_byAcronym.put(_fields.get(2), _newHeader);
        }

        _fin.close();


        // Build the lookup tables
        inputStream = StreamOpener.fileThenResource.openForRead(s57ExpectedInputFile);
         _fin = new BufferedReader(new InputStreamReader(inputStream));

        //#_fin = new BufferedReader(new FileReader(s57ExpectedInputFile));

        _line = _fin.readLine();

        while ((_line = _fin.readLine()) != null) {


            String[] _contents = _line.split(",", 3);

            int _attributeId = Integer.valueOf(_contents[0]);
            int _valueId = Integer.valueOf(_contents[1]);

            String _value = "";
            if (_contents.length == 3 && _contents[2].length() > 0) {
                _value = _contents[2];

                if (_value.charAt(0) == '\"') {
                    _value = _value.substring(1, _value.length() - 1);
                }
            }


            // Append it to the attribute header
            attributeHeader_byID.get(_attributeId).setTableValue(_valueId, _value);

        }

        _fin.close();
    }

}
