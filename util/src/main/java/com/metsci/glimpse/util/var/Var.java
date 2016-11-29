package com.metsci.glimpse.util.var;

import static com.google.common.base.Objects.*;
import static com.metsci.glimpse.util.PredicateUtils.*;

import java.util.function.Function;
import java.util.function.Predicate;

public class Var<V> extends Notifier<VarEvent>
{

    public final Predicate<? super V> validateFn;

    protected V value;
    protected boolean ongoing;


    public Var( V value )
    {
        this( value, alwaysTrue );
    }

    public Var( V value, Predicate<? super V> validateFn )
    {
        this.validateFn = validateFn;

        this.value = require( value, validateFn );
        this.ongoing = false;
    }

    public V v( )
    {
        return this.value;
    }

    public V set( V value )
    {
        return this.set( false, value );
    }

    public V set( boolean ongoing, V value )
    {
        // Update if value has changed, or if changes were previously ongoing but no longer are
        if ( ( !ongoing && this.ongoing ) || !equal( value, this.value ) )
        {
            this.value = require( value, this.validateFn );
            this.ongoing = ongoing;
            this.fire( new VarEvent( ongoing ) );
        }
        return this.value;
    }

    public V update( Function<? super V,? extends V> updateFn )
    {
        return this.update( false, updateFn );
    }

    public V update( boolean ongoing, Function<? super V,? extends V> updateFn )
    {
        return this.set( ongoing, updateFn.apply( this.value ) );
    }

}
