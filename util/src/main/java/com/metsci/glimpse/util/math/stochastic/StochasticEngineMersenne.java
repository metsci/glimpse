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
package com.metsci.glimpse.util.math.stochastic;

/**
 *  Modified from a C-program for MT19937 by Takuji Nishimura and Makoto Matsumoto.
 *  (http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/VERSIONS/C-LANG/mt19937-64.c)
 *  License on original C-program is 3-clause BSD.
 *
 *  Seeding could be improved to use longs.
 *
 *  @author osborn
 */
public final class StochasticEngineMersenne implements StochasticEngine
{
    private final Generator _generator;

    private final static int N = 624;
    private final static int M = 397;
    private final static int MATRIX_A[] = { 0x0, 0x9908b0df };
    private final static int UPPER_MASK = 0x80000000;
    private final static int LOWER_MASK = 0x7fffffff;

    // state
    private transient int[] mt;
    private transient int mti;

    private StochasticEngineMersenne( int seed )
    {
        setSeed( seed );
        _generator = new GeneratorImpl( );
    }

    @Override
    public final State getState( )
    {
        return null;
    }

    @Override
    public final Generator getGenerator( )
    {
        return _generator;
    }

    public static StochasticEngineMersenne createEngine( int seed )
    {
        return new StochasticEngineMersenne( seed );
    }

    private final void setSeed( int seed )
    {
        mt = new int[ N ];
        mt[ 0 ] = seed;

        for ( mti = 1; mti < N; mti++ )
        {
            mt[ mti ] = ( 1812433253 * ( mt[ mti - 1 ] ^ ( mt[ mti - 1 ] >>> 30 ) ) + mti );
        }
    }

    protected final int next( int bits )
    {
        int y, kk;
        if ( mti >= N ) // generate N words at one time
        {
            for ( kk = 0; kk < N - M; kk++ )
            {
                y = ( mt[ kk ] & UPPER_MASK ) | ( mt[ kk + 1 ] & LOWER_MASK );
                mt[ kk ] = mt[ kk + M ] ^ ( y >>> 1 ) ^ MATRIX_A[ y & 0x1 ];
            }

            for ( ; kk < N - 1; kk++ )
            {
                y = ( mt[ kk ] & UPPER_MASK ) | ( mt[ kk + 1 ] & LOWER_MASK );
                mt[ kk ] = mt[ kk + ( M - N ) ] ^ ( y >>> 1 ) ^ MATRIX_A[ y & 0x1 ];
            }

            y = ( mt[ N - 1 ] & UPPER_MASK ) | ( mt[ 0 ] & LOWER_MASK );
            mt[ N - 1 ] = mt[ M - 1 ] ^ ( y >>> 1 ) ^ MATRIX_A[ y & 0x1 ];

            mti = 0;
        }

        y = mt[ mti++ ];

        // Tempering
        y ^= ( y >>> 11 );
        y ^= ( y << 7 ) & 0x9d2c5680;
        y ^= ( y << 15 ) & 0xefc60000;
        y ^= ( y >>> 18 );

        return ( y >>> ( 32 - bits ) );
    }

    private final class GeneratorImpl implements Generator
    {
        @Override
        public final double nextDouble( )
        {
            return ( ( ( long ) next( 26 ) << 27 ) + next( 27 ) ) / ( double ) ( 1L << 53 );
        }

        @Override
        public final int nextInt( int n )
        {
            // powers of two
            if ( ( n & -n ) == n )
            {
                return ( int ) ( ( n * ( long ) next( 31 ) ) >> 31 );
            }

            int bits, val;
            do
            {
                bits = next( 31 );
                val = bits % n;
            }
            while ( bits - val + ( n - 1 ) < 0 );
            return val;
        }

        @Override
        public final int nextBits( int numBits )
        {
            return next( numBits );
        }
    }
}
