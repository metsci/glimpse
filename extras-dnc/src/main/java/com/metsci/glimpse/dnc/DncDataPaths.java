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
package com.metsci.glimpse.dnc;

import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseSharedDataDir;
import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseUserCacheDir;
import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseUserDataDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;

import java.io.File;
import java.util.logging.Logger;

import com.metsci.glimpse.util.GlimpseDataPaths;

public class DncDataPaths
{

    /**
     * Most code should NOT use this, but should use {@link DncDataPaths#glimpseDncFlatDir} instead.
     *
     * This is provided for code that, for some reason, needs to bypass the ( userDir || sharedDir )
     * fallback behavior and access the user dir specifically.
     */
    public static final File glimpseDncDefaultUserFlatDir = new File( glimpseUserDataDir, "DNC_FLAT" );

    /**
     * Most code should NOT use this, but should use {@link DncDataPaths#glimpseDncFlatDir} instead.
     *
     * This is provided for code that, for some reason, needs to bypass the ( userDir || sharedDir )
     * fallback behavior and access the shared dir specifically.
     */
    public static final File glimpseDncDefaultSharedFlatDir = new File( glimpseSharedDataDir, "DNC_FLAT" );

    /**
     * Standard dir for DNC_FLAT permanent data.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.dnc.flatDir}
     * <li>Env var (if defined): {@code GLIMPSE_DNC_FLAT}
     * <li>User default (if dir exists): subdir DNC_FLAT of {@link GlimpseDataPaths#glimpseUserDataDir}
     * <li>Shared default: subdir DNC_FLAT of {@link GlimpseDataPaths#glimpseSharedDataDir}
     * </ol>
     */
    public static final File glimpseDncFlatDir = glimpseDncFlatDir( );
    private static File glimpseDncFlatDir( )
    {
        String jvmProp = System.getProperty( "glimpse.dnc.flatDir" );
        if ( jvmProp != null )
        {
            return new File( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_DNC_FLAT" );
        if ( envVar != null )
        {
            return new File( envVar );
        }

        return ( glimpseDncDefaultUserFlatDir.isDirectory( ) ? glimpseDncDefaultUserFlatDir : glimpseDncDefaultSharedFlatDir );
    }

    /**
     * Standard dir for DNC_RENDER cache data.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.dnc.renderDir}
     * <li>Env var (if defined): {@code GLIMPSE_DNC_RENDER}
     * <li>Default: subdir DNC_RENDER of {@link GlimpseDataPaths#glimpseUserCacheDir}
     * </ol>
     */
    public static final File glimpseDncRenderDir = glimpseDncRenderDir( );
    private static File glimpseDncRenderDir( )
    {
        String jvmProp = System.getProperty( "glimpse.dnc.renderDir" );
        if ( jvmProp != null )
        {
            return new File( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_DNC_RENDER" );
        if ( envVar != null )
        {
            return new File( envVar );
        }

        return new File( glimpseUserCacheDir, "DNC_RENDER" );
    }

    /**
     * Standard dir for DNC_QUERY cache data.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.dnc.queryDir}
     * <li>Env var (if defined): {@code GLIMPSE_DNC_QUERY}
     * <li>Default: subdir DNC_QUERY of {@link GlimpseDataPaths#glimpseUserCacheDir}
     * </ol>
     */
    public static final File glimpseDncQueryDir = glimpseDncQueryDir( );
    private static File glimpseDncQueryDir( )
    {
        String jvmProp = System.getProperty( "glimpse.dnc.queryDir" );
        if ( jvmProp != null )
        {
            return new File( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_DNC_QUERY" );
        if ( envVar != null )
        {
            return new File( envVar );
        }

        return new File( glimpseUserCacheDir, "DNC_QUERY" );
    }

    // Log dir locations, after they've been initialized
    private static final Logger logger = getLogger( DncDataPaths.class );
    static
    {
        logInfo( logger, "Default DNC_FLAT user dir: %s", glimpseDncDefaultUserFlatDir );
        logInfo( logger, "Default DNC_FLAT shared dir: %s", glimpseDncDefaultSharedFlatDir );
        logInfo( logger, "Standard DNC_FLAT dir: %s", glimpseDncFlatDir );
        logInfo( logger, "Standard DNC_RENDER dir: %s", glimpseDncRenderDir );
        logInfo( logger, "Standard DNC_QUERY dir: %s", glimpseDncQueryDir );
    }

}
