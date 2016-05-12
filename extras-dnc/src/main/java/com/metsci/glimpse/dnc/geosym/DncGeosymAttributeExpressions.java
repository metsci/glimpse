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

import static com.metsci.glimpse.util.GeneralUtils.array;
import static com.metsci.glimpse.util.GeneralUtils.newArrayList;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

public class DncGeosymAttributeExpressions
{

    public static DncGeosymAttributeExpression buildAttributeExpression(List<String> connectorOps, List<? extends DncGeosymAttributeExpression> predicates)
    {
        List<String> ops = newArrayList(connectorOps);
        List<DncGeosymAttributeExpression> terms = newArrayList(predicates);
        if (terms.size() != ops.size() + 1) throw new IllegalArgumentException("Operator count and predicate count don't match: operator-count = " + ops.size() + ", predicate-count = " + terms.size());

        // Add a dummy operator at the end so that the final
        // term gets handled correctly by the loop
        //
        ops.add("Dummy Operator");

        for (String currentOp : array("and", "or", "AND", "OR"))
        {
            ListIterator<String> iOp = ops.listIterator();
            ListIterator<DncGeosymAttributeExpression> iTerm = terms.listIterator();
            List<DncGeosymAttributeExpression> currentTerms = newArrayList();
            while (iOp.hasNext())
            {
                String op = iOp.next();
                DncGeosymAttributeExpression term = iTerm.next();

                if (op.equals(currentOp))
                {
                    iOp.remove();
                    iTerm.remove();
                    currentTerms.add(term);
                }
                else if (!currentTerms.isEmpty())
                {
                    iTerm.remove();
                    currentTerms.add(term);
                    iTerm.add( new DncGeosymAttributeCompoundExpression(currentOp, currentTerms) );
                    currentTerms = newArrayList();
                }
            }
        }

        if (terms.size() != 1) throw new RuntimeException("Failed to collapse attribute-expression terms");
        return terms.get(0);
    }

    public static final DncGeosymAttributeExpression alwaysTrue = new DncGeosymAttributeExpression()
    {
        public boolean eval(Function<String,Object> featureAttrs, Function<String,Object> externalAttrs)
        {
            return true;
        }
    };

}
