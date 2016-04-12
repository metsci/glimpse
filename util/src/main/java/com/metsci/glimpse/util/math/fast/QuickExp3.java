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
 * Similar to {@link QuickExp} but uses 3 smaller lookup tables instead of one large one.
 * We compensate the extra table lookups by using a simpler polynomial approximation.
 * It has a maximum relative error of about 4.77e-7 and is about 10 times faster than Math.exp.
 *
 * @see QuickExp
 * @author ellis
 */
public class QuickExp3
{
    /*
     * We represent the exponent as:
     *
     *      x = 16m + n/8 + p/1024 + r
     *
     * so that
     *
     *      exp(x) = exp(16m) exp(n/8) exp(p/1024) exp(r)
     *
     * where
     *
     *      -44 <= m <= 44       (m integer)
     *        0 <= n <  128      (n integer)
     *        0 <= p <  128      (p integer)
     *        0 <= r <  1/1024   (r floating point)
     *
     * exp(16m),  exp(n/8), and exp(p/1024) are looked into tables, and exp(r) is approximated
     * as 1+r. The combined size of the lookup tables is 89+128+128=345 entries.
     *
     * Max error: 4.77e-7 [ = exp(z) - (1+z), where z = 1/1024 ]
     */

    private static final double _invDx = 1024.0; // Exactly representable
    private static final double _dx = 1.0 / _invDx; // Exactly representable
    private static final double _xMin = -704.0; // Exactly representable
    private static final double _xMax = 709.0 + 801.0 * _dx; // Exactly representable

    private static final double EXP_NEG_XMAX = Math.exp( -_xMax );

    private static final double[] _values1;
    private static final double[] _values2;
    private static final double[] _values3;

    static
    {
        int n1 = 89;
        _values1 = new double[n1];
        for ( int i = 0; i < n1; i++ )
        {
            double x = _xMin + 16 * i;
            _values1[i] = Math.exp( x );
        }

        int n2 = 128;
        _values2 = new double[n2];
        for ( int i = 0; i < n2; i++ )
        {
            double x = i / 8.0;
            _values2[i] = Math.exp( x );
        }

        int n3 = 128;
        _values3 = new double[n3];
        for ( int i = 0; i < n3; i++ )
        {
            double x = i / 1024.0;
            _values3[i] = Math.exp( x );
        }
    }

    public static double eval( double x )
    {
        if ( x < _xMin )
        {
            if ( x < -_xMax )
            {
                return 0.0;
            }
            else
            {
                return QuickExp3.eval( x + _xMax ) * EXP_NEG_XMAX;
            }
        }
        else if ( x > _xMax )
        {
            return Double.POSITIVE_INFINITY;
        }
        else
        {
            int i = ( int ) ( ( x - _xMin ) * _invDx );
            int m = i >> 14;
            int n = ( i >> 7 ) & 127;
            int p = i & 127;
            double x0 = _xMin + i * _dx;
            double r = x - x0;

            return _values1[m] * _values2[n] * _values3[p] * ( r + 1.0 );
        }
    }

}
