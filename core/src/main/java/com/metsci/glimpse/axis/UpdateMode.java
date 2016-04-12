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
package com.metsci.glimpse.axis;

/**
 * Defines how this axis will interact with its linked parent and child axis and
 * how it responds to having its size adjusted.
 *
 * MinMax update mode indicates that the minimum and maximum axis values do not
 * change when the axis is resized. When a linked axis is updated, this axis
 * adopts the minimum and maximum values of the linked axis. This is the default
 * behavior.
 *
 * MinScale update mode indicates that the minimum axis value and the axis scale
 * (in values per pixel) do not change when the axis is resized (the maximum
 * value is updated). When a linked axis is updated, this axis adopts the minimum
 * and scale values of the linked axis.
 *
 * CenterScale update mode indicates that the center axis value and axis scale
 * (in values per pixel) do not change when the axis is resized.
 *
 * FixedPixel update mode indicates that when the axis is resized, the absolute
 * min and max are updated so that the values/pixel ratio is constant. When a
 * linked axis is updated, the axis does nothing.
 */
public enum UpdateMode
{
    MinMax(false), MinScale(true), CenterScale(true), FixedPixel(true);

    private boolean isScalePerserving;

    private UpdateMode( boolean isScalePerserving )
    {
        this.isScalePerserving = isScalePerserving;
    }

    public boolean isScalePreserving( )
    {
        return isScalePerserving;
    }
}
