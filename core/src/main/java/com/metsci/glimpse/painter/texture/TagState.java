package com.metsci.glimpse.painter.texture;

import static com.metsci.glimpse.util.GeneralUtils.floatsEqual;

public class TagState
{

    public final float fraction;
    public final float value;


    public TagState( float fraction, float value )
    {
        this.fraction = fraction;
        this.value = value;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 99017;
        int result = 1;
        result = prime * result + Float.hashCode( this.fraction );
        result = prime * result + Float.hashCode( this.value );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        TagState other = ( TagState ) o;
        return ( floatsEqual( other.fraction, this.fraction )
              && floatsEqual( other.value, this.value ) );
    }

    @Override
    public String toString( )
    {
        return ( getClass( ).getSimpleName( ) + "[" + this.fraction + ":" + this.value + "]" );
    }

}
