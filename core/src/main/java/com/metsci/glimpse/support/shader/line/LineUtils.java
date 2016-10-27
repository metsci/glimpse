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
package com.metsci.glimpse.support.shader.line;

import static java.lang.Math.*;

import com.metsci.glimpse.axis.Axis2D;

public class LineUtils
{

    /**
     * Computes XY distance in X units, allowing Y units to differ from X units by a linear factor.
     * The ratio of X units to Y units is specified with {@code ppvAspectRatio}. This is useful when
     * computing cumulative distance along a line-strip, for stippling.
     * <p>
     * @see #ppvAspectRatio(Axis2D)
     */
    public static double distance( double x0, double y0, double x1, double y1, double ppvAspectRatio )
    {
        double dx = x1 - x0;
        double dy = ( y1 - y0 ) / ppvAspectRatio;
        return sqrt( dx*dx + dy*dy );
    }

    public static double ppvAspectRatio( Axis2D axis )
    {
        return ( axis.getAxisX( ).getPixelsPerValue( ) / axis.getAxisY( ).getPixelsPerValue( ) );
    }

}
