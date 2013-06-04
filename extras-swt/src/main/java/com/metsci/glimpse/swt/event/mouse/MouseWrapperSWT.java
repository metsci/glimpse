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
import com.metsci.glimpse.event.mouse.MouseWrapperImpl;

public class MouseWrapperSWT extends MouseWrapperImpl<MouseEvent> implements MouseListener, MouseMoveListener, MouseTrackListener, MouseWheelListener
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

    protected MouseEvent toLocalCoords( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null ) return null;

        if ( ! ( e.getSource( ) instanceof Control ) ) return null;

        Rectangle parentBounds = ( ( Control ) e.getSource( ) ).getBounds( );

        int parentHeight = parentBounds.height;

        Event localEvent = new Event( );

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

    protected MouseEvent toLocalCoordsWheel( MouseEvent e, GlimpseTargetStack stack )
    {
        return toLocalCoords( e, stack );
    }

    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent event )
    {
        return GlimpseMouseWrapper.fromMouseEvent( event );
    }

    protected GlimpseMouseEvent toGlimpseEventWheel( MouseEvent event )
    {
        return GlimpseMouseWrapper.fromMouseWheelEvent( event );
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
    public void mouseEnter( MouseEvent event )
    {
        mouseEntered0( event );
    }

    @Override
    public void mouseExit( MouseEvent event )
    {
        mouseExited0( event );
    }

    @Override
    public void mouseHover( MouseEvent event )
    {
        // not handled by GlimpseMouseListener
    }

    @Override
    public void mouseMove( MouseEvent event )
    {
        if ( isButtonDown( event ) )
        {
            mouseDragged0( event );
        }
        else
        {
            mouseMoved0( event );
        }
    }

    @Override
    public void mouseScrolled( MouseEvent event )
    {
        mouseWheelMoved0( event );
    }

    @Override
    public void mouseDoubleClick( MouseEvent event )
    {
        // not handled by GlimpseMouseListener
    }

    @Override
    public void mouseDown( MouseEvent event )
    {
        mousePressed0( event );
    }

    @Override
    public void mouseUp( MouseEvent event )
    {
        mouseReleased0( event );
    }
}
