/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.dnc.util;

import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.util.GeneralUtils.newArrayList;
import static java.lang.Integer.parseInt;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.util.Arrays.fill;
import static java.util.Collections.sort;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class DncMiscUtils
{

    public static final long MiB = 1024 * 1024;
    public static final long GiB = 1024 * MiB;

    public static <T> T requireResult( Future<? extends T> future )
    {
        try
        {
            while ( true )
            {
                try
                {
                    return future.get( );
                }
                catch ( InterruptedException e )
                { }
            }
        }
        catch ( ExecutionException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static Thread startThread( String name, boolean daemon, Runnable runnable )
    {
        Thread thread = new Thread( runnable, name );
        thread.setDaemon( daemon );
        thread.start( );
        return thread;
    }


    public static <V> V takeNewValue( BlockingQueue<V> queue, V oldValue ) throws InterruptedException
    {
        while ( true )
        {
            V value = queue.take( );
            if ( !equal( value, oldValue ) )
            {
                return value;
            }
        }
    }

    /**
     * Creates an executor with a single daemon thread and an
     * unbounded job queue. Jobs submitted after the executor
     * has been shutdown will be silently dropped.
     */
    public static ExecutorService newWorkerDaemon( String threadNamePrefix )
    {
        return new ThreadPoolExecutor( 1,
                                       1,
                                       0, MILLISECONDS,
                                       new LinkedBlockingQueue<Runnable>( ),
                                       newThreadFactory( threadNamePrefix, true ),
                                       new DiscardPolicy( ) );
    }

    public static ThreadFactory newThreadFactory( final String namePrefix, final boolean isDaemon )
    {
        return new ThreadFactory( )
        {
            final Object threadNumMutex = new Object( );
            int nextThreadNum = 0;

            int nextThreadNum( )
            {
                synchronized ( threadNumMutex )
                {
                    return ( nextThreadNum++ );
                }
            }

            public Thread newThread( Runnable runnable )
            {
                Thread thread = new Thread( runnable );
                thread.setDaemon( isDaemon );
                if ( namePrefix != null )
                {
                    thread.setName( namePrefix + nextThreadNum( ) );
                }
                return thread;
            }
        };
    }

    public static abstract class ThrowingRunnable implements Runnable
    {
        public abstract void runThrows( ) throws Exception;

        @Override
        public final void run( )
        {
            try
            {
                runThrows( );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
    }

    /**
     * Throws {@link IllegalArgumentException} if x is negative
     */
    public static int nextPowerOfTwo( int x )
    {
        if ( x < 0 ) throw new IllegalArgumentException( "Argument is negative: " + x );
        if ( x == 0 ) return 1;

        // Copy the highest set bit into all lower bits, then add
        // one -- but first, subtract one, so that it works if x
        // is already a power of two
        x--;
        x |= ( x >> 1 );
        x |= ( x >> 2 );
        x |= ( x >> 4 );
        x |= ( x >> 8 );
        x |= ( x >> 16 );
        x++;

        return x;
    }

    public static int sum( int... xs )
    {
        int sum = 0;
        for ( int x : xs ) sum += x;
        return sum;
    }

    public static long timeSince_MILLIS( long start_PMILLIS )
    {
        return ( System.currentTimeMillis( ) - start_PMILLIS );
    }

    public static <V> List<V> sorted( Collection<V> values, Comparator<? super V> comparator )
    {
        List<V> list = newArrayList( values );
        sort( list, comparator );
        return list;
    }

    public static <T> ArrayList<T> toArrayList( Iterable<? extends T> iterable )
    {
        ArrayList<T> list = new ArrayList<>( );
        for ( T t : iterable ) list.add( t );
        return list;
    }

    public static <T> T last( List<T> list )
    {
        return list.get( list.size( ) - 1 );
    }

    public static MappedByteBuffer memmapReadOnly( File file ) throws IOException
    {
        RandomAccessFile raf = null;
        try
        {
            raf = new RandomAccessFile( file, "r" );
            MappedByteBuffer mapped = raf.getChannel( ).map( READ_ONLY, 0, raf.length( ) );
            mapped.order( ByteOrder.nativeOrder( ) );
            return mapped;
        }
        finally
        {
            if ( raf != null ) raf.close( );
        }
    }

    public static MappedByteBuffer memmapReadWrite( File file ) throws IOException
    {
        RandomAccessFile raf = null;
        try
        {
            raf = new RandomAccessFile( file, "rw" );
            MappedByteBuffer mapped = raf.getChannel( ).map( READ_WRITE, 0, raf.length( ) );
            mapped.order( ByteOrder.nativeOrder( ) );
            return mapped;
        }
        finally
        {
            if ( raf != null ) raf.close( );
        }
    }

    public static MappedByteBuffer createAndMemmapReadWrite( File file, int newFileSize ) throws IOException
    {
        RandomAccessFile raf = null;
        try
        {
            raf = new RandomAccessFile( file, "rw" );
            raf.setLength( newFileSize );
            MappedByteBuffer mapped = raf.getChannel( ).map( READ_WRITE, 0, newFileSize );
            mapped.order( ByteOrder.nativeOrder( ) );
            return mapped;
        }
        finally
        {
            if ( raf != null ) raf.close( );
        }
    }

    public static void poslim( Buffer buf, int first, int count, int size )
    {
        buf.limit( size * ( first + count ) );
        buf.position( size * ( first ) );
    }

    public static long packBytesIntoLong( byte[] bytes )
    {
        if ( bytes.length > 7 ) throw new RuntimeException( "More than 7 bytes cannot be packed into a long: num-bytes = " + bytes.length );

        long packed = ( ( ( long ) bytes.length ) & 0xFF ) << 56;
        for ( int i = 0; i < bytes.length; i++ )
        {
            int shift = 8*( 6 - i );
            packed |= ( ( ( long ) bytes[ i ] ) & 0xFF ) << shift;
        }
        return packed;
    }


    public static byte[] unpackLongIntoBytes( long packed )
    {
        int length = ( int ) ( ( packed >> 56 ) & 0xFF );
        byte[] bytes = new byte[ length ];
        for ( int i = 0; i < bytes.length; i++ )
        {
            int shift = 8*( 6 - i );
            bytes[ i ] = ( byte ) ( ( packed >> shift ) & 0xFF );
        }
        return bytes;
    }

    public static String repchar( char c, int n )
    {
        char[] chars = new char[ n ];
        fill( chars, c );
        return String.valueOf( chars );
    }

    public static void writeIdsMapFile( Object2IntMap<String> idsMap, File file, Charset charset ) throws IOException
    {
        PrintStream stream = null;
        try
        {
            stream = new PrintStream( file, charset.name( ) );
            for ( Object2IntMap.Entry<String> en : idsMap.object2IntEntrySet( ) )
            {
                stream.println( en.getIntValue( ) + " " + en.getKey( ) );
            }
            stream.flush( );
        }
        finally
        {
            if ( stream != null ) stream.close( );
        }
    }

    public static Int2ObjectMap<String> readIdsMapFile( File file, Charset charset ) throws IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), charset ) );

            Int2ObjectMap<String> idsMap = new Int2ObjectLinkedOpenHashMap<>( );
            for ( int i = 0; true; i++ )
            {
                String line = reader.readLine( );
                if ( line == null ) break;

                String[] tokens = line.split( " " );
                if ( tokens.length < 2 ) throw new IOException( "Format error in " + file.getAbsolutePath( ) + " on line " + i );

                int id = parseInt( tokens[ 0 ] );
                String string = tokens[ 1 ];
                idsMap.put( id, string );
            }
            return idsMap;
        }
        finally
        {
            if ( reader != null ) reader.close( );
        }
    }

    public static <T> Object2IntMap<T> invertIdsMap( Int2ObjectMap<T> idsMap )
    {
        Object2IntMap<T> inverted = new Object2IntLinkedOpenHashMap<>( );
        for ( Int2ObjectMap.Entry<T> en : idsMap.int2ObjectEntrySet( ) )
        {
            inverted.put( en.getValue( ), en.getIntKey( ) );
        }
        return inverted;
    }

    public static <T> Object2IntMap<T> invertList( List<T> list )
    {
        Object2IntMap<T> inverted = new Object2IntLinkedOpenHashMap<>( );
        int nextNum = 0;
        for ( T value : list )
        {
            int num = ( nextNum++ );
            inverted.put( value, num );
        }
        return inverted;
    }

    public static <F,T> Function<F,T> constFunc( final T value )
    {
        return new Function<F,T>( )
        {
            public T apply( F input )
            {
                return value;
            }
        };
    }

    public static Map<String,Object> newAttrsMap( Object... keysAndValues )
    {
        Map<String,Object> map = new LinkedHashMap<>( );
        for ( int i = 0; i < keysAndValues.length; i+=2 )
        {
            map.put( ( String ) keysAndValues[ i ], keysAndValues[ i + 1 ] );
        }
        return map;
    }

    public static Function<String,Object> newAttrsFunc( final Map<String,Object> map )
    {
        return  new Function<String,Object>( )
        {
            public Object apply( String key )
            {
                return map.get( key );
            }
        };
    }

    public static File createNewDir( File parentDir, String childPath )
    {
        return createNewDir( new File( parentDir, childPath ) );
    }

    public static File createNewDir( String dirPath )
    {
        return createNewDir( new File( dirPath ) );
    }

    public static File createNewDir( File dir )
    {
        if ( !dir.mkdirs( ) )
        {
            if ( dir.exists( ) )
            {
                throw new RuntimeException( "Directory already exists: path = " + dir.getAbsolutePath( ) );
            }
            else
            {
                throw new RuntimeException( "Failed to create directory: path = " + dir.getAbsolutePath( ) );
            }
        }
        return dir;
    }

}
