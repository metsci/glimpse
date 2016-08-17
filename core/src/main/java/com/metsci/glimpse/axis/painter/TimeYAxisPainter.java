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

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.TimeZone;

import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.time.AbsoluteTimeAxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.time.TimeAxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.time.TimeStruct;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * A vertical (y) time axis painter.
 *
 * @author ulman
 * @see TimeAxisPainter
 */
public class TimeYAxisPainter extends TimeAxisPainter
{
    protected static final double dateTextRightPadding = 4;

    public TimeYAxisPainter( TimeZone timeZone, Epoch epoch )
    {
        super( new AbsoluteTimeAxisLabelHandler( timeZone, epoch ) );
    }

    public TimeYAxisPainter( Epoch epoch )
    {
        super( new AbsoluteTimeAxisLabelHandler( epoch ) );
    }

    public TimeYAxisPainter( TimeAxisLabelHandler handler )
    {
        super( handler );
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        super.paintTo( context, bounds, axis );

        if ( textRenderer == null ) return;

        GL2 gl = context.getGL( ).getGL2( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( width == 0 || height == 0 ) return;

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( -0.5, width - 1 + 0.5f, axis.getMin( ), axis.getMax( ), -1, 1 );

        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity( );

        gl.glColor4fv( tickColor, 0 );

        List<TimeStamp> tickTimes = handler.getTickPositions( axis, height );

        // Tick marks
        gl.glBegin( GL2.GL_LINES );
        for ( TimeStamp t : tickTimes )
        {
            double y = fromTimeStamp( t );
            gl.glVertex2d( width, y );
            gl.glVertex2d( width - tickLineLength, y );
        }
        gl.glEnd( );

        GlimpseColor.setColor( textRenderer, textColor );

        List<String> tickLabels = handler.getTickLabels( axis, tickTimes );

        double iTimeText = printTickLabels( tickLabels, tickTimes, axis, width, height );
        if ( isShowDateLabels( ) ) printHoverLabels( gl, tickTimes, axis, iTimeText, width, height );
    }

    private void printHoverLabels( GL2 gl, List<TimeStamp> tickTimes, Axis1D axis, double iTimeText, int width, int height )
    {
        // Date labels
        List<TimeStruct> timeStructs = handler.getTimeStructs( axis, tickTimes );
        for ( TimeStruct timeStruct : timeStructs )
        {
            String text = timeStruct.text;
            Rectangle2D textBounds = textRenderer.getBounds( text );

            // Text will be drawn rotated 90 degrees, so height
            // is the *width* of the bounds rectangle
            //
            double halfTextHeight = 0.5 * textBounds.getWidth( );

            // To make translate/rotate work right, we need j
            // to be the vertical *center* of the text
            //
            int jMin = ( int ) Math.ceil( axis.valueToScreenPixel( fromTimeStamp( timeStruct.start ) ) + halfTextHeight );
            int jMax = ( int ) Math.floor( axis.valueToScreenPixel( fromTimeStamp( timeStruct.end ) ) - halfTextHeight );
            int jApprox = axis.valueToScreenPixel( fromTimeStamp( timeStruct.textCenter ) );
            int j = Math.max( jMin, Math.min( jMax, jApprox ) );
            if ( j - halfTextHeight < 0 || j + halfTextHeight > height ) continue;

            int i = ( int ) Math.floor( iTimeText - dateTextRightPadding - 1 );

            // Getting rotated text out of TextRenderer is tricky ...
            // begin, matrix-mode, push, draw, end, and pop must be
            // called in exactly this order
            //
            textRenderer.beginRendering( width, height );
            gl.glMatrixMode( GL2.GL_MODELVIEW );
            gl.glPushMatrix( );
            try
            {
                gl.glTranslatef( i, j, 0 );
                gl.glRotatef( 90, 0, 0, 1 );
                textRenderer.draw( text, ( int ) Math.round( -halfTextHeight ), 0 );
            }
            finally
            {
                textRenderer.endRendering( );
                gl.glPopMatrix( );
            }
        }
    }

    private double printTickLabels( List<String> tickLabels, List<TimeStamp> tickTimes, Axis1D axis, int width, int height )
    {
        int iTimeText = Integer.MAX_VALUE;
        textRenderer.beginRendering( width, height );
        try
        {
            for ( int index = 0; index < tickLabels.size( ); index++ )
            {
                TimeStamp t = tickTimes.get( index );
                String string = tickLabels.get( index );
                Rectangle2D textBounds = textRenderer.getBounds( string );

                double textHeight = textBounds.getHeight( );
                int j = ( int ) Math.round( axis.valueToScreenPixel( fromTimeStamp( t ) ) - 0.5 * Math.max( 1, textHeight - 2 ) );
                if ( j < 0 || j + textHeight > height ) continue;

                int i = ( int ) Math.round( width - tickLineLength - textBounds.getWidth( ) ) - 1;
                iTimeText = Math.min( iTimeText, i );

                textRenderer.draw( string, i, j );
            }
        }
        finally
        {
            textRenderer.endRendering( );
        }

        return iTimeText;
    }
}
