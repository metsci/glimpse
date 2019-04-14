/*
 * Copyright (c) 2019, Metron, Inc.
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

import static com.metsci.glimpse.dspl.parser.util.ParserUtils.buildPropertyTableData;
import static com.metsci.glimpse.dspl.parser.util.ParserUtils.buildSliceTableData;
import static com.metsci.glimpse.dspl.parser.util.ParserUtils.getColumns;
import static com.metsci.glimpse.dspl.parser.util.ParserUtils.getConcepts;
import static com.metsci.glimpse.dspl.parser.util.ParserUtils.getTypes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.joda.time.format.DateTimeFormatter;

import com.metsci.glimpse.dspl.parser.column.SliceColumnType;
import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.parser.table.PropertyTableData;
import com.metsci.glimpse.dspl.parser.table.SliceTableData;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.BooleanColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.DateColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.FloatColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.IntegerColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.SimpleParserFactory;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.StringColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.TableColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.TableParserInfo;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.Table.Column;
import com.metsci.glimpse.dspl.util.DsplException;
import com.metsci.glimpse.dspl.util.DsplHelper;

public class SimpleBinaryParser implements TableParser, TableWriter
{
    public static final int MAGIC = 0xFEDC4321;
    public static final String ENCODING = "UTF-8";

    protected BinaryParserFactory factory;
    protected byte[] buffer;

    public SimpleBinaryParser( )
    {
        this.factory = createParserFactory( );
        this.buffer = new byte[1024];
    }

    public BinaryParserFactory createParserFactory( )
    {
        return new BinaryParserFactory( );
    }

    @Override
    public boolean isCachable( )
    {
        return false;
    }

    @Override
    public SliceTableData parse( Slice slice ) throws IOException, JAXBException, DsplException
    {
        return parse( slice, DsplHelper.getTableInputStream( slice ) );
    }

    @Override
    public SliceTableData parse( Slice slice, ReadableByteChannel channel ) throws IOException, JAXBException, DsplException
    {
        return parse( slice, Channels.newInputStream( channel ) );
    }

    @Override
    public SliceTableData parse( Slice slice, InputStream stream ) throws IOException, JAXBException, DsplException
    {
        DataInputStream in = new DataInputStream( new BufferedInputStream( stream ) );

        try
        {
            BinaryTableParserInfo info = newParserInfo( slice, in );

            parse( in, info );

            return buildSliceTableData( slice, info, factory );
        }
        finally
        {
            in.close( );
        }
    }

    @Override
    public PropertyTableData parse( Concept concept ) throws IOException, JAXBException, DsplException
    {
        return parse( concept, DsplHelper.getTableInputStream( concept ) );
    }

    @Override
    public PropertyTableData parse( Concept concept, ReadableByteChannel channel ) throws IOException, JAXBException, DsplException
    {
        return parse( concept, Channels.newInputStream( channel ) );
    }

    @Override
    public PropertyTableData parse( Concept concept, InputStream stream ) throws IOException, JAXBException, DsplException
    {
        DataInputStream in = new DataInputStream( new BufferedInputStream( stream ) );

        try
        {
            BinaryTableParserInfo info = newParserInfo( concept, in );

            parse( in, info );

            return buildPropertyTableData( concept, info, factory );
        }
        finally
        {
            in.close( );
        }
    }

    protected void parse( DataInputStream in, BinaryTableParserInfo info ) throws IOException
    {
        BinaryTableColumnParser[] parsers = info.getParsers( );

        for ( int i = 0; i < info.getNumRows( ); i++ )
        {
            for ( int j = 0; j < info.getNumColumns( ); j++ )
            {
                parsers[j].addData( in );
            }
        }
    }

    protected HeaderInformation parseFirstLine( DataInputStream in ) throws IOException, JAXBException, DsplException
    {
        int magic = in.readInt( );

        if ( magic != MAGIC )
        {
            throw new DsplException( "File does not contain proper header code: %s", Integer.toHexString( MAGIC ) );
        }

        int numColumns = in.readInt( );
        int numRowsPerColumn = in.readInt( );

        String[] headerStrings = new String[numColumns];

        for ( int i = 0; i < numColumns; i++ )
        {
            headerStrings[i] = readString( in );
        }

        return new HeaderInformation( numColumns, numRowsPerColumn, headerStrings );
    }

    protected BinaryTableParserInfo newParserInfo( Concept concept, DataInputStream in ) throws IOException, JAXBException, DsplException
    {
        HeaderInformation header = parseFirstLine( in );
        String[] columnIds = header.getHeaderNames( );
        Concept[] concepts = getConcepts( columnIds, concept );
        Column[] columns = getColumns( columnIds, concept.getTable( ) );
        DataType[] types = getTypes( concepts, columns );
        BinaryTableColumnParser[] parsers = getParsers( factory, columns, types );

        return new BinaryTableParserInfo( header.getNumColumns( ), header.getNumRowsPerColumn( ), columnIds, concepts, null, columns, types, parsers );
    }

    protected BinaryTableParserInfo newParserInfo( Slice slice, DataInputStream in ) throws IOException, JAXBException, DsplException
    {
        HeaderInformation header = parseFirstLine( in );
        String[] columnIds = header.getHeaderNames( );
        int numColumns = header.getNumColumns( );
        Concept[] concepts = new Concept[numColumns];
        SliceColumnType[] sliceColumnTypes = new SliceColumnType[numColumns];
        getConcepts( header.getHeaderNames( ), slice, concepts, sliceColumnTypes );
        Column[] columns = getColumns( columnIds, slice.getTable( ) );
        DataType[] types = getTypes( concepts, columns );
        BinaryTableColumnParser[] parsers = getParsers( factory, columns, types );

        return new BinaryTableParserInfo( header.getNumColumns( ), header.getNumRowsPerColumn( ), columnIds, concepts, sliceColumnTypes, columns, types, parsers );
    }

    protected class BinaryTableParserInfo extends TableParserInfo
    {
        protected int numColumns;
        protected int numRows;
        protected BinaryTableColumnParser[] parsers;

        public BinaryTableParserInfo( int numColumns, int numRows, String[] columnIds, Concept[] concepts, SliceColumnType[] sliceColumnTypes, Column[] columns, DataType[] types, BinaryTableColumnParser[] parsers )
        {
            super( columnIds, concepts, sliceColumnTypes, columns, types, parsers );

            this.numColumns = numColumns;
            this.numRows = numRows;
            this.parsers = parsers;
        }

        public int getNumColumns( )
        {
            return numColumns;
        }

        public int getNumRows( )
        {
            return numRows;
        }

        @Override
        public BinaryTableColumnParser[] getParsers( )
        {
            return parsers;
        }
    }

    public static class HeaderInformation
    {
        int numColumns;
        int numRowsPerColumn;
        String[] headerNames;

        public HeaderInformation( int numColumns, int numRowsPerColumn, String[] headerNames )
        {
            super( );
            this.numColumns = numColumns;
            this.numRowsPerColumn = numRowsPerColumn;
            this.headerNames = headerNames;
        }

        public int getNumColumns( )
        {
            return numColumns;
        }

        public int getNumRowsPerColumn( )
        {
            return numRowsPerColumn;
        }

        public String[] getHeaderNames( )
        {
            return headerNames;
        }
    }

    protected BinaryTableColumnParser[] getParsers( BinaryParserFactory factory, Column[] columns, DataType[] types ) throws DsplException
    {
        int size = columns.length;

        BinaryTableColumnParser[] parsers = new BinaryTableColumnParser[size];

        for ( int i = 0; i < size; i++ )
        {
            parsers[i] = factory.getParser( columns[i], types[i] );
        }

        return parsers;
    }

    public interface BinaryTableColumnParser extends TableColumnParser
    {
        public void addData( DataInputStream in ) throws IOException;
    }

    public class BinaryParserFactory extends SimpleParserFactory
    {
        @Override
        public BinaryTableColumnParser getParser( Column column, DataType type ) throws DsplException
        {
            return ( BinaryTableColumnParser ) super.getParser( column, type );
        }

        @Override
        public BinaryTableColumnParser newConceptParser( Column column )
        {
            return new BinaryStringColumnParser( );
        }

        @Override
        public BinaryTableColumnParser newDateParser( Column column )
        {
            return new BinaryDateColumnParser( column );
        }

        @Override
        public BinaryTableColumnParser newBooleanParser( Column column )
        {
            return new BinaryBooleanColumnParser( );
        }

        @Override
        public BinaryTableColumnParser newIntegerParser( Column column )
        {
            return new BinaryIntegerColumnParser( );
        }

        @Override
        public BinaryTableColumnParser newFloatParser( Column column )
        {
            return new BinaryFloatColumnParser( );
        }

        @Override
        public BinaryTableColumnParser newStringParser( Column column )
        {
            return new BinaryStringColumnParser( );
        }
    }

    protected class BinaryStringColumnParser extends StringColumnParser implements BinaryTableColumnParser
    {
        @Override
        public void addData( DataInputStream in ) throws IOException
        {
            String value = readString( in );
            data.add( value );
        }
    }

    protected class BinaryIntegerColumnParser extends IntegerColumnParser implements BinaryTableColumnParser
    {
        @Override
        public void addData( DataInputStream in ) throws IOException
        {
            data.append( in.readInt( ) );
        }
    }

    protected class BinaryFloatColumnParser extends FloatColumnParser implements BinaryTableColumnParser
    {
        @Override
        public void addData( DataInputStream in ) throws IOException
        {
            data.append( in.readFloat( ) );
        }
    }

    protected class BinaryBooleanColumnParser extends BooleanColumnParser implements BinaryTableColumnParser
    {
        @Override
        public void addData( DataInputStream in ) throws IOException
        {
            data.append( in.readBoolean( ) );
        }
    }

    protected class BinaryDateColumnParser extends DateColumnParser implements BinaryTableColumnParser
    {
        protected DateTimeFormatter dateFormat;

        public BinaryDateColumnParser( Column column )
        {
            super( column );
        }

        @Override
        public void addData( DataInputStream in ) throws IOException
        {
            data.append( in.readLong( ) );
        }
    }

    @Override
    public void write( Concept concept, PropertyTableData data, WritableByteChannel channel ) throws IOException, DsplException, JAXBException
    {
        write( concept, data, Channels.newOutputStream( channel ) );
    }

    @Override
    public void write( Concept concept, PropertyTableData data, OutputStream stream ) throws IOException, DsplException, JAXBException
    {
        DataOutputStream out = new DataOutputStream( new BufferedOutputStream( stream ) );

        try
        {
            // each .bin file must start with the correct magic number identifier
            out.writeInt( SimpleBinaryParser.MAGIC );

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
            out.writeInt( numColumns );
            out.writeInt( numRows );

            // write the header information for non-constant columns
            for ( String conceptId : conceptIds )
            {
                TableColumn column = data.getColumn( conceptId );

                if ( !column.isConstant( ) )
                {
                    writeString( column.getColumn( ).getId( ), out );
                }
            }

            for ( int i = 0; i < numRows; i++ )
            {
                for ( int j = 0; j < numColumns; j++ )
                {
                    TableColumn column = columnList.get( j );
                    write( column, out, i );
                }
            }
        }
        finally
        {
            out.close( );
        }
    }

    @Override
    public void write( Slice slice, SliceTableData data, WritableByteChannel channel ) throws IOException, DsplException, JAXBException
    {
        write( slice, data, Channels.newOutputStream( channel ) );
    }

    @Override
    public void write( Slice slice, SliceTableData data, OutputStream stream ) throws IOException, DsplException, JAXBException
    {
        DataOutputStream out = new DataOutputStream( new BufferedOutputStream( stream ) );

        try
        {
            // each .bin file must start with the correct magic number identifier
            out.writeInt( SimpleBinaryParser.MAGIC );

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
            out.writeInt( numColumns );
            out.writeInt( numRows );

            // write the header information for non-constant columns
            for ( String dimensionConceptId : dimensionConceptIds )
            {
                TableColumn column = data.getDimensionColumn( dimensionConceptId );

                if ( !column.isConstant( ) )
                {
                    writeString( column.getColumn( ).getId( ), out );
                }
            }

            for ( String metricConceptId : metricConceptIds )
            {
                TableColumn column = data.getMetricColumn( metricConceptId );

                if ( !column.isConstant( ) )
                {
                    writeString( column.getColumn( ).getId( ), out );
                }
            }

            for ( int i = 0; i < numRows; i++ )
            {
                for ( int j = 0; j < numColumns; j++ )
                {
                    TableColumn column = columnList.get( j );
                    write( column, out, i );
                }
            }
        }
        finally
        {
            out.close( );
        }
    }

    protected void write( TableColumn column, DataOutputStream out, int row ) throws DsplException, IOException
    {
        switch ( column.getType( ) )
        {
            case STRING:
                writeString( column.getStringData( row ), out );
                break;
            case FLOAT:
                out.writeFloat( column.getFloatData( row ) );
                break;
            case INTEGER:
                out.writeInt( column.getIntegerData( row ) );
                break;
            case BOOLEAN:
                out.writeBoolean( column.getBooleanData( row ) );
                break;
            case DATE:
                out.writeLong( column.getDateData( row ) );
                break;
            case CONCEPT:
                writeString( column.getStringData( row ), out );
                break;
            default:
                throw new DsplException( "Unknown Type %s provided.", column.getType( ) );
        }
    }

    protected String readString( DataInputStream in ) throws IOException
    {
        int stringSize = in.readInt( );

        if ( stringSize == 0 ) return null;

        if ( buffer.length < stringSize ) buffer = new byte[stringSize];

        int currentPosition = 0;
        int remainingBytes = stringSize;

        while ( remainingBytes > 0 )
        {
            int bytesRead = in.read( buffer, currentPosition, remainingBytes );
            currentPosition += bytesRead;
            remainingBytes -= bytesRead;
        }

        return new String( buffer, 0, stringSize, ENCODING );
    }

    protected void writeString( String string, DataOutputStream out ) throws IOException
    {
        if ( string == null )
        {
            out.writeInt( 0 );
        }
        else
        {
            byte[] stringData = string.getBytes( SimpleBinaryParser.ENCODING );
            out.writeInt( stringData.length );
            out.write( stringData );
        }
    }
}
