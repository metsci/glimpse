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

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.shape.DynamicLineSetPainter;
import com.metsci.glimpse.painter.shape.DynamicLineSetPainter.BulkLineAccumulator;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * @author ulman
 * @see com.metsci.glimpse.examples.scatterplot.DynamicPointPainterExample
 */
public class DynamicLinePainterExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new DynamicLinePainterExample( ) );
    }

    @Override
    public EmptyPlot2D getLayout( )
    {
        // create a simple pre-built Glimpse plot
        EmptyPlot2D plot = new EmptyPlot2D( );

        // set the x and y axis bounds
        plot.getAxis( ).set( -1, 2, -1, 2 );

        // create a painter to display dynamically colored lines
        final DynamicLineSetPainter painter = new DynamicLineSetPainter( );

        // tell the painter to display dotted lines with the provided stipple pattern
        painter.setDotted( 2, ( short ) 0xAAAA );

        plot.addPainter( painter );

        ( new Thread( )
        {
            int count = 0;

            public void run( )
            {
                try
                {

                    while ( true )
                    {
                        BulkLineAccumulator accum = new BulkLineAccumulator( );

                        float[] color = GlimpseColor.fromColorRgba( ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ) );

                        for ( int i = 0; i < 20; i++ )
                        {
                            accum.add( count++, ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), color );
                        }

                        painter.putLines( accum );

                        try
                        {
                            Thread.sleep( 20 );
                        }
                        catch ( InterruptedException e )
                        {
                        }
                    }

                }
                catch ( Exception e )
                {
                    e.printStackTrace( );
                }
            }
        } ).start( );

        return plot;
    }
}