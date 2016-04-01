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
package com.metsci.glimpse.dspl.lite;

import static com.metsci.glimpse.dspl.util.DataSetFactory.*;
import static com.metsci.glimpse.dspl.util.DsplHelper.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.lite.schema.Column;
import com.metsci.glimpse.dspl.lite.schema.DsplLite;
import com.metsci.glimpse.dspl.lite.schema.DsplLite.Columns;
import com.metsci.glimpse.dspl.schema.Attribute;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.ConceptInfo;
import com.metsci.glimpse.dspl.schema.Data;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.SliceConceptRef;
import com.metsci.glimpse.dspl.schema.SliceTableMapping;
import com.metsci.glimpse.dspl.schema.Table;
import com.metsci.glimpse.dspl.schema.Value;
import com.metsci.glimpse.dspl.schema.Data.File;
import com.metsci.glimpse.dspl.schema.DataSet.Tables;
import com.metsci.glimpse.dspl.util.DsplException;
import com.metsci.glimpse.dspl.util.DsplHelper;
import com.metsci.glimpse.util.io.StreamOpener;

public class DsplLiteHelper
{
    public static final String dsplLiteSchema = "com.metsci.glimpse.dspl.lite.schema";
    public static final String defaultTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS Z";
    public static final DateTimeFormatter defaultFormatter = DateTimeFormat.forPattern( defaultTimeFormat );

    public static DataSet loadNonCanonicalDataSet_xml_lite( DsplParser parser, String location ) throws JAXBException, IOException, DsplException
    {
        InputStream stream = StreamOpener.fileThenResource.openForRead( location );

        JAXBContext jc = JAXBContext.newInstance( dsplLiteSchema );
        Unmarshaller unmarshaller = jc.createUnmarshaller( );

        DsplLite lite_dataset = ( DsplLite ) unmarshaller.unmarshal( stream );

        DataSet dataset = loadNonCanonicalDataSet_xml_lite( lite_dataset, location );

        return linkDataset( parser, dataset, new java.io.File( location ) );
    }

    public static DataSet loadNonCanonicalDataSet_csv( DsplParser parser, String location ) throws JAXBException, IOException, DsplException
    {
        DsplLite dataset_lite = loadNonCanonicalDataSet_csv_lite( parser, location );
        DataSet dataset = loadNonCanonicalDataSet_xml_lite( dataset_lite, location );
        return linkDataset( parser, dataset, new java.io.File( location ) );
    }

    protected static DataSet loadNonCanonicalDataSet_xml_lite( DsplLite dspl_lite, String location ) throws JAXBException, IOException, DsplException
    {
        String namespace = dspl_lite.getTargetNamespace( );

        DataSet dataset = newDatasetWithAllImports( namespace );

        InputStream stream = StreamOpener.fileThenResource.openForRead( location );
        BufferedReader in = new BufferedReader( new InputStreamReader( stream ) );
        try
        {
            // create a Slice for the DataSet
            Slice slice = new Slice( );
            slice.setId( "default_slice" );
            SliceTableMapping mapping = new SliceTableMapping( );
            mapping.setRef( new QName( namespace, "default_table" ) );
            slice.setTableMapping( mapping );
            dataset.getSlices( ).getSlice( ).add( slice );

            // create a Table for the DataSet
            Table table = new Table( );
            dataset.setTables( new Tables( ) );
            table.setId( "default_table" );
            Data data = new Data( );
            File file = new File( );
            com.metsci.glimpse.dspl.lite.schema.File file_lite = dspl_lite.getFile( );
            file.setEncoding( file_lite.getEncoding( ) ); // only utf-8 is supported
            file.setFormat( file_lite.getFormat( ) ); // csv plus header information
            file.setValue( file_lite.getValue( ) );
            data.setFile( file );
            table.setData( data );
            dataset.getTables( ).getTable( ).add( table );

            // add a new Concept for each header line
            for ( Column column : dspl_lite.getColumns( ).getColumn( ) )
            {
                String header = column.getId( );

                Concept concept = new Concept( );
                concept.setId( header );
                concept.setDataSet( dataset );
                dataset.getConcepts( ).getConcept( ).add( concept );

                ConceptInfo info = new ConceptInfo( );
                concept.setInfo( info );

                boolean isMetric = true;
                String format = null;
                DataType type = null;

                // set the type of the concept (float,integer,string,date,long,concept)
                if ( column.getType( ) != null )
                {
                    type = DataType.fromValue( column.getType( ) );
                    Concept.Type typeElement = new Concept.Type( );
                    typeElement.setRef( type );
                    concept.setType( typeElement );
                }

                // set the units associated with the concept (this is set as an attribute of the concept)
                if ( column.getUnit( ) != null )
                {
                    Attribute attribute = new Attribute( );
                    attribute.setId( "unit" );
                    attribute.setParentConcept( concept );

                    Value valueElement = new Value( );
                    valueElement.setValue( column.getUnit( ) );
                    attribute.getValue( ).add( valueElement );

                    Attribute.Type typeElement = new Attribute.Type( );
                    typeElement.setRef( DataType.CONCEPT );
                    attribute.setType( typeElement );

                    attribute.setConceptRef( new QName( "http://www.metsci.com/dspl/physical_units", "physical_unit" ) );

                    concept.getAttribute( ).add( attribute );
                }

                // set the parent concept
                if ( column.getParent( ) != null )
                {
                    if ( !column.getParent( ).equals( "none" ) )
                    {
                        Pattern p = Pattern.compile( "\\{(.*)\\}(.*)" );
                        Matcher m = p.matcher( column.getParent( ) );

                        if ( m.matches( ) )
                        {
                            String parentNamespace = m.group( 1 );
                            String parentConcept = m.group( 2 );

                            concept.setExtends( new QName( parentNamespace, parentConcept ) );
                        }
                        else
                        {
                            throw new DsplException( "Failed to parse %s. Malformed parent parameter %s.", location, column.getParent( ) );
                        }
                    }
                }
                else
                {
                    // if no parent is specified, automatically assign one for certain types
                    switch ( type )
                    {
                        case INTEGER: concept.setExtends( new QName( "http://www.google.com/publicdata/dataset/google/quantity", "amount" ) );
                        case FLOAT: concept.setExtends( new QName( "http://www.google.com/publicdata/dataset/google/quantity", "magnitude" ) );
                        default: // do nothing
                    }
                }

                if ( column.getName( ) != null )
                {
                    info.setName( newValues( column.getName( ) ) );
                }

                if ( column.getDescription( ) != null )
                {
                    info.setDescription( newValues( column.getDescription( ) ) );
                }

                if ( column.getUrl( ) != null )
                {
                    info.setUrl( newValues( column.getUrl( ) ) );
                }

                if ( column.getFormat( ) != null )
                {
                    format = column.getFormat( );
                }

                isMetric = !column.isKey( );

                SliceConceptRef conceptRef = new SliceConceptRef( );
                conceptRef.setConceptRef( new QName( namespace, header ) );

                if ( isMetric )
                {
                    slice.getMetric( ).add( conceptRef );
                }
                else
                {
                    slice.getDimension( ).add( conceptRef );
                }

                com.metsci.glimpse.dspl.schema.Table.Column dspl_column;
                dspl_column = new com.metsci.glimpse.dspl.schema.Table.Column( );
                dspl_column.setId( header );
                dspl_column.setFormat( format );
                table.getColumn( ).add( dspl_column );
            }
            return dataset;
        }
        finally
        {
            in.close( );
        }
    }

    protected static DsplLite loadNonCanonicalDataSet_csv_lite( DsplParser parser, String location ) throws JAXBException, IOException, DsplException
    {
        InputStream stream = StreamOpener.fileThenResource.openForRead( location );
        BufferedReader in = new BufferedReader( new InputStreamReader( stream ) );
        try
        {
            DsplLite dspl_lite = new DsplLite( );
            dspl_lite.setColumns( new Columns( ) );

            dspl_lite.setTargetNamespace( location );

            com.metsci.glimpse.dspl.lite.schema.File file;
            file = new com.metsci.glimpse.dspl.lite.schema.File( );
            file.setValue( getFileName( location ) );
            dspl_lite.setFile( file );

            // read the header line
            String line = in.readLine( );
            String[] headers = line.split( "," );
            int size = headers.length;

            line = in.readLine( );
            String[] data = line.split( "," );
            String[] types = inferTypesFromData( data );

            // add a new Concept for each header line
            for ( int i = 0 ; i < size ; i++ )
            {
                String header = headers[i];
                String type = types[i];

                Column column = new Column( );
                column.setId( header );
                column.setType( type );
                if ( type.equals( "date" ) ) column.setFormat( defaultTimeFormat );

                dspl_lite.getColumns( ).getColumn( ).add( column );
            }

            return dspl_lite;
        }
        finally
        {
            in.close( );
        }
    }

    protected static String[] inferTypesFromData( String[] data )
    {
        int size = data.length;
        String[] types = new String[size];

        for ( int i = 0 ; i < size ; i++ )
        {
            types[i] = inferTypeFromData( data[i] );
        }

        return types;
    }

    protected static String inferTypeFromData( String data )
    {
        if ( data == null || data.isEmpty( ) )
            return "string";

        try
        {
            Float.parseFloat( data );
            return "float";
        }
        catch ( NumberFormatException e ) { }

        try
        {
            Integer.parseInt( data );
            return "integer";
        }
        catch ( NumberFormatException e ) { }

        // can't use Boolean.parseBoolean( data ) here because
        // it interprets everything which is not "true" as false
        if ( data.equals( "true" ) || data.equals( "false" ) )
        {
            return "boolean";
        }

        try
        {
            defaultFormatter.parseMillis( data );
            return "date";
        }
        catch ( IllegalArgumentException e ) { }

        return "string";
    }

    protected static String getFileName( String location )
    {
        try
        {
            URL url = DsplHelper.class.getClassLoader( ).getResource( location );
            if ( url != null )
            {
                String path = url.getFile( );
                int index = path.lastIndexOf( "/" );
                return path.substring( index + 1, path.length( ) );
            }
        }
        catch ( Exception e )
        {
            // do nothing, try loading as a local file
        }

        java.io.File file = new java.io.File( location );
        return file.getName( );
    }

    protected static DataSet newDatasetWithAllImports( String name )
    {
        //@formatter:off
        return newDataset( name, "http://www.google.com/publicdata/dataset/google/entity",
                                 "http://www.google.com/publicdata/dataset/google/geo",
                                 "http://www.google.com/publicdata/dataset/google/quantity",
                                 "http://www.google.com/publicdata/dataset/google/unit",
                                 "http://www.metsci.com/dspl/time",
                                 "http://www.metsci.com/dspl/physical_units",
                                 "http://www.metsci.com/dspl/track" );
        //@formatter:on
    }
}
