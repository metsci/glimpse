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
package com.metsci.glimpse.charts.slippy;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.metsci.glimpse.util.GlimpseDataPaths;

public class SlippyDataPaths
{

    private SlippyDataPaths( )
    {
    }

    /**
     * Standard dir for slippy tile cache. Contains a folder for each tile source.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.slippy.cacheDir}
     * <li>Env var (if defined): {@code GLIMPSE_SLIPPY_CACHE}
     * <li>Default: subdir slippy-cache of {@link GlimpseDataPaths#glimpseUserCacheDir}
     * </ol>
     */
    public static final Path slippyCacheRoot = slippyCacheRootDir( );
    private static Path slippyCacheRootDir( )
    {
        String jvmProp = System.getProperty( "glimpse.slippy.cacheDir" );
        if ( jvmProp != null )
        {
            return Paths.get( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_SLIPPY_CACHE" );
        if ( envVar != null )
        {
            return Paths.get( envVar );
        }
        Path parent = GlimpseDataPaths.glimpseUserCacheDir.toPath( );
        return parent.resolve("slippy-cache");
    }
}
