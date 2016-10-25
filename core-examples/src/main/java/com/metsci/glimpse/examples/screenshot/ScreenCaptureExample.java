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
package com.metsci.glimpse.examples.screenshot;

import static com.metsci.glimpse.context.TargetStackUtil.newTargetStack;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.metsci.glimpse.axis.factory.AxisFactory2D;
import com.metsci.glimpse.axis.factory.ConditionalEndsWithAxisFactory2D;
import com.metsci.glimpse.axis.factory.FixedAxisFactory2D;
import com.metsci.glimpse.canvas.FBOGlimpseCanvas;
import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseAdapter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.font.FontUtils;

/**
 * Demonstrates the ability to render Glimpse plots to an off-screen buffer
 * and save the buffer to a screenshot.
 *
 * @author ulman
 */
public class ScreenCaptureExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        // create a normal onscreen GlimpseCanvas
        Example example = Example.showWithSwing( new ScreenCaptureExample( ) );

        setupScreenCapture( example );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        return new HeatMapExample( ).getLayout( );
    }

    public static void setupScreenCapture( Example example )
    {
        GlimpseCanvas canvas = example.getCanvas( );

        ColorAxisPlot2D plot = ( ColorAxisPlot2D ) example.getLayout( );
        plot.setTitleFont( FontUtils.getDefaultBold( 18 ) );
        plot.setTitle( "Click Center Mouse Button To Take Screenshot" );

        // create an offscreen GlimpseCanvas
        final FBOGlimpseCanvas offscreenCanvas = new FBOGlimpseCanvas( canvas.getGLContext( ), 1000, 1000 );

        // add the GlimpseLayout from the onscreen canvas to the offscreen canvas as well
        // (GlimpseLayouts can have multiple parents)
        offscreenCanvas.addLayout( example.getLayout( ) );

        // if we want the axes used for the screenshot to behave differently than the axes
        // used to render the plot on the screen we must create an AxisFactory which
        // specifies how the axes for the ColorAxisPlot2D are duplicated when they are
        // used in different context (in this case we have two contexts: drawing to the
        // SWT or Swing window and drawing to the offscreen buffer).
        //
        // The ConditionalAxisFactory2D class uses a different axis factory for different contexts.
        // Here, we use a FixedAxisFactory which fixes the axis bound to 0 to 1000 when the axes
        // are used in any context starting with screenshot.getGlimpseCanvas( ).
        //
        // these three lines could simply be removed and things would work, however the screenshot
        // axes would then be linked with the onscreen axes.
        GlimpseTargetStack stack = newTargetStack( offscreenCanvas );
        AxisFactory2D factory = new FixedAxisFactory2D( 0, 1000, 0, 1000 );
        plot.setAxisFactory( new ConditionalEndsWithAxisFactory2D( stack, factory ) );

        // add a mouse listener which takes a screenshot whenever the mouse wheel button is pressed
        plot.addGlimpseMouseListener( new GlimpseMouseAdapter( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                if ( event.isButtonDown( MouseButton.Button2 ) )
                {
                    try
                    {
                        BufferedImage image = offscreenCanvas.toBufferedImage( );
                        ImageIO.write( image, "PNG", new File( "ScreenCaptureExample.png" ) );
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace( );
                    }
                }
            }
        } );
    }
}
