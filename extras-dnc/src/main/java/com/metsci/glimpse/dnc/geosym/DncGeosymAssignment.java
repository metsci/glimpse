/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.dnc.geosym;

import static com.metsci.glimpse.util.GeneralUtils.newUnmodifiableList;

import java.util.List;

import com.google.common.base.Function;

public class DncGeosymAssignment
{

    public final int id;

    public final String fcode;
    public final String delineation;
    public final String coverageType;
    public final DncGeosymAttributeExpression attrExpr;

    public final String pointSymbolId;
    public final String lineSymbolId;
    public final String areaSymbolId;
    public final int displayPriority;

    // Can be null
    public final String orientationAttr;

    public final List<DncGeosymLabelMaker> labelMakers;


    public DncGeosymAssignment(int id,
                               String fcode,
                               String delineation,
                               String coverageType,
                               DncGeosymAttributeExpression attrExpr,
                               String pointSymbolId,
                               String lineSymbolId,
                               String areaSymbolId,
                               int displayPriority,
                               String orientationAttr,
                               List<DncGeosymLabelMaker> labelMakers)
    {
        this.id = id;

        this.fcode = fcode;
        this.delineation = delineation;
        this.coverageType = coverageType;
        this.attrExpr = attrExpr;

        this.pointSymbolId = pointSymbolId;
        this.lineSymbolId = lineSymbolId;
        this.areaSymbolId = areaSymbolId;
        this.displayPriority = displayPriority;

        this.orientationAttr = orientationAttr;

        this.labelMakers = newUnmodifiableList(labelMakers);
    }

    public boolean matches(String featureFcode,
                           String featureDelineation,
                           String featureCoverageType,
                           Function<String,Object> featureAttrs,
                           Function<String,Object> externalAttrs)
    {
        return fcode.equalsIgnoreCase(featureFcode)
                && (delineation == null || delineation.isEmpty() || delineation.equalsIgnoreCase(featureDelineation))
                && (coverageType == null || coverageType.isEmpty() || coverageType.equalsIgnoreCase(featureCoverageType))
                && attrExpr.eval(featureAttrs, externalAttrs);
    }

    public boolean hasAreaSymbol()
    {
        return (areaSymbolId != null && !areaSymbolId.isEmpty());
    }

    public boolean hasLineSymbol()
    {
        return (lineSymbolId != null && !lineSymbolId.isEmpty());
    }

    public boolean hasPointSymbol()
    {
        return (pointSymbolId != null && !pointSymbolId.isEmpty());
    }

}
