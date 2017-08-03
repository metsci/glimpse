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
package com.metsci.glimpse.support.wrapped;

import static com.metsci.glimpse.util.GeneralUtils.doublesEqual;
import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;

public class StandardWrapper1D implements Wrapper1D
{

    public final double wrapMin;
    public final double wrapMax;


    /**
     * Client code should generally NOT call this constructor, but should use
     * {@link Wrapper1D#getWrapper(com.metsci.glimpse.axis.Axis1D)} instead.
     */
    public StandardWrapper1D( double wrapMin, double wrapMax )
    {
        this.wrapMin = wrapMin;
        this.wrapMax = wrapMax;
    }

    @Override
    public double wrapMin( )
    {
        return this.wrapMin;
    }

    @Override
    public double wrapMax( )
    {
        return this.wrapMax;
    }

    @Override
    public double wrapValue( double value )
    {
        double wrapSpan = this.wrapMax - this.wrapMin;
        double wrapCount = floor( ( value - this.wrapMin ) / wrapSpan );
        return ( value - ( wrapCount * wrapSpan ) );
    }

    @Override
    public double wrapNear( double ref, double value )
    {
        return ( ref + this.wrapDelta( value - ref ) );
    }

    @Override
    public double wrapDelta( double delta )
    {
        double wrapSpan = this.wrapMax - this.wrapMin;
        double wrapCount = floor( delta / wrapSpan );
        double deltaA = delta - ( wrapCount * wrapSpan );
        double deltaB = delta - ( ( wrapCount + 1.0 ) * wrapSpan );
        return ( abs( deltaA ) <= abs( deltaB ) ? deltaA : deltaB );
    }

    @Override
    public double[] getRenderShifts( double minValue, double maxValue )
    {
        double first = this.wrapValue( minValue ) - minValue;
        double step = this.wrapMax - this.wrapMin;
        int count = ( int ) max( 0, ceil( ( ( maxValue + first ) - this.wrapMin ) / step ) );

        double[] shifts = new double[ count ];
        for ( int i = 0; i < count; i++ )
        {
            double offset = first - i*step;
            shifts[ i ] = offset;
        }
        return shifts;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 83389;
        int result = 1;
        result = prime * result + Double.hashCode( this.wrapMin );
        result = prime * result + Double.hashCode( this.wrapMax );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        StandardWrapper1D other = ( StandardWrapper1D ) o;
        return ( doublesEqual( other.wrapMin, this.wrapMin )
              && doublesEqual( other.wrapMax, this.wrapMax ) );
    }

}
