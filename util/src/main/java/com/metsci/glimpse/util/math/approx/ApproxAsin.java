package com.metsci.glimpse.util.math.approx;

import static java.lang.Math.PI;
import static com.metsci.glimpse.util.math.MathConstants.*;

/**
 * Similar to the classes in {@link com.metsci.glimpse.util.math.fast}, but uses linear interpolation
 * between samples instead of nearest-neighbor.
 */
public class ApproxAsin
{
    public static final double ONE_OVER_TWO_PI = 1.0 / TWO_PI;
    public static final double PI_OVER_2 = PI / 2.0;


    protected final int n;
    protected final double xStep;
    protected final double oneOverXStep;
    protected final double[] y;


    public ApproxAsin( int numSamples )
    {
        this.n = numSamples;
        this.xStep = 2.0 / ( this.n - 1 );
        this.oneOverXStep = 1.0 / this.xStep;

        this.y = new double[ this.n ];

        // Set first and last values explicitly
        this.y[ 0 ] = -HALF_PI;
        this.y[ this.n - 1 ] = +HALF_PI;

        for ( int i = 1; i < this.n - 1; i++ )
        {
            double x = -1.0 + ( i * this.xStep );
            this.y[ i ] = Math.asin( x );
        }
    }
    
    // Helper method for taking advantage of the precalculated asin values to
    // compute acos values. Requires an additional subtraction over creating
    // an ApproxAcos class, but requires no additional storage.
    public double acos( double x )
    {
        return PI_OVER_2 - asin( x );
    }

    public double asin( double x )
    {
        if ( Double.isNaN( x ) || x < -1.0 || x > +1.0 )
        {
            return Double.NaN;
        }

        // How many steps is x above xMin
        double w = ( x - (-1.0) ) * this.oneOverXStep;

        int iBefore = ( int ) w;
        double yBefore = this.y[ iBefore ];
        double yAfter = this.y[ iBefore + 1 ];
        double xFrac = w - iBefore;

        return ( yBefore + ( xFrac * ( yAfter - yBefore ) ) );
    }

}
