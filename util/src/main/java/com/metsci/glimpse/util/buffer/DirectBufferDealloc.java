/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.util.buffer;

import static com.metsci.glimpse.util.UglyUtils.findClass;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;

import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.logging.Logger;

import com.metsci.glimpse.util.ThrowingSupplier;

/**
 * Uses non-public APIs (e.g. via reflection) to free the off-heap memory that
 * backs an NIO direct buffer. Normally such off-heap memory is not freed until
 * the garbage collector finalizes the direct buffer. This delay causes problems
 * when a lot of off-heap memory is used, but garbage collections are infrequent.
 * <p>
 * Works on Oracle/OpenJDK 8 JVMs.
 * <p>
 * Works on OpenJDK 9+ JVMs, but requires the following JVM args:
 * <pre>
 * --add-opens java.base/sun.nio.ch=com.metsci.glimpse.util
 * --add-opens java.base/jdk.internal.ref=com.metsci.glimpse.util
 * </pre>
 * <p>
 * <strong><em>Use with caution.</em></strong> Deallocating a buffer inappropriately
 * can crash the JVM, or worse.
 */
public class DirectBufferDealloc
{
    private static final Logger logger = getLogger( DirectBufferDealloc.class );


    protected static interface Impl
    {
        void deallocate( Buffer buffer ) throws Exception;
    }


    protected static class Impl0 implements Impl
    {
        @Override
        public void deallocate( Buffer buffer ) throws Exception
        {
            logger.warning( "Ignoring request to deallocate a DirectBuffer" );
        }

        @Override
        public String toString( )
        {
            return "DirectBufferDealloc impl NOOP stub for unsupported JVMs";
        }
    }


    protected static class Impl1 implements Impl
    {
        private final Method getCleanerMethod;
        private final Method getAttachmentMethod;

        private final Class<?> cleanerClass;
        private final Method doCleanMethod;

        public Impl1( ) throws Exception
        {
            Class<?> directBufferClass = Class.forName( "sun.nio.ch.DirectBuffer" );
            this.getCleanerMethod = directBufferClass.getMethod( "cleaner" );
            this.getAttachmentMethod = directBufferClass.getMethod( "attachment" );

            this.cleanerClass = findClass( "jdk.internal.ref.Cleaner", "sun.misc.Cleaner" );
            this.doCleanMethod = cleanerClass.getMethod( "clean" );
        }

        @Override
        public void deallocate( Buffer buffer ) throws Exception
        {
            if ( buffer.isDirect( ) )
            {
                Object cleaner = this.getCleanerMethod.invoke( buffer );
                Object attachment = this.getAttachmentMethod.invoke( buffer );

                if ( this.cleanerClass.isInstance( cleaner ) )
                {
                    this.doCleanMethod.invoke( cleaner );
                }

                if ( attachment instanceof Buffer )
                {
                    this.deallocate( ( Buffer ) attachment );
                }
            }
        }

        @Override
        public String toString( )
        {
            return "DirectBufferDealloc impl for Oracle/OpenJDK 8 JVMs, and OpenJDK 9+ JVMs (JVM args required -- see javadocs)";
        }
    }


    protected static final Impl impl = chooseImpl( Impl1::new );

    @SafeVarargs
    protected static Impl chooseImpl( ThrowingSupplier<? extends Impl>... suppliers )
    {
        FloatBuffer testBuffer = ByteBuffer.allocateDirect( 8 ).asFloatBuffer( );
        for ( ThrowingSupplier<? extends Impl> supplier : suppliers )
        {
            try
            {
                Impl impl = supplier.get( );
                impl.deallocate( testBuffer );
                return impl;
            }
            catch ( Exception e )
            { }
        }

        logger.severe( "DirectBuffer dealloc is not supported on this JVM -- deallocation requests will be logged but otherwise ignored" );
        return new Impl0( );
    }


    public static void deallocateDirectBuffer( Buffer directBuffer )
    {
        try
        {
            impl.deallocate( directBuffer );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void deallocateDirectBuffers( Buffer... directBuffers )
    {
        if ( directBuffers != null )
        {
            for ( Buffer b : directBuffers )
            {
                deallocateDirectBuffer( b );
            }
        }
    }

    public static void deallocateDirectBuffers( Collection<? extends Buffer> directBuffers )
    {
        if ( directBuffers != null )
        {
            for ( Buffer b : directBuffers )
            {
                deallocateDirectBuffer( b );
            }
        }
    }

}
