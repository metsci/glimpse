package com.metsci.glimpse.layers;

import com.metsci.glimpse.axis.Axis1D;

public class AxisSelection1D
{

    public static AxisSelection1D axisSelection1D( Axis1D axis )
    {
        return new AxisSelection1D( axis.getSelectionCenter( ), axis.getSelectionSize( ) );
    }


    public final double center;
    public final double size;

    public final double min;
    public final double max;

    public AxisSelection1D( double center, double size )
    {
        this.center = center;
        this.size = size;

        this.min = center - 0.5*size;
        this.max = center + 0.5*size;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 6791;
        int result = 1;
        result = prime * result + Double.hashCode( this.center );
        result = prime * result + Double.hashCode( this.size );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        AxisSelection1D other = ( AxisSelection1D ) o;
        return ( Double.compare( other.center, this.center ) == 0
              && Double.compare( other.size, this.size ) == 0 );
    }

}
