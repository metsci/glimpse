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
package com.metsci.glimpse.support.color;

import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;

public final class RGBA
{
    public static final RGBA white = new RGBA( Color.white );
    public static final RGBA WHITE = new RGBA( Color.WHITE );
    public static final RGBA lightGray = new RGBA( Color.lightGray );
    public static final RGBA LIGHT_GRAY = new RGBA( Color.LIGHT_GRAY );
    public static final RGBA gray = new RGBA( Color.gray );
    public static final RGBA GRAY = new RGBA( Color.GRAY );
    public static final RGBA darkGray = new RGBA( Color.darkGray );
    public static final RGBA DARK_GRAY = new RGBA( Color.DARK_GRAY );
    public static final RGBA black = new RGBA( Color.black );
    public static final RGBA BLACK = new RGBA( Color.BLACK );
    public static final RGBA red = new RGBA( Color.red );
    public static final RGBA RED = new RGBA( Color.RED );
    public static final RGBA pink = new RGBA( Color.pink );
    public static final RGBA PINK = new RGBA( Color.PINK );
    public static final RGBA orange = new RGBA( Color.orange );
    public static final RGBA ORANGE = new RGBA( Color.ORANGE );
    public static final RGBA yellow = new RGBA( Color.yellow );
    public static final RGBA YELLOW = new RGBA( Color.YELLOW );
    public static final RGBA green = new RGBA( Color.green );
    public static final RGBA GREEN = new RGBA( Color.GREEN );
    public static final RGBA magenta = new RGBA( Color.magenta );
    public static final RGBA MAGENTA = new RGBA( Color.MAGENTA );
    public static final RGBA cyan = new RGBA( Color.cyan );
    public static final RGBA CYAN = new RGBA( Color.CYAN );
    public static final RGBA blue = new RGBA( Color.blue );
    public static final RGBA BLUE = new RGBA( Color.BLUE );

    public final float r;
    public final float g;
    public final float b;

    public final float a;

    public RGBA( float r, float g, float b, float a )
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public RGBA( RGBA rgba )
    {
        this.r = rgba.r;
        this.g = rgba.g;
        this.b = rgba.b;
        this.a = rgba.a;
    }

    public RGBA( float[] rgba )
    {
        this.r = rgba[0];
        this.g = rgba[1];
        this.b = rgba[2];
        this.a = rgba[3];
    }

    public RGBA( float[] rgb, float a )
    {
        this.r = rgb[0];
        this.g = rgb[1];
        this.b = rgb[2];
        this.a = a;
    }

    public RGBA( Color color )
    {
        float[] rgba = color.getRGBComponents( null );

        this.r = rgba[0];
        this.g = rgba[1];
        this.b = rgba[2];
        this.a = rgba[3];
    }

    public final static RGBA fromIntRGBA( int r, int g, int b, int a )
    {
        return new RGBA( r / 255f, g / 255f, b / 255f, a / 255f );
    }

    public final static RGBA fromIntRGB( int r, int g, int b )
    {
        return new RGBA( r / 255f, g / 255f, b / 255f, 1f );
    }

    public final static RGBA fromHsb( float hue, float saturation, float brightness, float alpha )
    {
        saturation = min( 1, max( 0, saturation ) );
        brightness = min( 1, max( 0, brightness ) );

        if ( saturation == 0 )
        {
            return new RGBA( brightness, brightness, brightness, 1f );
        }

        float h = ( hue - ( float ) floor( hue ) ) * 6.0f;
        float f = h - ( float ) floor( h );
        float p = brightness * ( 1.0f - saturation );
        float q = brightness * ( 1.0f - saturation * f );
        float t = brightness * ( 1.0f - ( saturation * ( 1.0f - f ) ) );

        float r = 0f;
        float g = 0f;
        float b = 0f;
        switch ( ( int ) h )
        {
            case 0:
                r = brightness;
                g = t;
                b = p;
                break;

            case 1:
                r = q;
                g = brightness;
                b = p;
                break;

            case 2:
                r = p;
                g = brightness;
                b = t;
                break;

            case 3:
                r = p;
                g = q;
                b = brightness;
                break;

            case 4:
                r = t;
                g = p;
                b = brightness;
                break;

            case 5:
                r = brightness;
                g = p;
                b = q;
                break;
        }

        return new RGBA( r, g, b, alpha );
    }

    public final int getIntRed( )
    {
        return ( int ) ( r * 255f + 0.5f );
    }

    public final int getIntGreen( )
    {
        return ( int ) ( g * 255f + 0.5f );
    }

    public final int getIntBlue( )
    {
        return ( int ) ( b * 255f + 0.5f );
    }

    public final int getIntAlpha( )
    {
        return ( int ) ( a * 255f + 0.5f );
    }

    public final RGBA withAlpha( float a )
    {
        return new RGBA( r, g, b, a );
    }

    public final RGBA withIntAlpha( int a )
    {
        return new RGBA( r, g, b, a / 255f );
    }

    public final boolean isTransparent( )
    {
        return a < 1f;
    }

    public final float[] toFloat4( )
    {
        return new float[] { r, g, b, a };
    }

    public final float[] toFloat3( )
    {
        return new float[] { r, g, b };
    }

    public final int[] toInt4( )
    {
        return new int[] { getIntRed( ), getIntGreen( ), getIntBlue( ), getIntAlpha( ) };
    }

    public final int[] toInt3( )
    {
        return new int[] { getIntRed( ), getIntGreen( ), getIntBlue( ) };
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits( a );
        result = prime * result + Float.floatToIntBits( b );
        result = prime * result + Float.floatToIntBits( g );
        result = prime * result + Float.floatToIntBits( r );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        RGBA other = ( RGBA ) obj;
        if ( Float.floatToIntBits( a ) != Float.floatToIntBits( other.a ) ) return false;
        if ( Float.floatToIntBits( b ) != Float.floatToIntBits( other.b ) ) return false;
        if ( Float.floatToIntBits( g ) != Float.floatToIntBits( other.g ) ) return false;
        if ( Float.floatToIntBits( r ) != Float.floatToIntBits( other.r ) ) return false;
        return true;
    }
}
