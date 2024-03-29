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
package com.metsci.glimpse.util.math.stochastic.pdfcont;

import static com.metsci.glimpse.util.GeneralUtils.doublesEqual;

import java.util.Objects;

import com.metsci.glimpse.util.math.stochastic.Generator;

/**
 * @author osborn
 */
public class PdfTruncatedCont implements PdfCont
{
    private final PdfCont _pdf;
    private final double _lowCutoff;
    private final double _highCutoff;

    public PdfTruncatedCont( PdfCont pdf, double lowCutoff, double highCutoff )
    {
        _pdf = pdf;
        _lowCutoff = lowCutoff;
        _highCutoff = highCutoff;
    }

    @Override
    public final double draw( Generator g )
    {
        double value;

        do
        {
            value = _pdf.draw( g );
        }
        while ( value < _lowCutoff || value > _highCutoff );

        return value;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 20593;
        int result = 1;
        result = prime * result + Objects.hashCode( _pdf );
        result = prime * result + Double.hashCode( _lowCutoff );
        result = prime * result + Double.hashCode( _highCutoff );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        PdfTruncatedCont other = ( PdfTruncatedCont ) o;
        return ( Objects.equals( other._pdf, _pdf )
              && doublesEqual( other._lowCutoff, _lowCutoff )
              && doublesEqual( other._highCutoff, _highCutoff ) );
    }
}
