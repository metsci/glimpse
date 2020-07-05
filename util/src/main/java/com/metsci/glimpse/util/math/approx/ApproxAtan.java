/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.util.math.approx;

import static com.metsci.glimpse.util.math.MathConstants.*;
import static java.lang.Double.*;
import static java.lang.Math.*;

/**
 * Similar to the classes in {@link com.metsci.glimpse.util.math.fast}, but uses linear interpolation
 * between samples instead of nearest-neighbor.
 * <p>
 * Anecdotally, speed is about 7.5x faster than {@link Math#atan(double)}. With 100k samples, max error
 * is around 1e-10. Max error decreases as the number of samples increases.
 * <p>
 * Original domain is unbounded:
 * <pre>
 *     a = y / x
 *     a ∊ [ -inf, +inf ]
 * </pre>
 * <p>
 * Transform to bounded domain:
 * <pre>
 *     b = a / ( 1.0 + abs( a ) )
 *     b ∊ [ -1, +1 ]
 * </pre>
 * <p>
 * Invert the transform to compute initial samples:
 * <pre>
 *     a = b / ( 1.0 - abs( b ) )
 * </pre>
 */
public class ApproxAtan
{

    protected final int n;
    protected final double bStep;
    protected final double oneOverBStep;
    protected final double[] v;


    public ApproxAtan( int numSamples )
    {
        this.n = numSamples;
        this.bStep = 2.0 / ( this.n - 1 );
        this.oneOverBStep = 1.0 / this.bStep;

        this.v = new double[ this.n ];

        // Set first and last values explicitly
        this.v[ 0 ] = -HALF_PI;
        this.v[ this.n - 1 ] = +HALF_PI;

        // Compute values other than first and last
        for ( int i = 1; i < this.n - 1; i++ )
        {
            double b = -1.0 + ( i * this.bStep );
            double a = b / ( 1.0 - abs( b ) );
            this.v[ i ] = Math.atan( a );
        }
    }

    public double atan( double a )
    {
        double b = a / ( 1.0 + abs( a ) );
        if ( Double.isNaN( b ) )
        {
            return Double.NaN;
        }

        // How many steps is b above bMin
        double w = ( b - (-1.0) ) * this.oneOverBStep;

        int iBefore = ( int ) w;
        if( iBefore + 1 == this.n ) {
            // handle edge case where steps is the last edge
            return +HALF_PI;
        }
        double vBefore = this.v[ iBefore ];
        double vAfter = this.v[ iBefore + 1 ];
        double bFrac = w - iBefore;

        return ( vBefore + ( bFrac * ( vAfter - vBefore ) ) );
    }

    public double atan2( double y, double x )
    {
        if ( Double.isNaN( x ) || Double.isNaN( y ) )
        {
            return Double.NaN;
        }

        // 00: positive
        // 01: negative
        // 10: zero
        //
        int xSignCode = ( x == 0.0 ? 0b10 : rawSignBit( x ) );
        int ySignCode = ( y == 0.0 ? 0b10 : rawSignBit( y ) );

        // 0000: x > 0, y > 0
        // 0001: x > 0, y < 0
        // 0010: x > 0, y = 0
        //
        // 0100: x < 0, y > 0
        // 0101: x < 0, y < 0
        // 0110: x < 0, y = 0
        //
        // 1000: x = 0, y > 0
        // 1001: x = 0, y < 0
        // 1010: x = 0, y = 0
        //
        int signCode = ( ( xSignCode << 2 ) | ( ySignCode ) );

        switch ( signCode )
        {
            // x > 0
            case 0b0000:
            case 0b0001:
            case 0b0010:
            {
                return this.atan( y / x );
            }

            // x < 0, y >= 0
            case 0b0100:
            case 0b0110:
            {
                return ( this.atan( y / x ) + PI );
            }

            // x < 0, y < 0
            case 0b0101:
            {
                return ( this.atan( y / x ) - PI );
            }

            // x = 0, y > 0
            case 0b1000:
            {
                return +HALF_PI;
            }

            // x = 0, y < 0
            case 0b1001:
            {
                return -HALF_PI;
            }

            // x = 0, y = 0
            case 0b1010:
            {
                // Match behavior of Math.atan2(0,0)
                return 0.0;
            }

            default:
            {
                // Should never happen -- would indicate a bug
                throw new RuntimeException( );
            }
        }
    }

    public static int rawSignBit( double d )
    {
        return ( int ) ( ( doubleToRawLongBits( d ) & ( 1L << 63 ) ) >>> 63 );
    }

}
