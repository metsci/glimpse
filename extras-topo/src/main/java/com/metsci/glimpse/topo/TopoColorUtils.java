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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.core.support.color.GlimpseColor.fromColorHex;
import static com.metsci.glimpse.core.support.colormap.ColorGradientUtils.newColorGradient;
import static com.metsci.glimpse.core.support.colormap.ColorGradientUtils.newColorTable;
import static com.metsci.glimpse.core.support.colormap.ColorGradientUtils.vc;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.listPlus;

import java.text.ParseException;

import com.google.common.collect.ImmutableList;
import com.metsci.glimpse.core.gl.texture.ColorTexture1D;
import com.metsci.glimpse.core.support.colormap.ColorGradient;
import com.metsci.glimpse.core.support.colormap.ColorGradientUtils.ValueAndColor;

public class TopoColorUtils
{

    public static final float bathyColormapMinValue = -11000f;
    public static final float topoColormapMaxValue = +8000f;

    public static final ColorGradient bathyColorGradient = newColorGradient( bathyColormapMinValue,
                                                                             -0f,
                                                                             vc( -10000f,  0.00f, 0.00f, 0.00f  ),
                                                                             vc(  -8000f,  0.12f, 0.44f, 0.60f  ),
                                                                             vc(  -7000f,  0.32f, 0.62f, 0.80f  ),
                                                                             vc(  -6000f,  0.40f, 0.72f, 0.90f  ),
                                                                             vc(  -5000f,  0.53f, 0.79f, 0.95f  ),
                                                                             vc(     -0f,  0.84f, 0.92f, 1.00f  ) );

    public static final ColorGradient topoColorGradient = newColorGradient( +0f,
                                                                            topoColormapMaxValue,
                                                                            vc(     +0f,  0.36f, 0.63f, 0.31f  ),
                                                                            vc(    +50f,  0.42f, 0.70f, 0.38f  ),
                                                                            vc(   +750f,  0.49f, 0.76f, 0.45f  ),
                                                                            vc(  +3000f,  0.67f, 0.90f, 0.65f  ),
                                                                            vc(  +5500f,  0.90f, 0.95f, 0.90f  ),
                                                                            vc(  +6500f,  0.99f, 0.99f, 0.99f  ) );

    /**
     * Colors as a function of bathymetry only, not including topography.
     * These colors, when used as a step function and not a gradient, provide
     * good visual separation for depths in a blue hue.
     *
     * These colors are based on an existing popular colormap.
     *
     * Particularly useful in {@link ShadedReliefTiledPainter}.
     */
    public static final ImmutableList<ValueAndColor> bathyColorsStepped;
    static
    {
        try
        {
            bathyColorsStepped = ImmutableList.of(
                    vc( -8_000f, fromColorHex( "#3c6e98" ) ),
                    vc( -4_000f, fromColorHex( "#6499c1" ) ),
                    vc( -2_000f, fromColorHex( "#76a5cf" ) ),
                    vc( -1_000f, fromColorHex( "#81acd6" ) ),
                    vc( -500f, fromColorHex( "#a3c9e6" ) ),
                    vc( -100f, fromColorHex( "#b0cee8" ) ),
                    vc( -20f, fromColorHex( "#bbd9f0" ) ),
                    vc( 0, fromColorHex( "#c9dfef" ) ),
                    vc( topoColormapMaxValue, fromColorHex( "#c9dfef" ) ) );
        }
        catch ( ParseException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Colors as a function of bathymetry and topography. The bathymetry colors
     * come from {@link #bathyColorsStepped}.
     *
     * Particularly useful in {@link ShadedReliefTiledPainter}.
     */
    public static final ImmutableList<ValueAndColor> topoColorsStepped = listPlus( bathyColorsStepped,
                    vc( +0f, 0.36f, 0.63f, 0.31f ),
                    vc( +50f, 0.42f, 0.70f, 0.38f ),
                    vc( +750f, 0.49f, 0.76f, 0.45f ),
                    vc( +3000f, 0.67f, 0.90f, 0.65f ),
                    vc( +5500f, 0.90f, 0.95f, 0.90f ),
                    vc( +6500f, 0.99f, 0.99f, 0.99f ) );

    public static ColorTexture1D bathyColorTable( )
    {
        return newColorTable( bathyColorGradient, 1024 );
    }

    public static ColorTexture1D topoColorTable( )
    {
        return newColorTable( topoColorGradient, 1024 );
    }

}
