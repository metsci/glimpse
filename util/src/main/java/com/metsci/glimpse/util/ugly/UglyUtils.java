package com.metsci.glimpse.util.ugly;

import java.lang.reflect.Method;

import com.metsci.glimpse.util.ThrowingSupplier;

/**
 * Utilities that should only be used as a last resort -- because, for example,
 * they rely on reflective access to implementation details.
 * <p>
 * <strong>USE AT YOUR OWN RISK.</strong>
 * <p>
 * It is usually preferable to keep such utilities private. However, some of them
 * are needed over and over; better to provide public impls of those than to have
 * multiple duplicated private impls.
 */
public class UglyUtils
{

    /**
     * Iterate over {@code classnames} calling {@link Class#forName(String)}, and
     * immediately return the first class that is successfully found. If no class
     * is found for any of the names, throw a {@link ClassNotFoundException}.
     * <p>
     * Useful for getting reflective access to a class that has been moved or renamed
     * (e.g. {@code jdk.internal.ref.Cleaner}, formerly known as {@code sun.misc.Cleaner}).
     */
    public static Class<?> findClass( String... classnames ) throws ClassNotFoundException
    {
        for ( String classname : classnames )
        {
            try
            {
                return Class.forName( classname );
            }
            catch ( ClassNotFoundException e )
            { }
        }
        throw new ClassNotFoundException( );
    }

    public static Method requireDeclaredMethod( Class<?> clazz, String methodName, Class<?>... paramTypes ) throws Exception
    {
        Method method = clazz.getDeclaredMethod( methodName, paramTypes );
        method.setAccessible( true );
        return method;
    }

    @SafeVarargs
    public static <T> T firstSuccessfulReturnValue( ThrowingSupplier<? extends T>... suppliers )
    {
        for ( ThrowingSupplier<? extends T> supplier : suppliers )
        {
            try
            {
                return supplier.get( );
            }
            catch ( Exception e )
            { }
        }

        throw new RuntimeException( "No supplier successfully returned a value" );
    }

}
