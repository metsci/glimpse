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
package com.metsci.glimpse.util;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Float.floatToIntBits;
import static java.lang.Math.multiplyExact;
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
import java.util.Set;
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

    // An epsilon value for comparing when double values are approximately equal
    // NOTE: In general the correct epsilon to use when defining a sense of floating point
    //       equality is quite application specific. This epsilon is chosen to be appropriate
    //       for comparing floating point values close to 0.
    public static final double EPSILON = 1e-10;

    /**
     * Prevent instantiation.
     */
    private GeneralUtils( )
    {
    }

    /**
     * Convenience function for tersely re-throwing a checked exception wrapped in a {@link RuntimeException}.
     */
    public static <T> T require( ThrowingSupplier<T> fn )
    {
        try
        {
            return fn.get( );
        }
        catch ( RuntimeException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
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
     * Return true iff the set contains any of the specified values.
     */
    @SafeVarargs
    public static <T> boolean containsAny( Set<? super T> set, T... values )
    {
        for ( T value : values )
        {
            if ( set.contains( value ) )
            {
                return true;
            }
        }
        return false;
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

    /**
     * Is the given integer within epsilon (1e-10) of an integer.
     */
    public static boolean approximateInteger( double d1 )
    {
        return approximateInteger( d1, EPSILON );
    }

    /**
     * Is the given integer within epsilon of an integer.
     */
    public static boolean approximateInteger( double d1, double epsilon )
    {
        // d1 % 1 returns the fractional part of d1, which should be near 0 or 1 if d1 is an integer
        double frac = Math.abs( d1 % 1 );
        return approximateEqual( frac, 0, epsilon ) || approximateEqual( frac, 1, epsilon );
    }

    /**
     * Returns true if two doubles are within a given epsilon (1e-10) of each other.
     */
    public static boolean approximateEqual( double d1, double d2 )
    {
        return approximateEqual( d1, d2, EPSILON );
    }

    /**
     * Returns true if two doubles are within a given epsilon of each other.
     */
    public static boolean approximateEqual( double d1, double d2, double epsilon )
    {
        return Math.abs( d1 - d2 ) < epsilon;
    }

    /**
     * Returns true if two doubles are further than a given epsilon (1e-10) of each other.
     */
    public static boolean approximateNotEqual( double d1, double d2 )
    {
        return approximateNotEqual( d1, d2, EPSILON );
    }

    /**
     * Returns true if two doubles are further than a given epsilon of each other.
     */
    public static boolean approximateNotEqual( double d1, double d2, double epsilon )
    {
        return Math.abs( d1 - d2 ) > epsilon;
    }

    /**
     * Multiplies int factors and returns the int product, or throws {@link ArithmeticException}
     * if there is an overflow (either positive or negative).
     */
    public static int multiplyInts( int... factors )
    {
        try
        {
            int r = 1;
            for ( int factor : factors )
            {
                r = multiplyExact( r, factor );
            }
            return r;
        }
        catch ( ArithmeticException e )
        {
            StringBuilder s = new StringBuilder( );
            for ( int factor : factors )
            {
                s.append( factor ).append( " * " );
            }
            s.setLength( max( 0, s.length( ) - 3 ) );
            throw new ArithmeticException( "Integer overflow while multiplying factors:" + s );
        }
    }

    /**
     * See {@link Double#equals(Object)}.
     */
    public static boolean doublesEqual( double d1, double d2 )
    {
        return ( doubleToLongBits( d1 ) == doubleToLongBits( d2 ) );
    }

    /**
     * See {@link Float#equals(Object)}.
     */
    public static boolean floatsEqual( float f1, float f2 )
    {
        return ( floatToIntBits( f1 ) == floatToIntBits( f2 ) );
    }

    /**
     * Returns the first argument (value) unless it is ouside the range [min,max]. In that
     * case, min is returned if value is less than min and max is returned if value is greater
     * than max.
     */
    public static double clamp( double value, double min, double max )
    {
        if ( value < min )
            return min;
        else if ( value > max )
            return max;
        else
            return value;
    }

    /**
     * @see #clamp(double, double, double)
     */
    public static int clamp( int value, int min, int max )
    {
        if ( value < min )
            return min;
        else if ( value > max )
            return max;
        else
            return value;
    }

    /**
     * @see #clamp(double, double, double)
     */
    public static float clamp( float value, float min, float max )
    {
        if ( value < min )
            return min;
        else if ( value > max )
            return max;
        else
            return value;
    }

    /**
     * @see #clamp(double, double, double)
     */
    public static long clamp( long value, long min, long max )
    {
        if ( value < min )
            return min;
        else if ( value > max )
            return max;
        else
            return value;
    }

    /**
     * @see #clamp(double, double, double)
     */
    public static short clamp( short value, short min, short max )
    {
        if ( value < min )
            return min;
        else if ( value > max )
            return max;
        else
            return value;
    }

    /**
     * @see #clamp(double, double, double)
     */
    public static byte clamp( byte value, byte min, byte max )
    {
        if ( value < min )
            return min;
        else if ( value > max )
            return max;
        else
            return value;
    }

    /**
     * Returns the maximum value from among a set of inputs.
     */
    public static int max( int... values )
    {
        if ( values.length == 0 ) throw new IllegalArgumentException( "Unable to compute max with no arguments" );

        int max = Integer.MIN_VALUE;

        for ( int value : values )
        {
            if ( value > max ) max = value;
        }

        return max;
    }

    /**
     * Returns the maximum value from among three inputs. Should be preferred
     * over {@link #max(int...)} for efficiency with only three arguments.
     */
    public static int max( int v1, int v2, int v3 )
    {
        return Math.max( Math.max( v1, v2 ), v3 );
    }

    /**
     * @see #max(int...)
     */
    public static long max( long... values )
    {
        if ( values.length == 0 ) throw new IllegalArgumentException( "Unable to compute max with no arguments" );

        long max = Long.MIN_VALUE;

        for ( long value : values )
        {
            if ( value > max ) max = value;
        }

        return max;
    }

    /**
     * @see #max(int,int,int)
     */
    public static long max( long v1, long v2, long v3 )
    {
        return Math.max( Math.max( v1, v2 ), v3 );
    }
    
    /**
     * @see #max(int...)
     */
    public static double max( double... values )
    {
        if ( values.length == 0 ) throw new IllegalArgumentException( "Unable to compute max with no arguments" );

        double max = Double.NEGATIVE_INFINITY;

        for ( double value : values )
        {
            if ( value > max ) max = value;
        }

        return max;
    }

    /**
     * @see #max(int,int,int)
     */
    public static double max( double v1, double v2, double v3 )
    {
        return Math.max( Math.max( v1, v2 ), v3 );
    }
    
    /**
     * @see #max(int...)
     */
    public static float max( float... values )
    {
        if ( values.length == 0 ) throw new IllegalArgumentException( "Unable to compute max with no arguments" );

        float max = Float.NEGATIVE_INFINITY;

        for ( float value : values )
        {
            if ( value > max ) max = value;
        }

        return max;
    }

    /**
     * @see #max(int,int,int)
     */
    public static float max( float v1, float v2, float v3 )
    {
        return Math.max( Math.max( v1, v2 ), v3 );
    }
    
    /**
     * Returns the minimum value from among a set of inputs.
     */
    public static int min( int... values )
    {
        if ( values.length == 0 ) throw new IllegalArgumentException( "Unable to compute min with no arguments" );

        int min = Integer.MAX_VALUE;

        for ( int value : values )
        {
            if ( value < min ) min = value;
        }

        return min;
    }

    /**
     * Returns the minimum value from among three inputs. Should be preferred
     * over {@link #min(int...)} for efficiency with only three arguments.
     */
    public static int min( int v1, int v2, int v3 )
    {
        return Math.min( Math.min( v1, v2 ), v3 );
    }

    /**
     * @see #min(int...)
     */
    public static long min( long... values )
    {
        if ( values.length == 0 ) throw new IllegalArgumentException( "Unable to compute min with no arguments" );

        long min = Integer.MAX_VALUE;

        for ( long value : values )
        {
            if ( value < min ) min = value;
        }

        return min;
    }


    /**
     * @see #max(int,int,int)
     */
    public static long min( long v1, long v2, long v3 )
    {
        return Math.min( Math.min( v1, v2 ), v3 );
    }
    
    /**
     * @see #min(int...)
     */
    public static double min( double... values )
    {
        if ( values.length == 0 ) throw new IllegalArgumentException( "Unable to compute min with no arguments" );

        double min = Double.POSITIVE_INFINITY;

        for ( double value : values )
        {
            if ( value < min ) min = value;
        }

        return min;
    }


    /**
     * @see #max(int,int,int)
     */
    public static double min( double v1, double v2, double v3 )
    {
        return Math.min( Math.min( v1, v2 ), v3 );
    }
    
    /**
     * @see #min(int...)
     */
    public static float min( float... values )
    {
        if ( values.length == 0 ) throw new IllegalArgumentException( "Unable to compute min with no arguments" );

        float min = Float.POSITIVE_INFINITY;

        for ( float value : values )
        {
            if ( value < min ) min = value;
        }

        return min;
    }


    /**
     * @see #max(int,int,int)
     */
    public static float min( float v1, float v2, float v3 )
    {
        return Math.min( Math.min( v1, v2 ), v3 );
    }
}
