/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.axis.factory;

import static com.metsci.glimpse.core.context.TargetStackUtil.endsWith;

import com.metsci.glimpse.core.context.GlimpseTargetStack;

/**
 * A {@link ConditionalAxisFactory2D} which chooses the AxisFactory2D to use based on
 * whether the query GlimpseTargetStack ends with the associated GlimpseTargetStack.
 *
 * @author ulman
 */
public class ConditionalEndsWithAxisFactory2D extends ConditionalAxisFactory2D
{
    public ConditionalEndsWithAxisFactory2D( )
    {
        super( );
    }

    public ConditionalEndsWithAxisFactory2D( GlimpseTargetStack stack, AxisFactory2D factory )
    {
        super( stack, factory );
    }

    @Override
    protected boolean isConditionMet( GlimpseTargetStack stack, GlimpseTargetStack candidate )
    {
        return endsWith( stack, candidate );
    }
}