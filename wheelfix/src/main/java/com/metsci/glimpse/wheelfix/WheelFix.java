package com.metsci.glimpse.wheelfix;

import static com.metsci.glimpse.util.jnlu.FileUtils.copy;
import static com.metsci.glimpse.util.jnlu.NativeLibUtils.onPlatform;

import java.io.File;
import java.net.URL;

import com.metsci.glimpse.util.jnlu.FileUtils;

public class WheelFix
{

    public static final String wheelfixLibFilenameProperty = "wheelfix.libfile";


    private static final String libFilename = getLibFilename( );
    private static final boolean shouldApplyFix = ( libFilename != null );
    private static boolean needsInit = shouldApplyFix;


    public static void applyWheelFix( )
    {
        if ( shouldApplyFix )
        {
            initLib( );
            String errorString = _applyWheelFix( );
            if ( errorString != null ) throw new RuntimeException( "Failed to apply wheel-fix: " + errorString );
        }
    }

    private static String getLibFilename( )
    {
        String override = System.getProperty( wheelfixLibFilenameProperty );
        if ( override != null ) return override;

        if ( onPlatform( "win", "amd64"  ) ) return "wheelfix64.dll";
        if ( onPlatform( "win", "x86_64" ) ) return "wheelfix64.dll";
        if ( onPlatform( "win", "x86"    ) ) return "wheelfix32.dll";

        return null;
    }

    private static synchronized void initLib( )
    {
        if ( needsInit )
        {
            try
            {
                ClassLoader cl = WheelFix.class.getClassLoader( );
                URL url = cl.getResource( "wheelfix/" + libFilename );

                File tempDir = FileUtils.createTempDir( "wheelfix" );
                File file = new File( tempDir, libFilename );
                copy( url, file );

                System.err.println( file.getPath( ) );

                System.load( file.getPath( ) );
                needsInit = false;
            }
            catch ( Exception e )
            {
                throw new RuntimeException( "Failed to initialize wheelfix", e );
            }
        }
    }

    private static native String _applyWheelFix( );

}
