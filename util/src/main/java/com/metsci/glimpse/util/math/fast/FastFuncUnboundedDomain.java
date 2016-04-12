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
 * Provides table look-up capability for functions of bounded
 * range but unbounded domain.
 *
 * It uses a domain transformation to achieve this:
 *
 * x -> x / ( 1 - |x| ), mapping [-inf,inf] -> [-1,1]
 *
 * the inverse of which (used for lookups) is:
 *
 * x -> x / ( 1 + |x| ).
 *
 * @author ellis
 */
public abstract class FastFuncUnboundedDomain extends FastFunc
{
    public FastFuncUnboundedDomain( int samples )
    {
        super( -1.0, 1.0, samples );
    }

    @Override
    protected double f( double x )
    {
        if ( x == -1.0 ) return gNegativeInfinity( );
        if ( x == 1.0 ) return gPositiveInfinity( );
        return g( x / ( 1.0 - Math.abs( x ) ) );
    }

    @Override
    public double evaluate( double x )
    {
        return super.evaluate( x / ( 1.0 + Math.abs( x ) ) );
    }

    /**
     * Overridden with function to be represented.
     */
    protected abstract double g( double x );

    /**
     * lim[x->-inf] g(x)
     */
    protected abstract double gNegativeInfinity( );

    /**
     * lim[x->+inf] g(x)
     */
    protected abstract double gPositiveInfinity( );
}
