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
package com.metsci.glimpse.dspl.parser.table;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.parser.util.ParserUtils;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.util.DsplException;

import java.util.logging.Logger;

public class SimplePropertyTableData implements PropertyTableData
{
    public static final Logger logger = Logger.getLogger( SimplePropertyTableData.class.getName( ) );

    private Concept parentConcept;
    private Map<String, TableColumn> columns;
    private int size;

    public SimplePropertyTableData( Concept parentConcept, Map<String, TableColumn> columns ) throws DsplException
    {
        this.parentConcept = parentConcept;
        this.columns = columns;
        this.size = validateColumnSizes( columns.values( ) );
    }

    protected int validateColumnSizes( Collection<TableColumn> columns ) throws DsplException
    {
        Integer size = null;
        for ( TableColumn column : columns )
        {
            if ( size == null )
                size = column.getSize( );
            else if ( size != column.getSize( ) ) throw new DsplException( "Mismatched Column sizes in SimplePropertyTableData." );
        }

        return size;
    }

    @Override
    public Collection<String> getColumnIds( )
    {
        return Collections.unmodifiableCollection( columns.keySet( ) );
    }

    @Override
    public int getNumRows( )
    {
        return size;
    }

    @Override
    public TableColumn getColumn( String ref )
    {
        return columns.get( ref );
    }

    @Override
    public TableColumn getColumn( Concept concept )
    {
        return columns.get( ParserUtils.getMappedColumn( parentConcept, concept ) );
    }
}
