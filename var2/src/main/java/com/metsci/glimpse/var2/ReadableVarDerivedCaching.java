/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
