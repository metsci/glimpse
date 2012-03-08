/*
 * Copyright (c) 2012, Metron, Inc.
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

import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Table.Column;
import com.metsci.glimpse.dspl.util.DataSetFactory;

public class DynamicStringTableColumn extends DynamicTableColumn
{
    protected List<String> data;

    public DynamicStringTableColumn( Concept concept )
    {
        this( DataSetFactory.newColumn( concept.getId( ), DataType.STRING ), concept );
    }

    public DynamicStringTableColumn( Column column, Concept concept )
    {
        super( column, concept, DataType.STRING, 0 );

        this.data = new ArrayList<String>( 10 );
    }

    public DynamicStringTableColumn( Concept concept, String[] initialArray )
    {
        this( DataSetFactory.newColumn( concept.getId( ), DataType.STRING ), concept, initialArray );
    }

    public DynamicStringTableColumn( Column column, Concept concept, String[] initialArray )
    {
        super( column, concept, DataType.STRING, initialArray.length );

        this.data = new ArrayList<String>( initialArray.length );
        for ( int i = 0; i < initialArray.length; i++ )
        {
            this.data.add( initialArray[i] );
        }
    }

    @Override
    public boolean isConstant( )
    {
        return false;
    }

    @Override
    public int getSize( )
    {
        return data.size( );
    }

    @Override
    public String[] getStringData( )
    {
        return data.toArray( new String[data.size( )] );
    }

    @Override
    public String getStringData( int i )
    {
        return data.get( i );
    }

    @Override
    public void set( int i, String value )
    {
        data.set( i, value );
    }

    @Override
    public void insert( String value )
    {
        data.add( value );
    }
}
