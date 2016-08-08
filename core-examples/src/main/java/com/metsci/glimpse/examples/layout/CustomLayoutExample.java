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
package com.metsci.glimpse.examples.layout;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.axis.painter.NumericXAxisPainter;
import com.metsci.glimpse.axis.painter.NumericYAxisPainter;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutManagerMig;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;

/**
 * Many Glimpse applications can simply use the plots in the
 * {@link com.metsci.glimpse.plot package}. However, when a custom arrangement
 * of axes and plotting areas is required, low level Glimpse axes and painters
 * can be arranged into any configuration.<p>
 *
 * The following example creates a simple side-by-side plot with two plotting
 * areas each with their own x axis and a common y axis. It demonstrates how
 * to use the OpenGL MIG layout, how to create axes and hook them up to
 * mouse and resize events to enable user interaction, and how to combine
 * everything together into a custom Glimpse plot.
 *
 * @author ulman
 * @see SimpleLayoutExample
 */
public class CustomLayoutExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new CustomLayoutExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        // create a delegate painter which lays out its child painters based on a mig layout
        GlimpseLayout layoutParent = new GlimpseLayout( );

        // create a layout manager
        GlimpseLayoutManagerMig layout = new GlimpseLayoutManagerMig( );

        // attach the mig layout to the layout painter
        layoutParent.setLayoutManager( layout );

        // create GlimpseLayouts for for each mig layout cell and assign a mig layout string to each painter
        layout.setLayoutConstraints( String.format( "bottomtotop, gapx 10, gapy 10, insets 10 10 10 10" ) );

        // add a painter to the canvas which will paint a solid background color over the entire plot
        BackgroundPainter frameBackground = new BackgroundPainter( true );
        layoutParent.addPainter( frameBackground );

        // create a common y axis and separate x axes for the right and left plots
        Axis1D rightAxisX = new Axis1D( );
        Axis1D leftAxisX = new Axis1D( );
        Axis1D axisY = new Axis1D( );

        GlimpseAxisLayout2D rightPlotLayout = new GlimpseAxisLayout2D( layoutParent, new Axis2D( rightAxisX, axisY ) );
        rightPlotLayout.setLayoutData( String.format( "cell 2 0 1 1, push, grow" ) );

        GlimpseAxisLayout2D leftPlotLayout = new GlimpseAxisLayout2D( layoutParent, new Axis2D( leftAxisX, axisY ) );
        leftPlotLayout.setLayoutData( String.format( "cell 1 0 1 1, push, grow" ) );

        GlimpseAxisLayoutY axisYLayout = new GlimpseAxisLayoutY( layoutParent, axisY );
        axisYLayout.setLayoutData( String.format( "cell 0 0 1 1, pushy, growy, width 55px" ) );

        GlimpseAxisLayoutX leftXAxisLayout = new GlimpseAxisLayoutX( layoutParent, leftAxisX );
        leftXAxisLayout.setLayoutData( String.format( "cell 1 1 1 1, pushx, growx, height 45px" ) );

        GlimpseAxisLayoutX rightXAxisLayout = new GlimpseAxisLayoutX( layoutParent, rightAxisX );
        rightXAxisLayout.setLayoutData( String.format( "cell 2 1 1 1, pushx, growx, height 45px" ) );

        // tell the layout parent to lay itself out
        layoutParent.invalidateLayout( );

        // attach mouse listeners to every layout painter which allows mouse interaction
        // this associates this layout painter with the axes which its mouse events should modify
        AxisUtil.attachMouseListener( rightPlotLayout );
        AxisUtil.attachMouseListener( leftPlotLayout );
        AxisUtil.attachHorizontalMouseListener( rightXAxisLayout );
        AxisUtil.attachHorizontalMouseListener( leftXAxisLayout );
        AxisUtil.attachVerticalMouseListener( axisYLayout );

        // create painters to draw labels and tick marks on the three axes
        NumericXAxisPainter rightAxisPainterX = new NumericXAxisPainter( new GridAxisLabelHandler( ) );
        NumericXAxisPainter leftAxisPainterX = new NumericXAxisPainter( new GridAxisLabelHandler( ) );
        NumericYAxisPainter axisPainterY = new NumericYAxisPainter( new GridAxisLabelHandler( ) );

        // allow some extra space between the axis edge and its label
        rightAxisPainterX.setAxisLabelBufferSize( 4 );
        leftAxisPainterX.setAxisLabelBufferSize( 4 );

        // create painters to draw sold backgrounds on the axes
        BackgroundPainter plotBackground = new BackgroundPainter( false );
        rightXAxisLayout.addPainter( plotBackground );
        leftXAxisLayout.addPainter( plotBackground );
        axisYLayout.addPainter( plotBackground );

        // create painters to draw borders around the axes
        rightXAxisLayout.addPainter( new BorderPainter( ) );
        leftXAxisLayout.addPainter( new BorderPainter( ) );
        axisYLayout.addPainter( new BorderPainter( ) );

        // add the axis painters to their associated layout painters (which will determine where on the screen they will draw)
        rightXAxisLayout.addPainter( rightAxisPainterX );
        leftXAxisLayout.addPainter( leftAxisPainterX );
        axisYLayout.addPainter( axisPainterY );

        // create painters to draw sold backgrounds on the right and left plot areas
        rightPlotLayout.addPainter( plotBackground );
        leftPlotLayout.addPainter( plotBackground );

        // create painters to draw borders around the right and left plot areas
        rightPlotLayout.addPainter( new BorderPainter( ) );
        leftPlotLayout.addPainter( new BorderPainter( ) );

        // create painters to draw grid lines on the right and left plot areas
        rightPlotLayout.addPainter( new GridPainter( ) );
        leftPlotLayout.addPainter( new GridPainter( ) );

        return layoutParent;
    }
}
