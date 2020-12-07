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
import static com.metsci.glimpse.core.support.wrapped.WrappedGlimpseContext.getWrapper2D;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.axis.WrappedAxis1D;
import com.metsci.glimpse.core.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.core.axis.painter.label.WrappedLabelHandler;
import com.metsci.glimpse.core.context.GlimpseBounds;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.core.gl.GLEditableBuffer;
import com.metsci.glimpse.core.gl.util.GLUtils;
import com.metsci.glimpse.core.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.core.painter.group.WrappedPainter;
import com.metsci.glimpse.core.painter.texture.HeatMapPainter;
import com.metsci.glimpse.core.plot.ColorAxisPlot2D;
import com.metsci.glimpse.core.support.color.GlimpseColor;
import com.metsci.glimpse.core.support.shader.point.PointFlatColorProgram;
import com.metsci.glimpse.core.support.wrapped.Wrapper2D;

public class WrappedAxisExample
{
    public static void main( String[] args )
    {
        swingInvokeLater( ( ) ->
        {
            // create a ColorAxisPlot, but with wrapped axes and modified painters
            ColorAxisPlot2D plot = new ColorAxisPlot2D( )
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

            // apply the same axis customizations as HeatMapExample to our plot
            HeatMapExample.customizePlot( plot );

            plot.setTitle( "Wrapped Axis Example" );

            // don't let the user zoom out too far (especially important with wrapped axes
            // since this will cause the scene to be painted many times)
            plot.getAxis( ).getAxisX( ).setMaxSpan( 3000 );
            plot.getAxis( ).getAxisY( ).setMaxSpan( 3000 );

            // create a heat map painter
            HeatMapPainter heatmapPainter = HeatMapExample.newPainter( plot.getAxisZ( ) );

            // add the HeatMapPainter to a WrappedPainter then add the WrappedPainter to the plot
            WrappedPainter wrappedPainter = new WrappedPainter( );
            wrappedPainter.addPainter( heatmapPainter );
            plot.addPainter( wrappedPainter );

            // load the color map into the plot (so the color scale is displayed on the z axis)
            plot.setColorScale( heatmapPainter.getColorScale( ) );

            // add a painter that paints things in pixel-space (round dots should stay round
            // regardless of zooming, wrapping, and canvas-resizing)
            wrappedPainter.addPainter( new DotPainter( ) );

            // create a window and show the plot
            quickGlimpseApp( "Wrapped Axis Example", GL3, plot );
        } );
    }

    public static class DotPainter extends GlimpsePainterBase
    {
        protected PointFlatColorProgram prog;
        protected GLEditableBuffer buffer;

        public DotPainter( )
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
            Wrapper2D wrapper = getWrapper2D( context );
            GlimpseBounds bounds = getBounds( context );

            GLUtils.enableStandardBlending( gl );
            prog.begin( gl );
            try
            {
                prog.setAxisOrtho( gl, axis );
                prog.setWrapper( gl, wrapper );
                prog.setViewport( gl, bounds );
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
    }
}
