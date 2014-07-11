package com.metsci.glimpse.platformFixes;

public class PlatformFixes
{


    public static void fixPlatformQuirks( )
    {
        // Call the platform-specific function for each platform ... only
        // the one for the platform we're on will have any effect

        WindowsFixes.fixWindowsQuirks( );
    }


    public static void applyPlatformFixes( )
    {
        // Call the platform-specific function for each platform ... only
        // the one for the platform we're on will have any effect

        WindowsFixes.applyWindowsFixes( );
    }


}
