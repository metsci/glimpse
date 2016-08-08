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
package com.metsci.glimpse.swt.misc;

import static com.metsci.glimpse.support.color.GlimpseColor.fromColorRgb;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class GlimpseColorSwt
{
    /////////////////////////////////////////////////////////////////////////////////////////
    //                                   FROM methods                                      //
    //                                                                                     //
    //      take outside formats and convert them to a float[] suitable for                //
    //      passing to the color setter methods of glimpse painters or glColor4fv().       //
    /////////////////////////////////////////////////////////////////////////////////////////

    public static float[] fromColorSwt( RGB color )
    {
        float r = color.red / 255.0f;
        float g = color.green / 255.0f;
        float b = color.blue / 255.0f;

        return fromColorRgb( r, g, b );
    }

    public static float[] fromColorSwt( org.eclipse.swt.graphics.Color color )
    {
        float r = color.getRed( ) / 255.0f;
        float g = color.getGreen( ) / 255.0f;
        float b = color.getBlue( ) / 255.0f;

        return fromColorRgb( r, g, b );
    }

    public static float[] fromColorSwt( RGB color, float alpha )
    {
        float[] out = fromColorSwt( color );
        out[3] = alpha;
        return out;
    }

    public static float[] fromColorSwt( org.eclipse.swt.graphics.Color color, float alpha )
    {
        float[] out = fromColorSwt( color );
        out[3] = alpha;
        return out;
    }

    public static float[] fromColorSwtSystemColor( int color )
    {
        return fromColorSwt( Display.getDefault( ).getSystemColor( color ) );
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //                                    TO methods                                       //
    //                                                                                     //
    //      take float[] arrays and convert them to outside color wrapper formats.         //
    /////////////////////////////////////////////////////////////////////////////////////////

    public static org.eclipse.swt.graphics.Color toColorSwt( float[] color )
    {
        return toColorSwt( Display.getDefault( ), color );
    }

    // when finished with the provided org.eclipse.swt.graphics.Color object, its
    // dispose() method must be called because it holds native resources
    public static org.eclipse.swt.graphics.Color toColorSwt( Display display, float[] color )
    {
        int r = ( int ) ( color[0] * 255 );
        int g = ( int ) ( color[1] * 255 );
        int b = ( int ) ( color[2] * 255 );

        return new org.eclipse.swt.graphics.Color( display, r, g, b );
    }
}
