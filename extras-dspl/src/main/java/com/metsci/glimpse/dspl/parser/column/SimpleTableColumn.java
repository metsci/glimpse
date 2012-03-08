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
package com.metsci.glimpse.dspl.parser.column;

import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Table.Column;

/**
 * @author ulman
 *
 */
public class SimpleTableColumn extends AbstractTableColumn
{
    protected Object data;

    public SimpleTableColumn( Column column, Concept concept, DataType type, Object data, int size )
    {
        super( column, concept, type, size );

        this.data = data;
    }

    @Override
    public boolean isConstant( )
    {
        return false;
    }

    @Override
    public String[] getStringData( )
    {
        return ( String[] ) data;
    }

    @Override
    public int[] getIntegerData( )
    {
        return ( int[] ) data;
    }

    @Override
    public float[] getFloatData( )
    {
        return ( float[] ) data;
    }

    @Override
    public boolean[] getBooleanData( )
    {
        return ( boolean[] ) data;
    }

    @Override
    public long[] getDateData( )
    {
        return ( long[] ) data;
    }

    @Override
    public String getStringData( int i )
    {
        return getStringData( )[i];
    }

    @Override
    public int getIntegerData( int i )
    {
        return getIntegerData( )[i];
    }

    @Override
    public float getFloatData( int i )
    {
        return getFloatData( )[i];
    }

    @Override
    public boolean getBooleanData( int i )
    {
        return getBooleanData( )[i];
    }

    @Override
    public long getDateData( int i )
    {
        return getDateData( )[i];
    }

    @Override
    public int hashCode( )
    {
        return 31 + ( ( column.getId( ) == null ) ? 0 : column.getId( ).hashCode( ) );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        SimpleTableColumn other = ( SimpleTableColumn ) obj;
        if ( column.getId( ) == null ) return other.column.getId( ) == null;
        return column.getId( ).equals( other.column.getId( ) );
    }
}
