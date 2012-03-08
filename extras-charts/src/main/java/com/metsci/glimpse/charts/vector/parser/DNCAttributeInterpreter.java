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


import com.metsci.glimpse.charts.vector.parser.attributes.DNCAbstractAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.DNCFloatAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.DNCFreeAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.DNCIntAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.GenericAttribute;
import com.metsci.glimpse.charts.vector.parser.objects.DNCAttributeType;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;

import java.util.logging.Logger;


public class DNCAttributeInterpreter {
    private static Logger logger = Logger.getLogger(DNCAttributeInterpreter.class.toString());

    public DNCAbstractAttribute convertAttribute(GenericAttribute attrib, GenericObject obj)  {
        //  STATUS (String) = 1
        //  VERACC (Real) = (null)
        //  VERDAT (Integer) = (null)

        DNCAttributeType attributeType = DNCAttributeType.getInstance(attrib.getName());

        String value = getNullAdjValue(attrib.getValue());

        DNCAbstractAttribute attribute = null;
        if (attrib.getType().equals("(Integer)")) {
            attribute = new DNCIntAttribute(attributeType, value);
        } else if (attrib.getType().equals("(String)")) {
            attribute = new DNCFreeAttribute(attributeType, value);
        } else if (attrib.getType().equals("(Real)")) {
            attribute = new DNCFloatAttribute(attributeType, value);
        } else
            throw new IllegalStateException("Unknown type for attribute " + attrib);
        return attribute;
    }

    private String getNullAdjValue(String value) {
        if (value == null)
            return null;
        if (value.equals("(null)"))
            return null;
        return value;
    }

}
