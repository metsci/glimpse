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
package com.metsci.glimpse.event.mouse.newt;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseEvent.PointerType;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.metsci.glimpse.canvas.NewtGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseWrapperImpl;

public class MouseWrapperNewt extends MouseWrapperImpl<MouseEvent> implements MouseListener
{
    protected GLWindow glWindow;
    protected final int scaleX;
    protected final int scaleY;

    public MouseWrapperNewt( NewtGlimpseCanvas canvas )
    {
        super( canvas );

        this.glWindow = canvas.getGLWindow( );
        int[] scale = canvas.getSurfaceScale( );
        this.scaleX = scale[0];
        this.scaleY = scale[1];
    }

    @Override
    protected boolean isInterior( MouseEvent e, GlimpseBounds bounds )
    {
        return bounds.contains( e.getX( ) / scaleX, ( glWindow.getSurfaceHeight( ) - e.getY( ) ) / scaleY );
    }

    @Override
    protected boolean isButtonDown( MouseEvent e )
    {
        return ( e.getButtonsDown( ).length ) > 0;
    }

    @Override
    protected boolean isValid( MouseEvent e, GlimpseBounds bounds )
    {
        return true;
    }

    protected MouseEvent toLocalCoords( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null ) return null;

        int parentHeight = glWindow.getSurfaceHeight( );

        short eventType = e.getEventType( );
        Object source = e.getSource( );
        long when = e.getWhen( );
        int modifiers = e.getModifiers( );
        int[] local_x = getX( e, bounds );
        int[] local_y = getY( e, bounds, parentHeight );
        float[] pressure = getPressure( e );
        float maxPressure = e.getMaxPressure( );
        PointerType[] types = getPointerTypes( e );
        short[] pointerIds = getPointerIds( e );
        short clickCount = e.getClickCount( );
        short button = e.getButton( );
        float[] rotation = e.getRotation( );
        float rotationScale = e.getRotationScale( );

        MouseEvent event = new MouseEvent( eventType, source, when, modifiers, types, pointerIds, local_x, local_y, pressure, maxPressure, button, clickCount, rotation, rotationScale );
        event.setAttachment( stack );

        return event;
    }

    protected int[] getX( MouseEvent e, GlimpseBounds bounds )
    {
        int[] allX = e.getAllX( );
        int[] newX = new int[allX.length];

        for ( int i = 0; i < allX.length; i++ )
        {
            newX[i] = ( allX[i] / scaleX ) - bounds.getX( );
        }

        return newX;
    }

    protected int[] getY( MouseEvent e, GlimpseBounds bounds, int parentHeight )
    {
        int[] allY = e.getAllY( );
        int[] newY = new int[allY.length];

        for ( int i = 0; i < allY.length; i++ )
        {
            newY[i] = e.getY( ) / scaleY - ( parentHeight / scaleY - ( bounds.getY( ) + bounds.getHeight( ) ) );
        }

        return newY;
    }

    protected PointerType[] getPointerTypes( MouseEvent e )
    {
        PointerType[] types = new PointerType[e.getPointerCount( )];

        for ( int i = 0; i < e.getPointerCount( ); i++ )
        {
            types[i] = e.getPointerType( i );
        }

        return types;
    }

    protected float[] getPressure( MouseEvent e )
    {
        float[] ids = new float[e.getPointerCount( )];

        for ( int i = 0; i < e.getPointerCount( ); i++ )
        {
            ids[i] = e.getPressure( i, false );
        }

        return ids;
    }

    protected short[] getPointerIds( MouseEvent e )
    {
        short[] ids = new short[e.getPointerCount( )];

        for ( int i = 0; i < e.getPointerCount( ); i++ )
        {
            ids[i] = e.getPointerId( i );
        }

        return ids;
    }

    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e )
    {
        return GlimpseMouseWrapper.fromMouseEvent( e );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e, GlimpseTargetStack stack )
    {
        return toGlimpseEvent( toLocalCoords( e, stack ) );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEventWheel( MouseEvent e, GlimpseTargetStack stack )
    {
        return toGlimpseEvent( toLocalCoords( e, stack ) );
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

    @Override
    public void mouseWheelMoved( MouseEvent e )
    {
        mouseWheelMoved0( e );
    }
}