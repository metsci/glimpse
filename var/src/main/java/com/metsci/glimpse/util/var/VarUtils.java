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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

public class VarUtils
{

    public static interface OldNewListener<V>
    {
        void accept( VarEvent ev, V vOld, V vNew );
    }

    public static <V> Disposable addOldNewListener( ReadableVar<? extends V> var, boolean runImmediately, OldNewListener<? super V> oldNewListener )
    {
        return var.addListener( runImmediately, new Consumer<VarEvent>( )
        {
            private V valuePrev = null;
            private boolean ongoingPrev = true;

            @Override
            public void accept( VarEvent ev )
            {
                V valueOld = this.valuePrev;
                boolean ongoingOld = this.ongoingPrev;

                V valueNew = var.v( );
                boolean ongoingNew = ev.ongoing;

                // Update prev values BEFORE firing listeners, in case one of them triggers this method again
                this.valuePrev = valueNew;
                this.ongoingPrev = ongoingNew;

                if ( ( !ongoingNew && ongoingOld ) || !equal( valueNew, valueOld ) )
                {
                    oldNewListener.accept( ev, valueOld, valueNew );
                }
            }
        } );
    }

    public static <K,V> Disposable addOldNewListener( ReadableVar<? extends Map<K,V>> mapVar, K key, boolean runImmediately, OldNewListener<? super V> oldNewListener )
    {
        return mapVar.addListener( runImmediately, new Consumer<VarEvent>( )
        {
            private V valuePrev = null;
            private boolean ongoingPrev = true;

            @Override
            public void accept( VarEvent ev )
            {
                V valueOld = this.valuePrev;
                boolean ongoingOld = this.ongoingPrev;

                V valueNew = mapVar.v( ).get( key );
                boolean ongoingNew = ev.ongoing;

                // Update prev values BEFORE firing listeners, in case one of them triggers this method again
                this.valuePrev = valueNew;
                this.ongoingPrev = ongoingNew;

                if ( ( !ongoingNew && ongoingOld ) || !equal( valueNew, valueOld ) )
                {
                    oldNewListener.accept( ev, valueOld, valueNew );
                }
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
            private Set<T> ongoingPrev = emptySet( );
            private Set<T> completePrev = emptySet( );

            @Override
            public void accept( VarEvent ev )
            {
                if ( ev.ongoing )
                {
                    Set<T> ongoingOld = this.ongoingPrev;
                    Set<T> ongoingNew = new HashSet<>( var.v( ) );

                    // Update prev values BEFORE firing listeners, in case one of them triggers this method again
                    this.ongoingPrev = ongoingNew;

                    // difference() returns an unmodifiable view, which is what we want
                    Set<T> ongoingAdded = difference( ongoingNew, ongoingOld );
                    for ( T value : ongoingAdded )
                    {
                        listener.accept( ev, value );
                    }
                }
                else
                {
                    Set<T> completeOld = this.completePrev;
                    Set<T> completeNew = new HashSet<>( var.v( ) );

                    // Update prev values BEFORE firing listeners, in case one of them triggers this method again
                    this.ongoingPrev = completeNew;
                    this.completePrev = completeNew;

                    // difference() returns an unmodifiable view, which is what we want
                    Set<T> completeAdded = difference( completeNew, completeOld );
                    for ( T value : completeAdded )
                    {
                        listener.accept( ev, value );
                    }
                }
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
            private Set<T> ongoingPrev = new HashSet<>( var.v( ) );
            private Set<T> completePrev = new HashSet<>( var.v( ) );

            @Override
            public void accept( VarEvent ev )
            {
                if ( ev.ongoing )
                {
                    Set<T> ongoingOld = this.ongoingPrev;
                    Set<T> ongoingNew = new HashSet<>( var.v( ) );

                    // Update prev values BEFORE firing listeners, in case one of them triggers this method again
                    this.ongoingPrev = ongoingNew;

                    // difference() returns an unmodifiable view, which is what we want
                    Set<T> ongoingRemoved = difference( ongoingOld, ongoingNew );
                    for ( T value : ongoingRemoved )
                    {
                        listener.accept( ev, value );
                    }
                }
                else
                {
                    Set<T> completeOld = this.completePrev;

                    Set<T> completeNew = new HashSet<>( var.v( ) );

                    // Update prev values BEFORE firing listeners, in case one of them triggers this method again
                    this.ongoingPrev = completeNew;
                    this.completePrev = completeNew;

                    // difference() returns an unmodifiable view, which is what we want
                    Set<T> completeRemoved = difference( completeOld, completeNew );
                    for ( T value : completeRemoved )
                    {
                        listener.accept( ev, value );
                    }
                }
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
            private ImmutableMap<K,V> ongoingPrev = ImmutableMap.of( );
            private ImmutableMap<K,V> completePrev = ImmutableMap.of( );

            @Override
            public void accept( VarEvent ev )
            {
                if ( ev.ongoing )
                {
                    ImmutableMap<K,V> ongoingOld = this.ongoingPrev;
                    ImmutableMap<K,V> ongoingNew = ImmutableMap.copyOf( var.v( ) );

                    // Update this.mapPrev BEFORE firing listeners, in case one of them triggers this method again
                    this.ongoingPrev = ongoingNew;

                    Set<K> keys = new LinkedHashSet<>( );
                    keys.addAll( ongoingOld.keySet( ) );
                    keys.addAll( ongoingNew.keySet( ) );
                    for ( K k : keys )
                    {
                        V vOld = ongoingOld.get( k );
                        V vNew = ongoingNew.get( k );
                        if ( !equal( vNew, vOld ) )
                        {
                            listener.accept( ev, k, vOld, vNew );
                        }
                    }
                }
                else
                {
                    ImmutableMap<K,V> completeOld = this.completePrev;
                    ImmutableMap<K,V> completeNew = ImmutableMap.copyOf( var.v( ) );

                    // Update this.mapPrev BEFORE firing listeners, in case one of them triggers this method again
                    this.ongoingPrev = completeNew;
                    this.completePrev = completeNew;

                    Set<K> keys = new LinkedHashSet<>( );
                    keys.addAll( completeOld.keySet( ) );
                    keys.addAll( completeNew.keySet( ) );
                    for ( K k : keys )
                    {
                        V vOld = completeOld.get( k );
                        V vNew = completeNew.get( k );
                        if ( !equal( vNew, vOld ) )
                        {
                            listener.accept( ev, k, vOld, vNew );
                        }
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

    protected static <K,V> ImmutableMap<K,V> mapWith( ImmutableMap<K,V> map, K key, V value )
    {
        if ( equal( value, map.get( key ) ) )
        {
            return map;
        }
        else
        {
            Map<K,V> newMap = new LinkedHashMap<>( map );
            if ( value == null )
            {
                newMap.remove( key );
            }
            else
            {
                newMap.put( key, value );
            }
            return ImmutableMap.copyOf( newMap );
        }
    }

}
