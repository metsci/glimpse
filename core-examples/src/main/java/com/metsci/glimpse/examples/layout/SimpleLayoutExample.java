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

import com.metsci.glimpse.axis.UpdateMode;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutManagerMig;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.SimplePlot2D;

/**
 * @author ulman
 * @see CustomLayoutExample
 */
public class SimpleLayoutExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new SimpleLayoutExample( ) );
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
        layout.setLayoutConstraints( String.format( "bottomtotop, gapx 10, gapy 10, insets 0 15 15 40" ) );

        // add a painter to the canvas which will paint a solid background color over the entire plot
        layoutParent.addPainter( new BackgroundPainter( true ) );

        GlimpseLayout rightPlotLayout = new GlimpseLayout( layoutParent );
        rightPlotLayout.setLayoutData( String.format( "cell 1 0 1 1, push, grow" ) );

        GlimpseLayout leftPlotLayout = new GlimpseLayout( layoutParent );
        leftPlotLayout.setLayoutData( String.format( "cell 0 0 1 1, push, grow" ) );

        // create two pre-configured plots
        SimplePlot2D rightPlot = new SimplePlot2D( );
        rightPlot.setTitle( "Right Plot" );
        rightPlot.setAxisLabelX( "Right Axis X" );
        rightPlot.setAxisLabelY( "Right Axis Y" );
        rightPlot.setAxisSizeY( 50 );
        rightPlot.setTitleHeight( 40 );
        rightPlot.setBorderSize( 0 );
        rightPlot.setTickSpacingX( 40 );
        rightPlot.setTickSpacingY( 40 );
        rightPlot.getAxisX( ).setUpdateMode( UpdateMode.MinScale );
        rightPlot.getAxisY( ).setUpdateMode( UpdateMode.MinScale );
        rightPlot.setMinX( 0.0 );
        rightPlot.setMinY( 0.0 );
        rightPlot.lockAspectRatioXY( 1.0 );

        SimplePlot2D leftPlot = new SimplePlot2D( );
        leftPlot.setTitle( "Left Plot" );
        leftPlot.setAxisLabelX( "Left Axis X" );
        leftPlot.setAxisLabelY( "Left Axis Y" );
        leftPlot.setAxisSizeY( 50 );
        leftPlot.setTitleHeight( 40 );
        leftPlot.setBorderSize( 0 );
        leftPlot.setTickSpacingX( 40 );
        leftPlot.setTickSpacingY( 40 );
        leftPlot.getAxisX( ).setUpdateMode( UpdateMode.MinScale );
        leftPlot.getAxisY( ).setUpdateMode( UpdateMode.MinScale );
        leftPlot.setMinX( 0.0 );
        leftPlot.setMinY( 0.0 );
        leftPlot.lockAspectRatioXY( 1.0 );

        // add the plots to their respective layouts
        rightPlotLayout.addLayout( rightPlot );
        leftPlotLayout.addLayout( leftPlot );

        return layoutParent;
    }
}
