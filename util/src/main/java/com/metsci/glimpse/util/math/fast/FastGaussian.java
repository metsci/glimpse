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

import com.metsci.glimpse.util.math.MathConstants;

/**
 * An approximation to a univariate gaussian pdf of mean zero, deviation 1.
 *
 * The width of the interval over which the function is sampled is chosen as
 * a function of the number of sample points to match the maximum error
 * within the table (which occurs at x = 1, where the slope is maximal) with
 * that at the boundary of the interval, outside of which the function is
 * assumed to be identically zero. It turns out that the exact value is
 * given by:
 *
 *     a = sqrt(W(exp(1)*n*n)),
 *
 * where W(z) is the Lambert function, [-a,a] is the interval over which
 * the function should be tabulated, and n is the number of sample points.
 *
 * For our purposes, the following asymptotic approximation to W(z) is
 * sufficient:
 *
 *     W(z) ~ ln(z) - ln(ln(z)) + ln(ln(z))/ln(z).
 *
 * Maximum magnitude of approximation error as a function of n:
 *
 *     n          error      interval where non-zero
 *   ------     ----------   -----------------------
 *      100     6.89x10^-3   [-2.85 , +2.85]
 *     1000     8.49x10^-4   [-3.51 , +3.51]
 *    10000     9.86x10^-5   [-4.08 , +4.08]
 *   100000     1.11x10^-5   [-4.58 , +4.58]
 *
 * Minimum value of n as a function of required accuracy:
 *
 *   accuracy        n
 *   --------    ---------
 *   < 10^-2         >= 66
 *   < 10^-3        >= 838
 *   < 10^-4       >= 9854
 *   < 10^-5     >= 111381
 *
 * @author ellis
 */
public class FastGaussian extends FastFunc
{
    private static final FastGaussian _instance = new FastGaussian( 1000 );

    private static double intervalSize( int samples )
    {
        double z = Math.exp( 1.0 ) * samples * samples;
        double logz = Math.log( z );
        double loglogz = Math.log( logz );

        return Math.sqrt( logz - loglogz + loglogz / logz );
    }

    public static FastGaussian getInstance( )
    {
        return _instance;
    }

    public FastGaussian( int samples )
    {
        super( -intervalSize( samples ), intervalSize( samples ), samples );
    }

    @Override
    protected double f( double x )
    {
        return Math.exp( -0.5 * x * x ) * MathConstants.ONE_OVER_ROOT_TWO_PI;
    }

    @Override
    public double evaluate( double x )
    {
        if ( x < min || x > max )
        {
            return 0.0;
        }
        else
        {
            return lookup( x );
        }
    }

}
