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
package com.metsci.glimpse.axis.listener.touch;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.touch.GlimpseLongPressGestureEvent;
import com.metsci.glimpse.event.touch.GlimpsePanGestureEvent;
import com.metsci.glimpse.event.touch.GlimpsePinchGestureEvent;
import com.metsci.glimpse.event.touch.GlimpseSwipeGestureEvent;
import com.metsci.glimpse.event.touch.GlimpseTapGestureEvent;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;

/**
 * An AxisGestureListener for use with GlimpseAxisLayout2D.
 *
 * @author ulman
 * @see com.metsci.glimpse.layout.GlimpseAxisLayout2D
 */
public class AxisGestureListener2D extends AxisGestureListener
{
    protected GlimpseAxisLayout2D getAxisLayout( GlimpseTargetStack stack )
    {
        GlimpseTarget target = stack.getTarget( );

        if ( target instanceof GlimpseAxisLayout2D )
        {
            return ( GlimpseAxisLayout2D ) target;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void panDetected( GlimpsePanGestureEvent event )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( event.getTargetStack( ) );
        if ( layout == null ) return;

        Axis2D targetAxis = layout.getAxis( event.getTargetStack( ) );
        pan( targetAxis.getAxisX( ), true, event.getDx( ), event.getDy( ) );
        pan( targetAxis.getAxisY( ), false, event.getDx( ), event.getDy( ) );

        applyAndUpdate( targetAxis.getAxisX( ), targetAxis.getAxisY( ) );
    }

    @Override
    public void pinchDetected( GlimpsePinchGestureEvent event )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( event.getTargetStack( ) );
        if ( layout == null ) return;

        float scaleX = getScale( true, event.getScale( ), event.getAngle( ) );
        float scaleY = getScale( false, event.getScale( ), event.getAngle( ) );

        Axis2D targetAxis = layout.getAxis( event.getTargetStack( ) );
        zoom( targetAxis.getAxisX( ), true, event.getX( ), event.getX( ), scaleX );
        zoom( targetAxis.getAxisY( ), false, event.getX( ), event.getX( ), scaleY );

        applyAndUpdate( targetAxis.getAxisX( ), targetAxis.getAxisY( ) );
    }

    public void applyAndUpdate( Axis1D axisX, Axis1D axisY )
    {
        axisX.applyConstraints( );
        axisY.applyConstraints( );

        axisX.updateLinkedAxes( axisY );
        axisY.updateLinkedAxes( );
    }

    @Override
    public void tapDetected( GlimpseTapGestureEvent event )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( event.getTargetStack( ) );
        if ( layout == null ) return;

        Axis2D targetAxis = layout.getAxis( event.getTargetStack( ) );
        select( targetAxis.getAxisX( ), true, event.getX( ), event.getY( ) );
        select( targetAxis.getAxisY( ), false, event.getX( ), event.getY( ) );

        applyAndUpdate( targetAxis.getAxisX( ), targetAxis.getAxisY( ) );
    }

    @Override
    public void longPressDetected( GlimpseLongPressGestureEvent event )
    {
    }

    @Override
    public void swipeDetected( GlimpseSwipeGestureEvent event )
    {
    }
}
