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
package com.metsci.glimpse.support.colormap;

import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * A collection of common color gradients.
 *
 * @author hogye
 */
public class ColorGradients
{

    public static final ColorGradient clearToBlack = new ColorGradient( )
    {
        public void toColor( float fraction, float[] rgba )
        {
            rgba[0] = 0;
            rgba[1] = 0;
            rgba[2] = 0;
            rgba[3] = fraction;
        }
    };

    public static final ColorGradient blackToClear = new ColorGradient( )
    {
        public void toColor( float fraction, float[] rgba )
        {
            rgba[0] = 0;
            rgba[1] = 0;
            rgba[2] = 0;
            rgba[3] = 1 - fraction;
        }
    };

    public static final ColorGradient gray = new ColorGradient( )
    {
        public void toColor( float fraction, float[] rgba )
        {
            rgba[0] = fraction;
            rgba[1] = fraction;
            rgba[2] = fraction;
            rgba[3] = 1;
        }
    };

    public static final ColorGradient jet = new ColorGradient( )
    {
        public void toColor( float fraction, float[] rgba )
        {
            float x = 4 * fraction;
            int segment = ( int ) ( 8 * fraction );
            switch ( segment )
            {
                case 0:
                    rgba[0] = 0;
                    rgba[1] = 0;
                    rgba[2] = 0.5f + x;
                    break;

                case 1:
                case 2:
                    rgba[0] = 0;
                    rgba[1] = -0.5f + x;
                    rgba[2] = 1;
                    break;

                case 3:
                case 4:
                    rgba[0] = -1.5f + x;
                    rgba[1] = 1;
                    rgba[2] = 2.5f - x;
                    break;

                case 5:
                case 6:
                    rgba[0] = 1;
                    rgba[1] = 3.5f - x;
                    rgba[2] = 0;
                    break;

                default:
                    rgba[0] = 4.5f - x;
                    rgba[1] = 0;
                    rgba[2] = 0;
                    break;
            }
            rgba[3] = 1;
        }
    };

    public static final ColorGradient greenBone = new ColorGradient( )
    {

        @Override
        public void toColor( float fraction, float[] rgba )
        {
            if ( fraction < 0.5 )
            {
                rgba[0] = 0.0f;
                rgba[1] = 2 * fraction;
                rgba[2] = 0.0f;
                rgba[3] = 1.0f;
            }
            else
            {
                rgba[0] = ( fraction - 0.5f ) * 2;
                rgba[1] = 1.0f;
                rgba[2] = ( fraction - 0.5f ) * 2;
                rgba[3] = 1.0f;
            }
        }

    };

    public static final ColorGradient purpleBone = new ColorGradient( )
    {

        @Override
        public void toColor( float fraction, float[] rgba )
        {
            float[] rgb;

            if ( fraction < 0.5 )
            {
                rgb = GlimpseColor.fromColorHsb( 0.749f, 1.0f, 2 * fraction );
            }
            else
            {
                rgb = GlimpseColor.fromColorHsb( 0.749f, 1.0f - ( ( fraction - 0.5f ) * 2 ), 1.0f );
            }

            rgba[0] = rgb[0];
            rgba[1] = rgb[1];
            rgba[2] = rgb[2];
            rgba[3] = 1.0f;
        }

    };

    public static final ColorGradient reverseBone = new ColorGradient( )
    {
        public void toColor( float fraction, float[] rgba )
        {
            float x = 1f - ( float ) 0.875 * fraction;
            if ( fraction < 0.375 )
            {
                rgba[0] = x;
                rgba[1] = x;
                rgba[2] = x - fraction / 3f;
            }
            else if ( fraction < 0.75 )
            {
                rgba[0] = x;
                rgba[1] = x + 0.125f - fraction / 3f;
                rgba[2] = x - 0.125f;
            }
            else
            {
                rgba[0] = x + 0.375f - fraction * 0.5f;
                rgba[1] = x - 0.125f;
                rgba[2] = x - 0.125f;
            }
            rgba[3] = 1;
        }
    };

    public static final ColorGradient bathymetry = new ColorGradient( )
    {
        public void toColor( float fraction, float[] rgba )
        {
            float bathyMin = -10000f;
            float dataVal = ( 1.0f - fraction ) * -8000.0f;

            if ( dataVal < -5000.0 )
            {
                rgba[0] = 0.0f;
                rgba[1] = 0.0f;
                rgba[2] = ( 192.0f / ( -5000.0f - bathyMin ) * dataVal + 192.0f + 192.0f * 5000.0f / ( -5000.0f - bathyMin ) + 0.5f ) / 255.0f;
            }
            else if ( dataVal < -2500.0 )
            {
                rgba[0] = 0.0f;
                rgba[1] = ( 192.0f / 2500.0f * dataVal + 384.0f + 0.5f ) / 255.0f;
                rgba[2] = ( 63.0f / 2500.0f * dataVal + 318.0f + 0.5f ) / 255.0f;
            }
            else if ( dataVal < -500.0 )
            {
                rgba[0] = 0.0f;
                rgba[1] = ( 63.0f / 2000.0f * dataVal + 270.75f + 0.5f ) / 255.0f;
                rgba[2] = 1.0f;
            }
            else
            {
                rgba[0] = ( 128.0f / 500.0f * dataVal + 128.0f + 0.5f ) / 255.0f;
                rgba[1] = 1.0f;
                rgba[2] = 1.0f;
            }

            rgba[3] = 1;
        }
    };

    public static final ColorGradient topography = new ColorGradient( )
    {
        public void toColor( float fraction, float[] rgba )
        {
            float bathyMax = 8000f;
            float dataVal = fraction * 8000.0f;

            if ( dataVal < 1000.0 )
            {
                rgba[0] = ( 0.255f * dataVal + 0.5f ) / 255.0f;
                rgba[1] = ( 0.081f * dataVal + 174.0f + 0.5f ) / 255.0f;
                rgba[2] = 0.0f;
            }
            else if ( dataVal < 3000.0 )
            {
                rgba[0] = 1.0f;
                rgba[1] = ( -127.0f / 2000.0f * dataVal + 318.5f + 0.5f ) / 255.0f;
                rgba[2] = 0.0f;
            }
            else if ( dataVal < 5000.0 )
            {
                rgba[0] = 1.0f;
                rgba[1] = ( -128.0f / 2000.0f * dataVal + 320.0f + 0.5f ) / 255.0f;
                rgba[2] = 0.0f;
            }
            else
            {
                rgba[0] = ( 255.0f / ( 5000.0f - bathyMax ) * dataVal + 255.0f - 5000.0f * 255.0f / ( 5000.0f - bathyMax ) + 0.5f ) / 255.0f;
                rgba[1] = 0.0f;
                rgba[2] = 0.0f;
            }

            rgba[3] = 1;
        }
    };

    public static ColorGradient reverse( final ColorGradient gradient )
    {
        return new ColorGradient( )
        {
            public void toColor( float fraction, float[] rgba )
            {
                gradient.toColor( 1 - fraction, rgba );
            }
        };
    }

    private ColorGradients( )
    {
    }

}
