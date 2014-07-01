package com.metsci.glimpse.support.wheelfix;


public class WheelFix
{

    static
    {
        // XXX
        System.load( "C:/Users/mike/metron/projects/glimpse/code/wheelfix-natives/wheelfix.dll" );
    }


    public static void applyWheelFix( )
    {
        String errorString = _activateWheelFix( );
        if ( errorString != null ) throw new RuntimeException( "Failed to activate wheel-fix: " + errorString );
    }

    private static native String _activateWheelFix( );

}
