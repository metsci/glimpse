package com.metsci.glimpse;

import java.util.HashMap;
import java.util.Map;

public class WheelFix
{

    static
    {
        // XXX
        System.load( "C:/Users/mike/metron/projects/glimpse/wheelfix/wheelfix.dll" );
    }


    private static final Map<Long,Long> _hookHandles = new HashMap<>( );


    public static void activateInputFix( )
    {
        synchronized ( _hookHandles )
        {
            long threadId = Thread.currentThread( ).getId( );
            if ( _hookHandles.containsKey( threadId ) ) return;

            long[] hookHandles = { 0 };
            int errorCode = _activateWheelFix( hookHandles );
            if ( errorCode != 0 ) throw new RuntimeException( "Failed to activate input fix: " + _getErrorString( errorCode ) );

            _hookHandles.put( threadId, hookHandles[ 0 ] );
        }
    }

    public static void deactivateInputFix( )
    {
        synchronized ( _hookHandles )
        {
            long threadId = Thread.currentThread( ).getId( );
            if ( !_hookHandles.containsKey( threadId ) ) return;

            long hookHandle = _hookHandles.get( threadId );
            int errorCode = _deactivateWheelFix( hookHandle );
            if ( errorCode != 0 ) throw new RuntimeException( "Failed to deactivate input fix: " + _getErrorString( errorCode ) );

            _hookHandles.remove( threadId );
        }
    }

    private static native int _activateWheelFix( long[] result );
    private static native int _deactivateWheelFix( long hookHandle );
    private static native String _getErrorString( int errorCode );

}
