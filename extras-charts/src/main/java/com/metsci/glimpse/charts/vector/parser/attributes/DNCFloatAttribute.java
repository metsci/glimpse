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


public class DNCFloatAttribute extends DNCAbstractAttribute{
    private final float attributeValue;


    public DNCFloatAttribute(DNCAttributeType type) {
        super(type, true);
        attributeValue = Float.NaN;
    }

    public DNCFloatAttribute(DNCAttributeType type, float attributeValue) {
        super(type, false);
        this.attributeValue = attributeValue;
    }

    public DNCFloatAttribute(DNCAttributeType type, String attributeValueText) {
        super(type, false);
        if (attributeValueText.endsWith("nan"))
            attributeValue = Float.NaN;
        else {
            attributeValue = Float.valueOf(attributeValueText);
        }
    }

    public float getAttributeValue(){
        return attributeValue;
    }


    @Override
    public DNCAttributeClass getAttributeClass() {
        return DNCAttributeClass.Float;
    }

    @Override
    public Object getAttributeValueAsObject() {
        return Float.valueOf(attributeValue);
    }

    @Override
    public String getAttributeValueAsString() {
        return attributeValue + "";
    }

    protected void write0(DataOutputStream fout) throws IOException {
        if (isNullValued())
            fout.writeBoolean(false);
        else {
            fout.writeBoolean(true);
            fout.writeFloat(getAttributeValue());
        }
    }

    public static DNCFloatAttribute read(DataInput fin) throws IOException{
        DNCAttributeType attributeType = DNCAttributeType.read(fin);

        boolean haveValue = fin.readBoolean();
        if(! haveValue) {
            return new DNCFloatAttribute(attributeType);
        } else {
            float attributeValue = fin.readFloat();
            return new DNCFloatAttribute(attributeType, attributeValue);
        }

    }

}
