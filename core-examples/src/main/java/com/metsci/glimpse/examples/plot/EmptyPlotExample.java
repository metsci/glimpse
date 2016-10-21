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
package com.metsci.glimpse.examples.plot;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * Demonstrates construction of a plot from scratch, starting with
 * an EmptyFrame and adding custom painters to create a basic plot
 * with no data.
 *
 * This approach is a good one if the default painters that the
 * other frames in the com.metsci.glimpse.frame.swt package have
 * unwanted painters.
 *
 * @author ulman
 */
public class EmptyPlotExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new EmptyPlotExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        Plot2D plot = new Plot2D( "plot" );

        GlimpseLayout plotLayout = plot.getLayoutCenter( );

        // add a painter to paint a solid dark background on the plot
        plotLayout.addPainter( new BackgroundPainter( false ) );

        // add a painter to display grid lines
        GridPainter gridPainter = new GridPainter( plot.getLabelHandlerX( ), plot.getLabelHandlerY( ) );
        plotLayout.addPainter( gridPainter );

        // add a painter to display mouse selection crosshairs
        CrosshairPainter crosshairPainter = new CrosshairPainter( );
        crosshairPainter.setCursorColor( GlimpseColor.getBlack( ) );
        plotLayout.addPainter( crosshairPainter );

        // add a painter to paint a simple line border on the plot
        plotLayout.addPainter( new BorderPainter( ).setColor( GlimpseColor.getBlack( ) ) );

        // add axis and plot labels
        plot.setAxisLabelX( "Axis X" );
        plot.setAxisLabelY( "Axis Y" );
        plot.setTitle( "Plot Title" );

        return plot;
    }
}
