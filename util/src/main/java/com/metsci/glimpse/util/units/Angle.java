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
public class Angle
{
    private static final double twoPi = 2.0 * Math.PI;

    // To make conversions as accurate as possible, first define all the
    // factors that can be written exactly as literals, then compute other
    // factors based on the exact ones. Be careful about computing factors
    // based on others that are not literal-exact.
    //
    public static final double degreesToRadians = Math.PI / 180.0;
    public static final double radiansToDegrees = 1.0 / degreesToRadians;
    public static final double rightAngle = fromDeg( 90 );

    public static double fromDeg( double degrees )
    {
        return degrees * degreesToRadians;
    }

    public static double[] fromDeg( double... degrees )
    {
        return multiply( degrees, degreesToRadians );
    }

    public static double fromRad( double radians )
    {
        return radians;
    }

    public static double[] fromRad( double... radians )
    {
        return multiply( radians, 1 );
    }

    public static double toDeg( double su )
    {
        return su * radiansToDegrees;
    }

    public static double[] toDeg( double... su )
    {
        return multiply( su, radiansToDegrees );
    }

    public static double toRad( double su )
    {
        return su;
    }

    public static double[] toRad( double... su )
    {
        return multiply( su, 1 );
    }

    /**
     * Converts angle from degrees to radians.
     *
     * NOTE: The angle returned is not normalized.
     */
    public static double degreesToRadians( double degrees )
    {
        return degrees * degreesToRadians;
    }

    /**
     * Converts angle from degrees to radians.
     *
     * NOTE: The angle returned is not normalized.
     */
    public static double[] degreesToRadians( double... degrees )
    {
        return multiply( degrees, degreesToRadians );
    }

    /**
     * Converts angle from radians to degrees.
     *
     * NOTE: The angle returned is not normalized.
     */
    public static double radiansToDegrees( double radians )
    {
        return radians * radiansToDegrees;
    }

    /**
     * Converts angle from radians to degrees.
     *
     * NOTE: The angle returned is not normalized.
     */
    public static double[] radiansToDegrees( double... radians )
    {
        return multiply( radians, radiansToDegrees );
    }

    /**
     * Normalizes angle to be from 0 degrees inclusive to 360 degrees exclusive.
     *
     * <p>Note: For extremely large arguments see
     * http://www.derekroconnor.net/Software/Ng--ArgReduction.pdf</p>
     */
    public static double normalizeAngle360( double degrees )
    {
        degrees = degrees % 360;

        return ( degrees < 0 ) ? ( degrees + 360 ) : degrees;
    }

    /**
     * Normalizes angle to be from -180 degrees exclusive to 180 degrees inclusive.
     *
     * <p>Note: For extremely large arguments see
     * http://www.derekroconnor.net/Software/Ng--ArgReduction.pdf</p>
     */
    public static double normalizeAngle180( double degrees )
    {
        degrees = normalizeAngle360( degrees );

        return ( degrees > 180 ) ? ( degrees - 360 ) : degrees;
    }

    /**
     * Normalizes angle to be from 0 radians inclusive to 2Pi radians exclusive.
     *
     * <p>Note: For extremely large arguments see
     * http://www.derekroconnor.net/Software/Ng--ArgReduction.pdf</p>
     */
    public static double normalizeAngleTwoPi( double radians )
    {
        radians = radians % twoPi;

        return ( radians < 0 ) ? ( radians + twoPi ) : radians;
    }

    /**
     * Normalizes angle to be from -Pi radians exclusive to Pi radians inclusive.
     *
     * <p>Note: For extremely large arguments see
     * http://www.derekroconnor.net/Software/Ng--ArgReduction.pdf</p>
     */
    public static double normalizeAnglePi( double radians )
    {
        radians = normalizeAngleTwoPi( radians );

        return ( radians > Math.PI ) ? ( radians - twoPi ) : radians;
    }

    private static double[] multiply( double[] array, double factor )
    {
        double[] result = new double[array.length];
        for ( int i = 0; i < result.length; i++ )
            result[i] = factor * array[i];
        return result;
    }

}
