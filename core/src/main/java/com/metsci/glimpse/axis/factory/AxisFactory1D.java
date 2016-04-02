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
package com.metsci.glimpse.axis.factory;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;

/**
 * Glimpse axes may be used in many different contexts (defined by a unique GlimpseTargetStack).
 * When an axis is used in a new context, a new copy is created for that context (which might
 * have different bounds, or require different rendering). Instances of AxisFactory, which every
 * GlimpseTarget must provide, define how this new copy is created.
 *
 * @author ulman
 */
public interface AxisFactory1D
{
    /**
     * Creates a copy of the provided axis which is valid for the given GlimpseTargetStack.<p>
     *
     * Normally, this method simply calls Axis1D.clone( ) which returns a copied axis linked
     * to the original. However, other implementations of AxisFactory may have different
     * behavior (perhaps not linking the original and copy, or setting the copy to a fixed size).<p>
     *
     * The GlimpseTargetStack argument may be used if the axis which is created should be different
     * depending on where the axis is being used. Most normal implementations can ignore this.
     *
     * @param stack the context in which the axis is being used
     * @param axis the original/parent axis
     * @return a new Axis1D which will be used when the original axis is used in the context
     *         described by the GlimpseTargetStack argument
     */
    public Axis1D newAxis( GlimpseTargetStack stack, Axis1D axis );
}
