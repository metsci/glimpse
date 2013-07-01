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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObjectType;
import com.metsci.glimpse.charts.vector.parser.objects.GeoFeatureType;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;


/**
 * A filter for filtering geoobjects by feature type
 * 
 * @author Cunningham
 */
public class GeoFilterByFeatureCode<V extends GeoObject> implements GeoFilter<V> {

    private Set<? extends GeoFeatureType> objectTypeSet;

    protected GeoFilterByFeatureCode(Set<? extends ENCObjectType> objectTypeSet) {
        this.objectTypeSet = objectTypeSet;
    }

    @Override
    public boolean passGeoFilter(V geoObj) {
        return objectTypeSet.contains(geoObj.getGeoFeatureType());
    }


    public static class ENCFilterBuilder {

        private Set<ENCObjectType> objectTypeSet;

        public static ENCFilterBuilder allOff() {
            Set<ENCObjectType> objectTypeSet = EnumSet.<ENCObjectType>allOf(ENCObjectType.class);
            return new ENCFilterBuilder(objectTypeSet);
        }

        public static ENCFilterBuilder noneOff() {
            Set<ENCObjectType> objectTypeSet = EnumSet.<ENCObjectType>noneOf(ENCObjectType.class);
            return new ENCFilterBuilder(objectTypeSet);
        }

        public ENCFilterBuilder(Set<ENCObjectType> objectTypeSet) {
            this.objectTypeSet = objectTypeSet;
        }

        public GeoFilterByFeatureCode<ENCObject> build() {
            return new GeoFilterByFeatureCode(objectTypeSet);
        }

        public ENCFilterBuilder addType(ENCObjectType type) {
            objectTypeSet.add(type);
            return this;
        }

        public ENCFilterBuilder addTypes(ENCObjectType ... types) {
            objectTypeSet.addAll(Arrays.asList(types));
            return this;
        }

        public ENCFilterBuilder removeType(ENCObjectType type) {
            objectTypeSet.remove(type);
            return this;
        }

        public ENCFilterBuilder removeTypes(ENCObjectType ... types) {
            objectTypeSet.removeAll(Arrays.asList(types));
            return this;
        }
    }
}
