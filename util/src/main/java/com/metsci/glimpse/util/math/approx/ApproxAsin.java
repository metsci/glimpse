/*
 * Copyright (c) 2020, Metron, Inc.
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
import static java.lang.Math.*;

/**
 * Similar to the classes in {@link com.metsci.glimpse.util.math.fast}, but uses linear interpolation
 * between samples instead of nearest-neighbor.
 */
public class ApproxAsin
{
    public static final double ONE_OVER_TWO_PI = 1.0 / TWO_PI;
    public static final double PI_OVER_2 = PI / 2.0;


    protected final int n;
    protected final double xStep;
    protected final double oneOverXStep;
    protected final double[] y;


    public ApproxAsin( int numSamples )
    {
        this.n = numSamples;
        this.xStep = 2.0 / ( this.n - 1 );
        this.oneOverXStep = 1.0 / this.xStep;

        this.y = new double[ this.n ];

        // Set first and last values explicitly
        this.y[ 0 ] = -HALF_PI;
        this.y[ this.n - 1 ] = +HALF_PI;

        for ( int i = 1; i < this.n - 1; i++ )
        {
            double x = -1.0 + ( i * this.xStep );
            this.y[ i ] = Math.asin( x );
        }
    }

    // Helper method for taking advantage of the precalculated asin values to
    // compute acos values. Requires an additional subtraction over creating
    // an ApproxAcos class, but requires no additional storage.
    public double acos( double x )
    {
        return PI_OVER_2 - asin( x );
    }

    public double asin( double x )
    {
        if ( Double.isNaN( x ) || x < -1.0 || x > +1.0 )
        {
            return Double.NaN;
        }

        // How many steps is x above xMin
        double w = ( x - (-1.0) ) * this.oneOverXStep;

        int iBefore = ( int ) w;
        if( iBefore + 1 == this.n ) {
            // handle edge case where steps is the last edge
            return PI_OVER_2;
        }
        double yBefore = this.y[ iBefore ];
        double yAfter = this.y[ iBefore + 1 ];
        double xFrac = w - iBefore;

        return ( yBefore + ( xFrac * ( yAfter - yBefore ) ) );
    }

}
