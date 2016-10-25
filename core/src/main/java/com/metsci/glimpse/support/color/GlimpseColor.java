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

import java.awt.Color;
import java.text.ParseException;

import javax.media.opengl.GL;

import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;

/**
 * A simple color utility class for converting various color wrapper classes/formats
 * to and from the Glimpse/OpenGl cannonical format (a float[] array with four elements
 * for red, green, blue, and alpha in that order). Glimpse uses this format for its
 * color setters partly because OpenGl uses it, and partly in order to reduce reliance
 * on outside color wrappers (of which there are many, and choosing one seems arbitrary).
 *
 * @author ulman
 */
public class GlimpseColor
{

    /////////////////////////////////////////////////////////////////////////////////////////
    //                                   FROM methods                                      //
    //                                                                                     //
    //      take outside formats and convert them to a float[] suitable for                //
    //      passing to the color setter methods of glimpse painters or glColor4fv().       //
    /////////////////////////////////////////////////////////////////////////////////////////

    public static float[] fromColorRgb( float r, float g, float b )
    {
        return new float[] { r, g, b, 1.0f };
    }

    public static float[] fromColorRgba( float r, float g, float b, float a )
    {
        return new float[] { r, g, b, a };
    }

    public static float[] fromColorRgb( int r, int g, int b )
    {
        float fr = r / 255.0f;
        float fg = g / 255.0f;
        float fb = b / 255.0f;

        return new float[] { fr, fg, fb, 1.0f };
    }

    public static float[] fromColorRgba( int r, int g, int b, int a )
    {
        float fr = r / 255.0f;
        float fg = g / 255.0f;
        float fb = b / 255.0f;
        float fa = a / 255.0f;

        return new float[] { fr, fg, fb, fa };
    }

    public static float[] fromColorHsb( float hue, float saturation, float brightness )
    {
        int rgb = Color.HSBtoRGB( hue, saturation, brightness );
        int r = rgb >> 16 & 0xff;
        int g = rgb >> 8 & 0xff;
        int b = rgb & 0xff;

        return fromColorRgb( r, g, b );
    }

    public static float[] fromColorAwt( java.awt.Color color )
    {
        float r = color.getRed( ) / 255.0f;
        float g = color.getGreen( ) / 255.0f;
        float b = color.getBlue( ) / 255.0f;
        float a = color.getAlpha( ) / 255.0f;

        return fromColorRgba( r, g, b, a );
    }

    public static float[] fromColorHex( String color ) throws ParseException
    {
        color = color.trim( );

        if ( color == null || ( color.length( ) != 7 && color.length( ) != 9 ) ) throw new ParseException( "Color string must be of the form: #RRGGBB or #RRGGBBAA", 0 );

        int r = Integer.decode( "#" + color.substring( 1, 3 ) );
        int g = Integer.decode( "#" + color.substring( 3, 5 ) );
        int b = Integer.decode( "#" + color.substring( 5, 7 ) );
        int a = color.length( ) == 9 ? Integer.decode( "#" + color.substring( 7, 9 ) ) : 255;

        return fromColorRgba( r, g, b, a );
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //                                    TO methods                                       //
    //                                                                                     //
    //      take float[] arrays and convert them to outside color wrapper formats.         //
    /////////////////////////////////////////////////////////////////////////////////////////

    public static java.awt.Color toColorAwt( float[] color )
    {
        return new java.awt.Color( color[0], color[1], color[2], color[3] );
    }

    public static String toColorHex( float[] color )
    {
        long value = ( int ) ( color[0] * 255 );
        value = value * 256 + ( int ) ( color[1] * 255 );
        value = value * 256 + ( int ) ( color[2] * 255 );
        value = value * 256 + ( int ) ( color[3] * 255 );
        return String.format( "#%08x", value );
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //                            Convenience methods                                      //
    //                                                                                     //
    //miscellaneous adapter methods for using glimpse float[] color arrays with other APIs.//
    /////////////////////////////////////////////////////////////////////////////////////////

    // JOGL doesn't provide a glClearColor method that takes an array, this is provided as a convenience
    public static void glClearColor( GL gl, float[] color )
    {
        gl.glClearColor( color[0], color[1], color[2], color[3] );
    }

    // TextRenderer doesn't provide a glClearColor method that takes an array, this is provided as a convenience
    public static void setColor( TextRenderer renderer, float[] color )
    {
        renderer.setColor( color[0], color[1], color[2], color[3] );
    }

    // a toString method to convert colors into a format parseable by com.metsci.util.params.param.StringArrayParam
    public static String getString( float[] color )
    {
        if ( color == null || color.length == 0 ) return "";

        StringBuilder s = new StringBuilder( );
        for ( int i = 0; i < color.length - 1; i++ )
        {
            s.append( color[i] ).append( "," );
        }
        s.append( color[color.length - 1] );

        return s.toString( );
    }

    public static float[] addRgb( float[] color, float value )
    {
        return add( color, fromColorRgba( value, value, value, 0.0f ) );
    }

    public static float[] add( float[] color1, float[] color2 )
    {
        float[] newColor = new float[4];

        for ( int i = 0; i < 4; i++ )
        {
            float color = color1[i] + color2[i];

            if ( color < 0.0f ) color = 0.0f;
            if ( color > 1.0f ) color = 1.0f;

            newColor[i] = color;
        }

        return newColor;
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //                                    GET methods                                      //
    //                                                                                     //
    //            provides common colors in glimpse float[] array format.                  //
    /////////////////////////////////////////////////////////////////////////////////////////

    public static float[] getRed( )
    {
        return new float[] { 1.0f, 0.0f, 0.0f, 1.0f };
    }

    public static float[] getRed( float alpha )
    {
        return new float[] { 1.0f, 0.0f, 0.0f, alpha };
    }

    public static float[] getGreen( )
    {
        return new float[] { 0.0f, 1.0f, 0.0f, 1.0f };
    }

    public static float[] getGreen( float alpha )
    {
        return new float[] { 0.0f, 1.0f, 0.0f, alpha };
    }

    public static float[] getYellow( )
    {
        return new float[] { 1.0f, 1.0f, 0.0f, 1.0f };
    }

    public static float[] getYellow( float alpha )
    {
        return new float[] { 1.0f, 1.0f, 0.0f, alpha };
    }

    public static float[] getCyan( )
    {
        return new float[] { 0.0f, 1.0f, 1.0f, 1.0f };
    }

    public static float[] getCyan( float alpha )
    {
        return new float[] { 0.0f, 1.0f, 1.0f, alpha };
    }

    public static float[] getMagenta( )
    {
        return new float[] { 1.0f, 0.0f, 1.0f, 1.0f };
    }

    public static float[] getMagenta( float alpha )
    {
        return new float[] { 1.0f, 0.0f, 1.0f, alpha };
    }

    public static float[] getBlue( )
    {
        return new float[] { 0.0f, 0.0f, 1.0f, 1.0f };
    }

    public static float[] getBlue( float alpha )
    {
        return new float[] { 0.0f, 0.0f, 1.0f, alpha };
    }

    public static float[] getWhite( )
    {
        return new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    }

    public static float[] getWhite( float alpha )
    {
        return new float[] { 1.0f, 1.0f, 1.0f, alpha };
    }

    public static float[] getBlack( )
    {
        return new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
    }

    public static float[] getBlack( float alpha )
    {
        return new float[] { 0.0f, 0.0f, 0.0f, alpha };
    }

    public static float[] getGray( )
    {
        return new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
    }

    public static float[] getGray( float alpha )
    {
        return new float[] { 0.5f, 0.5f, 0.5f, alpha };
    }
}
