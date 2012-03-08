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

import com.metsci.glimpse.charts.vector.parser.ENCAttributeInterpreter;
import com.metsci.glimpse.charts.vector.parser.ENCObjectInterpreter.ENCObjectHeader;

public class ENCObjectMaker {



    private void generateObjectClass(ENCObjectHeader header, ENCAttributeInterpreter attributeInterp){



    }


    private String cleanObjectName(String value){

        String _retVal   = new String(value);
        char[] _valArray = null;

        // Make upper case
        _retVal = _retVal.toLowerCase();


        // Remove unwanted contents
        _valArray = _retVal.toCharArray();

        for(int i = 0, n = _valArray.length; i < n; i++){

            // Contents in parenthesis
            if(_valArray[i] == '('){
                _valArray[i] = ' ';

                while(_valArray[i++] != ')'){
                    _valArray[i] = ' ';
                }
                _valArray[i] = ' ';
            }

            // Unwanted characters
            else if(_valArray[i] == '\'' || _valArray[i] == '`'){

                // For apostrophes we perform a backward shift
                for(int j = i; j < _valArray.length - 1; j++){
                    _valArray[j] = _valArray[j + 1];
                }
                _valArray[_valArray.length - 1] = ' ';
            }
            else if(_valArray[i] < 'a' || _valArray[i] > 'z'){
                _valArray[i] = ' ';
            }
        }
        _retVal = new String(_valArray);


        // Remove redundant spaces
        String[] _contents = _retVal.split(" ");

        _retVal = "";

        // Put in canonical form
        for(int i = 0; i < _contents.length; i++){
            if(_contents[i].length() > 0){

                String _firstLetter = "";
                _firstLetter += _contents[i].charAt(0);
                _firstLetter.toUpperCase();
                _firstLetter += _contents[i].substring(1);

                _retVal += _firstLetter;
            }
        }


        return _retVal;
    }


    private String packageName;
    private String location;
}
