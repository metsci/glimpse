package com.metsci.glimpse.layers;

import static com.metsci.glimpse.layers.AxisSelection1D.axisSelection1D;

import java.util.Objects;

import com.metsci.glimpse.axis.Axis2D;

public class AxisSelection2D
{

    public static AxisSelection2D axisSelection2D( Axis2D axis )
    {
        return new AxisSelection2D( axisSelection1D( axis.getAxisX( ) ), axisSelection1D( axis.getAxisY( ) ) );
    }


    public final AxisSelection1D xSelection;
    public final AxisSelection1D ySelection;

    public AxisSelection2D( AxisSelection1D xSelection, AxisSelection1D ySelection )
    {
        this.xSelection = xSelection;
        this.ySelection = ySelection;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 7039;
        int result = 1;
        result = prime * result + Objects.hashCode( this.xSelection );
        result = prime * result + Objects.hashCode( this.ySelection );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        AxisSelection2D other = ( AxisSelection2D ) o;
        return ( Objects.equals( other.xSelection, this.xSelection )
              && Objects.equals( other.ySelection, this.ySelection ) );
    }

}
