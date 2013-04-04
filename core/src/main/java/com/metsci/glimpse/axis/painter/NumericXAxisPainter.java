/*
 * Copyright (c) 2012, Metron, Inc.
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
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * A horizontal (x) axis with labeled ticks along the bottom.
 *
 * @author ulman
 */
public class NumericXAxisPainter extends NumericAxisPainter
{
    protected boolean packLabel = false;

    public NumericXAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        updateTextRenderer( );
        if ( textRenderer == null ) return;

        GL gl = context.getGL( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( -0.5, width - 1 + 0.5f, -0.5, height - 1 + 0.5f, -1, 1 );

        paintTicks( gl, axis, width, height );
        paintAxisLabel( gl, axis, width, height );
        paintSelectionLine( gl, axis, width, height );
    }

    protected void paintTicks( GL gl, Axis1D axis, int width, int height )
    {
        double[] xTicks = ticks.getTickPositions( axis );
        String[] xLabels = ticks.getTickLabels( axis, xTicks );

        AxisUnitConverter converter = ticks.getAxisUnitConverter( );

        int min = -1;
        int max = xTicks.length;
        int jTick0 = getTickTopY( height, tickSize );
        int jTick1 = getTickBottomY( height, tickSize );

        // Tick marks
        GlimpseColor.glColor( gl, tickColor );
        gl.glBegin( GL.GL_LINES );
        try
        {
            for ( int i = 0; i < xTicks.length; i++ )
            {
                int iTick = axis.valueToScreenPixel( converter.fromAxisUnits( xTicks[i] ) );

                // keep the last tick on the screen
                if ( iTick == width ) iTick -= 1;

                // don't draw ticks off the screen
                if ( iTick < 0 && !showLabelsForOffscreenTicks )
                {
                    min = i;
                    continue;
                }
                else if ( iTick > width && !showLabelsForOffscreenTicks )
                {
                    max = i;
                    break;
                }

                gl.glVertex2f( iTick, jTick0 );
                gl.glVertex2f( iTick, jTick1 );
            }

            if ( showMinorTicks )
            {
                double[] xMinor = ticks.getMinorTickPositions( xTicks );
                jTick0 = getTickTopY( height, tickSize / 2 );
                jTick1 = getTickBottomY( height, tickSize / 2 );

                for ( int i = 0; i < xMinor.length; i++ )
                {
                    int iTick = axis.valueToScreenPixel( converter.fromAxisUnits( xMinor[i] ) );

                    gl.glVertex2f( iTick, jTick0 );
                    gl.glVertex2f( iTick, jTick1 );
                }
            }
        }
        finally
        {
            gl.glEnd( );
        }

        // Tick labels
        GlimpseColor.setColor( textRenderer, tickLabelColor );
        textRenderer.beginRendering( width, height );
        try
        {
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

    protected void paintAxisLabel( GL gl, Axis1D axis, int width, int height )
    {
        // Axis Label
        if ( showLabel )
        {
            GlimpseColor.setColor( textRenderer, axisLabelColor );
            textRenderer.beginRendering( width, height );
            try
            {
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

    protected void paintSelectionLine( GL gl, Axis1D axis, int width, int height )
    {
        // Selection line
        if ( showSelectionLine )
        {
            gl.glLineWidth( markerWidth );

            int x0 = axis.valueToScreenPixel( axis.getSelectionCenter( ) );

            gl.glBegin( GL.GL_LINES );
            try
            {
                gl.glVertex2f( x0, 0 );
                gl.glVertex2f( x0, height );
            }
            finally
            {
                gl.glEnd( );
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
