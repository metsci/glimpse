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

import static com.metsci.glimpse.util.GeneralUtils.newArrayList;
import static com.metsci.glimpse.util.GeneralUtils.newHashMap;
import static net.miginfocom.layout.ConstraintParser.parseColumnConstraints;
import static net.miginfocom.layout.ConstraintParser.parseComponentConstraint;
import static net.miginfocom.layout.ConstraintParser.parseLayoutConstraint;
import static net.miginfocom.layout.ConstraintParser.parseRowConstraints;
import static net.miginfocom.layout.ConstraintParser.prepare;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.Grid;
import net.miginfocom.layout.LC;
import net.miginfocom.layout.LayoutCallback;

public class GlimpseLayoutManagerMig implements GlimpseLayoutManager
{
    private LC lc = null;
    private AC cc = null;
    private AC rc = null;

    private ArrayList<LayoutCallback> callbackList = newArrayList( );

    private int cacheHash = -1;
    private Grid gridCache = null;
    private Map<GlimpseLayoutDelegate, Object> childDataCache = new IdentityHashMap<GlimpseLayoutDelegate, Object>( 8 );
    private Map<ComponentWrapper, CC> childConstraintCache = newHashMap( );

    public GlimpseLayoutManagerMig( )
    {
        this( "", "", "" );
    }

    public GlimpseLayoutManagerMig( LC layoutConstraints, AC colConstraints, AC rowConstraints )
    {
        lc = layoutConstraints;
        cc = colConstraints;
        rc = rowConstraints;
    }

    public GlimpseLayoutManagerMig( String layoutConstraints, String colConstraints, String rowConstraints )
    {
        setLayoutConstraints( layoutConstraints );
        setColumnConstraints( colConstraints );
        setRowConstraints( rowConstraints );
    }

    public void setLayoutConstraints( String s )
    {
        lc = parseLayoutConstraint( prepare( s ) );
        gridCache = null;
    }

    public void setColumnConstraints( String s )
    {
        cc = parseColumnConstraints( prepare( s ) );
        gridCache = null;
    }

    public void setRowConstraints( String s )
    {
        rc = parseRowConstraints( prepare( s ) );
        gridCache = null;
    }

    private void setChildConstraint( GlimpseLayoutDelegate child, Object constraint, boolean noCheck )
    {
        if ( noCheck == false && childDataCache.containsKey( child ) == false ) throw new IllegalArgumentException( "Component must already be added to parent!" );

        if ( constraint == null || constraint instanceof String )
        {
            String ccString = prepare( ( String ) constraint );

            childDataCache.put( child, constraint );
            childConstraintCache.put( child, parseComponentConstraint( ccString ) );
        }
        else if ( constraint instanceof CC )
        {
            childDataCache.put( child, constraint );
            childConstraintCache.put( child, ( CC ) constraint );
        }
        else
        {
            throw new IllegalArgumentException( "Constraint must be String or ComponentConstraint: " + constraint.getClass( ).toString( ) );
        }

        gridCache = null;
    }

    public void addLayoutCallback( LayoutCallback callback )
    {
        if ( callback == null ) throw new NullPointerException( );

        callbackList.add( callback );
    }

    public void removeLayoutCallback( LayoutCallback callback )
    {
        if ( callbackList != null ) callbackList.remove( callback );
    }

    private void checkCache( GlimpseLayoutDelegate parent )
    {
        checkChildCache( parent );

        int hash = parent.getWidth( ) ^ parent.getHeight( );
        for ( Iterator<ComponentWrapper> it = childConstraintCache.keySet( ).iterator( ); it.hasNext( ); )
        {
            hash += it.next( ).getLayoutHashCode( );
        }

        if ( hash != cacheHash )
        {
            gridCache = null;
            cacheHash = hash;
        }

        if ( gridCache == null )
        {
            gridCache = new Grid( parent, lc, rc, cc, childConstraintCache, callbackList );
        }
    }

    private boolean checkChildCache( GlimpseLayoutDelegate parent )
    {
        ComponentWrapper[] comps = parent.getComponents( );
        boolean changed = comps.length != childDataCache.size( );

        if ( !changed )
        {
            for ( int i = 0; i < comps.length; i++ )
            {
                GlimpseLayoutDelegate c = ( GlimpseLayoutDelegate ) comps[i];

                if ( childDataCache.get( c ) == null && c.getLayoutData( ) == null )
                {
                    continue;
                }

                if ( childDataCache.get( c ) != c.getLayoutData( ) )
                {
                    changed = true;
                    break;
                }
            }
        }

        if ( changed )
        {
            childConstraintCache.clear( );
            childDataCache.clear( );
            for ( int i = 0; i < comps.length; i++ )
            {
                GlimpseLayoutDelegate c = ( GlimpseLayoutDelegate ) comps[i];
                setChildConstraint( c, c.getLayoutData( ), true );
            }
        }

        return changed;
    }

    @Override
    public void layout( GlimpseLayoutDelegate parent )
    {
        checkCache( parent );

        int[] b = new int[] { parent.getX( ), parent.getY( ), parent.getWidth( ), parent.getHeight( ) };

        boolean layoutAgain = gridCache.layout( b, lc.getAlignX( ), lc.getAlignY( ), false, true );
        if ( layoutAgain )
        {
            gridCache = null;
            checkCache( parent );
            gridCache.layout( b, lc.getAlignX( ), lc.getAlignY( ), false, false );
        }
    }
}
