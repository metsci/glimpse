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
package com.metsci.glimpse.context;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;

/**
 * <p>Utility method for manipulating {@link GlimpseTargetStack} instances.</p>
 *
 * <p>In order to uniquely name a GlimpseLayout, the stack containing the
 * GlimpseLayout and all of its parent layouts down to the underlying GlimpseCanvas
 * must be provided (because the GlimpseLayout may be reused and could have
 * multiple parents).</p>
 *
 * @author ulman
 */
public class TargetStackUtil
{
    /**
     * Creates a new GlimpseTargetStack which is an exact copy of the given stack.
     *
     * @param stack the stack to copy
     * @return a deep copy of the provided stack
     */
    public static GlimpseTargetStack newTargetStack( GlimpseTargetStack stack )
    {
        return newTargetStack( ).push( stack );
    }

    /**
     * Creates a new GlimpseTargetStack which is an exact copy of the given stack
     * and is unmodifiable.
     *
     * @param stack the stack to copy
     * @return a deep copy of the provided stack
     */
    public static GlimpseTargetStack newUnmodifiableTargetStack( GlimpseTargetStack stack )
    {
        return new UnmodifiableTargetStack( newTargetStack( ).push( stack ) );
    }

    /**
     * Creates a new target stack which is the concatenation of the provided GlimpseTargets.
     * The first provided GlimpseTarget is placed at the bottom of the new GlimpseTargetStack.
     *
     * @param targets the GlimpseTargets to concatenate
     * @return the newly constructed GlimpseTargetStack
     */
    public static GlimpseTargetStack newTargetStack( GlimpseTarget... targets )
    {
        return new GlimpseTargetStackImpl( targets );
    }

    /**
     * Returns true if the query stack ends with the sequence of GlimpseTargets defined by the prefix stack.
     * Ignores the GlimpseBounds.
     *
     * @param query the GlimpseTargetStack to investigate
     * @param suffix a GlimpseTargetStack to search for at the end of the query stack
     * @return whether the query stack ends with the GlimpseTargets in the prefix stack
     */
    public static boolean endsWith( GlimpseTargetStack query, GlimpseTargetStack suffix )
    {
        int suffixSize = suffix.getSize( );
        int querySize = query.getSize( );

        if ( suffixSize > querySize ) return false;

        List<GlimpseTarget> suffixList = suffix.getTargetList( );
        ListIterator<GlimpseTarget> suffixIter = suffixList.listIterator( suffixList.size( ) );

        List<GlimpseTarget> queryList = query.getTargetList( );
        ListIterator<GlimpseTarget> queryIter = queryList.listIterator( queryList.size( ) );

        while ( suffixIter.hasPrevious( ) )
        {
            GlimpseTarget suffixTarget = suffixIter.previous( );
            GlimpseTarget queryTarget = queryIter.previous( );

            if ( !queryTarget.equals( suffixTarget ) ) return false;
        }

        return true;
    }

    /**
     * Returns true if the query target stack contains the provided target.
     *
     * @param query the GlimpseTargetStack to investigate
     * @param target the GlimpseTarget to look for in the query stack
     * @return true if the query stack contains the target
     */
    public static boolean contains( GlimpseTargetStack query, GlimpseTarget target )
    {
        Iterator<GlimpseTarget> queryIter = query.getTargetList( ).iterator( );

        while ( queryIter.hasNext( ) )
        {
            GlimpseTarget queryTarget = queryIter.next( );

            if ( queryTarget.equals( target ) ) return true;
        }

        return false;
    }

    /**
     *
     * @return true if either target stack contain a GlimpseTarget in common
     */
    public static boolean intersects( GlimpseTargetStack query1, GlimpseTargetStack query2 )
    {
        for ( GlimpseTarget target1 : query1.getTargetList( ) )
        {
            for ( GlimpseTarget target2 : query2.getTargetList( ) )
            {
                if ( target1.equals( target2 ) ) return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the query stack starts with the sequence of GlimpseTargets defined by the prefix stack.
     * Ignores the GlimpseBounds.
     *
     * @param query the GlimpseTargetStack to investigate
     * @param prefix a GlimpseTargetStack to search for at the start of the query stack
     * @return whether the query stack starts with the GlimpseTargets in the prefix stack
     */
    public static boolean startsWith( GlimpseTargetStack query, GlimpseTargetStack prefix )
    {
        int prefixSize = prefix.getSize( );
        int querySize = query.getSize( );

        if ( prefixSize > querySize ) return false;

        Iterator<GlimpseTarget> prefixIter = prefix.getTargetList( ).iterator( );
        Iterator<GlimpseTarget> queryIter = query.getTargetList( ).iterator( );

        while ( prefixIter.hasNext( ) )
        {
            GlimpseTarget prefixTarget = prefixIter.next( );
            GlimpseTarget queryTarget = queryIter.next( );

            if ( !queryTarget.equals( prefixTarget ) ) return false;
        }

        return true;
    }

    public static GlimpseTargetStack popTo( GlimpseTargetStack stack, GlimpseTarget target )
    {
        // short circuit of the event is already relative to the target
        if ( stack.getTarget( ) == target ) return stack;

        stack = newTargetStack( stack );

        while ( stack.getSize( ) > 0 )
        {
            stack.pop( );

            if ( stack.getTarget( ) == target ) return stack;
        }

        return null;
    }

    public static GlimpseTargetStack pushToBottom( GlimpseTargetStack stack, int x, int y )
    {
        stack = newTargetStack( stack );

        for ( ;; )
        {
            boolean foundNextChild = false;

            GlimpseTarget target = stack.getTarget( );
            List<GlimpseTarget> children = target.getTargetChildren( );
            for ( int i = children.size( ) - 1; i >= 0; i-- )
            {
                GlimpseTarget child = children.get( i );
                GlimpseBounds childBounds = child.getTargetBounds( stack );

                if ( childBounds.contains( x, y ) )
                {
                    stack.push( child, childBounds );
                    foundNextChild = true;
                    break;
                }
            }

            if ( !foundNextChild )
            {
                break;
            }
        }

        return stack;
    }

    public static GlimpseMouseEvent translateCoordinates( GlimpseMouseEvent event, GlimpseTarget target )
    {
        // short circuit of the event is already relative to the target
        if ( event.getTargetStack( ).getTarget( ) == target ) return event;

        GlimpseTargetStack stack = newTargetStack( event.getTargetStack( ) );

        // coordinates relative to the GlimpseTarget at the top of the stack
        int x = event.getX( );
        int y = event.getY( );

        GlimpseBounds topBounds = stack.getBounds( );

        while ( stack.getSize( ) > 0 )
        {
            stack.pop( );

            if ( stack.getTarget( ) == target )
            {
                GlimpseBounds targetBounds = stack.getBounds( );

                x += topBounds.getX( ) - targetBounds.getX( );
                y += targetBounds.getHeight( ) - ( topBounds.getY( ) - targetBounds.getY( ) + topBounds.getHeight( ) );

                return new GlimpseMouseEvent( event, stack, x, y );
            }
        }

        // the provided GlimpseTarget was not in the GlimpseMouseEvent hierarchy
        // (the click was not on top of the provided GlimpseTarget)
        return null;
    }
}
