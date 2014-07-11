package com.metsci.glimpse.platformFixes;

import static com.metsci.glimpse.util.jnlu.NativeLibUtils.onPlatform;
import static java.awt.Window.getOwnerlessWindows;

import java.awt.Window;
import java.awt.peer.ComponentPeer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

public class WindowsFixes
{

    public static final LibraryList libs = getLibs( );
    private static LibraryList getLibs( )
    {
        if ( onPlatform( "win", "amd64"  ) ) return new LibraryList( "platformFixes/windows64", "libgcc_s_seh-1.dll", "libstdc++-6.dll", "windowsFixes.dll" );
        if ( onPlatform( "win", "x86_64" ) ) return new LibraryList( "platformFixes/windows64", "libgcc_s_seh-1.dll", "libstdc++-6.dll", "windowsFixes.dll" );
        if ( onPlatform( "win", "x86"    ) ) return new LibraryList( "platformFixes/windows32", "libgcc_s_dw2-1.dll", "libstdc++-6.dll", "windowsFixes.dll" );
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
        //
        for ( Window w : getOwnerlessWindows( ) )
        {
            Long h = getHwnd( w );
            if ( h != null && h == hwnd )
            {
                return w;
            }
        }
        return null;
    }

    private static Long getHwnd( Window window )
    {
        try
        {
            @SuppressWarnings( "deprecation" )
            ComponentPeer peer = window.getPeer( );
            Class<?> wcpClass = Class.forName( "sun.awt.windows.WComponentPeer" );
            if ( wcpClass.isInstance( peer ) )
            {
                return ( Long ) wcpClass.getMethod( "getHWnd" ).invoke( peer );
            }
            else
            {
                return null;
            }
        }
        catch ( Exception e )
        {
            return null;
        }
    }

}
