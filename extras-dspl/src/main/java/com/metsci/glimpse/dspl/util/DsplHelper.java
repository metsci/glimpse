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
package com.metsci.glimpse.dspl.util;

import static com.metsci.glimpse.dspl.lite.DsplLiteHelper.*;
import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.parser.TableParser;
import com.metsci.glimpse.dspl.parser.table.PropertyTableData;
import com.metsci.glimpse.dspl.parser.table.SliceTableData;
import com.metsci.glimpse.dspl.parser.util.DsplCacheHelper;
import com.metsci.glimpse.dspl.schema.Attribute;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.ConceptProperty;
import com.metsci.glimpse.dspl.schema.Data;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.SliceConceptRef;
import com.metsci.glimpse.dspl.schema.Table;
import com.metsci.glimpse.dspl.schema.Value;
import com.metsci.glimpse.dspl.schema.Data.File;
import com.metsci.glimpse.dspl.schema.DataSet.Import;
import com.metsci.glimpse.util.io.StreamOpener;

import java.util.logging.Logger;

public class DsplHelper
{
    public static final Logger logger = Logger.getLogger( DsplHelper.class.getName( ) );

    public static final String defaultDsplNamespace = "http://schemas.google.com/dspl/2010";
    public static final String canonicalGoogleNamespace = "http://www.google.com/publicdata/dataset/google/";
    public static final String canonicalGoogleUrlBase = "http://dspl.googlecode.com/hg/datasets/google/canonical/";
    public static final String canonicalGoogleLocalBase = "dspl/canonical/google/";

    public static final String canonicalMetronNamespace = "http://www.metsci.com/dspl/";
    public static final String canonicalMetronLocalBase = "dspl/canonical/metron/";

    public static final String dsplSchema = "com.metsci.glimpse.dspl.schema";
    public static final String objectFactoryProp = "com.sun.xml.bind.ObjectFactory";
    
    private static JAXBContext jc;

    static
    {
        try
        {
            jc = JAXBContext.newInstance( dsplSchema );
        }
        catch ( JAXBException e )
        {
            logWarning( logger, "Unable to initialize JAXB Context", e );
        }
    }
    
    ///////////////////////////////////////////////////////////////////////
    //////                 DataSet utility methods                   //////
    ///////////////////////////////////////////////////////////////////////

    public static DataSet loadDataset( DsplParser parser, DataSet parent, String namespace, String location ) throws JAXBException, IOException, DsplException
    {
        if ( namespace != null && namespace.startsWith( canonicalGoogleNamespace ) )
        {
            return loadCanonicalDataset( parser, namespace, canonicalGoogleLocalBase, canonicalGoogleUrlBase );
        }
        else if ( namespace != null && namespace.startsWith( canonicalMetronNamespace ) )
        {
            return loadCanonicalDataset( parser, namespace, canonicalMetronLocalBase, null );
        }
        else
        {
            return loadNonCanonicalDataSet( parser, parent, namespace, location );
        }
    }

    public static DataSet loadNonCanonicalDataSet( DsplParser parser, DataSet parent, String namespace, String location ) throws JAXBException, IOException, DsplException
    {
        if ( location.endsWith( ".xml" ) )
        {
            try
            {
                return loadNonCanonicalDataSet_xml( parser, parent, namespace, location );
            }
            catch ( JAXBException e )
            {
                // if we fail to interpret the xml document using the dspl schema, try dspl-lite
                return loadNonCanonicalDataSet_xml_lite( parser, location );
            }
        }
        else if ( location.endsWith( ".zip" ) )
        {
            return loadNonCanonicalDataSet_zip( parser, parent, namespace, location );
        }
        else if ( location.endsWith( ".csv" ) )
        {
            return loadNonCanonicalDataSet_csv( parser, location );
        }
        else
        {
            throw new DsplException( "DataSet location must point to either xml or zip file. Provided location is invalid: %s", location );
        }
    }

    protected static DataSet loadNonCanonicalDataSet_xml( DsplParser parser, DataSet parent, String namespace, String location ) throws JAXBException, IOException, DsplException
    {
        // try loading the file using the name as is (as a file or classpath resource)
        try
        {
            InputStream stream = StreamOpener.fileThenResource.openForRead( location );
            return loadDataset( parser, stream, new java.io.File( location ) );
        }
        catch ( IOException e )
        {
            logFine( logger, "Unable to load relative path: %s. Trying absolute path.", e, location );
        }

        if ( parent != null && parent.getFile( ) != null )
        {
            // try loading the file by appending the parent DataSet url
            java.io.File dataSetFile = parent.getFile( );
            java.io.File dataSetParent = dataSetFile.getParentFile( );
            String dataSetPath = new java.io.File( dataSetParent, location ).getPath( );
            InputStream stream = StreamOpener.fileThenResource.openForRead( dataSetPath );
            return loadDataset( parser, stream, new java.io.File( dataSetPath ) );
        }
        else
        {
            throw new DsplException( "Unable to load data set namespace %s location %s because absolute path was not provided and no parent namespace exists.", namespace, location );
        }
    }

    protected static DataSet loadNonCanonicalDataSet_zip( DsplParser parser, DataSet parent, String namespace, String location ) throws JAXBException, IOException, DsplException
    {
        try
        {
            java.io.File file = new java.io.File( location );
            URL metadata = new URL( String.format( "jar:file:%s!/metadata.xml", file.getAbsolutePath( ) ) );
            InputStream stream = metadata.openStream( );
            return loadDataset( parser, stream, new java.io.File( location ) );
        }
        catch ( IOException e )
        {
            logFine( logger, "Unable to load relative path: %s. Trying absolute path.", e, location );
        }

        URL resource = parser.getClass( ).getClassLoader( ).getResource( location );
        if ( resource != null && resource.getProtocol( ).equals( "file" ) )
        {
            InputStream stream = getInputStreamFromZipFile( resource.getFile( ), "metadata.xml" );
            return loadDataset( parser, stream, new java.io.File( resource.getFile( ) ) );
        }
        else if ( parent != null && parent.getFile( ) != null )
        {
            // try loading the file by appending the parent DataSet url
            java.io.File dataSetFile = parent.getFile( );
            java.io.File dataSetParent = dataSetFile.getParentFile( );
            String dataSetPath = new java.io.File( dataSetParent, location ).getPath( );
            ZipFile zipFile = new ZipFile( dataSetPath );
            ZipEntry metadataEntry = zipFile.getEntry( "metadata.xml" );
            InputStream stream = zipFile.getInputStream( metadataEntry );
            return loadDataset( parser, stream, new java.io.File( dataSetPath ) );
        }
        else
        {
            throw new DsplException( "Unable to load data set namespace %s location %s because absolute path was not provided and no parent namespace exists.", namespace, location );
        }
    }

    protected static InputStream getInputStreamFromZipFile( String zipFilePath, String resourcePath ) throws IOException
    {
        URL metadata = new URL( String.format( "jar:file:%s!/%s", zipFilePath, resourcePath ) );
        return metadata.openStream( );
    }

    public static DataSet loadLocalCanonicalDataset( DsplParser parser, String namespace, String localBase ) throws JAXBException, IOException, DsplException
    {
        InputStream stream = StreamOpener.fileThenResource.openForRead( localBase + getName( namespace ) + ".xml" );

        DataSet canonicalDataset = loadDataset( parser, stream, null );

        return canonicalDataset;
    }

    public static DataSet loadNetworkCanonicalDataset( DsplParser parser, String namespace, String urlBase ) throws JAXBException, IOException, DsplException
    {
        URL canonicalUrl = new URL( urlBase + getName( namespace ) + ".xml" );

        DataSet canonicalDataset = loadDataset( parser, canonicalUrl.openStream( ), null );

        return canonicalDataset;
    }

    public static DataSet loadCanonicalDataset( DsplParser parser, String namespace, String localBase, String urlBase ) throws JAXBException, IOException, DsplException
    {
        if ( parser.isNetworkMode( ) )
        {
            try
            {
                return loadLocalCanonicalDataset( parser, namespace, localBase );
            }
            catch ( IOException e )
            {
                return loadNetworkCanonicalDataset( parser, namespace, urlBase );
            }
        }
        else
        {
            return loadLocalCanonicalDataset( parser, namespace, localBase );
        }
    }

    public static DataSet loadDataset( DsplParser parser, InputStream stream, java.io.File base ) throws JAXBException, IOException, DsplException
    {
        try
        {
            Unmarshaller unmarshaller = jc.createUnmarshaller( );

            DataSet dataset = ( DataSet ) unmarshaller.unmarshal( stream );

            return linkDataset( parser, dataset, base );
        }
        finally
        {
            stream.close( );
        }
    }

    public static DataSet linkDataset( DsplParser parser, DataSet dataset ) throws JAXBException, IOException, DsplException
    {
        return linkDataset( parser, dataset, null );
    }

    public static DataSet linkDataset( DsplParser parser, DataSet dataset, java.io.File base ) throws JAXBException, IOException, DsplException
    {
        dataset.setFile( base );
        dataset.setParser( parser );

        loadImportedDataSets( parser, dataset );
        resolveConceptRefs( dataset );
        resolveDataSetLinks( dataset );
        resolveConceptIds( dataset );
        resolveConceptTypes( dataset );
        checkConceptReferences( dataset );
        resolveConceptExtension( dataset );

        parser.cacheDataset( dataset );

        return dataset;
    }

    // make sure that we've loaded all datasets imported by this data set
    public static void loadImportedDataSets( DsplParser parser, DataSet dataset ) throws JAXBException, IOException, DsplException
    {
        for ( Import imp : dataset.getImport( ) )
        {
            dataset.getDataSet( imp );
        }
    }

    ///////////////////////////////////////////////////////////////////////
    //////                  Table utility methods                    //////
    ///////////////////////////////////////////////////////////////////////

    public static SliceTableData getTableData( Slice slice ) throws IOException, JAXBException, DsplException
    {
        if ( slice == null ) return null;

        DataSet dataset = slice.getDataSet( );

        if ( dataset == null ) return null;

        Table table = slice.getTable( );
        if ( table == null ) return null;

        DsplParser dsplParser = dataset.getParser( );
        if ( dsplParser == null ) return null;

        TableParser parser = dsplParser.getTableParser( table );
        if ( parser == null ) return null;

        if ( dsplParser.isCacheMode( ) )
        {
            return DsplCacheHelper.getTableData( slice );
        }
        else
        {
            return parser.parse( slice );
        }
    }

    public static PropertyTableData getTableData( Concept concept ) throws IOException, JAXBException, DsplException
    {
        if ( concept == null ) return null;

        DataSet dataset = concept.getDataSet( );

        if ( dataset == null ) return null;

        Table table = concept.getTable( );
        if ( table == null ) return null;

        DsplParser dsplParser = dataset.getParser( );
        if ( dsplParser == null ) return null;

        TableParser parser = dsplParser.getTableParser( table );
        if ( parser == null ) return null;

        if ( dsplParser.isCacheMode( ) )
        {
            return DsplCacheHelper.getTableData( concept );
        }
        else
        {
            return parser.parse( concept );
        }
    }

    public static InputStream getTableInputStream( Concept concept ) throws IOException, JAXBException, DsplException
    {
        return getTableInputStream( concept.getTable( ) );
    }

    public static InputStream getTableInputStream( Slice slice ) throws IOException, JAXBException, DsplException
    {
        return getTableInputStream( slice.getTable( ) );
    }

    public static Table getTable( Slice slice ) throws javax.xml.bind.JAXBException, java.io.IOException, DsplException
    {
        if ( slice == null || slice.getDataSet( ) == null || slice.getTableMapping( ) == null ) return null;

        return getTable( slice.getDataSet( ), slice.getTableMapping( ).getRef( ) );
    }

    public static Table getTable( Concept concept ) throws javax.xml.bind.JAXBException, java.io.IOException, DsplException
    {
        if ( concept == null || concept.getDataSet( ) == null || concept.getTableMapping( ) == null ) return null;

        return getTable( concept.getDataSet( ), null, concept.getTableMapping( ).getRef( ) );
    }

    public static Table getTable( DataSet dataset, QName ref ) throws javax.xml.bind.JAXBException, java.io.IOException, DsplException
    {
        return getTable( dataset, ref.getNamespaceURI( ), ref.getLocalPart( ) );
    }

    public static Table getTable( DataSet dataset, String namespace, String local ) throws javax.xml.bind.JAXBException, java.io.IOException, DsplException
    {
        if ( local == null ) return null;

        if ( namespace == null || namespace.equals( dataset.getTargetNamespace( ) ) || namespace.equals( com.metsci.glimpse.dspl.util.DsplHelper.defaultDsplNamespace ) )
        {
            if ( dataset.getTables( ) != null )
            {
                for ( Table table : dataset.getTables( ).getTable( ) )
                {
                    if ( local.equals( table.getId( ) ) )
                    {
                        return table;
                    }
                }
            }
        }
        else if ( namespace != null )
        {
            for ( Import imp : dataset.getImport( ) )
            {
                if ( namespace.equals( imp.getNamespace( ) ) )
                {
                    DataSet importedDataset = dataset.getDataSet( imp );
                    Table importedTable = getTable( importedDataset, namespace, local );

                    if ( importedTable != null ) return importedTable;
                }
            }
        }

        return null;
    }

    public static InputStream getTableInputStream( Table table ) throws IOException
    {
        if ( table == null ) return null;

        DataSet dataset = table.getDataSet( );

        DsplParser parser = dataset.getParser( );

        Data data = table.getData( );
        if ( data == null ) return null;

        File file = data.getFile( );
        String name = file.getValue( );

        String namespace = dataset.getTargetNamespace( );

        if ( namespace != null && namespace.startsWith( canonicalGoogleNamespace ) )
        {
            return getCanonicalTableInputStream( parser, name, canonicalGoogleLocalBase, canonicalGoogleUrlBase );
        }
        else if ( namespace != null && namespace.startsWith( canonicalMetronNamespace ) )
        {
            return getCanonicalTableInputStream( parser, name, canonicalMetronLocalBase, null );
        }
        else
        {
            return getLocalTableInputStream( dataset, name );
        }
    }

    public static InputStream getCanonicalTableInputStream( DsplParser parser, String file, String localBase, String urlBase ) throws IOException
    {
        if ( parser.isNetworkMode( ) )
        {
            try
            {
                return getCanonicalLocalTableInputStream( file, localBase );
            }
            catch ( IOException e )
            {
                return getCanonicalNetworkTableInputStream( file, urlBase );
            }
        }
        else
        {
            return getCanonicalLocalTableInputStream( file, localBase );
        }
    }

    public static InputStream getCanonicalLocalTableInputStream( String file, String localBase ) throws IOException
    {
        String canonicalPathLocal = localBase + file;
        return StreamOpener.fileThenResource.openForRead( canonicalPathLocal );
    }

    public static InputStream getCanonicalNetworkTableInputStream( String file, String urlBase ) throws IOException
    {
        String canonicalFile = urlBase + file;
        URL canonicalUrl = new URL( canonicalFile );
        return canonicalUrl.openStream( );
    }

    public static InputStream getLocalTableInputStream( DataSet dataSet, String name )
    {
        // try loading the file using the name as is (as a file or classpath resource)
        try
        {
            return StreamOpener.fileThenResource.openForRead( name );
        }
        catch ( Exception e )
        {
            logFine( logger, "Unable to load relative path: %s", name );
        }

        // try loading the file by appending the parent DataSet url
        String tableFile = null;
        try
        {
            java.io.File dataSetFile = dataSet.getFile( );
            if ( dataSetFile != null )
            {
                if ( dataSetFile.getName( ).endsWith( ".zip" ) )
                {
                    return getInputStreamFromZipFile( dataSetFile.getAbsolutePath( ), name );
                }
                else
                {
                    java.io.File dataSetParent = dataSetFile.getParentFile( );
                    tableFile = new java.io.File( dataSetParent, name ).getPath( );
                    return StreamOpener.fileThenResource.openForRead( tableFile );
                }
            }
        }
        catch ( Exception e )
        {
            logFine( logger, "Unable to load absolute path to file %s", e, name );
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////////
    //////                Concept utility methods                    //////
    ///////////////////////////////////////////////////////////////////////

    /**
     * Searches the provided dataset (and recursively searches that dataset's
     * imported datasets) for the Concept with id matching the provided id.
     *
     * @param dataset the dataset to search for the concept in
     * @param id the concept to search for
     * @return the concept referenced by id
     * @throws JAXBException
     * @throws IOException
     */
    public static Concept getConcept( DataSet dataset, String namespace, String local ) throws javax.xml.bind.JAXBException, java.io.IOException, com.metsci.glimpse.dspl.util.DsplException
    {
        if ( local == null ) return null;

        if ( namespace == null || namespace.equals( dataset.getTargetNamespace( ) ) || namespace.equals( com.metsci.glimpse.dspl.util.DsplHelper.defaultDsplNamespace ) )
        {
            if ( dataset.getConcepts( ) != null )
            {
                for ( Concept concept : dataset.getConcepts( ).getConcept( ) )
                {
                    if ( local.equals( concept.getId( ) ) )
                    {
                        return concept;
                    }
                }
            }
        }
        else
        {
            for ( Import imp : dataset.getImport( ) )
            {
                if ( namespace == null || namespace.equals( imp.getNamespace( ) ) )
                {
                    DataSet importedDataset = dataset.getDataSet( imp );
                    Concept importedConcept = getConcept( importedDataset, namespace, local );

                    if ( importedConcept != null ) return importedConcept;
                }
            }
        }

        return null;
    }

    public static void checkConceptReferences( DataSet dataset ) throws JAXBException, IOException, DsplException
    {
        if ( dataset == null ) return;

        if ( dataset.getConcepts( ) != null )
        {
            for ( Concept concept : dataset.getConcepts( ).getConcept( ) )
            {
                for ( Attribute attribute : concept.getAttribute( ) )
                {
                    if ( attribute.getConceptRef( ) != null && attribute.getConcept( ) == null )
                    {
                        throw new DsplException( "Could not resolve Attribute %s Concept %s in Concept %s.", attribute.getId( ), attribute.getConcept( ), concept.getId( ) );
                    }
                }

                for ( ConceptProperty property : concept.getProperty( ) )
                {
                    if ( property.getConceptRef( ) != null && property.getConcept( ) == null )
                    {
                        throw new DsplException( "Could not resolve Property %s Concept %s in Concept %s.", property.getId( ), property.getConcept( ), concept.getId( ) );
                    }
                }

                if ( concept.getExtends( ) != null && dataset.getConcept( concept.getExtends( ) ) == null )
                {
                    throw new DsplException( "Could not resolve super-Concept %s of Concept %s.", concept.getExtends( ), concept.getId( ) );
                }
            }
        }
    }

    public static void resolveConceptIds( DataSet dataset ) throws DsplException
    {
        if ( dataset == null ) return;

        if ( dataset.getConcepts( ) != null )
        {
            for ( Concept concept : dataset.getConcepts( ).getConcept( ) )
            {
                for ( Attribute attribute : concept.getAttribute( ) )
                {
                    if ( attribute.getId( ) == null && attribute.getConceptRef( ) == null )
                    {
                        throw new DsplException( "Attribute of Concept %s must declare either an id or a concept.", concept.getId( ) );
                    }
                    else if ( attribute.getId( ) == null )
                    {
                        // if the attribute was not given an id, set it equal to the concept id
                        attribute.setId( attribute.getConceptRef( ).getLocalPart( ) );
                    }
                }

                for ( ConceptProperty property : concept.getProperty( ) )
                {
                    if ( property.getId( ) == null && property.getConceptRef( ) == null )
                    {
                        throw new DsplException( "Property of Concept %s must declare either an id or a concept.", concept.getId( ) );
                    }
                    else if ( property.getId( ) == null )
                    {
                        // if the property was not given an id, set it equal to the concept id
                        property.setId( property.getConceptRef( ).getLocalPart( ) );
                    }
                }
            }
        }
    }

    public static void resolveConceptRefs( DataSet dataset )
    {
        if ( dataset == null ) return;

        if ( dataset.getSlices( ) != null )
        {
            for ( Slice slice : dataset.getSlices( ).getSlice( ) )
            {
                for ( SliceConceptRef ref : slice.getDimension( ) )
                {
                    ref.setSlice( slice );
                }

                for ( SliceConceptRef ref : slice.getMetric( ) )
                {
                    ref.setSlice( slice );
                }
            }
        }

        if ( dataset.getConcepts( ) != null )
        {
            for ( Concept concept : dataset.getConcepts( ).getConcept( ) )
            {
                for ( ConceptProperty ref : concept.getProperty( ) )
                {
                    ref.setParentConcept( concept );
                }

                for ( Attribute ref : concept.getAttribute( ) )
                {
                    ref.setParentConcept( concept );
                }
            }
        }
    }

    public static void resolveDataSetLinks( DataSet dataset ) throws JAXBException, IOException
    {
        if ( dataset == null ) return;

        if ( dataset.getConcepts( ) != null )
        {
            for ( Concept concept : dataset.getConcepts( ).getConcept( ) )
            {
                concept.setDataSet( dataset );
            }
        }

        if ( dataset.getTables( ) != null )
        {
            for ( Table table : dataset.getTables( ).getTable( ) )
            {
                table.setDataSet( dataset );
            }
        }

        if ( dataset.getSlices( ) != null )
        {
            for ( Slice slice : dataset.getSlices( ).getSlice( ) )
            {
                slice.setDataSet( dataset );
            }
        }
    }

    public static void resolveConceptExtension( DataSet dataset ) throws JAXBException, IOException, DsplException
    {
        if ( dataset == null || dataset.getConcepts( ) == null || dataset.getConcepts( ).getConcept( ) == null ) return;

        for ( Concept concept : dataset.getConcepts( ).getConcept( ) )
        {
            resolveConceptExtension( dataset, concept );
        }
    }

    public static void resolveConceptTypes( DataSet dataset ) throws JAXBException, IOException, DsplException
    {
        if ( dataset == null || dataset.getConcepts( ) == null || dataset.getConcepts( ).getConcept( ) == null ) return;

        for ( Concept concept : dataset.getConcepts( ).getConcept( ) )
        {
            for ( ConceptProperty property : concept.getProperty( ) )
            {
                // fill in the type for properties which define no explicit type, but define a concept which has a type
                if ( property.getType( ) == null )
                {
                    Concept propertyConcept = property.getConcept( );

                    if ( propertyConcept != null && propertyConcept.getType( ) != null )
                    {
                        ConceptProperty.Type type = new ConceptProperty.Type( );
                        type.setRef( propertyConcept.getType( ).getRef( ) );
                        property.setType( type );
                    }
                }
                // if both a type and concept are provided, make sure the type is less restrictive than the concept type
                else
                {
                    Concept propertyConcept = property.getConcept( );

                    if ( propertyConcept != null && propertyConcept.getType( ) != null )
                    {
                        if ( !isLessRestrictiveThan( property.getType( ).getRef( ), propertyConcept.getType( ).getRef( ) ) ) throw new DsplException( "Property %s of Concept %s declares type %s which is not less restrictive than the type %s of the Property's Concept %s", property.getId( ), concept.getId( ), property.getType( ).getRef( ), propertyConcept.getType( ).getRef( ), propertyConcept.getId( ) );
                    }
                }
            }

            for ( Attribute attribute : concept.getAttribute( ) )
            {
                // fill in the type for attributes which define no explicit type, but define a concept which has a type
                if ( attribute.getType( ) == null )
                {
                    Concept attributeConcept = attribute.getConcept( );

                    if ( attributeConcept != null && attributeConcept.getType( ) != null )
                    {
                        Attribute.Type type = new Attribute.Type( );
                        type.setRef( attributeConcept.getType( ).getRef( ) );
                        attribute.setType( type );
                    }
                }
                // if both a type and concept are provided, make sure the type is less restrictive than the concept type
                else
                {
                    Concept attributeConcept = attribute.getConcept( );

                    if ( attributeConcept != null && attributeConcept.getType( ) != null )
                    {
                        if ( !isLessRestrictiveThan( attribute.getType( ).getRef( ), attributeConcept.getType( ).getRef( ) ) ) throw new DsplException( "Attribute %s of Concept %s declares type %s which is not less restrictive than the type %s of the Attribute's Concept %s", attribute.getId( ), concept.getId( ), attribute.getType( ).getRef( ), attributeConcept.getType( ).getRef( ), attributeConcept.getId( ) );
                    }
                }
            }
        }
    }

    public static Concept resolveConceptExtension( DataSet dataset, Concept concept ) throws JAXBException, IOException, DsplException
    {
        // if our parent concept has already been set, then this concept (and all its super-concepts) have been resolved
        if ( concept.getParentConcept( ) != null ) return concept;

        // get this concept's parent concept, loading the dataset in which it resides if necessary
        Concept parent = getParentConcept( dataset, concept );

        // if this concept does not have a parent, there is nothing to do
        if ( parent == null ) return concept;

        // detect cycle
        if ( parent.isInstanceOf( concept ) )
        {
            throw new DsplException( "Cycle detected in Concept: {%s}%s", dataset.getTargetNamespace( ), concept.getId( ) );
        }

        // recursively resolve the parent's parent (and so forth) before working with this concept
        parent = resolveConceptExtension( dataset, parent );

        // save a reference to our parent concept, marking that we have been resolved
        concept.setParentConcept( parent );

        // child concepts inherit their type from their parent
        //
        // from dspl.xsd (for the type element of the Concept complex type):
        //
        // The data type of the concept. A concept must provide a type declaration or extend
        // another concept. In the case where it's extending a concept, it may also
        // provide a type declaration. The type of the extended concept must be less restrictive
        // than the type of the concept extending it.
        //
        // "Less restrictive than" (LRT) is a partial order defined as follows:
        //
        // string LRT float
        // float LRT integer
        // string LRT date
        // string LRT boolean
        //
        if ( concept.getType( ) == null )
        {
            if ( parent.getType( ) == null )
            {
                // this is only a problem if the concept is used as the column of a table, it is not an error otherwise
                //throw new DsplException( "Concept %s nor any of its ancestors declare a type.", concept.getId( ) );
            }
            else
            {
                concept.setType( parent.getType( ) );
            }
        }
        else
        {
            DataType childType = concept.getType( ).getRef( );
            DataType parentType = parent.getType( ).getRef( );

            // ensure that the LRT rules specified above are followed
            if ( !isLessRestrictiveThan( childType, parentType ) )
            {
                throw createTypeException( concept, parent, childType, parentType );
            }
        }

        // concepts inherit the properties of their parent
        for ( ConceptProperty parentProperty : parent.getProperty( ) )
        {
            ConceptProperty childProperty = concept.getProperty( parentProperty.getId( ) );

            if ( childProperty == null )
            {
                concept.getProperty( ).add( parentProperty );
            }
            else
            {
                if ( childProperty.getConcept( ) == null && parentProperty.getConcept( ) != null )
                {
                    childProperty.setConceptRef( parentProperty.getConceptRef( ) );
                }

                if ( childProperty.getType( ) == null && parentProperty.getType( ) != null )
                {
                    ConceptProperty.Type type = new ConceptProperty.Type( );
                    type.setRef( parentProperty.getType( ).getRef( ) );
                    childProperty.setType( type );
                }

                checkLegalOverride( dataset, childProperty, parentProperty );
            }
        }

        // concepts inherit the attributes of their parent
        for ( Attribute parentAttribute : parent.getAttribute( ) )
        {
            Attribute childAttribute = concept.getAttribute( parentAttribute.getId( ) );

            if ( childAttribute == null )
            {
                concept.getAttribute( ).add( parentAttribute );
            }
            else
            {
                if ( childAttribute.getConcept( ) == null && parentAttribute.getConcept( ) != null )
                {
                    childAttribute.setConceptRef( parentAttribute.getConceptRef( ) );
                }

                if ( childAttribute.getType( ) == null && parentAttribute.getType( ) != null )
                {
                    Attribute.Type type = new Attribute.Type( );
                    type.setRef( parentAttribute.getType( ).getRef( ) );
                    childAttribute.setType( type );
                }

                checkLegalOverride( dataset, childAttribute, parentAttribute );
            }
        }

        return concept;
    }

    public static boolean checkLegalOverride( DataSet dataset, Attribute child, Attribute parent ) throws JAXBException, IOException, DsplException
    {
        //XXX I'm unsure about the dspl rules for "overriding" attributes defined by the parent concept
        //XXX this method should check for type agreement as well
        Concept childConcept = child.getConcept( );
        Concept parentConcept = parent.getConcept( );

        if ( childConcept != null && parentConcept != null )
        {
            if ( !isInstanceOf( childConcept, parentConcept ) )
            {
                throw new DsplException( "Attribute %s with Concept %s cannot override Attribute %s with incompatible concept %s.", child.getId( ), childConcept.getId( ), parent.getId( ), parentConcept.getId( ) );
            }
        }

        if ( child.getType( ) == null || child.getType( ).getRef( ) == null )
        {
            throw new DsplException( "Attribute %s has no defined type", child.getId( ) );
        }

        if ( parent.getType( ) == null || parent.getType( ).getRef( ) == null )
        {
            throw new DsplException( "Attribute %s has no defined type", parent.getId( ) );
        }

        DataType childType = child.getType( ).getRef( );
        DataType parentType = parent.getType( ).getRef( );

        if ( !isLessRestrictiveThan( childType, parentType ) )
        {
            throw new DsplException( "Attribute %s with Type %s cannot override Attribute %s with more restrictive type %s.", child.getId( ), childType, parent.getId( ), parentType );
        }

        return true;
    }

    public static boolean checkLegalOverride( DataSet dataset, ConceptProperty child, ConceptProperty parent ) throws JAXBException, IOException, DsplException
    {
        //XXX I'm unsure about the dspl rules for "overriding" attributes defined by the parent concept
        //XXX this method should check for type agreement as well
        Concept childConcept = child.getConcept( );
        Concept parentConcept = parent.getConcept( );

        if ( childConcept != null && parentConcept != null )
        {
            if ( !isInstanceOf( childConcept, parentConcept ) )
            {
                throw new DsplException( "Property %s with Concept %s cannot override Property %s with incompatible concept %s.", child.getId( ), childConcept.getId( ), parent.getId( ), parentConcept.getId( ) );
            }
        }

        if ( child.getType( ) == null || child.getType( ).getRef( ) == null )
        {
            throw new DsplException( "Attribute %s has no defined type", child.getId( ) );
        }

        if ( parent.getType( ) == null || parent.getType( ).getRef( ) == null )
        {
            throw new DsplException( "Attribute %s has no defined type", parent.getId( ) );
        }

        DataType childType = child.getType( ).getRef( );
        DataType parentType = parent.getType( ).getRef( );

        if ( !isLessRestrictiveThan( childType, parentType ) )
        {
            throw new DsplException( "Property %s with Type %s cannot override Property %s with more restrictive type %s.", child.getId( ), childType, parent.getId( ), parentType );
        }

        return true;
    }

    public static boolean isLessRestrictiveThan( DataType childType, DataType parentType )
    {
        if ( childType != parentType )
        {
            if ( parentType == DataType.FLOAT )
            {
                if ( childType != DataType.STRING ) return false;
            }
            else if ( parentType == DataType.INTEGER )
            {
                if ( childType != DataType.FLOAT || childType != DataType.STRING ) return false;
            }
            else if ( parentType == DataType.DATE )
            {
                if ( childType != DataType.STRING ) return false;
            }
            else if ( parentType == DataType.BOOLEAN )
            {
                if ( childType != DataType.STRING ) return false;
            }
        }

        return true;
    }

    /**
     * Tests whether subConcept either is the same concept as superConcept, or has superConcept
     * somewhere in its parent Concept hierarchy.
     *
     * @param subConcept
     * @param superConcept
     * @return true if subConcept is an instance of superConcept
     */
    public static boolean isInstanceOf( Concept subConcept, Concept superConcept )
    {
        if ( equals( subConcept, superConcept ) )
        {
            return true;
        }
        else if ( subConcept == null || subConcept.getParentConcept( ) == null )
        {
            return false;
        }
        else
        {
            return isInstanceOf( subConcept.getParentConcept( ), superConcept );
        }
    }

    public static boolean equals( Concept ref1, Concept ref2 )
    {
        if ( ref1 == null || ref2 == null ) return false;

        return equals( ref1.getDataSet( ).getTargetNamespace( ), ref1.getId( ), ref2.getDataSet( ).getTargetNamespace( ), ref2.getId( ) );
    }

    public static boolean equals( QName ref1, QName ref2 )
    {
        if ( ref1 == null || ref2 == null ) return false;

        return equals( ref1.getNamespaceURI( ), ref1.getLocalPart( ), ref2.getNamespaceURI( ), ref2.getLocalPart( ) );
    }

    public static boolean equals( String namespace1, String local1, String namespace2, String local2 )
    {
        if ( namespace1 == null || local1 == null || namespace2 == null || local2 == null ) return false;

        return namespace1.equals( namespace2 ) && local1.equals( local2 );
    }

    protected static DsplException createTypeException( Concept child, Concept parent, DataType childType, DataType parentType )
    {
        return new DsplException( "Concept %s has illegal type %s because its parent Concept %s has type %s (%s is not less restrictive than %s).", child.getId( ), childType, parent.getId( ), parentType, childType, parentType );
    }

    public static Concept getParentConcept( DataSet dataset, Concept concept ) throws JAXBException, IOException, DsplException
    {
        QName parentRef = concept.getExtends( );
        return parentRef != null ? dataset.getConcept( parentRef ) : null;
    }

    public static Concept getCompatibleConceptRef( Slice slice, Concept superConcept, List<SliceConceptRef> refs ) throws JAXBException, IOException, DsplException
    {
        for ( SliceConceptRef dimension : refs )
        {
            Concept subConcept = dimension.getConcept( );
            if ( isInstanceOf( subConcept, superConcept ) ) return subConcept;
        }

        return null;
    }

    public static String getName( String namespace ) throws MalformedURLException
    {
        URL url = new URL( namespace );
        String[] tokens = url.getPath( ).split( "/" );
        if ( tokens.length == 0 ) return null;

        String name = tokens[tokens.length - 1];

        return name;
    }

    public static final String LANGUAGE_ENGLISH = "en";

    public static String getValueEnglish( List<Value> valueList )
    {
        if ( valueList == null || valueList.isEmpty( ) ) return null;

        // search through the names for an English value
        for ( Value value : valueList )
        {
            if ( LANGUAGE_ENGLISH.equals( value.getLang( ) ) ) return value.getValue( );
        }

        // if we didn't find an English language value, return the first value
        return valueList.get( 0 ).getValue( );
    }
}
