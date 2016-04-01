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

import com.metsci.glimpse.dspl.parser.column.AbstractTableColumn;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Table.Column;

public abstract class DynamicTableColumn extends AbstractTableColumn
{
    public DynamicTableColumn( Column column, Concept concept, DataType type, int size )
    {
        super( column, concept, type, size );
    }

    public void set( int i, float value )
    {
        throw new UnsupportedOperationException( "Float DataType is not supported by this TableColumn." );
    }

    public void insert( float value )
    {
        throw new UnsupportedOperationException( "Float DataType is not supported by this TableColumn." );
    }

    public void set( int i, int value )
    {
        throw new UnsupportedOperationException( "Integer DataType is not supported by this TableColumn." );
    }

    public void insert( int value )
    {
        throw new UnsupportedOperationException( "Integer DataType is not supported by this TableColumn." );
    }

    public void set( int i, boolean value )
    {
        throw new UnsupportedOperationException( "Boolean DataType is not supported by this TableColumn." );
    }

    public void insert( boolean value )
    {
        throw new UnsupportedOperationException( "Boolean DataType is not supported by this TableColumn." );
    }

    public void set( int i, long value )
    {
        throw new UnsupportedOperationException( "Date DataType is not supported by this TableColumn." );
    }

    public void insert( long value )
    {
        throw new UnsupportedOperationException( "Date DataType is not supported by this TableColumn." );
    }

    public void set( int i, String value )
    {
        throw new UnsupportedOperationException( "String DataType is not supported by this TableColumn." );
    }

    public void insert( String value )
    {
        throw new UnsupportedOperationException( "String DataType is not supported by this TableColumn." );
    }
}
