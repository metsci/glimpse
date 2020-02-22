/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.util.var2;

import static com.metsci.glimpse.util.var2.VarUtils.doHandleImmediateFlag;
import static com.metsci.glimpse.util.var2.VarUtils.filterListenable;
import static com.metsci.glimpse.util.var2.VarUtils.filterListener;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Set;

import com.metsci.glimpse.util.var.Disposable;

public abstract class ReadableVarDerived<V> implements ReadableVar<V>
{

    protected final ActivityListenableSet listenables;
    protected final Listenable completed;
    protected final Listenable all;


    @SafeVarargs
    public ReadableVarDerived( ActivityListenable... listenables )
    {
        this( asList( listenables ) );
    }

    public ReadableVarDerived( Collection<? extends ActivityListenable> listenables )
    {
        this.listenables = new ActivityListenableSet( listenables );
        this.completed = filterListenable( this.listenables.completed( ), this::v );
        this.all = filterListenable( this.listenables.all( ), this::v );
    }

    @Override
    public abstract V v( );

    @Override
    public Listenable completed( )
    {
        return this.completed;
    }

    @Override
    public Listenable all( )
    {
        return this.all;
    }

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, ActivityListener listener )
    {
        return doHandleImmediateFlag( flags, listener, flags2 ->
        {
            return this.listenables.addListener( flags2, filterListener( listener, this::v ) );
        } );
    }

}
