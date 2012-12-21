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

import javax.media.opengl.GLContext;
import javax.swing.JFrame;

import com.metsci.glimpse.canvas.SwingGlimpseCanvas;
import com.metsci.glimpse.gl.Jogular;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;

import static com.metsci.glimpse.gl.util.GLPBufferUtils.*;

/**
 * Provides static utility methods for initializing a Swing JFrame, adding a GlimpseCanvas,
 * and adding a GlimpseLayout from one of the example classes.
 *
 * @author ulman
 */
public class Example
{
    private SwingGlimpseCanvas canvas;
    private RepaintManager manager;
    private JFrame frame;
    
    public Example( SwingGlimpseCanvas canvas, RepaintManager manager, JFrame frame )
    {
        super( );
        this.canvas = canvas;
        this.manager = manager;
        this.frame = frame;
    }
    
    public SwingGlimpseCanvas getCanvas( )
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

    public static Example showWithSwing( GlimpseLayoutProvider layoutProvider ) throws Exception
    {
        Jogular.initJogl( );

        GLContext context = createPixelBuffer( 1, 1 ).getContext( );
        final SwingGlimpseCanvas canvas = new SwingGlimpseCanvas( true, context );

        canvas.addLayout( layoutProvider.getLayout( ) );
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        final RepaintManager manager = RepaintManager.newRepaintManager( canvas );

        JFrame frame = new JFrame( "Glimpse Example" );
        frame.add( canvas );

        frame.pack( );
        frame.setSize( 800, 800 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );

        Runtime.getRuntime( ).addShutdownHook( new Thread( )
        {
            @Override
            public void run( )
            {
                canvas.dispose( manager );
            }
        } );

        return new Example( canvas, manager, frame );
    }

    public static void showWithSwing( GlimpseLayoutProvider layoutProviderA, GlimpseLayoutProvider layoutProviderB ) throws Exception
    {
        Jogular.initJogl( );

        SwingGlimpseCanvas leftPanel = new SwingGlimpseCanvas( true );
        leftPanel.addLayout( layoutProviderA.getLayout( ) );

        SwingGlimpseCanvas rightPanel = new SwingGlimpseCanvas( true, leftPanel.getGLContext( ) );
        rightPanel.addLayout( layoutProviderB.getLayout( ) );

        RepaintManager repaintManager = new RepaintManager( );
        repaintManager.addGlimpseCanvas( leftPanel );
        repaintManager.addGlimpseCanvas( rightPanel );
        repaintManager.start( );

        JFrame rightFrame = new JFrame( "Glimpse Example (Frame A)" );
        rightFrame.add( rightPanel );

        rightFrame.pack( );
        rightFrame.setSize( 800, 800 );
        rightFrame.setLocation( 800, 0 );
        rightFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        rightFrame.setVisible( true );

        JFrame leftFrame = new JFrame( "Glimpse Example (Frame B)" );
        leftFrame.add( leftPanel );

        leftFrame.pack( );
        leftFrame.setSize( 800, 800 );
        leftFrame.setLocation( 0, 0 );
        leftFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        leftFrame.setVisible( true );

        return;

    }
}
