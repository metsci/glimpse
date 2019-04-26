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
package com.metsci.glimpse.platformFixes;

import static com.metsci.glimpse.util.jnlu.NativeLibUtils.onPlatform;
import static java.awt.Window.getOwnerlessWindows;

import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.util.jnlu.LibraryList;

public class WindowsFixes
{

    public static final LibraryList libs = getLibs( );

    private static LibraryList getLibs( )
    {
        if ( onPlatform( "win", "amd64" ) ) return new LibraryList( "platformFixes/windows64", "windowsFixes.dll" );
        if ( onPlatform( "win", "x86_64" ) ) return new LibraryList( "platformFixes/windows64", "windowsFixes.dll" );
        if ( onPlatform( "win", "x86" ) ) return new LibraryList( "platformFixes/windows32", "windowsFixes.dll" );
        return null;
    }

    public static final boolean shouldApplyFixes = ( libs != null );

    private static boolean libsNeedInit = shouldApplyFixes;

    private static synchronized void initLibs( )
    {
        if ( libsNeedInit )
        {
            libs.extractAndLoad( WindowsFixes.class.getClassLoader( ), "windowsFixes" );
            libsNeedInit = false;
        }
    }

    private static boolean pollingNeedsInit = shouldApplyFixes;
    public static final long defaultPollingInterval_MILLIS = 1000;

    public static synchronized void fixWindowsQuirks( )
    {
        fixWindowsQuirks( defaultPollingInterval_MILLIS );
    }

    public static synchronized void fixWindowsQuirks( long pollingInterval_MILLIS )
    {
        if ( pollingNeedsInit )
        {
            initLibs( );

            TimerTask applyFixesTask = new TimerTask( )
            {
                public void run( )
                {
                    applyWindowsFixes( );
                }
            };

            Timer timer = new Timer( "WindowsFixes Polling", true );
            timer.schedule( applyFixesTask, 0, pollingInterval_MILLIS );

            pollingNeedsInit = false;
        }
    }

    public static synchronized void applyWindowsFixes( )
    {
        if ( shouldApplyFixes )
        {
            initLibs( );
            String errorString = _applyFixes( );
            if ( errorString != null ) throw new RuntimeException( "Failed to apply Windows fixes: " + errorString );
        }
    }

    private static native String _applyFixes( );

    // Called from native code
    private static void handleVerticalMaximize( final long hwnd )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                Window window = findWindow( hwnd );
                if ( window != null )
                {
                    window.invalidate( );
                    window.validate( );
                }
            }
        } );
    }

    private static Window findWindow( long hwnd )
    {
        // A map would scale better as the number of windows increases ...
        // but that would require bookkeeping, and presumably there won't be
        // all that many top-level windows
        for ( Window w : getOwnerlessWindows( ) )
        {
            Long h = getHWndFn.apply( w );
            if ( h != null && h == hwnd )
            {
                return w;
            }
        }
        return null;
    }

    private static final Function<Window,Long> getHWndFn = createGetHWndFn( );
    private static Function<Window,Long> createGetHWndFn( )
    {
        try
        {
            Field Component_peer = Component.class.getDeclaredField( "peer" );
            Component_peer.setAccessible( true );
            return w ->
            {
                try
                {
                    Object peer = Component_peer.get( w );
                    return ( Long ) peer.getClass( ).getMethod( "getHWnd" ).invoke( peer );
                }
                catch ( Exception e )
                {
                    return null;
                }
            };
        }
        catch ( Exception e )
        {
            return w -> null;
        }
    }

}
