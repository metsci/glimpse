/*
 * Copyright (c) 2019, Metron, Inc.
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

import static com.metsci.glimpse.event.mouse.FocusBehavior.*;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.eclipse.swt.widgets.Display;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.FocusBehavior;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseWrapperImpl;
import com.metsci.glimpse.event.mouse.swing.GlimpseMouseWrapper;
import com.metsci.glimpse.event.mouse.swing.MouseWrapperSwing;

public class MouseWrapperSWTBridge extends MouseWrapperImpl<MouseEvent> implements MouseWheelListener, MouseMotionListener, MouseListener
{
    public MouseWrapperSWTBridge( GlimpseCanvas canvas )
    {
        this( canvas, CLICK_FOCUS );
    }

    public MouseWrapperSWTBridge( GlimpseCanvas canvas, FocusBehavior focusBehavior )
    {
        super( canvas, focusBehavior );
    }

    @Override
    protected boolean isInterior( MouseEvent e, GlimpseBounds bounds )
    {
        return bounds.contains( e.getX( ), e.getComponent( ).getBounds( ).height - e.getY( ) );
    }

    @Override
    protected boolean isButtonDown( MouseEvent e )
    {
        return ( e.getModifiersEx( ) & MouseWrapperSwing.ANY_BUTTON_DOWN_MASK ) > 0;
    }

    @Override
    protected boolean isValid( MouseEvent e, GlimpseBounds bounds )
    {
        return e.getComponent( ) != null;
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null ) return null;

        int parentHeight = canvas.getTargetBounds( ).getHeight( );

        int local_x = e.getX( ) - bounds.getX( );
        int local_y = e.getY( ) - ( parentHeight - ( bounds.getY( ) + bounds.getHeight( ) ) );

        return GlimpseMouseWrapper.fromMouseEvent( e, stack, local_x, local_y );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEventWheel( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null ) return null;

        int parentHeight = canvas.getTargetBounds( ).getHeight( );

        int local_x = e.getX( ) - bounds.getX( );
        int local_y = e.getY( ) - ( parentHeight - ( bounds.getY( ) + bounds.getHeight( ) ) );

        return GlimpseMouseWrapper.fromMouseWheelEvent( ( MouseWheelEvent ) e, stack, local_x, local_y );
    }

    @Override
    public void mouseClicked( final MouseEvent event )
    {
        Display.getDefault( ).asyncExec( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseClicked0( event );
            }
        } );
    }

    @Override
    public void mouseEntered( final MouseEvent event )
    {
        Display.getDefault( ).asyncExec( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseEntered0( event );
            }
        } );
    }

    @Override
    public void mouseExited( final MouseEvent event )
    {
        Display.getDefault( ).asyncExec( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseExited0( event );
            }
        } );
    }

    @Override
    public void mousePressed( final MouseEvent event )
    {
        Display.getDefault( ).asyncExec( new Runnable( )
        {
            @Override
            public void run( )
            {
                mousePressed0( event );
            }
        } );
    }

    @Override
    public void mouseReleased( final MouseEvent event )
    {
        Display.getDefault( ).asyncExec( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseReleased0( event );
            }
        } );
    }

    @Override
    public void mouseDragged( final MouseEvent event )
    {
        Display.getDefault( ).asyncExec( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseDragged0( event );
            }
        } );
    }

    @Override
    public void mouseMoved( final MouseEvent event )
    {
        Display.getDefault( ).asyncExec( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseMoved0( event );
            }
        } );
    }

    @Override
    public void mouseWheelMoved( final MouseWheelEvent event )
    {
        Display.getDefault( ).asyncExec( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseWheelMoved0( event );
            }
        } );
    }
}
