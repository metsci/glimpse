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

import com.metsci.glimpse.charts.vector.parser.objects.DNCAttributeType;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInput;


public class DNCFreeAttribute extends DNCAbstractAttribute{
    private final String attributeValue;


    public DNCFreeAttribute(DNCAttributeType type) {
        super(type, true);
        attributeValue = null;
    }

    public DNCFreeAttribute(DNCAttributeType type, String attributeValue) {
        super(type, false);
        this.attributeValue = attributeValue;
    }

    public String getAttributeValue(){
        return attributeValue;
    }

    @Override
    public Object getAttributeValueAsObject() {
        return attributeValue;
    }

    @Override
    public String getAttributeValueAsString() {
        return (attributeValue == null) ? null : attributeValue;
    }

    @Override
    public DNCAttributeClass getAttributeClass() {
        return DNCAttributeClass.Free;
    }

    protected void write0(DataOutputStream fout) throws IOException {
        if(attributeValue == null){
            fout.writeBoolean(false);
        }
        else{
            fout.writeBoolean(true);
            fout.writeUTF(attributeValue);
        }
    }


    public static DNCFreeAttribute read(DataInput fin) throws IOException{
        DNCAttributeType attributeName = DNCAttributeType.read(fin);

        boolean haveValue = fin.readBoolean();
        if(! haveValue) {
            return new DNCFreeAttribute(attributeName);
        } else {
            String attributeValue = fin.readUTF().intern();
            return new DNCFreeAttribute(attributeName, attributeValue);
        }

        /*
        DNCFreeAttribute _retVal = new DNCFreeAttribute();

        _retVal.setAttributeName(fin.readUTF().intern());

        boolean _haveValue = fin.readBoolean();
        if(_haveValue) {
            String attributeValue = fin.readUTF();
            _retVal.setAttributeValue(attributeValue.intern());
        }

        _retVal.isNull = ! _haveValue;

        return _retVal;
         */
    }

}
