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

import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.util.Pair;

/**
 * An axis factory which acts as a different AxisFactory depending on what
 * context (defined by a GlimpseTargetStack) it is used under.
 *
 * @author ulman
 *
 * @see com.metsci.glimpse.examples.screenshot.ScreenCaptureExample
 * @see com.metsci.glimpse.context.GlimpseTargetStack
 *
 */
public abstract class ConditionalAxisFactory2D implements AxisFactory2D
{
    protected List<Pair<GlimpseTargetStack, AxisFactory2D>> delegateList;
    protected AxisFactory2D defaultFactory;

    public ConditionalAxisFactory2D( )
    {
        delegateList = new ArrayList<Pair<GlimpseTargetStack, AxisFactory2D>>( );
        defaultFactory = new DefaultAxisFactory2D( );
    }

    public ConditionalAxisFactory2D( GlimpseTargetStack stack, AxisFactory2D factory )
    {
        this( );

        addFactory( stack, factory );
    }

    public void addFactory( GlimpseTargetStack stack, AxisFactory2D factory )
    {
        delegateList.add( new Pair<GlimpseTargetStack, AxisFactory2D>( stack, factory ) );
    }

    @Override
    public Axis2D newAxis( GlimpseTargetStack stack, Axis2D axis )
    {
        return getAxisFactory( stack ).newAxis( stack, axis );
    }

    @Override
    public AxisFactory1D getAxisFactoryX( GlimpseTargetStack stack )
    {
        return getAxisFactory( stack ).getAxisFactoryX( stack );
    }

    @Override
    public AxisFactory1D getAxisFactoryY( GlimpseTargetStack stack )
    {
        return getAxisFactory( stack ).getAxisFactoryY( stack );
    }

    protected AxisFactory2D getAxisFactory( GlimpseTargetStack stack )
    {
        for ( Pair<GlimpseTargetStack, AxisFactory2D> pair : delegateList )
        {
            GlimpseTargetStack candidateStack = pair.first( );
            AxisFactory2D candidateFactory = pair.second( );

            if ( isConditionMet( stack, candidateStack ) )
            {
                return candidateFactory;
            }
        }

        return defaultFactory;
    }

    protected abstract boolean isConditionMet( GlimpseTargetStack stack, GlimpseTargetStack candidate );

}
