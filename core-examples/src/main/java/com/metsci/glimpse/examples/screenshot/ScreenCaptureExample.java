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

import static com.metsci.glimpse.context.TargetStackUtil.newTargetStack;
import static com.metsci.glimpse.gl.util.GLPBufferUtils.createPixelBuffer;

import java.io.File;

import javax.media.opengl.GLContext;
import javax.swing.JFrame;

import com.metsci.glimpse.axis.factory.AxisFactory2D;
import com.metsci.glimpse.axis.factory.ConditionalAxisFactory2D;
import com.metsci.glimpse.axis.factory.FixedAxisFactory2D;
import com.metsci.glimpse.canvas.SwingGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.basic.HeatMapExample;
import com.metsci.glimpse.gl.Jogular;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.repaint.SwingRepaintManager;
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
        Jogular.initJogl( );

        GLContext context = createPixelBuffer( 1, 1 ).getContext( );
        final SwingGlimpseCanvas canvas = new SwingGlimpseCanvas( true, context );

        canvas.addLayout( new ScreenCaptureExample( ).getLayout( context ) );
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        final RepaintManager manager = SwingRepaintManager.newRepaintManager( canvas );

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
