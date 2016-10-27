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

import javax.media.opengl.GL3;

import com.jogamp.opengl.math.Matrix4;
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
    protected static final float PI_2 = ( float ) ( Math.PI / 2.0f );
    protected static final double dateTextRightPadding = 4;

    protected Matrix4 transformMatrix = new Matrix4( );

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
    public void doPaintTo( GlimpseContext context )
    {
        super.doPaintTo( context );

        if ( textRenderer == null ) return;

        GL3 gl3 = context.getGL( ).getGL3( );
        Axis1D axis = getAxis1D( context );
        GlimpseBounds bounds = getBounds( context );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( width == 0 || height == 0 ) return;

        List<TimeStamp> tickTimes = handler.getTickPositions( axis, height );

        progLine.begin( gl3 );
        try
        {
            pathLine.clear( );
            for ( TimeStamp t : tickTimes )
            {
                float y = ( float ) fromTimeStamp( t );

                pathLine.moveTo( width, y );
                pathLine.lineTo( width - tickSize, y );
            }

            style.thickness_PX = tickLineWidth;
            style.rgba = tickColor;

            progLine.setViewport( gl3, bounds );
            progLine.setOrtho( gl3, -0.5f, width - 0.5f, ( float ) axis.getMin( ), ( float ) axis.getMax( ) );

            progLine.draw( gl3, style, pathLine );
        }
        finally
        {
            progLine.end( gl3 );
        }

        List<String> tickLabels = handler.getTickLabels( axis, tickTimes );

        double iTimeText = printTickLabels( tickLabels, tickTimes, axis, width, height );
        if ( isShowDateLabels( ) ) printHoverLabels( gl3, tickTimes, axis, iTimeText, width, height );
    }

    private void printHoverLabels( GL3 gl, List<TimeStamp> tickTimes, Axis1D axis, double iTimeText, int width, int height )
    {
        textRenderer.begin3DRendering( );
        try
        {
            GlimpseColor.setColor( textRenderer, textColor );

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
                float jMin = ( float ) ( axis.valueToScreenPixel( fromTimeStamp( timeStruct.start ) ) + halfTextHeight );
                float jMax = ( float ) ( axis.valueToScreenPixel( fromTimeStamp( timeStruct.end ) ) - halfTextHeight );
                float jApprox = axis.valueToScreenPixel( fromTimeStamp( timeStruct.textCenter ) );
                float j = Math.max( jMin, Math.min( jMax, jApprox ) );
                if ( j - halfTextHeight < 0 || j + halfTextHeight > height ) continue;

                float i = ( float ) ( iTimeText - dateTextRightPadding - 1 );

                transformMatrix.loadIdentity( );
                transformMatrix.makeOrtho( 0, width, 0, height, -1, 1 );
                transformMatrix.translate( i, j, 0 );
                transformMatrix.rotate( PI_2, 0, 0, 1.0f );

                textRenderer.setTransform( transformMatrix.getMatrix( ) );

                textRenderer.draw3D( text, ( int ) Math.round( -halfTextHeight ), 0, 0, 1 );
            }
        }
        finally
        {
            textRenderer.end3DRendering( );
        }
    }

    private float printTickLabels( List<String> tickLabels, List<TimeStamp> tickTimes, Axis1D axis, int width, int height )
    {
        float iTimeText = Float.POSITIVE_INFINITY;
        textRenderer.beginRendering( width, height );
        try
        {
            GlimpseColor.setColor( textRenderer, textColor );

            for ( int index = 0; index < tickLabels.size( ); index++ )
            {
                TimeStamp t = tickTimes.get( index );
                String string = tickLabels.get( index );
                Rectangle2D textBounds = textRenderer.getBounds( string );

                double textHeight = textBounds.getHeight( );
                float j = ( float ) ( axis.valueToScreenPixel( fromTimeStamp( t ) ) - 0.5 * Math.max( 1, textHeight - 2 ) );
                if ( j < 0 || j + textHeight > height ) continue;

                float i = ( float ) ( width - tickSize - textBounds.getWidth( ) - 1 - textBufferSize - tickBufferSize );
                iTimeText = Math.min( iTimeText, i );

                textRenderer.draw3D( string, i, j, 0, 1 );
            }
        }
        finally
        {
            textRenderer.endRendering( );
        }

        return iTimeText;
    }
}