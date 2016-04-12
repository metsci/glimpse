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

import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.util.logging.Logger;

/**
 * @author moskowitz, sherman
 */

// See http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm

public class StatCollectorNDim
{
    private static Logger logger = Logger.getLogger( StatCollectorNDim.class.getName( ) );
    private static String SIZE_MESSAGE = "Incompatible size for entry in StatCollectorNDim";
    private static final double EPSILON = 1.0e-6;

    private final int dim;
    private int nsamples;
    private double count;
    private final double[] mean;
    private final double[][] M2;
    private final double[] deltas;

    public StatCollectorNDim( int dim )
    {
        this.dim = dim;
        this.count = 0.0;
        this.nsamples = 0;
        this.deltas = new double[dim];
        this.mean = new double[dim];
        this.M2 = new double[dim][];

        for ( int i = 0; i < dim; i++ )
        {
            this.M2[i] = new double[i + 1];
        }
    }

    public void addElement( double[] x )
    {
        this.addElement( x, 1.0 );
    }

    public void addElement( double[] x, double weight )
    {

        if ( x.length != dim )
        {
            throw new RuntimeException( SIZE_MESSAGE );
        }

        nsamples++;

        count += weight;

        for ( int i = 0; i < dim; i++ )
        {
            deltas[i] = x[i] - mean[i];
            mean[i] += deltas[i] * weight / count;

            for ( int j = 0; j <= i; j++ )
            {
                M2[i][j] += deltas[i] * ( x[j] - mean[j] ) * weight;
            }
        }

    }

    public double getCount( )
    {
        return count;
    }

    public int getNumSamples( )
    {
        return nsamples;
    }

    public double[] getMean( )
    {
        double[] retMean = new double[dim];
        System.arraycopy( mean, 0, retMean, 0, dim );
        return retMean;
    }

    public double[][] getCovariance( )
    {
        double[][] covariance = new double[dim][dim];

        for ( int i = 0; i < dim; i++ )
        {
            for ( int j = 0; j <= i; j++ )
            {
                double cov = M2[i][j] / count;
                if ( i == j )
                {
                    if ( cov < 0 )
                    {
                        if ( cov < -EPSILON )
                        {
                            logWarning( logger, "negative diagonal cov %s, replacing with 0", cov );
                        }
                        else
                        {
                            logFine( logger, "negative diagonal cov %s, replacing with 0", cov );
                        }

                        cov = 0.0;
                    }
                }

                covariance[i][j] = cov;
                covariance[j][i] = cov;
            }
        }

        return covariance;
    }

    /**
     * For the two dimensional case, this routine will return the parameters defining the unit
     * covariance ellipse for a Bivariate Gaussian distribution centered about the mean with sigmaX,
     * sigmaY, and correlation estimated by this stat collector.  The unit covariance ellipse is a
     * constant probability contour enclosing a 1-sigma area.  Note that the ellipse is based on an
     * assumption that the distribution is approximately a Bivariate Gaussian.  If this is not the
     * case then its use may be inappropriate.
     *
     * @return  array of size 3: {semi-major axis length, semi-minor axis length, orientation} of
     *          1-sigma covariance ellipse
     */
    public double[] getTwoDimUnitCovarianceEllipse( )
    {
        if ( dim != 2 )
        {
            throw new RuntimeException( "dimension must be 2 for getTwoDimUnitCovarianceEllipse" );
        }

        return getTwoDimUnitCovarianceEllipseLenient( );
    }

    /**
     * This routine will return the parameters defining the unit covariance ellipse for a Bivariate
     * Gaussian distribution centered about the mean with sigmaX, sigmaY, and correlation estimated
     * by this stat collector. This 'lenient' version does not check for exactly a two dimensional
     * case but simply uses the first two dimensions.
     *
     * @return  array of size 3: {semi-major axis length, semi-minor axis length, orientation} of
     *          1-sigma covariance ellipse
     */
    public double[] getTwoDimUnitCovarianceEllipseLenient( )
    {
        if ( dim < 2 )
        {
            throw new RuntimeException( "dimension must be at least 2 for getTwoDimUnitCovarianceEllipse" );
        }

        double[][] cov = getCovariance( );

        double sigmaX = Math.sqrt( cov[0][0] );
        double sigmaY = Math.sqrt( cov[1][1] );
        double corr = cov[0][1] / ( sigmaX * sigmaY );
        corr = checkAndCorrectCorrelation( corr );

        return BivariateGaussianDistributionUtils.toUnitCovarianceEllipse( sigmaX, sigmaY, corr );
    }

    /**
     * This routine will return the principal axes for the distribution in the two dimensional case.
     * Obviously, it would be useful to have a method for the general case, but that is not
     * currently implemented. It will require a general eigenvector routine. Note that the three
     * dimensional case will also not be too difficult.
     */
    public double[][] getTwoDimAxes( )
    {
        if ( dim != 2 )
        {
            throw new RuntimeException( "dimension must be 2 to call getTwoDimAxes" );
        }

        return getTwoDimAxesLenient( );
    }

    /**
     * This routine will return the principal axes for the distribution in the two dimensional case.
     * This 'lenient' version does not check for exactly a two dimensional case but simply uses the
     * first two dimensions.
     */
    public double[][] getTwoDimAxesLenient( )
    {
        if ( dim < 2 )
        {
            throw new RuntimeException( "dimension must be at least 2 to call getTwoDimAxesLenient" );
        }

        double[][] axes = new double[2][2];

        double[][] cov = getCovariance( );
        double determinant = ( cov[0][0] * cov[1][1] ) - ( cov[0][1] * cov[1][0] );
        double trace = cov[0][0] + cov[1][1];
        double d = Math.sqrt( Math.max( ( trace * trace ) - ( 4 * determinant ), 0 ) );

        // Calculate eigenvalues
        double lambda1 = ( trace + d ) / 2;
        double lambda2 = ( trace - d ) / 2;

        // Store eigenvector for major axis (smaller eigenvalue)
        axes[0][0] = -cov[0][1];

        // axes[0][1] = cov[0][0] - lambda2;
        axes[0][1] = cov[0][0] - lambda1;

        double norm = Math.sqrt( ( axes[0][0] * axes[0][0] ) + ( axes[0][1] * axes[0][1] ) );
        axes[0][0] /= norm;
        axes[0][1] /= norm;
        axes[0][0] *= Math.sqrt( lambda1 );
        axes[0][1] *= Math.sqrt( lambda1 );

        // Store eigenvector for minor axis (larger eigenvalue)
        axes[1][0] = -cov[0][1];

        // axes[1][1] = cov[0][0] - lambda1;
        axes[1][1] = cov[0][0] - lambda2;
        norm = Math.sqrt( ( axes[1][0] * axes[1][0] ) + ( axes[1][1] * axes[1][1] ) );
        axes[1][0] /= norm;
        axes[1][1] /= norm;
        axes[1][0] *= Math.sqrt( lambda2 );
        axes[1][1] *= Math.sqrt( lambda2 );

        return axes;
    }

    public static void main( String[] args )
    {

        // Simple example of how StatCollectorNDim is used...
        StatCollectorNDim stats = new StatCollectorNDim( 2 );
        double[] vals = new double[2];
        for ( int i = 0; i < 1000; i++ )
        {
            vals[0] = ( i % 10 ) + ( 0.2 * Math.random( ) );
            vals[1] = ( ( i + 5 ) % 10 ) + ( 0.4 * Math.random( ) );
            stats.addElement( vals );
        }

        double[] mean = stats.getMean( );
        double[][] cov = stats.getCovariance( );

        System.out.printf( "results:%ncount = %.0f%nmean = [%.4f, %.4f]%n", stats.getCount( ), mean[0], mean[1] );
        System.out.printf( "covariance:%n\t%8.4f\t%8.4f%n\t%8.4f\t%8.4f%n", cov[0][0], cov[0][1], cov[1][0], cov[1][1] );
    }

    /**
     * Check for valid correlation value.  Log and correct if invalid.
     */
    private double checkAndCorrectCorrelation( double correlation )
    {
        if ( correlation > 1.0 )
        {
            if ( correlation > ( 1.0 + EPSILON ) )
            {
                logWarning( logger, "correlation %s > 1.0, replacing with 1.0", correlation );
            }
            else
            {
                logFine( logger, "correlation %s > 1.0, replacing with 1.0", correlation );
            }

            correlation = 1.0;
        }
        else if ( correlation < -1.0 )
        {
            if ( correlation < - ( 1.0 + EPSILON ) )
            {
                logWarning( logger, "correlation %s < -1.0, replacing with -1.0", correlation );
            }
            else
            {
                logFine( logger, "correlation %s < -1.0, replacing with -1.0", correlation );
            }

            correlation = -1.0;
        }
        else if ( Double.isNaN( correlation ) )
        {
            logWarning( logger, "correlation NaN, replacing with 0.0" );
            correlation = 0.0;
        }

        return correlation;
    }
}
