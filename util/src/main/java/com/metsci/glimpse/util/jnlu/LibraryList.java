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
package com.metsci.glimpse.util.jnlu;

import static com.metsci.glimpse.util.jnlu.FileUtils.copy;
import static com.metsci.glimpse.util.jnlu.FileUtils.createTempDir;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LibraryList
{

    public final String resourceDir;
    public final List<String> filenames;

    public LibraryList( String resourceDir, String... filenames )
    {
        this( resourceDir, asList( filenames ) );
    }

    public LibraryList( String resourceDir, List<String> filenames )
    {
        this.resourceDir = resourceDir;
        this.filenames = unmodifiableList( new ArrayList<>( filenames ) );
    }

    public void extractAndLoad( ClassLoader resourceLoader, String tempDirName )
    {
        try
        {
            File tempDir = createTempDir( tempDirName );
            for ( String filename : filenames )
            {
                URL url = resourceLoader.getResource( resourceDir + "/" + filename );
                File file = new File( tempDir, filename );
                copy( url, file );
                System.load( file.getPath( ) );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to extract and load native libraries", e );
        }
    }

}