/*
 * Copyright (c) 2012, Metron, Inc.
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
 * This engine is based around the same Linear Congruential Pseudorandom (LCP)
 * number generator used in java.util.Random.  It is described in Knuth's book.
 *
 * @author osborn
 */
public final class StochasticEngineLcp implements StochasticEngine
{
    private long _state;
    private final Generator _generator;

    public StochasticEngineLcp( LcpState state )
    {
        _state = state.getState();
        _generator = new GeneratorImpl();
    }

    public final State getState()
    {
        return new LcpState( _state );
    }

    public final Generator getGenerator()
    {
        return _generator;
    }

    protected final int next( int bits )
    {
        _state = (_state * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        return (int)(_state >>> (48 - bits));
    }

    public static StochasticEngineLcp createEngine( long lcpStateSeed )
    {
        LcpState state = new LcpState( lcpStateSeed );
        return state.intializeEngine( );
    }

    public static class LcpState implements StochasticEngine.State
    {
        private final long _state;

        public LcpState( long state )
        {
            _state = state;
        }

        public StochasticEngineLcp intializeEngine()
        {
            return new StochasticEngineLcp( this );
        }

        public long getState()
        {
            return _state;
        }
    }

    private final class GeneratorImpl implements Generator
    {
        public final double nextDouble()
        {
            return (((long)next(26) << 27) + next(27)) / (double)(1L << 53);
        }

        public final int nextInt( int n )
        {
            // powers of two
            if ((n & -n) == n)
            {
                return (int)((n * (long)next(31)) >> 31);
            }

            int bits, val;
            do
            {
                bits = next(31);
                val = bits % n;
            }
            while(bits - val + (n-1) < 0);
            return val;
        }
    }
}
