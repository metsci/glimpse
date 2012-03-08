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
package com.metsci.glimpse.dspl.parser.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.parser.MultipleFileTableParser;
import com.metsci.glimpse.dspl.parser.TableParser;
import com.metsci.glimpse.dspl.parser.TableWriter;
import com.metsci.glimpse.dspl.parser.table.PropertyTableData;
import com.metsci.glimpse.dspl.parser.table.SliceTableData;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.Table;
import com.metsci.glimpse.dspl.util.DsplException;
import com.metsci.glimpse.dspl.util.DsplHelper;
import com.metsci.glimpse.dspl.util.FileNameCleaner;
import com.metsci.glimpse.dspl.util.MD5Checksum;

import java.util.logging.Logger;

public class DsplCacheHelper
{
    public static final Logger logger = Logger.getLogger( DsplCacheHelper.class.getName( ) );

    public static final String CACHE_ROOT = ".dspl";

    public static final String CACHE_FILE = "cache_data";

    public static final String CACHE_FORMAT = "bin";
    public static final String HASH_FILE = ".md5";

    public static PropertyTableData getTableData( Concept concept ) throws DsplException, JAXBException, IOException
    {
        try
        {
            if ( concept == null ) return null;

            DataSet dataset = concept.getDataSet( );
            if ( dataset == null ) return null;

            DsplParser dsplParser = dataset.getParser( );
            if ( dsplParser == null ) return null;

            String calculatedHash = getCalculatedHash( concept );
            if ( calculatedHash == null ) return null;

            Table table = concept.getTable( );
            if ( table == null ) return null;

            TableParser parser = dsplParser.getTableParser( table );
            if ( parser == null ) return null;

            // check whether this data type should be cached (we don't bother for fast formats)
            // if not, simply parse it in the regular way
            if ( !parser.isCachable( ) )
            {
                return parser.parse( concept );
            }

            String cachedHash = getCachedHash( concept );

            // the cache has not been created or the csv data has changed, load the data from the csv file and recreate the hash
            if ( cachedHash == null || !cachedHash.equals( calculatedHash ) )
            {
                PropertyTableData tableData = parser.parse( concept );

                File cacheFile = getCacheFile( concept );
                FileOutputStream cacheFileStream = new FileOutputStream( cacheFile );

                String cacheFormat = getExtension( cacheFile );

                TableParser cacheParser = dsplParser.getTableParser( cacheFormat );

                if ( cacheParser instanceof TableWriter )
                {
                    ( ( TableWriter ) cacheParser ).write( concept, tableData, cacheFileStream.getChannel( ) );
                    writeHash( concept, calculatedHash );
                }
                else
                {
                    throw new DsplException( "Invalid cache format: %s. TableParser %s is not a TableWriter.", cacheFormat, cacheParser.getClass( ) );
                }

                return tableData;
            }
            // load the data from the cache
            else
            {
                File cacheFile = getCacheFile( concept );
                FileInputStream cacheFileStream = new FileInputStream( cacheFile );
                TableParser cacheParser = dsplParser.getTableParser( getExtension( cacheFile ) );
                return cacheParser.parse( concept, cacheFileStream.getChannel( ) );
            }
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new DsplException( "Unable to load from cache.", e );
        }
    }

    public static SliceTableData getTableData( Slice slice ) throws DsplException, JAXBException, IOException
    {
        try
        {
            if ( slice == null ) return null;

            DataSet dataset = slice.getDataSet( );
            if ( dataset == null ) return null;

            DsplParser dsplParser = dataset.getParser( );
            if ( dsplParser == null ) return null;

            String calculatedHash = getCalculatedHash( slice );
            if ( calculatedHash == null ) return null;

            Table table = slice.getTable( );
            if ( table == null ) return null;

            TableParser parser = dsplParser.getTableParser( table );
            if ( parser == null ) return null;

            // check whether this data type should be cached (we don't bother for fast formats)
            // if not, simply parse it in the regular way
            if ( !parser.isCachable( ) )
            {
                return parser.parse( slice );
            }

            String cachedHash = getCachedHash( slice );

            // the cache has not been created or the csv data has changed, load the data from the csv file and recreate the hash
            if ( cachedHash == null || !cachedHash.equals( calculatedHash ) )
            {
                SliceTableData tableData = parser.parse( slice );

                File cacheFile = getCacheFile( slice );
                OutputStream cacheFileStream = new FileOutputStream( cacheFile );

                String cacheFormat = getExtension( cacheFile );

                TableParser cacheParser = dsplParser.getTableParser( cacheFormat );

                if ( cacheParser instanceof TableWriter )
                {
                    ( ( TableWriter ) cacheParser ).write( slice, tableData, cacheFileStream );
                    writeHash( slice, calculatedHash );
                }
                else
                {
                    throw new DsplException( "Invalid cache format: %s. TableParser %s is not a TableWriter.", cacheFormat, cacheParser.getClass( ) );
                }

                return tableData;
            }
            // load the data from the cache
            else
            {
                File cacheFile = getCacheFile( slice );
                InputStream cacheFileStream = new FileInputStream( cacheFile );
                TableParser cacheParser = dsplParser.getTableParser( getExtension( cacheFile ) );
                return cacheParser.parse( slice, cacheFileStream );
            }
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new DsplException( "Unable to load from cache.", e );
        }
    }

    public static File getCacheDirectory( Concept concept ) throws DsplException, JAXBException, IOException
    {
        return getCacheDirectory( concept.getDataSet( ), concept.getTable( ), concept.getId( ) );
    }

    public static File getCacheDirectory( Slice slice ) throws DsplException, JAXBException, IOException
    {
        return getCacheDirectory( slice.getDataSet( ), slice.getTable( ), slice.getId( ) );
    }

    protected static File getCacheDirectory( DataSet dataset, Table table, String id ) throws DsplException, JAXBException, IOException
    {
        if ( dataset == null || table == null || id == null ) throw new DsplException( "Dataset, Table, or Id is not initialized. Unable to create cache file." );

        File cacheDirectory = dataset.getParser( ).getCacheDirectory( );

        if ( cacheDirectory == null )
        {
            String userHome = System.getProperty( "user.home" );
            cacheDirectory = new File( userHome );

            if ( !cacheDirectory.canWrite( ) )
            {
                String tempDir = System.getProperty( "java.io.tmpdir" );
                cacheDirectory = new File( tempDir );

                if ( !cacheDirectory.canWrite( ) )
                {
                    throw new DsplException( "Unable to save cached dspl data files." );
                }
            }
        }

        String namespace = null;

        if ( dataset.getTargetNamespace( ) == null )
        {
            namespace = dataset.getFile( ).getName( );
        }
        else
        {
            namespace = FileNameCleaner.cleanFileName( dataset.getTargetNamespace( ) );
        }

        String sliceId = FileNameCleaner.cleanFileName( id );

        File dsplCacheBase = new File( cacheDirectory, CACHE_ROOT );
        File namespaceDir = new File( dsplCacheBase, namespace );
        File sliceDir = new File( namespaceDir, sliceId );

        if ( sliceDir.exists( ) )
        {
            return sliceDir;
        }

        boolean success = sliceDir.mkdirs( );

        if ( !success )
        {
            throw new DsplException( "Unable to save cached dspl data files." );
        }

        return sliceDir;
    }

    public static File getCacheFile( Concept concept ) throws DsplException, JAXBException, IOException, NoSuchAlgorithmException
    {
        return getCacheFile( concept.getDataSet( ), concept.getTable( ), concept.getId( ) );
    }

    public static File getCacheFile( Slice slice ) throws DsplException, JAXBException, IOException, NoSuchAlgorithmException
    {
        return getCacheFile( slice.getDataSet( ), slice.getTable( ), slice.getId( ) );
    }

    protected static File getCacheFile( DataSet dataset, Table table, String id ) throws DsplException, JAXBException, IOException, NoSuchAlgorithmException
    {
        //String dataFile = table.getData( ).getFile( ).getValue( );
        File sliceDir = getCacheDirectory( dataset, table, id );
        return new File( sliceDir, CACHE_FILE + "." + CACHE_FORMAT );
    }

    public static String getCalculatedHash( Slice slice ) throws DsplException, JAXBException, IOException, NoSuchAlgorithmException
    {
        DataSet dataset = slice.getDataSet( );
        DsplParser dsplParser = dataset.getParser( );
        Table table = slice.getTable( );
        TableParser parser = dsplParser.getTableParser( table );

        // the existence of ExtendedCsvParser complicates things because the data is split over multiple
        // tables so the hash must hash over all the data files
        // we handle this by computing a hash for each file then computing the hash of the individual hashes
        if ( parser instanceof MultipleFileTableParser )
        {
            MultipleFileTableParser multiFileParser = ( MultipleFileTableParser ) parser;
            List<URL> fileList = multiFileParser.getDataFiles( slice );
            List<byte[]> fileHashList = new ArrayList<byte[]>( );

            for ( URL file : fileList )
            {
                fileHashList.add( MD5Checksum.createChecksum( file.openStream( ) ) );
            }

            int totalSize = 0;
            for ( byte[] hash : fileHashList )
            {
                totalSize += hash.length;
            }

            int currentSize = 0;
            byte[] allHashes = new byte[totalSize];
            for ( byte[] hash : fileHashList )
            {
                System.arraycopy( hash, 0, allHashes, currentSize, hash.length );
                currentSize += hash.length;
            }

            return MD5Checksum.getMD5Checksum( allHashes );
        }
        else
        {
            InputStream hashIn = DsplHelper.getTableInputStream( slice );
            return MD5Checksum.getMD5Checksum( hashIn );
        }
    }

    public static String getCalculatedHash( Concept concept ) throws DsplException, JAXBException, IOException, NoSuchAlgorithmException
    {
        DataSet dataset = concept.getDataSet( );
        DsplParser dsplParser = dataset.getParser( );
        Table table = concept.getTable( );
        TableParser parser = dsplParser.getTableParser( table );

        // the existence of ExtendedCsvParser complicates things because the data is split over multiple
        // tables so the hash must hash over all the data files
        // we handle this by computing a hash for each file then computing the hash of the individual hashes
        if ( parser instanceof MultipleFileTableParser )
        {
            MultipleFileTableParser multiFileParser = ( MultipleFileTableParser ) parser;
            List<URL> fileList = multiFileParser.getDataFiles( concept );
            List<byte[]> fileHashList = new ArrayList<byte[]>( );

            for ( URL file : fileList )
            {
                fileHashList.add( MD5Checksum.createChecksum( file.openStream( ) ) );
            }

            int totalSize = 0;
            for ( byte[] hash : fileHashList )
            {
                totalSize += hash.length;
            }

            int currentSize = 0;
            byte[] allHashes = new byte[totalSize];
            for ( byte[] hash : fileHashList )
            {
                System.arraycopy( hash, 0, allHashes, currentSize, hash.length );
                currentSize += hash.length;
            }

            return MD5Checksum.getMD5Checksum( allHashes );
        }
        else
        {
            InputStream hashIn = DsplHelper.getTableInputStream( concept );
            return MD5Checksum.getMD5Checksum( hashIn );
        }
    }

    public static String getCachedHash( Slice slice ) throws DsplException, JAXBException, IOException
    {
        return getCachedHash( slice.getDataSet( ), slice.getTable( ), slice.getId( ) );
    }

    public static String getCachedHash( Concept concept ) throws DsplException, JAXBException, IOException
    {
        return getCachedHash( concept.getDataSet( ), concept.getTable( ), concept.getId( ) );
    }

    protected static String getCachedHash( DataSet dataset, Table table, String id ) throws DsplException, JAXBException, IOException
    {
        File cacheDirectory = getCacheDirectory( dataset, table, id );

        for ( File file : cacheDirectory.listFiles( ) )
        {
            String fileName = file.getName( );

            if ( fileName.equals( HASH_FILE ) )
            {
                BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) );
                try
                {
                    return in.readLine( );
                }
                finally
                {
                    in.close( );
                }
            }
        }

        return null;
    }

    public static void writeHash( Concept concept, String hash ) throws IOException, DsplException, NoSuchAlgorithmException, JAXBException
    {
        writeHash( getCacheDirectory( concept ), hash );
    }

    public static void writeHash( Slice slice, String hash ) throws IOException, DsplException, NoSuchAlgorithmException, JAXBException
    {
        writeHash( getCacheDirectory( slice ), hash );
    }

    public static void writeHash( File directory, String hash ) throws IOException, DsplException, NoSuchAlgorithmException, JAXBException
    {
        File hashFile = new File( directory, HASH_FILE );

        BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( hashFile ) ) );
        out.write( hash );
        out.close( );
    }

    protected static String getExtension( File file )
    {
        if ( file == null ) return null;

        String name = file.getName( );

        int index = name.lastIndexOf( "." );

        return name.substring( index + 1, name.length( ) );
    }
}
