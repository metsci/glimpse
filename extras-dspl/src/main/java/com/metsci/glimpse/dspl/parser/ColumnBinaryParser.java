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
package com.metsci.glimpse.dspl.parser;

import static com.metsci.glimpse.dspl.parser.util.ParserUtils.*;
import static com.metsci.glimpse.util.GeneralUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import com.metsci.glimpse.dspl.parser.SimpleBinaryParser.HeaderInformation;
import com.metsci.glimpse.dspl.parser.column.CompactInternStringTableColumn;
import com.metsci.glimpse.dspl.parser.column.CompactStringTableColumn;
import com.metsci.glimpse.dspl.parser.column.SimpleTableColumn;
import com.metsci.glimpse.dspl.parser.column.SliceColumnType;
import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.parser.table.PropertyTableData;
import com.metsci.glimpse.dspl.parser.table.SimplePropertyTableData;
import com.metsci.glimpse.dspl.parser.table.SimpleSliceTableData;
import com.metsci.glimpse.dspl.parser.table.SliceTableData;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.ParserFactory;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.SimpleParserFactory;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.TableColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.TableParserInfo;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.Table.Column;
import com.metsci.glimpse.dspl.util.DsplException;
import com.metsci.glimpse.dspl.util.DsplHelper;
import com.metsci.glimpse.util.io.datapipe.ReadableDataChannel;
import com.metsci.glimpse.util.io.datapipe.WritableDataChannel;

public class ColumnBinaryParser implements TableParser, TableWriter
{
    public static final int MAGIC = 0x1234CDEF;
    public static final String ENCODING = "UTF-8";

    protected SimpleParserFactory factory;
    protected byte[] buffer;

    public ColumnBinaryParser( )
    {
        this.factory = createParserFactory( );
        this.buffer = new byte[1024];
    }

    public SimpleParserFactory createParserFactory( )
    {
        return new SimpleParserFactory( );
    }

    public void write( Slice slice, SliceTableData data, WritableByteChannel byteChannel ) throws IOException, DsplException, JAXBException
    {
        WritableDataChannel dataChannel = new WritableDataChannel( byteChannel );

        try
        {
            // each .bin file must start with the correct magic number identifier
            dataChannel.writeInt( MAGIC );

            Collection<String> dimensionConceptIds = data.getDimensionColumnIds( );
            Collection<String> metricConceptIds = data.getMetricColumnIds( );

            // count the number of columns to write out
            // constant columns have their value specified in the dspl metadata, so they are not counted
            int numColumns = 0;
            int numRows = data.getNumRows( );

            List<TableColumn> columnList = new ArrayList<TableColumn>( );

            for ( String dimensionConceptId : dimensionConceptIds )
            {
                TableColumn column = data.getDimensionColumn( dimensionConceptId );

                if ( !column.isConstant( ) )
                {
                    columnList.add( column );
                    numColumns++;
                }
            }

            for ( String metricConceptId : metricConceptIds )
            {
                TableColumn column = data.getMetricColumn( metricConceptId );

                if ( !column.isConstant( ) )
                {
                    columnList.add( column );
                    numColumns++;
                }
            }

            // write the number of rows and columns in the data set
            dataChannel.writeInt( numColumns );
            dataChannel.writeInt( numRows );

            // write the header information for non-constant columns
            for ( String dimensionConceptId : dimensionConceptIds )
            {
                TableColumn column = data.getDimensionColumn( dimensionConceptId );

                if ( !column.isConstant( ) )
                {
                    dataChannel.writeString( column.getColumn( ).getId( ) );
                }
            }

            for ( String metricConceptId : metricConceptIds )
            {
                TableColumn column = data.getMetricColumn( metricConceptId );

                if ( !column.isConstant( ) )
                {
                    dataChannel.writeString( column.getColumn( ).getId( ) );
                }
            }

            for ( int j = 0; j < numColumns; j++ )
            {
                TableColumn column = columnList.get( j );
                write( column, dataChannel );
            }
        }
        finally
        {
            dataChannel.flushBuffer( );
            dataChannel.close( );
        }
    }

    @Override
    public void write( Slice slice, SliceTableData data, OutputStream stream ) throws IOException, DsplException, JAXBException
    {
        write( slice, data, Channels.newChannel( stream ) );
    }

    public void write( Concept concept, PropertyTableData data, WritableByteChannel byteChannel ) throws IOException, DsplException, JAXBException
    {
        WritableDataChannel dataChannel = new WritableDataChannel( byteChannel );

        try
        {
            // each .bin file must start with the correct magic number identifier
            dataChannel.writeInt( MAGIC );

            Collection<String> conceptIds = data.getColumnIds( );

            // count the number of columns to write out
            // constant columns have their value specified in the dspl metadata, so they are not counted
            int numColumns = 0;
            int numRows = data.getNumRows( );

            List<TableColumn> columnList = new ArrayList<TableColumn>( );

            for ( String conceptId : conceptIds )
            {
                TableColumn column = data.getColumn( conceptId );

                if ( !column.isConstant( ) )
                {
                    columnList.add( column );
                    numColumns++;
                }
            }

            // write the number of rows and columns in the data set
            dataChannel.writeInt( numColumns );
            dataChannel.writeInt( numRows );

            // write the header information for non-constant columns
            for ( String conceptId : conceptIds )
            {
                TableColumn column = data.getColumn( conceptId );

                if ( !column.isConstant( ) )
                {
                    dataChannel.writeString( column.getColumn( ).getId( ) );
                }
            }

            for ( int j = 0; j < numColumns; j++ )
            {
                TableColumn column = columnList.get( j );
                write( column, dataChannel );
            }
        }
        finally
        {
            dataChannel.flushBuffer( );
            dataChannel.close( );
        }
    }

    @Override
    public void write( Concept concept, PropertyTableData data, OutputStream stream ) throws IOException, DsplException, JAXBException
    {
        write( concept, data, Channels.newChannel( stream ) );

    }

    protected void write( TableColumn column, WritableDataChannel dataChannel ) throws DsplException, IOException
    {
        String columnFormat = column.getColumn( ).getFormat( );

        switch ( column.getType( ) )
        {
        case STRING:
            if ( columnFormat != null && columnFormat.contentEquals( "intern" ) )
                writeInternStringArray( dataChannel, column.getStringData( ) );
            else
                writeStringArray( dataChannel, column.getStringData( ) );
            break;

        case FLOAT:
            dataChannel.writeFloatArray( column.getFloatData( ) );
            break;
        case INTEGER:
            dataChannel.writeIntArray( column.getIntegerData( ) );
            break;
        case BOOLEAN:
            dataChannel.writeBooleanArray( column.getBooleanData( ) );
            break;
        case DATE:
            dataChannel.writeLongArray( column.getDateData( ) );
            break;
        case CONCEPT:
            writeStringArray( dataChannel, column.getStringData( ) );
            break;
        default:
            throw new DsplException( "Unknown Type %s provided.", column.getType( ) );
        }
    }

    @Override
    public SliceTableData parse( Slice slice ) throws IOException, JAXBException, DsplException
    {
        return parse( slice, DsplHelper.getTableInputStream( slice ) );
    }

    @Override
    public PropertyTableData parse( Concept concept ) throws IOException, JAXBException, DsplException
    {
        return parse( concept, DsplHelper.getTableInputStream( concept ) );
    }

    @Override
    public PropertyTableData parse( Concept concept, InputStream stream ) throws IOException, JAXBException, DsplException
    {
        return parse( concept, Channels.newChannel( stream ) );
    }

    @Override
    public PropertyTableData parse( Concept concept, ReadableByteChannel byteChannel ) throws IOException, JAXBException, DsplException
    {
        ReadableDataChannel dataChannel = new ReadableDataChannel( byteChannel );

        try
        {
            BinaryTableParserInfo info = newParserInfo( concept, dataChannel );

            Concept[] concepts = info.getConcepts( );
            Column[] columns = info.getColumns( );
            DataType[] types = info.getDataTypes( );
            int numRows = info.getNumRows( );

            Map<String, TableColumn> map = new HashMap<String, TableColumn>( );

            for ( int i = 0; i < info.getNumColumns( ); i++ )
            {
                DataType type = types[i];
                Column column = columns[i];
                Concept columnConcept = concepts[i];
                String columnFormat = column.getFormat( );

                TableColumn columnData = null;

                switch ( type )
                {
                case STRING:
                    if ( columnFormat != null && columnFormat.contentEquals( "intern" ) )
                        columnData = readInternStringArray( dataChannel, column, columnConcept, new String[numRows] );
                    else
                        columnData = readStringArray( dataChannel, column, columnConcept, new String[numRows] );
                    break;

                case FLOAT:
                    columnData = new SimpleTableColumn( column, columnConcept, type, dataChannel.readFloatArray( new float[numRows] ), numRows );
                    break;
                case INTEGER:
                    columnData = new SimpleTableColumn( column, columnConcept, type, dataChannel.readIntArray( new int[numRows] ), numRows );
                    break;
                case BOOLEAN:
                    columnData = new SimpleTableColumn( column, columnConcept, type, dataChannel.readBooleanArray( new boolean[numRows] ), numRows );
                    break;
                case DATE:
                    columnData = new SimpleTableColumn( column, columnConcept, type, dataChannel.readLongArray( new long[numRows] ), numRows );
                    break;
                case CONCEPT:
                    columnData = readStringArray( dataChannel, column, columnConcept, new String[numRows] );
                    break;
                default:
                    throw new DsplException( "Unknown Type %s provided for Column %s.", type, info.getColumnIds( )[i] );
                }

                map.put( column.getId( ), columnData );
            }

            map.putAll( getConstantTableColumns( concept, info, factory, numRows ) );

            return new SimplePropertyTableData( concept, map );
        }
        finally
        {
            dataChannel.close( );
        }
    }

    @Override
    public SliceTableData parse( Slice slice, InputStream stream ) throws IOException, JAXBException, DsplException
    {
        return parse( slice, Channels.newChannel( stream ) );
    }

    @Override
    public SliceTableData parse( Slice slice, ReadableByteChannel byteChannel ) throws IOException, JAXBException, DsplException
    {
        ReadableDataChannel dataChannel = new ReadableDataChannel( byteChannel );

        try
        {
            BinaryTableParserInfo info = newParserInfo( slice, dataChannel );

            Concept[] concepts = info.getConcepts( );
            Column[] columns = info.getColumns( );
            DataType[] types = info.getDataTypes( );
            SliceColumnType[] sliceColumnTypes = info.getSliceColumnTypes( );
            int numRows = info.getNumRows( );

            Map<String, TableColumn> dimensionMap = new HashMap<String, TableColumn>( );
            Map<String, TableColumn> metricMap = new HashMap<String, TableColumn>( );
            Map<String, TableColumn> map = null;

            for ( int i = 0; i < info.getNumColumns( ); i++ )
            {
                DataType type = types[i];
                Column column = columns[i];
                Concept columnConcept = concepts[i];
                SliceColumnType sliceType = sliceColumnTypes[i];
                String columnFormat = column.getFormat( );

                switch ( sliceType )
                {
                case Dimension:
                    map = dimensionMap;
                    break;
                case Metric:
                    map = metricMap;
                    break;
                }

                TableColumn columnData = null;

                switch ( type )
                {
                case STRING:
                    if ( columnFormat != null && columnFormat.contentEquals( "intern" ) )
                        columnData = readInternStringArray( dataChannel, column, columnConcept, new String[numRows] );
                    else
                        columnData = readStringArray( dataChannel, column, columnConcept, new String[numRows] );
                    break;

                case FLOAT:
                    columnData = new SimpleTableColumn( column, columnConcept, type, dataChannel.readFloatArray( new float[numRows] ), numRows );
                    break;
                case INTEGER:
                    columnData = new SimpleTableColumn( column, columnConcept, type, dataChannel.readIntArray( new int[numRows] ), numRows );
                    break;
                case BOOLEAN:
                    columnData = new SimpleTableColumn( column, columnConcept, type, dataChannel.readBooleanArray( new boolean[numRows] ), numRows );
                    break;
                case DATE:
                    columnData = new SimpleTableColumn( column, columnConcept, type, dataChannel.readLongArray( new long[numRows] ), numRows );
                    break;
                case CONCEPT:
                    columnData = readStringArray( dataChannel, column, columnConcept, new String[numRows] );
                    break;
                default:
                    throw new DsplException( "Unknown Type %s provided for Column %s.", type, info.getColumnIds( )[i] );
                }

                map.put( column.getId( ), columnData );
            }

            dimensionMap.putAll( getConstantTableColumns( slice, info, factory, SliceColumnType.Dimension, numRows ) );
            metricMap.putAll( getConstantTableColumns( slice, info, factory, SliceColumnType.Metric, numRows ) );

            return new SimpleSliceTableData( slice, dimensionMap, metricMap );
        }
        finally
        {
            dataChannel.close( );
        }
    }

    @Override
    public boolean isCachable( )
    {
        return false;
    }

    protected HeaderInformation parseFirstLine( ReadableDataChannel dataChannel ) throws IOException, JAXBException, DsplException
    {
        int magic = dataChannel.readInt( );

        if ( magic != MAGIC )
        {
            throw new DsplException( "File does not contain proper header code: %s", Integer.toHexString( MAGIC ) );
        }

        int numColumns = dataChannel.readInt( );
        int numRowsPerColumn = dataChannel.readInt( );

        String[] headerStrings = new String[numColumns];

        for ( int i = 0; i < numColumns; i++ )
        {
            headerStrings[i] = dataChannel.readString( );
        }

        return new HeaderInformation( numColumns, numRowsPerColumn, headerStrings );
    }

    protected BinaryTableParserInfo newParserInfo( Concept concept, ReadableDataChannel dataChannel ) throws IOException, JAXBException, DsplException
    {
        HeaderInformation header = parseFirstLine( dataChannel );
        String[] columnIds = header.getHeaderNames( );
        Concept[] concepts = getConcepts( columnIds, concept );
        Column[] columns = getColumns( columnIds, concept.getTable( ) );
        DataType[] types = getTypes( concepts, columns );
        TableColumnParser[] parsers = getParsers( factory, columns, types );

        return new BinaryTableParserInfo( header.getNumColumns( ), header.getNumRowsPerColumn( ), columnIds, concepts, null, columns, types, parsers );
    }

    protected BinaryTableParserInfo newParserInfo( Slice slice, ReadableDataChannel dataChannel ) throws IOException, JAXBException, DsplException
    {
        HeaderInformation header = parseFirstLine( dataChannel );
        String[] columnIds = header.getHeaderNames( );
        int numColumns = header.getNumColumns( );
        Concept[] concepts = new Concept[numColumns];
        SliceColumnType[] sliceColumnTypes = new SliceColumnType[numColumns];
        getConcepts( header.getHeaderNames( ), slice, concepts, sliceColumnTypes );
        Column[] columns = getColumns( columnIds, slice.getTable( ) );
        DataType[] types = getTypes( concepts, columns );
        TableColumnParser[] parsers = getParsers( factory, columns, types );

        return new BinaryTableParserInfo( header.getNumColumns( ), header.getNumRowsPerColumn( ), columnIds, concepts, sliceColumnTypes, columns, types, parsers );
    }

    protected TableColumnParser[] getParsers( ParserFactory factory, Column[] columns, DataType[] types ) throws DsplException
    {
        int size = columns.length;

        TableColumnParser[] parsers = new TableColumnParser[size];

        for ( int i = 0; i < size; i++ )
        {
            parsers[i] = factory.getParser( columns[i], types[i] );
        }

        return parsers;
    }

    protected class BinaryTableParserInfo extends TableParserInfo
    {
        protected int numColumns;
        protected int numRows;

        public BinaryTableParserInfo( int numColumns, int numRows, String[] columnIds, Concept[] concepts, SliceColumnType[] sliceColumnTypes, Column[] columns, DataType[] types, TableColumnParser[] parsers )
        {
            super( columnIds, concepts, sliceColumnTypes, columns, types, parsers );

            this.numColumns = numColumns;
            this.numRows = numRows;
        }

        public int getNumColumns( )
        {
            return numColumns;
        }

        public int getNumRows( )
        {
            return numRows;
        }
    }

    protected void writeInternStringArray( WritableDataChannel dataChannel, String[] array ) throws IOException
    {
        // build mapping
        TreeSet<String> unique = newTreeSet( );
        for ( String string : array )
            if ( string != null ) unique.add( string );

        // mapping from string to index (probably could skip this or the previous if clever)
        Map<String, Integer> map = newLinkedHashMap( );
        int stringIndex = 0;
        for ( String string : unique )
            map.put( string, stringIndex++ );

        // lookups for each row
        int[] index = new int[array.length];
        for ( int i = 0; i < array.length; i++ )
            index[i] = array[i] == null ? -1 : map.get( array[i] );

        // write strings
        dataChannel.writeInt( map.size( ) );
        for ( String string : map.keySet( ) )
            writeString( string, dataChannel );

        dataChannel.writeIntArray( index );
    }

    protected TableColumn readInternStringArray( ReadableDataChannel dataChannel, Column column, Concept concept, String[] array ) throws IOException
    {
        int nUnique = dataChannel.readInt( );

        String[] uniqueStrings = new String[nUnique];
        for ( int i = 0; i < nUnique; i++ )
        {
            String string = readString( dataChannel );
            uniqueStrings[i] = string.intern( );
        }

        int[] indexForRow = new int[array.length];
        dataChannel.readIntArray( indexForRow );

        return new CompactInternStringTableColumn( column, concept, array.length, uniqueStrings, indexForRow );
    }

    protected String readString( ReadableDataChannel in ) throws IOException
    {
        int stringSize = in.readInt( );
        if ( stringSize == 0 ) return null;

        byte[] data = new byte[stringSize];
        in.readByteArray( data );

        return new String( data, ENCODING );
    }

    protected void writeString( String string, WritableDataChannel dataChannel ) throws IOException
    {
        if ( string == null )
        {
            dataChannel.writeInt( 0 );
        }
        else
        {
            byte[] stringData = string.getBytes( SimpleBinaryParser.ENCODING );
            dataChannel.writeInt( stringData.length );
            dataChannel.writeByteArray( stringData );
        }
    }

    protected void writeStringArray( WritableDataChannel dataChannel, String[] array ) throws IOException
    {
        byte[][] encodings = new byte[array.length][];
        int byteCount = 0;

        for ( int i = 0; i < array.length; i++ )
        {
            String s = array[i];

            if ( s != null )
            {
                encodings[i] = s.getBytes( ENCODING );
                byteCount += encodings[i].length;
            }
        }

        // write the total number of bytes in the encoded string array
        dataChannel.writeInt( byteCount );

        // assuming the character data for the strings are stored in a single large string,
        // write the index of the first character of each string
        int stringStartChar = 0;
        for ( int i = 0; i < array.length; i++ )
        {
            String s = array[i];

            dataChannel.writeInt( stringStartChar );

            if ( s != null ) stringStartChar += array[i].length( );
        }

        for ( int i = 0; i < array.length; i++ )
        {
            String s = array[i];

            if ( s != null )
            {
                dataChannel.writeByteArray( encodings[i] );
            }
        }
    }

    protected TableColumn readStringArray( ReadableDataChannel dataChannel, Column column, Concept concept, String[] array ) throws IOException
    {
        int byteCount = dataChannel.readInt( );

        int[] stringStartIndices = new int[array.length];
        dataChannel.readIntArray( stringStartIndices );

        byte[] stringData = new byte[byteCount];
        dataChannel.readByteArray( stringData );
        String allStrings = new String( stringData, 0, byteCount, ENCODING );

        return new CompactStringTableColumn( column, concept, array.length, allStrings, stringStartIndices );
    }
}
