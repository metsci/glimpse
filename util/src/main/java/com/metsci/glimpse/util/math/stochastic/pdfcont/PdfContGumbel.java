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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.metsci.glimpse.util.math.stochastic.Generator;
import com.metsci.glimpse.util.math.stochastic.StochasticEngineLcp;

/**
 * See <a href=http://en.wikipedia.org/wiki/Gumbel_distribution}>Wikipedia article</a>
 *
 * @author osborn
 */
public class PdfContGumbel implements PdfCont
{
    private final double _mu;
    private final double _beta;

    public PdfContGumbel( double mu, double beta )
    {
        _mu = mu;
        _beta = beta;
    }

    @Override
    public double draw( Generator g )
    {
        return _mu - _beta * Math.log( -Math.log( g.nextDouble( ) ) );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 20533;
        int result = 1;
        result = prime * result + Double.hashCode( _mu );
        result = prime * result + Double.hashCode( _beta );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        PdfContGumbel other = ( PdfContGumbel ) o;
        return ( doublesEqual( other._mu, _mu )
              && doublesEqual( other._beta, _beta ) );
    }

    public static void main( String[] args ) throws IOException
    {
        PdfCont[] pdfs = { new PdfContGumbel( 0.5, 2.0 ),
                           new PdfContGumbel( 1.0, 2.0 ),
                           new PdfContGumbel( 1.5, 3.0 ),
                           new PdfContGumbel( 3.0, 4.0 ) };

        StochasticEngineLcp engine = StochasticEngineLcp.createEngine( 2813308004L );

        int N = 1000000;
        for ( int d = 0; d < pdfs.length; d++ )
        {
            BufferedWriter writer = new BufferedWriter( new FileWriter( "gumbelValues" + d + ".csv" ) );
            for ( int n = 0; n < N; n++ )
            {
                double y = pdfs[ d ].draw( engine.getGenerator( ) );
                writer.write( y + "," );
            }
            writer.close( );
        }
    }
}
