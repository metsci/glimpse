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
package com.metsci.glimpse.swt.event;

import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;

public class SwtAwtKeycodeMap
{

    private final static Logger logger = Logger.getLogger(SwtAwtKeycodeMap.class.getName());


    public static int awtKeycode(int swtKeycode)
    {
        switch (swtKeycode)
        {
            case 'a': return KeyEvent.VK_A;
            case 'b': return KeyEvent.VK_B;
            case 'c': return KeyEvent.VK_C;
            case 'd': return KeyEvent.VK_D;
            case 'e': return KeyEvent.VK_E;
            case 'f': return KeyEvent.VK_F;
            case 'g': return KeyEvent.VK_G;
            case 'h': return KeyEvent.VK_H;
            case 'i': return KeyEvent.VK_I;
            case 'j': return KeyEvent.VK_J;
            case 'k': return KeyEvent.VK_K;
            case 'l': return KeyEvent.VK_L;
            case 'm': return KeyEvent.VK_M;
            case 'n': return KeyEvent.VK_N;
            case 'o': return KeyEvent.VK_O;
            case 'p': return KeyEvent.VK_P;
            case 'q': return KeyEvent.VK_Q;
            case 'r': return KeyEvent.VK_R;
            case 's': return KeyEvent.VK_S;
            case 't': return KeyEvent.VK_T;
            case 'u': return KeyEvent.VK_U;
            case 'v': return KeyEvent.VK_V;
            case 'w': return KeyEvent.VK_W;
            case 'x': return KeyEvent.VK_X;
            case 'y': return KeyEvent.VK_Y;
            case 'z': return KeyEvent.VK_Z;

            case '0': return KeyEvent.VK_0;
            case '1': return KeyEvent.VK_1;
            case '2': return KeyEvent.VK_2;
            case '3': return KeyEvent.VK_3;
            case '4': return KeyEvent.VK_4;
            case '5': return KeyEvent.VK_5;
            case '6': return KeyEvent.VK_6;
            case '7': return KeyEvent.VK_7;
            case '8': return KeyEvent.VK_8;
            case '9': return KeyEvent.VK_9;

            case '`': return KeyEvent.VK_BACK_QUOTE;
            case ' ': return KeyEvent.VK_SPACE;
            case ',': return KeyEvent.VK_COMMA;
            case '.': return KeyEvent.VK_PERIOD;
            case '/': return KeyEvent.VK_SLASH;
            case '[': return KeyEvent.VK_BRACELEFT;
            case ']': return KeyEvent.VK_BRACERIGHT;
            case '-': return KeyEvent.VK_MINUS;
            case '=': return KeyEvent.VK_EQUALS;
            case ';': return KeyEvent.VK_SEMICOLON;
            case '\\': return KeyEvent.VK_BACK_SLASH;
            case '\'': return KeyEvent.VK_QUOTE;

            case SWT.CTRL:            return KeyEvent.VK_CONTROL;
            case SWT.SHIFT:           return KeyEvent.VK_SHIFT;
            case SWT.ALT:             return KeyEvent.VK_ALT;
            case SWT.COMMAND:         return KeyEvent.VK_META;

            case SWT.F1:              return KeyEvent.VK_F1;
            case SWT.F2:              return KeyEvent.VK_F2;
            case SWT.F3:              return KeyEvent.VK_F3;
            case SWT.F4:              return KeyEvent.VK_F4;
            case SWT.F5:              return KeyEvent.VK_F5;
            case SWT.F6:              return KeyEvent.VK_F6;
            case SWT.F7:              return KeyEvent.VK_F7;
            case SWT.F8:              return KeyEvent.VK_F8;
            case SWT.F9:              return KeyEvent.VK_F9;
            case SWT.F10:             return KeyEvent.VK_F10;
            case SWT.F11:             return KeyEvent.VK_F11;
            case SWT.F12:             return KeyEvent.VK_F12;
            case SWT.F13:             return KeyEvent.VK_F13;
            case SWT.F14:             return KeyEvent.VK_F14;
            case SWT.F15:             return KeyEvent.VK_F15;

            case SWT.ESC:             return KeyEvent.VK_ESCAPE;
            case SWT.BS:              return KeyEvent.VK_BACK_SPACE;
            case SWT.INSERT:          return KeyEvent.VK_INSERT;
            case SWT.DEL:             return KeyEvent.VK_DELETE;
            case SWT.HOME:            return KeyEvent.VK_HOME;
            case SWT.END:             return KeyEvent.VK_END;
            case SWT.PAGE_UP:         return KeyEvent.VK_PAGE_UP;
            case SWT.PAGE_DOWN:       return KeyEvent.VK_PAGE_DOWN;
            case SWT.ARROW_UP:        return KeyEvent.VK_UP;
            case SWT.ARROW_DOWN:      return KeyEvent.VK_DOWN;
            case SWT.ARROW_LEFT:      return KeyEvent.VK_LEFT;
            case SWT.ARROW_RIGHT:     return KeyEvent.VK_RIGHT;
            case SWT.PRINT_SCREEN:    return KeyEvent.VK_PRINTSCREEN;
            case SWT.SCROLL_LOCK:     return KeyEvent.VK_SCROLL_LOCK;
            case SWT.PAUSE:           return KeyEvent.VK_PAUSE;
            case SWT.TAB:             return KeyEvent.VK_TAB;
            case SWT.CR:              return KeyEvent.VK_ENTER;
            case SWT.LF:              return KeyEvent.VK_ENTER;
            case SWT.HELP:            return KeyEvent.VK_HELP;
            case SWT.CAPS_LOCK:       return KeyEvent.VK_CAPS_LOCK;

            case SWT.NUM_LOCK:        return KeyEvent.VK_NUM_LOCK;
            case SWT.KEYPAD_0:        return KeyEvent.VK_NUMPAD0;
            case SWT.KEYPAD_1:        return KeyEvent.VK_NUMPAD1;
            case SWT.KEYPAD_2:        return KeyEvent.VK_NUMPAD2;
            case SWT.KEYPAD_3:        return KeyEvent.VK_NUMPAD3;
            case SWT.KEYPAD_4:        return KeyEvent.VK_NUMPAD4;
            case SWT.KEYPAD_5:        return KeyEvent.VK_NUMPAD5;
            case SWT.KEYPAD_6:        return KeyEvent.VK_NUMPAD6;
            case SWT.KEYPAD_7:        return KeyEvent.VK_NUMPAD7;
            case SWT.KEYPAD_8:        return KeyEvent.VK_NUMPAD8;
            case SWT.KEYPAD_9:        return KeyEvent.VK_NUMPAD9;
            case SWT.KEYPAD_ADD:      return KeyEvent.VK_ADD;
            case SWT.KEYPAD_SUBTRACT: return KeyEvent.VK_SUBTRACT;
            case SWT.KEYPAD_MULTIPLY: return KeyEvent.VK_MULTIPLY;
            case SWT.KEYPAD_DIVIDE:   return KeyEvent.VK_DIVIDE;
            case SWT.KEYPAD_CR:       return KeyEvent.VK_ENTER;
            case SWT.KEYPAD_DECIMAL:  return KeyEvent.VK_PERIOD;
            case SWT.KEYPAD_EQUAL:    return KeyEvent.VK_EQUALS;

            default:
                logger.warning("No AWT keycode defined for this SWT keycode: swt-keycode = " + swtKeycode);
                return KeyEvent.VK_UNDEFINED;
        }
    }

    private SwtAwtKeycodeMap()
    { }

}
