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
package com.metsci.glimpse.plot;

import com.metsci.glimpse.axis.painter.ColorYAxisPainter;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.gl.texture.ColorTexture1D;

/**
 * A {@link SimplePlot2D} with a right-side axis with a color scale in addition to
 * x and y axes and a central plotting area.
 *
 * @author ulman
 *
 */
public class ColorAxisPlot2D extends SimplePlot2D
{
    public ColorAxisPlot2D( )
    {
        // show the Z axis
        setAxisSizeZ( 65 );
    }

    @Override
    protected void initializePainters( )
    {
        super.initializePainters( );
    }

    @Override
    protected NumericAxisPainter createAxisPainterZ( AxisLabelHandler tickHandler )
    {
        return new ColorYAxisPainter( tickHandler );
    }

    public void setColorScale( ColorTexture1D colorScale )
    {
        ( ( ColorYAxisPainter ) getAxisPainterZ( ) ).setColorScale( colorScale );
    }

    @Override
    public String toString( )
    {
        return ColorAxisPlot2D.class.getSimpleName( );
    }
}
