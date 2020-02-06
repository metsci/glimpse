/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.util.GeneralUtils.floats;

import java.util.Arrays;

import com.metsci.glimpse.core.support.color.GlimpseColor;

public class ExampleStyle
{

    public float feather_PX = 0.9f;

    public float[] rgbaOutsideTWindow = floats( 0.6f, 0.6f, 0.6f, 0.5f );
    public float[] rgbaInsideTWindow = GlimpseColor.getBlack( );

    public float pointSizeOutsideXyWindow_PX = 4.0f;
    public float pointSizeMinInsideXyWindow_PX = 5.0f;
    public float pointSizeMaxInsideXyWindow_PX = 10.0f;
    public long pointSizePeriodInsideXyWindow_MILLIS = 2000;


    public ExampleStyle( )
    {
    }

    public ExampleStyle( ExampleStyle orig )
    {
        this.feather_PX = orig.feather_PX;

        this.rgbaOutsideTWindow = Arrays.copyOf( orig.rgbaOutsideTWindow, 4 );
        this.rgbaInsideTWindow = Arrays.copyOf( orig.rgbaInsideTWindow, 4 );

        this.pointSizeOutsideXyWindow_PX = orig.pointSizeOutsideXyWindow_PX;
        this.pointSizeMinInsideXyWindow_PX = orig.pointSizeMinInsideXyWindow_PX;
        this.pointSizeMaxInsideXyWindow_PX = orig.pointSizeMaxInsideXyWindow_PX;
        this.pointSizePeriodInsideXyWindow_MILLIS = orig.pointSizePeriodInsideXyWindow_MILLIS;
    }

    public float pointSizeInsideXyWindow_PX( long renderTime_PMILLIS )
    {
        double periodFrac = ( renderTime_PMILLIS % this.pointSizePeriodInsideXyWindow_MILLIS ) / ( ( double ) ( this.pointSizePeriodInsideXyWindow_MILLIS - 1 ) );
        double sizeFrac = 2.0*( periodFrac < 0.5 ? periodFrac : 1.0 - periodFrac );
        return ( float ) ( this.pointSizeMinInsideXyWindow_PX + sizeFrac*( this.pointSizeMaxInsideXyWindow_PX - this.pointSizeMinInsideXyWindow_PX ) );
    }

}
