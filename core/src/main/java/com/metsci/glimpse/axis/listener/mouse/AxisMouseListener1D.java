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
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;

/**
 * An AxisMouseListener for use with GlimpseAxisLayout1D.
 *
 * @author ulman
 * @see com.metsci.glimpse.layout.GlimpseAxisLayout1D
 */
public class AxisMouseListener1D extends AxisMouseListener
{
    protected GlimpseAxisLayout1D getAxisLayout( GlimpseMouseEvent event )
    {
        GlimpseTargetStack stack = event.getTargetStack( );
        GlimpseTarget target = stack.getTarget( );

        if ( target instanceof GlimpseAxisLayout1D )
        {
            return ( GlimpseAxisLayout1D ) target;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void mousePressed( GlimpseMouseEvent event )
    {
        GlimpseAxisLayout1D layout = getAxisLayout( event );
        if ( layout == null ) return;

        Axis1D targetAxis = layout.getAxis( event.getTargetStack( ) );

        this.mousePressed( event, targetAxis, layout.isHorizontal( ) );
        this.validateAxes( targetAxis );
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent event )
    {
        GlimpseAxisLayout1D layout = getAxisLayout( event );
        if ( layout == null ) return;

        Axis1D targetAxis = layout.getAxis( event.getTargetStack( ) );

        this.mouseMoved( event, targetAxis, layout.isHorizontal( ) );
        this.validateAxes( targetAxis );
    }

    @Override
    public void mouseWheelMoved( GlimpseMouseEvent event )
    {
        GlimpseAxisLayout1D layout = getAxisLayout( event );
        if ( layout == null ) return;

        Axis1D targetAxis = layout.getAxis( event.getTargetStack( ) );

        this.mouseWheelMoved( event, targetAxis, layout.isHorizontal( ) );
        this.validateAxes( targetAxis );
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent event )
    {
        GlimpseAxisLayout1D layout = getAxisLayout( event );
        if ( layout == null ) return;

        Axis1D targetAxis = layout.getAxis( event.getTargetStack( ) );

        this.mouseReleased( event, targetAxis, layout.isHorizontal( ) );
        this.validateAxes( targetAxis );
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

    protected void validateAxes( Axis1D axis )
    {
        axis.applyConstraints( );

        if ( axis.getLockedAspectAxis( ) != null ) axis.getLockedAspectAxis( ).applyConstraints( );

        axis.updateLinkedAxes( );

        if ( axis.getLockedAspectAxis( ) != null ) axis.getLockedAspectAxis( ).updateLinkedAxes( );
    }

}
