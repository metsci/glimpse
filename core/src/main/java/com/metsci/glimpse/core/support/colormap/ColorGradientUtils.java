/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.support.colormap;

import static com.metsci.glimpse.util.GeneralUtils.clamp;
import static java.lang.Math.min;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOf;

import com.metsci.glimpse.core.gl.texture.ColorTexture1D;
import com.metsci.glimpse.core.support.color.RGBA;

public class ColorGradientUtils
{

    /**
     * See {@link #newColorGradient(float, float, ValueAndColor...)}.
     */
    public static class ValueAndColor
    {
        public final float v;
        public final float r;
        public final float g;
        public final float b;
        public final float a;

        public ValueAndColor( float v, float r, float g, float b, float a )
        {
            this.v = v;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }

    /**
     * See {@link #newColorGradient(float, float, ValueAndColor...)}.
     */
    public static ValueAndColor vc( float v, float r, float g, float b )
    {
        return vc( v, r, g, b, 1f );
    }

    /**
     * See {@link #newColorGradient(float, float, ValueAndColor...)}.
     */
    public static ValueAndColor vc( float v, float r, float g, float b, float a )
    {
        return new ValueAndColor( v, r, g, b, a );
    }

    /**
     * Like {@link #newColorGradient(float, float, ValueAndColor...)}, but without the value scaling, so
     * values are always interpreted as fractions on [0,1].
     */
    public static ColorGradient newColorGradient( ValueAndColor... vcs )
    {
        return newColorGradient( 0f, 1f, vcs );
    }

    /**
     * Allows a {@link ColorGradient} to be described as a multi-segment gradient, by specifying a list of
     * values, and a color for each value. For example, a multi-segment gradient for topography could be
     * specified like this:
     * <p>
     * <blockquote>
     * <pre>
     * {@code
     * ColorGradient topoColorGradient = newColorGradient( +0f,
     *                                                     +8000f,
     *                                                     vc(     +0f,  0.36f, 0.63f, 0.31f  ),
     *                                                     vc(    +50f,  0.42f, 0.70f, 0.38f  ),
     *                                                     vc(   +750f,  0.49f, 0.76f, 0.45f  ),
     *                                                     vc(  +3000f,  0.67f, 0.90f, 0.65f  ),
     *                                                     vc(  +5500f,  0.90f, 0.95f, 0.90f  ),
     *                                                     vc(  +6500f,  0.99f, 0.99f, 0.99f  ) );
     * }
     * </pre>
     * </blockquote>
     * <p>
     * In some cases it is more natural to specify scaled values, instead of fractions in [0,1]. The above
     * example specifies values as topographical altitudes in feet, which fall roughly in [0,8000]. The first
     * two arguments indicate how values are scaled.
     * <p>
     * Discontinuities can be introduced by passing two consecutive entries with the same value, but different
     * colors.
     * <p>
     * Values must be in ascending order; otherwise, behavior is undefined.
     */
    public static ColorGradient newColorGradient( float vMin, float vMax, ValueAndColor... vcs )
    {
        float[] fracs = new float[ vcs.length ];
        float[][] rgbas = new float[ vcs.length ][ 4 ];
        for ( int i = 0; i < vcs.length; i++ )
        {
            fracs[i] = ( vcs[i].v - vMin ) / ( vMax - vMin );
            rgbas[i][0] = vcs[i].r;
            rgbas[i][1] = vcs[i].g;
            rgbas[i][2] = vcs[i].b;
            rgbas[i][3] = vcs[i].a;
        }

        return ( frac, result ) ->
        {
            int iMax = clamp( indexAfter( fracs, frac ), 0, fracs.length - 1 );
            int iMin = clamp( iMax - 1, 0, fracs.length - 1 );
            mixRgba( fracs[iMin], rgbas[iMin], fracs[iMax], rgbas[iMax], frac, result );
        };
    }

    public static int indexAfter( float[] a, float x )
    {
        int n = a.length;

        int i = binarySearch( a, 0, n, x );

        // Exact value not found
        if ( i < 0 ) return ( -i - 1 );

        // If the exact value was found, find the value's
        // last occurrence
        for ( int j = i + 1; j < n; j++ )
        {
            if ( a[j] > x ) return j;
        }
        return n;
    }

    public static void mixRgba( float vMin, float[] rgbaMin, float vMax, float[] rgbaMax, float v, float[] result )
    {
        float wMax = clamp( ( v - vMin ) / ( vMax - vMin ), 0f, 1f );
        float wMin = 1f - wMax;
        result[0] = wMin*rgbaMin[0] + wMax*rgbaMax[0];
        result[1] = wMin*rgbaMin[1] + wMax*rgbaMax[1];
        result[2] = wMin*rgbaMin[2] + wMax*rgbaMax[2];
        result[3] = wMin*rgbaMin[3] + wMax*rgbaMax[3];
    }

    public static ColorTexture1D newColorTable( ColorGradient gradient, int numLevels )
    {
        ColorTexture1D colorTable = new ColorTexture1D( numLevels );
        colorTable.setColorGradient( gradient );
        return colorTable;
    }

    public static ColorGradient buildCellColorGradient( final RGBA[] cellColors )
    {
        return new ColorGradient( )
        {
            RGBA[] c = copyOf( cellColors, cellColors.length );

            @Override
            public void toColor( float fraction, float[] rgba )
            {
                int index = min( c.length - 1, ( int ) ( c.length * fraction ) );

                rgba[0] = c[index].r;
                rgba[1] = c[index].g;
                rgba[2] = c[index].b;
                rgba[3] = c[index].a;
            }
        };
    }

}
