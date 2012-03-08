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
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author john
 */
public class DNCAttributeType implements GeoAttributeType {

    public static DNCAttributeType FACCCode = new DNCAttributeType("f_code");
    public static DNCAttributeType NAM = new DNCAttributeType("nam");

    private static Map<String, DNCAttributeType> lookupMap = new HashMap<String, DNCAttributeType>() {
        {
            put(FACCCode.code, FACCCode);
            put(NAM.code, NAM);
        }
    };

    public static DNCAttributeType getInstance(String c) {
        DNCAttributeType type = lookupMap.get(c);
        if (type == null) {
            c = c.intern();
            type = new DNCAttributeType(c);
            lookupMap.put(c, type);
        }
        return type;
    }

    private String code;

    private DNCAttributeType(String c) {
        code = c;
    }

    public String name() {
        return code;
    }

    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (this == other) return true;
        if ( !(other instanceof DNCAttributeType) )
            return false;
        return internalEquals((DNCAttributeType) this);
    }

    public boolean equals(DNCAttributeType otherType) {
        if (otherType == null) return false;
        if (this == otherType) return true;
        return internalEquals(otherType);
    }

    private boolean internalEquals(DNCAttributeType otherType) {

        return code.equals(otherType.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    public void write(DataOutputStream fout) throws IOException {
        fout.writeUTF(code);
    }

    public static DNCAttributeType read(DataInput fin) throws IOException {
        String code = fin.readUTF();
        return getInstance(code);
    }


}
