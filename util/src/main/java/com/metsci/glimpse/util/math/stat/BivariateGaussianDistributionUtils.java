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
package com.metsci.glimpse.util.math.stat;

import com.metsci.glimpse.util.units.Angle;
import com.metsci.glimpse.util.units.Azimuth;

/**
 * This class contains a collection of static methods for dealing with Bivariate Gaussian
 * distributions.
 *
 * @author  moskowitz
 */
public class BivariateGaussianDistributionUtils
{

    /**
     * Returns the parameters defining the unit covariance ellipse for a Bivariate Gaussian
     * distribution centered about the origin with given sigmaX, sigmaY, and correlation.  The unit
     * covariance ellipse is a constant probability contour enclosing a 1-sigma area (probability of
     * being inside the ellipse approximately equals 39.35%).
     *
     * <p>Note: correlation must be <= 1 and >= -1.</p>
     *
     * <p>Ref:  Data Analysis for Scientists and Engineers, Stuart Meyer, 1975, p.288-290.</p>
     *
     * @param   sigmaX       X standard deviation = Sqrt[Var(X)]
     * @param   sigmaY       Y standard deviation = Sqrt[Var(Y)]
     * @param   correlation  correlation coefficient = Cov(X,Y) / (sigmaX sigmaY)
     * @return  array of size 3: {semi-major axis length, semi-minor axis length, orientation} of
     *          1-sigma covariance ellipse
     * @throws  IllegalArgumentException  if correlation is outside valid domain.
     */
    public static double[] toUnitCovarianceEllipse( double sigmaX, double sigmaY, double correlation )
    {
        assert ( correlation <= 1 ) && ( correlation >= -1 );

        double sigmaXSq = sigmaX * sigmaX;
        double sigmaYSq = sigmaY * sigmaY;

        double theta = 0.5 * Math.atan2( 2 * correlation * sigmaX * sigmaY, sigmaXSq - sigmaYSq );

        double sin = Math.sin( theta );
        double cos = Math.cos( theta );
        double sinSq = sin * sin;
        double cosSq = cos * cos;
        double sinDbl = 2.0 * sin * cos; // formula: sin(2z) = 2 sin(z) cos(z)

        double rxSq;
        double rySq;
        if ( ( correlation < 1 ) && ( correlation > -1 ) )
        {
            double numerator = sigmaXSq * sigmaYSq * ( 1.0 - ( correlation * correlation ) );
            double denomX = ( sigmaYSq * cosSq ) - ( correlation * sigmaX * sigmaY * sinDbl ) + ( sigmaXSq * sinSq );
            double denomY = ( sigmaYSq * sinSq ) + ( correlation * sigmaX * sigmaY * sinDbl ) + ( sigmaXSq * cosSq );

            rxSq = numerator / denomX;
            rySq = numerator / denomY;
        }
        else // Special case, reduces to 1D
        {
            rxSq = sigmaXSq + sigmaYSq;
            rySq = 0;
        }

        double semiMajSq = rxSq;
        double semiMinSq = rySq;
        double orientation = Azimuth.fromMathRad( theta );

        // semiMajor axis should be >= semiMinor, swap and adjust orientation if needed
        if ( semiMajSq < semiMinSq )
        {
            semiMajSq = rySq;
            semiMinSq = rxSq;
            orientation = orientation + Angle.rightAngle; // symmetric, direction doesn't matter
        }

        return new double[] { Math.sqrt( semiMajSq ), Math.sqrt( semiMinSq ), orientation };
    }

    /**
     * Given the parameters defining the unit covariance ellipse for a Bivariate Gaussian
     * distribution centered about the origin, this method returns sigmaX = Sqrt[Var(X)], sigmaY =
     * Sqrt[Var(Y)], and correlation = Cov(X,Y) / (sigmaX sigmaY) for this probability distribution.
     * The unit covariance ellipse is a constant probability contour enclosing a 1-sigma area
     * (probability of being inside the ellipse approximately equals 39.35%).
     *
     * <p>Note: Calculation is based on COV = Q' R Q where Q is rotation matrix {cos,sin;-sin, cos}
     * for given orientation and R is diagonal matrix {semiMaj, 0; 0, semiMin} for axis lengths.
     * With COV computed, we get sigmaX = Sqrt[COV(0,0)], sigmaY = Sqrt[COV(1,1)], and finally
     * correlation = COV(0,1)/(sigmaX * sigmaY).</p>
     *
     * @param   semiMajorAxisLength  length of semi-major axis of 1-sigma covariance ellipse
     * @param   semiMinorAxisLength  length of semi-minor axis of 1-sigma covariance ellipse
     * @param   orientation          orientation of covariance ellipse (azimuth of semi-major axis)
     * @return  array of size 3: {sigmaX, sigmaY, correlation}
     */
    public static double[] fromUnitCovarianceEllipse( double semiMajorAxisLength, double semiMinorAxisLength, double orientation )
    {
        double rxSq = semiMajorAxisLength * semiMajorAxisLength;
        double rySq = semiMinorAxisLength * semiMinorAxisLength;
        double az = Azimuth.toMathRad( orientation );
        double sin = Math.sin( az );
        double cos = Math.cos( az );
        double sinSq = sin * sin;
        double cosSq = cos * cos;

        double sigmaXSq = ( rxSq * cosSq ) + ( rySq * sinSq );
        double sigmaYSq = ( rxSq * sinSq ) + ( rySq * cosSq );
        double sigmaXY = ( rxSq - rySq ) * sin * cos;

        double sigmaX = Math.sqrt( sigmaXSq );
        double sigmaY = Math.sqrt( sigmaYSq );
        double correlation = sigmaXY / ( sigmaX * sigmaY );

        return new double[] { sigmaX, sigmaY, correlation };
    }

    public static void main( String[] args )
    {
        double tolerance = 1.0E-8;

        // Compute some simple examples and check for round trip consistency ...
        double[] c1;

        c1 = new double[] { 10, 10, 0 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 20, 10, 0 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 20, 10, .3 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 1, 10, .3 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 20, 10, -.7 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 20, 10, .99 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 20, 10, .999999 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 20, 10, 1 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 1, 1, -.999999 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 1, 1, -1 };
        runTestCase( tolerance, c1 );

        c1 = new double[] { 12.5, 17.5, .2928 };
        runTestCase( tolerance, c1 );
    }

    private static void runTestCase( double tolerance, double[] c1 )
    {
        double[] e1;
        double[] c2;
        e1 = toUnitCovarianceEllipse( c1[0], c1[1], c1[2] );
        c2 = fromUnitCovarianceEllipse( e1[0], e1[1], e1[2] );
        System.out.printf( "sigmaX  %.8f sigmaY  %.8f corr   %.8f%n", c1[0], c1[1], c1[2] );
        System.out.printf( "semiMaj %.8f semiMin %.8f orient %.8f%n", e1[0], e1[1], e1[2] );
        System.out.printf( "sigmaX  %.8f sigmaY  %.8f corr   %.8f%n%n", c2[0], c2[1], c2[2] );
        assert Math.abs( c1[0] - c2[0] ) < tolerance;
        assert Math.abs( c1[1] - c2[1] ) < tolerance;
        assert Math.abs( c1[2] - c2[2] ) < tolerance;
    }
}
