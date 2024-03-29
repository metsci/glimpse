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
package com.metsci.glimpse.core.axis.listener;

import com.metsci.glimpse.core.axis.Axis2D;

/**
 * An AxisListener2D which guarantees that axisUpdated( Axis2D ) will not be
 * called at greater than a given rate.
 *
 * @author ulman
 *
 */
public abstract class RateLimitedAxisListener2D extends RateLimitedEventDispatcher<Axis2D> implements AxisListener2D
{
    public RateLimitedAxisListener2D( )
    {
        super( 1000l / 60l );
    }

    public RateLimitedAxisListener2D( double maxFreqHz )
    {
        super( ( long ) ( 1000 / maxFreqHz ) );
    }

    public RateLimitedAxisListener2D( long _idleTimeMillis )
    {
        super( _idleTimeMillis );
    }

    public RateLimitedAxisListener2D( String name )
    {
        super( 1000l / 60l, name );
    }

    public RateLimitedAxisListener2D( double maxFreqHz, String name )
    {
        super( ( long ) ( 1000 / maxFreqHz ), name );
    }

    public RateLimitedAxisListener2D( long _idleTimeMillis, String name )
    {
        super( _idleTimeMillis, name );
    }

    public abstract void axisUpdatedRateLimited( Axis2D axis );

    @Override
    public void axisUpdated( Axis2D axis )
    {
        eventOccurred( axis );
    }

    @Override
    public void eventDispatch( Axis2D data )
    {
        axisUpdatedRateLimited( data );
    }
}
