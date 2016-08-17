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

import static java.lang.Math.round;

import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * A vertical (y) axis with labeled ticks along the left side.
 *
 * @author ulman
 */
public class NumericYAxisPainter extends NumericLabelHandlerAxisPainter
{
    public NumericYAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        updateTextRenderer( );
        if ( textRenderer == null ) return;

        GL2 gl = context.getGL( ).getGL2( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( -0.5, width - 1 + 0.5f, axis.getMin( ), axis.getMax( ), -1, 1 );

        paintTicks( gl, axis, width, height );
        paintAxisLabel( gl, axis, width, height );
        paintSelectionLine( gl, axis, width, height );
    }

    protected void paintTicks( GL2 gl, Axis1D axis, int width, int height )
    {
        double[] yTicks = ticks.getTickPositions( axis );
        String[] yLabels = ticks.getTickLabels( axis, yTicks );

        AxisUnitConverter converter = ticks.getAxisUnitConverter( );

        // Tick marks
        double iTick0 = getTickRightX( width, tickSize );
        double iTick1 = getTickLeftX( width, tickSize );
        int min = -1;
        int max = yTicks.length;

        // Tick marks
        GlimpseColor.glColor( gl, tickColor );
        gl.glBegin( GL2.GL_LINES );
        try
        {
            for ( int i = 0; i < yTicks.length; i++ )
            {
                double jTick = converter.fromAxisUnits( yTicks[i] );

                // don't draw ticks off the screen
                if ( jTick > axis.getMax( ) && !showLabelsForOffscreenTicks )
                {
                    max = i;
                    break;
                }
                else if ( jTick < axis.getMin( ) && !showLabelsForOffscreenTicks )
                {
                    min = i;
                    continue;
                }
                else
                {
                    gl.glVertex2d( iTick0, jTick );
                    gl.glVertex2d( iTick1, jTick );
                }
            }

            if ( showMinorTicks )
            {
                double[] xMinor = ticks.getMinorTickPositions( yTicks );
                iTick0 = getTickRightX( width, tickSize / 2 );
                iTick1 = getTickLeftX( width, tickSize / 2 );

                for ( int i = 0; i < xMinor.length; i++ )
                {
                    double jTick = converter.fromAxisUnits( xMinor[i] );

                    gl.glVertex2d( iTick0, jTick );
                    gl.glVertex2d( iTick1, jTick );
                }
            }
        }
        finally
        {
            gl.glEnd( );
        }

        if ( showTickLabels )
        {
            // Tick labels
            GlimpseColor.setColor( textRenderer, tickLabelColor );
            textRenderer.beginRendering( width, height );
            try
            {
                for ( int i = min + 1; i < max; i++ )
                {
                    double yTick = yTicks[i];
                    String yLabel = yLabels[i];
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

    protected void paintAxisLabel( GL2 gl, Axis1D axis, int width, int height )
    {
        // Axis label
        if ( showLabel )
        {
            GlimpseColor.setColor( textRenderer, axisLabelColor );
            textRenderer.beginRendering( width, height );
            try
            {
                String label = ticks.getAxisLabel( axis );
                Rectangle2D labelSize = textRenderer.getBounds( label );
                int iAxisLabel = getAxisLabelPositionX( width, ( int ) labelSize.getHeight( ) );
                int jAxisLabel = round( 0.5f * ( height - ( int ) labelSize.getWidth( ) ) );

                gl.glMatrixMode( GL2.GL_PROJECTION );
                gl.glTranslatef( iAxisLabel, jAxisLabel, 0 );
                gl.glRotatef( 90, 0, 0, 1.0f );

                textRenderer.draw( label, 0, 0 );
            }
            finally
            {
                textRenderer.endRendering( );
            }
        }
    }

    protected void paintSelectionLine( GL2 gl, Axis1D axis, int width, int height )
    {
        AxisUnitConverter converter = ticks.getAxisUnitConverter( );

        // Selection line
        if ( showSelectionLine )
        {
            gl.glLineWidth( markerWidth );

            double y0 = converter.fromAxisUnits( axis.getSelectionCenter( ) );

            gl.glBegin( GL2.GL_LINES );
            try
            {
                gl.glVertex2d( 0, y0 );
                gl.glVertex2d( width, y0 );
            }
            finally
            {
                gl.glEnd( );
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
