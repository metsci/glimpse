package com.metsci.glimpse.platformFixes;

import static com.metsci.glimpse.util.jnlu.FileUtils.copy;
import static com.metsci.glimpse.util.jnlu.NativeLibUtils.onPlatform;
import static java.awt.Window.getOwnerlessWindows;

import java.awt.Window;
import java.awt.peer.ComponentPeer;
import java.io.File;
import java.net.URL;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.util.jnlu.FileUtils;

public class WindowsFixes
{

    public static void fixWindowsQuirks( )
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



    public static final LibraryList libs = getLibs( );
    private static final boolean shouldApplyFixes = ( libs != null );
    private static boolean needsInit = shouldApplyFixes;

    private static LibraryList getLibs( )
    {
        if ( onPlatform( "win", "amd64"  ) ) return new LibraryList( "platformFixes/windows64", "libgcc_s_seh-1.dll", "libstdc++-6.dll", "windowsFixes.dll" );
        if ( onPlatform( "win", "x86_64" ) ) return new LibraryList( "platformFixes/windows64", "libgcc_s_seh-1.dll", "libstdc++-6.dll", "windowsFixes.dll" );
        if ( onPlatform( "win", "x86"    ) ) return new LibraryList( "platformFixes/windows32", "libgcc_s_dw2-1.dll", "libstdc++-6.dll", "windowsFixes.dll" );
        return null;
    }

    private static synchronized void initLibs( )
    {
        if ( needsInit )
        {
            try
            {
                ClassLoader cl = WindowsFixes.class.getClassLoader( );
                File tempDir = FileUtils.createTempDir( "platformFixes" );

                for ( String filename : libs.filenames )
                {
                    URL url = cl.getResource( libs.resourceDir + "/" + filename );
                    File file = new File( tempDir, filename );
                    copy( url, file );
                    System.load( file.getPath( ) );
                }

                needsInit = false;
            }
            catch ( Exception e )
            {
                throw new RuntimeException( "Failed to initialize windows fixes", e );
            }
        }
    }

}
