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

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.painter.track.TrackPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;

/**
 * Demonstrates usage of a custom {@link com.metsci.glimpse.axis.listener.mouse.AxisMouseListener}
 * to paint onto a Glimpse plot.
 *
 * @author ulman
 */
public class MouseDrawingExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new MouseDrawingExample( ) );
    }

    protected TrackPainter painter;

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        // create a standard pre-made SimplePlot2D, but override the
        // constructor method which it uses to create the listener
        // that handles mouse events inside the central plot area
        SimplePlot2D plot = new SimplePlot2D( )
        {
            @Override
            protected AxisMouseListener createAxisMouseListenerXY( )
            {
                return new CustomMouseListener2D( );
            }
        };

        // hide the title and labeled axis elements of the plot
        plot.setTitleHeight( 0 );
        plot.setAxisSizeX( 0 );
        plot.setAxisSizeY( 0 );

        // hide the crosshair painter
        plot.getCrosshairPainter( ).setVisible( false );

        // create a painter to draw lines and dots
        painter = new TrackPainter( );
        plot.addPainter( painter );

        // setup "track" 1, a track is simply a logical set of vertices which are
        // displayed in a common fashion (in this case: size 10 red dots with
        // no connecting lines)
        painter.setPointColor( 1, GlimpseColor.getRed( ) );
        painter.setPointSize( 1, 10.0f );
        painter.setShowLines( 1, false );

        // setup "track" 2 (connected green width 2.5 lines with no points at the vertices)
        painter.setShowPoints( 2, false );
        painter.setShowLines( 2, true );
        painter.setDotted( 2, false );
        painter.setLineColor( 2, GlimpseColor.getGreen( ) );
        painter.setLineWidth( 2, 2.5f );

        // add a painter to display text instructions on the screen
        SimpleTextPainter text = new SimpleTextPainter( );
        text.setText( "Hold Shift to draw. Right Click to add dot." );
        text.setHorizontalPosition( HorizontalPosition.Center );
        text.setVerticalPosition( VerticalPosition.Top );
        text.setFont( FontUtils.getDefaultBold( 16 ) );
        plot.addPainter( text );

        return plot;
    }

    // create a custom subclass of the standard AxisMouseListener2D
    // and add some custom functionality
    public class CustomMouseListener2D extends AxisMouseListener2D
    {
        int id1 = 0;
        int id2 = 0;

        public CustomMouseListener2D( )
        {
            super( );
        }

        @Override
        public void mouseMoved( GlimpseMouseEvent event )
        {
            // if mouse button 1 is down (i.e. this mouseMoved event is a drag with the left mouse button down)
            // and the Shift key is being held down, then perform our custom action, otherwise
            // default to the standard axis interaction
            if ( event.isButtonDown( MouseButton.Button1 ) && event.isKeyDown( ModifierKey.Shift ) )
            {
                double x = event.getAxisCoordinatesX( );
                double y = event.getAxisCoordinatesY( );

                painter.addPoint( 2, id1++, x, y, 0 );
            }
            else
            {
                super.mouseMoved( event );
            }
        }

        @Override
        public void mousePressed( GlimpseMouseEvent event )
        {
            Axis2D axis = event.getAxis2D( );
            Axis1D axisX = axis.getAxisX( );
            Axis1D axisY = axis.getAxisY( );

            // clicking button 3 normally locks the axis selection cursor, here
            // we provide an additional action (drawing a dot on the screen)
            // but still also pass through to super.mouseMoved to get the default behavior
            if ( event.isButtonDown( MouseButton.Button3 ) )
            {
                // instead of using the getAxisCoordinatesX() helper method, we can do the translation manually as well
                double x = axisX.screenPixelToValue( event.getX( ) );
                double y = axisY.screenPixelToValue( axisY.getSizePixels( ) - event.getY( ) );

                painter.addPoint( 1, id2++, x, y, 0 );
            }

            super.mousePressed( event );
        }
    }
}
