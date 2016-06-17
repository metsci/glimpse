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

import java.io.File;

public class DncDataPaths
{

    /**
     * Most code should NOT use this, but should use {@link DncDataPaths#glimpseDncFlatDir} instead.
     *
     * This is provided for programs that, for some reason, need to bypass the ( userDir || sharedDir )
     * fallback behavior and  access the shared dir specifically.
     */
    public static final File glimpseDncDefaultSharedFlatDir = new File( glimpseSharedDataDir, "DNC_FLAT" );

    /**
     * Most code should NOT use this, but should use {@link DncDataPaths#glimpseDncFlatDir} instead.
     *
     * This is provided for programs that, for some reason, need to bypass the ( userDir || sharedDir )
     * fallback behavior and  access the user dir specifically.
     */
    public static final File glimpseDncDefaultUserFlatDir = new File( glimpseUserDataDir, "DNC_FLAT" );

    /**
     *
     */
    public static final String glimpseDncFlatDirProp = "glimpse.dnc.flatDir";

    /**
     *
     */
    public static final File glimpseDncFlatDir;
    static
    {
        String override = System.getProperty( glimpseDncFlatDirProp );
        if ( override != null )
        {
            glimpseDncFlatDir = new File( override );
        }
        else
        {
            glimpseDncFlatDir = ( glimpseDncDefaultUserFlatDir.isDirectory( ) ? glimpseDncDefaultUserFlatDir : glimpseDncDefaultSharedFlatDir );
        }
    }

    /**
     *
     */
    public static final String glimpseDncRenderDirProp = "glimpse.dnc.renderDir";

    /**
     *
     */
    public static final File glimpseDncRenderDir;
    static
    {
        String override = System.getProperty( glimpseDncRenderDirProp );
        if ( override != null )
        {
            glimpseDncRenderDir = new File( override );
        }
        else
        {
            glimpseDncRenderDir = new File( glimpseUserCacheDir, "DNC_RENDER" );
        }
    }

    /**
     *
     */
    public static final String glimpseDncQueryDirProp = "glimpse.dnc.queryDir";

    /**
     *
     */
    public static final File glimpseDncQueryDir;
    static
    {
        String override = System.getProperty( glimpseDncQueryDirProp );
        if ( override != null )
        {
            glimpseDncQueryDir = new File( override );
        }
        else
        {
            glimpseDncQueryDir = new File( glimpseUserCacheDir, "DNC_QUERY" );
        }
    }

}
