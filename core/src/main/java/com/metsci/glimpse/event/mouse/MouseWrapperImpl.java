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
package com.metsci.glimpse.event.mouse;

import java.util.List;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTargetStack;

public abstract class MouseWrapperImpl<I> extends MouseWrapper<I>
{

    public MouseWrapperImpl( GlimpseCanvas canvas )
    {
        super( canvas );
    }

    public void mouseClicked0( I event )
    {
        // not handled by GlimpseMouseListener, use mousePressed/mouseReleased
    }

    public void mouseEntered0( I event )
    {
        if ( event == null ) return;

        // save the old hovered stacks
        List<GlimpseTargetStack> oldHovered = clearHovered( );

        // update hovered stacks
        getContainingTargets( event );

        // get the new hovered stacks
        List<GlimpseTargetStack> newHovered = getHovered( );

        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
    }

    public void mouseExited0( I event )
    {
        if ( event == null ) return;

        // save the old hovered stacks
        List<GlimpseTargetStack> oldHovered = clearHovered( );

        // update hovered stacks
        getContainingTargets( event );

        // get the new hovered stacks
        List<GlimpseTargetStack> newHovered = getHovered( );

        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
    }

    public boolean mousePressed0( I event )
    {
        if ( event == null ) return false;

        List<GlimpseTargetStack> list = getContainingTargets( event );

        setAllHovered( list );

        // stacks with low indices are on top in the layout, and
        // have their mouse events generated first
        for ( GlimpseTargetStack stack : list )
        {
            Mouseable mouseTarget = getMouseTarget( stack );
            if ( mouseTarget == null ) return false;

            GlimpseMouseEvent glimpseEvent = toGlimpseEvent( event, stack );

            mouseTarget.mousePressed( glimpseEvent );

            if ( glimpseEvent.isHandled( ) ) return true;
        }

        return false;
    }

    public boolean mouseReleased0( I event )
    {
        if ( event == null ) return false;

        boolean handled = false;

        // always always deliver the mouseUp event regardless of which
        // component the mouse event occurred inside
        if ( isDragHovered( ) )
        {
            List<GlimpseTargetStack> hoveredList = getDragHovered( );
            for ( GlimpseTargetStack hoveredStack : hoveredList )
            {
                Mouseable mouseTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseEvent = toGlimpseEvent( event, hoveredStack );

                if ( mouseTarget != null ) mouseTarget.mouseReleased( glimpseEvent );

                if ( glimpseEvent.isHandled( ) )
                {

                    handled = true;
                    break;
                }
            }
        }

        // call getContainingTarget to setHovered correctly
        // call after event is sent because we want to send the mouseReleased event
        // to the previously hovered component then setHovered to the GlimpseTarget
        // currently under the mouse
        getContainingTargets( event );

        return handled;
    }

    public boolean mouseDragged0( I event )
    {
        if ( event == null ) return false;

        // save the old hovered stacks
        List<GlimpseTargetStack> oldHovered = clearHovered( );

        // call getContainingTarget to setHovered correctly
        getContainingTargets( event );

        // get the new hovered stacks
        List<GlimpseTargetStack> newHovered = getHovered( );

        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );

        if ( isDragHovered( ) )
        {
            List<GlimpseTargetStack> hoveredList = getDragHovered( );
            for ( GlimpseTargetStack hoveredStack : hoveredList )
            {
                Mouseable mouseHoveredTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseHoveredEvent = toGlimpseEvent( event, hoveredStack );

                if ( mouseHoveredTarget != null ) mouseHoveredTarget.mouseMoved( glimpseHoveredEvent );

                if ( glimpseHoveredEvent.isHandled( ) ) return true;
            }
        }

        return false;
    }

    public boolean mouseMoved0( I event )
    {
        if ( event == null ) return false;

        // if the mouse is hovering, recalculate hovered components every event
        // isButtonDown check isn't necessary like it is for MouseWrapperSWT.mouseMove(),
        // since this event would be a mouseDragged if it was
        List<GlimpseTargetStack> oldHovered = clearAllHovered( );

        // call getContainingTarget to setHovered correctly
        getContainingTargets( event );

        // get the new hovered stacks
        List<GlimpseTargetStack> newHovered = getHovered( );

        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );

        // if we have something hovered, send mouseMoved events
        if ( isDragHovered( ) )
        {
            for ( GlimpseTargetStack hoveredStack : newHovered )
            {
                Mouseable mouseHoveredTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseHoveredEvent = toGlimpseEvent( event, hoveredStack );

                if ( mouseHoveredTarget != null ) mouseHoveredTarget.mouseMoved( glimpseHoveredEvent );

                if ( glimpseHoveredEvent.isHandled( ) ) return true;
            }
        }

        return false;
    }

    public boolean mouseWheelMoved0( I event )
    {
        if ( event == null ) return false;

        // stacks with low indices are on top in the layout, and
        // have their mouse events generated first
        for ( GlimpseTargetStack stack : getContainingTargets( event ) )
        {
            Mouseable mouseTarget = getMouseTarget( stack );
            if ( mouseTarget == null ) return false;

            GlimpseMouseEvent glimpseEvent = toGlimpseEventWheel( event, stack );
            mouseTarget.mouseWheelMoved( glimpseEvent );

            if ( glimpseEvent.isHandled( ) ) return true;
        }

        return false;
    }
}
