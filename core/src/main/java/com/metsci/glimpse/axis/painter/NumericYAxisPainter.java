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

import com.jogamp.opengl.math.Matrix4;
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
    protected static final float PI_2 = ( float ) ( Math.PI / 2.0f );

    protected Matrix4 transformMatrix;

    public NumericYAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );

        this.transformMatrix = new Matrix4( );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis1D axis = getAxis1D( context );
        GL gl = context.getGL( );

        updateTextRenderer( );
        if ( textRenderer == null ) return;

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
            textRenderer.beginRendering( width, height );
            try
            {
                GlimpseColor.setColor( textRenderer, tickLabelColor );

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
        GL3 gl3 = gl.getGL3( );

        int width = bounds.getWidth( );

        AxisUnitConverter converter = ticks.getAxisUnitConverter( );

        // Tick marks
        double iTick0 = getTickRightX( width, tickSize );
        double iTick1 = getTickLeftX( width, tickSize );

        progLine.begin( gl3 );
        try
        {
            pathLine.clear( );
            style.thickness_PX = tickLineWidth;
            style.rgba = tickColor;

            for ( int i = info.minIndex + 1; i < info.maxIndex; i++ )
            {
                double jTick = converter.fromAxisUnits( info.ticks[i] );

                pathLine.moveTo( ( float ) iTick0, ( float ) jTick );
                pathLine.lineTo( ( float ) iTick1, ( float ) jTick );
            }

            if ( showMinorTicks )
            {
                double[] xMinor = ticks.getMinorTickPositions( info.ticks );
                iTick0 = getTickRightX( width, tickSize / 2 );
                iTick1 = getTickLeftX( width, tickSize / 2 );

                for ( int i = 0; i < xMinor.length; i++ )
                {
                    double jTick = converter.fromAxisUnits( xMinor[i] );

                    pathLine.moveTo( ( float ) iTick0, ( float ) jTick );
                    pathLine.lineTo( ( float ) iTick1, ( float ) jTick );
                }
            }

            progLine.setViewport( gl3, bounds );
            progLine.setOrtho( gl3, -0.5f, width - 0.5f, ( float ) axis.getMin( ), ( float ) axis.getMax( ) );

            progLine.draw( gl3, style, pathLine, 1.0 );
        }
        finally
        {
            progLine.end( gl3 );
        }
    }

    protected void paintAxisLabel( GL gl, Axis1D axis, GlimpseBounds bounds )
    {
        // Axis label
        if ( showLabel )
        {
            int width = bounds.getWidth( );
            int height = bounds.getHeight( );

            textRenderer.begin3DRendering( );
            try
            {
                GlimpseColor.setColor( textRenderer, axisLabelColor );

                String label = ticks.getAxisLabel( axis );
                Rectangle2D labelSize = textRenderer.getBounds( label );
                int iAxisLabel = getAxisLabelPositionX( width, ( int ) labelSize.getHeight( ) );
                int jAxisLabel = round( 0.5f * ( height - ( int ) labelSize.getWidth( ) ) );

                transformMatrix.loadIdentity( );
                transformMatrix.makeOrtho( 0, width, 0, height, -1, 1 );
                transformMatrix.translate( iAxisLabel, jAxisLabel, 0 );
                transformMatrix.rotate( PI_2, 0, 0, 1.0f );

                textRenderer.setTransform( transformMatrix.getMatrix( ) );

                textRenderer.draw3D( label, 0, 0, 0, 1.0f );
            }
            finally
            {
                textRenderer.end3DRendering( );
            }
        }
    }

    protected void paintSelectionLine( GL gl, Axis1D axis, GlimpseBounds bounds )
    {
        AxisUnitConverter converter = ticks.getAxisUnitConverter( );

        // Selection line
        if ( showSelectionLine )
        {
            GL3 gl3 = gl.getGL3( );

            int width = bounds.getWidth( );
            double y0 = converter.fromAxisUnits( axis.getSelectionCenter( ) );

            progLine.begin( gl3 );
            try
            {
                pathLine.clear( );
                style.thickness_PX = markerWidth;
                style.rgba = tickColor;

                pathLine.moveTo( 0, ( float ) y0 );
                pathLine.lineTo( width, ( float ) y0 );

                progLine.draw( gl3, style, pathLine, 1.0 );
            }
            finally
            {
                progLine.end( gl3 );
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
