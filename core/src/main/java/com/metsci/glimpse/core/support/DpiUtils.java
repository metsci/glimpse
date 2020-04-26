package com.metsci.glimpse.core.support;

public class DpiUtils
{
    public static final String DEFAULT_DPI_KEY = "com.metsci.glimpse.dpi.default";
    private static final int defaultDpiValue = Integer.parseInt( System.getProperty( DEFAULT_DPI_KEY, "-1" ) );

    public static int adjustForDesktopScaling( int px )
    {
        return ( int ) adjustForDesktopScaling( ( float ) px );
    }

    public static float adjustForDesktopScaling( float px )
    {
        return px * getDefaultDpi( ) / 96;
    }

    public static int getDefaultDpi( )
    {
        if ( defaultDpiValue < 0 )
        {
            return 96;
        }

        // TODO Not implemented yet...
        return 120;
    }
}
