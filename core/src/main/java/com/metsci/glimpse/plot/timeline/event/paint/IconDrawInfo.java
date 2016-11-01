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
package com.metsci.glimpse.plot.timeline.event.paint;

public class IconDrawInfo
{
    public Object id;
    public double positionX;
    public double positionY;
    public double scaleX;
    public double scaleY;
    public int centerX;
    public int centerY;
    public float[] color;

    public boolean isX;

    public IconDrawInfo( Object id, double positionX, double positionY, double scaleX, double scaleY, int centerX, int centerY, boolean isX, float[] color )
    {
        this.id = id;
        this.positionX = positionX;
        this.positionY = positionY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.centerX = centerX;
        this.centerY = centerY;
        this.isX = isX;
        this.color = color;
    }

    public Object getId( )
    {
        return id;
    }

    public double getPositionX( )
    {
        return positionX;
    }

    public double getPositionY( )
    {
        return positionY;
    }

    public double getScaleX( )
    {
        return scaleX;
    }

    public double getScaleY( )
    {
        return scaleY;
    }

    public int getCenterX( )
    {
        return centerX;
    }

    public int getCenterY( )
    {
        return centerY;
    }

    public boolean isX( )
    {
        return isX;
    }

    public float[] getColor( )
    {
        return color;
    }
}