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
package com.metsci.glimpse.support.swing;

import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.metsci.glimpse.util.var.Disposable;

import jogamp.newt.WindowImpl;

/**
 * A creative workaround for changing the NEWT click timeout, which is hard-
 * coded to 300ms and not straightforward to change, even using reflection.
 *
 * <p>Like in AWT/Swing, NEWT's click timeout is used for detecting multi-clicks.
 * <em>Unlike</em> in AWT/Swing, it is <em>also</em> used to decide whether a
 * PRESS+RELEASE counts as a CLICK.
 *
 * @author hogye
 */
public class NewtClickTimeoutWorkaround
{
    private static final Logger logger = getLogger( NewtClickTimeoutWorkaround.class );


    public static Disposable attachNewtClickTimeoutWorkaround( Window window, long newTimeout_MILLIS )
    {
        try
        {
            Field WindowImpl_pState0 = WindowImpl.class.getDeclaredField( "pState0" );
            WindowImpl_pState0.setAccessible( true );
            Object pState0 = WindowImpl_pState0.get( window );

            Field WindowImpl_pState1 = WindowImpl.class.getDeclaredField( "pState1" );
            WindowImpl_pState1.setAccessible( true );
            Object pState1 = WindowImpl_pState1.get( window );

            Field PointerState0_lastButtonPressTime = pState0.getClass( ).getDeclaredField( "lastButtonPressTime" );
            PointerState0_lastButtonPressTime.setAccessible( true );

            long pressTimeShift = newTimeout_MILLIS - MouseEvent.getClickTimeout( );

            // The listener may remove itself early in response to an exception, which is
            // fine -- after that, running the returned Disposable will have no effect
            return addNewtMouseListener( window, new MouseAdapter( )
            {
                @Override
                public void mousePressed( MouseEvent ev )
                {
                    try
                    {
                        if ( ev.getPointerCount( ) == 1 )
                        {
                            long evTime = ev.getWhen( );

                            // Check for unexpected state before mucking with WindowImpl internals
                            long pressTime0 = PointerState0_lastButtonPressTime.getLong( pState0 );
                            long pressTime1 = PointerState0_lastButtonPressTime.getLong( pState1 );
                            if ( evTime != pressTime0 || evTime != pressTime1 )
                            {
                                // Skip the "set" calls and jump to the catch block
                                throw new IllegalStateException( "Unexpected WindowImpl state: ev-press = " + evTime + ", press0 = " + pressTime0 + ", press1 = " + pressTime1 );
                            }

                            PointerState0_lastButtonPressTime.setLong( pState0, evTime + pressTimeShift );
                            PointerState0_lastButtonPressTime.setLong( pState1, evTime + pressTimeShift );
                        }
                    }
                    catch ( Exception e )
                    {
                        logWarning( logger, "Failed to apply NEWT click-timeout workaround", e );
                        window.removeMouseListener( this );
                    }
                }
            } );
        }
        catch ( Exception e )
        {
            logWarning( logger, "Failed to attach NEWT click-timeout workaround", e );
            return ( ) -> { };
        }
    }

    protected static Disposable addNewtMouseListener( Window window, MouseListener listener )
    {
        window.addMouseListener( listener );

        return ( ) ->
        {
            window.removeMouseListener( listener );
        };
    }

}
