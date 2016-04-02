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
package com.metsci.glimpse.util.math.fast;

/**
 * This class contains polynomial approximations to Trigonometric functions, which are much faster
 * than the Java built-in routines, but not as accurate.
 *
 * @author ellis
 */
public class PolynomialApprox
{
    private static double PI_OVER_2 = Math.PI / 2.0;

    /**
     * Fast acos using 4 term polynomial approximation from Abramowitz and Stegun, pg. 81.
     *
     * <p>Note: Accuracy to within 7 x 10^-5 radians.</p>
     *
     * @param   x
     * @return  arccos(x)
     */
    public static double acos_4( double x )
    {
        return PI_OVER_2 - asin_4( x );
    }

    /**
     * Fast acos using 8 term polynomial approximation from Abramowitz and Stegun, pg. 81.
     *
     * <p>Note: Accuracy to within 3 x 10^-8 radians.</p>
     *
     * @param   x
     * @return  arccos(x)
     */
    public static double acos( double x )
    {
        return PI_OVER_2 - asin( x );
    }

    /**
     * Fast asin using 4 term polynomial approximation from Abramowitz and Stegun, pg. 81.
     *
     * <p>Note: Accuracy to within 7 x 10^-5 radians.</p>
     *
     * @param   x
     * @return  arcsin(x)
     */
    public static double asin_4( double x )
    {
        boolean isNeg = x < 0;
        x = Math.abs( x );

        double y = 1.5707288 + ( x * ( -.2121144 + ( x * ( .0742610 + ( x * -.0187293 ) ) ) ) );
        double theta = PI_OVER_2 - ( Math.sqrt( 1.0 - x ) * y );

        if ( isNeg )
        {
            theta = -theta;
        }

        return theta;
    }

    /**
     * Fast asin using 8 term polynomial approximation from Abramowitz and Stegun, pg. 81.
     *
     * <p>Note: Accuracy to within 3 x 10^-8 radians.</p>
     *
     * @param   x
     * @return  arcsin(x)
     */
    public static double asin( double x )
    {
        boolean isNeg = x < 0;
        x = Math.abs( x );

        double y1 = x * ( -.0170881256 + ( x * ( .0066700901 + ( x * -.0012624911 ) ) ) );
        double y2 = x * ( -.0501743046 + ( x * ( .0308918810 + y1 ) ) );
        double y = 1.5707963050 + ( x * ( -.2145988016 + ( x * ( .0889789874 + y2 ) ) ) );
        double theta = PI_OVER_2 - ( Math.sqrt( 1.0 - x ) * y );

        if ( isNeg )
        {
            theta = -theta;
        }

        return theta;
    }

    public static double atan( double x )
    {
        double h = Math.sqrt( x * x + 1 );

        return asin( x / h );
    }

    public static double atan2( double y, double x )
    {
        if ( x > 0 )
        {
            return atan( y / x );
        }
        else if ( x < 0 )
        {
            if ( y >= 0 )
            {
                return Math.PI + atan( y / x );
            }
            else
            {
                return -Math.PI + atan( y / x );
            }
        }
        else
        {
            if ( y > 0 )
            {
                return Math.PI / 2;
            }
            else if ( y < 0 )
            {
                return -Math.PI / 2;
            }
            else
            {
                return 0; // undefined
            }
        }
    }
}
