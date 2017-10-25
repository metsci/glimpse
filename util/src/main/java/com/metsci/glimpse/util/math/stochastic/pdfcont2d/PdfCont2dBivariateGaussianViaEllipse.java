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
package com.metsci.glimpse.util.math.stochastic.pdfcont2d;

import static com.metsci.glimpse.util.GeneralUtils.doublesEqual;

import com.metsci.glimpse.util.math.stochastic.Generator;
import com.metsci.glimpse.util.math.stochastic.pdfcont.PdfCont;
import com.metsci.glimpse.util.math.stochastic.pdfcont.PdfContGaussianBoxMuller;
import com.metsci.glimpse.util.units.Azimuth;

/**
 * Generates 2-dimensional samples from a bivariate Gaussian distribution which is defined via an
 * "error" or "covariance" ellipse which represents the 1-sigma contours of the density function.
 *
 * @author moskowitz
 */
public class PdfCont2dBivariateGaussianViaEllipse implements PdfCont2d
{
    private final double _semiMajorAxis;
    private final double _semiMinorAxis;
    private final double _orientation;
    private final PdfCont _gaussianPdf;
    private final double _cosOrient;
    private final double _sinOrient;

    /**
     * @param  semiMajorAxis_SU
     * @param  semiMinorAxis_SU
     * @param  orientation_SU
     */
    public PdfCont2dBivariateGaussianViaEllipse( double semiMajorAxis_SU, double semiMinorAxis_SU, double orientation_SU )
    {
        _semiMajorAxis = semiMajorAxis_SU;
        _semiMinorAxis = semiMinorAxis_SU;
        _orientation = orientation_SU;
        _gaussianPdf = new PdfContGaussianBoxMuller( 0.0, 1.0 );
        _cosOrient = Math.cos( Azimuth.toNavRad( _orientation ) );
        _sinOrient = Math.sin( Azimuth.toNavRad( _orientation ) );
    }


    @Override
    public double[] draw( Generator g )
    {
        double y1 = _semiMajorAxis * _gaussianPdf.draw( g );
        double y2 = _semiMinorAxis * _gaussianPdf.draw( g );
        double x1 = ( y1 * _sinOrient ) - ( y2 * _cosOrient );
        double x2 = ( y1 * _cosOrient ) + ( y2 * _sinOrient );

        return new double[ ] { x1, x2 };
    }

    @Override
    public int hashCode( )
    {
        final int prime = 39097;
        int result = 1;
        result = prime * result + Double.hashCode( _semiMajorAxis );
        result = prime * result + Double.hashCode( _semiMinorAxis );
        result = prime * result + Double.hashCode( _orientation );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        PdfCont2dBivariateGaussianViaEllipse other = ( PdfCont2dBivariateGaussianViaEllipse ) o;
        return ( doublesEqual( other._semiMajorAxis, _semiMajorAxis )
              && doublesEqual( other._semiMinorAxis, _semiMinorAxis )
              && doublesEqual( other._orientation, _orientation ) );
    }
}
