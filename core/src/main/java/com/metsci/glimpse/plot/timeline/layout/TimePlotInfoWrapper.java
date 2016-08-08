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
package com.metsci.glimpse.plot.timeline.layout;

import java.awt.Font;

import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.plot.stacked.PlotInfoWrapper;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.listener.DataAxisMouseListener1D;

public class TimePlotInfoWrapper extends PlotInfoWrapper implements TimePlotInfo
{
    protected TimePlotInfo info;

    public TimePlotInfoWrapper( TimePlotInfo info )
    {
        super( info );
        this.info = info;
    }

    @Override
    public TaggedAxis1D getCommonAxis( )
    {
        return info.getCommonAxis( );
    }

    @Override
    public TaggedAxis1D getCommonAxis( GlimpseTargetStack stack )
    {
        return info.getCommonAxis( stack );
    }

    @Override
    public void setTimeToolTipHandler( TimeToolTipHandler toolTipHandler )
    {
        info.setTimeToolTipHandler( toolTipHandler );
    }

    @Override
    public DataAxisMouseListener1D getDataAxisMouseListener( )
    {
        return info.getDataAxisMouseListener( );
    }

    @Override
    public void setDefaultEventBorderColor( float[] rgba )
    {
        info.setDefaultEventBorderColor( rgba );
    }

    @Override
    public void setBorderWidth( float width )
    {
        info.setBorderWidth( width );
    }

    @Override
    public void setLabelBorderColor( float[] rgba )
    {
        info.setLabelBorderColor( rgba );
    }

    @Override
    public void setLabelBorderWidth( float width )
    {
        info.setLabelBorderWidth( width );
    }

    @Override
    public void setLabelText( String text )
    {
        info.setLabelText( text );
    }

    @Override
    public void setLabelColor( float[] rgba )
    {
        info.setLabelColor( rgba );
    }

    @Override
    public void setAxisColor( float[] rgba )
    {
        info.setAxisColor( rgba );
    }

    @Override
    public void setAxisFont( Font font )
    {
        info.setAxisFont( font );
    }

    @Override
    public void setBackgroundColor( float[] rgba )
    {
        info.setBackgroundColor( rgba );
    }

    @Override
    public GlimpseLayout getLabelLayout( )
    {
        return info.getLabelLayout( );
    }

    @Override
    public BackgroundPainter getBackgroundPainter( )
    {
        return info.getBackgroundPainter( );
    }

    @Override
    public GridPainter getGridPainter( )
    {
        return info.getGridPainter( );
    }

    @Override
    public NumericXYAxisPainter getAxisPainter( )
    {
        return info.getAxisPainter( );
    }

    @Override
    public SimpleTextPainter getLabelPainter( )
    {
        return info.getLabelPainter( );
    }

    @Override
    public BorderPainter getBorderPainter( )
    {
        return info.getBorderPainter( );
    }

    @Override
    public BorderPainter getLabelBorderPainter( )
    {
        return info.getLabelBorderPainter( );
    }

    @Override
    public StackedTimePlot2D getStackedTimePlot( )
    {
        return info.getStackedTimePlot( );
    }

    @Override
    public DelegatePainter getDataPainter( )
    {
        return info.getDataPainter( );
    }

    @Override
    public void addPainter( GlimpsePainter painter )
    {
        info.addPainter( painter );
    }

    @Override
    public void removePainter( GlimpsePainter painter )
    {
        info.removePainter( painter );
    }

}
