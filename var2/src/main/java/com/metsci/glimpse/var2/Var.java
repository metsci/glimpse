package com.metsci.glimpse.var2;

import java.util.function.Function;

public interface Var<V> extends ReadableVar<V>
{

    boolean set( boolean ongoing, V value );

    default boolean set( V value )
    {
        return this.set( false, value );
    }

    default boolean update( boolean ongoing, Function<? super V,? extends V> updateFn )
    {
        return this.set( ongoing, updateFn.apply( this.v( ) ) );
    }

    default boolean update( Function<? super V,? extends V> updateFn )
    {
        return this.update( false, updateFn );
    }

    default boolean updateIfNonNull( boolean ongoing, Function<? super V,? extends V> updateFn )
    {
        V v = this.v( );
        if ( v != null )
        {
            return this.set( ongoing, updateFn.apply( v ) );
        }
        else
        {
            return false;
        }
    }

    default boolean updateIfNonNull( Function<? super V,? extends V> updateFn )
    {
        return this.updateIfNonNull( false, updateFn );
    }

}
