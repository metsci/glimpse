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
import static com.metsci.glimpse.dspl.parser.util.QuoteAwareStringSplitter.splitLine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.parser.column.SliceColumnType;
import com.metsci.glimpse.dspl.parser.table.PropertyTableData;
import com.metsci.glimpse.dspl.parser.table.SliceTableData;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.BooleanColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.DateColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.FloatColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.IntegerColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.ParserFactory;
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

/**
 * The standard DSPL CSV parser capable of parsing CSV files which correspond to Google's
 * rules for CSV table files.
 *
 * @author ulman
 */
public class CsvParser implements TableParser
{
    public static final Logger logger = Logger.getLogger( CsvParser.class.getName( ) );

    protected ParserFactory factory;
    protected DsplParser dsplParser;

    public CsvParser( DsplParser dsplParser )
    {
        this.dsplParser = dsplParser;
        this.factory = createParserFactory( );
    }

    public ParserFactory createParserFactory( )
    {
        return new CSVParserFactory( );
    }

    @Override
    public boolean isCachable( )
    {
        return true;
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
        BufferedReader in = new BufferedReader( new InputStreamReader( stream ) );

        try
        {
            TableParserInfo info = newParserInfo( slice, in );

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
        BufferedReader in = new BufferedReader( new InputStreamReader( stream ) );

        try
        {
            TableParserInfo info = newParserInfo( concept, in );

            parse( in, info );

            return buildPropertyTableData( concept, info, factory );
        }
        finally
        {
            in.close( );
        }
    }

    protected void parse( BufferedReader in, TableParserInfo info ) throws IOException, DsplException
    {
        CSVTableColumnParser[] parsers = ( CSVTableColumnParser[] ) info.getParsers( );

        int size = parsers.length;
        String line = null;

        while ( ( line = in.readLine( ) ) != null )
        {
            if ( line.isEmpty( ) ) continue;

            String[] tokens = splitLine( line );

            if ( tokens.length == size )
            {
                for ( int i = 0; i < size; i++ )
                {
                    parsers[i].addData( tokens[i] );
                }
            }
            else
            {
                if ( dsplParser.isFailOnErrorMode( ) )
                {
                    throw new DsplException( "Encountered row of incorrect size (expected %d found %d): [%s]", size, tokens.length, line );
                }
                else
                {
                    logWarning( logger, "Skipping row of incorrect size (expected %d found %d): [%s]", size, tokens.length, line );
                }
            }
        }
    }

    protected String[] parseFirstLine( BufferedReader in ) throws IOException, JAXBException
    {
        String line = in.readLine( );
        String[] tokens = splitLine( line );

        int size = tokens.length;

        String[] ids = new String[size];

        for ( int i = 0; i < size; i++ )
        {
            ids[i] = tokens[i].intern( );
        }

        return ids;
    }

    protected CSVTableColumnParser[] getParsers( ParserFactory factory, Column[] columns, DataType[] types ) throws DsplException
    {
        int size = columns.length;

        CSVTableColumnParser[] parsers = new CSVTableColumnParser[size];

        for ( int i = 0; i < size; i++ )
        {
            parsers[i] = ( CSVTableColumnParser ) factory.getParser( columns[i], types[i] );
        }

        return parsers;
    }

    public interface CSVTableColumnParser extends TableColumnParser
    {
        public void addData( String data ) throws DsplException;

        public void addGap( );
    }

    public class CSVParserFactory extends SimpleParserFactory
    {
        @Override
        public CSVTableColumnParser newConceptParser( Column column )
        {
            return new CSVStringColumnParser( );
        }

        @Override
        public CSVTableColumnParser newDateParser( Column column )
        {
            return new CSVDateColumnParser( column );
        }

        @Override
        public CSVTableColumnParser newBooleanParser( Column column )
        {
            return new CSVBooleanColumnParser( );
        }

        @Override
        public CSVTableColumnParser newIntegerParser( Column column )
        {
            return new CSVIntegerColumnParser( );
        }

        @Override
        public CSVTableColumnParser newFloatParser( Column column )
        {
            return new CSVFloatColumnParser( );
        }

        @Override
        public CSVTableColumnParser newStringParser( Column column )
        {
            return new CSVStringColumnParser( );
        }
    }

    protected class CSVStringColumnParser extends StringColumnParser implements CSVTableColumnParser
    {
        @Override
        public void addData( String token )
        {
            data.add( parse( token ) );
        }

        @Override
        public void addGap( )
        {
            data.add( null );
        }
    }

    protected class CSVIntegerColumnParser extends IntegerColumnParser implements CSVTableColumnParser
    {
        @Override
        public void addData( String token ) throws DsplException
        {
            if ( token.isEmpty( ) )
            {
                addGap( );
            }
            else
            {
                try
                {
                    data.append( Integer.parseInt( token ) );
                }
                catch ( NumberFormatException e )
                {
                    if ( dsplParser.isFailOnErrorMode( ) )
                    {
                        throw new DsplException( "Problem parsing: %s", e, token );
                    }
                    else
                    {
                        logWarning( logger, "Problem parsing token %s as type integer. Adding gap instead.", token );
                        addGap( );
                    }
                }
            }
        }

        //TODO we need a much better way to mark missing data
        @Override
        public void addGap( )
        {
            data.append( -1 );
        }
    }

    protected class CSVFloatColumnParser extends FloatColumnParser implements CSVTableColumnParser
    {
        @Override
        public void addData( String token ) throws DsplException
        {
            if ( token.isEmpty( ) )
            {
                addGap( );
            }
            else
            {
                try
                {
                    data.append( Float.parseFloat( token ) );
                }
                catch ( NumberFormatException e )
                {
                    if ( dsplParser.isFailOnErrorMode( ) )
                    {
                        throw new DsplException( "Problem parsing: %s", e, token );
                    }
                    else
                    {
                        logWarning( logger, "Problem parsing token %s as type float. Adding gap instead.", token );
                        addGap( );
                    }
                }
            }
        }

        //TODO we need a much better way to mark missing data
        @Override
        public void addGap( )
        {
            data.append( -1 );
        }
    }

    protected class CSVBooleanColumnParser extends BooleanColumnParser implements CSVTableColumnParser
    {
        @Override
        public void addData( String token ) throws DsplException
        {
            if ( token.isEmpty( ) )
            {
                addGap( );
            }
            else
            {
                try
                {
                    data.append( Boolean.parseBoolean( token ) );
                }
                catch ( NumberFormatException e )
                {
                    if ( dsplParser.isFailOnErrorMode( ) )
                    {
                        throw new DsplException( "Problem parsing: %s", e, token );
                    }
                    else
                    {
                        logWarning( logger, "Problem parsing token %s as type boolean. Adding gap instead.", token );
                        addGap( );
                    }
                }
            }
        }

        //TODO we need a much better way to mark missing data
        @Override
        public void addGap( )
        {
            data.append( false );
        }
    }

    protected class CSVDateColumnParser extends DateColumnParser implements CSVTableColumnParser
    {
        public CSVDateColumnParser( Column column )
        {
            super( column );
        }

        @Override
        public void addData( String token ) throws DsplException
        {
            if ( token.isEmpty( ) )
            {
                addGap( );
            }
            else
            {
                try
                {
                    data.append( dateFormat.parseMillis( token ) );
                }
                catch ( IllegalArgumentException e )
                {
                    if ( dsplParser.isFailOnErrorMode( ) )
                    {
                        throw new DsplException( "Problem parsing: %s", e, token );
                    }
                    else
                    {
                        logWarning( logger, "Trouble parsing date: %s. Adding gap instead.", token );
                        addGap( );
                    }
                }
            }
        }

        //TODO we need a much better way to mark missing data
        @Override
        public void addGap( )
        {
            data.append( -1 );
        }
    }

    protected TableParserInfo newParserInfo( Concept concept, BufferedReader in ) throws IOException, JAXBException, DsplException
    {
        String[] columnIds = parseFirstLine( in );
        Concept[] concepts = getConcepts( columnIds, concept );
        Column[] columns = getColumns( columnIds, concept.getTable( ) );
        DataType[] types = getTypes( concepts, columns );
        CSVTableColumnParser[] parsers = getParsers( factory, columns, types );

        return new TableParserInfo( columnIds, concepts, null, columns, types, parsers );
    }

    protected TableParserInfo newParserInfo( Slice slice, BufferedReader in ) throws IOException, JAXBException, DsplException
    {
        String[] columnIds = parseFirstLine( in );
        Concept[] concepts = new Concept[columnIds.length];
        SliceColumnType[] sliceColumnTypes = new SliceColumnType[columnIds.length];
        getConcepts( columnIds, slice, concepts, sliceColumnTypes );
        Column[] columns = getColumns( columnIds, slice.getTable( ) );
        DataType[] types = getTypes( concepts, columns );
        CSVTableColumnParser[] parsers = getParsers( factory, columns, types );

        return new TableParserInfo( columnIds, concepts, sliceColumnTypes, columns, types, parsers );
    }
}
