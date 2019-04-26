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
package com.metsci.glimpse.dspl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import com.metsci.glimpse.dspl.parser.ColumnBinaryParser;
import com.metsci.glimpse.dspl.parser.CsvParser;
import com.metsci.glimpse.dspl.parser.ExtendedCsvParser;
import com.metsci.glimpse.dspl.parser.TableParser;
import com.metsci.glimpse.dspl.parser.WildcardCsvParser;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.DataSet.Import;
import com.metsci.glimpse.dspl.schema.Table;
import com.metsci.glimpse.dspl.util.DsplException;
import com.metsci.glimpse.dspl.util.DsplHelper;

public class DsplParser
{
    protected Map<String, TableParser> parsers;

    // cache of loaded data sets
    protected java.util.Map<String, DataSet> datasetCache;

    protected boolean failOnError = true;
    protected boolean noNetworkMode = true;
    protected boolean cacheMode = false;

    protected File cacheLocation = null;

    public DsplParser( )
    {
        datasetCache = new HashMap<String, DataSet>( );
        parsers = new HashMap<String, TableParser>( );
        parsers.put( "csv", new CsvParser( this ) );
        parsers.put( "csvx", new ExtendedCsvParser( this ) );
        parsers.put( "csv*", new WildcardCsvParser( this ) );
        parsers.put( "bin", new ColumnBinaryParser( ) );
    }

    public boolean isFailOnErrorMode( )
    {
        return failOnError;
    }

    public void setFailOnError( boolean mode )
    {
        failOnError = mode;
    }

    public boolean isCacheMode( )
    {
        return cacheMode;
    }

    public void setCacheMode( boolean mode )
    {
        cacheMode = mode;
    }

    public void setCacheDirectory( String directory )
    {
        cacheLocation = new File( directory );
    }

    public void setCacheDirectory( File directory )
    {
        cacheLocation = directory;
    }

    public File getCacheDirectory( )
    {
        return cacheLocation;
    }

    public void setNetworkMode( boolean mode )
    {
        noNetworkMode = !mode;
    }

    public boolean isNetworkMode( )
    {
        return !noNetworkMode;
    }

    public void addTableParser( String format, TableParser parser )
    {
        parsers.put( format, parser );
    }

    public TableParser getTableParser( String format )
    {
        return parsers.get( format );
    }

    public TableParser getTableParser( Table table )
    {
        String format = table.getData( ).getFile( ).getFormat( );
        return parsers.get( format );
    }

    public DataSet loadDataset( java.io.File file ) throws JAXBException, IOException, DsplException
    {
        return loadDataset( file.getAbsolutePath( ) );
    }

    public DataSet loadDataset( String location ) throws JAXBException, IOException, DsplException
    {
        return loadDataset( null, null, location );
    }

    public DataSet loadDataset( String namespace, String location ) throws JAXBException, IOException, DsplException
    {
        return loadDataset( null, namespace, location );
    }

    public DataSet loadDataset( DataSet parentDataSet, Import importTag ) throws JAXBException, IOException, DsplException
    {
        return loadDataset( parentDataSet, importTag.getNamespace( ), importTag.getLocation( ) );
    }

    public DataSet loadDataset( DataSet parentDataSet, String namespace, String location ) throws JAXBException, IOException, DsplException
    {
        if ( datasetCache.containsKey( namespace ) ) return datasetCache.get( namespace );

        DataSet dataset = DsplHelper.loadDataset( this, parentDataSet, namespace, location );

        if ( namespace != null ) datasetCache.put( namespace, dataset );

        return dataset;
    }

    public void cacheDataset( DataSet dataset )
    {
        if ( dataset.getTargetNamespace( ) != null ) datasetCache.put( dataset.getTargetNamespace( ), dataset );
    }

    public Concept getConcept( QName ref ) throws JAXBException, IOException, DsplException
    {
        return getConcept( ref.getNamespaceURI( ), ref.getLocalPart( ) );
    }

    public Concept getConcept( String namespace, String localId ) throws JAXBException, IOException, DsplException
    {
        DataSet dataset = datasetCache.get( namespace );

        if ( dataset != null )
        {
            return dataset.getConcept( localId );
        }
        else
        {
            // if the concept is a canonical one, we can load it
            dataset = loadDataset( null, namespace, null );

            if ( dataset != null )
            {
                return dataset.getConcept( localId );
            }
            else
            {
                return null;
            }
        }
    }

    public DataSet getCachedDataset( String namespace )
    {
        return datasetCache.get( namespace );
    }
}
