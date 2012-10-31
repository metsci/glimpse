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
package com.metsci.glimpse.event.mouse.swing;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.Set;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseWrapper;
import com.metsci.glimpse.event.mouse.Mouseable;

public class MouseWrapperSwing extends MouseWrapper<MouseEvent> implements MouseWheelListener, MouseMotionListener, MouseListener
{
    public static final int ANY_BUTTON_DOWN_MASK = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK;

    public MouseWrapperSwing( GlimpseCanvas canvas )
    {
        super( canvas );
    }

    @Override
    protected boolean isInterior( MouseEvent e, GlimpseBounds bounds )
    {
        return bounds.contains( e.getX( ), e.getComponent( ).getBounds( ).height - e.getY( ) );
    }

    @Override
    protected boolean isButtonDown( MouseEvent e )
    {
        return ( e.getModifiersEx( ) & ANY_BUTTON_DOWN_MASK ) > 0;
    }

    @Override
    protected boolean isValid( MouseEvent e, GlimpseBounds bounds )
    {
        return e.getComponent( ) != null;
    }

    @Override
    protected MouseEvent toLocalCoords( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null ) return null;

        if ( e.getComponent( ) == null ) return null;

        int parentHeight = e.getComponent( ).getBounds( ).height;

        Component source = e.getComponent( );
        int id = e.getID( );
        long when = e.getWhen( );
        int modifiers = e.getModifiers( );
        int local_x = e.getX( ) - bounds.getX( );
        int local_y = e.getY( ) - ( parentHeight - ( bounds.getY( ) + bounds.getHeight( ) ) );
        int clickCount = e.getClickCount( );
        boolean popupTrigger = e.isPopupTrigger( );
        int button = e.getButton( );

        MouseEvent localEvent = new GlimpseSwingMouseEvent( stack, source, id, when, modifiers, local_x, local_y, clickCount, popupTrigger, button );

        return localEvent;
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e )
    {
        return GlimpseMouseWrapper.fromMouseEvent( e );
    }

    protected MouseWheelEvent toLocalCoords( MouseWheelEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null ) return null;

        if ( e.getComponent( ) == null ) return null;

        int parentHeight = e.getComponent( ).getBounds( ).height;

        Component source = e.getComponent( );
        int id = e.getID( );
        long when = e.getWhen( );
        int modifiers = e.getModifiers( );
        int local_x = e.getX( ) - bounds.getX( );
        int local_y = e.getY( ) - ( parentHeight - ( bounds.getY( ) + bounds.getHeight( ) ) );
        int clickCount = e.getClickCount( );
        boolean popupTrigger = e.isPopupTrigger( );
        int scrollType = e.getScrollType( );
        int scrollAmount = e.getScrollAmount( );
        int wheelRotation = e.getWheelRotation( );

        MouseWheelEvent localEvent = new GlimpseSwingMouseWheelEvent( stack, source, id, when, modifiers, local_x, local_y, clickCount, popupTrigger, scrollType, scrollAmount, wheelRotation );

        return localEvent;
    }

    protected GlimpseMouseEvent toLocalGlimpseEvent( MouseWheelEvent e, GlimpseTargetStack stack )
    {
        return GlimpseMouseWrapper.fromMouseWheelEvent( toLocalCoords( e, stack ) );
    }
    
    protected GlimpseMouseEvent toLocalGlimpseEvent( MouseWheelEvent e, GlimpseTargetStack stack, boolean handled )
    {
        GlimpseMouseEvent event = GlimpseMouseWrapper.fromMouseWheelEvent( toLocalCoords( e, stack ) );
        event.setHandled( handled );
        return event;
    }

    @Override
    public void mouseClicked( MouseEvent event )
    {
        // not handled by GlimpseMouseListener, use mousePressed/mouseReleased
    }

    @Override
    public void mouseEntered( MouseEvent event )
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
    public void mouseExited( MouseEvent event )
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
    public void mousePressed( MouseEvent event )
    {
        List<GlimpseTargetStack> list = getContainingTargets( event );

        setAllHovered( list );

        // stacks with low indices are on top in the layout, and
        // have their mouse events generated first
        for ( GlimpseTargetStack stack : list )
        {
            Mouseable mouseTarget = getMouseTarget( stack );
            if ( mouseTarget == null ) return;

            GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, stack );

            mouseTarget.mousePressed( glimpseEvent );
        
            if ( glimpseEvent.isHandled( ) ) break;
        }
    }

    @Override
    public void mouseReleased( MouseEvent event )
    {
        // always always deliver the mouseUp event regardless of which
        // component the mouse event occurred inside
        if ( isDragHovered( ) )
        {
            Set<GlimpseTargetStack> hoveredList = getDragHovered( );
            for ( GlimpseTargetStack hoveredStack : hoveredList )
            {
                Mouseable mouseTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, hoveredStack );

                if ( mouseTarget != null ) mouseTarget.mouseReleased( glimpseEvent );
                
                if ( glimpseEvent.isHandled( ) ) break;
            }
        }

        // call getContainingTarget to setHovered correctly
        // call after event is sent because we want to send the mouseReleased event
        // to the previously hovered component then setHovered to the GlimpseTarget
        // currently under the mouse
        getContainingTargets( event );
    }

    @Override
    public void mouseDragged( MouseEvent event )
    {
        // save the old hovered stacks
        Set<GlimpseTargetStack> oldHovered = clearHovered( );
        
        // call getContainingTarget to setHovered correctly
        getContainingTargets( event );

        // get the new hovered stacks
        Set<GlimpseTargetStack> newHovered = getHovered( );
        
        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
        
        if ( isDragHovered( ) )
        {
            Set<GlimpseTargetStack> hoveredList = getDragHovered( );
            for ( GlimpseTargetStack hoveredStack : hoveredList )
            {
                Mouseable mouseHoveredTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseHoveredEvent = toLocalGlimpseEvent( event, hoveredStack );

                if ( mouseHoveredTarget != null ) mouseHoveredTarget.mouseMoved( glimpseHoveredEvent );
                
                if ( glimpseHoveredEvent.isHandled( ) ) break;
            }
        }
    }

    @Override
    public void mouseMoved( MouseEvent event )
    {
        // if the mouse is hovering, recalculate hovered components every event
        // isButtonDown check isn't necessary like it is for MouseWrapperSWT.mouseMove(),
        // since this event would be a mouseDragged if it was
        Set<GlimpseTargetStack> oldHovered = clearAllHovered( );

        // call getContainingTarget to setHovered correctly
        getContainingTargets( event );

        // get the new hovered stacks
        Set<GlimpseTargetStack> newHovered = getHovered( );
        
        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
        
        // if we have something hovered, send mouseMoved events
        if ( isDragHovered( ) )
        {
            for ( GlimpseTargetStack hoveredStack : newHovered )
            {
                Mouseable mouseHoveredTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseHoveredEvent = toLocalGlimpseEvent( event, hoveredStack );

                if ( mouseHoveredTarget != null ) mouseHoveredTarget.mouseMoved( glimpseHoveredEvent );
                
                if ( glimpseHoveredEvent.isHandled( ) ) break;
            }
        }
    }
    
    @Override
    public void mouseWheelMoved( MouseWheelEvent event )
    {
        // stacks with low indices are on top in the layout, and
        // have their mouse events generated first
        for ( GlimpseTargetStack stack : getContainingTargets( event ) )
        {
            Mouseable mouseTarget = getMouseTarget( stack );
            if ( mouseTarget == null ) return;

            GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, stack );
            mouseTarget.mouseWheelMoved( glimpseEvent );
            
            if ( glimpseEvent.isHandled( ) ) break;
        }
    }
}
