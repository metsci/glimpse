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

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.io.StreamOpener;
import com.metsci.glimpse.util.primitives.FloatsArray;

/**
 * A collection of common color gradients.
 *
 * @author hogye
 */
public class ColorGradients
{

    private static final Logger logger = Logger.getLogger( ColorGradients.class.getName( ) );

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

    // matplotlib colormaps: http://matplotlib.org/examples/color/colormaps_reference.html
    public static final ColorGradient wistia = fromCSV( "colormap/matplotlib/wistia.csv" );
    public static final ColorGradient viridis = fromCSV( "colormap/matplotlib/viridis.csv" );
    public static final ColorGradient terrain = fromCSV( "colormap/matplotlib/terrain.csv" );
    public static final ColorGradient summer = fromCSV( "colormap/matplotlib/summer.csv" );
    public static final ColorGradient spring = fromCSV( "colormap/matplotlib/spring.csv" );
    public static final ColorGradient spectral = fromCSV( "colormap/matplotlib/spectral.csv" );
    public static final ColorGradient seismic = fromCSV( "colormap/matplotlib/seismic.csv" );
    public static final ColorGradient rainbow = fromCSV( "colormap/matplotlib/rainbow.csv" );
    public static final ColorGradient prism = fromCSV( "colormap/matplotlib/prism.csv" );
    public static final ColorGradient plasma = fromCSV( "colormap/matplotlib/plasma.csv" );
    public static final ColorGradient oranges = fromCSV( "colormap/matplotlib/oranges.csv" );
    public static final ColorGradient ocean = fromCSV( "colormap/matplotlib/ocean.csv" );
    public static final ColorGradient magma = fromCSV( "colormap/matplotlib/magma.csv" );
    public static final ColorGradient inferno = fromCSV( "colormap/matplotlib/inferno.csv" );
    public static final ColorGradient hot = fromCSV( "colormap/matplotlib/hot.csv" );
    public static final ColorGradient flag = fromCSV( "colormap/matplotlib/flag.csv" );
    public static final ColorGradient copper = fromCSV( "colormap/matplotlib/copper.csv" );
    public static final ColorGradient coolwarm = fromCSV( "colormap/matplotlib/coolwarm.csv" );
    public static final ColorGradient cool = fromCSV( "colormap/matplotlib/cool.csv" );
    public static final ColorGradient autumn = fromCSV( "colormap/matplotlib/autumn.csv" );
    public static final ColorGradient accent = fromCSV( "colormap/matplotlib/accent.csv" );

    public static ColorGradient nColorFade( final List<float[]> colors )
    {
        return new ColorGradient( )
        {
            public void toColor( float fraction, float[] rgba )
            {
                double val = fraction * ( colors.size( ) - 1 );
                int index = fraction != 1 ? ( int ) val : colors.size( ) - 2;

                rgba[0] = ( float ) ( colors.get( index )[0] * ( 1 - val + index ) + colors.get( index + 1 )[0] * ( val - index ) );
                rgba[1] = ( float ) ( colors.get( index )[1] * ( 1 - val + index ) + colors.get( index + 1 )[1] * ( val - index ) );
                rgba[2] = ( float ) ( colors.get( index )[2] * ( 1 - val + index ) + colors.get( index + 1 )[2] * ( val - index ) );
                rgba[3] = ( float ) ( colors.get( index )[3] * ( 1 - val + index ) + colors.get( index + 1 )[3] * ( val - index ) );
            }
        };
    }

    public static ColorGradient customMap( final List<float[]> colors )
    {
        return new ColorGradient( )
        {
            public void toColor( float fraction, float[] rgba )
            {
                int index = fraction != 1 ? ( int ) ( fraction * ( colors.size( ) ) ) : colors.size( ) - 1;

                rgba[0] = colors.get( index )[0];
                rgba[1] = colors.get( index )[1];
                rgba[2] = colors.get( index )[2];
                rgba[3] = colors.get( index )[3];
            }
        };
    }

    public static ColorGradient brighten( final ColorGradient gradient, final double beta )
    {
        return new ColorGradient( )
        {
            public void toColor( float fraction, float[] rgba )
            {
                gradient.toColor( fraction, rgba );

                if ( beta > 0 )
                {
                    for ( int k = 0; k < rgba.length; k++ )
                    {
                        rgba[k] = ( float ) Math.pow( rgba[k], 1 - beta );
                    }
                }
                else
                {
                    for ( int k = 0; k < rgba.length; k++ )
                    {
                        rgba[k] = ( float ) Math.pow( rgba[k], 1 / ( 1 + beta ) );
                    }
                }
            }
        };
    }

    public static ColorGradient lighten( final ColorGradient gradient, final double beta )
    {
        return new ColorGradient( )
        {
            public void toColor( float fraction, float[] rgba )
            {
                gradient.toColor( fraction, rgba );

                //convert rgb to hsl
                float xMax = Math.max( Math.max( rgba[0], rgba[1] ), rgba[2] );
                float xMin = Math.min( Math.min( rgba[0], rgba[1] ), rgba[2] );
                float light = ( xMax + xMin ) / 2, sat = 0, hue = 0, temp2 = 0;

                if ( xMin == xMax )
                {
                    sat = hue = 0;
                }
                else if ( light < .5 )
                {
                    sat = ( xMax - xMin ) / ( xMax + xMin );
                }
                else
                {
                    sat = ( xMax - xMin ) / ( 2 - xMax - xMin );
                }

                if ( rgba[0] == xMax ) hue = ( rgba[1] - rgba[2] ) / ( xMax - xMin );
                if ( rgba[1] == xMax ) hue = 2 + ( rgba[2] - rgba[0] ) / ( xMax - xMin );
                if ( rgba[2] == xMax ) hue = 4 + ( rgba[0] - rgba[1] ) / ( xMax - xMin );
                if ( hue < 0 ) hue = hue + 6;

                //increase light value
                light = ( float ) ( light + beta );

                //convert back to rgb from hsl
                if ( sat == 0 )
                {
                    rgba[0] = rgba[1] = rgba[2] = light;
                }
                else if ( light < .5 )
                {
                    temp2 = light * ( 1 + sat );
                }
                else
                {
                    temp2 = light + sat - light * sat;
                }

                float temp1 = 2 * light - temp2;
                hue = hue / 6;

                for ( int k = 0; k < 3; k++ )
                {
                    float temp3;
                    if ( k == 0 )
                    {
                        temp3 = ( float ) ( hue + 1.0 / 3 );
                        if ( temp3 > 1 ) temp3 = temp3 - 1;
                    }
                    else if ( k == 1 )
                    {
                        temp3 = hue;
                    }
                    else
                    {
                        temp3 = ( float ) ( hue - 1.0 / 3 );
                        if ( temp3 < 0 ) temp3 = temp3 + 1;
                    }

                    if ( temp3 < 1.0 / 6 )
                    {
                        rgba[k] = temp1 + ( temp2 - temp1 ) * 6 * temp3;
                    }
                    else if ( temp3 < .5 )
                    {
                        rgba[k] = temp2;
                    }
                    else if ( temp3 < 2.0 / 3 )
                    {
                        rgba[k] = ( float ) ( temp1 + ( temp2 - temp1 ) * ( 2.0 / 3 - temp3 ) * 6 );
                    }
                    else
                    {
                        rgba[k] = temp1;
                    }
                }
            }
        };
    }

    public static ColorGradient changeAlpha( final ColorGradient gradient, final float alpha )
    {
        return new ColorGradient( )
        {
            public void toColor( float fraction, float[] rgba )
            {
                gradient.toColor( fraction, rgba );
                rgba[3] = alpha;
            }
        };
    }

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

    protected static ColorGradient fromCSV( String file )
    {
        FloatsArray f = new FloatsArray( );
        String line = null;
        try ( BufferedReader reader = new BufferedReader( new InputStreamReader( StreamOpener.fileThenResource.openForRead( file ) ) ) )
        {
            while ( ( line = reader.readLine( ) ) != null )
            {
                String[] tokens = line.split( "," );
                f.append( Float.parseFloat( tokens[0] ) );
                f.append( Float.parseFloat( tokens[1] ) );
                f.append( Float.parseFloat( tokens[2] ) );
            }

            f.compact( );

            return new ColorGradientArray( f.a );
        }
        catch ( Exception e )
        {
            logWarning( logger, "Unable to load ColorGradient: %s", e, file );
            return new ColorGradientArray( new float[] { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f } );
        }
    }

    private ColorGradients( )
    {
    }

}
