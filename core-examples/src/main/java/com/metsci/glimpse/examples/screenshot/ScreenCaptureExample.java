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
package com.metsci.glimpse.examples.screenshot;

import static com.metsci.glimpse.context.TargetStackUtil.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.axis.factory.AxisFactory2D;
import com.metsci.glimpse.axis.factory.ConditionalAxisFactory2D;
import com.metsci.glimpse.axis.factory.FixedAxisFactory2D;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.basic.HeatMapExample;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.screenshot.ScreenshotUtil;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;

/**
 * Demonstrates the ability to render Glimpse plots to an off-screen buffer
 * and save the buffer to a screenshot.
 *
 * @author ulman
 */
public class ScreenCaptureExample
{
    public static void main( String[] args ) throws Exception
    {
        // generate a GLContext by constructing a small offscreen framebuffer
        GLProfile glProfile = GLProfile.get( GLProfile.GL2GL3 );
        GLDrawableFactory factory = GLDrawableFactory.getFactory( glProfile );
        GLCapabilities glCapabilities = new GLCapabilities( glProfile );
        final GLOffscreenAutoDrawable glDrawable = factory.createOffscreenAutoDrawable( null, glCapabilities, null, 1, 1 );

        // trigger GLContext creation
        glDrawable.display( );
        GLContext context = glDrawable.getContext( );

        // create a SwingGlimpseCanvas which shares the context
        // other canvases could also be created which all share resources through this context
        final NewtSwingGlimpseCanvas canvas = new NewtSwingGlimpseCanvas( GLProfile.GL2GL3, context );

        // create a top level GlimpseLayout which we can add painters and other layouts to
        GlimpseLayout layout = new ScreenCaptureExample( ).getLayout( context );
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
        frame.add( canvas );

        // make the frame visible
        frame.pack( );
        frame.setSize( 800, 800 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );
    }

    public ColorAxisPlot2D getLayout( GLContext context )
    {
        // instantiate another example to take a screenshot of
        HeatMapExample heatMapExample = new HeatMapExample( );
        final ColorAxisPlot2D heatMapPlot = heatMapExample.getLayout( );

        heatMapPlot.setTitleFont( FontUtils.getDefaultBold( 18 ) );
        heatMapPlot.setTitle( "Click Center Mouse Button To Take Screenshot" );

        // create a screenshot utility instance using the GL context for the example
        // (which will be shared with the onscreen canvas, this is important for the
        // heat map texture to appear correctly in the screenshot)
        final ScreenshotUtil screenshot = new ScreenshotUtil( context );

        // if we want the axes used for the screenshot to behave differently than the axes
        // used to render the plot on the screen we must create an AxisFactory which
        // specifies how the axes for the ColorAxisPlot2D are duplicated when they are
        // used in different context (in this case we have two contexts: drawing to the
        // SWT or Swing window and drawing to the offscreen buffer used by the ScreenshotUtil
        // class, obtained by screenshot.getGlimpseCanvas( ) ).
        //
        // The ConditionalAxisFactory2D class uses a different axis factory for different contexts.
        // Here, we use a FixedAxisFactory which fixes the axis bound to 0 to 1000 when the axes
        // are used in any context starting with screenshot.getGlimpseCanvas( )
        //
        // these three lines could simply be removed and things would work, however the screenshot
        // axes would then be linked with the onscreen axes and the heat map plot would appear
        // very small in the center of the screenshot because its canvas is so much larger in pixel space
        GlimpseTargetStack stack = newTargetStack( screenshot.getGlimpseCanvas( ) );
        AxisFactory2D factory = new FixedAxisFactory2D( 0, 1000, 0, 1000 );
        heatMapPlot.setAxisFactory( new ConditionalAxisFactory2D( stack, factory ) );

        // add a mouse listener which takes a screenshot whenever the mouse wheel button is pressed
        heatMapPlot.addGlimpseMouseListener( new GlimpseMouseListener( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                if ( event.isButtonDown( MouseButton.Button2 ) )
                {
                    try
                    {
                        // create a screenshot file named "ScreenCaptureExample.png" in the current working directory
                        // containing the heat map plot rendered to a 3000 by 3000 pixel canvas
                        File screenshotFile = new File( "ScreenCaptureExample.png" );
                        screenshot.captureScreenshot( heatMapPlot, screenshotFile, 3000, 3000 );
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace( );
                    }
                }
            }

            @Override
            public void mouseEntered( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseExited( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
            }
        } );

        return heatMapPlot;
    }
}
