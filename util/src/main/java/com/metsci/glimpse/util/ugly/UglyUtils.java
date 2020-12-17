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
