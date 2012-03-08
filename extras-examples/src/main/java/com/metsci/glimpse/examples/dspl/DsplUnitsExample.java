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
package com.metsci.glimpse.examples.dspl;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.xml.bind.JAXBException;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.canonical.Physical;
import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.Info;
import com.metsci.glimpse.dspl.schema.DataSet.Import;
import com.metsci.glimpse.dspl.util.DsplException;
import com.metsci.glimpse.util.logging.LoggerUtils;

import java.util.logging.Logger;

/**
 * XXX: Needs cleanup.
 *
 * @author osborn
 */
public class DsplUnitsExample
{
    public static final Logger logger = Logger.getLogger( DsplUnitsExample.class.getName( ) );

    public static void main( String[] args ) throws Exception
    {
        LoggerUtils.setTerseConsoleLogger( Level.INFO );
        loadDataset( );
    }

    protected static void loadDataset( ) throws Exception
    {
        // create a dspl parser
        DsplParser parser = new DsplParser( );

        // tell the parser not to grab canonical concept files from the web
        parser.setNetworkMode( false );

        // tell the parser not to created cached copies of the csv data files
        parser.setCacheMode( false );

        // load the example dataset
        DataSet dataset = parser.loadDataset( Physical.datasetPath );

        Info info = dataset.getInfo( );

        // print out some values from the xml metadata
        logInfo( logger,  "Dataset Name: %s", info.getNameEnglish( ) );
        logInfo( logger,  "Dataset Description: %s", info.getDescriptionEnglish( ) );
        logInfo( logger,  "Dataset Url: %s", info.getUrlEnglish( ) );

        // iterate through the imports in the DataSet
        List<Import> imports = dataset.getImport( );
        for ( Import importTag : imports )
        {
            logInfo( logger,  "Import: %s %s", importTag.getLocation( ), importTag.getNamespace( ) );

            DataSet importedDataset = dataset.getDataSet( importTag );

            if ( importedDataset != null ) logInfo( logger,  "Imported Dataset Name: %s", importedDataset.getInfo( ).getNameEnglish( ) );
        }

        printPhysicalUnitSystems( dataset );
        printPhysicalProperties( dataset );
        printPhysicalUnits( dataset );
    }

    protected static void printPhysicalProperties( DataSet dataset ) throws DsplException, IOException, JAXBException
    {
        Concept concept = Physical.PhysicalPropertyConcept.get( dataset );
        TableColumn values = Physical.PhysicalPropertyConcept.getValues( concept );
        TableColumn text = Physical.PhysicalPropertyConcept.getPropretyText( concept );

        for ( int i = 0; i < values.getSize( ); i++ )
            System.out.println( values.getStringData( i ) + "  " + text.getStringData( i ) );
    }

    protected static void printPhysicalUnits( DataSet dataset ) throws DsplException, IOException, JAXBException
    {
        Concept concept = Physical.PhysicalUnitConcept.get( dataset );
        TableColumn values = Physical.PhysicalUnitConcept.getValues( concept );
        TableColumn text = Physical.PhysicalUnitConcept.getUnitText( concept );

        for ( int i = 0; i < values.getSize( ); i++ )
            System.out.println( values.getStringData( i ) + "  " + text.getStringData( i ) );
    }

    protected static void printPhysicalUnitSystems( DataSet dataset ) throws DsplException, IOException, JAXBException
    {
        Concept concept = Physical.PhysicalUnitSystemConcept.get( dataset );
        TableColumn values = Physical.PhysicalUnitSystemConcept.getValues( concept );
        TableColumn text = Physical.PhysicalUnitSystemConcept.getUnitSystemText( concept );

        for ( int i = 0; i < values.getSize( ); i++ )
            System.out.println( values.getStringData( i ) + "  " + text.getStringData( i ) );
    }
}
