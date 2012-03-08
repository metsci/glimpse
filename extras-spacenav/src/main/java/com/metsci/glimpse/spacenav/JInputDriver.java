/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.spacenav;


import java.io.File;

import com.metsci.glimpse.util.jnlu.FileUtils;

import static com.metsci.glimpse.util.jnlu.NativeLibUtils.*;


public class JInputDriver
{
    private static boolean initSucceeded = false;

    public static synchronized boolean initSucceeded( )
    {
        return initSucceeded;
    }

    public static synchronized void init( )
    {
        if ( initSucceeded ) return;

        try
        {
            File tempDir = FileUtils.createTempDir( "jinput" );

            // JInput includes in its pom native libs packed in a jar ready to go.
            // The following code needs to be tested on osx/linux and cleaned up a bit.

            if ( onPlatform( "win", "amd64" ) || onPlatform( "win", "x86_64" ) )
            {
                loadLibs( "", tempDir, "jinput-wintab", "jinput-dx8_64", "jinput-raw_64" );
            }

            if ( onPlatform( "win", "x86" ) )
            {
                loadLibs( "", tempDir, "jinput-wintab", "jinput-dx8", "jinput-raw" );
            }

            if ( onPlatform( "linux", "amd64" ) || onPlatform( "linux", "x86_64" ) )
            {
                loadLibs( "", tempDir, "jinput-linux64" );
            }

            if ( onPlatform( "linux", "x86" ) || onPlatform( "linux", "i386" ) )
            {
                loadLibs( "", tempDir, "jinput-linux" );
            }

            if ( onPlatform( "mac", "i386" ) || onPlatform( "mac", "ppc" ) )
            {
                loadLibs( "", tempDir, "jinput-osx" );
            }

            initSucceeded = true;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to initialize", e );
        }
    }
}
