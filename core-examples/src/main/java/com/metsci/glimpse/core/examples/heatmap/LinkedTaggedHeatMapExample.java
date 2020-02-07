/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.core.examples.heatmap;

import static com.jogamp.opengl.GLProfile.GL3bc;
import static com.metsci.glimpse.core.support.QuickUtils.initGlimpseOrExitJvm;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseCanvas;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseWindow;
import static com.metsci.glimpse.core.support.QuickUtils.swingInvokeLater;

import javax.swing.JFrame;

import com.jogamp.opengl.GLProfile;
import com.metsci.glimpse.core.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.core.plot.ColorAxisPlot2D;
import com.metsci.glimpse.core.support.colormap.ColorGradients;

/**
 * Demonstrates Glimpse's axis linking capability by creating
 * two independent Glimpse plots and linking their axes so that they move together.
 *
 * @author ulman
 */
public class LinkedTaggedHeatMapExample
{
    public static void main( String[] args )
    {
        swingInvokeLater( ( ) ->
        {
            // create two heat map plots
            ColorAxisPlot2D leftPlot = TaggedHeatMapExample.newPlot( ColorGradients.reverseBone );
            ColorAxisPlot2D rightPlot = TaggedHeatMapExample.newPlot( ColorGradients.jet );

            // create a parent TaggedAxis1D and set it not to link the tags of its children
            TaggedAxis1D parent = new TaggedAxis1D( );
            parent.setLinkTags( false );

            // link the z (color) axis of the two plots
            leftPlot.getAxisZ( ).setParent( parent, true );
            rightPlot.getAxisZ( ).setParent( parent, true );

            // link x and y axes of the two plots
            leftPlot.getAxis( ).setParent( rightPlot.getAxis( ) );

            // create windows and show the plots
            String appName = "Linked Heat Map Example";
            GLProfile glProfile = initGlimpseOrExitJvm( appName, GL3bc );
            JFrame leftFrame = quickGlimpseWindow( appName, quickGlimpseCanvas( glProfile, leftPlot ) );
            JFrame rightFrame = quickGlimpseWindow( appName, quickGlimpseCanvas( glProfile, rightPlot ) );

            // place the windows side by side
            rightFrame.setLocation( 800, 0 );
            leftFrame.setLocation( 0, 0 );
        } );
    }
}
