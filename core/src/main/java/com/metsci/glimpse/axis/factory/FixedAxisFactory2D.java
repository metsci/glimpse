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

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseTargetStack;

/**
 * An AxisFactory2D which provides unlinked axis copies which
 * are set to an explicit min and max value.
 *
 * @author ulman
 */
public class FixedAxisFactory2D implements AxisFactory2D
{
    protected double minX;
    protected double maxX;
    protected double minY;
    protected double maxY;

    public FixedAxisFactory2D( double minX, double maxX, double minY, double maxY )
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public Axis2D newAxis( GlimpseTargetStack stack, Axis2D axis )
    {
        // don't just create a new Axis1D( ) -- if axis is actually
        // some subclass of Axis1D (like a TaggedAxis1D) we want to preserve that
        Axis2D newAxis = axis.clone( );
        newAxis.setParent( null );

        newAxis.getAxisX( ).setMin( minX );
        newAxis.getAxisX( ).setMax( maxX );
        newAxis.getAxisY( ).setMin( minY );
        newAxis.getAxisY( ).setMax( maxY );

        newAxis.getAxisX( ).lockMin( minX );
        newAxis.getAxisX( ).lockMax( maxX );
        newAxis.getAxisY( ).lockMin( minY );
        newAxis.getAxisY( ).lockMax( maxY );

        return newAxis;
    }

    @Override
    public AxisFactory1D getAxisFactoryX( GlimpseTargetStack stack )
    {
        return new FixedAxisFactory1D( minX, maxX );
    }

    @Override
    public AxisFactory1D getAxisFactoryY( GlimpseTargetStack stack )
    {
        return new FixedAxisFactory1D( minY, maxY );
    }
}
