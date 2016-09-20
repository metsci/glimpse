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
package com.metsci.glimpse.util;

import static java.util.Collections.unmodifiableList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class holds miscellaneous static methods that are simple but broadly
 * useful. In particular, workarounds for Java defects can go here.
 *
 * @author hogye
 */
public class GeneralUtils
{
    public static final String LINE_SEPARATOR = System.getProperty( "line.separator", "\n" );

    /**
     * Prevent instantiation.
     */
    private GeneralUtils( )
    {
    }

    /**
     * Workaround for <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6480539">bug #6480539</a>:
     * BigDecimal.stripTrailingZeros() has no effect on zero itself ("0.0").
     */
    public static BigDecimal stripTrailingZeros( BigDecimal value )
    {
        return ( value.compareTo( BigDecimal.ZERO ) == 0 ? BigDecimal.ZERO : value.stripTrailingZeros( ) );
    }

    /**
     * Get standard Java hashCode for a long without the extra object creation.
     *
     * <p>Equivalent to (new Long(longVal)).hashCode() in jdk1.6.</p>
     *
     * @param   longVal  value for which to compute hashCode
     * @return  hashCode
     */
    public static int hashCode( long longVal )
    {
        // combine upper 32 and lower 32 bits via exclusive or
        return ( int ) ( longVal ^ ( longVal >>> 32 ) );
    }

    /**
     * Get standard Java hashCode for a float without the extra object creation.
     *
     * <p>Equivalent to (new Float(floatVal)).hashCode() in jdk1.6.</p>
     *
     * @param   floatVal  value for which to compute hashCode
     * @return  hashCode
     */
    public static int hashCode( float floatVal )
    {
        return Float.floatToIntBits( floatVal );
    }

    /**
     * Get standard Java hashCode for a float without the extra object creation.
     *
     * <p>Equivalent to (new Double(doubleVal)).hashCode() in jdk1.6.</p>
     *
     * @param   doubleVal  value for which to compute hashCode
     * @return  hashCode
     */
    public static int hashCode( double doubleVal )
    {
        return hashCode( Double.doubleToLongBits( doubleVal ) );
    }

    /**
     * Get standard Java hashCode for a boolean without the extra object creation.
     *
     * <p>Equivalent to (new Boolean(booleanVal)).hashCode() in jdk1.6.</p>
     *
     * @param   booleanVal  value for which to compute hashCode
     * @return  hashCode
     */
    public static int hashCode( boolean booleanVal )
    {
        return ( booleanVal ? 1231 : 1237 );
    }

    /**
     * Compare two shorts, according to the standard Comparable interface.  Since 1.4, the JDK provides
     * Double.compare( double, double ) and Float.compare( float, float ) but not variants for
     * Integer, Long, and Short.
     *
     * @param   shortVal1
     * @param   shortVal2
     * @return  -1, 0, or 1 if shortVal1 is less than, equal to, or greater than shortVal2 respectively.
     */
    public static short compare( short shortVal1, short shortVal2 )
    {
        return compareShorts( shortVal1, shortVal2 );
    }

    /**
     * Compare two shorts, according to the standard Comparable interface.  Since 1.4, the JDK provides
     * Double.compare( double, double ) and Float.compare( float, float ) but not variants for
     * Integer, Long, and Short.
     *
     * @param   shortVal1
     * @param   shortVal2
     * @return  -1, 0, or 1 if shortVal1 is less than, equal to, or greater than shortVal2 respectively.
     */
    public static short compareShorts( short shortVal1, short shortVal2 )
    {
        if ( shortVal1 < shortVal2 )
        {
            return -1;
        }
        else if ( shortVal1 > shortVal2 )
        {
            return 1;
        }

        return 0;
    }

    /**
     * Compare two ints, according to the standard Comparable interface.  Since 1.4, the JDK provides
     * Double.compare( double, double ) and Float.compare( float, float ) but not variants for
     * Integer, Long, and Short.
     *
     * @param   intVal1
     * @param   intVal2
     * @return  -1, 0, or 1 if intVal1 is less than, equal to, or greater than intVal2 respectively.
     */
    public static int compare( int intVal1, int intVal2 )
    {
        return compareInts( intVal1, intVal2 );
    }

    /**
     * Compare two ints, according to the standard Comparable interface.  Since 1.4, the JDK provides
     * Double.compare( double, double ) and Float.compare( float, float ) but not variants for
     * Integer, Long, and Short.
     *
     * @param   intVal1
     * @param   intVal2
     * @return  -1, 0, or 1 if intVal1 is less than, equal to, or greater than intVal2 respectively.
     */
    public static int compareInts( int intVal1, int intVal2 )
    {
        if ( intVal1 < intVal2 )
        {
            return -1;
        }
        else if ( intVal1 > intVal2 )
        {
            return 1;
        }

        return 0;
    }

    /**
     * Compare two longs, according to the standard Comparable interface.  Since 1.4, the JDK provides
     * Double.compare( double, double ) and Float.compare( float, float ) but not variants for
     * Integer, Long, and Short.
     *
     * @param   longVal1
     * @param   longVal2
     * @return  -1, 0, or 1 if longVal1 is less than, equal to, or greater than longVal2
     *          respectively.
     */
    public static int compare( long longVal1, long longVal2 )
    {
        return compareLongs( longVal1, longVal2 );
    }

    /**
     * Compare two longs, according to the standard Comparable interface.  Since 1.4, the JDK provides
     * Double.compare( double, double ) and Float.compare( float, float ) but not variants for
     * Integer, Long, and Short.
     *
     * @param   longVal1
     * @param   longVal2
     * @return  -1, 0, or 1 if longVal1 is less than, equal to, or greater than longVal2
     *          respectively.
     */
    public static int compareLongs( long longVal1, long longVal2 )
    {
        if ( longVal1 < longVal2 )
        {
            return -1;
        }
        else if ( longVal1 > longVal2 )
        {
            return 1;
        }

        return 0;
    }

    /**
     * This method returns the correct type as specified by the caller, unlike {@link Class#forName(String)}.
     * It also helps localizing the "Type safety" warnings to a single place.
     */
    public static <T> Class<? extends T> classForName( String className ) throws ClassNotFoundException
    {
        return GeneralUtils.cast( Class.forName( className ) );
    }

    /**
     * Returns the directory in which the application was started (this is the
     * working/current directory).
     */
    public static String getWorkingDir( )
    {
        return System.getProperty( "user.dir" );
    }

    /**
     * Type casts from one type to another. Provided mostly to reduce the number
     * of "type safety" warnings.<br><br>
     *
     * NOTE: When using the Sun Java compiler, this method should be invoked as GeneralUtils.< T, U >cast(u)
     * to avoid the following <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6302954">bug</a>.
     */
    @SuppressWarnings( "unchecked" )
    public static <T, U> T cast( U u )
    {
        return ( T ) u;
    }

    /**
     * Creates a new {@link HashSet} by examining the expected return type.
     */
    public static final <K> HashSet<K> newHashSet( )
    {
        return new HashSet<K>( );
    }

    /**
     * Creates a new {@link HashSet} containing the elements of the specified
     * collection.
     *
     * @see {@link HashSet#HashSet(Collection)}
     */
    public static final <K> HashSet<K> newHashSet( Collection<? extends K> c )
    {
        return new HashSet<K>( c );
    }

    /**
     * Creates a new {@link LinkedHashSet} by examining the expected return type.
     */
    public static final <K> LinkedHashSet<K> newLinkedHashSet( )
    {
        return new LinkedHashSet<K>( );
    }

    /**
     * Creates a new {@link LinkedHashSet} containing the elements of the specified
     * collection.
     *
     * @see {@link LinkedHashSet#LinkedHashSet(Collection)}
     */
    public static final <K> LinkedHashSet<K> newLinkedHashSet( Collection<? extends K> c )
    {
        return new LinkedHashSet<K>( c );
    }

    /**
     * Creates a new {@link TreeSet} by examining the expected return type.
     */
    public static final <K> TreeSet<K> newTreeSet( )
    {
        return new TreeSet<K>( );
    }

    /**
     * Creates a new {@link HashMap} by examining the expected return type.
     */
    public static final <K, V> HashMap<K, V> newHashMap( )
    {
        return new HashMap<K, V>( );
    }

    /**
     * Creates a new {@link HashMap} containing the same mappings as the
     * specified map.
     *
     * @see {@link HashMap#HashMap(Map)}
     */
    public static final <K, V> HashMap<K, V> newHashMap( Map<? extends K, ? extends V> m )
    {
        return new HashMap<K, V>( m );
    }

    /**
     * Creates a new {@link LinkedHashMap} by examining the expected return type.
     */
    public static final <K, V> LinkedHashMap<K, V> newLinkedHashMap( )
    {
        return new LinkedHashMap<K, V>( );
    }

    /**
     * Creates a new {@link TreeMap} by examining the expected return type.
     */
    public static final <K, V> TreeMap<K, V> newTreeMap( )
    {
        return new TreeMap<K, V>( );
    }

    /**
     * Creates a new {@link ArrayList} by examining the expected return type.
     */
    public static final <K> ArrayList<K> newArrayList( )
    {
        return new ArrayList<K>( );
    }

    /**
     * Creates a new {@link ArrayList} containing the elements of the specified
     * collection, in order.
     *
     * @see {@link ArrayList#ArrayList(Collection)}.
     */
    public static final <K> ArrayList<K> newArrayList( Collection<? extends K> c )
    {
        return new ArrayList<K>( c );
    }

    /**
     * Creates a new unmodifiable {@link List} containing the elements of the
     * specified collection, in order.
     */
    public static final <K> List<K> newUnmodifiableList( Collection<? extends K> c )
    {
        return unmodifiableList( newArrayList( c ) );
    }

    /**
     * Creates a new {@link ArrayList} from a collection by building an
     * enumeration over the collection.
     */
    public static final <K> ArrayList<K> asList( Collection<K> values )
    {
        return Collections.list( Collections.<K> enumeration( values ) );
    }

    @SuppressWarnings( "unchecked" )
    public static final <K> HashSet<K> asSet( K... values )
    {
        return new HashSet<K>( Arrays.asList( values ) );
    }

    /**
     * Terse (especially as a static import) way to create an int[] literal.
     */
    public static int[] ints( int... values )
    {
        return values;
    }

    /**
     * Terse (especially as a static import) way to create a long[] literal.
     */
    public static long[] longs( long... values )
    {
        return values;
    }

    /**
     * Terse (especially as a static import) way to create a float[] literal.
     */
    public static float[] floats( float... values )
    {
        return values;
    }

    /**
     * Terse (especially as a static import) way to create a double[] literal.
     */
    public static double[] doubles( double... values )
    {
        return values;
    }

    /**
     * Terse (especially as a static import) way to create a boolean[] literal.
     */
    public static boolean[] booleans( boolean... values )
    {
        return values;
    }

    /**
     * Terse (especially as a static import) way to create a T[] literal.
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T[] array( T... values )
    {
        return values;
    }

    /**
     * Terse (especially as a static import) way to create an EnumSet.
     */
    @SuppressWarnings( "unchecked" )
    public static <T extends Enum<T>> EnumSet<T> enumSet( T... elements )
    {
        return EnumSet.<T> of( elements[0], elements );
    }

    /**
     * Convenience method for formatting and appending data to an existing StringBuffer.
     */
    public static void stringBufferAppend( StringBuffer sb, String format, Object... args )
    {
        sb.append( String.format( format, args ) );
    }

    /**
     * Convenience method for formatting and appending data to an existing StringBuilder.
     */
    public static void stringBuilderAppend( StringBuilder sb, String format, Object... args )
    {
        sb.append( String.format( format, args ) );
    }
}
