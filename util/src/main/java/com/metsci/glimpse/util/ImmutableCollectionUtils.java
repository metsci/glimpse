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
package com.metsci.glimpse.util;

import static com.google.common.base.Objects.equal;
import static java.util.Collections.disjoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ImmutableCollectionUtils
{

    public static <K1, K2, V> ImmutableMap<K1, ImmutableMap<K2, V>> mapWith( ImmutableMap<K1, ImmutableMap<K2, V>> map, K1 key1, K2 key2, V value )
    {
        ImmutableMap<K2, V> innerMap = mapWith( map.get( key1 ), key2, value );
        return mapWith( map, key1, innerMap );
    }

    public static <K, V> ImmutableMap<K, V> mapWith( Map<K, V> map, K key, V value )
    {
        if ( equal( value, map.get( key ) ) )
        {
            return castOrConvert( map );
        }
        else
        {
            Map<K, V> newMap = new LinkedHashMap<>( map );
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

    public static <K, V> ImmutableMap<K, V> mapWith( Map<K, V> map, Map<? extends K, ? extends V> newEntries )
    {
        // Leave newMap as null until we have to actually change a value
        Map<K, V> newMap = null;

        for ( Entry<? extends K, ? extends V> en : newEntries.entrySet( ) )
        {
            K key = en.getKey( );
            V newValue = en.getValue( );
            V oldValue = map.get( key );

            if ( !equal( newValue, oldValue ) )
            {
                // Have to change a value, so make sure we have a mutable copy of the map
                if ( newMap == null )
                {
                    newMap = new LinkedHashMap<>( map );
                }

                if ( newValue == null )
                {
                    newMap.remove( key );
                }
                else
                {
                    newMap.put( key, newValue );
                }
            }
        }

        // If newMap is still null, then we never actually changed any values
        return newMap == null ? castOrConvert( map ) : ImmutableMap.copyOf( newMap );
    }

    public static <K, V> ImmutableMap<K, V> mapUpdated( Map<K, V> map, K key, Function<? super V, ? extends V> transformFn )
    {
        V value = transformFn.apply( map.get( key ) );
        return mapWith( map, key, value );
    }

    public static <K, V> ImmutableMap<K, V> mapMinus( Map<K, V> map, K key )
    {
        if ( !map.containsKey( key ) )
        {
            return castOrConvert( map );
        }
        else
        {
            Map<K, V> newMap = new LinkedHashMap<>( map );
            newMap.remove( key );
            return ImmutableMap.copyOf( newMap );
        }
    }

    public static <K, V> ImmutableMap<K, V> mapMinus( Map<K, V> map, Collection<K> keys )
    {
        if ( disjoint( map.keySet( ), keys ) )
        {
            return castOrConvert( map );
        }
        else
        {
            Map<K, V> newMap = new LinkedHashMap<>( map );
            keys.forEach( newMap::remove );
            return ImmutableMap.copyOf( newMap );
        }
    }

    public static <K, V> ImmutableMap<K, V> mapSubset( Map<K, V> map, Collection<K> keys )
    {
        Map<K, V> subset = new LinkedHashMap<>( map );
        subset.keySet( ).retainAll( keys );
        return ImmutableMap.copyOf( subset );
    }

    public static <V> ImmutableSet<V> setPlus( Set<V> set, V value )
    {
        if ( set.contains( value ) )
        {
            return castOrConvert( set );
        }
        else
        {
            Set<V> newSet = new LinkedHashSet<>( set );
            newSet.add( value );
            return ImmutableSet.copyOf( newSet );
        }
    }

    @SafeVarargs
    public static <V> ImmutableSet<V> setPlus( Set<V> set, V... values )
    {
        Set<V> newSet = new LinkedHashSet<>( set );

        boolean changed = false;
        for ( V value : values )
        {
            changed |= newSet.add( value );
        }

        return ( changed ? ImmutableSet.copyOf( newSet ) : castOrConvert( set ) );
    }

    public static <V> ImmutableSet<V> setMinus( Set<V> set, V value )
    {
        if ( !set.contains( value ) )
        {
            return castOrConvert( set );
        }
        else
        {
            Set<V> newSet = new LinkedHashSet<>( set );
            newSet.remove( value );
            return ImmutableSet.copyOf( newSet );
        }
    }

    @SafeVarargs
    public static <V> ImmutableSet<V> setMinus( Set<V> set, V... values )
    {
        Set<V> newSet = new LinkedHashSet<>( set );

        boolean changed = false;
        for ( V value : values )
        {
            changed |= newSet.remove( value );
        }

        return ( changed ? ImmutableSet.copyOf( newSet ) : castOrConvert( set ) );
    }

    public static <V> ImmutableList<V> listPlus( List<V> list, V value )
    {
        List<V> newList = new ArrayList<>( list );
        newList.add( value );
        return ImmutableList.copyOf( newList );
    }

    public static <V> ImmutableList<V> listPlus( List<V> list, int index, V value )
    {
        List<V> newList = new ArrayList<>( list );
        newList.add( index, value );
        return ImmutableList.copyOf( newList );
    }

    @SafeVarargs
    public static <V> ImmutableList<V> listPlus( List<V> list, V... values )
    {
        return listPlus( list, Arrays.asList( values ) );
    }

    public static <V> ImmutableList<V> listPlus( List<V> list, Collection<? extends V> values )
    {
        List<V> newList = new ArrayList<>( list );
        newList.addAll( values );
        return ImmutableList.copyOf( newList );
    }

    public static <V> ImmutableList<V> listWith( List<V> list, int index, V value )
    {
        if ( equal( value, list.get( index ) ) )
        {
            return castOrConvert( list );
        }
        else
        {
            List<V> newList = new ArrayList<>( list );
            newList.set( index, value );
            return ImmutableList.copyOf( newList );
        }
    }

    @SafeVarargs
    public static <V> ImmutableList<V> listMinus( List<V> list, V... values )
    {
        return listMinus( list, Arrays.asList( values ) );
    }

    public static <V> ImmutableList<V> listMinus( List<V> list, Collection<? extends V> values )
    {
        List<V> newList = new ArrayList<>( list );

        boolean changed = false;
        for ( V value : values )
        {
            changed |= newList.remove( value );
        }

        return ( changed ? ImmutableList.copyOf( newList ) : castOrConvert( list ) );
    }

    public static <V> ImmutableList<V> listMinus( List<V> list, V value )
    {
        List<V> newList = new ArrayList<>( list );
        boolean removed = newList.remove( value );
        if ( removed )
        {
            return ImmutableList.copyOf( newList );
        }
        else
        {
            return castOrConvert( list );
        }
    }

    public static <V> ImmutableList<V> listMinus( List<V> list, int index )
    {
        List<V> newList = new ArrayList<>( list );
        newList.remove( index );
        return ImmutableList.copyOf( newList );
    }

    private static <V> ImmutableList<V> castOrConvert( List<V> list )
    {
        if ( list instanceof ImmutableList )
        {
            return ( ImmutableList<V> ) list;
        }
        else
        {
            return ImmutableList.copyOf( list );
        }
    }

    private static <V> ImmutableSet<V> castOrConvert( Set<V> set )
    {
        if ( set instanceof ImmutableSet )
        {
            return ( ImmutableSet<V> ) set;
        }
        else
        {
            return ImmutableSet.copyOf( set );
        }
    }

    private static <K, V> ImmutableMap<K, V> castOrConvert( Map<K, V> map )
    {
        if ( map instanceof ImmutableMap )
        {
            return ( ImmutableMap<K, V> ) map;
        }
        else
        {
            return ImmutableMap.copyOf( map );
        }
    }
}
