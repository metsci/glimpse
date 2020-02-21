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
package com.metsci.glimpse.util;

import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;

public class AppConfigUtils
{
    private static final Logger LOGGER = Logger.getLogger( AppConfigUtils.class.getName( ) );

    public static File getAppConfigPath( String appName, String filename )
    {
        File dataDir = GlimpseDataPaths.glimpseUserDataDir;
        File appDir = new File( dataDir, appName );
        logFine( LOGGER, "Making dirs for %s", appDir );
        appDir.mkdirs( );

        if ( !appDir.isDirectory( ) )
        {
            throw new RuntimeException( "Failed to create app dir: " + appDir.getAbsolutePath( ) );
        }
        if ( !appDir.canRead( ) )
        {
            throw new RuntimeException( "Do not have read permission on app dir: " + appDir.getAbsolutePath( ) );
        }
        if ( !appDir.canWrite( ) )
        {
            throw new RuntimeException( "Do not have write permission on app dir: " + appDir.getAbsolutePath( ) );
        }

        return new File( appDir, filename );
    }

    public static InputStream openAppConfigInput( String appName, String filename ) throws IOException
    {
        File file = getAppConfigPath( appName, filename );
        logInfo( LOGGER, "Opening application config input file %s", file );
        return new FileInputStream( file );
    }

    public static InputStream openAppConfigInput( String appName, String filename, URL fallbackUrl ) throws IOException
    {
        File file = getAppConfigPath( appName, filename );
        if ( file.isFile( ) && file.canRead( ) )
        {
            logInfo( LOGGER, "Opening application config input file %s", file );
            return new FileInputStream( file );
        }
        else
        {
            logInfo( LOGGER, "Opening application config input fallback url %s", fallbackUrl );
            return fallbackUrl.openStream( );
        }
    }

    public static OutputStream openAppConfigOutput( String appName, String filename ) throws IOException
    {
        File file = getAppConfigPath( appName, filename );
        logInfo( LOGGER, "Opening application config output file %s", file );
        return new FileOutputStream( file );
    }
}
