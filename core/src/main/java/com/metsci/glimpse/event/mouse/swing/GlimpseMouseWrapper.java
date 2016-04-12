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

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.META_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON2;
import static java.awt.event.MouseEvent.BUTTON3;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.EnumSet;

import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.event.mouse.MouseButton;

public class GlimpseMouseWrapper
{
    private GlimpseMouseWrapper( )
    {

    }

    public static GlimpseMouseEvent fromMouseEvent( MouseEvent event, GlimpseTargetStack stack, int x, int y )
    {
        int clickCount = event.getClickCount( );
        EnumSet<MouseButton> buttons = getMouseButtons( event );
        EnumSet<ModifierKey> modifiers = getModifierKeys( event );

        return new GlimpseMouseEvent( stack, modifiers, buttons, x, y, 0, clickCount );
    }

    public static GlimpseMouseEvent fromMouseEvent( MouseEvent event )
    {
        GlimpseTargetStack stack = null;

        if ( event instanceof GlimpseSwingMouseEvent )
        {
            stack = ( ( GlimpseSwingMouseEvent ) event ).getTargetStack( );
        }

        return fromMouseEvent( event, stack, event.getX( ), event.getY( ) );
    }

    public static GlimpseMouseEvent fromMouseWheelEvent( MouseWheelEvent event, GlimpseTargetStack stack, int x, int y )
    {
        int wheelRotation = event.getWheelRotation( );
        int clickCount = event.getClickCount( );

        EnumSet<MouseButton> buttons = getMouseButtons( event );
        EnumSet<ModifierKey> modifiers = getModifierKeys( event );

        return new GlimpseMouseEvent( stack, modifiers, buttons, x, y, wheelRotation, clickCount );
    }

    public static GlimpseMouseEvent fromMouseWheelEvent( MouseWheelEvent event )
    {
        GlimpseTargetStack stack = null;

        if ( event instanceof GlimpseSwingMouseWheelEvent )
        {
            stack = ( ( GlimpseSwingMouseWheelEvent ) event ).getTargetStack( );
        }

        return fromMouseWheelEvent( event, stack, event.getX( ), event.getY( ) );
    }

    public static EnumSet<MouseButton> getMouseButtons( MouseEvent event )
    {
        EnumSet<MouseButton> buttons;

        switch ( event.getButton( ) )
        {
            case BUTTON1:
                buttons = EnumSet.of( MouseButton.Button1 );
                break;
            case BUTTON2:
                buttons = EnumSet.of( MouseButton.Button2 );
                break;
            case BUTTON3:
                buttons = EnumSet.of( MouseButton.Button3 );
                break;
            default:
                buttons = EnumSet.noneOf( MouseButton.class );
                break;
        }

        int mod = event.getModifiersEx( );

        if ( ( mod & BUTTON1_DOWN_MASK ) == BUTTON1_DOWN_MASK ) buttons.add( MouseButton.Button1 );
        if ( ( mod & BUTTON2_DOWN_MASK ) == BUTTON2_DOWN_MASK ) buttons.add( MouseButton.Button2 );
        if ( ( mod & BUTTON3_DOWN_MASK ) == BUTTON3_DOWN_MASK ) buttons.add( MouseButton.Button3 );

        return buttons;
    }

    public static EnumSet<ModifierKey> getModifierKeys( MouseEvent event )
    {
        EnumSet<ModifierKey> modifiers = EnumSet.noneOf( ModifierKey.class );

        int mod = event.getModifiersEx( );

        if ( ( mod & ALT_DOWN_MASK ) == ALT_DOWN_MASK ) modifiers.add( ModifierKey.Alt );
        if ( ( mod & CTRL_DOWN_MASK ) == CTRL_DOWN_MASK ) modifiers.add( ModifierKey.Ctrl );
        if ( ( mod & META_DOWN_MASK ) == META_DOWN_MASK ) modifiers.add( ModifierKey.Meta );
        if ( ( mod & SHIFT_DOWN_MASK ) == SHIFT_DOWN_MASK ) modifiers.add( ModifierKey.Shift );

        return modifiers;
    }
}
