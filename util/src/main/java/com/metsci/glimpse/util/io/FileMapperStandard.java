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
package com.metsci.glimpse.util.io;

import static com.metsci.glimpse.util.io.MappedBufferStats.addToMappedBufferStats;
import static com.metsci.glimpse.util.io.MappedFile.getFileDescriptorForMapping;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

public class FileMapperStandard implements FileMapper
{

    /**
     * long map0( int mode, long position, long size )
     */
    protected static final BufferMapper mapper;
    static
    {
        BufferMapper mapper0;
        try
        {
            Class<?> clazz = Class.forName( "sun.nio.ch.FileChannelImpl" );
            Method FileChannelImpl_map0 = clazz.getDeclaredMethod( "map0", int.class, long.class, long.class );
            FileChannelImpl_map0.setAccessible( true );
            mapper0 = ( channel, mapMode, offset, size ) -> ( long ) FileChannelImpl_map0.invoke( channel, mapMode, offset, size );
        }
        catch ( Exception e )
        {
            try
            {
                Class<?> clazz = Class.forName( "sun.nio.ch.FileChannelImpl" );
                // Oracle JRE 15+
                Method FileChannelImpl_map0 = clazz.getDeclaredMethod( "map0", int.class, long.class, long.class, boolean.class );
                FileChannelImpl_map0.setAccessible( true );
                mapper0 = ( channel, mapMode, offset, size ) -> ( long ) FileChannelImpl_map0.invoke( channel, mapMode, offset, size, false );
            }
            catch ( Exception ex )
            {
                throw new RuntimeException( "Cannot access sun.nio.ch.FileChannelImpl.map0()", ex );
            }
        }

        mapper = mapper0;
    }

    private interface BufferMapper
    {
        long map( FileChannel channel, int mapMode, long offset, long size ) throws InvocationTargetException, IllegalAccessException;
    }

    @Override
    public long map( RandomAccessFile raf, long size, boolean writable ) throws IOException
    {
        try
        {
            int mapMode = ( writable ? 1 : 0 );
            return ( mapper.map( raf.getChannel( ), mapMode, 0, size ) );
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
            return ( ) -> {
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
