/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.examples;

import static com.metsci.glimpse.gl.util.GLPBufferUtils.*;

import javax.media.opengl.GLContext;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;
import javax.swing.JFrame;

import com.metsci.glimpse.canvas.NewtGlimpseCanvas;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.support.repaint.NEWTRepaintManager;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;

/**
 * Provides static utility methods for initializing a Swing JFrame, adding a GlimpseCanvas,
 * and adding a GlimpseLayout from one of the example classes.
 *
 * @author ulman
 */
public class Example
{
    private NewtGlimpseCanvas canvas;
    private RepaintManager manager;
    private JFrame frame;
    private GlimpseLayout layout;

    public Example( NewtGlimpseCanvas canvas, RepaintManager manager, JFrame frame, GlimpseLayout layout )
    {
        super( );
        this.canvas = canvas;
        this.manager = manager;
        this.frame = frame;
        this.layout = layout;
    }

    public NewtGlimpseCanvas getCanvas( )
    {
        return canvas;
    }

    public RepaintManager getManager( )
    {
        return manager;
    }

    public JFrame getFrame( )
    {
        return frame;
    }

    public GlimpseLayout getLayout( )
    {
        return layout;
    }

    public static Example showWithSwing( GlimpseLayoutProvider layoutProvider, String profile ) throws Exception
    {
        // generate a GLContext by constructing a small offscreen pixel buffer
        final GLOffscreenAutoDrawable pBuffer = createPixelBuffer( 1, 1 );
        final GLContext context = pBuffer.getContext( );
        
        // create a SwingGlimpseCanvas which shares the context
        // other canvases could also be created which all share resources through this context
        final NewtGlimpseCanvas canvas = new NewtGlimpseCanvas( profile, context );

        // create a top level GlimpseLayout which we can add painters and other layouts to
        GlimpseLayout layout = layoutProvider.getLayout( );
        canvas.addLayout( layout );
        
        // set a look and feel on the canvas (this will be applied to all attached layouts and painters)
        // the look and feel affects default colors, fonts, etc...
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        // attach a repaint manager which repaints the canvas in a loop
        final RepaintManager manager = NEWTRepaintManager.newRepaintManager( canvas );

        // create a Swing Frame to contain the GlimpseCanvas
        final JFrame frame = new JFrame( "Glimpse Example" );

        // add a listener which will dispose of canvas resources when the JFrame is closed
        canvas.addDisposeListener( frame, pBuffer );

        // add the GlimpseCanvas to the frame
        frame.add( canvas );

        // make the frame visible
        frame.pack( );
        frame.setSize( 800, 800 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );

        return new Example( canvas, manager, frame, layout );
    }

    public static Example showWithSwing( GlimpseLayoutProvider layoutProvider ) throws Exception
    {
        return showWithSwing( layoutProvider, GLProfile.GL2GL3 );
    }

    public static void showWithSwing( GlimpseLayoutProvider layoutProviderA, GlimpseLayoutProvider layoutProviderB ) throws Exception
    {
        final GLOffscreenAutoDrawable pBuffer = createPixelBuffer( 1, 1 );
        final GLContext context = pBuffer.getContext( );

        NewtGlimpseCanvas leftPanel = new NewtGlimpseCanvas( context );
        leftPanel.addLayout( layoutProviderA.getLayout( ) );

        NewtGlimpseCanvas rightPanel = new NewtGlimpseCanvas( context );
        rightPanel.addLayout( layoutProviderB.getLayout( ) );

        RepaintManager repaintManager = new NEWTRepaintManager( leftPanel.getGLDrawable( ) );
        repaintManager.addGlimpseCanvas( leftPanel );
        repaintManager.addGlimpseCanvas( rightPanel );
        repaintManager.start( );

        JFrame rightFrame = new JFrame( "Glimpse Example (Frame A)" );
        rightPanel.addDisposeListener( rightFrame, pBuffer );
        rightFrame.add( rightPanel );

        rightFrame.pack( );
        rightFrame.setSize( 800, 800 );
        rightFrame.setLocation( 800, 0 );
        rightFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        rightFrame.setVisible( true );

        JFrame leftFrame = new JFrame( "Glimpse Example (Frame B)" );
        leftPanel.addDisposeListener( leftFrame, pBuffer );
        leftFrame.add( leftPanel );

        leftFrame.pack( );
        leftFrame.setSize( 800, 800 );
        leftFrame.setLocation( 0, 0 );
        leftFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        leftFrame.setVisible( true );

        return;

    }
}
