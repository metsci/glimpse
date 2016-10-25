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
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.listener.DataAxisMouseListener1D;

/**
 * A handle to one of the plotting areas making up a {@link StackedTimePlot2D}.
 * TimePlotInfo can be used to modify the look and feel of the plot, change its
 * ordering or size within the overall StackedTimePlot2D, or add painters to the plot.
 *
 * @author ulman
 */
public interface TimePlotInfo extends PlotInfo
{
    public void setTimeToolTipHandler( TimeToolTipHandler toolTipHandler );

    public DataAxisMouseListener1D getDataAxisMouseListener( );

    public void setDefaultEventBorderColor( float[] rgba );

    public void setBorderWidth( float width );

    public void setLabelBorderColor( float[] rgba );

    public void setLabelBorderWidth( float width );

    public void setLabelText( String text );

    public void setLabelColor( float[] rgba );

    public void setAxisColor( float[] rgba );

    public void setAxisFont( Font font );

    public void setBackgroundColor( float[] rgba );

    public GlimpseLayout getLabelLayout( );

    public BackgroundPainter getBackgroundPainter( );

    public GridPainter getGridPainter( );

    public NumericXYAxisPainter getAxisPainter( );

    public SimpleTextPainter getLabelPainter( );

    public BorderPainter getBorderPainter( );

    public BorderPainter getLabelBorderPainter( );

    public StackedTimePlot2D getStackedTimePlot( );

    public DelegatePainter getDataPainter( );

    public void addPainter( GlimpsePainter painter );

    public void removePainter( GlimpsePainter painter );

    @Override
    public TaggedAxis1D getCommonAxis( );

    @Override
    public TaggedAxis1D getCommonAxis( GlimpseTargetStack stack );
}
