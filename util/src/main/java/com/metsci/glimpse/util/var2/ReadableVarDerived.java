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
package com.metsci.glimpse.util.var2;

import static com.metsci.glimpse.util.var2.VarUtils.completedListenable;
import static com.metsci.glimpse.util.var2.VarUtils.doAddPairListener;
import static com.metsci.glimpse.util.var2.VarUtils.doHandleImmediateFlag;
import static com.metsci.glimpse.util.var2.VarUtils.filterListenable;
import static com.metsci.glimpse.util.var2.VarUtils.filterListener;
import static com.metsci.glimpse.util.var2.VarUtils.listenable;
import static com.metsci.glimpse.util.var2.VarUtils.mapCollection;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Set;

import com.metsci.glimpse.util.var.Disposable;

public abstract class ReadableVarDerived<V> implements ReadableVar<V>
{

    protected final Listenable ongoingRaw;
    protected final Listenable completedRaw;
    protected final Listenable allRaw;

    protected final Listenable ongoingFiltered;
    protected final Listenable completedFiltered;
    protected final Listenable allFiltered;


    @SafeVarargs
    public ReadableVarDerived( ListenablePair... listenables )
    {
        this( asList( listenables ) );
    }

    public ReadableVarDerived( Collection<? extends ListenablePair> listenables )
    {
        this.ongoingRaw = ongoingListenable( listenables );
        this.completedRaw = completedListenable( listenables );
        this.allRaw = listenable( this.ongoingRaw, this.completedRaw );

        this.completedFiltered = filterListenable( this.completedRaw, this::v );
        this.ongoingFiltered = filterListenable( this.ongoingRaw, this::v );
        this.allFiltered = filterListenable( this.allRaw, this::v );
    }

    @Deprecated
    protected static Listenable ongoingListenable( Collection<? extends ListenablePair> pairs )
    {
        return listenable( mapCollection( pairs, ListenablePair::ongoing ) );
    }

    @Override
    public abstract V v( );

    @Deprecated
    @Override
    public Listenable ongoing( )
    {
        return this.ongoingFiltered;
    }

    @Override
    public Listenable completed( )
    {
        return this.completedFiltered;
    }

    @Override
    public Listenable all( )
    {
        return this.allFiltered;
    }

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, ListenablePairListener listener )
    {
        return doHandleImmediateFlag( flags, listener, flags2 ->
        {
            ListenablePairListener listener2 = filterListener( listener, this::v );
            return doAddPairListener( this.ongoingRaw, this.completedRaw, flags2, listener2 );
        } );
    }

}
