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

import java.util.EnumSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.event.mouse.MouseButton;

public class GlimpseMouseWrapper
{
    private GlimpseMouseWrapper( )
    {

    }

    public static GlimpseMouseEvent fromMouseEvent( MouseEvent event )
    {
        int x = event.x;
        int y = event.y;
        int clickCount = event.count;
        EnumSet<MouseButton> buttons = getMouseButtons( event );
        EnumSet<ModifierKey> modifiers = getModifierKeys( event );

        GlimpseTargetStack stack = null;

        if ( event.data instanceof GlimpseTargetStack )
        {
            stack = (GlimpseTargetStack) event.data;
        }

        return new GlimpseMouseEvent( stack, modifiers, buttons, x, y, 0, clickCount );
    }

    public static GlimpseMouseEvent fromMouseWheelEvent( MouseEvent event )
    {
        int x = event.x;
        int y = event.y;
        // for some reason SWT appears to count one "click" of the mouse wheel as
        // three clicks on most platforms, this brings it in line with Swing
        // mouse wheel reporting. It also flips the sign
        // (extra logic avoids rounding signed 1 or 2 to 0)
        double c = - event.count / 3.0;
        int clickCount = ( int ) ( c < 0 ? Math.floor( c ) : Math.ceil( c ) );
        EnumSet<MouseButton> buttons = getMouseButtons( event );
        EnumSet<ModifierKey> modifiers = getModifierKeys( event );

        GlimpseTargetStack stack = null;

        if ( event.data instanceof GlimpseTargetStack )
        {
            stack = (GlimpseTargetStack) event.data;
        }

        return new GlimpseMouseEvent( stack, modifiers, buttons, x, y, clickCount, 0 );
    }

    public static EnumSet<MouseButton> getMouseButtons( MouseEvent event )
    {
        EnumSet<MouseButton> buttons;

        // SWT MouseEvents report the button pressed or released differently
        // than it reports the buttons held down during a move or drag.
        // However, GlimpseMouseEvent treats these two cases the same.

        switch( event.button )
        {
            case 1: buttons = EnumSet.of( MouseButton.Button1 ); break;
            case 2: buttons = EnumSet.of( MouseButton.Button2 ); break;
            case 3: buttons = EnumSet.of( MouseButton.Button3 ); break;
            default: buttons = EnumSet.noneOf( MouseButton.class ); break;
        }

        if ( ( event.stateMask & SWT.BUTTON1 ) != 0 ) buttons.add( MouseButton.Button1 );
        if ( ( event.stateMask & SWT.BUTTON2 ) != 0 ) buttons.add( MouseButton.Button2 );
        if ( ( event.stateMask & SWT.BUTTON3 ) != 0 ) buttons.add( MouseButton.Button3 );

        return buttons;
    }

    public static EnumSet<ModifierKey> getModifierKeys( MouseEvent event )
    {
        EnumSet<ModifierKey> modifiers = EnumSet.noneOf( ModifierKey.class );

        if ( ( event.stateMask & SWT.ALT ) != 0 ) modifiers.add( ModifierKey.Alt );
        if ( ( event.stateMask & SWT.CTRL ) != 0 ) modifiers.add( ModifierKey.Ctrl );
        if ( ( event.stateMask & SWT.COMMAND ) != 0 ) modifiers.add( ModifierKey.Meta );
        if ( ( event.stateMask & SWT.SHIFT ) != 0 ) modifiers.add( ModifierKey.Shift );

        return modifiers;
    }
}
