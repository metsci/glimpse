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
import static com.metsci.glimpse.dspl.parser.util.QuoteAwareStringSplitter.splitLine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBException;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.parser.column.CompactInternStringTableColumn;
import com.metsci.glimpse.dspl.parser.column.CompactStringTableColumn;
import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.parser.table.PropertyTableData;
import com.metsci.glimpse.dspl.parser.table.SliceTableData;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.DateColumnParser;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.ParserFactory;
import com.metsci.glimpse.dspl.parser.util.ParserUtils.TableParserInfo;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.Data;
import com.metsci.glimpse.dspl.schema.Data.File;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.Table;
import com.metsci.glimpse.dspl.schema.Table.Column;
import com.metsci.glimpse.dspl.util.DsplException;
import com.metsci.glimpse.util.primitives.BytesArray;
import com.metsci.glimpse.util.primitives.IntsArray;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ExtendedCsvParser extends CsvParser implements MultipleFileTableParser
{
    public ExtendedCsvParser( DsplParser dsplParser )
    {
        super( dsplParser );
    }

    public static final String META = "([{\\^-$|]})?*+.";

    @Override
    public ParserFactory createParserFactory( )
    {
        return new ExtendedCSVParserFactory( );
    }

    @Override
    protected void parse( BufferedReader in, TableParserInfo info ) throws IOException, DsplException
    {
        CSVTableColumnParser[] parsers = ( CSVTableColumnParser[] ) info.getParsers( );
        Column[] columns = info.getColumns( );

        int size = parsers.length;
        String line = null;

        // extends the default parsing semantics to include a possible "remainder" format
        // for the last column, indicating that that column should get all extra tokens as one string
        String trailingFormat = columns[size - 1].getFormat( );
        boolean allowExtraTrailingTokens = ( trailingFormat != null && trailingFormat.equals( "remainder" ) );
        int splitLimit = allowExtraTrailingTokens ? size : Integer.MAX_VALUE;

        while ( ( line = in.readLine( ) ) != null )
        {
            if ( line.isEmpty( ) ) continue;

            String[] tokens = splitLine( line, splitLimit );

            for ( int i = 0; i < size; i++ )
            {
                if ( i < tokens.length )
                    parsers[i].addData( tokens[i] );
                else
                    parsers[i].addGap( );
            }
        }
    }

    @Override
    public SliceTableData parse( Slice slice ) throws IOException, JAXBException, DsplException
    {
        List<URL> files = getDataFiles( slice );

        BufferedReader in = new BufferedReader( new InputStreamReader( files.get( 0 ).openStream( ) ) );

        TableParserInfo info = null;

        try
        {
            info = newParserInfo( slice, in );
        }
        finally
        {
            in.close( );
        }

        for ( URL f : files )
        {
            in = new BufferedReader( new InputStreamReader( f.openStream( ) ) );

            try
            {
                in.readLine( ); // read the header line, which must be the same in all the files
                parse( in, info );
            }
            finally
            {
                in.close( );
            }
        }

        return buildSliceTableData( slice, info, factory );
    }

    @Override
    public PropertyTableData parse( Concept concept ) throws IOException, JAXBException, DsplException
    {
        List<URL> files = getDataFiles( concept );

        BufferedReader in = new BufferedReader( new InputStreamReader( files.get( 0 ).openStream( ) ) );

        TableParserInfo info = null;

        try
        {
            info = newParserInfo( concept, in );
        }
        finally
        {
            in.close( );
        }

        for ( URL f : files )
        {
            in = new BufferedReader( new InputStreamReader( f.openStream( ) ) );

            try
            {
                in.readLine( ); // read the header line, which must be the same in all the files
                parse( in, info );
            }
            finally
            {
                in.close( );
            }
        }

        return buildPropertyTableData( concept, info, factory );
    }

    @Override
    public List<URL> getDataFiles( Slice slice ) throws JAXBException, IOException, DsplException
    {
        return getDataFiles( slice.getDataSet( ), slice.getTable( ), slice.getId( ) );
    }

    @Override
    public List<URL> getDataFiles( Concept concept ) throws JAXBException, IOException, DsplException
    {
        return getDataFiles( concept.getDataSet( ), concept.getTable( ), concept.getId( ) );
    }

    protected List<URL> getDataFiles( DataSet dataset, Table table, String id ) throws JAXBException, IOException, DsplException
    {
        java.io.File baseFile = dataset.getFile( );

        if ( baseFile == null )
        {
            throw new DsplException( "ExtendedCsvParser (csvx) can only be used with datasets whose getFile( ) is set." );
        }

        Data data = table.getData( );
        if ( data == null )
        {
            throw new DsplException( "No assoicated table is defined for %s.", id );
        }

        File file = data.getFile( );
        String name = file.getValue( );

        String regexp = toRegularExpression( name );
        Pattern pattern = Pattern.compile( regexp );

        java.io.File parentFile = baseFile.getParentFile( );

        if ( baseFile.getName( ).endsWith( ".xml" ) )
        {
            return toURLs( getAllFiles( parentFile, pattern ) );
        }
        else if ( baseFile.getName( ).endsWith( ".zip" ) )
        {
            return getAllFiles( new ZipFile( baseFile ), pattern );
        }

        throw new DsplException( "DataSet file field must reference xml file or zip archive." );
    }

    protected String toRegularExpression( String expression )
    {
        return expression;
    }

    protected List<URL> toURLs( List<java.io.File> files ) throws MalformedURLException
    {
        List<URL> urls = new ArrayList<URL>( );

        for ( java.io.File file : files )
        {
            urls.add( file.toURI( ).toURL( ) );
        }

        return urls;
    }

    protected List<URL> getAllFiles( ZipFile file, Pattern pattern ) throws MalformedURLException
    {
        List<URL> urls = new ArrayList<URL>( );

        Enumeration<? extends ZipEntry> entries = file.entries( );
        while ( entries.hasMoreElements( ) )
        {
            ZipEntry entry = entries.nextElement( );
            if ( pattern.matcher( entry.getName( ) ).find( ) )
            {
                urls.add( new URL( String.format( "jar:file:%s!/%s", file.getName( ), entry.getName( ) ) ) );
            }
        }

        return urls;
    }

    protected List<java.io.File> getAllFiles( java.io.File file, Pattern pattern )
    {
        List<java.io.File> files = getAllFiles( file );

        Iterator<java.io.File> iter = files.iterator( );
        while ( iter.hasNext( ) )
        {
            java.io.File f = iter.next( );
            if ( !pattern.matcher( f.getName( ) ).find( ) ) iter.remove( );
        }

        return files;
    }

    protected List<java.io.File> getAllFiles( java.io.File file )
    {
        List<java.io.File> files = new ArrayList<java.io.File>( );
        getAllFiles( file, files );
        return files;
    }

    protected void getAllFiles( java.io.File file, List<java.io.File> files )
    {
        if ( file.isDirectory( ) )
        {
            java.io.File[] childFiles = file.listFiles( );
            for ( java.io.File childFile : childFiles )
            {
                getAllFiles( childFile, files );
            }
        }
        else
        {
            files.add( file );
        }
    }

    public class ExtendedCSVParserFactory extends CSVParserFactory
    {
        // replace the joda date parser which Google specifies with the standard Java date parser
        // which handles string formatted time zone specifications better
        @Override
        public CSVTableColumnParser newDateParser( Column column )
        {
            return new ExtendedCSVDateColumnParser( column );
        }

        // allow the "intern" format string which provides control over when strings are interned
        @Override
        public CSVTableColumnParser newStringParser( Column column )
        {
            String format = column.getFormat( );
            boolean intern = format != null && format.equals( "intern" );

            if ( intern )
            {
                return new CSVCompactInternStringColumnParser( );
            }
            else
            {
                return new CSVCompactStringColumnParser( );
            }
        }
    }

    protected static class CSVCompactStringColumnParser implements CSVTableColumnParser
    {
        protected BytesArray allStrings;
        protected IntsArray offsets;

        public CSVCompactStringColumnParser( )
        {
            super( );

            allStrings = new BytesArray( );
            offsets = new IntsArray( );
        }

        @Override
        public TableColumn createTableColumn( Column column, Concept concept, DataType type )
        {
            return new CompactStringTableColumn( column, concept, offsets.n, allStrings.string( ), offsets.copyOf( ) );
        }

        @Override
        public String parse( String data )
        {
            return new String( data );
        }

        @Override
        public void addData( String value )
        {
            offsets.append( allStrings.n );

            if ( value != null )
            {
                allStrings.append( value );
            }
        }

        @Override
        public void addGap( )
        {
            addData( null );
        }
    }

    protected static class CSVCompactInternStringColumnParser implements CSVTableColumnParser
    {
        protected Object2IntOpenHashMap<String> indexByString;
        protected IntsArray indexByRow;

        public CSVCompactInternStringColumnParser( )
        {
            indexByString = new Object2IntOpenHashMap<String>( );
            indexByString.defaultReturnValue( -1 );

            indexByRow = new IntsArray( );
        }

        @Override
        public void addData( String value )
        {
            // We don't want to store a substring, which might hang on
            // to more characters than it needs. So make a clean copy.
            value = new String( value );

            int index = indexByString.getInt( value );
            if ( index == -1 )
            {
                index = indexByString.size( );
                indexByString.put( value, index );
            }

            indexByRow.append( index );
        }

        @Override
        public void addGap( )
        {
            indexByRow.append( -1 );
        }

        @Override
        public TableColumn createTableColumn( Column column, Concept concept, DataType type )
        {
            // This seems a little dangerous, but works because we're careful
            // to put each integer on [0,size) into the map exactly once
            String[] uniqueStrings = new String[indexByString.size( )];
            for ( Object2IntMap.Entry<String> entry : indexByString.object2IntEntrySet( ) )
            {
                int index = entry.getIntValue( );
                uniqueStrings[index] = entry.getKey( );
            }

            return new CompactInternStringTableColumn( column, concept, indexByRow.n, uniqueStrings, indexByRow.copyOf( ) );
        }

        @Override
        public String parse( String data )
        {
            return new String( data );
        }
    }

    protected class ExtendedCSVDateColumnParser extends DateColumnParser implements CSVTableColumnParser
    {
        protected DateFormat alternateDateFormat;

        public ExtendedCSVDateColumnParser( Column column )
        {
            super( column );

            this.alternateDateFormat = new SimpleDateFormat( format );
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
                    data.append( parse( token ) );
                }
                catch ( Exception e )
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

        @Override
        public Long parse( String data ) throws DsplException
        {
            try
            {
                //XXX The Java parser doesn't handle "Z" as a time zone
                //XXX this slows down the parse though, so it's an ugly hack
                data = data.replaceFirst( "Z", "GMT" );
                return alternateDateFormat.parse( data ).getTime( );
            }
            catch ( ParseException e )
            {
                throw new DsplException( "Trouble parsing date: %s.", e, data );
            }
        }
    }
}
