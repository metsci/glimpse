package com.metsci.glimpse.util.math.approx;

import static com.metsci.glimpse.util.math.MathConstants.*;
import static java.lang.Math.*;

/**
 * Similar to the classes in {@link com.metsci.glimpse.util.math.fast}, but uses linear interpolation
 * between samples instead of nearest-neighbor.
 * <p>
 * Anecdotally, speed is about 12x faster than {@link Math#cos(double)}. With 100k samples, max error
 * is around 5e-10. Max error decreases as the number of samples increases.
 */
public class ApproxCos
{
    public static final double ONE_OVER_TWO_PI = 1.0 / TWO_PI;


    protected final int n;
    protected final double xMin;
    protected final double xMax;
    protected final double xStep;
    protected final double oneOverXStep;
    protected final double[] y;


    public ApproxCos( int numSamples )
    {
        double xMinPrelim = 0.0;
        double xMaxPrelim = TWO_PI;
        double xStepPrelim = ( xMaxPrelim - xMinPrelim ) / ( numSamples - 1 );

        // Avoid edge effects by tacking on an extra sample at each end
        this.n = numSamples + 2;
        this.xMin = xMinPrelim - xStepPrelim;
        this.xMax = xMaxPrelim + xStepPrelim;
        this.xStep = ( this.xMax - this.xMin ) / ( this.n - 1 );
        this.oneOverXStep = 1.0 / this.xStep;

        this.y = new double[ this.n ];
        for ( int i = 0; i < this.n; i++ )
        {
            double x = this.xMin + ( i * this.xStep );
            this.y[ i ] = Math.cos( x );
        }
    }

    public static double normalizeAngleTwoPi( double x_RAD )
    {
        return ( x_RAD - ( TWO_PI * floor( x_RAD * ONE_OVER_TWO_PI ) ) );
    }

    public double cos( double x_RAD )
    {
        double x = normalizeAngleTwoPi( x_RAD );

        // How many steps is x above xMin
        double w = ( x - this.xMin ) * this.oneOverXStep;

        int iBefore = ( int ) w;
        double yBefore = this.y[ iBefore ];
        double yAfter = this.y[ iBefore + 1 ];
        double xFrac = w - iBefore;

        return ( yBefore + ( xFrac * ( yAfter - yBefore ) ) );
    }

}
