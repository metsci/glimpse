package com.metsci.glimpse.var2;

import static java.util.Arrays.asList;

import java.util.Collection;

public abstract class VarDerived<V> extends ReadableVarDerived<V> implements Var<V>
{

    @SafeVarargs
    public VarDerived( ListenablePair... listenables )
    {
        this( asList( listenables ) );
    }

    public VarDerived( Collection<? extends ListenablePair> listenables )
    {
        super( listenables );
    }

    @Override
    public abstract boolean set( boolean ongoing, V value );

}
