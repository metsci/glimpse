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
 * A function that precomputes its values for speed.  Intended
 * to accelerate the evaluation of transcendental functions in cases
 * where speed is more important than accuracy.  To implement
 * a particular precomputed function, extend this abstract class and
 * implement the method that evaluates the function.
 *
 * @author ellis
 */
public abstract class FastFunc
{
    /** minimum precomputed value */
    protected final double min;

    /** maximum precomputed value */
    protected final double max;

    /** number of samples between min and max */
    protected final int nSamples;

    /** sampling spacing */
    private final double dx;

    /** 1 / dx */
    private final double invdx;

    /** - min * invdx + 0.5 */
    private final double b;

    /** computed samples of function */
    protected double[] f;

    /**
     * Main Constructor in which the values of this function are computed
     *
     * @param min minimum value to sample
     * @param max maximum value to sample
     * @param samples number of samples
     */
    public FastFunc( double min, double max, int nSamples )
    {
        this.min = min;
        this.max = max;
        this.nSamples = nSamples;

        this.dx = ( max - min ) / ( nSamples - 1 );
        this.invdx = 1 / dx;
        this.b = -min * invdx + 0.5;

        precompute( );
    }

    protected void precompute( )
    {
        f = new double[nSamples];

        for ( int i = 0; i < nSamples - 1; i++ )
        {
            double x = min + i * dx;
            f[i] = f( x );
        }

        this.f[nSamples - 1] = f( max );
    }

    /**
     * Returns the value of the precomputed function using a simple
     * nearest neighbor interpolation.
     *
     * @param x function argument (you must ensure this is in range)
     * @return value of the function
     */
    public double lookup( double x )
    {
        return f[( int ) ( x * invdx + b )];
    }

    /**
     * Returns the value of the precomputed function using a simple
     * nearest neighbor interpolation.  size(A) should = size(x).
     *
     * @param A function values
     * @param x arguments
     */
    public void lookup( double[][] A, double[][] x )
    {
        for ( int i = A.length; --i >= 0; )
        {
            double[] Ai = A[i];
            double[] xi = x[i];

            for ( int j = Ai.length; --j >= 0; )
            {
                Ai[j] = f[( int ) ( xi[j] * invdx + b )];
            }
        }
    }

    /**
     * Computes the actual value of the function at x.
     *
     * @param x argument
     * @return f(x)
     */
    protected abstract double f( double x );

    public double evaluate( double x )
    {
        if ( x < min || x > max ) return Double.NaN;

        return lookup( x );
    }

}
