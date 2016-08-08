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
package com.metsci.glimpse.plot.timeline.listener;

import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;

public class DataAxisMouseListener1D extends AxisMouseListener1D
{
    protected static final int AXIS_SIZE = 28;

    protected StackedTimePlot2D plot;
    protected PlotInfo info;

    protected boolean axisSelected;

    protected int axisSize = AXIS_SIZE;

    public DataAxisMouseListener1D( StackedTimePlot2D plot, PlotInfo info )
    {
        this.info = info;
        this.plot = plot;
    }

    public void setAxisSize( int size )
    {
        this.axisSize = size;
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent e )
    {
        GlimpseAxisLayout1D layout = getAxisLayout( e );
        if ( layout == null ) return;

        if ( e.isAnyButtonDown( ) && this.axisSelected )
        {
            super.mouseMoved( e );
            e.setHandled( true );
        }
    }

    @Override
    public void mousePressed( GlimpseMouseEvent e )
    {
        plot.setSelectedPlot( info );

        this.axisSelected = isAxisSelected( e );

        if ( this.axisSelected )
        {
            super.mousePressed( e );
            e.setHandled( true );
        }
    }

    protected boolean isAxisSelected( GlimpseMouseEvent e )
    {
        GlimpseAxisLayout1D layout = getAxisLayout( e );
        if ( layout == null ) return false;

        if ( e.isKeyDown( ModifierKey.Shift ) ) return true;
        if ( layout.isHorizontal( ) && e.getY( ) < axisSize ) return true;
        if ( !layout.isHorizontal( ) && e.getX( ) < axisSize ) return true;

        return false;
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent e )
    {
        super.mouseReleased( e );
    }

    @Override
    public void mouseWheelMoved( GlimpseMouseEvent e )
    {
        this.axisSelected = isAxisSelected( e );

        if ( this.axisSelected )
        {
            super.mouseWheelMoved( e );
            e.setHandled( true );
        }
    }
}
