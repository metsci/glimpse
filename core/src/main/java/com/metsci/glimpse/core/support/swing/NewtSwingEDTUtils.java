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
package com.metsci.glimpse.core.support.swing;

import static com.metsci.glimpse.core.support.swing.NewtSwingEDTUtils.ModalBlockedStatus.DEFINITELY_BLOCKED;
import static com.metsci.glimpse.core.support.swing.NewtSwingEDTUtils.ModalBlockedStatus.DEFINITELY_NOT_BLOCKED;
import static com.metsci.glimpse.core.support.swing.NewtSwingEDTUtils.ModalBlockedStatus.UNKNOWN;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logSevere;
import static javax.swing.SwingUtilities.getWindowAncestor;

import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.logging.Logger;

import com.jogamp.nativewindow.NativeWindow;
import com.jogamp.nativewindow.awt.JAWTWindow;
import com.jogamp.newt.event.InputEvent;

public class NewtSwingEDTUtils
{
    private static final Logger logger = getLogger( NewtSwingEDTUtils.class );


    public static enum ModalBlockedStatus
    {
        DEFINITELY_BLOCKED, DEFINITELY_NOT_BLOCKED, UNKNOWN
    }

    /**
     * <strong>WARNING:</strong> This method relies on implementation details of both
     * NEWT and AWT. If for any reason it cannot determine whether the event is modal-
     * blocked, it returns {@link ModalBlockedStatus#UNKNOWN}. Client code should be
     * written with this possibility in mind.
     * <p>
     * Works on Oracle/OpenJDK 8 JVMs.
     * <p>
     * Works on OpenJDK 9+ JVMs, but requires the following JVM arg:
     * <pre>
     * --add-opens java.desktop/java.awt=com.metsci.glimpse.core
     * </pre>
     */
    public static ModalBlockedStatus getModalBlockedStatus( InputEvent ev )
    {
        // TODO: Does JOGL always use the window as the event source?
        Object source = ev.getSource( );
        if ( source instanceof NativeWindow )
        {
            // TODO: Does JOGL's window hierarchy always follow this pattern?
            NativeWindow parent = ( ( NativeWindow ) source ).getParent( );
            if ( parent instanceof JAWTWindow )
            {
                Component c = ( ( JAWTWindow ) parent ).getAWTComponent( );
                if ( c != null )
                {
                    Window w = getWindowAncestor( c );
                    if ( w != null )
                    {
                        return modalBlockedStatusFn.apply( w );
                    }
                }
            }
        }
        return UNKNOWN;
    }

    protected static final Function<Window,ModalBlockedStatus> modalBlockedStatusFn = createModalBlockedStatusFn( );
    protected static Function<Window,ModalBlockedStatus> createModalBlockedStatusFn( )
    {
        try
        {
            Method method = Window.class.getDeclaredMethod( "isModalBlocked" );
            method.setAccessible( true );
            return ( w ) ->
            {
                try
                {
                    Object result = method.invoke( w );
                    if ( result instanceof Boolean )
                    {
                        return ( ( Boolean ) result ? DEFINITELY_BLOCKED : DEFINITELY_NOT_BLOCKED );
                    }
                    else
                    {
                        return UNKNOWN;
                    }
                }
                catch ( ReflectiveOperationException | IllegalArgumentException e )
                {
                    return UNKNOWN;
                }
            };
        }
        catch ( Exception e )
        {
            logSevere( logger, "Cannot reliably determine a window's modal-blocked status on this JVM", e );
            return ( w ) ->
            {
                return UNKNOWN;
            };
        }
    }

}
