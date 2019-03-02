package com.metsci.glimpse.var2;

import static java.util.Arrays.asList;

import java.util.Collection;

public abstract class ReadableVarDerived<V> extends ListenablePairSet implements ReadableVar<V>
{

    @SafeVarargs
    public ReadableVarDerived( ListenablePair... listenables )
    {
        this( asList( listenables ) );
    }

    public ReadableVarDerived( Collection<? extends ListenablePair> listenables )
    {
        super( listenables );
    }

    @Override
    public abstract V v( );

}
