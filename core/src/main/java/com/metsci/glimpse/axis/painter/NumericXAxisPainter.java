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
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * A horizontal (x) axis with labeled ticks along the bottom.
 *
 * @author ulman
 */
public class NumericXAxisPainter extends NumericLabelHandlerAxisPainter
{
    protected boolean packLabel = false;

    public NumericXAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis1D axis = getAxis1D( context );
        GL gl = context.getGL( );

        updateTextRenderer( );
        if ( textRenderer == null ) return;

        paintTicks( gl, axis, bounds );
        paintAxisLabel( gl, axis, bounds );
        paintSelectionLine( gl, axis, bounds );
    }

    protected void paintTicks( GL gl, Axis1D axis, GlimpseBounds bounds )
    {
        GL3 gl3 = gl.getGL3( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        double[] xTicks = ticks.getTickPositions( axis );
        String[] xLabels = ticks.getTickLabels( axis, xTicks );

        AxisUnitConverter converter = ticks.getAxisUnitConverter( );

        int min = -1;
        int max = xTicks.length;
        double jTick0 = getTickTopY( height, tickSize );
        double jTick1 = getTickBottomY( height, tickSize );

        progLine.begin( gl3 );
        try
        {
            pathLine.clear( );
            style.thickness_PX = tickLineWidth;
            style.rgba = tickColor;

            for ( int i = 0; i < xTicks.length; i++ )
            {
                double iTick = converter.fromAxisUnits( xTicks[i] );

                // don't draw ticks off the screen
                if ( iTick < axis.getMin( ) && !showLabelsForOffscreenTicks )
                {
                    min = i;
                    continue;
                }
                else if ( iTick > axis.getMax( ) && !showLabelsForOffscreenTicks )
                {
                    max = i;
                    break;
                }

                pathLine.moveTo( ( float ) iTick, ( float ) jTick0 );
                pathLine.lineTo( ( float ) iTick, ( float ) jTick1 );
            }

            if ( showMinorTicks )
            {
                double[] xMinor = ticks.getMinorTickPositions( xTicks );
                jTick0 = getTickTopY( height, tickSize / 2 );
                jTick1 = getTickBottomY( height, tickSize / 2 );

                for ( int i = 0; i < xMinor.length; i++ )
                {
                    double iTick = converter.fromAxisUnits( xMinor[i] );

                    pathLine.moveTo( ( float ) iTick, ( float ) jTick0 );
                    pathLine.lineTo( ( float ) iTick, ( float ) jTick1 );
                }
            }

            progLine.setViewport( gl3, bounds );
            progLine.setOrtho( gl3, ( float ) axis.getMin( ), ( float ) axis.getMax( ), -0.5f, height - 0.5f );

            progLine.draw( gl3, style, pathLine, 1.0 );
        }
        finally
        {
            progLine.end( gl3 );
        }

        if ( showTickLabels )
        {
            // Tick labels
            textRenderer.beginRendering( width, height );
            try
            {
                GlimpseColor.setColor( textRenderer, tickLabelColor );

                // Tick labels
                for ( int i = min + 1; i < max; i++ )
                {
                    double xTick = xTicks[i];
                    String xLabel = xLabels[i];
                    Rectangle2D tickTextBounds = textRenderer.getBounds( xLabel );
                    Rectangle2D dashTextBounds = textRenderer.getBounds( "-" );
                    double dashTextWidth = xTick < 0 ? dashTextBounds.getWidth( ) : 0;
                    double absTextWidth = tickTextBounds.getWidth( ) - dashTextWidth;

                    int jTickText = getTickTextPositionY( height, ( int ) tickTextBounds.getHeight( ) );
                    int iTickText = ( int ) round( axis.valueToScreenPixel( converter.fromAxisUnits( xTick ) ) - 0.5f * absTextWidth - dashTextWidth );

                    if ( keepLabelsForExtremaFullyVisible )
                    {
                        if ( iTickText < 0 )
                        {
                            iTickText = 0;
                        }

                        if ( iTickText + tickTextBounds.getWidth( ) > width )
                        {
                            iTickText = width - ( int ) tickTextBounds.getWidth( );
                        }
                    }

                    textRenderer.draw( xLabel, iTickText, jTickText );
                }

            }
            finally
            {
                textRenderer.endRendering( );
            }
        }
    }

    protected void paintAxisLabel( GL gl, Axis1D axis, GlimpseBounds bounds )
    {
        // Axis Label
        if ( showLabel )
        {
            int width = bounds.getWidth( );
            int height = bounds.getHeight( );

            textRenderer.beginRendering( width, height );
            try
            {
                GlimpseColor.setColor( textRenderer, axisLabelColor );

                String label = ticks.getAxisLabel( axis );
                Rectangle2D axisLabelBounds = textRenderer.getBounds( label );
                int iAxisLabel = round( 0.5f * ( width - ( int ) axisLabelBounds.getWidth( ) ) );
                int jAxisLabel = getAxisLabelPositionY( height, ( int ) axisLabelBounds.getHeight( ) );

                textRenderer.draw( label, iAxisLabel, jAxisLabel );
            }
            finally
            {
                textRenderer.endRendering( );
            }
        }
    }

    protected void paintSelectionLine( GL gl, Axis1D axis, GlimpseBounds bounds )
    {
        // Selection line
        if ( showSelectionLine )
        {
            GL3 gl3 = gl.getGL3( );

            int height = bounds.getHeight( );
            float x0 = ( float ) axis.getSelectionCenter( );

            progLine.begin( gl3 );
            try
            {
                pathLine.clear( );
                style.thickness_PX = markerWidth;
                style.rgba = tickColor;

                pathLine.moveTo( x0, 0 );
                pathLine.lineTo( x0, height );

                progLine.draw( gl3, style, pathLine, 1.0 );
            }
            finally
            {
                progLine.end( gl3 );
            }
        }
    }

    public int getAxisLabelPositionY( int height, int textHeight )
    {
        return labelBufferSize;
    }

    public int getTickTextPositionY( int height, int textHeight )
    {
        return height - 1 - tickBufferSize - tickSize - textBufferSize - textHeight;
    }

    public int getTickTopY( int height, int size )
    {
        return height - 1 - tickBufferSize;
    }

    public int getTickBottomY( int height, int size )
    {
        return height - 1 - tickBufferSize - size;
    }
}
