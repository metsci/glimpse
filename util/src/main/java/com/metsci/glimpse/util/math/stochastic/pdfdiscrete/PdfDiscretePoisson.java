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
package com.metsci.glimpse.util.math.stochastic.pdfdiscrete;

import com.metsci.glimpse.util.math.stochastic.Generator;

/**
 * @author osborn
 */
public final class PdfDiscretePoisson implements PdfDiscrete
{
    private final double _invMean;
    private final boolean _meanZero;

    public PdfDiscretePoisson( double mean )
    {
        if ( mean == 0 )
        {
            _meanZero = true;
            _invMean = 0;
        }
        else
        {
            _meanZero = false;
            _invMean = 1.0 / mean;
        }
    }

    @Override
    public int draw( Generator g )
    {
        if ( _meanZero )
        {
            return 0;
        }

        int x = 0;

        double t = 0.0;
        for ( ;; )
        {
            t = t - ( Math.log( g.nextDouble( ) ) * _invMean );

            if ( t > 1.0 )
            {
                break;
            }

            x = x + 1;
        }

        return x;
    }
}
