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
package com.metsci.glimpse.examples.heatmap;

import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.colormap.ColorGradients;

/**
 * Demonstrates Glimpse's axis linking capability by creating
 * two independent Glimpse plots and linking their axes so that they move together.
 *
 * @author ulman
 */
public class LinkedTaggedHeatMapExample
{
    public static void main( String[] args ) throws Exception
    {
        final TaggedHeatMapExample example = new TaggedHeatMapExample( );

        final ColorAxisPlot2D layout1 = example.getLayout( ColorGradients.greenBone );
        final ColorAxisPlot2D layout2 = example.getLayout( ColorGradients.jet );

        // create a parent TaggedAxis1D and set it not to link the tags of its children
        TaggedAxis1D parent = new TaggedAxis1D( );
        parent.setLinkTags( false );

        // link the z (color) axis of the two plots
        layout1.getAxisZ( ).setParent( parent, true );
        layout2.getAxisZ( ).setParent( parent, true );

        // link x and y axes of the two plots
        layout1.getAxis( ).setParent( layout2.getAxis( ) );

        Example.showWithSwing( new GlimpseLayoutProvider( )
        {
            @Override
            public GlimpseLayout getLayout( )
            {
                return layout1;
            }

        }, new GlimpseLayoutProvider( )
        {
            @Override
            public GlimpseLayout getLayout( )
            {
                return layout2;
            }
        } );
    }
}
