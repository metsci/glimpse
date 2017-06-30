package com.metsci.glimpse.support.wrapped;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;

public class StandardWrapper1D implements Wrapper1D
{

    public final double wrapMin;
    public final double wrapMax;


    /**
     * Client code should generally NOT call this constructor, but should use
     * {@link Wrapper1D#getWrapper(com.metsci.glimpse.axis.Axis1D)} instead.
     */
    public StandardWrapper1D( double wrapMin, double wrapMax )
    {
        this.wrapMin = wrapMin;
        this.wrapMax = wrapMax;
    }

    @Override
    public double wrapMin( )
    {
        return this.wrapMin;
    }

    @Override
    public double wrapMax( )
    {
        return this.wrapMax;
    }

    @Override
    public double wrapValue( double value )
    {
        double wrapSpan = this.wrapMax - this.wrapMin;
        double wrapCount = floor( ( value - this.wrapMin ) / wrapSpan );
        return ( value - ( wrapCount * wrapSpan ) );
    }

    @Override
    public double wrapNear( double ref, double value )
    {
        return ( ref + this.wrapDelta( value - ref ) );
    }

    @Override
    public double wrapDelta( double delta )
    {
        double wrapSpan = this.wrapMax - this.wrapMin;
        double wrapCount = floor( delta / wrapSpan );
        double deltaA = delta - ( wrapCount * wrapSpan );
        double deltaB = delta - ( ( wrapCount + 1.0 ) * wrapSpan );
        return ( abs( deltaA ) <= abs( deltaB ) ? deltaA : deltaB );
    }

    @Override
    public double[] getRenderShifts( double minValue, double maxValue )
    {
        double first = this.wrapValue( minValue ) - minValue;
        double step = this.wrapMax - this.wrapMin;
        int count = ( int ) max( 0, ceil( ( ( maxValue + first ) - this.wrapMin ) / step ) );

        double[] shifts = new double[ count ];
        for ( int i = 0; i < count; i++ )
        {
            double offset = first - i*step;
            shifts[ i ] = offset;
        }
        return shifts;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 83389;
        int result = 1;
        result = prime * result + Double.hashCode( this.wrapMin );
        result = prime * result + Double.hashCode( this.wrapMax );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        StandardWrapper1D other = ( StandardWrapper1D ) o;
        return ( Double.compare( other.wrapMin, this.wrapMin ) == 0
              && Double.compare( other.wrapMax, this.wrapMax ) == 0 );
    }

}
