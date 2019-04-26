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
package com.metsci.glimpse.dnc.convert;

import static com.google.common.base.Charsets.US_ASCII;
import static com.metsci.glimpse.dnc.convert.Render.readRenderCoveragesFile;
import static com.metsci.glimpse.dnc.convert.Render.readRenderLibrariesFile;

import java.io.File;
import java.io.IOException;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;
import com.metsci.glimpse.dnc.DncCoverage;
import com.metsci.glimpse.dnc.DncLibrary;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;

public class Query
{

    public static final String queryFormatVersion = "1";


    public static final String queryCharsetFilename = "charset";
    public static final String queryConfigFilename = "config";


    public static final String queryLibrariesFilename = "libraries";
    public static final String queryCoveragesFilename = "coverages";


    public static final String queryMutexFilename = "mutex";
    public static final String queryCursorFilename = "cursor";
    public static final String queryChunksFilename = "chunks";
    public static final String queryTreesFilename = "trees";


    public static final int longsPerQueryChunk = 8;
    public static final int intsPerQueryInteriorNode = 6;
    public static final int intsPerQueryLeafNode = 10;
    public static final int intsPerQueryPointItem = 3;
    public static final int intsPerQueryLineItem = 5;
    public static final int intsPerQueryTriangleItem = 7;


    public static Charset readQueryCharset( File queryDir ) throws IOException
    {
        File charsetFile = new File( queryDir, queryCharsetFilename );
        return Charset.forName( Files.toString( charsetFile, US_ASCII ).trim( ) );
    }


    public static void writeQueryCharset( File queryDir, Charset charset ) throws IOException
    {
        File charsetFile = new File( queryDir, queryCharsetFilename );
        Files.write( charset.name( ), charsetFile, US_ASCII );
    }


    public static String readQueryConfig( File queryDir, Charset charset ) throws IOException
    {
        File configFile = new File( queryDir, queryConfigFilename );
        return Files.toString( configFile, charset );
    }


    public static void writeQueryConfig( File queryDir, String configString, Charset charset ) throws IOException
    {
        File configFile = new File( queryDir, queryConfigFilename );
        Files.write( configString, configFile, charset );
    }


    public static List<DncLibrary> readQueryLibrariesFile( File file, Charset charset ) throws IOException
    {
        return readRenderLibrariesFile( file, charset );
    }


    public static List<DncCoverage> readQueryCoveragesFile( File file, Charset charset ) throws IOException
    {
        return readRenderCoveragesFile( file, charset );
    }


    public static class QueryChunk
    {
        public final DncChunkKey chunkKey;

        public final long treeWordFirst;
        public final int treeWordCount;

        public final int interiorNodeCount;
        public final int leafNodeCount;

        public final int pointItemCount;
        public final int lineItemCount;
        public final int triangleItemCount;

        public QueryChunk( DncChunkKey chunkKey,

                           long treeWordFirst,

                           int interiorNodeCount,
                           int leafNodeCount,

                           int pointItemCount,
                           int lineItemCount,
                           int triangleItemCount )
        {
            this.chunkKey = chunkKey;

            this.treeWordFirst = treeWordFirst;
            this.treeWordCount = ( interiorNodeCount * intsPerQueryInteriorNode )
                               + ( leafNodeCount * intsPerQueryLeafNode )
                               + ( pointItemCount * intsPerQueryPointItem )
                               + ( lineItemCount * intsPerQueryLineItem )
                               + ( triangleItemCount * intsPerQueryTriangleItem );

            this.interiorNodeCount = interiorNodeCount;
            this.leafNodeCount = leafNodeCount;

            this.pointItemCount = pointItemCount;
            this.lineItemCount = lineItemCount;
            this.triangleItemCount = triangleItemCount;
        }
    }


    public static List<QueryChunk> readQueryChunks( LongBuffer chunksBuf, List<DncLibrary> libraries, List<DncCoverage> coverages )
    {
        List<QueryChunk> chunks = new ArrayList<>( );
        while ( chunksBuf.hasRemaining( ) )
        {
            int libraryNum = ( int ) chunksBuf.get( );
            int coverageNum = ( int ) chunksBuf.get( );

            long treeWordFirst = chunksBuf.get( );

            int interiorNodeCount = ( int ) chunksBuf.get( );
            int leafNodeCount = ( int ) chunksBuf.get( );

            int pointItemCount = ( int ) chunksBuf.get( );
            int lineItemCount = ( int ) chunksBuf.get( );
            int triangleItemCount = ( int ) chunksBuf.get( );

            DncLibrary library = libraries.get( libraryNum );
            DncCoverage coverage = coverages.get( coverageNum );
            DncChunkKey chunkKey = new DncChunkKey( library, coverage );
            chunks.add( new QueryChunk( chunkKey, treeWordFirst, interiorNodeCount, leafNodeCount, pointItemCount, lineItemCount, triangleItemCount ) );
        }
        return chunks;
    }

}
