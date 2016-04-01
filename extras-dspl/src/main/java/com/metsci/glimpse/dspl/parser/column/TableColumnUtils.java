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
package com.metsci.glimpse.dspl.parser.column;

public class TableColumnUtils
{
    public static TableColumn buildFilteredTableColumn( TableColumn in, int[] indexArray )
    {
        int size = indexArray.length;

        switch ( in.getType( ) )
        {
        case STRING:
            return buildTableColumn( in, buildStringArray( in, indexArray ), size );
        case FLOAT:
            return buildTableColumn( in, buildFloatArray( in, indexArray ), size );
        case INTEGER:
            return buildTableColumn( in, buildIntegerArray( in, indexArray ), size );
        case BOOLEAN:
            return buildTableColumn( in, buildBooleanArray( in, indexArray ), size );
        case DATE:
            return buildTableColumn( in, buildDateArray( in, indexArray ), size );
        case CONCEPT:
            return buildTableColumn( in, buildStringArray( in, indexArray ), size );
        default:
            throw new UnsupportedOperationException( "Unknown Type: " + in.getType( ) );
        }
    }

    public static TableColumn buildTableColumn( TableColumn tableColumn, Object data, int size )
    {
        return new SimpleTableColumn( tableColumn.getColumn( ), tableColumn.getConcept( ), tableColumn.getType( ), data, size );
    }

    public static String[] buildStringArray( TableColumn tableColumn, int[] indexArray )
    {
        int size = indexArray.length;

        String[] data = new String[size];

        for ( int i = 0; i < size; i++ )
        {
            data[i] = tableColumn.getStringData( indexArray[i] );
        }

        return data;
    }

    public static float[] buildFloatArray( TableColumn tableColumn, int[] indexArray )
    {
        int size = indexArray.length;

        float[] data = new float[size];

        for ( int i = 0; i < size; i++ )
        {
            data[i] = tableColumn.getFloatData( indexArray[i] );
        }

        return data;
    }

    public static int[] buildIntegerArray( TableColumn tableColumn, int[] indexArray )
    {
        int size = indexArray.length;

        int[] data = new int[size];

        for ( int i = 0; i < size; i++ )
        {
            data[i] = tableColumn.getIntegerData( indexArray[i] );
        }

        return data;
    }

    public static boolean[] buildBooleanArray( TableColumn tableColumn, int[] indexArray )
    {
        int size = indexArray.length;

        boolean[] data = new boolean[size];

        for ( int i = 0; i < size; i++ )
        {
            data[i] = tableColumn.getBooleanData( indexArray[i] );
        }

        return data;
    }

    public static long[] buildDateArray( TableColumn tableColumn, int[] indexArray )
    {
        int size = indexArray.length;

        long[] data = new long[size];

        for ( int i = 0; i < size; i++ )
        {
            data[i] = tableColumn.getDateData( indexArray[i] );
        }

        return data;
    }
}
