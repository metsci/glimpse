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

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.context.GlimpseBounds;
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
    protected void paintTickLabels( GL gl, Axis1D axis, GlimpseBounds bounds, TickInfo info )
    {
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( showTickLabels )
        {
            AxisUnitConverter converter = ticks.getAxisUnitConverter( );

            textRenderer.begin3DRendering( );
            try
            {
                // Tick labels
                GlimpseColor.setColor( textRenderer, tickLabelColor );

                for ( int i = info.minIndex + 1; i < info.maxIndex; i++ )
                {
                    double yTick = info.ticks[i];
                    String yLabel = info.labels[i];
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

                    transformMatrix.loadIdentity( );
                    transformMatrix.makeOrtho( 0, width, 0, height, -1, 1 );
                    transformMatrix.translate( iTickText, jTickText, 0 );
                    transformMatrix.rotate( -PI_2, 0, 0, 1.0f );

                    textRenderer.setTransform( transformMatrix.getMatrix( ) );

                    textRenderer.draw3D( yLabel, 0, 0, 0, 1 );
                }
            }
            finally
            {
                textRenderer.end3DRendering( );
            }
        }
    }
}
