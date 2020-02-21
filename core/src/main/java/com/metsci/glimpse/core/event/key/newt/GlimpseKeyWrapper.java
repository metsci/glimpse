/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.event.key.newt;

import static com.jogamp.newt.event.KeyEvent.*;
import static com.metsci.glimpse.core.event.key.GlimpseKey.*;
import static com.metsci.glimpse.core.event.mouse.newt.GlimpseMouseWrapper.getModifierKeys;

import java.util.EnumSet;

import com.jogamp.newt.event.KeyEvent;
import com.metsci.glimpse.core.context.GlimpseTargetStack;
import com.metsci.glimpse.core.event.key.GlimpseKey;
import com.metsci.glimpse.core.event.key.GlimpseKeyEvent;
import com.metsci.glimpse.core.event.mouse.ModifierKey;

public class GlimpseKeyWrapper
{

    public static GlimpseKeyEvent fromNewtKeyEvent( KeyEvent ev, GlimpseTargetStack stack )
    {
        GlimpseKey key = fromNewtKeyCode( ev.getKeyCode( ) );
        EnumSet<ModifierKey> modifiers = getModifierKeys( ev.getModifiers( ) );
        return new GlimpseKeyEvent( stack, modifiers, key );
    }

    public static GlimpseKey fromNewtKeyCode( short newtKeyCode )
    {
        switch ( newtKeyCode )
        {
            case VK_ESCAPE: return KEY_ESCAPE;

            case VK_F1: return KEY_F1;
            case VK_F2: return KEY_F2;
            case VK_F3: return KEY_F3;
            case VK_F4: return KEY_F4;
            case VK_F5: return KEY_F5;
            case VK_F6: return KEY_F6;
            case VK_F7: return KEY_F7;
            case VK_F8: return KEY_F8;
            case VK_F9: return KEY_F9;
            case VK_F10: return KEY_F10;
            case VK_F11: return KEY_F11;
            case VK_F12: return KEY_F12;
            case VK_F13: return KEY_F13;
            case VK_F14: return KEY_F14;
            case VK_F15: return KEY_F15;
            case VK_F16: return KEY_F16;
            case VK_F17: return KEY_F17;
            case VK_F18: return KEY_F18;
            case VK_F19: return KEY_F19;
            case VK_F20: return KEY_F20;
            case VK_F21: return KEY_F21;
            case VK_F22: return KEY_F22;
            case VK_F23: return KEY_F23;
            case VK_F24: return KEY_F24;

            case VK_PRINTSCREEN: return KEY_PRINTSCREEN;
            case VK_SCROLL_LOCK: return KEY_SCROLLLOCK;
            case VK_PAUSE: return KEY_PAUSE;

            case VK_0: return KEY_0;
            case VK_1: return KEY_1;
            case VK_2: return KEY_2;
            case VK_3: return KEY_3;
            case VK_4: return KEY_4;
            case VK_5: return KEY_5;
            case VK_6: return KEY_6;
            case VK_7: return KEY_7;
            case VK_8: return KEY_8;
            case VK_9: return KEY_9;

            case VK_A: return KEY_A;
            case VK_B: return KEY_B;
            case VK_C: return KEY_C;
            case VK_D: return KEY_D;
            case VK_E: return KEY_E;
            case VK_F: return KEY_F;
            case VK_G: return KEY_G;
            case VK_H: return KEY_H;
            case VK_I: return KEY_I;
            case VK_J: return KEY_J;
            case VK_K: return KEY_K;
            case VK_L: return KEY_L;
            case VK_M: return KEY_M;
            case VK_N: return KEY_N;
            case VK_O: return KEY_O;
            case VK_P: return KEY_P;
            case VK_Q: return KEY_Q;
            case VK_R: return KEY_R;
            case VK_S: return KEY_S;
            case VK_T: return KEY_T;
            case VK_U: return KEY_U;
            case VK_V: return KEY_V;
            case VK_W: return KEY_W;
            case VK_X: return KEY_X;
            case VK_Y: return KEY_Y;
            case VK_Z: return KEY_Z;

            case VK_BACK_QUOTE: return KEY_BACKTICK;
            case VK_TAB: return KEY_TAB;
            case VK_CAPS_LOCK: return KEY_CAPSLOCK;
            case VK_SHIFT: return KEY_SHIFT;
            case VK_CONTROL: return KEY_CTRL;
            case VK_ALT: return KEY_ALT;
            case VK_ALT_GRAPH: return KEY_ALT;
            case VK_WINDOWS: return KEY_WINDOWS;
            case VK_CONTEXT_MENU: return KEY_MENU;
            case VK_META: return KEY_META;

            case VK_SPACE: return KEY_SPACE;

            case VK_MINUS: return KEY_HYPHEN;
            case VK_EQUALS: return KEY_EQUALS;
            case VK_BACK_SPACE: return KEY_BACKSPACE;
            case VK_OPEN_BRACKET: return KEY_LEFTBRACKET;
            case VK_CLOSE_BRACKET: return KEY_RIGHTBRACKET;
            case VK_BACK_SLASH: return KEY_BACKSLASH;
            case VK_SEMICOLON: return KEY_SEMICOLON;
            case VK_QUOTE: return KEY_QUOTE;
            case VK_ENTER: return KEY_ENTER;
            case VK_COMMA: return KEY_COMMA;
            case VK_PERIOD: return KEY_PERIOD;
            case VK_SLASH: return KEY_SLASH;

            case VK_INSERT: return KEY_INSERT;
            case VK_DELETE: return KEY_DELETE;
            case VK_HOME: return KEY_HOME;
            case VK_END: return KEY_END;
            case VK_PAGE_UP: return KEY_PAGEUP;
            case VK_PAGE_DOWN: return KEY_PAGEDOWN;

            case VK_NUM_LOCK: return KEY_NUMLOCK;
            case VK_NUMPAD0: return KEY_NUMPAD_0;
            case VK_NUMPAD1: return KEY_NUMPAD_1;
            case VK_NUMPAD2: return KEY_NUMPAD_2;
            case VK_NUMPAD3: return KEY_NUMPAD_3;
            case VK_NUMPAD4: return KEY_NUMPAD_4;
            case VK_NUMPAD5: return KEY_NUMPAD_5;
            case VK_NUMPAD6: return KEY_NUMPAD_6;
            case VK_NUMPAD7: return KEY_NUMPAD_7;
            case VK_NUMPAD8: return KEY_NUMPAD_8;
            case VK_NUMPAD9: return KEY_NUMPAD_9;
            case VK_ADD: return KEY_NUMPAD_ADD;
            case VK_SUBTRACT: return KEY_NUMPAD_SUBTRACT;
            case VK_MULTIPLY: return KEY_NUMPAD_MULTIPLY;
            case VK_DIVIDE: return KEY_NUMPAD_DIVIDE;
            case VK_DECIMAL: return KEY_NUMPAD_DECIMAL;

            case VK_LEFT: return KEY_ARROW_LEFT;
            case VK_UP: return KEY_ARROW_UP;
            case VK_RIGHT: return KEY_ARROW_RIGHT;
            case VK_DOWN: return KEY_ARROW_DOWN;

            default: return KEY_UNRECOGNIZED;
        }
    }

}
