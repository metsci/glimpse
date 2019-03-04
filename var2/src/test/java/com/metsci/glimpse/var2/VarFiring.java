package com.metsci.glimpse.var2;

import java.util.Objects;

class VarFiring<V>
{

    public final boolean ongoing;
    public final V value;


    public VarFiring( boolean ongoing, V value )
    {
        this.ongoing = ongoing;
        this.value = value;
    }

    @Override
    public String toString( )
    {
        return ( ( this.ongoing ? "ongoing" : "completed" ) + ":" + this.value.toString( ) );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 16061;
        int result = 1;
        result = prime * result + Boolean.hashCode( this.ongoing );
        result = prime * result + Objects.hashCode( this.value );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        VarFiring<?> other = ( VarFiring<?> ) o;
        return ( other.ongoing == this.ongoing
              && Objects.equals( other.value, this.value ) );
    }

}
