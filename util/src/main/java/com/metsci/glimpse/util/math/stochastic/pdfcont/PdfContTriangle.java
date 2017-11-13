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
package com.metsci.glimpse.util.math.stochastic.pdfcont;

import static com.metsci.glimpse.util.GeneralUtils.doublesEqual;
import static java.lang.Math.sqrt;

import com.metsci.glimpse.util.math.stochastic.Generator;

/**
 * @author borkholder
 */
public class PdfContTriangle implements PdfCont
{
    private final double min;
    private final double max;
    private final double mode;
    private final double f;

    public PdfContTriangle( double min, double mode, double max )
    {
        if ( min > mode )
        {
            throw new IllegalArgumentException( String.format( "min (%f) < mode (%f)", min, mode ) );
        }
        if ( mode > max )
        {
            throw new IllegalArgumentException( String.format( "mode (%f) < max (%f)", mode, max ) );
        }
        if ( min == max )
        {
            throw new IllegalArgumentException( String.format( "min (%f) = max (%f)", min, max ) );
        }

        this.min = min;
        this.mode = mode;
        this.max = max;
        f = ( mode - min ) / ( max - min );
    }

    @Override
    public double draw( Generator g )
    {
        double u = g.nextDouble( );
        if ( u < f )
        {
            return min + sqrt( u * ( max - min ) * ( mode - min ) );
        }
        else
        {
            return max - sqrt( ( 1 - u ) * ( max - min ) * ( max - mode ) );
        }
    }

    @Override
    public int hashCode( )
    {
        final int prime = 20549;
        int result = 1;
        result = prime * result + Double.hashCode( min );
        result = prime * result + Double.hashCode( mode );
        result = prime * result + Double.hashCode( max );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this )
        {
            return true;
        }
        if ( o == null )
        {
            return false;
        }
        if ( o.getClass( ) != this.getClass( ) )
        {
            return false;
        }

        PdfContTriangle other = ( PdfContTriangle ) o;
        return doublesEqual( other.min, other.min ) &&
                doublesEqual( other.mode, other.mode ) &&
                doublesEqual( other.max, other.max );

    }
}
