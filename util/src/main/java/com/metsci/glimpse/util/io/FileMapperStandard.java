package com.metsci.glimpse.util.io;

import static com.metsci.glimpse.util.io.MappedBufferStats.*;
import static com.metsci.glimpse.util.io.MappedFile.*;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sun.nio.ch.FileChannelImpl;

@SuppressWarnings( "restriction" )
public class FileMapperStandard implements FileMapper
{

    /**
     * long map0( int mode, long position, long size )
     */
    protected static final Method FileChannelImpl_map0;
    static
    {
        try
        {
            FileChannelImpl_map0 = FileChannelImpl.class.getDeclaredMethod( "map0", int.class, long.class, long.class );
            FileChannelImpl_map0.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access " + FileChannelImpl.class.getName( ) + ".map0()", e );
        }
    }

    @Override
    public long map( RandomAccessFile raf, long size, boolean writable ) throws IOException
    {
        try
        {
            int mapMode = ( writable ? 1 : 0 );
            return ( ( long ) FileChannelImpl_map0.invoke( raf.getChannel( ), mapMode, 0, size ) );
        }
        catch ( InvocationTargetException e )
        {
            Throwable e2 = e.getTargetException( );
            if ( e2 instanceof IOException )
            {
                throw ( ( IOException ) e2 );
            }
            else if ( e2 instanceof Error )
            {
                throw ( ( Error ) e2 );
            }
            else
            {
                throw new RuntimeException( "Failed to memmap file", e2 );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to memmap file", e );
        }
    }

    /**
     * Unmapper( long address, long size, int capacity, FileDescriptor fd )
     */
    protected static final Constructor<?> Unmapper_init;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "sun.nio.ch.FileChannelImpl$Unmapper" );
            Unmapper_init = clazz.getDeclaredConstructor( long.class, long.class, int.class, FileDescriptor.class );
            Unmapper_init.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access sun.nio.ch.FileChannelImpl$Unmapper.<init>()", e );
        }
    }

    @Override
    public Runnable createUnmapper( long address, long size, RandomAccessFile raf )
    {
        try
        {
            // Unmapper updates count and totalSize, but only accepts 32-bit capacity, so
            // update totalCapacity manually. The manual update is not atomic with the update
            // inside Unmapper ... but this is only used for monitoring, so that's okay.
            addToMappedBufferStats( 0, 0, +size );

            FileDescriptor fd = getFileDescriptorForMapping( raf );
            Runnable unmapper = ( Runnable ) Unmapper_init.newInstance( address, size, 0, fd );
            return ( ) ->
            {
                unmapper.run( );
                addToMappedBufferStats( 0, 0, -size );
            };
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to create Unmapper", e );
        }
    }

}
