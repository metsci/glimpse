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
package com.metsci.glimpse.examples.axis;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.WrappedAxis1D;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.WrappedLabelHandler;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.group.WrappedPainter;
import com.metsci.glimpse.painter.texture.HeatMapPainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.point.PointFlatColorProgram;

public class WrappedAxisExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new WrappedAxisExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        // create a plot from the heat map example, but with wrapped axes and modified painters
        HeatMapExample example = new HeatMapExample( )
        {
            @Override
            protected ColorAxisPlot2D newPlot( )
            {
                return new ColorAxisPlot2D( )
                {
                    @Override
                    protected GridAxisLabelHandler createLabelHandlerX( )
                    {
                        return new WrappedLabelHandler( );
                    }

                    @Override
                    protected GridAxisLabelHandler createLabelHandlerY( )
                    {
                        return new WrappedLabelHandler( );
                    }

                    @Override
                    protected Axis1D createAxisX( )
                    {
                        return new WrappedAxis1D( 0, 1000 );
                    }

                    @Override
                    protected Axis1D createAxisY( )
                    {
                        return new WrappedAxis1D( 0, 1000 );
                    }
                };
            }
        };

        ColorAxisPlot2D plot = example.getLayout( );

        plot.setTitle( "Wrapped Axis Example" );

        // don't let the user zoom out too far (especially important with wrapped axes
        // since this will cause the scene to be painted many times)
        plot.getAxis( ).getAxisX( ).setMaxSpan( 3000 );
        plot.getAxis( ).getAxisY( ).setMaxSpan( 3000 );

        // remove the heat map painter from the plot and instead add it to a WrappedPainter
        // which is then added to the plot
        HeatMapPainter heatMapPainter = example.getPainter( );
        plot.removePainter( heatMapPainter );
        WrappedPainter wrappedPainter = new WrappedPainter( );
        wrappedPainter.addPainter( heatMapPainter );

        // add a painter that paints things in pixel-space (round dots should stay round
        // regardless of zooming, wrapping, and canvas-resizing)
        wrappedPainter.addPainter( new GlimpsePainterBase( )
        {
            protected PointFlatColorProgram prog;
            protected GLEditableBuffer buffer;

            {
                prog = new PointFlatColorProgram( );
                buffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );

                for ( int x = 0; x < 5; x++ )
                {
                    for ( int y = 0; y < 5; y++ )
                    {
                        buffer.grow2f( 200 * ( x + 0.5f ), 200 * ( y + 0.5f ) );
                    }
                }
            }

            @Override
            public void doPaintTo( GlimpseContext context )
            {
                GL3 gl = context.getGL( ).getGL3( );
                Axis2D axis = requireAxis2D( context );

                GLUtils.enableStandardBlending( gl );
                prog.begin( gl );
                try
                {
                    prog.setAxisOrtho( gl, axis );
                    prog.setRgba( gl, GlimpseColor.getWhite( ) );
                    prog.setPointSize( gl, 20.0f );

                    prog.draw( gl, buffer );
                }
                finally
                {
                    prog.end( gl );
                    GLUtils.disableBlending( gl );
                }
            }

            @Override
            protected void doDispose( GlimpseContext context )
            {
                GL3 gl = context.getGL( ).getGL3( );

                prog.dispose( gl );
                buffer.dispose( gl );
            }
        } );

        plot.addPainter( wrappedPainter );

        return plot;
    }
}