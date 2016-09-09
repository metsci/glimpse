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
package com.metsci.glimpse.axis.painter;

import static java.lang.Math.*;

import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.line.LineProgram;

/**
 * A vertical (y) axis with labeled ticks along the left side.
 *
 * @author ulman
 */
public class NumericYAxisPainter extends NumericAxisPainter
{
    public NumericYAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis1D axis = getAxis1D( context );
        GL2 gl = context.getGL( ).getGL2( );

        updateTextRenderer( );
        if ( textRenderer == null ) return;

        if ( prog == null )
        {
            prog = new LineProgram( gl );
        }

        TickInfo info = getTickInfo( axis, bounds );

        paintTicks( gl, axis, bounds, info );
        paintTickLabels( gl, axis, bounds, info );
        paintAxisLabel( gl, axis, bounds );
        paintSelectionLine( gl, axis, bounds );
    }

    protected void paintTickLabels( GL gl, Axis1D axis, GlimpseBounds bounds, TickInfo info )
    {
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( showTickLabels )
        {
            AxisUnitConverter converter = ticks.getAxisUnitConverter( );

            // Tick labels
            GlimpseColor.setColor( textRenderer, tickLabelColor );
            textRenderer.beginRendering( width, height );
            try
            {
                for ( int i = info.minIndex + 1; i < info.maxIndex; i++ )
                {
                    double yTick = info.ticks[i];
                    String yLabel = info.labels[i];
                    Rectangle2D tickTextBounds = textRenderer.getBounds( yLabel );
                    int iTickText = getTickTextPositionX( width, ( int ) tickTextBounds.getWidth( ) );
                    int jTickText = ( int ) round( axis.valueToScreenPixel( converter.fromAxisUnits( yTick ) ) - 0.35 * tickTextBounds.getHeight( ) );

                    if ( keepLabelsForExtremaFullyVisible )
                    {
                        if ( jTickText < 0 )
                        {
                            jTickText = 0;
                        }

                        if ( jTickText + tickTextBounds.getHeight( ) > height )
                        {
                            jTickText = height - ( int ) tickTextBounds.getHeight( );
                        }
                    }

                    textRenderer.draw( yLabel, iTickText, jTickText );
                }
            }
            finally
            {
                textRenderer.endRendering( );
            }
        }
    }

    protected void paintTicks( GL gl, Axis1D axis, GlimpseBounds bounds, TickInfo info )
    {
        GL2ES2 gl2es2 = gl.getGL2ES2( );

        int width = bounds.getWidth( );

        AxisUnitConverter converter = ticks.getAxisUnitConverter( );

        // Tick marks
        double iTick0 = getTickRightX( width, tickSize );
        double iTick1 = getTickLeftX( width, tickSize );

        prog.begin( gl2es2 );
        try
        {
            path.clear( );
            style.thickness_PX = tickLineWidth;
            style.rgba = tickColor;

            for ( int i = info.minIndex + 1; i < info.maxIndex; i++ )
            {
                double jTick = converter.fromAxisUnits( info.ticks[i] );

                path.moveTo( ( float ) iTick0, ( float ) jTick );
                path.lineTo( ( float ) iTick1, ( float ) jTick );
            }

            if ( showMinorTicks )
            {
                double[] xMinor = ticks.getMinorTickPositions( info.ticks );
                iTick0 = getTickRightX( width, tickSize / 2 );
                iTick1 = getTickLeftX( width, tickSize / 2 );

                for ( int i = 0; i < xMinor.length; i++ )
                {
                    double jTick = converter.fromAxisUnits( xMinor[i] );

                    path.moveTo( ( float ) iTick0, ( float ) jTick );
                    path.lineTo( ( float ) iTick1, ( float ) jTick );
                }
            }

            prog.setViewport( gl2es2, bounds );
            prog.setOrtho( gl2es2, -0.5f, width - 0.5f, ( float ) axis.getMin( ), ( float ) axis.getMax( ) );

            prog.draw( gl2es2, style, path, 1.0 );
        }
        finally
        {
            prog.end( gl2es2 );
        }
    }

    protected void paintAxisLabel( GL gl, Axis1D axis, GlimpseBounds bounds )
    {
        // Axis label
        if ( showLabel )
        {
            GL2 gl2 = gl.getGL2( );

            int width = bounds.getWidth( );
            int height = bounds.getHeight( );

            GlimpseColor.setColor( textRenderer, axisLabelColor );
            textRenderer.beginRendering( width, height );
            try
            {
                String label = ticks.getAxisLabel( axis );
                Rectangle2D labelSize = textRenderer.getBounds( label );
                int iAxisLabel = getAxisLabelPositionX( width, ( int ) labelSize.getHeight( ) );
                int jAxisLabel = round( 0.5f * ( height - ( int ) labelSize.getWidth( ) ) );

                gl2.glMatrixMode( GL2.GL_PROJECTION );
                gl2.glTranslatef( iAxisLabel, jAxisLabel, 0 );
                gl2.glRotatef( 90, 0, 0, 1.0f );

                textRenderer.draw( label, 0, 0 );
            }
            finally
            {
                textRenderer.endRendering( );
            }
        }
    }

    protected void paintSelectionLine( GL gl, Axis1D axis, GlimpseBounds bounds )
    {
        AxisUnitConverter converter = ticks.getAxisUnitConverter( );

        // Selection line
        if ( showSelectionLine )
        {
            GL2ES2 gl2es2 = gl.getGL2ES2( );

            int width = bounds.getWidth( );
            double y0 = converter.fromAxisUnits( axis.getSelectionCenter( ) );

            prog.begin( gl2es2 );
            try
            {
                path.clear( );
                style.thickness_PX = markerWidth;
                style.rgba = tickColor;

                path.moveTo( 0, ( float ) y0 );
                path.lineTo( width, ( float ) y0 );

                prog.draw( gl2es2, style, path, 1.0 );
            }
            finally
            {
                prog.end( gl2es2 );
            }
        }
    }

    public int getAxisLabelPositionX( int width, int textHeight )
    {
        return labelBufferSize + textHeight;
    }

    public int getTickTextPositionX( int width, int textWidth )
    {
        return width - 1 - tickBufferSize - tickSize - textBufferSize - textWidth;
    }

    public int getTickRightX( int width, int size )
    {
        return width - 1 - tickBufferSize;
    }

    public int getTickLeftX( int width, int size )
    {
        return width - 1 - tickBufferSize - size;
    }
}
