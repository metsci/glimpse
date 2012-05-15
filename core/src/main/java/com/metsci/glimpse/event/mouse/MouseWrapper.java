/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.event.mouse;

import static com.metsci.glimpse.context.TargetStackUtil.newTargetStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.context.TargetStackUtil;

public abstract class MouseWrapper<E>
{
    protected GlimpseCanvas canvas;
    // set of hovered stacks which does not change during drags
    protected Set<GlimpseTargetStack> dragHoveredSet;
    // set of hovered stacks which changes even during drags
    // this is necessary because mouseExited and mouseEntered events
    // are fired even while the mouse is dragging
    protected Set<GlimpseTargetStack> hoveredSet;

    public MouseWrapper( GlimpseCanvas canvas )
    {
        this.canvas = canvas;
        this.dragHoveredSet = new HashSet<GlimpseTargetStack>( );
        this.hoveredSet = new HashSet<GlimpseTargetStack>( );
    }

    public List<GlimpseTargetStack> getContainingTargets( E e )
    {
        // create a new context using the context associated with this mouse wrapper
        // GlimpseTargets will be popped on and off the context as we search through
        // the hierarchy in order to determine which GlimpseTargets to dispatch
        // GlimpseMouseEvents to
        GlimpseContext context = new GlimpseContextImpl( canvas );

        List<GlimpseTargetStack> result = new ArrayList<GlimpseTargetStack>( );

        getContainingTargets( e, context, result );

        return result;
    }

    // perform a depth first search of the hierarchy of GlimpseLayouts in order
    // to find the top most component which contains the click
    protected boolean getContainingTargets( E e, GlimpseContext context, List<GlimpseTargetStack> accumulator )
    {
        GlimpseTargetStack stack = context.getTargetStack( );
        GlimpseTarget layout = stack.getTarget( );
        GlimpseBounds bounds = stack.getBounds( );
        List<GlimpseTarget> list = layout.getTargetChildren( );
        int size = list.size( );

        // run though the list backwards. GlimpseTarget.getTargetChildren() returns
        // a list sorted by zOrder. GlimpseTargets with larger zOrder (or those added
        // later among GlimpseTargets with the same zOrder) are considered "on top"
        // and should have events delivered to them first
        for ( int i = size - 1; i >= 0; i-- )
        {
            GlimpseTarget childLayout = list.get( i );

            GlimpseBounds childBounds = childLayout.getTargetBounds( context.getTargetStack( ) );

            stack.push( childLayout, childBounds );

            boolean consumeEvent = getContainingTargets( e, context, accumulator );

            if ( consumeEvent ) return true;

            stack.pop( );
        }

        // if the layout neither consumes nor responds to events, we don't need
        // to check whether or not the event was interior to the layout
        if ( !layout.isEventConsumer( ) && !layout.isEventGenerator( ) ) return false;

        // if the event was interior to the layout, add it to the accumulator list
        // if it is an event generator and signal that we should short-circuit the
        // search if it was an event consumer
        if ( handleInterior( e, context, bounds ) )
        {
            if ( layout.isEventGenerator( ) )
            {
                accumulator.add( newTargetStack( stack ) );
            }

            return layout.isEventConsumer( );
        }
        else
        {
            return false;
        }
    }

    protected boolean handleInterior( E e, GlimpseContext context, GlimpseBounds bounds )
    {
        if ( bounds == null ) return false;

        if ( !isValid( e, bounds ) ) return false;

        boolean isButtonDown = isButtonDown( e );
        boolean isInterior = isInterior( e, bounds );

        if ( isInterior )
        {
            addHovered( context.getTargetStack( ) );
            if ( !isButtonDown ) addDragHovered( context.getTargetStack( ) );
        }

        return isInterior;
    }

    protected Mouseable getMouseTarget( GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseTarget target = stack.getTarget( );
        if ( target == null || ! ( target instanceof Mouseable ) ) return null;

        Mouseable mouseTarget = ( Mouseable ) target;

        return mouseTarget;
    }

    protected Set<GlimpseTargetStack> clearAllHovered( )
    {
        Set<GlimpseTargetStack> oldHovered = copyHovered( );
        dragHoveredSet.clear( );
        hoveredSet.clear( );
        return oldHovered;
    }

    protected void setAllHovered( Collection<GlimpseTargetStack> list )
    {
        setDragHovered( list );
        setHovered( list );
    }

    protected Set<GlimpseTargetStack> clearDragHovered( )
    {
        Set<GlimpseTargetStack> oldHovered = copyDragHovered( );
        dragHoveredSet.clear( );
        return oldHovered;
    }

    protected void addDragHovered( GlimpseTargetStack stack )
    {
        dragHoveredSet.add( TargetStackUtil.newTargetStack( stack ) );
    }

    protected void setDragHovered( Collection<GlimpseTargetStack> list )
    {
        clearDragHovered( );
        for ( GlimpseTargetStack stack : list )
            addDragHovered( stack );
    }

    protected boolean isDragHovered( )
    {
        return dragHoveredSet != null && !dragHoveredSet.isEmpty( );
    }

    protected Set<GlimpseTargetStack> copyDragHovered( )
    {
        return new HashSet<GlimpseTargetStack>( dragHoveredSet );
    }

    protected Set<GlimpseTargetStack> getDragHovered( )
    {
        return dragHoveredSet;
    }

    protected Set<GlimpseTargetStack> clearHovered( )
    {
        Set<GlimpseTargetStack> oldHovered = copyHovered( );
        hoveredSet.clear( );
        return oldHovered;
    }

    protected void addHovered( GlimpseTargetStack stack )
    {
        hoveredSet.add( TargetStackUtil.newTargetStack( stack ) );
    }

    protected void setHovered( Collection<GlimpseTargetStack> list )
    {
        clearHovered( );
        for ( GlimpseTargetStack stack : list )
            addHovered( stack );
    }

    protected boolean isHovered( )
    {
        return hoveredSet != null && !hoveredSet.isEmpty( );
    }

    protected Set<GlimpseTargetStack> copyHovered( )
    {
        return new HashSet<GlimpseTargetStack>( hoveredSet );
    }

    protected Set<GlimpseTargetStack> getHovered( )
    {
        return hoveredSet;
    }

    protected GlimpseMouseEvent toLocalGlimpseEvent( E e, GlimpseTargetStack stack )
    {
        return toGlimpseEvent( toLocalCoords( e, stack ) );
    }

    protected abstract boolean isButtonDown( E e );

    protected abstract boolean isInterior( E e, GlimpseBounds bounds );

    protected abstract boolean isValid( E e, GlimpseBounds bounds );

    protected abstract E toLocalCoords( E e, GlimpseTargetStack stack );

    protected abstract GlimpseMouseEvent toGlimpseEvent( E e );

    public void notifyMouseEnteredExited( E event, Set<GlimpseTargetStack> oldStacks, Set<GlimpseTargetStack> newStacks )
    {
        for ( GlimpseTargetStack oldStack : oldStacks )
        {
            if ( !newStacks.contains( oldStack ) )
            {
                notifyMouseExited( event, oldStack );
            }
        }

        for ( GlimpseTargetStack newStack : newStacks )
        {
            if ( !oldStacks.contains( newStack ) )
            {
                notifyMouseEntered( event, newStack );
            }
        }
    }

    public void notifyMouseEntered( E event, GlimpseTargetStack stack )
    {
        Mouseable mouseTarget = getMouseTarget( stack );
        GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, stack );
        if ( mouseTarget != null ) mouseTarget.mouseEntered( glimpseEvent );
    }

    public void notifyMouseEntered( E event, Set<GlimpseTargetStack> stacks )
    {
        for ( GlimpseTargetStack stack : stacks )
        {
            notifyMouseEntered( event, stack );
        }
    }

    public void notifyMouseExited( E event, GlimpseTargetStack stack )
    {
        Mouseable mouseTarget = getMouseTarget( stack );
        GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, stack );
        if ( mouseTarget != null ) mouseTarget.mouseExited( glimpseEvent );
    }

    public void notifyMouseExited( E event, Set<GlimpseTargetStack> stacks )
    {
        for ( GlimpseTargetStack stack : stacks )
        {
            notifyMouseExited( event, stack );
        }
    }
}
