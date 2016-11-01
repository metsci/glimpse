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
package com.metsci.glimpse.examples.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;

/**
 * Tests the capability of SwingGlimpseCanvas to move between JFrames.
 * This capability can be important when using Glimpse with docking
 * frameworks.
 *
 * @author borkholder
 */
public class SwingMoveCanvasTest
{
    public static void main( String[] args ) throws Exception
    {
        // create a canvas and a plot
        final NewtSwingGlimpseCanvas canvas = new NewtSwingGlimpseCanvas( );
        GlimpseLayout plot = new HeatMapExample( ).getLayout( );
        canvas.addLayout( plot );
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        // attach a repaint manager which repaints the canvas in a loop
        new FPSAnimator( canvas.getGLDrawable( ), 120 ).start( );

        // create two frames
        final JFrame frame = makeFrame( 0, 0, 800, 800 );
        final JFrame frame2 = makeFrame( 800, 0, 800, 800 );

        // add the GlimpseCanvas to one of the frames
        // this must be done on the Swing EDT to avoid JOGL crashes
        // when removing the canvas from the frame
        SwingUtilities.invokeAndWait( new Runnable( )
        {
            @Override
            public void run( )
            {
                frame.add( canvas );
            }
        } );

        // periodically switch the canvas between the frames
        // Swing actions must be called on the EventDispatch thread
        new Timer( 1000, new ActionListener( )
        {
            boolean toc = true;

            @Override
            public void actionPerformed( ActionEvent e )
            {
                if ( toc )
                {
                    frame.remove( canvas );
                    frame2.add( canvas );

                }
                else
                {
                    frame2.remove( canvas );
                    frame.add( canvas );
                }

                frame2.validate( );
                frame.validate( );

                toc = !toc;
            }
        } ).start( );
    }

    public static JFrame makeFrame( int x, int y, int width, int height )
    {
        JFrame frame = new JFrame( "Glimpse Example (Swing)" );

        frame.pack( );
        frame.setSize( width, height );
        frame.setLocation( x, y );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );

        return frame;
    }
}
