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

import static com.metsci.glimpse.layout.GlimpseVerticallyScrollableLayout.attachScrollableToScrollbar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.context.TargetStackUtil;
import com.metsci.glimpse.examples.timeline.CollapsibleTimelinePlotExample;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseVerticallyScrollableLayout;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.support.settings.OceanLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class VerticallyScrollableLayoutExample
{

    public static void main( String[] args ) throws Exception
    {

        // Much of the standard glimpse-canvas setup code here is copied from
        // com.metsci.glimpse.examples.Example, with modifications that allow
        // the canvas and the scrollbar to live in the same JFrame

        // Don't attempt to shrink content to any smaller than this height -- if canvas height is
        // less than this, make it scrollable instead of shrinking it further
        final int minContentHeight = 800;

        // Make a scroller and add some content to it
        final GlimpseVerticallyScrollableLayout scroller = new GlimpseVerticallyScrollableLayout( minContentHeight );
        scroller.addPainter( new BackgroundPainter( ) );
        scroller.addLayout( ( new CollapsibleTimelinePlotExample( ) ).getLayout( ) );

        // Add the scroller to a glimpse canvas (see com.metsci.glimpse.examples.Example for explanatory comments)
        GLOffscreenAutoDrawable glDrawable = GLUtils.newOffscreenDrawable( GLUtils.getDefaultGLProfile( ) );
        GLContext context = glDrawable.getContext( );
        final NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( context );
        canvas.setPreferredSize( new Dimension( 800, 600 ) );
        canvas.addLayout( scroller );
        canvas.setLookAndFeel( new OceanLookAndFeel( ) );

        // Swing scrollbar, for interactively controlling the scroller's vertical offset
        final JScrollBar scrollbar = new JScrollBar( );

        // Attach the scrollable-layout and the scrollbar
        //
        // Really the scrollbar is attached not to the layout, but to a particular (layout,stack)
        // tuple -- so the stack must be specified as well
        //
        GlimpseTargetStack scrollerStack = TargetStackUtil.newTargetStack( canvas );
        attachScrollableToScrollbar( scroller, scrollerStack, scrollbar );

        // Create a frame to house the canvas and the scrollbar (see com.metsci.glimpse.examples.Example for explanatory comments)
        final JFrame frame = new JFrame( "Glimpse Example" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        frame.addWindowListener( new WindowAdapter( )
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                canvas.disposeAttached( );
                canvas.destroy( );
            }
        } );

        // Make the frame visible (see com.metsci.glimpse.examples.Example for explanatory comments)
        SwingUtilities.invokeAndWait( new Runnable( )
        {
            @Override
            public void run( )
            {
                // Add canvas and scrollbar to the frame
                frame.setLayout( new BorderLayout( ) );
                frame.add( canvas, BorderLayout.CENTER );
                frame.add( scrollbar, BorderLayout.EAST );

                frame.pack( );
                frame.setLocationRelativeTo( null );
                frame.setVisible( true );

                GLAnimatorControl animator = new SwingEDTAnimator( 60 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );
            }
        } );

    }

}
