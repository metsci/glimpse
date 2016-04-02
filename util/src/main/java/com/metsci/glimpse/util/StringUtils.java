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

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.String.format;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author hogye
 */
public final class StringUtils
{
    public static final String degreeSymbol = "\u00b0";
    public static final String superscriptTwo = "\u00B2";
    public static final String lowercaseSigma = "\u03C3";

    public static final String whiteBox = "\u25A0";
    public static final String whiteCircle = "\u25CF";
    public static final String whiteDiamond = "\u2666";

    // Prevent instantiation
    private StringUtils( )
    {
    }

    /**
     * This method splits a String into an array of values, the values being delimited with the given character. For
     * example <tt>splitIntoArray("one~two~three", '~')</tt> returns <tt>{"one", "two", "three" }</tt>. Consecutive
     * delimiters are correctly handled, for example <tt>splitIntoArray("first~~last", '~')</tt> returns <tt>{"first",
     * "", "last" }</tt>
     *
     * @param  s          the String to be split. If s is <tt>null</tt> then the returned array is also <tt>null</tt>.
     * @param  delimiter  the delimiter character used to separate values.
     *
     * @see    String#split(String)
     */
    public static String[] split( String s, char delimiter )
    {
        if ( s == null ) return null;

        // Determine the size of the returned array.
        int arraySize = 1;
        for ( int i = 0; i < s.length( ); ++i )
            if ( s.charAt( i ) == delimiter ) ++arraySize;

        String[] values = new String[arraySize];
        int index;
        int fromIndex = 0;
        int i = 0;
        while ( ( index = s.indexOf( delimiter, fromIndex ) ) >= 0 )
        {
            values[i] = s.substring( fromIndex, index );
            ++i;
            fromIndex = index + 1;
        }

        // Don't forget the last value!
        values[i] = s.substring( fromIndex );

        return values;
    }

    /**
     * Joins strings with given separator between each pair. For instance, join(", ", "one", "two") returns the String
     * "one, two".
     */
    public static String join( String separator, String... strings )
    {
        if ( strings == null ) return null;

        if ( separator == null ) separator = "";

        StringBuilder joined = new StringBuilder( strings[0] );
        for ( int i = 1; i < strings.length; i++ )
            joined.append( separator ).append( strings[i] );

        return joined.toString( );
    }

    /**
     * Removes the filename extension from a filename, if there is one. The character that separates the filename base
     * from its extension is assumed to be the period ('.').
     *
     * <p>For example "foo.txt" gives "foo", while "foo.java.txt" gives "foo.java".</p>
     */
    public static String removeFilenameExtension( String filename )
    {
        if ( filename == null ) return null;
        int pos = filename.lastIndexOf( '.' );
        return ( pos >= 0 ) ? filename.substring( 0, pos ) : filename;
    }

    /**
     * Repeats a given string a specified number of times.
     */

    public static String repeat( String val, int rep )
    {
        StringBuilder builder = new StringBuilder( );
        for ( int i = 0; i < rep; i++ )
            builder.append( val );

        return builder.toString( );
    }

    public static String toString( int[] a )
    {
        StringBuilder s = new StringBuilder( );
        for ( int i = 0; i < a.length; i++ )
            s.append( " " + a[i] );

        s.append( GeneralUtils.LINE_SEPARATOR );

        return s.toString( );
    }

    public static String toString( long[] a )
    {
        StringBuilder s = new StringBuilder( );
        for ( int i = 0; i < a.length; i++ )
            s.append( " " + a[i] );

        s.append( GeneralUtils.LINE_SEPARATOR );

        return s.toString( );
    }

    public static String toString( double[] a )
    {
        StringBuilder s = new StringBuilder( );
        for ( int i = 0; i < a.length; i++ )
            s.append( " " + a[i] );

        s.append( GeneralUtils.LINE_SEPARATOR );

        return s.toString( );
    }

    /**
     * Get the extension of a file.
     */
    public static String getFilenameExtension( String filename )
    {
        String ext = null;
        if ( filename != null )
        {
            int i = filename.lastIndexOf( '.' );
            if ( ( i > 0 ) && ( i < ( filename.length( ) - 1 ) ) )
            {
                ext = filename.substring( i + 1 ).toLowerCase( );
            }
        }

        return ext;
    }

    /**
     * Returns the stack trace of an exception in the form of a string.
     */
    public static String getStackTraceString( Throwable t )
    {
        String answer;
        if ( t == null )
        {
            answer = null;
        }
        else
        {
            StringWriter sw = new StringWriter( );
            PrintWriter pw = new PrintWriter( sw, true );
            t.printStackTrace( pw );
            answer = sw.toString( );
            pw.close( );
        }

        return answer;
    }

    public static String decimalFormat( double d, int numDecimalPlaces )
    {
        return String.format( "%." + Integer.toString( numDecimalPlaces ) + "f", d );
    }

    public static String formatByteCount( long numBytes )
    {
        int groupSize = 1024;
        if ( numBytes < groupSize )
        {
            return ( numBytes + " B" );
        }
        else
        {
            int exp = ( int ) ( log( numBytes ) / log( groupSize ) );
            String prefix = "KMGTPE".charAt( exp - 1 ) + "B";
            return format( "%.1f %s", ( numBytes / pow( groupSize, exp ) ), prefix );
        }
    }

}
