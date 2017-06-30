package com.metsci.glimpse.support.wrapped;

public class NoopWrapper1D implements Wrapper1D
{

    public static final Wrapper1D NOOP_WRAPPER_1D = new NoopWrapper1D( );


    private NoopWrapper1D( )
    { }

    @Override
    public double wrapMin( )
    {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double wrapMax( )
    {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double wrapValue( double value )
    {
        return value;
    }

    @Override
    public double wrapNear( double ref, double value )
    {
        return value;
    }

    @Override
    public double wrapDelta( double delta )
    {
        return delta;
    }

    @Override
    public double[] getRenderShifts( double minValue, double maxValue )
    {
        return new double[] { 0.0 };
    }

    @Override
    public int hashCode( )
    {
        // Singleton, so just use Object identity
        return super.hashCode( );
    }

    @Override
    public boolean equals( Object o )
    {
        // Singleton, so just use Object identity
        return super.equals( o );
    }

}
