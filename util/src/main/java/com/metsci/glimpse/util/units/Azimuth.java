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
package com.metsci.glimpse.util.units;

/**
 * @author hogye
 */
public final class Azimuth
{
    // To make conversions as accurate as possible, first define all the
    // factors that can be written exactly as literals, then compute other
    // factors based on the exact ones. Be careful about computing factors
    // based on others that are not literal-exact.
    //
    private static final double pi = Math.PI;
    private static final double degreesToRadians = pi / 180d;
    private static final double radiansToDegrees = 1d / degreesToRadians;
    private static final double piOverTwo = pi / 2d;

    // cardinal directions for convenience
    public static final double east = 0d;
    public static final double north = pi / 2d;
    public static final double west = pi;
    public static final double south = 3d * pi / 2d;

    // additional directions for convenience
    public static final double northeast = pi / 4d;
    public static final double northwest = 3d * pi / 4d;
    public static final double southwest = 5d * pi / 4d;
    public static final double southeast = 7d * pi / 4d;

    public static double fromMathRad( double radians )
    {
        return radians;
    }

    public static double fromMathDeg( double degrees )
    {
        return degrees * degreesToRadians;
    }

    public static double fromNavRad( double radians )
    {
        return piOverTwo - radians;
    }

    public static double fromNavDeg( double degrees )
    {
        return piOverTwo - degreesToRadians * degrees;
    }

    public static double toMathRad( double su )
    {
        return su;
    }

    public static double toMathDeg( double su )
    {
        return su * radiansToDegrees;
    }

    public static double toNavRad( double su )
    {
        return piOverTwo - su;
    }

    public static double toNavDeg( double su )
    {
        return radiansToDegrees * ( piOverTwo - su );
    }

    public static double navDegToMathRad( double navDeg )
    {
        return toMathRad( fromNavDeg( navDeg ) );
    }

    public static double navRadToMathRad( double navRad )
    {
        return toMathRad( fromNavRad( navRad ) );
    }

    public static double navDegToMathDeg( double navDeg )
    {
        return toMathDeg( fromNavDeg( navDeg ) );
    }

    public static double navRadToMathDeg( double navRad )
    {
        return toMathDeg( fromNavRad( navRad ) );
    }
}
