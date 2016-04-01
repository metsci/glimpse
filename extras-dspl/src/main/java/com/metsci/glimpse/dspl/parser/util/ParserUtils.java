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
package com.metsci.glimpse.dspl.parser.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.metsci.glimpse.dspl.parser.column.ConstantTableColumn;
import com.metsci.glimpse.dspl.parser.column.SimpleTableColumn;
import com.metsci.glimpse.dspl.parser.column.SliceColumnType;
import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.parser.table.SimplePropertyTableData;
import com.metsci.glimpse.dspl.parser.table.SimpleSliceTableData;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.ConceptProperty;
import com.metsci.glimpse.dspl.schema.ConceptTableMapping;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.SliceConceptRef;
import com.metsci.glimpse.dspl.schema.SliceTableMapping;
import com.metsci.glimpse.dspl.schema.Table;
import com.metsci.glimpse.dspl.schema.ConceptTableMapping.MapProperty;
import com.metsci.glimpse.dspl.schema.SliceTableMapping.MapDimension;
import com.metsci.glimpse.dspl.schema.SliceTableMapping.MapMetric;
import com.metsci.glimpse.dspl.schema.Table.Column;
import com.metsci.glimpse.dspl.util.DsplException;
import com.metsci.glimpse.util.Pair;
import com.metsci.glimpse.util.primitives.BooleansArray;
import com.metsci.glimpse.util.primitives.FloatsArray;
import com.metsci.glimpse.util.primitives.IntsArray;
import com.metsci.glimpse.util.primitives.LongsArray;

public class ParserUtils
{
    public static SimplePropertyTableData buildPropertyTableData( Concept concept, TableParserInfo info, ParserFactory factory ) throws IOException, DsplException, JAXBException
    {
        TableColumnParser[] parsers = info.getParsers( );
        Concept[] concepts = info.getConcepts( );
        Column[] columns = info.getColumns( );
        DataType[] types = info.getDataTypes( );

        Map<String, TableColumn> map = new HashMap<String, TableColumn>( );
        for ( int i = 0; i < parsers.length; i++ )
        {
            Concept columnConcept = concepts[i];
            Column column = columns[i];
            TableColumnParser parser = parsers[i];
            DataType type = types[i];

            map.put( column.getId( ), parser.createTableColumn( column, columnConcept, type ) );
        }

        int size = 1;
        if ( !map.isEmpty( ) )
        {
            size = map.values( ).iterator( ).next( ).getSize( );
        }

        map.putAll( getConstantTableColumns( concept, info, factory, size ) );

        return new SimplePropertyTableData( concept, map );
    }

    public static SimpleSliceTableData buildSliceTableData( Slice slice, TableParserInfo info, ParserFactory factory ) throws IOException, DsplException, JAXBException
    {
        TableColumnParser[] parsers = info.getParsers( );
        Concept[] concepts = info.getConcepts( );
        Column[] columns = info.getColumns( );
        DataType[] types = info.getDataTypes( );
        SliceColumnType[] sliceColumnTypes = info.getSliceColumnTypes( );

        Map<String, TableColumn> dimensionMap = new HashMap<String, TableColumn>( );
        Map<String, TableColumn> metricMap = new HashMap<String, TableColumn>( );
        for ( int i = 0; i < parsers.length; i++ )
        {
            Concept columnConcept = concepts[i];
            Column column = columns[i];
            TableColumnParser parser = parsers[i];
            DataType type = types[i];
            SliceColumnType sliceType = sliceColumnTypes[i];

            Map<String, TableColumn> map = null;

            switch ( sliceType )
            {
            case Dimension:
                map = dimensionMap;
                break;
            case Metric:
                map = metricMap;
                break;
            }

            map.put( column.getId( ), parser.createTableColumn( column, columnConcept, type ) );
        }

        int size = 1;
        if ( !dimensionMap.isEmpty( ) )
        {
            size = dimensionMap.values( ).iterator( ).next( ).getSize( );
        }
        else if ( !metricMap.isEmpty( ) )
        {
            size = metricMap.values( ).iterator( ).next( ).getSize( );
        }

        dimensionMap.putAll( getConstantTableColumns( slice, info, factory, SliceColumnType.Dimension, size ) );
        metricMap.putAll( getConstantTableColumns( slice, info, factory, SliceColumnType.Metric, size ) );

        return new SimpleSliceTableData( slice, dimensionMap, metricMap );
    }

    public static Map<String, TableColumn> getConstantTableColumns( Slice slice, TableParserInfo info, ParserFactory factory, SliceColumnType columnType, int size ) throws JAXBException, IOException, DsplException
    {
        Map<String, TableColumn> constantColumns = new HashMap<String, TableColumn>( );

        Table table = slice.getTable( );

        for ( Column column : table.getColumn( ) )
        {
            if ( column.getValue( ) == null ) continue;

            if ( containsColumn( info, column ) ) throw new DsplException( "Column %s cannot both have a constant value (%s) and a column heading in its csv data file.", column.getId( ), column.getValue( ) );

            Pair<Concept, SliceColumnType> pair = getConcept( column.getId( ), slice );

            if ( columnType != pair.second( ) ) continue;

            Concept concept = pair.first( );

            DataType type = getType( concept, column );

            TableColumnParser parser = factory.getParser( column, type );

            constantColumns.put( column.getId( ), new ConstantTableColumn( column, concept, type, parser.parse( column.getValue( ) ), size ) );
        }

        return constantColumns;
    }

    public static Map<String, TableColumn> getConstantTableColumns( Concept parent, TableParserInfo info, ParserFactory factory, int size ) throws JAXBException, IOException, DsplException
    {
        Map<String, TableColumn> constantColumns = new HashMap<String, TableColumn>( );

        Table table = parent.getTable( );

        for ( Column column : table.getColumn( ) )
        {
            if ( column.getValue( ) == null ) continue;

            if ( containsColumn( info, column ) ) throw new DsplException( "Column %s cannot both have a constant value (%s) and a column heading in its csv data file.", column.getId( ), column.getValue( ) );

            Concept concept = getConcept( column.getId( ), parent );

            DataType type = getType( concept, column );

            TableColumnParser parser = factory.getParser( column, type );

            constantColumns.put( column.getId( ), new ConstantTableColumn( column, concept, type, parser.parse( column.getValue( ) ), size ) );
        }

        return constantColumns;
    }

    public static boolean containsColumn( TableParserInfo info, Column searchColumn )
    {
        for ( Column column : info.getColumns( ) )
        {
            if ( searchColumn.getId( ).equals( column.getId( ) ) )
            {
                return true;
            }
        }

        return false;
    }

    public static class TableParserInfo
    {
        protected String[] columnIds = null;
        protected Concept[] concepts = null;
        protected SliceColumnType[] sliceColumnTypes = null;
        protected Column[] columns = null;
        protected DataType[] types = null;
        protected TableColumnParser[] parsers = null;

        public TableParserInfo( String[] columnIds, Concept[] concepts, SliceColumnType[] sliceColumnTypes, Column[] columns, DataType[] types, TableColumnParser[] parsers )
        {
            super( );
            this.columnIds = columnIds;
            this.concepts = concepts;
            this.sliceColumnTypes = sliceColumnTypes;
            this.columns = columns;
            this.types = types;
            this.parsers = parsers;
        }

        public String[] getColumnIds( )
        {
            return columnIds;
        }

        public Concept[] getConcepts( )
        {
            return concepts;
        }

        public SliceColumnType[] getSliceColumnTypes( )
        {
            return sliceColumnTypes;
        }

        public Column[] getColumns( )
        {
            return columns;
        }

        public DataType[] getDataTypes( )
        {
            return types;
        }

        public TableColumnParser[] getParsers( )
        {
            return parsers;
        }
    }

    public static interface ParserFactory
    {
        public TableColumnParser getParser( Column column, DataType type ) throws DsplException;
    }

    public static interface TableColumnParser
    {
        public TableColumn createTableColumn( Column column, Concept concept, DataType type );

        // all parsers need to be able to parse string representations of their data
        // because default values may be specified that way in the csv file
        public Object parse( String data ) throws DsplException;
    }

    public static class SimpleParserFactory implements ParserFactory
    {
        public TableColumnParser getParser( Column column, DataType type ) throws DsplException
        {
            if ( type == null )
            {
                throw new DsplException( "No Type provided for Column %s.", column.getId( ) );
            }

            TableColumnParser parser = null;

            switch ( type )
            {
            case STRING:
                parser = newStringParser( column );
                break;
            case FLOAT:
                parser = newFloatParser( column );
                break;
            case INTEGER:
                parser = newIntegerParser( column );
                break;
            case BOOLEAN:
                parser = newBooleanParser( column );
                break;
            case DATE:
                parser = newDateParser( column );
                break;
            case CONCEPT:
                parser = newConceptParser( column );
                break;
            default:
                throw new DsplException( "Unknown Type %s provided for Column %s.", type, column.getId( ) );
            }

            return parser;
        }

        public TableColumnParser newConceptParser( Column column )
        {
            return new StringColumnParser( );
        }

        public TableColumnParser newDateParser( Column column )
        {
            return new DateColumnParser( column );
        }

        public TableColumnParser newBooleanParser( Column column )
        {
            return new BooleanColumnParser( );
        }

        public TableColumnParser newIntegerParser( Column column )
        {
            return new IntegerColumnParser( );
        }

        public TableColumnParser newFloatParser( Column column )
        {
            return new FloatColumnParser( );
        }

        public TableColumnParser newStringParser( Column column )
        {
            return new StringColumnParser( );
        }
    }

    public static class StringColumnParser implements TableColumnParser
    {
        protected List<String> data;

        public StringColumnParser( )
        {
            this.data = new ArrayList<String>( );
        }

        @Override
        public TableColumn createTableColumn( Column column, Concept concept, DataType type )
        {
            String[] array = data.toArray( new String[data.size( )] );
            return new SimpleTableColumn( column, concept, type, array, array.length );
        }

        @Override
        public String parse( String data )
        {
            return new String( data );
        }
    }

    public static class IntegerColumnParser implements TableColumnParser
    {
        protected IntsArray data;

        public IntegerColumnParser( )
        {
            this.data = new IntsArray( new int[10], 0 );
        }

        @Override
        public TableColumn createTableColumn( Column column, Concept concept, DataType type )
        {
            int n = data.n;
            int[] array = new int[n];
            data.copyTo( 0, array, 0, n );
            return new SimpleTableColumn( column, concept, type, array, n );
        }

        @Override
        public Integer parse( String data )
        {
            return Integer.parseInt( data );
        }
    }

    public static class FloatColumnParser implements TableColumnParser
    {
        protected FloatsArray data;

        public FloatColumnParser( )
        {
            this.data = new FloatsArray( new float[10], 0 );
        }

        @Override
        public TableColumn createTableColumn( Column column, Concept concept, DataType type )
        {
            int n = data.n;
            float[] array = new float[n];
            data.copyTo( 0, array, 0, n );
            return new SimpleTableColumn( column, concept, type, array, n );
        }

        @Override
        public Float parse( String data )
        {
            return Float.parseFloat( data );
        }
    }

    public static class BooleanColumnParser implements TableColumnParser
    {
        protected BooleansArray data;

        public BooleanColumnParser( )
        {
            this.data = new BooleansArray( new boolean[10], 0 );
        }

        @Override
        public TableColumn createTableColumn( Column column, Concept concept, DataType type )
        {
            int n = data.n;
            boolean[] array = new boolean[n];
            data.copyTo( 0, array, 0, n );
            return new SimpleTableColumn( column, concept, type, array, n );
        }

        @Override
        public Boolean parse( String data )
        {
            return Boolean.parseBoolean( data );
        }
    }

    public static class DateColumnParser implements TableColumnParser
    {
        protected LongsArray data;
        protected String format;
        protected DateTimeFormatter dateFormat;

        public DateColumnParser( Column column )
        {
            this.data = new LongsArray( new long[10], 0 );
            this.format = column.getFormat( );
            this.dateFormat = DateTimeFormat.forPattern( format );
        }

        @Override
        public TableColumn createTableColumn( Column column, Concept concept, DataType type )
        {
            int n = data.n;
            long[] array = new long[n];
            data.copyTo( 0, array, 0, n );
            return new SimpleTableColumn( column, concept, type, array, n );
        }

        @Override
        public Long parse( String data ) throws DsplException
        {
            return dateFormat.parseMillis( data );
        }
    }

    // convert the header of the csv table file into concepts
    // normally the header strings are simply interpreted as concept ids
    // however the ConceptTableMapping can provide different names
    public static Concept[] getConcepts( String[] columnIds, Concept parent ) throws JAXBException, IOException, DsplException
    {
        int size = columnIds.length;

        Concept[] concepts = new Concept[size];

        for ( int i = 0; i < size; i++ )
        {
            concepts[i] = getConcept( columnIds[i], parent );
        }

        return concepts;
    }

    public static Concept getConcept( String columnId, Concept concept ) throws JAXBException, IOException, DsplException
    {
        ConceptTableMapping mapping = concept.getTableMapping( );

        // first check whether the current column is the value of the concept
        // i.e. the primary key for the concept
        if ( mapping.getMapConcept( ) != null )
        {
            if ( columnId.equals( mapping.getMapConcept( ).getToColumn( ) ) )
            {
                return concept;
            }
        }

        // check whether the column id equals the concept id
        if ( concept.getId( ).equals( columnId ) )
        {
            return concept;
        }

        // check whether any concept mappings match the column id
        if ( mapping.getMapProperty( ) != null )
        {
            Concept columnConcept = getMappedConcept( columnId, concept );
            if ( columnConcept != null )
            {
                return columnConcept;
            }
        }

        // check whether a property of the concept matches the column id
        ConceptProperty conceptProperty = concept.getProperty( columnId );
        if ( conceptProperty != null )
        {
            Concept columnConcept = conceptProperty.getConcept( );
            if ( columnConcept != null )
            {
                return columnConcept;
            }
        }

        // not every column needs a concept associated with it (although its a good idea)
        // however if it doesn't have an associated column it needs a type
        return null;
    }

    public static void getConcepts( String[] columnIds, Slice slice, Concept[] concepts, SliceColumnType[] sliceColumnTypes ) throws JAXBException, IOException, DsplException
    {
        int size = columnIds.length;

        for ( int i = 0; i < size; i++ )
        {
            Pair<Concept, SliceColumnType> pair = getConcept( columnIds[i], slice );

            concepts[i] = pair.first( );
            sliceColumnTypes[i] = pair.second( );
        }
    }

    public static Pair<Concept, SliceColumnType> getConcept( String columnId, Slice slice ) throws JAXBException, IOException, DsplException
    {
        SliceTableMapping mapping = slice.getTableMapping( );

        // check whether any dimension mappings match the column id
        if ( mapping != null && mapping.getMapDimension( ) != null )
        {
            Concept columnConcept = getMappedDimension( columnId, slice );
            if ( columnConcept != null )
            {
                return new Pair<Concept, SliceColumnType>( columnConcept, SliceColumnType.Dimension );
            }
        }

        // check whether any metric mappings match the column id
        if ( mapping != null && mapping.getMapMetric( ) != null )
        {
            Concept columnConcept = getMappedMetric( columnId, slice );
            if ( columnConcept != null )
            {
                return new Pair<Concept, SliceColumnType>( columnConcept, SliceColumnType.Metric );
            }
        }

        Concept dimensionConcept = getSliceConceptRef( columnId, slice, slice.getDimension( ) );
        if ( dimensionConcept != null )
        {
            return new Pair<Concept, SliceColumnType>( dimensionConcept, SliceColumnType.Dimension );
        }

        Concept metricConcept = getSliceConceptRef( columnId, slice, slice.getMetric( ) );
        if ( metricConcept != null )
        {
            return new Pair<Concept, SliceColumnType>( metricConcept, SliceColumnType.Metric );
        }

        // every column needs to be identified as either a dimension or a metric
        throw new DsplException( "Column %s is not present in Slice %s as a Dimension or Metric.", columnId, slice.getId( ) );
    }

    public static Concept getSliceConceptRef( String id, Slice slice, List<SliceConceptRef> list ) throws JAXBException, IOException, DsplException
    {
        for ( SliceConceptRef concept : list )
        {
            if ( id.equals( concept.getConceptRef( ).getLocalPart( ) ) )
            {
                return concept.getConcept( );
            }
        }

        return null;
    }

    public static Concept getMappedMetric( String id, Slice slice ) throws JAXBException, IOException, DsplException
    {
        SliceTableMapping mapping = slice.getTableMapping( );
        DataSet dataset = slice.getDataSet( );

        if ( mapping != null )
        {
            for ( MapMetric mapmetric : mapping.getMapMetric( ) )
            {
                if ( id.equals( mapmetric.getToColumn( ) ) )
                {
                    return dataset.getConcept( mapmetric.getConcept( ) );
                }
            }
        }

        return null;
    }

    public static Concept getMappedDimension( String id, Slice slice ) throws JAXBException, IOException, DsplException
    {
        SliceTableMapping mapping = slice.getTableMapping( );
        DataSet dataset = slice.getDataSet( );

        if ( mapping != null )
        {
            for ( MapDimension mapDimension : mapping.getMapDimension( ) )
            {
                if ( id.equals( mapDimension.getToColumn( ) ) )
                {
                    return dataset.getConcept( mapDimension.getConcept( ) );
                }
            }
        }

        return null;
    }

    public static Concept getMappedConcept( String id, Concept concept ) throws JAXBException, IOException, DsplException
    {
        ConceptTableMapping mapping = concept.getTableMapping( );

        if ( mapping != null )
        {
            for ( MapProperty mapProperty : mapping.getMapProperty( ) )
            {
                if ( id.equals( mapProperty.getToColumn( ) ) )
                {
                    ConceptProperty property = concept.getProperty( id );
                    return property.getConcept( );
                }
            }
        }

        return null;
    }

    public static String getMappedMetricColumn( Slice slice, Concept columnConcept )
    {
        SliceTableMapping mapping = slice.getTableMapping( );

        if ( mapping != null )
        {
            for ( MapMetric mapMetric : mapping.getMapMetric( ) )
            {
                if ( columnConcept.getId( ).equals( mapMetric.getConcept( ).getLocalPart( ) ) ) return mapMetric.getToColumn( );
            }
        }

        return columnConcept.getId( );
    }

    public static String getMappedDimensionColumn( Slice slice, Concept columnConcept )
    {
        SliceTableMapping mapping = slice.getTableMapping( );

        if ( mapping != null )
        {
            for ( MapDimension mapDimension : mapping.getMapDimension( ) )
            {
                if ( columnConcept.getId( ).equals( mapDimension.getConcept( ).getLocalPart( ) ) ) return mapDimension.getToColumn( );
            }
        }

        return columnConcept.getId( );
    }

    public static String getMappedColumn( Concept parentConcept, Concept columnConcept )
    {
        ConceptTableMapping mapping = parentConcept.getTableMapping( );

        if ( mapping != null )
        {
            for ( MapProperty mapProperty : mapping.getMapProperty( ) )
            {
                if ( columnConcept.getId( ).equals( mapProperty.getRef( ) ) ) return mapProperty.getToColumn( );
            }
        }

        return columnConcept.getId( );
    }

    public static Column[] getColumns( String[] columnIds, Table table ) throws DsplException
    {
        int size = columnIds.length;
        Column[] columns = new Column[size];

        for ( int i = 0; i < size; i++ )
        {
            String columnId = columnIds[i];

            for ( Column column : table.getColumn( ) )
            {
                if ( columnId.equals( column.getId( ) ) )
                {
                    columns[i] = column;
                    break;
                }
            }
        }

        return columns;
    }

    public static DataType getType( Concept concept, Column column ) throws DsplException
    {
        if ( concept == null )
        {
            if ( column.getType( ) != null )
            {
                return column.getType( );
            }
            else
            {
                throw new DsplException( "Column %s has no Concept and does not define a Type.", column.getId( ) );
            }
        }
        else
        {
            if ( column.getType( ) == null )
            {
                return concept.getType( ).getRef( );
            }
            else if ( column.getType( ) == concept.getType( ).getRef( ) )
            {
                return column.getType( );
            }
            else
            {
                throw new DsplException( "Column %s and its assoicated Concept %s have conflicting types (%s and %s).", column.getId( ), concept.getId( ), concept.getType( ).getRef( ), column.getType( ) );
            }
        }
    }

    public static DataType[] getTypes( Concept[] concepts, Column[] columns ) throws DsplException
    {
        int size = concepts.length;
        DataType[] types = new DataType[size];

        for ( int i = 0; i < size; i++ )
        {
            Concept concept = concepts[i];
            Column column = columns[i];

            types[i] = getType( concept, column );
        }

        return types;
    }
}
