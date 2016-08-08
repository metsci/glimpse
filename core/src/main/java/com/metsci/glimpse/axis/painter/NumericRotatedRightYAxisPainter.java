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

import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;

/**
 * A vertical (y) axis with labeled ticks along the right hand side. Ticks labels are
 * oriented vertically, taking up less horizontal room and allowing the axis to be more
 * compact than {@link NumericRightYAxisPainter}.<p>
 *
 * Suitable for an axis which should sit flush against the right hand side of a plot.
 *
 * @author ulman
 */
public class NumericRotatedRightYAxisPainter extends NumericRotatedYAxisPainter
{
    public NumericRotatedRightYAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );
    }

    @Override
    public int getAxisLabelPositionX( int width, int textHeight )
    {
        return width - 1 - textHeight;
    }

    @Override
    public int getTickTextPositionX( int width, int textWidth )
    {
        return tickBufferSize + tickSize + textBufferSize;
    }

    @Override
    public int getTickRightX( int width, int size )
    {
        return tickBufferSize + size;
    }

    @Override
    public int getTickLeftX( int width, int size )
    {
        return tickBufferSize;
    }
}
