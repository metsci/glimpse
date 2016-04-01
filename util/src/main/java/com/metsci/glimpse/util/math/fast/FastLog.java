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
 * Provides about 12 digits of accuracy at a fraction of the
 * cost of Math.log().
 *
 * @author ellis
 */
public class FastLog extends FastFunc
{
    public static final double LN_2 = Math.log( 2.0 );
    public static final double LN_2_INV = 1.0 / LN_2;
    public static final double LOG10_2 = Math.log10( 2.0 );

    // Note: This must follow the static final variables above, which are used indirectly by the constructor call.
    private static final FastLog instance = new FastLog( 100000 );

    public static FastLog getInstance( )
    {
        return instance;
    }

    public FastLog( int samples )
    {
        super( 1.0, 2.0, samples );
    }

    @Override
    protected double f( double x )
    {
        return Math.log( x ) * LN_2_INV;
    }

    public double log2( double x )
    {
        long bits = Double.doubleToLongBits( x );
        long exponent = bits & 0x7ff0000000000000L;
        long mantissa = bits & 0x000fffffffffffffL;

        // shift exponent bits
        exponent >>= 52;

        // unbias the exponent
        exponent -= 1023;

        // convert to a double (integer part of result)
        double result = exponent;

        // mask in an exponent equivalent to zero
        mantissa |= 0x3ff0000000000000L;

        // convert to a double
        double xx = Double.longBitsToDouble( mantissa );

        // add in fractional part, giving log2(x)
        result += super.evaluate( xx );

        return result;
    }

    @Override
    public double evaluate( double x )
    {
        return log2( x ) * LN_2;
    }

    public double log10( double x )
    {
        return log2( x ) * LOG10_2;
    }
}
