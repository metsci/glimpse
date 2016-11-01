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
package com.metsci.glimpse.examples.plot;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.axis.listener.RateLimitedAxisListener2D;
import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.shape.LineSetPainter;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.math.fast.FastGaussian;

/**
 * Demonstrates construction of a simple Glimpse plot without the labeled axes,
 * a plot title, or any of the other extras which other Glimpse plots provide. Also
 * demonstrates usage of the NumericXYAxisPainter to paint "floating" X and Y axes
 * on the plot surface itself.
 *
 * @author ulman
 */
public class FunctionPlotExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new FunctionPlotExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        // add a layout painter which will act as the parent of all the other plot painters
        GlimpseAxisLayout2D layout = new GlimpseAxisLayout2D( );

        // use the Glimpse AxisFactory to create a pair of horizontal and vertical axis
        // and attach them to canvas mouse event handlers in one step
        final Axis2D axis = AxisUtil.createAxis2D( layout );

        // set axis bounds and lock the x-y axis aspect ratio
        axis.getAxisX( ).setMin( -2.0 );
        axis.getAxisX( ).setMax( 8.0 );

        axis.getAxisY( ).setMin( -2.0 );
        axis.getAxisY( ).setMax( 8.0 );

        axis.lockAspectRatioXY( 1.0 );

        // create a painter to fill in a blank background
        BackgroundPainter backgroundPainter = new BackgroundPainter( false );
        layout.addPainter( backgroundPainter );

        // create a painter to draw horizontal and vertical grid lines
        GridPainter gridPainter = new GridPainter( );
        layout.addPainter( gridPainter );

        // create a painter to draw axes at the origin and tick marks and labels
        NumericXYAxisPainter axisPainter = new NumericXYAxisPainter( );
        layout.addPainter( axisPainter );

        // define a 1D function (the pdf of the normal distribution)
        final Function1D f1 = new Function1D( )
        {
            final FastGaussian f = new FastGaussian( 1000000 );

            public double f( double x )
            {
                return f.evaluate( x ) * 5;
            }
        };

        // define another 1D function (the sin function)
        final Function1D f2 = new Function1D( )
        {
            public double f( double x )
            {
                return Math.sin( x );
            }
        };

        // create a painter to display the first function
        // the painter will automatically take care of creating enough
        // sample points to make the curve smooth as it is panned/zoomed
        final Function1DPainter functionPainter1 = new Function1DPainter( axis, f1 );
        functionPainter1.setLineColor( GlimpseColor.getBlue( ) );
        functionPainter1.setLineWidth( 2.5f );
        layout.addPainter( functionPainter1 );

        // create a painter to display the second function
        final Function1DPainter functionPainter2 = new Function1DPainter( axis, f2 );
        functionPainter2.setLineColor( GlimpseColor.getRed( ) );
        functionPainter2.setLineWidth( 2.5f );
        layout.addPainter( functionPainter2 );

        return layout;
    }

    public static class Function1DPainter extends LineSetPainter
    {
        private static final double PAN_BUFFER = 0.8;
        private static final double ZOOM_BUFFER = 0.5;
        private static final int X_POINTS = 300;

        private double currentMin;
        private double currentMax;
        private double currentDiff;

        private float[] dataX = new float[X_POINTS];
        private float[] dataY = new float[X_POINTS];

        public Function1DPainter( final Axis2D axis, final Function1D function )
        {
            axis.addAxisListener( new RateLimitedAxisListener2D( )
            {
                @Override
                public void axisUpdatedRateLimited( Axis2D axis )
                {
                    double min = axis.getAxisX( ).getMin( );
                    double max = axis.getAxisX( ).getMax( );
                    double diff = max - min;

                    if ( min < currentMin || max > currentMax || diff < currentDiff * ZOOM_BUFFER ) recalculate( min, max );
                }

                protected void recalculate( double min, double max )
                {
                    double buffer = ( max - min ) * PAN_BUFFER;

                    currentMin = min - buffer;
                    currentMax = max + buffer;
                    currentDiff = currentMax - currentMin;

                    double step = currentDiff / X_POINTS;

                    double x = currentMin;
                    for ( int i = 0; i < X_POINTS; i++, x += step )
                    {
                        dataX[i] = ( float ) x;
                        dataY[i] = ( float ) function.f( x );
                    }

                    setData( dataX, dataY );
                }
            } );
        }
    }

    public interface Function1D
    {
        public double f( double x );
    }
}
