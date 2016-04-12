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
package com.metsci.glimpse.util.primitives.algorithms;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.metsci.glimpse.util.primitives.IntsArray;

/**
 * @author osborn
 */
public class Partitioning
{
    private Partitioning( )
    {
    }

    /////////////
    /////////////
    /////////////

    public static Map<Strings, IntsArray> partitionUnique( String[]... data )
    {
        Map<Strings, IntsArray> unique = new LinkedHashMap<Strings, IntsArray>( );

        int nfields = data.length;
        int size = data[0].length;

        Strings s = new Strings( nfields );
        for ( int i = 0; i < size; i++ )
        {
            for ( int j = 0; j < nfields; j++ )
                s.strings[j] = data[j][i];

            IntsArray index = unique.get( s );
            if ( index == null )
            {
                index = new IntsArray( );
                unique.put( s.copy( ), index );
            }

            index.append( i );
        }

        return unique;
    }

    public static class Strings
    {
        public final int n;
        public final String[] strings;

        public Strings( int n )
        {
            this.n = n;
            this.strings = new String[n];
        }

        public Strings copy( )
        {
            Strings s = new Strings( n );
            for ( int i = 0; i < n; i++ )
                s.strings[i] = this.strings[i];

            return s;
        }

        @Override
        public int hashCode( )
        {
            return Arrays.hashCode( strings );
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            Strings other = ( Strings ) obj;

            return Arrays.equals( strings, other.strings );
        }
    }

    /////////////
    /////////////
    /////////////

    public static Map<String, IntsArray> partitionUnique( String[] data )
    {
        Map<String, IntsArray> unique = new LinkedHashMap<String, IntsArray>( );

        for ( int i = 0; i < data.length; i++ )
        {
            String s = data[i];
            if ( s != null )
            {
                IntsArray index = unique.get( s );
                if ( index == null )
                {
                    index = new IntsArray( );
                    unique.put( s, index );
                }

                index.append( i );
            }
        }

        return unique;
    }

    /////////////
    /////////////
    /////////////

    public static long[] extract( long[] src, int[] index, int len )
    {
        long[] result = new long[index.length];
        for ( int i = 0; i < len; i++ )
            result[i] = src[index[i]];

        return result;
    }

    public static float[] extract( float[] src, int[] index, int len )
    {
        float[] result = new float[index.length];
        for ( int i = 0; i < len; i++ )
            result[i] = src[index[i]];

        return result;
    }

    public static double[] extract( double[] src, int[] index, int len )
    {
        double[] result = new double[index.length];
        for ( int i = 0; i < len; i++ )
            result[i] = src[index[i]];

        return result;
    }

    public static int[] extract( int[] src, int[] index, int len )
    {
        int[] result = new int[index.length];
        for ( int i = 0; i < len; i++ )
            result[i] = src[index[i]];

        return result;
    }

    public static char[] extract( char[] src, int[] index, int len )
    {
        char[] result = new char[index.length];
        for ( int i = 0; i < len; i++ )
            result[i] = src[index[i]];

        return result;
    }

    public static boolean[] extract( boolean[] src, int[] index, int len )
    {
        boolean[] result = new boolean[index.length];
        for ( int i = 0; i < len; i++ )
            result[i] = src[index[i]];

        return result;
    }

    public static Object[] extract( Object[] src, int[] index, int len )
    {
        Object[] result = new Object[index.length];
        for ( int i = 0; i < len; i++ )
            result[i] = src[index[i]];

        return result;
    }

    public static String[] extract( String[] src, int[] index, int len )
    {
        String[] result = new String[index.length];
        for ( int i = 0; i < len; i++ )
            result[i] = src[index[i]];

        return result;
    }

    /////////////
    /////////////
    /////////////

    public static void shuffle( double[] src, double[] dst, int[] index )
    {
        for ( int i = 0; i < index.length; i++ )
            dst[i] = src[index[i]];
    }

    public static void shuffle( float[] src, float[] dst, int[] index )
    {
        for ( int i = 0; i < index.length; i++ )
            dst[i] = src[index[i]];
    }

    public static void shuffle( long[] src, long[] dst, int[] index )
    {
        for ( int i = 0; i < index.length; i++ )
            dst[i] = src[index[i]];
    }

    public static void shuffle( int[] src, int[] dst, int[] index )
    {
        for ( int i = 0; i < index.length; i++ )
            dst[i] = src[index[i]];
    }

    public static void shuffle( char[] src, char[] dst, int[] index )
    {
        for ( int i = 0; i < index.length; i++ )
            dst[i] = src[index[i]];
    }

    public static void shuffle( Object[] src, Object[] dst, int[] index )
    {
        for ( int i = 0; i < index.length; i++ )
            dst[i] = src[index[i]];
    }

    public static void shuffle( String[] src, String[] dst, int[] index )
    {
        for ( int i = 0; i < index.length; i++ )
            dst[i] = src[index[i]];
    }

}
