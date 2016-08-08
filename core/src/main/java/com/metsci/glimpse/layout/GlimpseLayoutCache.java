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
package com.metsci.glimpse.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.matcher.TargetStackMatcher;
import com.metsci.glimpse.util.Pair;

/**
 * Stores the bounds of a GlimpseLayout keyed off of the sequence of nested
 * parent GlimpseLayouts leading back to the GlimpseCanvas. If a given GlimpseLayout
 * is rendered to the same sequence of parent GlimpseLayouts and none of the parent
 * GlimpseLayouts have changed shape, then the layout algorithm does not need to be
 * run, the cached LayoutBounds can be used.
 *
 * @author ulman
 */
public class GlimpseLayoutCache<D>
{
    protected Map<List<GlimpseTarget>, Pair<List<GlimpseBounds>, D>> map;

    public GlimpseLayoutCache( )
    {
        this.map = new HashMap<List<GlimpseTarget>, Pair<List<GlimpseBounds>, D>>( );
    }

    public int size( )
    {
        return map.size( );
    }

    public List<D> getValues( )
    {
        Collection<Pair<List<GlimpseBounds>, D>> pairs = map.values( );

        List<D> values = Lists.newArrayList( );

        for ( Pair<List<GlimpseBounds>, D> pair : pairs )
        {
            values.add( pair.second( ) );
        }

        return values;
    }

    public D getValue( GlimpseContext context )
    {
        return getValue( context.getTargetStack( ) );
    }

    public D getValue( GlimpseTargetStack layoutStack )
    {
        Pair<List<GlimpseBounds>, D> entry = map.get( layoutStack.getTargetList( ) );

        if ( entry != null )
        {
            List<GlimpseBounds> cachedBounds = entry.first( );
            List<GlimpseBounds> contextBounds = layoutStack.getBoundsList( );

            if ( compareBounds( cachedBounds, contextBounds ) )
            {
                return entry.second( );
            }
        }

        return null;
    }

    public D getValueNoBoundsCheck( GlimpseContext context )
    {
        return getValueNoBoundsCheck( context.getTargetStack( ) );
    }

    public D getValueNoBoundsCheck( GlimpseTargetStack layoutStack )
    {
        Pair<List<GlimpseBounds>, D> entry = map.get( layoutStack.getTargetList( ) );

        if ( entry != null )
        {
            return entry.second( );
        }
        else
        {
            return null;
        }
    }

    public void setValue( GlimpseTargetStack stack, D value )
    {
        List<GlimpseTarget> targetList = Collections.unmodifiableList( new ArrayList<GlimpseTarget>( stack.getTargetList( ) ) );
        List<GlimpseBounds> boundsList = Collections.unmodifiableList( new ArrayList<GlimpseBounds>( stack.getBoundsList( ) ) );

        map.put( targetList, new Pair<List<GlimpseBounds>, D>( boundsList, value ) );
    }

    public void setValue( GlimpseContext context, D value )
    {
        setValue( context.getTargetStack( ), value );
    }

    /**
     * Removes all mappings from the cache, the component associated with this cache
     * will have to be laid out again for each of its RenderTargets.
     */
    public void clear( )
    {
        this.map.clear( );
    }

    public static boolean compareBounds( List<GlimpseBounds> list1, List<GlimpseBounds> list2 )
    {
        if ( list1 == null || list2 == null ) return false;
        if ( list1.size( ) != list2.size( ) ) return false;

        Iterator<GlimpseBounds> iter1 = list1.iterator( );
        Iterator<GlimpseBounds> iter2 = list2.iterator( );

        while ( iter1.hasNext( ) )
        {
            GlimpseBounds bounds1 = iter1.next( );
            GlimpseBounds bounds2 = iter2.next( );

            if ( !bounds1.equals( bounds2 ) ) return false;
        }

        return true;
    }

    /**
     * @return all keys in the cache which match the provided predicate
     */
    public Collection<D> getMatching( TargetStackMatcher matcher )
    {
        ArrayList<D> acum = Lists.newArrayList( );

        for ( List<GlimpseTarget> key : map.keySet( ) )
        {
            if ( matcher.matches( key ) )
            {
                acum.add( map.get( key ).second( ) );
            }
        }

        return acum;
    }
}
