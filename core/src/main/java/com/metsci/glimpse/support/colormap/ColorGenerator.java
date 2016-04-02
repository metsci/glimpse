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
 * Picks colors from a given {@code ColorGradient}. This is especially useful
 * when trying to get visually distinct colors and the number of colors is not
 * known a-priori.
 *
 * <p>
 * The current implementation indexes into the {@code ColorGradient} using the
 * following sequence (and allows for a max of ~1000 colors before wrapping):
 *
 * <pre>
 * 0, 1, 0.5, 0.25, 0.75, 0.125, 0.375, 0.625, 0.875, 0.0625, ...
 * </pre>
 *
 * </p>
 *
 * @author borkholder
 */
public class ColorGenerator
{
    /**
     * The exponent for the number of color divisions at this step, e.g. 2^exp.
     */
    private int exp;

    /**
     * The current index in the color division.
     */
    private int index;

    private ColorGradient colors;

    /**
     * Creates a new {@code ColorGenerator} using the jet color gradient.
     */
    public ColorGenerator( )
    {
        this( ColorGradients.jet );
    }

    /**
     * Creates a new {@code ColorGenerator} using the given {@code ColorGradient}.
     */
    public ColorGenerator( ColorGradient colors )
    {
        this.colors = colors;

        exp = 0;
        index = 0;
    }

    /**
     * Gets the next color in the series. At some point, this will wrap and
     * restart with the first color in the sequence.
     */
    public void next( float[] rgba )
    {
        float value = -1;

        if ( index == 0 )
        {
            /*
             * This happens on the first call (result is 0)
             */
            value = 0;
            index = 1;
        }
        else if ( index == 1L << exp )
        {
            /*
             * This happens on the second call (result is 1)
             */
            value = 1;
            exp++;
        }

        /*
         * If we assigned a value (0 or 1) then use it.
         */
        if ( value >= 0 )
        {
            colors.toColor( value, rgba );
            return;
        }

        /*
         * If we need to go to the next level.
         */
        if ( index > 1L << exp )
        {
            index = 1;
            exp++;
        }

        /*
         * If we hit the maximum number of levels. Currently max is 63 which allows
         * for about 1000 colors.
         */
        if ( exp > 63 )
        {
            index = 0;
            exp = 0;
        }

        value = index / ( float ) ( 1L << exp );

        /*
         * Increment. All even indexes have been hit by the previous level.
         */
        index += 2;

        colors.toColor( value, rgba );
    }
}
