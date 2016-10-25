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

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.heatmap.TaggedHeatMapExample;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.info.MinimapLayout;
import com.metsci.glimpse.plot.ColorAxisPlot2D;

/**
 * Demonstrates use of MinimapLayout to create a miniature navigation
 * area for any plot. This navigation area can be used to re-center the
 * main plot and displays a selection box indicating the axis bounds
 * of the main plot.
 *
 * @author ulman
 */
public class MiniMapLayoutExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new MiniMapLayoutExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        // create a plot using the HeatMapExample
        TaggedHeatMapExample example = new TaggedHeatMapExample( );
        ColorAxisPlot2D plot = example.getLayout( );

        // turn off the selection painter
        plot.getCrosshairPainter( ).showSelectionBox( false );

        // create a MinimapLayout
        MinimapLayout mini = new MinimapLayout( );

        // add the MinimapLayout to the center Layout of the ColorAxisPlot2D
        plot.getLayoutCenter( ).addLayout( mini );

        // set the axis bounds of the Minimap
        // (the Minimap will always be fixed at 0 to 1000 regardless of how
        //  the main plot is zoomed)
        mini.setBounds( 0, 1000, 0, 1000 );

        // set the position and size of the Minimap in pixels
        // within the ColorAxisPlot2D
        mini.setPosition( 10, 10, 100, 100 );

        // add the painter to display in the Minimap
        // in this case we reuse the painter displaying the heat map
        // in the main ColorAxisPlot2D
        mini.addPainter( example.getPainter( ) );

        return plot;
    }
}
