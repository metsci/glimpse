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
package com.metsci.glimpse.topo.io;

import static com.metsci.glimpse.util.GlimpseDataPaths.glimpseUserCacheDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;

import java.io.File;
import java.util.logging.Logger;

import com.metsci.glimpse.util.GlimpseDataPaths;

public class TopoDataPaths
{
    private static final Logger logger = getLogger( TopoDataPaths.class );

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
    static
    {
        logInfo( logger, "Standard TOPO_CACHE dir: %s", glimpseTopoCacheDir );
    }

}
