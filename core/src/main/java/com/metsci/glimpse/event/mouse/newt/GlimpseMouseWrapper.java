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

import java.util.EnumSet;

import com.jogamp.newt.event.MouseEvent;
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
        return fromMouseEvent( event, ( GlimpseTargetStack ) event.getAttachment( ), event.getX( ), event.getY( ) );
    }

    public static GlimpseMouseEvent fromMouseEvent( MouseEvent event, GlimpseTargetStack stack, int x, int y )
    {
        double wheelIncrement = getWheelIncrement( event );
        int clickCount = event.getClickCount( );
        EnumSet<MouseButton> buttons = getMouseButtons( event );
        EnumSet<ModifierKey> modifiers = getModifierKeys( event );

        return new GlimpseMouseEvent( stack, modifiers, buttons, x, y, wheelIncrement, clickCount );
    }

    public static EnumSet<MouseButton> getMouseButtons( MouseEvent event )
    {
        EnumSet<MouseButton> buttons;

        switch ( event.getButton( ) )
        {
            case MouseEvent.BUTTON1:
                buttons = EnumSet.of( MouseButton.Button1 );
                break;
            case MouseEvent.BUTTON2:
                buttons = EnumSet.of( MouseButton.Button2 );
                break;
            case MouseEvent.BUTTON3:
                buttons = EnumSet.of( MouseButton.Button3 );
                break;
            default:
                buttons = EnumSet.noneOf( MouseButton.class );
                break;
        }

        return buttons;
    }

    public static double getWheelIncrement( MouseEvent event )
    {
        // wheel increment is usually reported in index 1
        // however, when shift is down it is reported in index 0
        // there might be other surprises lurking here
        // for now, just take the first non-zero increment

        for ( float increment : event.getRotation( ) )
        {
            if ( increment != 0.0 ) return -increment;
        }

        return 0;
    }

    public static EnumSet<ModifierKey> getModifierKeys( MouseEvent event )
    {
        EnumSet<ModifierKey> modifiers = EnumSet.noneOf( ModifierKey.class );

        int mod = event.getModifiers( );

        if ( ( mod & MouseEvent.ALT_MASK ) == MouseEvent.ALT_MASK ) modifiers.add( ModifierKey.Alt );
        if ( ( mod & MouseEvent.CTRL_MASK ) == MouseEvent.CTRL_MASK ) modifiers.add( ModifierKey.Ctrl );
        if ( ( mod & MouseEvent.META_MASK ) == MouseEvent.META_MASK ) modifiers.add( ModifierKey.Meta );
        if ( ( mod & MouseEvent.SHIFT_MASK ) == MouseEvent.SHIFT_MASK ) modifiers.add( ModifierKey.Shift );

        return modifiers;
    }
}
