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
package com.metsci.glimpse.core.event.touch;

import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.context.GlimpseTarget;
import com.metsci.glimpse.core.context.GlimpseTargetStack;
import com.metsci.glimpse.core.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.core.layout.GlimpseAxisLayout2D;

public class GlimpseSwipeGestureEvent extends GlimpseGestureEvent
{
    protected float angle;
    protected int ntouches;

    public GlimpseSwipeGestureEvent( String source, GlimpseTargetStack stack, int x, int y, float angle, int ntouches )
    {
        super( source, stack, x, y );
        this.ntouches = ntouches;
        this.angle = angle;
    }

    /**
     * The angle of the swipe, in MATH RADIANS.
     */
    public float getAngle( )
    {
        return angle;
    }

    public int getNumTouches( )
    {
        return ntouches;
    }

    public Axis1D getAxis1D( )
    {
        GlimpseTargetStack stack = getTargetStack( );
        GlimpseTarget target = stack.getTarget( );

        if ( target instanceof GlimpseAxisLayout1D )
        {
            GlimpseAxisLayout1D layout = ( GlimpseAxisLayout1D ) target;
            return layout.getAxis( stack );
        }
        else
        {
            return null;
        }
    }

    public Axis2D getAxis2D( )
    {
        GlimpseTargetStack stack = getTargetStack( );
        GlimpseTarget target = stack.getTarget( );

        if ( target instanceof GlimpseAxisLayout2D )
        {
            GlimpseAxisLayout2D layout = ( GlimpseAxisLayout2D ) target;
            return layout.getAxis( stack );
        }
        else
        {
            return null;
        }
    }

    @Override
    public GlimpseSwipeGestureEvent withNewTarget( GlimpseTargetStack targetStack, int x, int y )
    {
        return new GlimpseSwipeGestureEvent( source, targetStack, x, y, angle, ntouches );
    }

    @Override
    public String toString( )
    {
        return String.format( "x,y = %d,%d direction: %d  ntouches: %d", x, y, angle, ntouches );
    }
}
