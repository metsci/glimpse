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
package com.metsci.glimpse.axis.listener.mouse;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;

/**
 * An AxisMouseListener for use with GlimpseAxisLayout2D.
 *
 * @author ulman
 * @see com.metsci.glimpse.layout.GlimpseAxisLayout2D
 */
public class AxisMouseListener2D extends AxisMouseListener
{
    protected GlimpseAxisLayout2D getAxisLayout( GlimpseMouseEvent event )
    {
        GlimpseTargetStack stack = event.getTargetStack( );
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
    public void mousePressed( GlimpseMouseEvent event )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( event );
        if ( layout == null ) return;

        Axis2D axis = layout.getAxis( event.getTargetStack( ) );

        this.mousePressed( event, axis.getAxisX( ), true );
        this.mousePressed( event, axis.getAxisY( ), false );
        this.applyAndUpdate( axis.getAxisX( ), axis.getAxisY( ) );
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent event )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( event );
        if ( layout == null ) return;

        Axis2D axis = layout.getAxis( event.getTargetStack( ) );

        this.mouseMoved( event, axis.getAxisX( ), true );
        this.mouseMoved( event, axis.getAxisY( ), false );
        this.applyAndUpdate( axis.getAxisX( ), axis.getAxisY( ) );
    }

    @Override
    public void mouseWheelMoved( GlimpseMouseEvent event )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( event );
        if ( layout == null ) return;

        Axis2D axis = layout.getAxis( event.getTargetStack( ) );

        this.mouseWheelMoved( event, axis.getAxisX( ), true );
        this.mouseWheelMoved( event, axis.getAxisY( ), false );
        this.applyAndUpdate( axis.getAxisX( ), axis.getAxisY( ) );
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent event )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( event );
        if ( layout == null ) return;

        Axis2D axis = layout.getAxis( event.getTargetStack( ) );

        this.mouseReleased( event, axis.getAxisX( ), true );
        this.mouseReleased( event, axis.getAxisY( ), false );
        this.applyAndUpdate( axis.getAxisX( ), axis.getAxisY( ) );
    }

    @Override
    public void mouseEntered( GlimpseMouseEvent event )
    {
        // do nothing
    }

    @Override
    public void mouseExited( GlimpseMouseEvent event )
    {
        // do nothing
    }

    public void applyAndUpdate( Axis1D axisX, Axis1D axisY )
    {
        axisX.applyConstraints( );
        axisY.applyConstraints( );

        axisX.updateLinkedAxes( axisY );
        axisY.updateLinkedAxes( );
    }
}
