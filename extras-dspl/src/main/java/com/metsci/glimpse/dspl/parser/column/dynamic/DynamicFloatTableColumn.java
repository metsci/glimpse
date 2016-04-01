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
package com.metsci.glimpse.dspl.parser.column.dynamic;

import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Table.Column;
import com.metsci.glimpse.dspl.util.DataSetFactory;
import com.metsci.glimpse.util.primitives.FloatsArray;

public class DynamicFloatTableColumn extends DynamicTableColumn
{
    protected FloatsArray data;

    public DynamicFloatTableColumn( Concept concept )
    {
        this( DataSetFactory.newColumn( concept.getId( ), DataType.FLOAT ), concept );
    }

    public DynamicFloatTableColumn( Column column, Concept concept )
    {
        super( column, concept, DataType.FLOAT, 0 );

        this.data = new FloatsArray( 10 );
    }

    public DynamicFloatTableColumn( Concept concept, float[] initialArray )
    {
        this( DataSetFactory.newColumn( concept.getId( ), DataType.FLOAT ), concept, initialArray );
    }

    public DynamicFloatTableColumn( Column column, Concept concept, float[] initialArray )
    {
        super( column, concept, DataType.FLOAT, initialArray.length );

        this.data = new FloatsArray( initialArray );
    }

    @Override
    public boolean isConstant( )
    {
        return false;
    }

    @Override
    public int getSize( )
    {
        return size;
    }

    @Override
    public float[] getFloatData( )
    {
        float[] returnArray = new float[size];
        System.arraycopy( data.a, 0, returnArray, 0, size );
        return returnArray;
    }

    @Override
    public float getFloatData( int i )
    {
        return data.a[i];
    }

    @Override
    public void set( int i, float value )
    {
        data.a[i] = value;
    }

    @Override
    public void insert( float value )
    {
        if ( size >= data.n ) data.prepForAppend( ( int ) ( data.n * 0.5 ) );

        data.a[size++] = value;
    }
}
