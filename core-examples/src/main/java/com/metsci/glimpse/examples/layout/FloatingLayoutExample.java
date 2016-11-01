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

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.NumericRotatedYAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.font.FontUtils;

/**
 * Demonstrates nesting of one Glimpse plot inside another. The inner Glimpse
 * plot is tied to a particular data location on the outer plot and moves
 * when the outer plot is panned or zoomed.<p>
 *
 * Further work is needed to encapsulate this behavior into easy-to-use helper
 * classes.
 *
 * @author ulman
 */
public class FloatingLayoutExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new FloatingLayoutExample( ) );
    }

    protected int plotHeight = 200;
    protected int plotWidth = 200;

    protected double plotMinX = 0.0;
    protected double plotMinY = 0.0;

    protected SimplePlot2D plot;
    protected GlimpseLayout floatingLayout;

    @Override
    public GlimpseLayout getLayout( )
    {
        // create the main plot
        plot = new SimplePlot2D( );

        // create a GlimpseLayout to contain the floating plot
        floatingLayout = new GlimpseLayout( );

        // create a new plot for the floating layout area
        // it uses a vertical text orientation for its y axis painter to save space
        final SimplePlot2D floatingPlot = new SimplePlot2D( )
        {
            @Override
            protected NumericAxisPainter createAxisPainterY( AxisLabelHandler tickHandler )
            {
                return new NumericRotatedYAxisPainter( tickHandler );
            }
        };

        // add a mouse listener which listens for middle mouse button (mouse wheel) clicks
        // and moves the floating plot in response
        plot.getLayoutCenter( ).addGlimpseMouseListener( new GlimpseMouseListener( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                if ( event.isButtonDown( MouseButton.Button2 ) )
                {
                    plotMinX = event.getAxisCoordinatesX( );
                    plotMinY = event.getAxisCoordinatesY( );
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

        // the floating plot is quite small, so use a smaller font, tighter tick spacing, and smaller bounds for the axes
        floatingPlot.setAxisFont( FontUtils.getSilkscreen( ), false );
        floatingPlot.setAxisSizeX( 25 );
        floatingPlot.setAxisSizeY( 25 );
        floatingPlot.setTickSpacingX( 35 );
        floatingPlot.setTickSpacingY( 35 );
        floatingPlot.setBorderSize( 4 );

        // don't show crosshairs in the floating plot or the main plot
        floatingPlot.getCrosshairPainter( ).setVisible( false );
        plot.getCrosshairPainter( ).setVisible( false );

        // don't provide any space for a title in the floating plot
        floatingPlot.setTitleHeight( 0 );

        // add a border to the outside of the floating plot, setting its zOrder
        // to ensure it appears above other plot features
        floatingLayout.addPainter( new BorderPainter( ).setLineWidth( 2 ), Integer.MAX_VALUE );

        // create a color scale axis for the heat maps created below
        Axis1D colorAxis = new Axis1D( );
        colorAxis.setMin( 0.0 );
        colorAxis.setMax( 1000.0 );

        // add a heat map painter to the floating plot
        floatingPlot.addPainter( HeatMapExample.newHeatMapPainter( colorAxis ) );
        floatingPlot.getAxis( ).set( 0, 1000, 0, 1000 );

        // add a heat map painter to the outer plot
        plot.addPainter( HeatMapExample.newHeatMapPainter( colorAxis ) );
        plot.getAxis( ).set( 0, 1000, 0, 1000 );

        // add the floating plot to the main plot
        floatingLayout.addLayout( floatingPlot );
        plot.getLayoutCenter( ).addLayout( floatingLayout );
        plot.getLayoutCenter( ).invalidateLayout( );

        plot.setAxisSizeY( 45 );
        plot.setAxisSizeX( 30 );
        plot.setTitleHeight( 0 );

        // add an axis listener which adjusts the position of the floating layout painter as the axis changes
        // (the layout painter is tied to a fixed axis value)
        plot.addAxisListener( new AxisListener2D( )
        {
            @Override
            public void axisUpdated( Axis2D axis )
            {
                int minX = plot.getAxisX( ).valueToScreenPixel( plotMinX );
                int minY = plot.getAxisY( ).valueToScreenPixel( plotMinY );

                floatingLayout.setLayoutData( String.format( "pos %d %d %d %d", minX, minY, minX + plotWidth, minY + plotHeight ) );
                plot.invalidateLayout( );
            }
        } );

        return plot;
    }
}
