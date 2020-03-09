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

import static com.metsci.glimpse.util.jnlu.NativeLibUtils.onPlatform;
import static com.metsci.glimpse.util.ugly.CleanerUtils.registerCleaner;
import static java.lang.String.format;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;

/**
 * Represents a file that gets memory-mapped, in its entirety, even if it is larger than 2GB.
 * <p>
 * The NIO Buffer APIs do not support 64-bit indexing, so it is not possible to access a large
 * file through a single Buffer. Instead, this class does a single memory-map call for the whole
 * file, and then creates Buffer objects, as needed, for slices of the memory block. Buffer
 * object creation is cheap.
 * <p>
 * Works on Oracle/OpenJDK 8 JVMs.
 * <p>
 * Works on OpenJDK 9+ JVMs, but requires the following JVM args:
 * <pre>
 * --add-opens java.base/sun.nio.ch=com.metsci.glimpse.util
 * --add-opens java.base/jdk.internal.ref=com.metsci.glimpse.util
 * --add-opens java.base/java.nio=com.metsci.glimpse.util
 * </pre>
 */
public class MappedFile
{
    protected static final FileMapper mapper;
    static
    {
        if ( onPlatform( "win", "x86_64" ) || onPlatform( "win", "amd64" ) )
        {
            mapper = new FileMapperWindows64( );
        }
        else
        {
            mapper = new FileMapperStandard( );
        }
    }


    protected final File file;
    protected final boolean writable;
    protected final ByteOrder byteOrder;

    protected final FileDescriptor fd;
    protected final long address;
    protected final long size;

    /**
     * Invoking {@link #disposer} is equivalent to calling {@link #dispose()}. However,
     * {@link #disposer} does not contain a strong reference to the {@link MappedFile}
     * instance, and can therefore be used without preventing the {@link MappedFile}
     * from being garbage collected.
     * <p>
     * Has the same semantics as a {@code java.lang.ref.Cleaner.Cleanable}:
     * <ul>
     * <li>Invoked automatically when the {@link MappedFile} is eligible for garbage collection
     * <li>May be invoked explicitly
     * <li>Runs its action at most once, regardless of how many times it gets invoked
     * </ul>
     * <strong>IMPORTANT:</strong> Must not be invoked while slices of this MappedFile
     * are still in use. If a slice is used after its MappedFile has been disposed,
     * behavior is undefined.
     */
    public final Runnable disposer;


    public MappedFile( File file, ByteOrder byteOrder ) throws IOException
    {
        this( file, byteOrder, false );
    }

    public MappedFile( File file, ByteOrder byteOrder, boolean writable ) throws IOException
    {
        this( file, byteOrder, writable, -1L );
    }

    public MappedFile( File file, ByteOrder byteOrder, long setSize ) throws IOException
    {
        this( file, byteOrder, true, setSize );
    }

    protected MappedFile( File file, ByteOrder byteOrder, boolean writable, long setSize ) throws IOException
    {
        this.file = file;
        this.writable = writable;
        this.byteOrder = byteOrder;

        String rafMode = ( this.writable ? "rw" : "r" );
        try (RandomAccessFile raf = new RandomAccessFile( file, rafMode ))
        {
            if ( setSize >= 0 )
            {
                raf.setLength( setSize );
            }

            this.size = raf.length( );
            this.fd = duplicateForMapping( raf.getFD( ) );

            if ( this.size == 0 )
            {
                // If size is zero, then address doesn't matter -- we can just set it to zero, like FileChannelImpl does
                this.address = 0;
            }
            else
            {
                this.address = mapper.map( raf, this.size, this.writable );
            }

            Runnable unmapper = mapper.createUnmapper( this.address, this.size, raf );
            this.disposer = registerCleaner( this, unmapper )::clean;
        }
    }

    public File file( )
    {
        return this.file;
    }

    public boolean writable( )
    {
        return this.writable;
    }

    public ByteOrder byteOrder( )
    {
        return this.byteOrder;
    }

    public long size( )
    {
        return this.size;
    }

    public void copyTo( long position, int size, ByteBuffer dest )
    {
        if ( dest.isDirect( ) && isDirectBuffer( dest ) )
        {
            // This block does all the same steps as the block below,
            // but without creating a temporary slice buffer

            if ( size < 0 )
            {
                throw new IllegalArgumentException( "Illegal slice size: size = " + size );
            }

            if ( position < 0 || position + size > this.size )
            {
                throw new RuntimeException( format( "Slice falls outside bounds of file: slice-position = %d, slice-size = %d, file-size = %d", position, size, this.size ) );
            }

            if ( dest.isReadOnly( ) )
            {
                throw new ReadOnlyBufferException( );
            }

            if ( dest.remaining( ) < size )
            {
                throw new BufferOverflowException( );
            }

            long sAddr = this.address + position;
            long dAddr = getDirectBufferAddress( dest ) + dest.position( );
            copyMemory( sAddr, dAddr, size );
            dest.position( dest.position( ) + size );
        }
        else
        {
            dest.put( this.slice( position, size ) );
        }
    }

    public MappedByteBuffer slice( long position, int size )
    {
        if ( size < 0 )
        {
            throw new IllegalArgumentException( "Illegal slice size: size = " + size );
        }

        if ( position < 0 || position + size > this.size )
        {
            throw new RuntimeException( format( "Slice falls outside bounds of file: slice-position = %d, slice-size = %d, file-size = %d", position, size, this.size ) );
        }

        MappedByteBuffer buffer = asDirectBuffer( this.address + position, size, this.fd, this, this.writable );
        buffer.order( this.byteOrder );
        return buffer;
    }

    public void force( )
    {
        if ( this.writable && this.size > 0 )
        {
            force( this.fd, this.address, this.size );
        }
    }

    /**
     * <strong>IMPORTANT:</strong> This method must not be called while slices of this MappedFile are
     * still in use. If a slice is used after its MappedFile has been disposed, behavior is undefined.
     */
    public void dispose( )
    {
        this.disposer.run( );
    }

    // Lots of verbose code to get access to various JVM-internal functionality

    protected static final Object unsafe;
    protected static final int pageSize;
    protected static final Method Unsafe_copyMemory;
    static
    {
        try
        {
            Class<?> Unsafe_class = Class.forName( "sun.misc.Unsafe" );

            Constructor<?> Unsafe_new = Unsafe_class.getDeclaredConstructor( );
            Unsafe_new.setAccessible( true );
            unsafe = Unsafe_new.newInstance( );

            Method Unsafe_pageSize = Unsafe_class.getDeclaredMethod( "pageSize" );
            pageSize = ( Integer ) Unsafe_pageSize.invoke( unsafe );

            Unsafe_copyMemory = Unsafe_class.getDeclaredMethod( "copyMemory", Long.TYPE, Long.TYPE, Long.TYPE );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access sun.misc.Unsafe", e );
        }
    }

    protected static void copyMemory( long srcAddress, long destAddress, long bytes )
    {
        try
        {
            Unsafe_copyMemory.invoke( unsafe, srcAddress, destAddress, bytes );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    protected static final Class<?> DirectBuffer_class;
    protected static final Method DirectBuffer_address;
    static
    {
        try
        {
            DirectBuffer_class = Class.forName( "sun.nio.ch.DirectBuffer" );
            DirectBuffer_address = DirectBuffer_class.getDeclaredMethod( "address" );
            DirectBuffer_address.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access sun.nio.ch.DirectBuffer.address()", e );
        }
    }

    public static boolean isDirectBuffer( Object obj )
    {
        return DirectBuffer_class.isInstance( obj );
    }

    public static long getDirectBufferAddress( Object directBuffer )
    {
        try
        {
            return ( Long ) DirectBuffer_address.invoke( directBuffer );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * FileDispatcherImpl( )
     */
    protected static final Constructor<?> FileDispatcherImpl_init;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "sun.nio.ch.FileDispatcherImpl" );
            FileDispatcherImpl_init = clazz.getDeclaredConstructor( );
            FileDispatcherImpl_init.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access sun.nio.ch.FileDispatcherImpl.<init>()", e );
        }
    }

    /**
     * FileDescriptor duplicateForMapping( FileDescriptor fd )
     */
    protected static final Method FileDispatcher_duplicateForMapping;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "sun.nio.ch.FileDispatcher" );
            FileDispatcher_duplicateForMapping = clazz.getDeclaredMethod( "duplicateForMapping", FileDescriptor.class );
            FileDispatcher_duplicateForMapping.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access sun.nio.ch.FileDispatcher.duplicateForMapping()", e );
        }
    }

    public static FileDescriptor getFileDescriptorForMapping( RandomAccessFile raf ) throws IOException
    {
        return duplicateForMapping( raf.getFD( ) );
    }

    protected static FileDescriptor duplicateForMapping( FileDescriptor fd ) throws IOException
    {
        try
        {
            Object fileDispatcher = FileDispatcherImpl_init.newInstance( );
            return ( ( FileDescriptor ) FileDispatcher_duplicateForMapping.invoke( fileDispatcher, fd ) );
        }
        catch ( InvocationTargetException e )
        {
            Throwable e2 = e.getTargetException( );
            if ( e2 instanceof IOException )
            {
                throw ( ( IOException ) e2 );
            }
            else
            {
                throw new RuntimeException( "Failed to duplicate file descriptor", e2 );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to duplicate file descriptor", e );
        }
    }

    /**
     * DirectByteBuffer( int cap, long addr, FileDescriptor fd, Runnable unmapper )
     */
    protected static final Constructor<?> DirectByteBuffer_init;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "java.nio.DirectByteBuffer" );
            DirectByteBuffer_init = clazz.getDeclaredConstructor( int.class, long.class, FileDescriptor.class, Runnable.class );
            DirectByteBuffer_init.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access java.nio.DirectByteBuffer.<init>()", e );
        }
    }

    /**
     * DirectByteBufferR( int cap, long addr, FileDescriptor fd, Runnable unmapper )
     */
    protected static final Constructor<?> DirectByteBufferR_init;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "java.nio.DirectByteBufferR" );
            DirectByteBufferR_init = clazz.getDeclaredConstructor( int.class, long.class, FileDescriptor.class, Runnable.class );
            DirectByteBufferR_init.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access java.nio.DirectByteBufferR.<init>()", e );
        }
    }

    /**
     * Object att
     */
    protected static final Field DirectByteBuffer_att;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "java.nio.DirectByteBuffer" );
            DirectByteBuffer_att = clazz.getDeclaredField( "att" );
            DirectByteBuffer_att.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access java.nio.DirectByteBuffer.att", e );
        }
    }

    /**
     * The {@code fd} arg is used in {@link MappedByteBuffer#isLoaded()}, {@link MappedByteBuffer#load()},
     * and {@link MappedByteBuffer#force()}.
     * <p>
     * The {@code attachment} arg will be stored by strong-reference in the returned buffer -- and therefore
     * won't be garbage-collected until the returned buffer has been garbage-collected.
     */
    protected static MappedByteBuffer asDirectBuffer( long address, int capacity, FileDescriptor fd, Object attachment, boolean writable )
    {
        try
        {
            Constructor<?> init = ( writable ? DirectByteBuffer_init : DirectByteBufferR_init );
            MappedByteBuffer buffer = ( MappedByteBuffer ) init.newInstance( capacity, address, fd, null );
            DirectByteBuffer_att.set( buffer, attachment );
            return buffer;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to create ByteBuffer", e );
        }
    }

    /**
     * long force0( FileDescriptor fd, long address, long length )
     */
    protected static final Method MappedByteBuffer_force0;
    static
    {
        try
        {
            MappedByteBuffer_force0 = MappedByteBuffer.class.getDeclaredMethod( "force0", FileDescriptor.class, long.class, long.class );
            MappedByteBuffer_force0.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access " + MappedByteBuffer.class.getName( ) + ".force0()", e );
        }
    }

    protected static void force( FileDescriptor fd, long address, long length ) throws RuntimeException
    {
        try
        {
            MappedByteBuffer buffer = asDirectBuffer( address, 1, fd, null, true );

            long offsetIntoPage = address % pageSize;
            if ( offsetIntoPage < 0 )
            {
                offsetIntoPage += pageSize;
            }

            long pageStart = address - offsetIntoPage;
            long lengthFromPageStart = length + offsetIntoPage;

            MappedByteBuffer_force0.invoke( buffer, fd, pageStart, lengthFromPageStart );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to force mapped file contents to storage device", e );
        }
    }

}
