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
package com.metsci.glimpse.spacenav;

import static com.metsci.glimpse.spacenav.SpaceNavPoller.*;
import static java.lang.Math.abs;

import com.metsci.glimpse.axis.Axis1D;

public class AxisNavigator implements SpaceNavListener
{
    protected SpaceNavState lastState;

    protected Axis1D xAxis;
    protected Axis1D yAxis;
    protected Axis1D zAxis;

    double zoomRate = 1 / 100d;

    // (axis width) PER (1 second)
    double panRate = 2d / 3d;

    public AxisNavigator( Axis1D xTrans, Axis1D yTrans, Axis1D zTrans )
    {
        this.xAxis = xTrans;
        this.yAxis = yTrans;
        this.zAxis = zTrans;
    }

    @Override
    public void update( SpaceNavState state )
    {
        if ( lastState == null )
        {
            lastState = state;
            return;
        }

        double dt = ( state.systemTimeMillis - lastState.systemTimeMillis ) / 1000d;
        if ( dt > 2 * POLL_INTERVAL / 1000d )
        {
            lastState = state;
        }
        else
        {
            if ( xAxis != null ) panAxis( xAxis, panRate, getIntensity( -state.xTranslation, POLL_INTERVAL ) );
            if ( yAxis != null ) panAxis( yAxis, panRate, getIntensity( state.yTranslation, POLL_INTERVAL ) );

            if ( xAxis != null ) zoomAxis( xAxis, zoomRate, getIntensity( state.zTranslation, POLL_INTERVAL ) );
            if ( yAxis != null ) zoomAxis( yAxis, zoomRate, getIntensity( state.zTranslation, POLL_INTERVAL ) );

            //            if( zAxis != null ) zoomAxis( zAxis, zoomRate, getIntensity(  state.yRotation, POLL_INTERVAL ) );
            if ( zAxis != null ) panAxis( zAxis, panRate, getIntensity( state.zRotation, POLL_INTERVAL ) );

            lastState = state;
        }
    }

    private static double getIntensity( double state, double interval )
    {
        // for a polling interval of 20ms
        if ( state > 350 )
        {
            return 1;
        }
        else if ( state < -350 )
        {
            return -1;
        }
        else
        {
            return state / 350;
        }
    }

    private static void panAxis( Axis1D axis, double panRate, double intensity )
    {
        if ( abs( intensity ) < 0.01 ) return;

        double axisWidth = axis.getMin( ) - axis.getMax( );
        double panDistance = axisWidth * panRate * intensity * POLL_INTERVAL / 1000d;

        axis.setMin( axis.getMin( ) + panDistance );
        axis.setMax( axis.getMax( ) + panDistance );
        axis.validate( );
        axis.updateLinkedAxes( );
    }

    private static void zoomAxis( Axis1D axis, double zoomRate, double intensity )
    {
        if ( abs( intensity ) < 0.1 ) return;

        double minValue = axis.getMin( );
        double maxValue = axis.getMax( );
        double axisWidth = maxValue - minValue;

        double updatedMin = minValue + axisWidth * zoomRate * intensity;
        double updatedMax = maxValue - axisWidth * zoomRate * intensity;

        axis.setMin( updatedMin );
        axis.setMax( updatedMax );
        axis.validate( );
        axis.updateLinkedAxes( );
    }
}
