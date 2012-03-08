/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.charts.vector.display;

import java.awt.Color;

public class ColorPalette
{
    private Color waterColor;
    private Color landColor;
    private Color coverageLineColor;
    private Color depthLineColor;
    private Color coastlineLineColor;

    public static ColorPalette createDefaultColorPalette( )
    {
        return new ColorPalette( new Color( 191, 219, 255 ), new Color( 254, 255, 215 ), Color.yellow, Color.gray, Color.black );
    }

    public static ColorPalette createGrayscaleColorPalette( )
    {
        ColorPalette colorPalette = createDefaultColorPalette( );

        //@formatter:off
        return new ColorPalette( grayScalize( colorPalette.waterColor ),
                                 grayScalize( colorPalette.landColor ),
                                 grayScalize( colorPalette.coverageLineColor ),
                                 grayScalize( colorPalette.depthLineColor ),
                                 grayScalize( colorPalette.coverageLineColor ) );
        //@formatter:on
    }

    // bright ugly contrasting colors
    public static ColorPalette createDebugColorPalette( )
    {
        return new ColorPalette( Color.green, Color.orange, Color.yellow, Color.gray, Color.black );
    }

    private static Color grayScalize( Color color )
    {
        // gray scale algorithm found on every google - in fact every google hit used the same one.
        float[] colorComp = color.getComponents( null );
        float num = colorComp[0] * .2989f + colorComp[1] * .587f + colorComp[2] * .1140f;
        return new Color( num, num, num, colorComp[3] );
    }

    public ColorPalette( Color waterColor, Color landColor, Color coverageLineColor, Color depthLineColor, Color coastlineLineColor )
    {
        this.waterColor = waterColor;
        this.landColor = landColor;
        this.coverageLineColor = coverageLineColor;
        this.depthLineColor = depthLineColor;
        this.coastlineLineColor = coastlineLineColor;
    }

    public float[] getWaterColor( )
    {
        return waterColor.getComponents( null );
    }

    public float[] getWaterColor( float alpha )
    {
        float[] f = waterColor.getComponents( null );
        f[3] = alpha;
        return f;
    }

    public float[] getLandColor( )
    {
        return landColor.getComponents( null );
    }

    public float[] getLandColor( float alpha )
    {
        float[] f = landColor.getComponents( null );
        f[3] = alpha;
        return f;
    }

    public float[] getCoverageLineColor( )
    {
        return coverageLineColor.getComponents( null );
    }

    public float[] getCoverageLineColor( float alpha )
    {
        float[] f = coverageLineColor.getComponents( null );
        f[3] = alpha;
        return f;
    }

    public float[] getDepthLineColor( )
    {
        return depthLineColor.getComponents( null );
    }

    public float[] getDepthLineColor( float alpha )
    {
        float[] f = depthLineColor.getComponents( null );
        f[3] = alpha;
        return f;
    }

    public float[] getCoastlineLineColor( )
    {
        return coastlineLineColor.getComponents( null );
    }

    public float[] getCoastlineLineColor( float alpha )
    {
        float[] f = coastlineLineColor.getComponents( null );
        f[3] = alpha;
        return f;
    }
}
