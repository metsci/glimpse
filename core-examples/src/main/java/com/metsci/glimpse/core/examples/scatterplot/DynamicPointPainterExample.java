/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.examples.scatterplot;

import static com.jogamp.opengl.GLProfile.GL3bc;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.util.concurrent.ConcurrencyUtils.startThread;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;

import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.axis.listener.RateLimitedAxisListener2D;
import com.metsci.glimpse.core.painter.info.FpsPainter;
import com.metsci.glimpse.core.painter.shape.DynamicPointSetPainter;
import com.metsci.glimpse.core.painter.shape.DynamicPointSetPainter.BulkPointAccumulator;
import com.metsci.glimpse.core.plot.SimplePlot2D;
import com.metsci.glimpse.core.support.color.GlimpseColor;

/**
 * <p>Demonstrates use of DynamicPointSetPainter. This painter is useful when painting
 * large sets of points which can change dynamically and must be individually colored.</p>
 *
 * <p>TrackPainter is another option for painting large numbers of points, but it can only
 * efficiently draw large groups of points with a single color. ShadedPointPainter allows
 * even more control over the coloring of points (via a shader)  but does not allow easy
 * dynamic addition and removal of points.</p>
 *
 * @author ulman
 */
public class DynamicPointPainterExample
{
    private static final Logger logger = Logger.getLogger( DynamicPointPainterExample.class.getName( ) );

    public static void main( String[] args )
    {
        SwingUtilities.invokeLater( ( ) ->
        {
            SimplePlot2D plot = new SimplePlot2D( );

            plot.getAxis( ).set( -1, 2, -1, 2 );

            final DynamicPointSetPainter painter = new DynamicPointSetPainter( );

            painter.setPointSize( 10f );

            plot.addPainter( painter );
            plot.addPainter( new FpsPainter( ) );

            startThread( "Data Updater", true, new Runnable( )
            {
                int count = 0;

                @Override
                public void run( )
                {
                    try
                    {
                        while ( count < 50000 )
                        {
                            BulkPointAccumulator accum = new BulkPointAccumulator( );

                            float[] color = GlimpseColor.fromColorRgba( ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ) );

                            for ( int i = 0; i < 500; i++ )
                            {
                                accum.add( count++, ( float ) Math.random( ), ( float ) Math.random( ), color );
                            }

                            painter.putPoints( accum );

                            try
                            {
                                Thread.sleep( 20 );
                            }
                            catch ( InterruptedException e )
                            {
                            }

                            logger.info( "Total Points: " + count );
                        }
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace( );
                    }
                }
            } );

            plot.addAxisListener( new RateLimitedAxisListener2D( )
            {
                @Override
                public void axisUpdatedRateLimited( Axis2D axis )
                {
                    double centerX = axis.getAxisX( ).getSelectionCenter( );
                    double sizeX = axis.getAxisX( ).getSelectionSize( );

                    double centerY = axis.getAxisY( ).getSelectionCenter( );
                    double sizeY = axis.getAxisY( ).getSelectionSize( );

                    Collection<Object> selection = painter.getGeoRange( centerX - sizeX / 2.0, centerX + sizeX / 2.0, centerY - sizeY / 2.0, centerY + sizeY / 2.0 );

                    logInfo( logger, "Selected Ids: %s", selection );
                }
            } );

            // create a window and show the plot
            quickGlimpseApp( "Animated Polar Projection Example", GL3bc, plot );
        } );
    }
}