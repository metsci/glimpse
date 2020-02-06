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
package com.metsci.glimpse.util.jnlu;

import static com.metsci.glimpse.util.io.IoUtils.findUniqueFile;
import static com.metsci.glimpse.util.jnlu.FileUtils.copy;
import static com.metsci.glimpse.util.jnlu.FileUtils.createTempDir;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author hogye
 */
public class NativeLibUtils
{

    public static boolean onPlatform( String osPrefix, String osArch )
    {
        String givenOsPrefix = osPrefix.toLowerCase( );
        String givenOsArch = osArch.toLowerCase( );

        String actualOsName = System.getProperty( "os.name" ).toLowerCase( );
        String actualOsArch = System.getProperty( "os.arch" ).toLowerCase( );

        return actualOsName.startsWith( givenOsPrefix ) && actualOsArch.equals( givenOsArch );
    }

    public static void extractAndLoad( List<URL> libraryUrls, String tempDirPrefix )
    {
        try
        {
            File tempDir = createTempDir( tempDirPrefix );
            for ( URL url : libraryUrls )
            {
                String tentativeFilename = extractFilenameFromUrl( url );
                String filename = findValidFilename( tempDir, tentativeFilename, "library.bin" );
                File file = findUniqueFile( tempDir, ( i -> i == 0 ? filename : format( "%s--%d", filename, i ) ) );
                copy( url, file );
                System.load( file.getPath( ) );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to extract and load native libraries", e );
        }
    }

    protected static String extractFilenameFromUrl( URL url )
    {
        String sWhole = url.getFile( );
        int slashIndex = sWhole.lastIndexOf( '/' );
        if ( slashIndex < 0 )
        {
            return sWhole;
        }
        else
        {
            String sSegment = sWhole.substring( slashIndex + 1 );
            return ( sSegment.isEmpty( ) ? null : sSegment );
        }
    }

    protected static String findValidFilename( File dir, String candidate, String fallback )
    {
        if ( candidate == null )
        {
            return fallback;
        }
        else
        {
            try
            {
                dir = dir.getCanonicalFile( );
                File file = ( new File( dir, candidate ) ).getCanonicalFile( );
                if ( file.getParentFile( ).equals( dir ) )
                {
                    return file.getName( );
                }
                else
                {
                    return fallback;
                }
            }
            catch ( IOException e )
            {
                return fallback;
            }
        }
    }

}
