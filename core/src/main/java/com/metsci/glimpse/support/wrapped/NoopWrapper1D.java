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
package com.metsci.glimpse.support.wrapped;

public class NoopWrapper1D implements Wrapper1D
{

    public static final Wrapper1D NOOP_WRAPPER_1D = new NoopWrapper1D( );


    private NoopWrapper1D( )
    { }

    @Override
    public double wrapMin( )
    {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double wrapMax( )
    {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double wrapValue( double value )
    {
        return value;
    }

    @Override
    public double wrapNear( double ref, double value )
    {
        return value;
    }

    @Override
    public double wrapDelta( double delta )
    {
        return delta;
    }

    @Override
    public double[] getRenderShifts( double minValue, double maxValue )
    {
        return new double[] { 0.0 };
    }

    @Override
    public int hashCode( )
    {
        // Singleton, so just use Object identity
        return super.hashCode( );
    }

    @Override
    public boolean equals( Object o )
    {
        // Singleton, so just use Object identity
        return super.equals( o );
    }

}
