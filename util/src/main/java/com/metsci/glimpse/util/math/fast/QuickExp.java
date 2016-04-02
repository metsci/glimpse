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
 * A faster version of Math.exp with maximum relative error of 1e-5.
 * It is about 10 times faster than Math.exp.
 *
 * @see QuickExp2
 * @author ellis
 */
public class QuickExp
{
    /*
     * Size of lookup table is 11357 entries.
     *
     * Minimax polynomial of degree 2 on [0,1/8] for exp(x):
     *
     * p(x) = a0 + a1*x + a2*x^2
     *
     * a0 = 1.000010170663564
     * a1 = 0.9984993976847192
     * a2 = 0.5321173005431935
     *
     * Max error: 1.01706635644372e-5 (obtained with Mathematica)
     */

    private static final double _xMin = -709.75; // Exactly representable
    private static final double _xMax = +709.75; // Exactly representable
    private static final double _dx = 0.125; // Exactly representable
    private static final double _invDx = 8.0; // Exactly representable
    private static final double[] _values;

    public static void main( String[] args )
    {
    }

    static
    {
        int n = ( int ) Math.round( ( _xMax - _xMin ) * _invDx ) + 1;

        _values = new double[n];
        for ( int i = 0; i < n; i++ )
        {
            double x = _xMin + i * _dx;
            _values[i] = Math.exp( x );
        }
    }

    public static double eval( double x )
    {
        if ( x < _xMin )
        {
            return 0.0;
        }
        else if ( x > _xMax )
        {
            return Double.POSITIVE_INFINITY;
        }
        else
        {
            int index = ( int ) ( ( x - _xMin ) * _invDx );
            double x0 = _xMin + index * _dx;
            double z = x - x0;

            return _values[index] * ( ( 0.5321173005431935 * z + 0.9984993976847192 ) * z + 1.000010170663564 );
        }
    }
}
