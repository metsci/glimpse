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
package com.metsci.glimpse.platformFixes;

import static com.metsci.glimpse.util.jnlu.NativeLibUtils.extractAndLoad;
import static com.metsci.glimpse.util.jnlu.NativeLibUtils.onPlatform;
import static com.metsci.glimpse.util.ugly.ModuleAccessChecker.expectDeepReflectiveAccess;
import static java.awt.Window.getOwnerlessWindows;

import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableList;

/**
 * Works on Oracle/OpenJDK 8 JVMs.
 * <p>
 * Works on OpenJDK 9+ JVMs, but requires the following JVM args:
 * <pre>
 * --add-opens java.desktop/java.awt=com.metsci.glimpse.platformFixes
 * </pre>
 */
public class WindowsFixes
{
    static
    {
        expectDeepReflectiveAccess( WindowsFixes.class, "java.desktop", "java.awt" );
    }

    public static void checkModuleAccess( )
    {
        // This method provides a way to explicitly trigger the static initializer
    }

    private static final ImmutableList<URL> nativeLibs = findNativeLibs( );
    private static ImmutableList<URL> findNativeLibs( )
    {
        if ( onPlatform( "win", "amd64"  ) ) return ImmutableList.of( WindowsFixes.class.getResource( "windows64/windowsFixes.dll" ) );
        if ( onPlatform( "win", "x86_64" ) ) return ImmutableList.of( WindowsFixes.class.getResource( "windows64/windowsFixes.dll" ) );
        if ( onPlatform( "win", "x86"    ) ) return ImmutableList.of( WindowsFixes.class.getResource( "windows32/windowsFixes.dll" ) );
        return ImmutableList.of( );
    }

    public static final boolean shouldApplyFixes = !nativeLibs.isEmpty( );

    private static boolean nativeLibsNeedInit = shouldApplyFixes;

    private static synchronized void initNativeLibs( )
    {
        if ( nativeLibsNeedInit )
        {
            extractAndLoad( nativeLibs, "windowsFixes" );
            nativeLibsNeedInit = false;
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
            initNativeLibs( );

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
            initNativeLibs( );
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
        // If this reflective access isn't necessary, avoid it, because
        // it triggers JVM warning messages that can be quite confusing
        if ( !shouldApplyFixes )
        {
            return w -> null;
        }

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
