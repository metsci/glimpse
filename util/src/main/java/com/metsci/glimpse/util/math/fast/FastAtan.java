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
 * @author ellis
 */
public class FastAtan extends FastFuncUnboundedDomain
{
    private static final FastAtan instance = new FastAtan( 100000 );

    public static FastAtan getInstance( )
    {
        return instance;
    }

    public FastAtan( int samples )
    {
        super( samples );
    }

    public double atan2( double y, double x )
    {
        double atan = evaluate( y / x );

        if ( x > 0.0 )
        {
            return atan;
        }
        else if ( x < 0.0 && y >= 0.0 )
        {
            return Math.PI + atan;
        }
        else if ( x < 0.0 && y < 0.0 )
        {
            return -Math.PI + atan;
        }
        else if ( y > 0.0 )
        {
            return 0.5 * Math.PI;
        }
        else if ( y < 0.0 )
        {
            return -0.5 * Math.PI;
        }
        else if ( x == 0 && y == 0 ) // match convention for Math.atan2(0,0)
        {
            return 0.0;
        }
        else
        {
            return Double.NaN;
        }
    }

    @Override
    protected double g( double x )
    {
        return Math.atan( x );
    }

    @Override
    protected double gNegativeInfinity( )
    {
        return -0.5 * Math.PI;
    }

    @Override
    protected double gPositiveInfinity( )
    {
        return 0.5 * Math.PI;
    }
}
