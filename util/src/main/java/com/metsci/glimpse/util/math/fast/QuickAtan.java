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
 * Faster but less accurate than Math.atan. Gives max relative error of 2.7e-7
 * and is about 4 times faster (server VM) than Math.atan.
 *
 * @author ellis
 */
public class QuickAtan
{
    private static final double ROOT_THREE = Math.sqrt( 3.0 );
    private static final double ONE_OVER_ROOT_THREE = 1.0 / Math.sqrt( 3.0 );
    private static final double TWO_MINUS_ROOT_THREE = 2.0 - Math.sqrt( 3.0 );
    private static final double TWO_PLUS_ROOT_THREE = 2.0 + Math.sqrt( 3.0 );

    private QuickAtan( )
    {
    }

    public static double eval( double x )
    {
        if ( x < 0.0 ) return -eval( -x );

        if ( x >= TWO_PLUS_ROOT_THREE )
        {
            // x in range [2+sqrt(3),+inf]
            return ( Math.PI * 0.5 ) - eval0( 1.0 / x );
        }
        else if ( x >= ROOT_THREE )
        {
            // x in range [sqrt(3),2+sqrt(3))
            return ( Math.PI / 3.0 ) + eval0( ( x - ROOT_THREE ) / ( x * ROOT_THREE + 1.0 ) );
        }
        else if ( x >= 1.0 )
        {
            // x in range [1,sqrt(3))
            return ( Math.PI / 3.0 ) - eval0( ( ROOT_THREE - x ) / ( x * ROOT_THREE + 1.0 ) );
        }
        else if ( x >= ONE_OVER_ROOT_THREE )
        {
            // x in range [1/sqrt(3),1)
            return ( Math.PI / 6.0 ) + eval0( ( x * ROOT_THREE - 1.0 ) / ( x + ROOT_THREE ) );
        }
        else if ( x >= TWO_MINUS_ROOT_THREE )
        {
            // x in range [2-sqrt(3),1/sqrt(3))
            return ( Math.PI / 6.0 ) - eval0( ( 1.0 - x * ROOT_THREE ) / ( x + ROOT_THREE ) );
        }
        else
        {
            // x in range [0,2-sqrt(3))
            return eval0( x );
        }
    }

    public static double eval2( double y, double x )
    {
        if ( x > 0 )
        {
            return eval( y / x );
        }
        else if ( x < 0 )
        {
            if ( y >= 0 )
            {
                return Math.PI + eval( y / x );
            }
            else
            {
                return -Math.PI + eval( y / x );
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

    /**
     * Compute atan(x) for x in range [0,2-sqrt(3)].
     */
    private static double eval0( double x )
    {
        //
        // Compute approximation to atan(x)/x on the interval [0,2-sqrt(3)].
        //
        // Using Mathematica, the minimax approximation for atan(x)/x of
        // degree 4 on the interval [0,2-sqrt(3)] is:
        //
        // a0 + a1*x + a2*x^2 + a3*x^3 + a4*x^4
        //
        // where a0 =  9.999997323941170e-1
        //       a1 =  4.688970430013420e-5
        //       a2 = -3.346336432273858e-1
        //       a3 =  1.207787323212745e-2
        //       a4 =  1.609853689386952e-1
        //
        // and the max relative error is approximately 2.7e-7.
        //
        // We can evaluate this polynomial either using Horner's rule
        //
        // a0 + x * (a1 + x * ( a2 + x * (a3 + x * a4))) [ 8 ops]
        //
        // or as
        //
        // (a0 + a1 * x) + x^2 * (a2 + a3 * x + a4 * x^2) [ 9 ops]
        //
        // The second way, even though it requires one more operation, is slighty faster
        // because it allows greater parallelism.
        //

        //return x * (9.999997323941170e-1 + x * (4.688970430013420e-5 + x * (-3.346336432273858e-1 + x * (1.207787323212745e-2 + x * 1.609853689386952e-1))));

        double x2 = x * x;
        double y0 = 9.999997323941170e-1 + 4.688970430013420e-5 * x;
        double y1 = -3.346336432273858e-1 + 1.207787323212745e-2 * x + 1.609853689386952e-1 * x2;
        return x * ( y0 + x2 * y1 );
    }
}
