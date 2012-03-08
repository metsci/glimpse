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
import com.metsci.glimpse.charts.vector.parser.autogen.ENCAttributeValues;

import java.io.DataInput;

public class ENCListAttribute extends ENCAbstractAttribute{
    private ENCAttributeValues[] attributeValue;

    public ENCListAttribute(){
        attributeValue = null;
    }

    public ENCAttributeValues[] getENCAttributeValue(){
        return attributeValue;
    }
    public void setENCAttributeValue(ENCAttributeValues[] value){
        attributeValue = value;
    }

    @Override
    public ENCAttributeClass getAttributeClass() {
        return ENCAttributeClass.List;
    }

    @Override
    public Object getAttributeValueAsObject() {
        if (attributeValue == null)
            return null;
        else
            return attributeValue;
    }

    public String getAttributeValueAsString() {
        if (attributeValue == null)
            return null;
        StringBuilder builder = new StringBuilder();
        builder.append(attributeValue[0].name());
        for (int i = 1; i < attributeValue.length; i++) {
            if (attributeValue[i] != null) {
                builder.append(", ");
                builder.append(attributeValue[i].name());
            }
        }
        return builder.toString();
    }

    protected void write0(DataOutputStream fout) throws IOException {
        ENCAttributeType.write(fout, getAttributeType());

        if(attributeValue == null){
            fout.writeBoolean(false);
        }
        else{
            fout.writeBoolean(true);

            ENCAttributeValues[] _list = getENCAttributeValue();
            fout.writeInt(_list.length);
            for(int i = 0, n = _list.length; i < n; i++){
                ENCAttributeValues.write(fout, _list[i]);
            }
        }
    }

    public static ENCListAttribute read(DataInput fin) throws IOException{
        ENCListAttribute _retVal = new ENCListAttribute();

        _retVal.setAttributeType(ENCAttributeType.read(fin));

        boolean _haveValue = fin.readBoolean();
        if(_haveValue) {
            int _length = fin.readInt();
            ENCAttributeValues[] _list = new ENCAttributeValues[_length];

            for(int i = 0; i < _length; i++){
                _list[i] = ENCAttributeValues.read(fin);
            }
            _retVal.setENCAttributeValue(_list);
        }

        _retVal.isNull = ! _haveValue;

        return _retVal;
    }
}
