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
package com.metsci.glimpse.examples.timeline;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.stacked.StackedPlot2D;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;

/**
 * @author ulman
 */
public class StackedPlotExample implements GlimpseLayoutProvider
{

    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new StackedPlotExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        // create a plot which arranges its sub-plots in a horizontal line
        StackedPlot2D plot = new StackedPlot2D( Orientation.HORIZONTAL );

        // use the HeatMapExample to create a ColorAxisPlot2D
        ColorAxisPlot2D plot1 = new HeatMapExample( ).getLayout( );
        createPlotArea( plot, plot1, "heat map 1" );

        // create another ColorAxisPlot2D
        ColorAxisPlot2D plot2 = new HeatMapExample( ).getLayout( );
        PlotInfo info2 = createPlotArea( plot, plot2, "heat map 2" );

        // set the size of the second layout area to be fixed at 300 pixels
        info2.setSize( 250 );

        return plot;
    }

    public PlotInfo createPlotArea( StackedPlot2D stacked_plot, ColorAxisPlot2D inner_plot, String name )
    {
        // customize the appearance of the inner plot
        // (remove the title and x axis, shrink the y axis to 30 pixels
        // and don't display the y axis label)
        inner_plot.setTitleHeight( 0 );
        inner_plot.setAxisSizeX( 0 );
        inner_plot.setAxisSizeY( 40 );
        inner_plot.setAxisLabelY( "" );

        // create a layout area for the ColorAxisPlot2D
        PlotInfo info = stacked_plot.createPlot( name );

        // get the axis2D associated with this new plot area
        Axis2D axis1 = info.getLayout( ).getAxis( );

        // set the axis bounds and lock the aspect ratio of he axis
        axis1.set( 0, 1000, 0, 1000 );
        axis1.lockAspectRatioXY( 1.0 );

        // add the inner plot to the newly created layout area in the stacked plot
        info.addLayout( inner_plot );

        return info;
    }

}
