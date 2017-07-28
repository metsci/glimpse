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
package com.metsci.glimpse.util.var;

import static com.google.common.base.Objects.equal;
import static com.google.common.collect.Sets.difference;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;

public class VarUtils
{

    public static <V> Disposable addOldNewListener( ReadableVar<? extends V> var, boolean runImmediately, BiConsumer<? super V, ? super V> oldNewListener )
    {
        return var.addListener( runImmediately, new Runnable( )
        {
            private V valueOld = null;

            @Override
            public void run( )
            {
                V valueNew = var.v( );

                if ( !equal( valueNew, valueOld ) )
                {
                    oldNewListener.accept( valueOld, valueNew );
                }

                this.valueOld = valueNew;
            }
        } );
    }

    public static <T> Disposable addElementAddedListener( ReadableVar<? extends Collection<? extends T>> var, boolean runImmediately, Consumer<? super T> listener )
    {
        return var.addListener( runImmediately, new Runnable( )
        {
            private Set<T> valuesOld = emptySet( );

            @Override
            public void run( )
            {
                Set<T> valuesNew = new HashSet<>( var.v( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<T> valuesAdded = difference( valuesNew, valuesOld );
                for ( T value : valuesAdded )
                {
                    listener.accept( value );
                }

                this.valuesOld = valuesNew;
            }
        } );
    }

    public static <T> Disposable addElementRemovedListener( ReadableVar<? extends Collection<? extends T>> var, Consumer<? super T> listener )
    {
        return var.addListener( false, new Runnable( )
        {
            private Set<T> valuesOld = emptySet( );

            @Override
            public void run( )
            {
                Set<T> valuesNew = new HashSet<>( var.v( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<T> valuesRemoved = difference( valuesOld, valuesNew );
                for ( T value : valuesRemoved )
                {
                    listener.accept( value );
                }

                this.valuesOld = valuesNew;
            }
        } );
    }

    public static <K, V> Disposable addEntryAddedListener( ReadableVar<? extends Map<K, V>> var, boolean runImmediately, BiConsumer<? super K, ? super V> listener )
    {
        return var.addListener( runImmediately, new Runnable( )
        {
            private Set<Entry<K, V>> entriesOld = emptySet( );

            @Override
            public void run( )
            {
                Set<Entry<K, V>> entriesNew = new HashSet<>( var.v( ).entrySet( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<Entry<K, V>> entriesAdded = difference( entriesNew, entriesOld );
                for ( Entry<K, V> entry : entriesAdded )
                {
                    listener.accept( entry.getKey( ), entry.getValue( ) );
                }

                this.entriesOld = entriesNew;
            }
        } );
    }

    public static <K, V> Disposable addEntryRemovedListener( ReadableVar<? extends Map<K, V>> var, BiConsumer<? super K, ? super V> listener )
    {
        return var.addListener( false, new Runnable( )
        {
            private Set<Entry<K, V>> entriesOld = emptySet( );

            @Override
            public void run( )
            {
                Set<Entry<K, V>> entriesNew = new HashSet<>( var.v( ).entrySet( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<Entry<K, V>> entriesRemoved = difference( entriesOld, entriesNew );
                for ( Entry<K, V> entry : entriesRemoved )
                {
                    listener.accept( entry.getKey( ), entry.getValue( ) );
                }

                this.entriesOld = entriesNew;
            }
        } );
    }

    public static <K,V> Disposable addEntryVarListener( ReadableVar<ImmutableMap<K,? extends ReadableVar<V>>> mapVar, BiConsumer<? super VarEvent,? super K> listener )
    {
        return mapVar.addListener( true, new Consumer<VarEvent>( )
        {
            private final Map<ReadableVar<?>,Disposable> disposables = new HashMap<>( );
            private ImmutableMap<K,? extends ReadableVar<V>> mapOld = null;

            @Override
            public void accept( VarEvent mapEv )
            {
                Set<K> keysUpdated = new LinkedHashSet<>( );

                ImmutableMap<K,? extends ReadableVar<V>> mapNew = mapVar.v( );

                if ( this.mapOld != null )
                {
                    for ( Entry<K,? extends ReadableVar<V>> en : this.mapOld.entrySet( ) )
                    {
                        K key = en.getKey( );
                        ReadableVar<V> varOld = en.getValue( );
                        ReadableVar<V> varNew = ( mapNew == null ? null : mapNew.get( key ) );
                        if ( varNew != varOld )
                        {
                            if ( varOld != null )
                            {
                                this.disposables.remove( varOld ).dispose( );
                            }
                            keysUpdated.add( key );
                        }
                    }
                }

                if ( mapNew != null )
                {
                    for ( Entry<K,? extends ReadableVar<V>> en : mapNew.entrySet( ) )
                    {
                        K key = en.getKey( );
                        ReadableVar<V> varNew = en.getValue( );
                        ReadableVar<V> varOld = ( this.mapOld == null ? null : this.mapOld.get( key ) );
                        if ( varNew != varOld )
                        {
                            if ( varNew != null )
                            {
                                this.disposables.put( varNew, varNew.addListener( false, ( ev ) ->
                                {
                                    listener.accept( ev, key );
                                } ) );
                            }
                            keysUpdated.add( key );
                        }
                    }
                }

                this.mapOld = mapNew;

                for ( K key : keysUpdated )
                {
                    listener.accept( mapEv, key );
                }
            }
        } );
    }

}
