package com.metsci.glimpse.var2;

import static com.google.common.base.Objects.equal;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link ReadableVarDerived} that only recomputes its value when
 * upstream vars (i.e. vars from which this var is derived) change.
 */
public abstract class ReadableVarDerivedCaching<V> extends ReadableVarDerived<V>
{

    protected final Map<ReadableVar<?>,Object> upstream;
    protected V value;


    @SafeVarargs
    public ReadableVarDerivedCaching( ReadableVar<?>... upstreamVars )
    {
        this( asList( upstreamVars ) );
    }

    public ReadableVarDerivedCaching( Collection<? extends ReadableVar<?>> upstreamVars )
    {
        super( upstreamVars );

        this.upstream = new HashMap<>( );
        for ( ReadableVar<?> upstreamVar : upstreamVars )
        {
            this.upstream.put( upstreamVar, upstreamVar.v( ) );
        }

        this.value = this.compute( );
    }

    @Override
    public V v( )
    {
        boolean anyUpstreamChanges = false;
        for ( Entry<ReadableVar<?>,Object> en : this.upstream.entrySet( ) )
        {
            ReadableVar<?> upstreamVar = en.getKey( );
            Object oldUpstreamValue = en.getValue( );
            Object newUpstreamValue = upstreamVar.v( );
            if ( !equal( newUpstreamValue, oldUpstreamValue ) )
            {
                this.upstream.put( upstreamVar, newUpstreamValue );
                anyUpstreamChanges = true;
            }
        }

        if ( anyUpstreamChanges )
        {
            this.value = this.compute( );
        }

        return this.value;
    }

    protected abstract V compute( );

}
