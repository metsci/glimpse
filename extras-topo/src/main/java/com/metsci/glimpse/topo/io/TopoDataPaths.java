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

import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseSharedDataDir;
import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseUserCacheDir;
import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseUserDataDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;

import java.io.File;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.metsci.glimpse.util.GlimpseDataPaths;

public class TopoDataPaths
{

    /**
     * Most code should NOT use this, but should use {@link TopoDataPaths#glimpseTopoDataDir} instead.
     *
     * This is provided for code that, for some reason, needs to bypass the ( userDir || sharedDir )
     * fallback behavior and access the user dir specifically.
     */
    public static final File glimpseTopoDefaultUserDataDir = new File( glimpseUserDataDir, "TOPO" );

    /**
     * Most code should NOT use this, but should use {@link TopoDataPaths#glimpseTopoDataDir} instead.
     *
     * This is provided for code that, for some reason, needs to bypass the ( userDir || sharedDir )
     * fallback behavior and access the shared dir specifically.
     */
    public static final File glimpseTopoDefaultSharedDataDir = new File( glimpseSharedDataDir, "TOPO" );

    /**
     * Standard dir for TOPO permanent data.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.topo.dataDir}
     * <li>Env var (if defined): {@code GLIMPSE_TOPO_DATA}
     * <li>User default (if dir exists): subdir TOPO of {@link GlimpseDataPaths#glimpseUserDataDir}
     * <li>Shared default: subdir TOPO of {@link GlimpseDataPaths#glimpseSharedDataDir}
     * </ol>
     */
    public static final File glimpseTopoDataDir = glimpseTopoDataDir( );
    private static File glimpseTopoDataDir( )
    {
        String jvmProp = System.getProperty( "glimpse.topo.dataDir" );
        if ( jvmProp != null )
        {
            return new File( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_TOPO_DATA" );
        if ( envVar != null )
        {
            return new File( envVar );
        }

        return ( glimpseTopoDefaultUserDataDir.isDirectory( ) ? glimpseTopoDefaultUserDataDir : glimpseTopoDefaultSharedDataDir );
    }

    public static final ImmutableList<String> topoDataFileRoots = ImmutableList.of( "gebco2020",
                                                                                    "gebco2019",
                                                                                    "gebco2014",
                                                                                    "etopo1_ice" );

    public static final ImmutableList<String> topoDataFileSuffixes = ImmutableList.of( "_c_i2.bin",
                                                                                       "_c.bin",
                                                                                       "_c_f4.flt",
                                                                                       "_c.flt",
                                                                                       "_g_i2.bin",
                                                                                       "_g.bin",
                                                                                       "_g_f4.flt",
                                                                                       "_g.flt" );

    public static File requireTopoDataFile( )
    {
        return requireTopoDataFile( glimpseTopoDataDir );
    }

    public static File requireTopoDataFile( File dir )
    {
        File file = findTopoDataFile( dir );
        if ( file == null )
        {
            throw new RuntimeException( "No topo data file found -- download one (e.g. from https://www.ngdc.noaa.gov/mgg/global/relief/ETOPO1/data/ice_surface/cell_registered/binary/etopo1_ice_c_i2.zip) and extract it to " + dir.getAbsolutePath( ) );
        }
        else
        {
            return file;
        }
    }

    public static File findTopoDataFile( File dir )
    {
        for ( String root : topoDataFileRoots )
        {
            for ( String suffix : topoDataFileSuffixes )
            {
                String filename = root + suffix;
                File file = new File( dir, filename );
                if ( file.isFile( ) && file.canRead( ) )
                {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * Standard dir for TOPO cache data.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.topo.cacheDir}
     * <li>Env var (if defined): {@code GLIMPSE_TOPO_CACHE}
     * <li>Default: subdir TOPO_CACHE of {@link GlimpseDataPaths#glimpseUserCacheDir}
     * </ol>
     */
    public static final File glimpseTopoCacheDir = glimpseTopoCacheDir( );
    private static File glimpseTopoCacheDir( )
    {
        String jvmProp = System.getProperty( "glimpse.topo.cacheDir" );
        if ( jvmProp != null )
        {
            return new File( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_TOPO_CACHE" );
        if ( envVar != null )
        {
            return new File( envVar );
        }

        return new File( glimpseUserCacheDir, "TOPO_CACHE" );
    }

    // Log dir locations, after they've been initialized
    private static final Logger logger = getLogger( TopoDataPaths.class );
    static
    {
        logInfo( logger, "Default TOPO user dir: %s", glimpseTopoDefaultUserDataDir );
        logInfo( logger, "Default TOPO shared dir: %s", glimpseTopoDefaultSharedDataDir );
        logInfo( logger, "Standard TOPO_CACHE dir: %s", glimpseTopoCacheDir );
    }

}
