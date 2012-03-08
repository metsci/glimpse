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

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Stores all the vertex information for a ENC/DNC object
 * 
 * @author Cunningham 
 */
public abstract class AbstractGeoShape implements GeoShape {
    private int				pointSize;
    private GeoShapeType 	type;
    private double[] 		values;

    public AbstractGeoShape(){
        type 			= GeoShapeType.Unspecified;
        values 			= null;
        pointSize 		= 0;
    }


    public GeoShapeType getShapeType(){
        return type;
    }
    public double[] getRawVertexArray(){
        return values;
    }
    /**
     *
     * @param dim 0 for lon, 1 for lat, 2 for depth(?) if applicable
     * @param index
     * @return
     */
    public double getVertex(int dim, int index) {
        int arrayIndex = index * pointSize + dim;
        return values[arrayIndex];
    }

    public double[] getVertexes(int index, double [] buffer) {
        if (buffer == null)
            buffer = new double[pointSize];
        int startIndex = index * pointSize;
        System.arraycopy(values, startIndex, buffer, 0, pointSize);
        return buffer;
    }

    public int getNumCoordinates() {
        return values.length / pointSize;
    }

    /**
     * Number of dimensions in vertex array. Typically 2 for lon lat.
     * @return
     */
    public int getPointSize(){
        return pointSize;
    }

    public void setShapeType(GeoShapeType t){
        type = t;
    }

    public void setVertexPoints(double[] vals){
        values = Arrays.copyOf(vals, vals.length);
    }

    public void setPointSize(int s){
        pointSize = s;
    }

    public boolean passSanityCheck() {
        return values.length % pointSize == 0;
    }

    public static void write(DataOutputStream fout, AbstractGeoShape shape) throws IOException{
        shape.write(fout);
    }

    public void write(DataOutputStream fout) throws IOException {
        write0(fout);
    }

    public void write0(DataOutputStream fout) throws IOException {
        // Write the type
        type.write(fout);

        // If the shape is unspecified then return
        if(type == GeoShapeType.Unspecified)
            return;

        // Otherwise, write the rest of the data
        fout.writeInt(pointSize);

        fout.writeInt(values.length);
        for(int i = 0; i < values.length; i++){
            fout.writeDouble(values[i]);
        }
    }

    protected static void read0(DataInput fin, AbstractGeoShape shape) throws IOException{
        // Read in the shape type
        shape.type = shape.type.read(fin);

        // If the type is specified, then read the rest in
        if(shape.type != GeoShapeType.Unspecified){
            shape.pointSize = fin.readInt();

            int _arrayLength = fin.readInt();
            shape.values = new double[_arrayLength];

            for(int i = 0; i < _arrayLength; i++){
                shape.values[i] = fin.readDouble();
            }
        }
    }
}
