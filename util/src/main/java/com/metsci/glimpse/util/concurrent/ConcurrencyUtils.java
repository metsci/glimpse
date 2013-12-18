package com.metsci.glimpse.util.concurrent;

import java.util.concurrent.locks.ReentrantLock;

public class ConcurrencyUtils
{

    public static void requireLock( ReentrantLock lock )
    {
        if ( !lock.isHeldByCurrentThread( ) )
        {
            throw new RuntimeException( "Lock is not held by current thread: thread-name = " + Thread.currentThread( ).getName( ) );
        }
    }

}
