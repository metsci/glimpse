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

import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Table.Column;

public abstract class AbstractTableColumn implements TableColumn
{
    protected Column column;
    protected Concept concept;
    protected DataType type;
    protected int size;

    public AbstractTableColumn( Column column, Concept concept, DataType type, int size )
    {
        this.concept = concept;
        this.column = column;
        this.type = type;
        this.size = size;
    }

    @Override
    public Column getColumn( )
    {
        return column;
    }

    @Override
    public Concept getConcept( )
    {
        return concept;
    }

    @Override
    public DataType getType( )
    {
        return type;
    }

    @Override
    public int getSize( )
    {
        return size;
    }

    @Override
    public String[] getStringData( )
    {
        throw new UnsupportedOperationException( "String DataType is not supported by this TableColumn." );
    }

    @Override
    public int[] getIntegerData( )
    {
        throw new UnsupportedOperationException( "Integer DataType is not supported by this TableColumn." );
    }

    @Override
    public float[] getFloatData( )
    {
        throw new UnsupportedOperationException( "Float DataType is not supported by this TableColumn." );
    }

    @Override
    public boolean[] getBooleanData( )
    {
        throw new UnsupportedOperationException( "Boolean DataType is not supported by this TableColumn." );
    }

    @Override
    public long[] getDateData( )
    {
        throw new UnsupportedOperationException( "Date DataType is not supported by this TableColumn." );
    }

    @Override
    public String getStringData( int i )
    {
        throw new UnsupportedOperationException( "String DataType is not supported by this TableColumn." );
    }

    @Override
    public int getIntegerData( int i )
    {
        throw new UnsupportedOperationException( "Integer DataType is not supported by this TableColumn." );
    }

    @Override
    public float getFloatData( int i )
    {
        throw new UnsupportedOperationException( "Float DataType is not supported by this TableColumn." );
    }

    @Override
    public boolean getBooleanData( int i )
    {
        throw new UnsupportedOperationException( "Boolean DataType is not supported by this TableColumn." );
    }

    @Override
    public long getDateData( int i )
    {
        throw new UnsupportedOperationException( "Date DataType is not supported by this TableColumn." );
    }

}
