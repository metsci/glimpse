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
package com.metsci.glimpse.event.mouse.swing;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseWrapperImpl;

public class MouseWrapperSwing extends MouseWrapperImpl<MouseEvent> implements MouseWheelListener, MouseMotionListener, MouseListener
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

    protected MouseWheelEvent toLocalCoordsWheel( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        if ( ! ( e instanceof MouseWheelEvent ) ) return null;

        MouseWheelEvent wheelEvent = ( MouseWheelEvent ) e;

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
        int scrollType = wheelEvent.getScrollType( );
        int scrollAmount = wheelEvent.getScrollAmount( );
        int wheelRotation = wheelEvent.getWheelRotation( );

        MouseWheelEvent localEvent = new GlimpseSwingMouseWheelEvent( stack, source, id, when, modifiers, local_x, local_y, clickCount, popupTrigger, scrollType, scrollAmount, wheelRotation );

        return localEvent;
    }

    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e )
    {
        return GlimpseMouseWrapper.fromMouseEvent( e );
    }

    protected GlimpseMouseEvent toGlimpseEventWheel( MouseEvent e )
    {
        return GlimpseMouseWrapper.fromMouseWheelEvent( ( MouseWheelEvent ) e );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e, GlimpseTargetStack stack )
    {
        return toGlimpseEvent( toLocalCoords( e, stack ) );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEventWheel( MouseEvent e, GlimpseTargetStack stack )
    {
        return toGlimpseEventWheel( toLocalCoordsWheel( e, stack ) );
    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent e )
    {
        mouseWheelMoved0( e );
    }

    @Override
    public void mouseClicked( MouseEvent e )
    {
        mouseClicked0( e );
    }

    @Override
    public void mousePressed( MouseEvent e )
    {
        mousePressed0( e );
    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
        mouseReleased0( e );
    }

    @Override
    public void mouseEntered( MouseEvent e )
    {
        mouseEntered0( e );
    }

    @Override
    public void mouseExited( MouseEvent e )
    {
        mouseExited0( e );
    }

    @Override
    public void mouseDragged( MouseEvent e )
    {
        mouseDragged0( e );
    }

    @Override
    public void mouseMoved( MouseEvent e )
    {
        mouseMoved0( e );
    }
}
