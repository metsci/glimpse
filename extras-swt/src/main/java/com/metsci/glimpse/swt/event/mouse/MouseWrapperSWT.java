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
package com.metsci.glimpse.swt.event.mouse;

import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseWrapper;
import com.metsci.glimpse.event.mouse.Mouseable;

public class MouseWrapperSWT extends MouseWrapper<MouseEvent> implements MouseListener, MouseMoveListener, MouseTrackListener, MouseWheelListener
{
    public MouseWrapperSWT( GlimpseCanvas canvas )
    {
        super( canvas );
    }

    @Override
    protected boolean isInterior( MouseEvent e, GlimpseBounds bounds )
    {
        GlimpseBounds canvasBounds = canvas.getTargetBounds( );
        return bounds.contains( e.x, canvasBounds.getHeight( ) - e.y );
    }

    @Override
    protected boolean isButtonDown( MouseEvent e )
    {
        return ( e.stateMask & SWT.BUTTON_MASK ) > 0;
    }

    @Override
    protected boolean isValid( MouseEvent e, GlimpseBounds bounds )
    {
        return e.getSource( ) instanceof Control;
    }

    @Override
    protected MouseEvent toLocalCoords( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null )
            return null;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null )
            return null;

        if ( !(e.getSource( ) instanceof Control) )
            return null;

        Rectangle parentBounds = ( (Control) e.getSource( ) ).getBounds( );

        int parentHeight = parentBounds.height;

        Event localEvent = new Event();

        localEvent.x = e.x - bounds.getX( );
        localEvent.y = e.y - ( parentHeight - ( bounds.getY( ) + bounds.getHeight( ) ) );

        localEvent.button = e.button;
        localEvent.count = e.count;
        localEvent.display = e.display;
        localEvent.stateMask = e.stateMask;
        localEvent.time = e.time;
        localEvent.widget = e.widget;

        // store the GlimpseTargetStack in the data field (marked as for application use)
        localEvent.data = stack;

        return new MouseEvent( localEvent );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent event )
    {
        return GlimpseMouseWrapper.fromMouseEvent( event );
    }

    protected GlimpseMouseEvent toLocalGlimpseWheelEvent( MouseEvent e, GlimpseTargetStack stack, boolean handled )
    {
        GlimpseMouseEvent event = GlimpseMouseWrapper.fromMouseWheelEvent( toLocalCoords( e, stack ) );
        event.setHandled( handled );
        return event;
    }

    @Override
    public void mouseEnter( MouseEvent event )
    {
        // save the old hovered stacks
        Set<GlimpseTargetStack> oldHovered = clearHovered( );
        
        // update hovered stacks
        getContainingTargets( event );
        
        // get the new hovered stacks
        Set<GlimpseTargetStack> newHovered = getHovered( );
        
        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
    }

    @Override
    public void mouseExit( MouseEvent event )
    {
        // save the old hovered stacks
        Set<GlimpseTargetStack> oldHovered = clearHovered( );
        
        // update hovered stacks
        getContainingTargets( event );
        
        // get the new hovered stacks
        Set<GlimpseTargetStack> newHovered = getHovered( );
        
        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
    }

    @Override
    public void mouseHover( MouseEvent e )
    {
        // not handled by GlimpseMouseListener
    }

    @Override
    public void mouseMove( MouseEvent event )
    {
        // if the mouse is hovering, recalculate hovered components every event
        Set<GlimpseTargetStack> oldHovered = isButtonDown( event ) ? clearHovered( ) : clearAllHovered( );

        // call getContainingTarget to setHovered correctly
        getContainingTargets( event );

        // get the new hovered stacks
        Set<GlimpseTargetStack> newHovered = getHovered( );
        
        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );

        if ( isDragHovered( ) )
        {
            boolean handled = false;
            
            Set<GlimpseTargetStack> hoveredList = getDragHovered( );
            for ( GlimpseTargetStack hoveredStack : hoveredList )
            {
                Mouseable mouseHoveredTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseHoveredEvent = toLocalGlimpseEvent( event, hoveredStack, handled );

                if ( mouseHoveredTarget != null ) mouseHoveredTarget.mouseMoved( glimpseHoveredEvent );
                
                handled = glimpseHoveredEvent.isHandled( );
            }
        }
    }

    @Override
    public void mouseScrolled( MouseEvent event )
    {
        List<GlimpseTargetStack> list = getContainingTargets( event );
        if ( list == null ) return;

        boolean handled = false;
        
        // stacks with low indices are on top in the layout, and
        // have their mouse events generated first
        for ( GlimpseTargetStack stack : list )
        {
            Mouseable mouseTarget = getMouseTarget( stack );
            if ( mouseTarget == null ) return;

            GlimpseMouseEvent glimpseEvent = toLocalGlimpseWheelEvent( event, stack, handled );
            mouseTarget.mouseWheelMoved( glimpseEvent );
            
            handled = glimpseEvent.isHandled( );
        }
    }

    @Override
    public void mouseDoubleClick( MouseEvent event )
    {
        // not handled by GlimpseMouseListener
    }

    @Override
    public void mouseDown( MouseEvent event )
    {
        List<GlimpseTargetStack> list = getContainingTargets( event );

        setAllHovered( list );

        boolean handled = false;
        
        // stacks with low indices are on top in the layout, and
        // have their mouse events generated first
        for ( GlimpseTargetStack stack : list )
        {
            Mouseable mouseTarget = getMouseTarget( stack );
            if ( mouseTarget == null ) return;

            GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, stack, handled );

            mouseTarget.mousePressed( glimpseEvent );
            
            handled = glimpseEvent.isHandled( );
        }
    }

    @Override
    public void mouseUp( MouseEvent event )
    {
        // always always deliver the mouseUp event regardless of which
        // component the mouse event occurred inside
        if ( isDragHovered( ) )
        {
            boolean handled = false;
         
            Set<GlimpseTargetStack> hoveredList = getDragHovered( );
            for ( GlimpseTargetStack hoveredStack : hoveredList )
            {
                Mouseable mouseTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, hoveredStack, handled );

                if ( mouseTarget != null ) mouseTarget.mouseReleased( glimpseEvent );
                
                handled = glimpseEvent.isHandled( );
            }
        }

        // call getContainingTarget to setHovered correctly
        // call after event is sent because we want to send the mouseReleased event
        // to the previously hovered component then setHovered to the GlimpseTarget
        // currently under the mouse
        getContainingTargets( event );
    }
}
