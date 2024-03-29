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
package com.metsci.glimpse.core.examples.axis;

import static com.jogamp.opengl.GLProfile.GL3;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.core.support.QuickUtils.swingInvokeLater;

import com.metsci.glimpse.core.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.core.axis.painter.label.HdrAxisLabelHandler;
import com.metsci.glimpse.core.plot.SimplePlot2D;

/**
 * Use a high dynamic range axis label handler. The tick labels are always a fixed
 * width (modulo the negative sign) so zooming will never obfuscate the values.
 *
 * @author borkholder
 */
public class HdrAxisExample
{
    public static void main( String[] args )
    {
        swingInvokeLater( ( ) ->
        {
            SimplePlot2D plot = new SimplePlot2D( )
            {
                @Override
                protected GridAxisLabelHandler createLabelHandlerX( )
                {
                    return new HdrAxisLabelHandler( );
                }

                @Override
                protected GridAxisLabelHandler createLabelHandlerY( )
                {
                    return new HdrAxisLabelHandler( );
                }

                @Override
                protected GridAxisLabelHandler createLabelHandlerZ( )
                {
                    return new HdrAxisLabelHandler( );
                }
            };
            plot.setAxisSizeZ( 80 );
            plot.getAxisPainterX( ).setAxisLabelBufferSize( 2 );

            plot.setTitle( "HDR Axis Example" );

            plot.getAxis( ).getAxisX( ).setMin( 0 );
            plot.getAxis( ).getAxisX( ).setMax( 1e5 );
            plot.getAxis( ).getAxisY( ).setMin( 100 );
            plot.getAxis( ).getAxisY( ).setMax( 100 + 1e-5 );
            plot.getAxisZ( ).setMin( 0 );
            plot.getAxisZ( ).setMax( 1.1 );

            // create a window and show the plot
            quickGlimpseApp( "HDR Axis Example", GL3, plot );
        } );
    }
}