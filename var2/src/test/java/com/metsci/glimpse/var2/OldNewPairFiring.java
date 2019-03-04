package com.metsci.glimpse.var2;

import java.util.Objects;

class OldNewPairFiring<V>
{

    public final boolean ongoing;
    public final V vOld;
    public final V vNew;


    public OldNewPairFiring( boolean ongoing, V vOld, V vNew )
    {
        this.ongoing = ongoing;
        this.vOld = vOld;
        this.vNew = vNew;
    }

    @Override
    public String toString( )
    {
        return ( ( this.ongoing ? "ongoing" : "completed" ) + ":" + this.vOld.toString( ) + "→" + this.vNew.toString( ) );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 12611;
        int result = 1;
        result = prime * result + Boolean.hashCode( this.ongoing );
        result = prime * result + Objects.hashCode( this.vOld );
        result = prime * result + Objects.hashCode( this.vNew );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        OldNewPairFiring<?> other = ( OldNewPairFiring<?> ) o;
        return ( other.ongoing == this.ongoing
              && Objects.equals( other.vOld, this.vOld )
              && Objects.equals( other.vNew, this.vNew ) );
    }

}
