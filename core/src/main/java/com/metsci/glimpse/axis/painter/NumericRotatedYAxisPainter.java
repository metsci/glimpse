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
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * A vertical (y) axis with labeled ticks along the left hand side. Ticks labels are
 * oriented vertically, taking up less horizontal room and allowing the axis to be more
 * compact than {@link NumericRightYAxisPainter}.<p>
 *
 * Suitable for an axis which should sit flush against the left hand side of a plot.
 *
 * @author ulman
 */
public class NumericRotatedYAxisPainter extends NumericYAxisPainter
{

    public NumericRotatedYAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );
    }

    @Override
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
                    if ( jTick == height ) jTick = height - 1;

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
            for ( int i = min + 1; i < max; i++ )
            {
                double yTick = yTicks[i];
                String yLabel = yLabels[i];
                Rectangle2D tickTextBounds = textRenderer.getBounds( yLabel );

                int iTickText = getTickTextPositionX( width, ( int ) tickTextBounds.getHeight( ) );
                int jTickText = ( int ) round( axis.valueToScreenPixel( converter.fromAxisUnits( yTick ) ) + tickTextBounds.getWidth( ) / 2 );

                if ( keepLabelsForExtremaFullyVisible )
                {
                    if ( jTickText < tickTextBounds.getWidth( ) )
                    {
                        jTickText = ( int ) tickTextBounds.getWidth( );
                    }

                    if ( jTickText > height )
                    {
                        jTickText = height;
                    }
                }

                textRenderer.beginRendering( width, height );
                try
                {
                    gl.glMatrixMode( GL2.GL_PROJECTION );
                    gl.glTranslatef( iTickText, jTickText, 0 );
                    gl.glRotatef( -90, 0, 0, 1.0f );

                    textRenderer.draw( yLabel, 0, 0 );
                }
                finally
                {
                    textRenderer.endRendering( );
                }
            }
        }
    }
}
