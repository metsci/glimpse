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
package com.metsci.glimpse.support.colormap;

/**
 * A {@code ColorMap} which linearly interpolates the provided
 * {@link ColorGradient} over the provided data bounds. If values
 * outside of the bounds are provided, the returned color will
 * simply saturate at one end of the {@link ColorGradient}.
 *
 * @author hogye
 */
public class ColorMapLinear implements ColorMap
{

    private final float min;
    private final float invExtent;
    private final ColorGradient gradient;

    public ColorMapLinear( float min, float max, ColorGradient gradient )
    {
        this.min = min;
        this.invExtent = 1f / ( max - min );
        this.gradient = gradient;
    }

    @Override
    public void toColor( float value, float[] rgba )
    {
        // (value - min) / (max - min)
        float unclipped = ( value - min ) * invExtent;

        if ( unclipped > 1 )
        {
            gradient.toColor( 1, rgba );
        }
        else if ( unclipped < 0 )
        {
            gradient.toColor( 0, rgba );
        }
        else
        {
            gradient.toColor( unclipped, rgba );
        }
    }

}
