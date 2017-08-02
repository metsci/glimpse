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

import static com.google.common.base.Objects.*;
import static com.google.common.collect.Sets.*;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.*;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
        return addElementAddedListener( var, runImmediately, ( ev, value ) ->
        {
            listener.accept( value );
        } );
    }

    public static <T> Disposable addElementAddedListener( ReadableVar<? extends Collection<? extends T>> var, boolean runImmediately, BiConsumer<VarEvent,? super T> listener )
    {
        return var.addListener( runImmediately, new Consumer<VarEvent>( )
        {
            private Set<T> valuesOld = emptySet( );

            @Override
            public void accept( VarEvent ev )
            {
                Set<T> valuesNew = new HashSet<>( var.v( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<T> valuesAdded = difference( valuesNew, valuesOld );
                for ( T value : valuesAdded )
                {
                    listener.accept( ev, value );
                }

                this.valuesOld = valuesNew;
            }
        } );
    }

    public static <T> Disposable addElementRemovedListener( ReadableVar<? extends Collection<? extends T>> var, Consumer<? super T> listener )
    {
        return addElementRemovedListener( var, ( ev, value ) ->
        {
            listener.accept( value );
        } );
    }

    public static <T> Disposable addElementRemovedListener( ReadableVar<? extends Collection<? extends T>> var, BiConsumer<VarEvent,? super T> listener )
    {
        return var.addListener( false, new Consumer<VarEvent>( )
        {
            private Set<T> valuesOld = new HashSet<>( var.v( ) );

            @Override
            public void accept( VarEvent ev )
            {
                Set<T> valuesNew = new HashSet<>( var.v( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<T> valuesRemoved = difference( valuesOld, valuesNew );
                for ( T value : valuesRemoved )
                {
                    listener.accept( ev, value );
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
            private Set<Entry<K, V>> entriesOld = new HashSet<>( var.v( ).entrySet( ) );

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

    public static interface MapVarListener<K,V>
    {
        void accept( VarEvent ev, K key, V vOld, V vNew );
    }

    public static <K,V> Disposable addMapVarListener( ReadableVar<? extends Map<K,V>> var, boolean runImmediately, MapVarListener<? super K,? super V> listener )
    {
        return var.addListener( runImmediately, new Consumer<VarEvent>( )
        {
            // FIXME: Handle ongoing flag carefully
            private ImmutableMap<K,V> mapPrev = ImmutableMap.of( );

            @Override
            public void accept( VarEvent ev )
            {
                ImmutableMap<K,V> mapOld = this.mapPrev;
                ImmutableMap<K,V> mapNew = ImmutableMap.copyOf( var.v( ) );

                // Update this.mapPrev BEFORE firing listeners, in case one of them triggers this method again
                this.mapPrev = mapNew;

                Set<K> keys = new LinkedHashSet<>( );
                keys.addAll( mapOld.keySet( ) );
                keys.addAll( mapNew.keySet( ) );
                for ( K k : keys )
                {
                    V vOld = mapOld.get( k );
                    V vNew = mapNew.get( k );
                    if ( !equal( vNew, vOld ) )
                    {
                        listener.accept( ev, k, vOld, vNew );
                    }
                }
            }
        } );
    }

    public static <K,V> void updateMapValue( Var<ImmutableMap<K,V>> var, K key, Function<? super V,? extends V> updateFn )
    {
        updateMapValue( var, false, key, updateFn );
    }

    public static <K,V> void updateMapValue( Var<ImmutableMap<K,V>> var, boolean ongoing, K key, Function<? super V,? extends V> updateFn )
    {
        var.update( ongoing, ( map ) ->
        {
            V vOld = map.get( key );
            V vNew = updateFn.apply( vOld );
            return mapWith( map, key, vNew );
        } );
    }

    public static <K,V> void putMapValue( Var<ImmutableMap<K,V>> var, K key, V value )
    {
        putMapValue( var, false, key, value );
    }

    public static <K,V> void putMapValue( Var<ImmutableMap<K,V>> var, boolean ongoing, K key, V value )
    {
        var.update( ongoing, ( map ) ->
        {
            return mapWith( map, key, value );
        } );
    }

}
