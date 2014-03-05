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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;

/**
 * Provides static utility methods for initializing a Swing JFrame, adding a GlimpseCanvas,
 * and adding a GlimpseLayout from one of the example classes.
 *
 * @author ulman
 */
public class Example
{
    private NewtSwingGlimpseCanvas canvas;
    private JFrame frame;
    private GlimpseLayout layout;

    public Example( NewtSwingGlimpseCanvas canvas, JFrame frame, GlimpseLayout layout )
    {
        super( );
        this.canvas = canvas;
        this.frame = frame;
        this.layout = layout;
    }

    public NewtSwingGlimpseCanvas getCanvas( )
    {
        return canvas;
    }

    public JFrame getFrame( )
    {
        return frame;
    }

    public GlimpseLayout getLayout( )
    {
        return layout;
    }

    public static Example showWithSwing( GlimpseLayoutProvider layoutProvider, String profileString ) throws Exception
    {
        // generate a GLContext by constructing a small offscreen framebuffer
        final GLOffscreenAutoDrawable glDrawable = GLUtils.newOffscreenDrawable( profileString );

        GLContext context = glDrawable.getContext( );

        // create a SwingGlimpseCanvas which shares the context
        // other canvases could also be created which all share resources through this context
        final NewtSwingGlimpseCanvas canvas = new NewtSwingGlimpseCanvas( profileString, context );

        // create a top level GlimpseLayout which we can add painters and other layouts to
        GlimpseLayout layout = layoutProvider.getLayout( );
        canvas.addLayout( layout );

        // set a look and feel on the canvas (this will be applied to all attached layouts and painters)
        // the look and feel affects default colors, fonts, etc...
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        // attach a repaint manager which repaints the canvas in a loop
        new FPSAnimator( canvas.getGLDrawable( ), 120 ).start( );

        // create a Swing Frame to contain the GlimpseCanvas
        final JFrame frame = new JFrame( "Glimpse Example" );

        // This listener is added before adding the SwingGlimpseCanvas to the frame because
        // NEWTGLCanvas adds its own WindowListener and this WindowListener should reveive the WindowEvent first
        // (although I'm now not sure how much this matters)
        frame.addWindowListener( new WindowAdapter( )
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                glDrawable.destroy( );

                // Removing the canvas from the frame may prevent X11 errors (see http://tinyurl.com/m4rnuvf)
                // However, it also seems to make SIGSEGV error occur more frequently
                // frame.remove( canvas );

                canvas.dispose( );
            }
        } );

        // add the GlimpseCanvas to the frame
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

        // make the frame visible
        frame.pack( );
        frame.setSize( 800, 800 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );

        return new Example( canvas, frame, layout );
    }

    public static Example showWithSwing( GlimpseLayoutProvider layoutProvider ) throws Exception
    {
        return showWithSwing( layoutProvider, GLProfile.GL2GL3 );
    }

    public static void showWithSwing( GlimpseLayoutProvider layoutProviderA, GlimpseLayoutProvider layoutProviderB ) throws Exception
    {
        // generate a GLContext by constructing a small offscreen framebuffer
        GLProfile glProfile = GLProfile.get( GLProfile.GL2GL3 );
        GLDrawableFactory factory = GLDrawableFactory.getFactory( glProfile );
        GLCapabilities glCapabilities = new GLCapabilities( glProfile );
        final GLOffscreenAutoDrawable glDrawable = factory.createOffscreenAutoDrawable( null, glCapabilities, null, 1, 1 );

        // trigger GLContext creation
        glDrawable.display( );
        GLContext context = glDrawable.getContext( );

        final NewtSwingGlimpseCanvas leftPanel = new NewtSwingGlimpseCanvas( context );
        leftPanel.addLayout( layoutProviderA.getLayout( ) );

        final NewtSwingGlimpseCanvas rightPanel = new NewtSwingGlimpseCanvas( context );
        rightPanel.addLayout( layoutProviderB.getLayout( ) );

        FPSAnimator animator = new FPSAnimator( 120 );
        animator.add( leftPanel.getGLDrawable( ) );
        animator.add( rightPanel.getGLDrawable( ) );
        animator.start( );

        WindowAdapter disposeListener = new WindowAdapter( )
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                glDrawable.destroy( );

                // Removing the canvas from the frame may prevent X11 errors (see http://tinyurl.com/m4rnuvf)
                // However, it also seems to make SIGSEGV error occur more frequently
                // frame.remove( canvas );

                leftPanel.dispose( );
                rightPanel.dispose( );
            }
        };

        JFrame rightFrame = new JFrame( "Glimpse Example (Frame A)" );
        rightFrame.addWindowListener( disposeListener );
        rightFrame.add( rightPanel );

        rightFrame.pack( );
        rightFrame.setSize( 800, 800 );
        rightFrame.setLocation( 800, 0 );
        rightFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        rightFrame.setVisible( true );

        JFrame leftFrame = new JFrame( "Glimpse Example (Frame B)" );
        leftFrame.addWindowListener( disposeListener );
        leftFrame.add( leftPanel );

        leftFrame.pack( );
        leftFrame.setSize( 800, 800 );
        leftFrame.setLocation( 0, 0 );
        leftFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        leftFrame.setVisible( true );

        return;

    }
}
