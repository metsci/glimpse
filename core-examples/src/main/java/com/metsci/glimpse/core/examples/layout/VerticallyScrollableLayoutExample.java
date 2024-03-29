/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.examples.layout;

import static com.jogamp.opengl.GLProfile.GL3;
import static com.metsci.glimpse.core.layout.GlimpseVerticallyScrollableLayout.attachScrollableToScrollbar;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseCanvas;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseWindow;
import static com.metsci.glimpse.core.support.QuickUtils.swingInvokeLater;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.core.context.GlimpseTargetStack;
import com.metsci.glimpse.core.context.TargetStackUtil;
import com.metsci.glimpse.core.examples.timeline.CollapsibleTimelinePlotExample;
import com.metsci.glimpse.core.layout.GlimpseVerticallyScrollableLayout;
import com.metsci.glimpse.core.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.core.support.settings.OceanLookAndFeel;
import com.metsci.glimpse.core.support.swing.NewtSwingEDTGlimpseCanvas;

public class VerticallyScrollableLayoutExample
{
    public static void main( String[] args )
    {
        swingInvokeLater( ( ) ->
        {
            // Don't attempt to shrink content to any smaller than this height -- if canvas height is
            // less than this, make it scrollable instead of shrinking it further
            final int minContentHeight = 800;

            // Make a scroller and add some content to it
            final GlimpseVerticallyScrollableLayout scroller = new GlimpseVerticallyScrollableLayout( minContentHeight );
            scroller.addPainter( new BackgroundPainter( ) );
            scroller.addLayout( new CollapsibleTimelinePlotExample( ).getPlot( ) );

            // Swing scrollbar, for interactively controlling the scroller's vertical offset
            final JScrollBar scrollbar = new JScrollBar( );

            NewtSwingEDTGlimpseCanvas canvas = quickGlimpseCanvas( GL3, scroller, new OceanLookAndFeel( ) );
            JFrame frame = quickGlimpseWindow( "Vertically Scrollable Example", canvas, 800, 600 );

            // perform some additional setup of the scroll bar
            SwingUtilities.invokeLater( ( ) ->
            {
                // Attach the scrollable-layout and the scrollbar
                //
                // Really the scrollbar is attached not to the layout, but to a particular (layout,stack)
                // tuple -- so the stack must be specified as well
                //
                GlimpseTargetStack scrollerStack = TargetStackUtil.newTargetStack( canvas );
                attachScrollableToScrollbar( scroller, scrollerStack, scrollbar );

                // Add the scrollbar to the frame on the right
                frame.setLayout( new BorderLayout( ) );
                frame.add( scrollbar, BorderLayout.EAST );
                // Re-add the canvas to the center of the border layout
                frame.add( canvas, BorderLayout.CENTER );
                frame.revalidate( );
            } );
        } );
    }
}
