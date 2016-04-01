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
 * Similar to {@link QuickExp} but uses 2 smaller lookup tables instead of one large one.
 * We compensate the extra table lookup by using a minimax polynomial of smaller degree.
 * It has a maximum relative error of about 3.8e-6 and is about 10 times faster than Math.exp.
 *
 * @see QuickExp
 * @author ellis
 */
public class QuickExp2
{
    /*
     * We represent the exponent as:
     *
     *      x = 4m + n/128 + r
     *
     * so that
     *
     *      exp(x) = exp(4m) exp(n/128) exp(r)
     *
     * where
     *
     *     -177 <= m <= 177      (m integer)
     *        0 <= n <  512      (n integer)
     *        0 <= r <  1/128    (r floating point)
     *
     * exp(4m) and exp(n/128) are looked into tables, and exp(r) is computed using a minimax
     * polymomial of degree one. The combined size of the lookup tables is 355+512=867 entries.
     *
     * Minimax polynomial of degree 1 on [0,1/128] for exp(r):
     *
     * p(r) = a0 + a1*r
     *
     * a0 = 0.9999961853059682
     * a1 = 1.003912612791292
     *
     * Max error: 3.814694031822877e-6 (obtained with Mathematica)
     */

    private static final double _invDx = 128.0; // Exactly representable
    private static final double _dx = 1.0 / _invDx; // Exactly representable
    private static final double _xMin = -708.0; // Exactly representable
    private static final double _xMax = 709.0 + 100.0 * _dx; // Exactly representable

    private static final double EXP_NEG_XMAX = Math.exp( -_xMax );

    private static final double[] _values1;
    private static final double[] _values2;

    static
    {
        int n1 = 355;
        _values1 = new double[n1];
        for ( int i = 0; i < n1; i++ )
        {
            double x = _xMin + 4 * i;
            _values1[i] = Math.exp( x );
        }

        int n2 = 512;
        _values2 = new double[n2];
        for ( int i = 0; i < n2; i++ )
        {
            double x = i / 128.0;
            _values2[i] = Math.exp( x );
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
                return QuickExp2.eval( x + _xMax ) * EXP_NEG_XMAX;
            }
        }
        else if ( x > _xMax )
        {
            return Double.POSITIVE_INFINITY;
        }
        else
        {
            int i = ( int ) ( ( x - _xMin ) * _invDx );
            int m = i >> 9;
            int n = i & 511;
            double x0 = _xMin + i * _dx;
            double r = x - x0;

            return _values1[m] * _values2[n] * ( 1.003912612791292 * r + 0.9999961853059682 );
        }
    }
}
