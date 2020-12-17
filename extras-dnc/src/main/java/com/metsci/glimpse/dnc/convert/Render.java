/*
 * Copyright (c) 2020, Metron, Inc.
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
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.DncCoverage;
import com.metsci.glimpse.dnc.DncLibrary;

public class Render
{

    public static final String renderFormatVersion = "13";


    public static final String renderCharsetFilename = "charset";
    public static final String renderConfigFilename = "config";


    public static final String renderLibrariesFilename = "libraries";
    public static final String renderCoveragesFilename = "coverages";


    public static final String renderMutexFilename = "mutex";
    public static final String renderCursorFilename = "cursor";
    public static final String renderChunksFilename = "chunks";
    public static final String renderGroupsFilename = "groups";
    public static final String renderLabelCharsFilename = "label-chars";
    public static final String renderLabelLengthsFilename = "label-lengths";
    public static final String renderVerticesFilename = "vertices";


    public static final int longsPerRenderChunk = 11;
    public static final int intsPerRenderGroup = 11;
    public static final int floatsPerRenderLibrary = 4;

    // x, y, featureNum
    public static final int coordsPerRenderTriangleVertex = 3;

    // x, y, featureNum, cumulativeDistance
    public static final int coordsPerRenderLineVertex = 4;

    // x, y, featureNum, rotation_CCWRAD
    public static final int coordsPerRenderIconVertex = 4;

    // x, y, featureNum
    public static final int coordsPerRenderLabelVertex = 3;


    public static Charset readRenderCharset( File renderDir ) throws IOException
    {
        File charsetFile = new File( renderDir, renderCharsetFilename );
        return Charset.forName( Files.toString( charsetFile, US_ASCII ).trim( ) );
    }


    public static void writeRenderCharset( File renderDir, Charset charset ) throws IOException
    {
        File charsetFile = new File( renderDir, renderCharsetFilename );
        Files.write( charset.name( ), charsetFile, US_ASCII );
    }


    public static String readRenderConfig( File renderDir, Charset charset ) throws IOException
    {
        File configFile = new File( renderDir, renderConfigFilename );
        return Files.toString( configFile, charset );
    }


    public static void writeRenderConfig( File renderDir, String configString, Charset charset ) throws IOException
    {
        File configFile = new File( renderDir, renderConfigFilename );
        Files.write( configString, configFile, charset );
    }


    public static List<DncLibrary> readRenderLibrariesFile( File file, Charset charset ) throws IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), charset ) );

            List<DncLibrary> libraries = new ArrayList<>( );
            for ( int i = 0; true; i++ )
            {
                String line = reader.readLine( );
                if ( line == null ) break;

                String[] tokens = line.split( " " );
                if ( tokens.length != 6 ) throw new IOException( "Format error in " + file.getAbsolutePath( ) + " on line " + i );

                int databaseNum = parseInt( tokens[ 0 ] );
                String libraryName = tokens[ 1 ];
                float xMin = parseFloat( tokens[ 2 ] );
                float xMax = parseFloat( tokens[ 3 ] );
                float yMin = parseFloat( tokens[ 4 ] );
                float yMax = parseFloat( tokens[ 5 ] );

                libraries.add( new DncLibrary( databaseNum, libraryName, xMin, xMax, yMin, yMax ) );
            }
            return libraries;
        }
        finally
        {
            if ( reader != null ) reader.close( );
        }
    }


    public static List<DncCoverage> readRenderCoveragesFile( File file, Charset charset ) throws IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), charset ) );

            List<DncCoverage> coverages = new ArrayList<>( );
            for ( int i = 0; true; i++ )
            {
                String line = reader.readLine( );
                if ( line == null ) break;

                String[] tokens = line.split( " " );
                if ( tokens.length != 1 ) throw new IOException( "Format error in " + file.getAbsolutePath( ) + " on line " + i );

                String coverageName = tokens[ 0 ];

                coverages.add( new DncCoverage( coverageName ) );
            }
            return coverages;
        }
        finally
        {
            if ( reader != null ) reader.close( );
        }
    }


    public static class RenderChunk
    {
        public final DncChunkKey chunkKey;

        public final int featureCount;

        public final int groupFirst;
        public final int groupCount;

        public final int labelCharFirst;
        public final int labelCharCount;

        public final int labelLengthFirst;
        public final int labelLengthCount;

        public final long vertexCoordFirst;
        public final int vertexCoordCount;

        public RenderChunk( DncChunkKey chunkKey,

                            int featureCount,

                            int groupFirst,
                            int groupCount,

                            int labelCharFirst,
                            int labelCharCount,

                            int labelLengthFirst,
                            int labelLengthCount,

                            long vertexCoordFirst,
                            int vertexCoordCount )
        {
            this.chunkKey = chunkKey;

            this.featureCount = featureCount;

            this.groupFirst = groupFirst;
            this.groupCount = groupCount;

            this.labelCharFirst = labelCharFirst;
            this.labelCharCount = labelCharCount;

            this.labelLengthFirst = labelLengthFirst;
            this.labelLengthCount = labelLengthCount;

            this.vertexCoordFirst = vertexCoordFirst;
            this.vertexCoordCount = vertexCoordCount;
        }
    }


    public static List<RenderChunk> readRenderChunks( LongBuffer chunksBuf, List<DncLibrary> libraries, List<DncCoverage> coverages )
    {
        List<RenderChunk> chunks = new ArrayList<>( );
        while ( chunksBuf.hasRemaining( ) )
        {
            int libraryNum = ( int ) chunksBuf.get( );
            int coverageNum = ( int ) chunksBuf.get( );

            int featureCount = ( int ) chunksBuf.get( );

            int groupFirst = ( int ) chunksBuf.get( );
            int groupCount = ( int ) chunksBuf.get( );

            int labelCharFirst = ( int ) chunksBuf.get( );
            int labelCharCount = ( int ) chunksBuf.get( );

            int labelLengthFirst = ( int ) chunksBuf.get( );
            int labelLengthCount = ( int ) chunksBuf.get( );

            long vertexCoordFirst = chunksBuf.get( );
            int vertexCoordCount = ( int ) chunksBuf.get( );

            DncLibrary library = libraries.get( libraryNum );
            DncCoverage coverage = coverages.get( coverageNum );
            DncChunkKey chunkKey = new DncChunkKey( library, coverage );
            chunks.add( new RenderChunk( chunkKey, featureCount, groupFirst, groupCount, labelCharFirst, labelCharCount, labelLengthFirst, labelLengthCount, vertexCoordFirst, vertexCoordCount ) );
        }
        return chunks;
    }

}
