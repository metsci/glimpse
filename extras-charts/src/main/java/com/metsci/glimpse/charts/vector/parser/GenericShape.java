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

import java.util.Arrays;

public class GenericShape {
    private String type;
    private double[][] values;

    public GenericShape(String t, String v){
        type 	= t;
        values 	= getPoints(v);
    }

    public String getType() {
        return type;
    }

    public boolean isEmpty() {
        return values.length == 0;
    }

    public int numCoordinates() {
        return values.length;
    }

    public int getPointSize() {
        return values[0].length;
    }

    public double [] getCoordinate(int index) {
        return values[index];
    }

    private static double[][] getPoints(String values){

        String[] _entries = values.split(",");

        double[][] _retVal = new double[_entries.length][];

        for(int i = 0, n = _entries.length; i < n; i++){

            String[] _currentPoint = _entries[i].split(" ");

            double[] _newPoint = new double[_currentPoint.length];

            for(int j = 0; j < _currentPoint.length; j++){
                _newPoint[j] = Double.valueOf(_currentPoint[j]);
            }

            _retVal[i] = _newPoint;
        }

        return _retVal;
    }

    @Override
    public String toString(){

        StringBuilder _retVal = new StringBuilder();

        _retVal.append("[type = " + type + ", values = [ ");

        for(int i = 0; i < values.length; i++){

            _retVal.append(Arrays.toString(values[i]));
            _retVal.append(" ");

        }

        _retVal.append("]");


        return _retVal.toString();
    }

}
