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

import com.metsci.glimpse.event.mouse.GlimpseMouseAdapter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * Demonstrates using a customized GlimpseLayout to simulate a simple button.
 *
 * @author ulman
 */
public class ButtonExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new ButtonExample( ) );
    }

    @Override
    public SimplePlot2D getLayout( )
    {
        // use the HeatMapExample as a base, then add a button to it
        HeatMapExample heatMapExample = new HeatMapExample( );
        ColorAxisPlot2D plot = heatMapExample.getLayout( );
        plot.getCrosshairPainter( ).setVisible( false );
        heatMapExample.getCursorPainter( ).setVisible( false );

        // create a new GlimpseLayout (which will be our button)
        GlimpseLayout button = new GlimpseLayout( );

        // create a BackgroundPainter to draw a solid color background on the button and add it to the GlimpseLayout
        final BackgroundPainter background = new BackgroundPainter( ).setColor( GlimpseColor.fromColorRgb( 0.4f, 0.4f, 0.4f ) );
        button.addPainter( background );

        // create a SimpleTextPainter to paint a simple centered text string and add it to the GlimpseLayout
        button.addPainter( new SimpleTextPainter( ).setText( "Button" ).setHorizontalPosition( HorizontalPosition.Center ).setVerticalPosition( VerticalPosition.Center ) );

        // create a BorderPainter to draw a solid color border around the GlimpseLayout
        button.addPainter( new BorderPainter( ).setColor( GlimpseColor.fromColorRgb( 0.2f, 0.2f, 0.2f ) ).setLineWidth( 3.0f ) );

        // set the MIG Layout constraints for the GlimpseLayout (see http://migcalendar.com/miglayout/cheatsheet.html for
        // a great MIG Layout guide). These constraints will position the GlimpseLayout button floating in the upper
        // right hand corder of the plot.
        button.setLayoutData( "pos (container.w-60) (container.h-30) (container.w-5) (container.h-5)" );

        // add a mouse listener to the button which will print a message and change the background color when pressed
        button.addGlimpseMouseListener( new GlimpseMouseAdapter( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                System.out.println( "Pressed!" );
                background.setColor( GlimpseColor.fromColorRgb( 0.8f, 0.8f, 0.8f ) );
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
                System.out.println( "Released!" );
                background.setColor( GlimpseColor.fromColorRgb( 0.4f, 0.4f, 0.4f ) );
            }
        } );

        // add the GlimpseLayout button to the main plotting area of the HeatMap plot
        plot.getLayoutCenter( ).addLayout( button );

        return plot;
    }
}
