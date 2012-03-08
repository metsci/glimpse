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
package com.metsci.glimpse.charts.vector.parser.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import com.metsci.glimpse.charts.vector.parser.autogen.ENCAttributeType;

import java.io.DataInput;

public abstract class ENCAbstractAttribute implements GeoAttribute {
    protected boolean			 	isNull;
    protected ENCAttributeType   	attributeType;

    public ENCAbstractAttribute() { }

    public abstract ENCAttributeClass getAttributeClass();

    public abstract Object getAttributeValueAsObject();

    public abstract String getAttributeValueAsString();

    public ENCAttributeType getAttributeType(){
        return attributeType;
    }
    public void setAttributeType(ENCAttributeType type){
        attributeType = type;
    }

    public boolean isNullValued(){
        return isNull;
    }

    public String toString() {
        String strValue = getAttributeValueAsString();
        return "[" + attributeType + "] " + ((strValue == null) ? "([" + getAttributeClass() + "]) null" : strValue);
    }

    public AttributeDescription getDescription() {
        return new AttributeDescription(getAttributeType(), getAttributeClass(), getAttributeValueAsString());
    }

    /**
     * Writes the attribute to the given stream.  Derived classes need to provide an write0 method
     * implementation to write their part of the data out to the stream.
     * @param fout
     * @throws IOException
     */
    final public void write(DataOutputStream fout) throws IOException {
        getAttributeClass().write(fout);
        write0(fout);
    }

    protected abstract void write0(DataOutputStream fout) throws IOException;

    public static ENCAbstractAttribute read(DataInput fin) throws IOException {
        ENCAttributeClass _currentClass = ENCAttributeClass.read(fin);
        switch(_currentClass){
        case Coded:
            return ENCCodedAttribute.read(fin);
        case Enumerated:
            return ENCEnumAttribute.read(fin);
        case Float:
            return ENCFloatAttribute.read(fin);
        case Free:
            return ENCFreeAttribute.read(fin);
        case Integer:
            return ENCIntAttribute.read(fin);
        case List:
            return ENCListAttribute.read(fin);
        default:
            throw new IOException("ENCAbstractAttribute -- Invalid file format.");
        }
    }
}
