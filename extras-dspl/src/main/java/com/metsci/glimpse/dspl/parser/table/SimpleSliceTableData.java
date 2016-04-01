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
package com.metsci.glimpse.dspl.parser.table;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.parser.util.ParserUtils;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.util.DsplException;

import java.util.logging.Logger;

public class SimpleSliceTableData implements SliceTableData
{
    public static final Logger logger = Logger.getLogger( SimplePropertyTableData.class.getName( ) );

    protected Slice slice;
    protected Map<String, TableColumn> metricColumns;
    protected Map<String, TableColumn> dimensionColumns;

    protected int size;

    public SimpleSliceTableData( Slice slice, Map<String, TableColumn> dimensionColumns, Map<String, TableColumn> metricColumns ) throws DsplException
    {
        this.slice = slice;

        this.metricColumns = metricColumns;
        this.dimensionColumns = dimensionColumns;

        this.size = validateColumnSizes( dimensionColumns.values( ), metricColumns.values( ) );
    }

    protected int validateColumnSizes( Collection<TableColumn> dimensionColumns, Collection<TableColumn> metricColumns ) throws DsplException
    {
        Integer size = null;
        for ( TableColumn column : dimensionColumns )
        {
            if ( size == null )
                size = column.getSize( );
            else if ( size != column.getSize( ) ) throw new DsplException( "Mismatched Dimension Column sizes in SimplePropertyTableData." );
        }

        for ( TableColumn column : metricColumns )
        {
            if ( size == null )
                size = column.getSize( );
            else if ( size != column.getSize( ) ) throw new DsplException( "Mismatched Metric Column sizes in SimplePropertyTableData." );
        }

        return size;
    }

    @Override
    public Collection<String> getDimensionColumnIds( )
    {
        return Collections.unmodifiableCollection( dimensionColumns.keySet( ) );
    }

    @Override
    public Collection<String> getMetricColumnIds( )
    {
        return Collections.unmodifiableCollection( metricColumns.keySet( ) );
    }

    @Override
    public int getNumRows( )
    {
        return size;
    }

    @Override
    public TableColumn getDimensionColumn( String ref )
    {
        return dimensionColumns.get( ref );
    }

    @Override
    public TableColumn getMetricColumn( String ref )
    {
        return metricColumns.get( ref );
    }

    @Override
    public TableColumn getDimensionColumn( Concept concept )
    {
        return dimensionColumns.get( ParserUtils.getMappedDimensionColumn( slice, concept ) );
    }

    @Override
    public TableColumn getMetricColumn( Concept concept )
    {
        return metricColumns.get( ParserUtils.getMappedMetricColumn( slice, concept ) );
    }
}
