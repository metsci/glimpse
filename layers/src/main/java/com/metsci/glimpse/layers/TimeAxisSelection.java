package com.metsci.glimpse.layers;

public class TimeAxisSelection
{

    public final long min_PMILLIS;
    public final long max_PMILLIS;
    public final long cursor_PMILLIS;

    public TimeAxisSelection( long min_PMILLIS, long max_PMILLIS, long cursor_PMILLIS )
    {
        this.min_PMILLIS = min_PMILLIS;
        this.max_PMILLIS = max_PMILLIS;
        this.cursor_PMILLIS = cursor_PMILLIS;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 2207;
        int result = 1;
        result = prime * result + Long.hashCode( this.min_PMILLIS );
        result = prime * result + Long.hashCode( this.max_PMILLIS );
        result = prime * result + Long.hashCode( this.cursor_PMILLIS );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        TimeAxisSelection other = ( TimeAxisSelection ) o;
        return ( other.min_PMILLIS == this.min_PMILLIS
              && other.max_PMILLIS == this.max_PMILLIS
              && other.cursor_PMILLIS == this.cursor_PMILLIS );
    }

}
