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
package com.metsci.glimpse.axis;

/**
 * <p>An Axis1D which is intended to be interpreted as wrapping for values outside of its minWrapVal and maxWrapVal.
 *    minWrapVal is considered an inclusive bound, maxWrapVal an exclusive bound.</p>
 *
 * <p>For example: minWrapVal=-10, maxWrapVal=10, minVal=20, and maxVal=21. The axis should paint data from 0 to 1
 *    (imagine shifting the minVal/maxVal down by maxWrapVal-minWrapVal until it falls in the correct range).</p>
 *
 * <p>Another example: minWrapVal=-10, maxWrapVal=10, minVal=-10, maxVal=20. The axis should paint the data twice, from
 *    -10 to 10 then wrapping and painting data from -10 to 10 again.</p>
 *
 * @author ulman
 */
public class WrappedAxis1D extends Axis1D
{
    private double minWrapVal;
    private double maxWrapVal;

    public WrappedAxis1D( WrappedAxis1D axis )
    {
        this( axis, axis.minWrapVal, axis.maxWrapVal );
    }

    public WrappedAxis1D( double minWrapVal, double maxWrapVal )
    {
        this( null, minWrapVal, maxWrapVal );
    }

    public WrappedAxis1D( Axis1D parent, double minWrapVal, double maxWrapVal )
    {
        super( parent );
        this.minWrapVal = minWrapVal;
        this.maxWrapVal = maxWrapVal;
    }

    public double getWrapMin( )
    {
        return minWrapVal;
    }

    public double getWrapMax( )
    {
        return maxWrapVal;
    }

    public void setWrapMin( double min )
    {
        this.minWrapVal = min;
    }

    public void setWrapMax( double max )
    {
        this.maxWrapVal = max;
    }

    public double getWrapSpan( )
    {
        return getWrapMax( ) - getWrapMin( );
    }

    public double getWrappedValue( double value )
    {
        return getWrappedValue( value, false );
    }

    /**
     * @param value the linear axis value to convert
     * @param roundUp if true, values on the seam will return getWrapMax( ) instead of getWrapMin( )
     * @return the value converted to wrapped coordinates
     */
    public double getWrappedValue( double value, boolean roundUp )
    {
        return minWrapVal + getWrappedMod( value, roundUp );
    }

    public double getWrappedMod( double value )
    {
        return getWrappedMod( value, false );
    }

    /**
     * @param value the linear axis value to convert
     * @param roundUp if true, values on the seam will return getWrapMax( ) instead of getWrapMin( )
     * @return the distance between this value and the left seam
     */
    public double getWrappedMod( double value, boolean roundUp )
    {
        double span = getWrapSpan( );
        double v = value - minWrapVal; // shift minWrapVal to 0
        double mod = v % span;
        return mod + ( ( mod == 0 && roundUp ) || mod < 0 ? span : 0 );
    }

    @Override
    public Axis1D clone( )
    {
        return new WrappedAxis1D( this );
    }
}
