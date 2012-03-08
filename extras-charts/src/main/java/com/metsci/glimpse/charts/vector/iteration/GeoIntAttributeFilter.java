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
package com.metsci.glimpse.charts.vector.iteration;

import com.metsci.glimpse.charts.vector.parser.attributes.ENCIntAttribute;
import com.metsci.glimpse.charts.vector.parser.attributes.GeoIntAttribute;
import com.metsci.glimpse.charts.vector.parser.autogen.ENCAttributeType;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoAttributeType;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;


/**
 * Filters GeoObjects for a given int attribute value against a given comparison 
 * value and operator (<, <=, =, >=, >).
 * 
 * @author Cunningham 
 */
public class GeoIntAttributeFilter<V extends GeoObject> implements GeoFilter<V> {

    public enum Operator {
        lessThan {
            protected boolean operate(int attribValue, int comparisonValue) {
                return attribValue < comparisonValue;
            }
        },
        lessThanEqualTo {
            protected boolean operate(int attribValue, int comparisonValue) {
                return attribValue <= comparisonValue;
            }
        },
        equalTo {
            protected boolean operate(int attribValue, int comparisonValue) {
                return attribValue == comparisonValue;
            }
        },
        greaterThan {
            protected boolean operate(int attribValue, int comparisonValue) {
                return attribValue > comparisonValue;
            }
        },
        greaterThanEqualTo {
            protected boolean operate(int attribValue, int comparisonValue) {
                return attribValue >= comparisonValue;
            }
        },
        nonNull {
            protected boolean operate(int attribValue, int comparisonValue) {
                return true;
            }
        };

        private static ENCIntAttribute extract(ENCObject object, ENCAttributeType intAttribKey) {
            return object.getIntAttribute(intAttribKey);
        }

        abstract protected boolean operate(int attribValue, int comparisonValue);
    }

    private GeoAttributeType intAttributeKey;
    private Operator operator;
    private int comparisonValue;
    private boolean nullsAreTrue;

    public GeoIntAttributeFilter(GeoAttributeType intAttributeKey, Operator operator, int comparisonValue) {
        this(intAttributeKey, operator, comparisonValue, false);
    }

    public GeoIntAttributeFilter(GeoAttributeType intAttributeKey, Operator operator, int comparisonValue, boolean nullsAreTrue) {
        this.intAttributeKey = intAttributeKey;
        this.operator = operator;
        this.comparisonValue = comparisonValue;
        this.nullsAreTrue = nullsAreTrue;
    }

    @Override
    public boolean passGeoFilter(GeoObject geoObject) {
        GeoIntAttribute intAttrib = geoObject.getGeoIntAttribute(intAttributeKey);
        if (intAttrib == null || intAttrib.isNullValued())
            return nullsAreTrue;

        return operator.operate(intAttrib.getAttributeValue(), comparisonValue);
    }
}
