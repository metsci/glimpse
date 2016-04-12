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
package com.metsci.glimpse.examples.dspl;

import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.parser.table.SliceTableData;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.DataSet.Import;
import com.metsci.glimpse.dspl.schema.Info;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.SliceConceptRef;
import com.metsci.glimpse.dspl.schema.Value;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.util.logging.LoggerUtils;

/**
 * Glimpse provides Java support for Google's Data Set Publishing Language (DSPL -- http://code.google.com/apis/publicdata/).
 *
 * DsplExample demonstrates loading data and meta-data from a DSPL dataset.
 *
 * XXX: Needs cleanup.
 * @author ulman
 */
public class DsplExample implements GlimpseLayoutProvider
{
    public static final Logger logger = Logger.getLogger( DsplExample.class.getName( ) );

    public static void main( String[] args ) throws Exception
    {
        LoggerUtils.setTerseConsoleLogger( Level.INFO );
        Example.showWithSwing( new DsplExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        // dspl xml metadata and data files can be packaged into a single zip file or stored as separate files
        final String datasetString = "src/main/resources/dspl/lite/person_table.csv";
        //final String datasetString = "src/main/resources/dspl/lite/person_lite.xml";
        //final String datasetString = "src/main/resources/dspl/dataset.zip";
        //final String datasetString = "src/main/resources/dspl/dataset.xml";

        // create a dspl parser
        DsplParser parser = new DsplParser( );

        // tell the parser not to grab canonical concept files from the web
        parser.setNetworkMode( false );

        // tell the parser to created cached copies of the csv data files it loads in an efficient binary file format
        parser.setCacheMode( true );

        // load the example dataset
        DataSet dataset = parser.loadDataset( datasetString );

        Info info = dataset.getInfo( );

        if ( info != null )
        {
            // print out some values from the xml metadata
            logInfo( logger, "Dataset Name: %s", info.getName( ).getValue( ).get( 0 ).getValue( ) );
            logInfo( logger, "Dataset Description: %s", info.getDescription( ).getValue( ).get( 0 ).getValue( ) );
            logInfo( logger, "Dataset Url: %s", info.getUrl( ).getValue( ).get( 0 ).getValue( ) );

            // the need for the ".getValue( ).get( 0 ).getValue( )" boilerplate arises because
            // Info tags can have multiple names for different languages
            // for example, the following prints the value of the second dataset name, which happens to be in French
            if ( info.getName( ).getValue( ).size( ) > 1 )
            {
                Value value = dataset.getInfo( ).getName( ).getValue( ).get( 2 );
                logInfo( logger, "Second Dataset Name: %s (Language: %s)", value.getValue( ), value.getLang( ) );
            }

            // fortunately, JAXB allows us to override the classes which it generates to provide convenient
            // helper and extension methods. We've created a getNameEnglish which automatically chooses the English name
            logInfo( logger, "Dataset Name: %s", info.getNameEnglish( ) );
            logInfo( logger, "Dataset Description: %s", info.getDescriptionEnglish( ) );
            logInfo( logger, "Dataset Url: %s", info.getUrlEnglish( ) );
        }

        // iterate through the Imports in the DataSet
        List<Import> imports = dataset.getImport( );
        for ( Import importTag : imports )
        {
            logInfo( logger, "Import: %s %s", importTag.getLocation( ), importTag.getNamespace( ) );

            DataSet importedDataset = dataset.getDataSet( importTag );

            if ( importedDataset != null ) logInfo( logger, "Imported Dataset Name: %s", importedDataset.getInfo( ).getNameEnglish( ) );
        }

        // iterate through the Slices in the DataSet
        for ( Slice slice : dataset.getSlices( ).getSlice( ) )
        {
            logInfo( logger, "Slice Id: %s", slice.getId( ) );

            SliceTableData tableData = slice.getTableData( );

            for ( SliceConceptRef dimension : slice.getDimension( ) )
            {
                Concept concept = dimension.getConcept( );

                logInfo( logger, "Dimension Concept Ref: %s", concept.getInfo( ).getNameEnglish( ) );
                logDataDimension( tableData, concept );
            }

            for ( SliceConceptRef metric : slice.getMetric( ) )
            {
                Concept concept = metric.getConcept( );

                logInfo( logger, "Metric Concept Ref: %s", metric.getConcept( ) );
                logDataMetric( tableData, concept );
            }
        }

        return new GlimpseLayout( );
    }

    protected void logDataMetric( SliceTableData table, Concept concept )
    {
        logData( table.getMetricColumn( concept ), concept );
    }

    protected void logDataDimension( SliceTableData table, Concept concept )
    {
        logData( table.getDimensionColumn( concept ), concept );
    }

    protected void logData( TableColumn column, Concept concept )
    {
        switch ( concept.getType( ).getRef( ) )
        {
            case STRING:
                logInfo( logger, "String Data: %s", Arrays.toString( column.getStringData( ) ) );
                break;
            case FLOAT:
                logInfo( logger, "Float Data: %s", Arrays.toString( column.getFloatData( ) ) );
                break;
            case INTEGER:
                logInfo( logger, "Integer Data: %s", Arrays.toString( column.getIntegerData( ) ) );
                break;
            case BOOLEAN:
                logInfo( logger, "Boolean Data: %s", Arrays.toString( column.getBooleanData( ) ) );
                break;
            case DATE:
                logInfo( logger, "Date Data: %s", Arrays.toString( column.getDateData( ) ) );
                break;
            case CONCEPT:
                logInfo( logger, "Concept Data: %s", Arrays.toString( column.getStringData( ) ) );
                break;
            default:
                logInfo( logger, "Unrecognized Data Type" );
                break;
        }
    }
}
