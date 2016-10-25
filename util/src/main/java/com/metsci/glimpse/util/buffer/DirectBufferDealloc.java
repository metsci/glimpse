package com.metsci.glimpse.util.buffer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.util.Collection;

/**
 * Uses non-public APIs (e.g. via reflection) to free the off-heap memory that
 * backs an NIO direct buffer. Normally such off-heap memory is not freed until
 * the garbage collector finalizes the direct buffer. This delay causes problems
 * when a lot of off-heap memory is used, but garbage collections are infrequent.
 * <p>
 * Currently only works for Oracle and OpenJDK, but could probably be extended to
 * others.
 * <p>
 * <strong><em>Use with caution.</em></strong> Deallocating a buffer inappropriately
 * can crash the JVM, or worse.
 */
public class DirectBufferDealloc
{

    protected static final Class<?> directBufferClass;
    protected static final Method getCleanerMethod;
    protected static final Method getAttachmentMethod;

    protected static final Class<?> cleanerClass;
    protected static final Method doCleanMethod;

    static
    {
        try
        {
            directBufferClass = Class.forName( "sun.nio.ch.DirectBuffer" );
            getCleanerMethod = directBufferClass.getMethod( "cleaner" );
            getAttachmentMethod = directBufferClass.getMethod( "attachment" );

            cleanerClass = Class.forName( "sun.misc.Cleaner" );
            doCleanMethod = cleanerClass.getMethod( "clean" );
        }
        catch ( ClassNotFoundException | NoSuchMethodException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void deallocateDirectBuffers( Collection<? extends Buffer> directBuffers )
    {
        if ( directBuffers != null )
        {
            for ( Buffer b : directBuffers )
            {
                deallocateDirectBuffer0( b );
            }
        }
    }

    public static void deallocateDirectBuffers( Buffer... directBuffers )
    {
        if ( directBuffers != null )
        {
            for ( Buffer b : directBuffers )
            {
                deallocateDirectBuffer0( b );
            }
        }
    }

    public static void deallocateDirectBuffer0( Object directBuffer )
    {
        if ( directBufferClass.isInstance( directBuffer ) )
        {
            try
            {
                Object cleaner = getCleanerMethod.invoke( directBuffer );
                if ( cleanerClass.isInstance( cleaner ) )
                {
                    doCleanMethod.invoke( cleaner );
                }

                Object attachment = getAttachmentMethod.invoke( directBuffer );
                deallocateDirectBuffer0( attachment );
            }
            catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
            {
                throw new RuntimeException( e );
            }
        }
    }

}
