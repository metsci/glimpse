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
package com.metsci.glimpse.topo.io;

import static com.google.common.base.Charsets.UTF_8;
import static com.metsci.glimpse.topo.io.TopoDataPaths.glimpseTopoCacheDir;
import static com.metsci.glimpse.topo.io.TopoReader.readTopoDataset;
import static com.metsci.glimpse.topo.io.TopoWriter.writeTopoDataset;
import static com.metsci.glimpse.util.io.FileSync.lockFile;
import static com.metsci.glimpse.util.io.FileSync.unlockFile;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.units.time.TimeUtils.formatTime_ISO8601;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class TopoCache
{
    private static final Logger logger = getLogger( TopoCache.class );


    public static final String topoCacheFormatVersion = "1";

    /**
     * We're not trying to protect against deliberate collisions, and we don't need
     * anything super fast -- but ubiquity and stability over time are important.
     */
    @SuppressWarnings( "deprecation" )
    public static final HashFunction topoConfigHashFn = Hashing.md5( );


    public static TopoDataset topoCacheDataset( TopoDataFile baseLevel ) throws IOException
    {
        return topoCacheDataset( baseLevel, glimpseTopoCacheDir );
    }

    public static TopoDataset topoCacheDataset( TopoDataFile baseLevel, File cacheParentDir ) throws IOException
    {
        String configString = topoConfigString( baseLevel );
        String configHash = topoConfigHashFn.newHasher( ).putString( configString, UTF_8 ).hash( ).toString( );
        File cacheDir = new File( cacheParentDir, "topoCache_" + configHash );
        cacheDir.mkdirs( );

        File mutexFile = new File( cacheDir, "mutex" );
        mutexFile.createNewFile( );

        lockFile( mutexFile );
        try
        {
            File configFile = new File( cacheDir, "config.txt" );
            if ( !configFile.isFile( ) )
            {
                // TODO: Notify a progress listener -- e.g. to show dialog box with progress bar
                logger.info( "Writing topo cache: base-data = " + baseLevel.dataFile.getAbsolutePath( ) + ", cache-dir = " + cacheDir.getAbsolutePath( ) );
                writeTopoDataset( baseLevel, cacheDir );
                Files.asCharSink( configFile, UTF_8 ).write( configString );
            }
        }
        finally
        {
            unlockFile( mutexFile );
        }

        return readTopoDataset( cacheDir );
    }

    public static String topoConfigString( TopoDataFile baseLevel )
    {
        StringBuilder configString = new StringBuilder( );

        configString.append( "formatVersion = " ).append( topoCacheFormatVersion ).append( "\n" );
        configString.append( "\n" );

        configString.append( "numRows = " ).append( baseLevel.numRows ).append( "\n" );
        configString.append( "numCols = " ).append( baseLevel.numCols ).append( "\n" );
        configString.append( "southLat_DEG = " ).append( baseLevel.southLat_DEG ).append( "\n" );
        configString.append( "westLon_DEG = " ).append( baseLevel.westLon_DEG ).append( "\n" );
        configString.append( "cellSize_DEG = " ).append( baseLevel.cellSize_DEG ).append( "\n" );
        configString.append( "\n" );

        configString.append( "dataFile = " ).append( baseLevel.dataFile.getAbsolutePath( ) ).append( "\n" );
        configString.append( "dataModified = " ).append( formatTime_ISO8601( baseLevel.dataFile.lastModified( ) ) ).append( "\n" );
        configString.append( "dataType = " ).append( baseLevel.dataType ).append( "\n" );
        configString.append( "dataByteOrder = " ).append( baseLevel.dataByteOrder ).append( "\n" );
        configString.append( "dataUnits = " ).append( baseLevel.dataUnits ).append( "\n" );

        return configString.toString( );
    }

}
